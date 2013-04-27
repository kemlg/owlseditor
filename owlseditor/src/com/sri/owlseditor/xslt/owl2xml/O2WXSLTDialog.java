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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import com.sri.owlseditor.util.OWLSIcons;
import com.sri.owlseditor.util.OWLUtils;
import com.sri.owlseditor.xslt.DataNode;
import com.sri.owlseditor.xslt.RootNode;
import com.sri.owlseditor.xslt.VariableNode;
import com.sri.owlseditor.xslt.XMLTreeRenderer;
import com.sri.owlseditor.xslt.XSLTDialog;
import com.sri.owlseditor.xslt.XSLTException;
import com.sri.owlseditor.xslt.XSLTNode;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;

public class O2WXSLTDialog extends XSLTDialog {
	private String wsdlparturi = null;
	private String wsdlmessageuri = null;
	private Part wsdlInputMessagePart;
	private Message wsdlInputMessage;
	private OWLIndividual atomicProcess = null;
	private JButton createTag, createData, createVariable, createAttribute, deleteNode;
	
	public O2WXSLTDialog(OWLModel okb, OWLIndividual aP, String wsdl, String wsdlM, OWLIndividual owlInst) 
						throws XSLTException{
		super(okb, wsdl);
		atomicProcess = aP;
		wsdlmessageuri = wsdlM;
		m_owlInst = owlInst;
		// Do some sanity checking
		if (wsdl == null)
			throw new XSLTException("No WSDL document defined.");
		if (wsdlM == null)
			throw new XSLTException("No WSDL Input Message defined.");
		OWLDatatypeProperty wsdlMessagePartProp = okb.getOWLDatatypeProperty ("grounding:wsdlMessagePart");
		RDFSLiteral wsdlpart = (RDFSLiteral)owlInst.getPropertyValue(wsdlMessagePartProp);
		if (wsdlpart == null)
			throw new XSLTException("No WSDL Input Message Part defined.");
		wsdlparturi = wsdlpart.getString();
		if (wsdlparturi == null)
			throw new XSLTException("No WSDL Input Message Part defined.");
		wsdlInputMessage = getWsdlMessage(wsdlM);
		if (wsdlInputMessage == null)
			throw new XSLTException("WSDL Input Message " + wsdlM + " not found.");
		// WSDL4J sucks. It expects just the local part of the message part uri.
		wsdlInputMessagePart = wsdlInputMessage.getPart(wsdlparturi.substring(wsdlparturi.indexOf('#')+1));
		if (wsdlInputMessagePart == null)
			throw new XSLTException("WSDL Input Message Part " + wsdlparturi + " not found.");
		
		prepareTree();
		setupGUI();		
		Collection inputs = generateParameterNameList(generateParameterList());
		createTranslationPanel();
		O2WParameterPanel pp = (O2WParameterPanel)transPanel; 
		pp.getAttributePanel().addParameters(inputs);
		pp.getDataPanel().addParameters(inputs);
		pp.getVariablePanel().addParameters(inputs);
		tree.addTreeSelectionListener((O2WParameterPanel)transPanel);
		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new XMLTreeRenderer());		
	}

	protected void setupGUI() {
		super.setupGUI();
		JPanel typesPanel = ComponentFactory.createPanel();
		typesPanel.setLayout(new BoxLayout(typesPanel, BoxLayout.Y_AXIS));
		
		JPanel targetWsdlTypePanel = new JPanel(new BorderLayout());
		JLabel targetWsdlTypeLabel = ComponentFactory.createLabel("Target WSDL type");
		JTextArea targetWsdlTypeArea = ComponentFactory.createTextArea();
		targetWsdlTypeArea.setEditable(false);
		JScrollPane targetWsdlTypePane = ComponentFactory.createScrollPane(targetWsdlTypeArea);
		targetWsdlTypePanel.add(targetWsdlTypeLabel, BorderLayout.NORTH);
		targetWsdlTypePanel.add(targetWsdlTypePane, BorderLayout.CENTER);

		targetWsdlTypeArea.setText(getXMLSchemaText(wsdlInputMessagePart));
		
		JPanel sourceOWLTypesPanel = new JPanel(new BorderLayout());
		JLabel sourceOWLTypesLabel = ComponentFactory.createLabel("Source OWL Types");
		JTextArea sourceOWLTypesArea = ComponentFactory.createTextArea();
		sourceOWLTypesArea.setEditable(false);
		JScrollPane sourceOWLTypesPane = ComponentFactory.createScrollPane(sourceOWLTypesArea);
		sourceOWLTypesPanel.add(sourceOWLTypesLabel, BorderLayout.NORTH);
		sourceOWLTypesPanel.add(sourceOWLTypesPane, BorderLayout.CENTER);

		Collection owlParams = generateParameterList();
		OWLIndividual owlParam = null;
		String owlText = "";
		OWLNamedClass LocalCls = okb.getOWLNamedClass("process:Local");
		OWLNamedClass InputCls = okb.getOWLNamedClass("process:Input");
		for (Iterator it = owlParams.iterator(); it.hasNext();){
			owlParam = (OWLIndividual)it.next();
			if (owlParam.hasRDFType(LocalCls, true)){
				owlText += "Local: " + owlParam.getName() + 
						   " with type: " + OWLUtils.getParameterType(owlParam, okb).getName() + "\n";
			}
			else if (owlParam.hasRDFType(InputCls, true)){
				owlText += "Input: " + owlParam.getName() + 
				   		   " with type: " + OWLUtils.getParameterType(owlParam, okb).getName();
			}
		}
		sourceOWLTypesArea.setText(owlText);
		
		typesPanel.add(targetWsdlTypePanel);
		typesPanel.add(sourceOWLTypesPanel);
		tabs.add("Type Information", typesPanel);
	}
	
	private Message getWsdlMessage(String wsdlMessage) throws XSLTException {
		URI wsdlMsgURI = null;
		try{	
			wsdlMsgURI = new URI(wsdlMessage);
		}
		catch (URISyntaxException e){
			throw new XSLTException("Bad URI for WSDL message: " + wsdlMessage);
		}
		QName wsdlMsgQName = new QName(wsdlMsgURI.getScheme() + ":" + wsdlMsgURI.getSchemeSpecificPart(),
									   wsdlMsgURI.getFragment());
		return wsdlDef.getMessage(wsdlMsgQName);
	}
	
	protected void prepareTree(){
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		XSLTNode root = (XSLTNode) model.getRoot();
		
		if ( wsdlInputMessagePart != null ) { // "part" was found
			QName type = wsdlInputMessagePart.getTypeName();
			String nodename = wsdlInputMessagePart.getName();
			XSLTNode partnode = null;
			if ( type.getNamespaceURI().equalsIgnoreCase("http://www.w3.org/2001/XMLSchema") ) {
				partnode = new DataNode(nodename);
			}
			else {
				partnode = new ElementNode(nodename);
			}
			root.add(partnode);
			tree.setModel(model);
		}
	}
	
	protected void createTranslationPanel() {
	    if ( transPanel == null ) transPanel = new O2WParameterPanel(this);
	}

	protected void addNewNode(XSLTNode n) {
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		XSLTNode root = (XSLTNode)model.getRoot();
		XSLTNode selected = (XSLTNode)tree.getLastSelectedPathComponent();
		XSLTNode parent = null;

		if ( selected == null ){
			tree.setSelectionPath(new TreePath(root));
			selected = root;
		}

		if (n instanceof VariableNode){
			((O2WParameterPanel)transPanel).getDataPanel().addParameter("$" + n.getNodeName());
			((O2WParameterPanel)transPanel).getAttributePanel().addParameter("$" + n.getNodeName());
			((O2WParameterPanel)transPanel).getVariablePanel().addParameter("$" + n.getNodeName());
			model.insertNodeInto(n,selected,getXSLTVariables(true).size());
		}
		else if (n instanceof AttributeNode)
			model.insertNodeInto(n,selected,0);
		else
			model.insertNodeInto(n,selected,selected.getChildCount());
	
		tree.updateUI();
		tree.setSelectionPath(new TreePath(n.getPath()));
	}

	protected void deleteNode() {
		XSLTNode node = (XSLTNode)tree.getLastSelectedPathComponent();
		XSLTNode parent = (XSLTNode) node.getParent();
		O2WParameterPanel pp = (O2WParameterPanel)transPanel;
		if ( !node.isRoot() ) {
			parent.remove(node);
			pp.switchPanel((XSLTNode)parent.getRoot());
			tree.updateUI();
			if ( node instanceof VariableNode ) {
				pp.getDataPanel().removeParameter(node.getNodeName());
				pp.getVariablePanel().removeParameter(node.getNodeName());
				pp.getAttributePanel().removeParameter(node.getNodeName());
			}
		}
    	tree.updateUI();
	}

	protected void cleanUpParameterPanel() {
		O2WParameterPanel pp = (O2WParameterPanel)transPanel;
		Collection variables = getXSLTVariables(true); // Just their names.
		Iterator it = variables.iterator();
		while ( it.hasNext() ) {
			Object variable = it.next();
			pp.getAttributePanel().removeParameter("$" + variable);
			pp.getVariablePanel().removeParameter("$" + variable);
			pp.getDataPanel().removeParameter("$" + variable);
		}
	}	
	
	protected void generateXSLT() {
		// Part 1, Initialize stuff:
		String script = "";
		String varHeader = "";
		String temp = "";
		xsltString = "";
		// Part 2, Generate the header:
		xsltString = "<![CDATA[" + NL +
		 	TAB + "<xsl:stylesheet version=\"1.0\"";
		Collection namespaces = generateNamespaces();
		Iterator it = namespaces.iterator();
		while ( it.hasNext() ) xsltString += NL + TAB + TAB + it.next();
		xsltString += ">" + NL;
		// Part 2.5, Generate the parameters:		
		Collection parameters = generateParameterNameList(generateParameterList());
		it = parameters.iterator();
		while ( it.hasNext() ) {
			temp = "<xsl:param name=\"" + it.next() +"\"/>";
			xsltString += TAB + TAB + temp + NL;
		}
		// Part 3, Generate the code for XSLT Variables:
		xsltString += TAB + TAB + "<xsl:template match=\"/\">" + NL;		
		Collection variables = getXSLTVariables(false); //Get Varibales and their info.
		it = variables.iterator();
		while ( it.hasNext() ) {
			XSLTNode node = (XSLTNode)it.next();
			//XSLTFunction function = node.getXSLTFunction();
			varHeader += node.getXSLTString(2);
		}
		xsltString += varHeader;
		// Part 4, Generate the tags and what is inside them:
		XSLTNode root = (XSLTNode)tree.getModel().getRoot();
		for (Enumeration e = root.children() ; e.hasMoreElements();) {
			XSLTNode n = (XSLTNode)e.nextElement();
			if ( !(n instanceof VariableNode) )
				xsltString += n.getXSLTString(4);
		}
		// Part 5, Generate the footer:
		xsltString += TAB + TAB + "</xsl:template>" + NL;
		xsltString += TAB + "</xsl:stylesheet>" + NL +
				"]]>" + NL;
		// Done !!!!
	}
	protected Collection generateNamespaces() {
		Collection ns = new ArrayList();
		ns.add("xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"");
		ns.add("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
		if ( atomicProcess == null ) return ns;
		Collection slotvalues;
		Iterator it2;
		OWLIndividual slotvalue = null;
		OWLObjectProperty slot = okb.getOWLObjectProperty("process:hasInput");
		slotvalues = atomicProcess.getPropertyValues(slot, true);		
		it2 = slotvalues.iterator();
		while(it2.hasNext()) {
		    slotvalue = (OWLIndividual)it2.next();
		    String prefix = slotvalue.getNamespacePrefix();
		    String namespace = slotvalue.getNamespace();
		    String nsp = "xmlns:"+prefix+"=\""+namespace+"\"";
		    if ( !ns.contains(nsp) ) ns.add(nsp);
		}
		slot = okb.getOWLObjectProperty("process:hasLocal");
		slotvalues = atomicProcess.getPropertyValues(slot, true);		
		it2 = slotvalues.iterator();
		while(it2.hasNext()) {
		    slotvalue = (OWLIndividual)it2.next();
		    String prefix = slotvalue.getNamespacePrefix();
		    String namespace = slotvalue.getNamespace();
		    String nsp = "xmlns:"+prefix+"=\""+namespace+"\"";
		    if ( !ns.contains(nsp) ) ns.add(nsp);
		}
		return ns;
	}
	
	protected Collection generateParameterNameList(Collection paramList){
		ArrayList newList = new ArrayList();
		OWLIndividual param = null;
		for (Iterator it = paramList.iterator(); it.hasNext();){
			param = (OWLIndividual)it.next();
			newList.add("$" + param.getName());
		}
		return newList;
	}
	
	protected Collection generateParameterList() {
		Collection inputs = new ArrayList();
		if ( atomicProcess == null ) return inputs;
		Collection slotvalues;
		Iterator it2;
		OWLIndividual slotvalue = null;
		OWLObjectProperty slot = okb.getOWLObjectProperty("process:hasInput");
		slotvalues = atomicProcess.getPropertyValues(slot, true);		
		it2 = slotvalues.iterator();
		while(it2.hasNext()) {
		    slotvalue = (OWLIndividual)it2.next();
		    inputs.add(slotvalue);
		}
		slot = okb.getOWLObjectProperty("process:hasLocal");
		slotvalues = atomicProcess.getPropertyValues(slot, true);		
		it2 = slotvalues.iterator();
		while(it2.hasNext()) {
		    slotvalue = (OWLIndividual)it2.next();
		    inputs.add(slotvalue);
		}
		return inputs;
	}

	public void valueChanged(TreeSelectionEvent e) {
		XSLTNode node = null;
		if ( e.getNewLeadSelectionPath() != null ) {
			node = (XSLTNode) e.getNewLeadSelectionPath().getLastPathComponent();
			if (node instanceof DataNode){
				createTag.setEnabled(false);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createAttribute.setEnabled(false);
				createVariable.setEnabled(false);
			}
			else if (node instanceof VariableNode){
				createTag.setEnabled(false);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createAttribute.setEnabled(false);
				createVariable.setEnabled(false);
			}
			else if (node instanceof AttributeNode){
				createTag.setEnabled(false);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createAttribute.setEnabled(false);
				createVariable.setEnabled(false);
			}
			else if (node instanceof ElementNode){
				createTag.setEnabled(true);
				createData.setEnabled(true);
				deleteNode.setEnabled(true);
				createAttribute.setEnabled(true);
				createVariable.setEnabled(false);
			}
			else if (node instanceof RootNode){
				createTag.setEnabled(true);
				createData.setEnabled(true);
				deleteNode.setEnabled(false);
				createAttribute.setEnabled(false);
				createVariable.setEnabled(true);
			}
		} else {
			createTag.setEnabled(false);
			createData.setEnabled(false);
			deleteNode.setEnabled(false);			
			createAttribute.setEnabled(false);
			createVariable.setEnabled(false);
		}
		//((DefaultTreeModel)tree.getModel()).reload();
	}	
	
	public JPanel makeTreeButtons() {
		JPanel treeButtons = new JPanel(new BorderLayout()); //Box.createHorizontalBox();
		JToolBar toolbar = new JToolBar();

		createTag = new JButton(((DefaultTreeCellRenderer)tree.getCellRenderer()).getDefaultClosedIcon());
	    createTag.setToolTipText("Create Element");		
	    createTag.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	addNewNode(new ElementNode());
	    	}
	    });
	    createTag.setBorderPainted(false);
	    toolbar.add(createTag);
	    
	    createData = new JButton(((DefaultTreeCellRenderer)tree.getCellRenderer()).getDefaultLeafIcon());
	    createData.setToolTipText("Create Data");
	    createData.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	addNewNode(new DataNode());
	    	}
	    });
	    createData.setBorderPainted(false);
	    toolbar.add(createData);
	    
	    createAttribute = new JButton(OWLSIcons.getXMLAttributeIcon());
	    createAttribute.setToolTipText("Create Attribute");
	    createAttribute.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	addNewNode(new AttributeNode());
	    	}
	    });
	    createAttribute.setBorderPainted(false);
	    createAttribute.setEnabled(false);
	    toolbar.add(createAttribute);
	    
		createVariable = new JButton(OWLSIcons.getXSLTVariableIcon());
	    createVariable.setToolTipText("Create XSLT Variable");
	    createVariable.setBorderPainted(false);
	    createVariable.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	addNewNode(new VariableNode(getNewVariableName()));
	    	}
	    });
	    toolbar.add(createVariable);
	    
	    deleteNode = new JButton(OWLSIcons.getDeleteControlConstructIcon());
	    deleteNode.setToolTipText("Delete Node");
	    deleteNode.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	deleteNode();
		    	tree.updateUI();
	    	}
	    });
	    deleteNode.setBorderPainted(false);
	    deleteNode.setEnabled(false);
	    toolbar.add(deleteNode);	    

	    toolbar.setFloatable(false);
		treeButtons.add(toolbar, BorderLayout.EAST);
		return treeButtons;
	}

}
