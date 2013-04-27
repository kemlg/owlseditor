/*****************************************************************************************
"The contents of this file are subject to the Mozilla Public License  Version 1.1 
(the "License"); you may not use this file except in compliance with the License.  
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License for the specific 
language governing rights and limitations under the License.

The Original Code is OWL-S Editor for Protege.

The Initial Developer of the Original Code is SRI International. 
Portions created by the Initial Developer are Copyright (C) 2004 the Initial Developer.  
All Rights Reserved.
******************************************************************************************/
package com.sri.owlseditor.matchmaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;

/**
 * @author Daniel Elenius
 */
public class DetailsWindow {
	private static DetailsWindow instance = null;

	/* The values are the R,G,B values for the colors */
	private final static Color EQUIVALENT_COLOR = new Color(16, 68, 13);    // dark green
	private final static Color SUBSUMES_COLOR = new Color(64, 145, 60);     // lighter green
	private final static Color SUBSUMED_COLOR = new Color(230, 119, 0);     // dark orange
	private final static Color FAIL_COLOR = new Color(183, 30, 0);  	    // dark red
	private final static Color EXTRA_COLOR = new Color(119, 40, 115);  	    // dark purple
	
	private OWLModel model;
	private OWLDatatypeProperty parameterType;
	
	private JDialog dialog;
	private Container dialogContents;
	private JTable inputDetailsTable;
	private JTable outputDetailsTable;

	private class MatchPairComparator implements Comparator{
		public int compare(Object o1, Object o2){
			MatchPair m1 = (MatchPair)o1;
			MatchPair m2 = (MatchPair)o2;
			return m1.getMatchType() - m2.getMatchType(); 
		}
	}
	
	private MatchPairComparator comparator = new MatchPairComparator();
	
	private class TableRow{
		public String oldParameter;
		public String oldType;
		public String newParameter;
		public String newType;
		public String matchType;
		
		public TableRow(OWLIndividual oldParameter,
						RDFResource oldType,
						String newParameter,
						String newType,
						int matchType){
			if (oldParameter != null)
				this.oldParameter = oldParameter.getName();
			else
				this.oldParameter = "<none>";
			
			if (newParameter != null)
				this.newParameter = newParameter;
			else
				this.newParameter = "<none>";

			if (oldType != null)
				this.oldType = oldType.getName();
			else
				this.oldType = "N/A";
			
			if (newType != null)
				this.newType = newType;
			else
				this.newType = "N/A";
			
			if (matchType == Matchmaker.FAIL)
				this.matchType = "FAIL";
			else if (matchType == Matchmaker.SUBSUMES)
				this.matchType = "SUBSUMES";
			else if (matchType == Matchmaker.SUBSUMED)
				this.matchType = "SUBSUMED";
			else if (matchType == Matchmaker.EQUIVALENT)
				this.matchType = "EQUIVALENT";
			else if (matchType == Matchmaker.EXTRA)
				this.matchType = "EXTRA";
			else
				System.out.println("ERROR! Unknown match type.");
		}
	}
	
	private class DetailsTableModel extends AbstractTableModel {
	    //private ArrayList parameters = new ArrayList();   // list of TableRows
		private List matchPairs;		
		private String[] columnNames = {"Original process parameter",
										"Type",
										"Matching process parameter",
										"Type",
										"Type of match"}; 
	    
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
	    
		public int getRowCount() { 
			if (matchPairs == null)
				return 0;
			else
				return matchPairs.size(); 
		}
	    
	    public int getColumnCount() { return columnNames.length; }
	    
	    public Object getValueAt(int row, int col) {
	        MatchPair pair = (MatchPair)matchPairs.get(row);
	    	if (col == 0){
	    		OWLIndividual oldParameter = pair.getOriginalParameter();
	    		if (oldParameter != null)
					return oldParameter.getName();
				else
					return "<none>";
	    	}
	    	else if (col == 1){
	    		RDFResource oldType = pair.getOldType();
				if (oldType != null)
					return oldType.getName();
				else
					return "N/A";
	    	}
	    	else if (col == 2){
	    		String newParameter = pair.getNewParameter();
	    		if (newParameter != null)
					return newParameter;
				else
					return "<none>";
	    	}
	    	else if (col == 3){
	    		String newType = pair.getNewType();
				if (newType != null)
					return newType;
				else
					return "N/A";
	    	}
	        else if (col == 4){
				int matchType = pair.getMatchType();
	        	if (matchType == Matchmaker.FAIL)
					return "FAIL";
				else if (matchType == Matchmaker.SUBSUMES)
					return "SUBSUMES";
				else if (matchType == Matchmaker.SUBSUMED)
					return "SUBSUMED";
				else if (matchType == Matchmaker.EQUIVALENT)
					return "EQUIVALENT";
				else if (matchType == Matchmaker.EXTRA)
					return "EXTRA";
				else
					System.out.println("ERROR! Unknown match type.");
	        }
			System.out.println("ERROR! Invalid column number.");
	    	return null;
	    }
	    
	    public MatchPair getTableRow(int row){
	    	return (MatchPair)matchPairs.get(row);
	    }
	    
