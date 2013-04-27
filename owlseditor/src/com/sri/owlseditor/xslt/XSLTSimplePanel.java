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
import java.awt.LayoutManager;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * @author Daniel Elenius
 */
public class XSLTSimplePanel extends XSLTPanel {
	String name = "";
	JTextField nameField = null;
	
	public XSLTSimplePanel(ActionListener parent, String text) {
		super(new BorderLayout());
		
		Box mainbox = Box.createVerticalBox();
		Box box = Box.createHorizontalBox();
		JLabel label = new JLabel(text);
		box.add(label);
		nameField = new JTextField(name);
		nameField.addActionListener(parent);
		box.add(nameField);
		box.setMaximumSize(new Dimension(1000,25));
		mainbox.add(box);
		mainbox.add(Box.createRigidArea(new Dimension(10, 30)));
		add(mainbox);
	}
	
	public String getName() {
		return nameField.getText();
	}
	public void setName(String s) {
		nameField.setText(s);
	}

}
