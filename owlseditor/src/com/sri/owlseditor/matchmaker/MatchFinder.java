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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.sri.owlseditor.cmp.tree.OWLSTreeNode;
import com.sri.owlseditor.cmp.tree.PerformNode;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * @author Daniel Elenius
 */
public abstract class MatchFinder extends JFrame {

	private JFrame frame;
	private Container frameContents;
	private JTable matchesTable;
	private TableColumn processColumn;
	private TableColumn matchValueColumn;
	JPanel buttonPanel;
	OWLIndividual parent;   // the parent composite process
	OWLIndividual perform;  // the perform of the current process
	OWLIndividual process;  // the process of the current perform

	OWLModel model;
	OWLObjectProperty processProperty;
	OWLObjectProperty hasInputProperty;
	OWLObjectProperty componentsProperty;
	OWLObjectProperty hasOutputProperty;
	OWLObjectProperty composedOfProperty;
	OWLObjectProperty hasDataFrom;
	OWLObjectProperty producedBinding;
	OWLObjectProperty toParam;
	OWLObjectProperty valueSource;
	OWLObjectProperty fromProcess;
	OWLObjectProperty theVar;
	OWLNamedClass bindingCls;
	OWLNamedClass inputBindingCls;
	OWLNamedClass outputBindingCls;
	OWLNamedClass valueOfCls;
	OWLNamedClass performCls;
	OWLNamedClass produceCls;
	OWLNamedClass choiceCls;
	OWLNamedClass sequenceCls;
	
	
	private class MatchesTableModel extends AbstractTableModel {
	    private List matchResults; 
		private String[] columnNames = {"Matching process", "Match degree", "Source"}; 
	    
		/*
		public MatchesTableModel(List matchResults){
			super();
			this.matchResults = matchResults; 
		}
		*/
		
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
	    
		public int getRowCount() { 
			if (matchResults == null)
				return 0;
			else
				return matchResults.size(); 
		}
	    
	    public int getColumnCount() { return columnNames.length; }
	    
	    public Object getValueAt(int row, int col) {
	        MatchResult mResult = (MatchResult)matchResults.get(row);
	    	if (col == 0){
	        	String processname = mResult.getProcessString(); 
	    		if (processname.equals(parent.getName()))
	    				return processname + " (parent)";
	        	return processname; 
	    	}
	        else if (col == 1)
	        	return Math.round(mResult.getScore() * 100) + "%";
	        else if (col == 2)
	        	return mResult.getProvider().getName();
	        else
	        	return null;
	    }
	    
	    public boolean isCellEditable(int row, int col)
	        { return false; }
	    
	    public void setTableData(List matchResults){
	    	this.matchResults = matchResults;
	    	fireTableDataChanged();
	    }
	    
	    /** Used for the tooltips */
	    public MatchResult getMatchResult(int row){
	    	return (MatchResult)matchResults.get(row);
	    }
	    
	    public MatchResult getSelection(int row){
	    	if (matchResults != null)
	    		return (MatchResult)matchResults.get(row);
	    	else
	    		return null;
	    }
	}

	private MatchesTableModel tableModel;
	
	protected MatchFinder(String windowTitle){
		frame = ComponentFactory.createFrame();
		frame.setTitle(windowTitle);
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frameContents = frame.getContentPane();
		frameContents.setLayout(new BorderLayout());
		
		setupTable();
		frameContents.add(new JScrollPane(matchesTable), BorderLayout.CENTER);
		
		setupButtonPanel();
		frameContents.add(buttonPanel, BorderLayout.SOUTH);
		
	}

	private void setupTable(){
		tableModel = new MatchesTableModel();
		matchesTable = new JTable(tableModel){
		    public String getToolTipText(MouseEvent e) {
		        String tip = null;
		        java.awt.Point p = e.getPoint();
		        int rowIndex = rowAtPoint(p);
		        MatchResult mResult = tableModel.getMatchResult(rowIndex);
		        return mResult.getTextDescription();
		    }
		};
		
		TableColumnModel cmodel = matchesTable.getColumnModel(); 
		cmodel.getColumn(0).setPreferredWidth(200);
		cmodel.getColumn(1).setPreferredWidth(50);
		cmodel.getColumn(2).setPreferredWidth(200);
	}
	
