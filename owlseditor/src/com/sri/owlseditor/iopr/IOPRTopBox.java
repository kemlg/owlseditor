// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License.  You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
// License for the specific language governing rights and limitations under
// the License.
//
// The Original Code is OWL-S Editor for Protege.
//
// The Initial Developer of the Original Code is SRI International.
// Portions created by the Initial Developer are Copyright (C) 2004 the
// Initial Developer.  All Rights Reserved.


package com.sri.owlseditor.iopr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.event.ClassAdapter;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import com.sri.owlseditor.consistency.ConsistencyCheckDisplay;
import com.sri.owlseditor.util.OWLSIcons;

public class IOPRTopBox extends JPanel implements ItemListener {

    private JButton           consistencyCheckButton;
    private JButton           copyLeftToRightButton;
    private JButton           copyRightToLeftButton;
    private JComboBox         first;
    private JComboBox         second;
    private OWLModel  	      _okb;
    private OWLIndividual     firstSelected;
    private OWLIndividual     secondSelected;
    public int desiredWidth = 0;
    private IOPRSelector	m_ioprList;
    private OWLNamedClass profileCls;
    private OWLNamedClass processCls;
    private String m_origName;
    private String m_newName;

    private HashSet comboListeners = new HashSet();
    
    IOPRTopBox (OWLModel okb, IOPRSelector ioprList) {
    	_okb = okb;
	m_ioprList = ioprList;

	profileCls = _okb.getOWLNamedClass ("profile:Profile");
	processCls = _okb.getOWLNamedClass ("process:Process");

	IOPRConsistencyCheck.getInstance (_okb, m_ioprList);

	setLayout (new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();

	first  = ComponentFactory.createComboBox();
	second = ComponentFactory.createComboBox();
	desiredWidth = populateCombos (0);

	JLabel leftLabel = new JLabel (" Left CheckBoxes");
	first.addItemListener (this);
	first.setMaximumSize (new Dimension (500, 20));

	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.anchor = GridBagConstraints.LINE_START;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
	add (leftLabel, gbc);

	JLabel rightLabel = new JLabel (" Right CheckBoxes ");
	second.addItemListener (this);
	second.setMaximumSize (new Dimension (500, 20));

	gbc.gridy = 1;
	add (rightLabel, gbc);

	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	add (first, gbc);

	gbc.gridy = 1;
	add (second, gbc);

	JPanel jj = new JPanel (new GridBagLayout());
	//GridBagConstraints gbcc = new GridBagConstraints();
	//gbcc.weightx = .5;

	//consistencyCheckButton = new JButton ("Consistency");
	//consistencyCheckButton.setForeground (Color.red);

        JToolBar toolbar = ComponentFactory.createToolBar();

	// iopr consistency check
	consistencyCheckButton = new JButton (OWLSIcons.getConsistencyCheckIcon());
    	consistencyCheckButton.setToolTipText ("IOPR Consistency Check");
	toolbar.add (consistencyCheckButton);

	ActionListener consistencyListener = new ActionListener() {
		public void actionPerformed (ActionEvent evt) {
		    Vector vIOPRProblems = new Vector();
		    IOPRConsistencyCheck.getInstance(_okb, null).checkIOPRProcessAndProfiles (vIOPRProblems);
		    IOPRConsistencyCheck.getInstance(_okb, null).checkIOPRProcessParameterType (vIOPRProblems);

		    if (vIOPRProblems.size() == 0)
			JOptionPane.showMessageDialog (null, "IOPR Consistency Check Completed - No Inconsistencies Found");
		    else
			ConsistencyCheckDisplay.getInstance().updateDisplay (vIOPRProblems);
		}
	    };

	consistencyCheckButton.addActionListener (consistencyListener);
	//jj.add (consistencyCheckButton, gbcc);

	copyLeftToRightButton = new JButton (OWLSIcons.getCopyLeftToRightIcon());
    	copyLeftToRightButton.setToolTipText ("Copy Left to Right");
	//copyLeftToRightButton.setForeground (Color.blue);
	toolbar.add (copyLeftToRightButton);

	ActionListener copyLeftToRightListener = new ActionListener() {
		public void actionPerformed (ActionEvent evt) {
		    if (firstSelected == null || secondSelected == null)
			return;

		    String msg = "Copy all parameter settings from " + firstSelected.getName() +
			" to " + secondSelected.getName() + ".\nNote: this action will modify the knowledgebase.";

		    if (JOptionPane.showConfirmDialog (null, msg, "", JOptionPane.OK_CANCEL_OPTION) !=
			JOptionPane.CANCEL_OPTION)
			m_ioprList.copyPropertyValue (firstSelected, secondSelected, true);
		}
	    };
	
	copyLeftToRightButton.addActionListener (copyLeftToRightListener);
	copyLeftToRightButton.setEnabled (false);
	//jj.add (copyLeftToRightButton, gbcc);

	copyRightToLeftButton = new JButton (OWLSIcons.getCopyRightToLeftIcon());
    	copyRightToLeftButton.setToolTipText ("Copy Right to Left");
	//copyRightToLeftButton.setForeground (Color.blue);
	toolbar.add (copyRightToLeftButton);
	
	ActionListener copyRightToLeftListener = new ActionListener() {
		public void actionPerformed (ActionEvent evt) {
		    if (firstSelected == null || secondSelected == null)
			return;

		    String msg = "Copy all parameter settings from " + secondSelected.getName() +
			" to " + firstSelected.getName() + ".\nNote: this action will modify the knowledgebase.";

		    if (JOptionPane.showConfirmDialog (null, msg, "", JOptionPane.OK_CANCEL_OPTION) !=
			JOptionPane.CANCEL_OPTION)
			m_ioprList.copyPropertyValue (firstSelected, secondSelected, false);
		}
	    };
	
	copyRightToLeftButton.addActionListener (copyRightToLeftListener);
	copyRightToLeftButton.setEnabled (false);
	//jj.add (copyRightToLeftButton, gbcc);

	gbc.gridx = 0;
	gbc.gridy = 2;
	//gbc.gridwidth = 3;
	add (toolbar, gbc);

	_okb.addModelListener(new ModelAdapter() {
		public void individualCreated(RDFResource inst) {
		    if (inst.hasRDFType(processCls, true))
		    	adjustCombosForChange();
		}

		public void individualDeleted(RDFResource inst) {
		    adjustCombosForChange();
		}

		public void resourceNameChanged (RDFResource resource, String origName) {
			if (resource.hasRDFType (profileCls, true) ||
			    resource.hasRDFType (processCls, true)) {
			    
				System.out.println("Process or profile renamed");
				
				m_origName = origName;
			    m_newName  = ((OWLIndividual) resource).getName();
			    adjustCombosForRename();
		    }
		}
	});

	//profileCls = _okb.getOWLNamedClass ("profile:Profile");
	/*
	profileCls.addClassListener (new ClassAdapter() {
		public void instanceAdded (RDFSClass cls, RDFResource inst) {
		    adjustCombosForChange();
		}

		public void instanceRemoved (RDFSClass cls, RDFResource inst) {
		    adjustCombosForChange();
		}
	    });
	    */
    }


    private void adjustCombosForChange () {
	Object firstSel  = first.getSelectedItem();
	Object secondSel = second.getSelectedItem();

	populateCombos (0);

	first.setSelectedItem (firstSel);
	second.setSelectedItem (secondSel);
    }


    private void adjustCombosForRename () {
	Object firstSel  = first.getSelectedItem();
	Object secondSel = second.getSelectedItem();

	populateCombos (0);

	if (firstSel.equals (m_origName))
	    firstSel = m_newName;
	first.setSelectedItem (firstSel);

	if (secondSel.equals (m_origName))
	    secondSel = m_newName;
	second.setSelectedItem (secondSel);
    }


    public void itemStateChanged (ItemEvent event) {
    	int state = event.getStateChange();
    	OWLIndividual item = _okb.getOWLIndividual ((String) event.getItem());

    	if (event.getStateChange() == ItemEvent.SELECTED) {
	    if ((JComboBox)event.getItemSelectable() == first) {
		firstSelected = item;
		Iterator it = comboListeners.iterator();
		while (it.hasNext()){
		    ComboListener listener = (ComboListener) it.next();
		    listener.leftComboSelectionChanged (item);
		}
	    }
	    else {
		secondSelected = item;
		Iterator it = comboListeners.iterator();
		while (it.hasNext()) {
		    ComboListener listener = (ComboListener) it.next();
		    listener.rightComboSelectionChanged (item);
		}
	    }

	    copyLeftToRightButton.setEnabled (firstSelected != null && secondSelected != null);
	    copyRightToLeftButton.setEnabled (firstSelected != null && secondSelected != null);
    	}
    }


    public OWLIndividual getLeftSelection(){
    	return firstSelected;
    }
    

    public OWLIndividual getRightSelection(){
    	return secondSelected;
    }
    

    public void addComboListener(ComboListener listener){
    	comboListeners.add(listener);
    }


    public void removeComboListener(ComboListener listener){
    	comboListeners.remove(listener);
    }
    

    private int populateCombos (int width) {
	//OWLNamedClass    processCls   = _okb.getOWLNamedClass("process:Process");
	//OWLNamedClass    profileCls = _okb.getOWLNamedClass("profile:Profile");
	FontMetrics fm = getFontMetrics (getFont());

	first.removeAllItems();
	second.removeAllItems();
 
	if (processCls != null) {
	    Collection  processes = processCls.getInstances(true);

	    if (processes != null) {
		ArrayList instList = new ArrayList();
		Iterator it = processes.iterator();

		while (it.hasNext()) {
		    OWLIndividual process = (OWLIndividual) it.next();
		    instList.add (process.getName());
		    width = Math.max (fm.stringWidth (process.getName()), width);
		}

		Collections.sort (instList);
		for (int i = 0; i < instList.size(); i++) {
		    first.addItem (instList.get(i));
		    second.addItem (instList.get(i));
		}
	    }
	}

	first.insertItemAt  ("--- Processes ---", 0);
	second.insertItemAt ("--- Processes ---", 0);

	first.addItem ("--- Profiles ---");
	second.addItem ("--- Profiles ---");

	if (profileCls != null) {
	    Collection profiles = profileCls.getInstances (true);

	    if (profiles != null) {
		ArrayList instList = new ArrayList();
		Iterator it = profiles.iterator();

		while (it.hasNext()) {
		    OWLIndividual profile = (OWLIndividual) it.next();
		    instList.add (profile.getName());
		    width = Math.max (fm.stringWidth (profile.getName()), width);
		}

		Collections.sort (instList);
		for (int i = 0; i < instList.size(); i++) {
		    first.addItem (instList.get(i));
		    second.addItem (instList.get(i));
		}

		/*
		OWLObjectProperty has_process = _okb.getOWLObjectProperty ("profile:has_process");
		while (it.hasNext()) {
		    OWLIndividual profile = (OWLIndividual) it.next();
		    Collection associatedProcesses = profile.getPropertyValues(has_process);
		    Iterator ii = associatedProcesses.iterator();
		    while (ii.hasNext()) {
			OWLIndividual associatedProcess =(OWLIndividual) ii.next();
		    }
		    instList.add (associatedProcess.getName());
		    width = Math.max (fm.stringWidth (inst.getName()), width);
		}

		Collections.sort (instList);
		for (int i = 0; i < instList.size(); i++) {
		    first.addItem (instList.get(i));
		    second.addItem (instList.get(i));
		}
		*/
	    }
	}

	first.setSelectedIndex (0);
	second.setSelectedIndex (0);

	return (width + fm.stringWidth ("Right CheckBoxes") + 20);
    }
}
