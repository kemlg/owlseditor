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
package com.sri.owlseditor.xslt.owl2xml;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;

import com.sri.owlseditor.xslt.DataNode;
import com.sri.owlseditor.xslt.RootNode;
import com.sri.owlseditor.xslt.TransformationPanel;
import com.sri.owlseditor.xslt.VariableNode;
import com.sri.owlseditor.xslt.XSLTNode;
import com.sri.owlseditor.xslt.XSLTComboPanel;

public class O2WParameterPanel extends TransformationPanel {
	private ElementPanel elementPanel = null;
	private XSLTComboPanel attributePanel = null;
	private O2WXSLTDialog parent;

	public O2WParameterPanel(O2WXSLTDialog par) {
		super(par);
		parent = par;
		elementPanel = new ElementPanel(this);
		attributePanel = new AttributePanel(this);
		add(elementPanel, "Element Panel");
		add(attributePanel, "Attribute Panel");
	}

	public ElementPanel getElementPanel() {
		return elementPanel;
	}

	public XSLTComboPanel getAttributePanel() {
		return attributePanel;
	}

	public void switchPanel(XSLTNode node) {
		CardLayout cl = (CardLayout) getLayout();
		if (node instanceof RootNode) {
			cl.show(this, "Root Panel");
		} else if (node instanceof ElementNode) {
			cl.show(this, "Element Panel");
		} else if (node instanceof DataNode) {
			cl.show(this, "Data Panel");
		} else if (node instanceof VariableNode) {
			cl.show(this, "Variable Panel");
		} else if (node instanceof AttributeNode) {
			cl.show(this, "Attribute Panel");
		}
	}

	public void writeFromPanel2Node(XSLTNode node) {
		if (node instanceof DataNode) {
			node.setXSLTFunction(dataPanel.getXSLTFunction());
		} else if (node instanceof ElementNode) {
			node.setNodeName(elementPanel.getName());
		} else if (node instanceof VariableNode) {
			node.setNodeName(variablePanel.getName());
			node.setXSLTFunction(variablePanel.getXSLTFunction());
		} else if (node instanceof AttributeNode) {
			node.setNodeName(attributePanel.getName());
			node.setXSLTFunction(attributePanel.getXSLTFunction());
		}
	}

	public void writeFromNode2Panel(XSLTNode node) {
		if (node instanceof DataNode) {
			dataPanel.setXSLTFunction(node.getXSLTFunction());
		} else if (node instanceof ElementNode) {
			elementPanel.setName(node.getNodeName());
		} else if (node instanceof VariableNode) {
			variablePanel.setName(node.getNodeName());
			variablePanel.setXSLTFunction(node.getXSLTFunction());
		} else if (node instanceof AttributeNode) {
			attributePanel.setName(node.getNodeName());
			attributePanel.setXSLTFunction(node.getXSLTFunction());
		}
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("action performed");

		XSLTNode node = (XSLTNode) parent.getTree()
				.getLastSelectedPathComponent();
		if (node != null) {
			if (e.getSource() instanceof JTextField) {
				if (node instanceof VariableNode) {
					String oldname = "$" + node.getNodeName();
					variablePanel.removeParameter(oldname);
					attributePanel.removeParameter(oldname);
					dataPanel.removeParameter(oldname);

					String newname = "$"
							+ ((JTextField) e.getSource()).getText();

					System.out.println("Parameter name is " + newname);

					variablePanel.addParameter(newname);
					attributePanel.addParameter(newname);
					dataPanel.addParameter(newname);
				}
				((DefaultTreeModel) parent.getTree().getModel()).reload();
			}
			writeFromPanel2Node(node);
		}
	}

}