	private void setTableData(List matchResults){
		tableModel.setTableData(matchResults);
	}

	protected MatchResult getSelection(){
		int row = matchesTable.getSelectedRow();
		if (row > -1)
			return tableModel.getSelection(row);
		else
			return null;
	}

	/** Helper method. */
	protected OWLSTreeNode getPerformNode(OWLSTreeNode root){
		for (int i = root.getChildCount()-1; i>=0 ; i--){
			OWLSTreeNode node = (OWLSTreeNode)root.getChildAt(i);
			if (node instanceof PerformNode){
				if (((PerformNode)node).getProcess() == process){
					return node;
				}
			}
			else{
				OWLSTreeNode result = getPerformNode(node);
				if (result != null)
					return result;
			}
		}
		return null;
	}
	
	protected JButton getDetailsButton(){
		class DetailsAction extends AbstractAction{
			public void actionPerformed(ActionEvent e){
				MatchResult mResult = getSelection();
				if (mResult != null)
					DetailsWindow.getInstance(frame).show(mResult);
			}
		}
		JButton detailsButton = new JButton(new DetailsAction());
		detailsButton.setText("Details...");
		return detailsButton;
	}

	protected JButton getHelpButton(){
		class HelpAction extends AbstractAction{
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(frame,
						"This is an unhelpful help message.",
						"Help",
						JOptionPane.PLAIN_MESSAGE);
			}
		}
		JButton helpButton = new JButton(new HelpAction());
		helpButton.setText("Help...");
		return helpButton;
	}

	protected abstract void setupButtonPanel();
	
	
	private void setupClassesAndProperties(){
		processProperty = model.getOWLObjectProperty("process:process");
		hasDataFrom = model.getOWLObjectProperty("process:hasDataFrom");
		producedBinding = model.getOWLObjectProperty("process:producedBinding");
		toParam = model.getOWLObjectProperty("process:toParam");
		valueSource = model.getOWLObjectProperty("process:valueSource");
		fromProcess = model.getOWLObjectProperty("process:fromProcess");
		theVar = model.getOWLObjectProperty("process:theVar");
		hasInputProperty = model.getOWLObjectProperty("process:hasInput");
		hasOutputProperty = model.getOWLObjectProperty("process:hasOutput");
		componentsProperty = model.getOWLObjectProperty("process:components");
		composedOfProperty = model.getOWLObjectProperty("process:composedOf");

		valueOfCls = model.getOWLNamedClass("process:ValueOf");
		bindingCls = model.getOWLNamedClass("process:Binding");
		inputBindingCls = model.getOWLNamedClass("process:InputBinding");
		outputBindingCls = model.getOWLNamedClass("process:OutputBinding");
		performCls = model.getOWLNamedClass("process:Perform");
		produceCls = model.getOWLNamedClass("process:Produce");
		choiceCls = model.getOWLNamedClass("process:Choice");
		sequenceCls = model.getOWLNamedClass("process:Sequence");
	}
	
	public abstract List getMatches(OWLIndividual perform);
	
	/** Brings up the window and shows the matches for
	 * the given perform. The process:process property of the given perform
	 * must be filled.
	 */
	public void show(OWLIndividual perform, OWLIndividual parent){
		this.parent = parent;
		this.perform = perform;
		model = perform.getOWLModel();
		setupClassesAndProperties();
		
 		process = (OWLIndividual)perform.getPropertyValue(processProperty);
		//Matchmaker matchMaker = Matchmaker.getInstance(process.getOWLModel()); 
		//List matches = matchMaker.findMatchingProcesses(process);
		List matches = getMatches(perform);
		
		//System.out.println("Matches for process " + process.getName() +
		//					" in composite process " + parent.getName() + ": " + 
		//					matches);
		
		setTableData(matches);
		
		frame.setVisible(true);
	}
	
}
