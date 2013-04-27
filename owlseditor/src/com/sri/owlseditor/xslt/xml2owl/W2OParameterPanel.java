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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;

import com.sri.owlseditor.xslt.DataNode;
import com.sri.owlseditor.xslt.RootNode;
import com.sri.owlseditor.xslt.TransformationPanel;
import com.sri.owlseditor.xslt.VariableNode;
import com.sri.owlseditor.xslt.XSLTNode;

public class W2OParameterPanel extends TransformationPanel implements ActionListener {
	private W2OXSLTDialog parent;
	private RDFNamePanel namePanel;
	private RDFResourcePanel resourcePanel;
	private ClassPanel classPanel;
	private DatatypePropertyPanel datatypePropPanel;
	private ObjectPropertyPanel objectPropPanel;
	
	public W2OParameterPanel(W2OXSLTDialog par) {
		super(par);
		parent = par;
		namePanel = new RDFNamePanel(this);
		resourcePanel = new RDFResourcePanel(this);
		classPanel = new ClassPanel(this);
		datatypePropPanel = new DatatypePropertyPanel(this);
		objectPropPanel = new ObjectPropertyPanel(this);
		
		add(classPanel, "Class Panel");
		add(namePanel, "RDF Name Panel");
		add(resourcePanel, "RDF Resource Panel");
		add(datatypePropPanel, "DatatypeProperty Panel");
		add(objectPropPanel, "ObjectProperty Panel");
	}

	public ClassPanel getClassPanel() {
		return classPanel;
	}

	public RDFResourcePanel getRDFResourcePanel() {
		return resourcePanel;
	}

	public RDFNamePanel getRDFNamePanel() {
		return namePanel;
	}
	
	public DatatypePropertyPanel getDatatypePropertyPanel(){
		return datatypePropPanel;
	}

	public ObjectPropertyPanel getObjectPropertyPanel(){
		return objectPropPanel;
	}

	public void switchPanel(XSLTNode node) {
		CardLayout cl = (CardLayout)getLayout();
		if (node instanceof RootNode){
			cl.show(this,"Root Panel");
		}
		else if (node instanceof ClassNode){
			cl.show(this,"Class Panel");
		}
		else if (node instanceof DataNode){
			cl.show(this,"Data Panel");
		}
		else if (node instanceof VariableNode){
			cl.show(this,"Variable Panel");
		}
		else if (node instanceof RDFNameNode){
			cl.show(this,"RDF Name Panel");
		}
		else if (node instanceof RDFResourceNode){
			cl.show(this,"RDF Resource Panel");
		}
		else if (node instanceof DatatypePropertyNode){
			cl.show(this,"DatatypeProperty Panel");
		}
		else if (node instanceof ObjectPropertyNode){
			cl.show(this,"ObjectProperty Panel");
		}
	}
	
	public void writeFromPanel2Node(XSLTNode node) {
		if (node instanceof DataNode){
			node.setXSLTFunction(dataPanel.getXSLTFunction());
		}
		else if (node instanceof ClassNode){
			node.setNodeName(classPanel.getClassName());
		}
		else if (node instanceof DatatypePropertyNode){
			node.setNodeName(datatypePropPanel.getName());
		}
		else if (node instanceof ObjectPropertyNode){
			node.setNodeName(objectPropPanel.getName());
		}
		else if (node instanceof VariableNode){
			node.setNodeName(variablePanel.getName());
			node.setXSLTFunction(variablePanel.getXSLTFunction());
		}
		else if (node instanceof RDFNameNode){
			node.setNodeName(namePanel.getName());
			node.setXSLTFunction(namePanel.getXSLTFunction());
		}
		else if (node instanceof RDFResourceNode){
			node.setNodeName(resourcePanel.getName());
			node.setXSLTFunction(resourcePanel.getXSLTFunction());
		}
	}
	
	public void writeFromNode2Panel(XSLTNode node) {
		if (node instanceof DataNode){
			dataPanel.setXSLTFunction(node.getXSLTFunction());
		}
		else if (node instanceof ClassNode){
			classPanel.setClassName(node.getNodeName());
		}
		else if (node instanceof ObjectPropertyNode){
			objectPropPanel.setName(node.getNodeName());
		}
		else if (node instanceof DatatypePropertyNode){
			datatypePropPanel.setName(node.getNodeName());
		}
		else if (node instanceof VariableNode){
			variablePanel.setName(node.getNodeName());
			variablePanel.setXSLTFunction(node.getXSLTFunction());
		}
		else if (node instanceof RDFNameNode){
			namePanel.setName(node.getNodeName());
			namePanel.setXSLTFunction(node.getXSLTFunction());
		}
		else if (node instanceof RDFResourceNode){
			resourcePanel.setName(node.getNodeName());
			resourcePanel.setXSLTFunction(node.getXSLTFunction());
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		XSLTNode node = (XSLTNode)parent.getTree().getLastSelectedPathComponent();
		if ( node != null ) {
			if ( e.getSource() instanceof JTextField ) {
				if ( node instanceof VariableNode ) {
					String oldname = node.getNodeName();
					variablePanel.removeParameter(oldname);
					namePanel.removeParameter(oldname);
					resourcePanel.removeParameter(oldname);
					dataPanel.removeParameter(oldname);

					String newname = ((JTextField)e.getSource()).getText();
					variablePanel.addParameter(newname);
					namePanel.addParameter(newname);
					resourcePanel.addParameter(newname);
					dataPanel.addParameter(newname);
				}
				((DefaultTreeModel)parent.getTree().getModel()).reload();
			}
			writeFromPanel2Node(node);
		}
	}
	
}