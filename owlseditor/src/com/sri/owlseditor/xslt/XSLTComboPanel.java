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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

public class XSLTComboPanel extends XSLTPanel implements ActionListener {
	private int totalXSLTFunctions;
	private List parameterBox;
	private JComboBox xsltFunctions = null;
	protected JTextField nameField;
	private Vector transFunc;
	private JPanel paramcontainer;
	private JButton addparam, delparam;
	private ActionListener parent;
	
	public XSLTComboPanel(ActionListener par) {
		parent = par;

		transFunc = XSLTFunction.getXSLTFunctions();
		totalXSLTFunctions = transFunc.size();

		setupGUI();
	}
	
	/** Separated out because RDFAttributePanel needs to override this. */
	protected Box getTopBox(){
		Box box = Box.createHorizontalBox();
		JLabel label = new JLabel("Name: ");
		box.add(label);
		nameField = new JTextField("");
		nameField.addActionListener(parent);
		box.add(nameField);
		box.setMaximumSize(new Dimension(1000,25));
		return box;
	}
	
	protected void setupGUI(){
		setLayout(new BorderLayout());

		Box mainbox = Box.createVerticalBox();

		Box box = getTopBox();
		mainbox.add(box);
		mainbox.add(Box.createRigidArea(new Dimension(10, 30)));

		Box functionlist = Box.createHorizontalBox();
	    functionlist.add(new JLabel("XSLT Function: "));
	    xsltFunctions = new JComboBox(transFunc);
	    xsltFunctions.setEditable(false);
	    functionlist.add(xsltFunctions);
	    functionlist.setMaximumSize(new Dimension(1000,25));
	    mainbox.add(Box.createRigidArea(new Dimension(10, 10)));
	    mainbox.add(functionlist);
	    mainbox.add(Box.createRigidArea(new Dimension(10, 10)));
	    xsltFunctions.addActionListener(this);
	    JToolBar toolbar = new JToolBar();
	    addparam = new JButton(OWLIcons.getAddIcon());
	    addparam.setActionCommand("add_parameter");
	    addparam.setBorderPainted(false);
	    addparam.addActionListener(this);
	    addparam.setToolTipText("Add More Paramater");
		addparam.setEnabled(false);
		delparam = new JButton(OWLIcons.getRemoveIcon());
		delparam.setActionCommand("remove_parameter");
		delparam.setBorderPainted(false);
		delparam.addActionListener(this);
	    delparam.setToolTipText("Remove Extra Paramater");
	    delparam.setEnabled(false);
	    toolbar.setFloatable(false);
	    toolbar.add(addparam);
	    toolbar.add(delparam);
	    mainbox.add(toolbar,BorderLayout.EAST);
	    mainbox.add(Box.createRigidArea(new Dimension(10, 10)));

		XSLTFunction defaultFunc = XSLTFunction.createDefaultFunction();
		int requiredParameters = defaultFunc.getMinParameters();

		parameterBox = new ArrayList();
		paramcontainer = new JPanel();
		paramcontainer.setLayout(new BoxLayout(paramcontainer, BoxLayout.PAGE_AXIS));
		paramcontainer.add(Box.createRigidArea(new Dimension(10, 10)));
		int i;
		for (i=0;i<defaultFunc.getMinParameters();i++) {
			box = addParameterBox();
			parameterBox.add(box);
			paramcontainer.add(box);
		}
		mainbox.add(new JScrollPane(paramcontainer));
		xsltFunctions.addActionListener(this);
		add(mainbox);
	}
	