	    public boolean isCellEditable(int row, int col)
	        { return false; }
	    
	    public void setTableData(List matchPairs){
	    	this.matchPairs = matchPairs;
	    	fireTableDataChanged();
	    }

	    /*
	    public void insertRow(OWLIndividual oldParameter,
	    					  String newParameter,
							  String newType,
							  int matchType){
	    	if (parameterType == null){
	    		model = oldParameter.getOWLModel();
	    		parameterType = model.getOWLDatatypeProperty("process:parameterType");
	    	}
	    	
	    	RDFResource oldType = null;
	    	if (oldParameter != null){
	    		DefaultRDFSLiteral oldTypeLiteral = (DefaultRDFSLiteral)oldParameter.
														getPropertyValue(parameterType);
	    		String oldTypeName = model.getResourceNameForURI(oldTypeLiteral.getString());
	    		oldType = model.getRDFResource(oldTypeName);
	    	}
	    	
	    	TableRow row = new TableRow(oldParameter, oldType, newParameter, newType, matchType);
	    	parameters.add(row);
	    	fireTableDataChanged();
	    }
	    */
	}

	private class ColorRenderer extends JLabel implements TableCellRenderer {
		public ColorRenderer() {
			setOpaque(true); //MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus,
													   int row, int column) {
			setBackground(Color.WHITE);
			DetailsTableModel tmodel = (DetailsTableModel)table.getModel();
			MatchPair pair = tmodel.getTableRow(row);
			if (pair.getMatchType() == Matchmaker.FAIL)
					setForeground(FAIL_COLOR);
			else if (pair.getMatchType() == Matchmaker.SUBSUMES)
				setForeground(SUBSUMES_COLOR);
			else if (pair.getMatchType() == Matchmaker.SUBSUMED)
				setForeground(SUBSUMED_COLOR);
			else if (pair.getMatchType() == Matchmaker.EQUIVALENT)
				setForeground(EQUIVALENT_COLOR);
			else
				setForeground(EXTRA_COLOR);
			
			setText((String)value);
			return this;
		}
	}
	
	private ColorRenderer renderer;
	
	private DetailsTableModel inputTableModel;
	private DetailsTableModel outputTableModel;
	
	private JPanel inputsPanel;
	private JPanel outputsPanel;
	
	private DetailsWindow(JFrame parent){
		dialog = new JDialog(parent, true);   // make it modal
		dialog.setTitle("Find Matching Process");
		dialog.setSize(800, 600);
		dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		dialogContents = dialog.getContentPane();
		dialogContents.setLayout(new BoxLayout(dialogContents, BoxLayout.Y_AXIS));
		
		setupTables();
		
		inputsPanel = new JPanel(new BorderLayout());
		inputsPanel.add(new JLabel("Inputs"), BorderLayout.NORTH);
		inputsPanel.add(new JScrollPane(inputDetailsTable));
		
		outputsPanel = new JPanel(new BorderLayout());
		outputsPanel.add(new JLabel("Outputs"), BorderLayout.NORTH);
		outputsPanel.add(new JScrollPane(outputDetailsTable));
		
		dialogContents.add(inputsPanel);
		dialogContents.add(outputsPanel);
	}

	private void setupTables(){
		inputTableModel = new DetailsTableModel();
		outputTableModel = new DetailsTableModel();
		inputDetailsTable = new JTable(inputTableModel);
		outputDetailsTable = new JTable(outputTableModel);
		inputDetailsTable.setDefaultRenderer(Object.class, new ColorRenderer());
		outputDetailsTable.setDefaultRenderer(Object.class, new ColorRenderer());
		
		TableColumn icolumn = null;
		TableColumn ocolumn = null;
		for (int i = 0; i < 5; i++) {
		    icolumn = inputDetailsTable.getColumnModel().getColumn(i);
		    ocolumn = outputDetailsTable.getColumnModel().getColumn(i);
		    if (i < 4) {
		        icolumn.setPreferredWidth(150);
		        ocolumn.setPreferredWidth(150);
		    } else {
		        icolumn.setPreferredWidth(50);   // last column should be smaller
		        ocolumn.setPreferredWidth(50);
		    }
		}
	}
	
	public static DetailsWindow getInstance(JFrame parent){
		if (instance == null)
			instance = new DetailsWindow(parent);
		return instance;
	}
	
	public void show(MatchResult match){
		Iterator it;

		/* Initialize the table models */
		inputTableModel = new DetailsTableModel();
		outputTableModel = new DetailsTableModel();
		inputDetailsTable.setModel(inputTableModel);
		outputDetailsTable.setModel(outputTableModel);
		
		/* Add all the inputs */
		ArrayList inputMatches = new ArrayList(match.getInputMatches());
		inputMatches.addAll(match.getExtraInputs());
		Collections.sort(inputMatches, comparator);
		inputTableModel.setTableData(inputMatches);
		
		/* Add all the outputs */
		ArrayList outputMatches = new ArrayList(match.getOutputMatches());
		inputMatches.addAll(match.getExtraOutputs());
		Collections.sort(outputMatches, comparator);
		outputTableModel.setTableData(outputMatches);
		
		dialog.setVisible(true);
	}
	
}
