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
package com.sri.owlseditor.xslt;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * @author Daniel Elenius
 */
public abstract class TransformationPanel extends JPanel 
										  implements ActionListener, ItemListener, TreeSelectionListener {
	protected XSLTComboPanel dataPanel = null;
	protected XSLTComboPanel variablePanel = null;
	protected JPanel rootPanel = null;
	protected XSLTDialog parent;
	
	public abstract void actionPerformed(ActionEvent e);
	public abstract void switchPanel(XSLTNode node);
	public abstract void writeFromPanel2Node(XSLTNode node);
	public abstract void writeFromNode2Panel(XSLTNode node);

	public TransformationPanel(XSLTDialog parent){
		super(new CardLayout());
		this.parent = parent;
		dataPanel = new DataPanel(this);
		variablePanel = new VariablePanel(this);
		rootPanel = new JPanel();
		add(rootPanel, "Root Panel");
		add(dataPanel, "Data Panel");
		add(variablePanel, "Variable Panel");

	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}

	public XSLTComboPanel getDataPanel() {
		return dataPanel;
	}
	public XSLTComboPanel getVariablePanel() {
		return variablePanel;
	}
	
	public void itemStateChanged(ItemEvent e) {
		XSLTNode node = (XSLTNode)parent.getTree().getLastSelectedPathComponent();
		if ( node != null ) writeFromPanel2Node(node);
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path;
		XSLTNode newNode = null;
		XSLTNode lastNode = null;
		path = e.getNewLeadSelectionPath();
		if ( path != null ) newNode = (XSLTNode)path.getLastPathComponent(); 
		path = e.getOldLeadSelectionPath();
		if ( path != null ) lastNode = (XSLTNode)path.getLastPathComponent();
		if ( lastNode != null ) {
			writeFromPanel2Node(lastNode);
		}
		if ( newNode != null ) {
			switchPanel(newNode);
			writeFromNode2Panel(newNode);
		}
	}


}
