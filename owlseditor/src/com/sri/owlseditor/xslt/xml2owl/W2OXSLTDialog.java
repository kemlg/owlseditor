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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
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

import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

public class W2OXSLTDialog extends XSLTDialog {
	private String wsdlmessageuri = null;
	private String wsdluri = null;
	private Message wsdlOutputMessage;
	private OWLIndividual atomicProcess = null;
	private RDFResource parameterType;
	private JButton createClass, createData, createVariable,
			createRDFAttribute, createDatatypeProp, createObjectProp,
			deleteNode;

	public W2OXSLTDialog(OWLModel okb, OWLIndividual aP, String wsdl,
			String wsdlMsg, OWLIndividual msgMap) throws XSLTException {
		super(okb, wsdl);
		atomicProcess = aP;

		// Do some sanity checking
		if (wsdl == null)
			throw new XSLTException("No WSDL document defined.");
		if (wsdlMsg == null)
			throw new XSLTException("No WSDL Input Message defined.");
		wsdlOutputMessage = getWsdlMessage(wsdlMsg);
		if (wsdlOutputMessage == null)
			throw new XSLTException("WSDL Output Message " + wsdlMsg
					+ " not found.");

		wsdlmessageuri = wsdlMsg;
		wsdluri = wsdl;
		m_owlInst = msgMap;

		parameterType = getParameterType(m_owlInst);

		prepareTree();
		setupGUI();

		Collection msgParts = getMessagePartNames(wsdlOutputMessage);

		W2OParameterPanel pp = (W2OParameterPanel) transPanel;
		pp.getRDFNamePanel().addParameters(msgParts);
		pp.getRDFResourcePanel().addParameters(msgParts);
		pp.getDataPanel().addParameters(msgParts);
		pp.getVariablePanel().addParameters(msgParts);

		tree.addTreeSelectionListener((W2OParameterPanel) transPanel);
		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new XMLTreeRenderer());
	}

	protected void setupGUI() {
		super.setupGUI();
		JPanel typesPanel = ComponentFactory.createPanel();
		typesPanel.setLayout(new BorderLayout());

		JPanel targetOWLTypePanel = new JPanel(new BorderLayout());
		JLabel targetOWLTypeLabel = ComponentFactory
				.createLabel("Target OWL Type: ");
		JTextField targetOWLTypeField = ComponentFactory.createTextField();
		targetOWLTypeField.setEditable(false);
		targetOWLTypePanel.add(targetOWLTypeLabel, BorderLayout.WEST);
		targetOWLTypePanel.add(targetOWLTypeField, BorderLayout.CENTER);

		targetOWLTypeField.setText(getParameterType(m_owlInst).getName());

		JPanel sourceWsdlTypesPanel = new JPanel(new BorderLayout());
		JLabel sourceWsdlTypesLabel = ComponentFactory
				.createLabel("Source WSDL types");
		JTextArea sourceWsdlTypesArea = ComponentFactory.createTextArea();
		sourceWsdlTypesArea.setEditable(false);
		JScrollPane sourceWsdlTypesPane = ComponentFactory
				.createScrollPane(sourceWsdlTypesArea);
		sourceWsdlTypesPanel.add(sourceWsdlTypesLabel, BorderLayout.NORTH);
		sourceWsdlTypesPanel.add(sourceWsdlTypesPane, BorderLayout.CENTER);

		Collection wsdlMessageParts = wsdlOutputMessage.getParts().values();
		Part msgPart = null;
		String wsdlText = "";
		for (Iterator it = wsdlMessageParts.iterator(); it.hasNext();) {
			msgPart = (Part) it.next();
			wsdlText += "WSDL Output Message Part: " + msgPart.getName()
					+ " with type:\n\n " + getXMLSchemaText(msgPart) + "\n\n";

		}
		sourceWsdlTypesArea.setText(wsdlText);

		typesPanel.add(targetOWLTypePanel, BorderLayout.NORTH);
		typesPanel.add(sourceWsdlTypesPanel, BorderLayout.CENTER);
		tabs.add("Type Information", typesPanel);
	}

	private OWLIndividual getOwlsParameter(OWLIndividual msgMap) {
		OWLObjectProperty owlsParameter = okb
				.getOWLObjectProperty("grounding:owlsParameter");
		return (OWLIndividual) msgMap.getPropertyValue(owlsParameter);
	}

	private RDFResource getParameterType(OWLIndividual msgMap) {
		OWLIndividual param = getOwlsParameter(msgMap);
		return OWLUtils.getParameterType(param, okb);
	}

	private Collection getMessagePartNames(Message wsdlMsg) {
		ArrayList partnames = new ArrayList();
		Iterator it = wsdlMsg.getParts().values().iterator();
		while (it.hasNext()) {
			Part part = (Part) it.next();
			partnames.add(part.getName());
		}
		return partnames;
	}

	private Message getWsdlMessage(String wsdlMessage) throws XSLTException {
		URI wsdlMsgURI = null;
		try {
			wsdlMsgURI = new URI(wsdlMessage);
		} catch (URISyntaxException e) {
			throw new XSLTException("Bad URI for WSDL message: " + wsdlMessage);
		}
		QName wsdlMsgQName = new QName(wsdlMsgURI.getScheme() + ":"
				+ wsdlMsgURI.getSchemeSpecificPart(), wsdlMsgURI.getFragment());
		return wsdlDef.getMessage(wsdlMsgQName);
	}

	protected void prepareTree() {
		if (parameterType != null) {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			XSLTNode root = (XSLTNode) model.getRoot();
			XSLTNode owlnode = null;
			String nodename = parameterType.getName();
			if (parameterType instanceof OWLNamedClass) {
				owlnode = new ClassNode((OWLNamedClass) parameterType);
			} else if (parameterType instanceof RDFSDatatype) {
				owlnode = new DataNode(nodename);
			}
			root.add(owlnode);
		}
	}

	protected void createTranslationPanel() {
		if (transPanel == null)
			transPanel = new W2OParameterPanel(this);
	}

	public JTree getTree() {
		return tree;
	}

	protected void addNewNode(XSLTNode n) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		XSLTNode root = (XSLTNode) model.getRoot();
		XSLTNode selected = (XSLTNode) tree.getLastSelectedPathComponent();
		XSLTNode parent = null;

		if (selected == null) {
			tree.setSelectionPath(new TreePath(root));
			selected = root;
		}

		if (n instanceof VariableNode) {
			((W2OParameterPanel) transPanel).getDataPanel().addParameter(
					"$" + n.getNodeName());
			((W2OParameterPanel) transPanel).getRDFNamePanel().addParameter(
					"$" + n.getNodeName());
			((W2OParameterPanel) transPanel).getRDFResourcePanel()
					.addParameter("$" + n.getNodeName());
			((W2OParameterPanel) transPanel).getVariablePanel().addParameter(
					"$" + n.getNodeName());
			model.insertNodeInto(n, selected, getXSLTVariables(true).size());
		} else if (n instanceof RDFAttributeNode)
			model.insertNodeInto(n, selected, 0);
		else
			model.insertNodeInto(n, selected, selected.getChildCount());

		tree.updateUI();
		tree.setSelectionPath(new TreePath(n.getPath()));
	}

	protected void deleteNode() {
		XSLTNode node = (XSLTNode) tree.getLastSelectedPathComponent();
		XSLTNode parent = (XSLTNode) node.getParent();
		W2OParameterPanel pp = (W2OParameterPanel) transPanel;
		if (!node.isRoot()) {
			parent.remove(node);
			pp.switchPanel((XSLTNode) parent.getRoot());
			tree.updateUI();
			if (node instanceof VariableNode) {
				pp.getDataPanel().removeParameter(node.getNodeName());
				pp.getVariablePanel().removeParameter(node.getNodeName());
				pp.getRDFNamePanel().removeParameter(node.getNodeName());
				pp.getRDFResourcePanel().removeParameter(node.getNodeName());
			}
		}
		tree.updateUI();
	}

	protected void cleanUpParameterPanel() {
		W2OParameterPanel pp = (W2OParameterPanel) transPanel;
		Collection variables = getXSLTVariables(true); // Just their names.
		Iterator it = variables.iterator();
		while (it.hasNext()) {
			Object variable = it.next();
			pp.getRDFNamePanel().removeParameter("$" + variable);
			pp.getRDFResourcePanel().removeParameter("$" + variable);
			pp.getVariablePanel().removeParameter("$" + variable);
			pp.getDataPanel().removeParameter("$" + variable);
		}
	}

	// Returns namespaces used in owl classes/properties
	private void getUsedOWLNamespaces(Collection namespaces, XSLTNode node) {
		if (node instanceof OWLNode) {
			OWLNode o = (OWLNode) node;
			RDFResource r = o.getResource();
			String prefix = r.getNamespacePrefix();
			String namespace = r.getNamespace();
			String nsp = "xmlns:" + prefix + "=\"" + namespace + "\"";
			if (!namespaces.contains(nsp)) {
				namespaces.add(nsp);
			}
		}
		// Only these three node types can have OWL children
		if (node instanceof RootNode || node instanceof DatatypePropertyNode
				|| node instanceof ClassNode) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				XSLTNode n = (XSLTNode) e.nextElement();
				getUsedOWLNamespaces(namespaces, n);
			}
		}
	}

	private String generateRDFHeader() {
		String rdfString = TAB + TAB + TAB + "<?xml version=\"1.0\"?>" + NL
				+ TAB + TAB + TAB + "<rdf:RDF";
		ArrayList namespaces = new ArrayList();
		namespaces
				.add("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
		namespaces.add("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"");
		namespaces.add("xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"");
		namespaces.add("xmlns:owl=\"http://www.w3.org/2002/07/owl#\"");
		RootNode root = (RootNode) getTree().getModel().getRoot();
		getUsedOWLNamespaces(namespaces, root);
		for (Iterator it = namespaces.iterator(); it.hasNext();) {
			rdfString += NL + TAB + TAB + TAB + TAB + (String) it.next();
		}
		return rdfString + ">" + NL;
	}

	private String generateRDFFooter() {
		return TAB + TAB + TAB + "</rdf:RDF>" + NL;
	}

	protected void generateXSLT() {
		// Part 1, Initialize stuff:
		String script = "";
		String varHeader = "";
		String temp = "";
		xsltString = "";
		// Part 2, Generate the header:
		xsltString = "<![CDATA[" + NL + TAB + "<xsl:stylesheet version=\"1.0\"";
		Collection namespaces = generateNamespaces();
		Iterator it = namespaces.iterator();
		while (it.hasNext())
			xsltString += NL + TAB + TAB + it.next();
		xsltString += ">" + NL;
		xsltString += TAB + TAB + "<xsl:template match=\"/\">" + NL;
		// Part 3, Generate the code for XSLT Variables:

		Collection variables = getXSLTVariables(false); // Get Varibales and
														// their info.
		it = variables.iterator();
		while (it.hasNext()) {
			XSLTNode node = (XSLTNode) it.next();
			// XSLTFunction function = node.getXSLTFunction();
			varHeader += node.getXSLTString(3);
		}
		xsltString += varHeader;

		// Part 4, Generate the RDF header
		xsltString += generateRDFHeader();

		// Part 5, Generate the tags and what is inside them:
		XSLTNode root = (XSLTNode) tree.getModel().getRoot();
		for (Enumeration e = root.children(); e.hasMoreElements();) {
			XSLTNode n = (XSLTNode) e.nextElement();
			if (!(n instanceof VariableNode))
				xsltString += n.getXSLTString(4);
		}

		// Part 5, Generate the RDF footer
		xsltString += generateRDFFooter();

		// Part 6, Generate the XSLT footer:
		xsltString += TAB + TAB + "</xsl:template>" + NL;
		xsltString += TAB + "</xsl:stylesheet>" + NL + "]]>" + NL;
		// Done !!!!
	}

	protected Collection generateNamespaces() {
		Collection ns = new ArrayList();
		ns.add("xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"");
		ns.add("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
		if (atomicProcess == null)
			return ns;
		Collection slotvalues;
		Iterator it2;
		OWLIndividual slotvalue = null;
		OWLObjectProperty slot = okb.getOWLObjectProperty("process:hasInput");
		slotvalues = atomicProcess.getPropertyValues(slot, true);
		it2 = slotvalues.iterator();
		while (it2.hasNext()) {
			slotvalue = (OWLIndividual) it2.next();
			String prefix = slotvalue.getNamespacePrefix();
			String namespace = slotvalue.getNamespace();
			String nsp = "xmlns:" + prefix + "=\"" + namespace + "\"";
			if (!ns.contains(nsp))
				ns.add(nsp);
		}
		slot = okb.getOWLObjectProperty("process:hasLocal");
		slotvalues = atomicProcess.getPropertyValues(slot, true);
		it2 = slotvalues.iterator();
		while (it2.hasNext()) {
			slotvalue = (OWLIndividual) it2.next();
			String prefix = slotvalue.getNamespacePrefix();
			String namespace = slotvalue.getNamespace();
			String nsp = "xmlns:" + prefix + "=\"" + namespace + "\"";
			if (!ns.contains(nsp))
				ns.add(nsp);
		}
		return ns;
	}

	protected Collection generateInputList() {
		Collection inputs = new ArrayList();
		if (atomicProcess == null)
			return inputs;
		Collection slotvalues;
		Iterator it2;
		OWLIndividual slotvalue = null;
		OWLObjectProperty slot = okb.getOWLObjectProperty("process:hasInput");
		slotvalues = atomicProcess.getPropertyValues(slot, true);
		it2 = slotvalues.iterator();
		while (it2.hasNext()) {
			slotvalue = (OWLIndividual) it2.next();
			inputs.add(slotvalue.getName());
		}
		slot = okb.getOWLObjectProperty("process:hasLocal");
		slotvalues = atomicProcess.getPropertyValues(slot, true);
		it2 = slotvalues.iterator();
		while (it2.hasNext()) {
			slotvalue = (OWLIndividual) it2.next();
			inputs.add(slotvalue.getName());
		}
		return inputs;
	}

	public void valueChanged(TreeSelectionEvent e) {
		XSLTNode node = null;
		if (e.getNewLeadSelectionPath() != null) {
			node = (XSLTNode) e.getNewLeadSelectionPath()
					.getLastPathComponent();
			if (node instanceof DataNode || node instanceof VariableNode) {
				createClass.setEnabled(false);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createRDFAttribute.setEnabled(false);
				createVariable.setEnabled(false);
				createDatatypeProp.setEnabled(false);
				createObjectProp.setEnabled(false);
			} else if (node instanceof RDFAttributeNode) {
				createClass.setEnabled(false);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createRDFAttribute.setEnabled(false);
				createVariable.setEnabled(false);
				createDatatypeProp.setEnabled(false);
				createObjectProp.setEnabled(false);
			} else if (node instanceof ClassNode) {
				createClass.setEnabled(false);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createRDFAttribute.setEnabled(true);
				createVariable.setEnabled(false);
				createDatatypeProp.setEnabled(true);
				createObjectProp.setEnabled(true);
			} else if (node instanceof DatatypePropertyNode) {
				createClass.setEnabled(false);
				createData.setEnabled(true);
				deleteNode.setEnabled(true);
				createRDFAttribute.setEnabled(false);
				createVariable.setEnabled(false);
				createDatatypeProp.setEnabled(false);
				createObjectProp.setEnabled(false);
			} else if (node instanceof ObjectPropertyNode) {
				createClass.setEnabled(true);
				createData.setEnabled(false);
				deleteNode.setEnabled(true);
				createRDFAttribute.setEnabled(true);
				createVariable.setEnabled(false);
				createDatatypeProp.setEnabled(false);
				createObjectProp.setEnabled(false);
			} else if (node instanceof RootNode) {
				createClass.setEnabled(true);
				createData.setEnabled(false);
				deleteNode.setEnabled(false);
				createRDFAttribute.setEnabled(false);
				createVariable.setEnabled(true);
				createDatatypeProp.setEnabled(false);
				createObjectProp.setEnabled(false);
			}
		} else {
			createClass.setEnabled(false);
			createData.setEnabled(false);
			deleteNode.setEnabled(false);
			createRDFAttribute.setEnabled(false);
			createVariable.setEnabled(false);
			createDatatypeProp.setEnabled(false);
			createObjectProp.setEnabled(false);
		}
		// ((DefaultTreeModel)tree.getModel()).reload();
	}

	/* pickCls doesn't care about sorting order. */
	private OWLNamedClass chooseClass() {
		return (OWLNamedClass) DisplayUtilities.pickCls((Component) this, okb,
				CollectionUtilities.createCollection(okb.getOWLThingClass()),
				"Select a class");
	}

	private OWLObjectProperty chooseObjectProperty() {
		return (OWLObjectProperty) DisplayUtilities.pickInstanceFromCollection(
				(Component) this, OWLUtils.sortFrameCollection(okb
						.getOWLObjectPropertyClass().getInstances(true)), 0,
				"Select an object property");
	}

	private OWLDatatypeProperty chooseDatatypeProperty() {
		return (OWLDatatypeProperty) DisplayUtilities
				.pickInstanceFromCollection((Component) this, OWLUtils
						.sortFrameCollection(okb.getOWLDatatypePropertyClass()
								.getInstances(true)), 0,
						"Select a datatype property");
	}

	public JPanel makeTreeButtons() {
		JPanel treeButtons = new JPanel(new BorderLayout()); // Box.createHorizontalBox();
		JToolBar toolbar = new JToolBar();

		createClass = new JButton(
				OWLIcons.getCreateIcon(OWLIcons.RDF_INDIVIDUAL));
		createClass.setToolTipText("Create Instance");
		createClass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OWLNamedClass cls = chooseClass();
				if (cls != null)
					addNewNode(new ClassNode(cls));
			}
		});
		createClass.setBorderPainted(false);
		toolbar.add(createClass);

		createObjectProp = new JButton(
				OWLIcons.getCreateIcon(OWLIcons.OWL_OBJECT_PROPERTY));
		createObjectProp.setToolTipText("Create object property");
		createObjectProp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OWLObjectProperty prop = chooseObjectProperty();
				if (prop != null)
					addNewNode(new ObjectPropertyNode(prop));
			}
		});
		createObjectProp.setBorderPainted(false);
		toolbar.add(createObjectProp);

		createDatatypeProp = new JButton(
				OWLIcons.getCreateIcon(OWLIcons.OWL_DATATYPE_PROPERTY));
		createDatatypeProp.setToolTipText("Create datatype property");
		createDatatypeProp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OWLDatatypeProperty prop = chooseDatatypeProperty();
				if (prop != null)
					addNewNode(new DatatypePropertyNode(prop));
			}
		});
		createDatatypeProp.setBorderPainted(false);
		toolbar.add(createDatatypeProp);

		createData = new JButton(
				((DefaultTreeCellRenderer) tree.getCellRenderer())
						.getDefaultLeafIcon());
		createData.setToolTipText("Create Data");
		createData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNewNode(new DataNode());
			}
		});
		createData.setBorderPainted(false);
		toolbar.add(createData);

		createRDFAttribute = new JButton(OWLSIcons.getXMLAttributeIcon());
		createRDFAttribute.setToolTipText("Create RDF Attribute");
		createRDFAttribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				XSLTNode parentNode = (XSLTNode) tree
						.getLastSelectedPathComponent();
				if (parentNode instanceof ClassNode)
					addNewNode(new RDFNameNode());
				else if (parentNode instanceof ObjectPropertyNode)
					addNewNode(new RDFResourceNode());
				else
					System.out.println("WARNING! Wrong parent type!");
			}
		});
		createRDFAttribute.setBorderPainted(false);
		createRDFAttribute.setEnabled(false);
		toolbar.add(createRDFAttribute);

		createVariable = new JButton(OWLSIcons.getXSLTVariableIcon());
		createVariable.setToolTipText("Create XSLT Variable");
		createVariable.setBorderPainted(false);
		createVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNewNode(new VariableNode(getNewVariableName()));
			}
		});
		toolbar.add(createVariable);

		deleteNode = new JButton(OWLSIcons.getDeleteControlConstructIcon());
		deleteNode.setToolTipText("Delete Node");
		deleteNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