	private Box addParameterBox() {
		Box b = Box.createVerticalBox();
		Box box = Box.createHorizontalBox();
		int x = parameterBox.size() + 1;
		JLabel label = new JLabel(" Parameter " + x + " : ");
		box.add(label);
		JComboBox inputList = new JComboBox();
		JComboBox firstone = getParameterJComboBox(0);
		if ( firstone != null ) 
			for (int i=0;i<firstone.getItemCount();i++) inputList.addItem(firstone.getItemAt(i));
		inputList.setEditable(true);
		box.add(inputList);
		box.add(Box.createRigidArea(new Dimension(5, 5)));
		box.setMaximumSize(new Dimension(1000,25));
		b.add(box);
		b.add(Box.createRigidArea(new Dimension(10, 10)));
		return b;
	}
	public void addParameter(Object p) {
		for (int j=0;j<getParameterNumbers();j++) 
			getParameterJComboBox(j).addItem(p.toString());
	}
	public void removeParameter(Object p) {
		for (int j=0;j<getParameterNumbers();j++)	
			getParameterJComboBox(j).removeItem(p.toString());
	}
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("comboBoxChanged") ) {
			XSLTFunction func = (XSLTFunction)((JComboBox)e.getSource()).getSelectedItem();
			int RequiredParameters = func.getParameterNumbers();
			if ( func.getParameterNumbers()>func.getMinParameters() ) delparam.setEnabled(true); else delparam.setEnabled(false);
			if ( func.getParameterNumbers()<func.getMaxParameters() ) addparam.setEnabled(true); else addparam.setEnabled(false);
			if ( RequiredParameters > getParameterNumbers() ) { // Add more Parameters.
				for (int i = getParameterNumbers();i<RequiredParameters;i++) {
					Box box = addParameterBox();
					parameterBox.add(box);
					paramcontainer.add(box);
				}
			}
			if ( RequiredParameters < getParameterNumbers() ) { // Delete Extra Parameters
				for (int i = getParameterNumbers();i>RequiredParameters;i--) {
					Box box = (Box)parameterBox.get(i-1);
					parameterBox.remove(box);
					paramcontainer.remove(box);
				}
			}
			for (int i=0;i<RequiredParameters;i++) getParameterJComboBox(i).setSelectedIndex(0);
		} else if(e.getActionCommand().equals("add_parameter")) {
			Box box = addParameterBox();
			parameterBox.add(box);
			paramcontainer.add(box);
		} else if(e.getActionCommand().equals("remove_parameter")) {
			Box box = (Box)parameterBox.get(parameterBox.size()-1);
			parameterBox.remove(box); //remove last one
			paramcontainer.remove(box);
		}
		XSLTFunction func = (XSLTFunction)xsltFunctions.getSelectedItem();
		if ( getParameterNumbers()>func.getMinParameters() ) delparam.setEnabled(true); else delparam.setEnabled(false);
		if ( getParameterNumbers()<func.getMaxParameters() ) addparam.setEnabled(true); else addparam.setEnabled(false);
		updateUI();
	}
	/** These parameters can be Inputs/Locals or WSDL message part, depending on
	 * what this XSLTPanel is for. */ 
	public void addParameters(Collection params) {
		Iterator it = params.iterator();
		while ( it.hasNext() ) addParameter(it.next());
	}
	public String getName() {
		return nameField.getText();
	}
	public void setName(String s) {
		nameField.setText(s);
	}
	public int getParameterNumbers() {
		return parameterBox.size();
	}
	public JComboBox getParameterJComboBox(int j) {
		if ( j<0 || j>=getParameterNumbers() ) return null;
		Box b = (Box)parameterBox.get(j);
		Box box = (Box)b.getComponent(0);
		for (int i=0;i<box.getComponentCount();i++) {
			if ( box.getComponent(i) instanceof JComboBox ) {
				return ((JComboBox)box.getComponent(i));
			}
		}
		return null;
	}
	public XSLTFunction getXSLTFunction() {
		XSLTFunction function = (XSLTFunction)xsltFunctions.getSelectedItem();
		function.resetParameters();
		for (int i=0;i<getParameterNumbers();i++)
			function.setParameter(i,getParameterJComboBox(i).getEditor().getItem().toString());
		return function;
	}
	public void setXSLTFunction(XSLTFunction function) {
		int i;
		for (i=0;i<totalXSLTFunctions;i++) {
			if ( function.getFunctionName().equals(((XSLTFunction)transFunc.elementAt(i)).getFunctionName()) ) {
				xsltFunctions.setSelectedIndex(i);
				break;
			}
		}
		for (i=0;i<function.getParameterNumbers();i++)
			getParameterJComboBox(i).setSelectedItem(function.getParameter(i));
	}
	protected Collection generateInputList() {
		return null;
	}
}