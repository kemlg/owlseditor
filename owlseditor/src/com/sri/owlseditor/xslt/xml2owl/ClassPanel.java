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
package com.sri.owlseditor.xslt.xml2owl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Daniel Elenius
 */
public class ClassPanel extends JPanel {
	private String className = "";
	private JTextField classField = null;
	
	public ClassPanel(ActionListener parent) {
		this(parent, "Class Name: ");
	}
	
	public ClassPanel(ActionListener parent, String text) {
		super(new BorderLayout());
		
		Box mainbox = Box.createVerticalBox();
		Box box = Box.createHorizontalBox();
		JLabel label = new JLabel(text);
		box.add(label);
		classField = new JTextField(className);
		classField.addActionListener(parent);
		box.add(classField);
		box.setMaximumSize(new Dimension(1000,25));
		mainbox.add(box);
		mainbox.add(Box.createRigidArea(new Dimension(10, 30)));
		add(mainbox);
	}
	
	public String getClassName() {
		return classField.getText();
	}
	public void setClassName(String s) {
		classField.setText(s);
	}

}