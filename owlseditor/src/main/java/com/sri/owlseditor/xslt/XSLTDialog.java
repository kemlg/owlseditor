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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sri.owlseditor.util.OWLUtils;

import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public abstract class XSLTDialog extends JDialog implements ChangeListener,
		TreeSelectionListener {
	protected JenaOWLModel okb = null;
	protected JTree tree = null;
	protected String xsltString = "";
	protected JTextArea texteditor;
	protected JTabbedPane tabs;
	protected OWLIndividual m_owlInst;
	protected String wsdluri = null;
	protected Definition wsdlDef;
	protected Document wsdlDoc;
	public String NL = System.getProperty("line.separator");
	public String TAB = "    ";
	protected TransformationPanel transPanel;

	protected abstract void addNewNode(XSLTNode n);

	protected abstract void deleteNode();

	protected abstract void prepareTree();

	protected abstract void createTranslationPanel();

	protected abstract void cleanUpParameterPanel();

	protected abstract void generateXSLT();

	public abstract void valueChanged(TreeSelectionEvent e);

	public abstract JPanel makeTreeButtons();

	public XSLTDialog(OWLModel okb, String wsdlDocument) throws XSLTException {
		super();
		this.okb = (JenaOWLModel) okb;
		tree = new JTree(new RootNode());
		wsdluri = wsdlDocument;
		readWsdl(wsdlDocument);
		createTranslationPanel();
	}

	protected void readWsdl(String wsdldocument) throws XSLTException {
		try {
			// First read it using DOM
			DOMParser parser = new DOMParser();
			parser.parse(wsdluri);
			wsdlDoc = parser.getDocument();

			// Then also into WSDL4J
			WSDLFactory wsdlfactory = WSDLFactory.newInstance();
			WSDLReader wsdlreader = wsdlfactory.newWSDLReader();
			wsdlDef = wsdlreader.readWSDL(wsdldocument);
		} catch (Exception e) {
			throw new XSLTException("Could not read WSDL document "
					+ wsdldocument);
		}
	}

	public Collection getXSLTVariables(boolean justTheNames) {
		Collection c = new ArrayList();
		XSLTNode root = (XSLTNode) tree.getModel().getRoot();
		for (Enumeration e = root.children(); e.hasMoreElements();) {
			XSLTNode child = (XSLTNode) e.nextElement();
			if (child instanceof VariableNode) {
				if (justTheNames)
					c.add(child.getNodeName());
				else
					c.add(child);
			}
		}
		return c;
	}

	protected String getNewVariableName() {
		Collection variables = getXSLTVariables(true); // Just get the names
		int idx = 1;
		while (variables.contains(VariableNode.DEFAULT_VARIABLE_NAME + idx))
			idx++;
		return VariableNode.DEFAULT_VARIABLE_NAME + idx;
	}

	public JTree getTree() {
		return tree;
	}

	private void showKBErrorDialog() {
		JOptionPane
				.showMessageDialog(
						this,
						"Could not update the Knowledge Base, because \n"
								+ "the ontology containing the WSDL grounding is not editable.\n"
								+ "To fix this, click the \"Select active sub-ontology\" button\n"
								+ "in the main toolbar, and make sure the \"editable\" checkbox\n"
								+ "is checked for the ontology containing the WSDL grounding.",
						"Error updating KB!", JOptionPane.ERROR_MESSAGE);
	}

	protected void setupGUI() {
		setTitle("XSLT Transformation Dialog");
		// xslt functions
		Container pane = getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		Box leftSide = Box.createVerticalBox();
		TitledBorder leftTitle = BorderFactory
				.createTitledBorder("Tree Representation");
		leftSide.setBorder(leftTitle);
		leftSide.add(makeTreeButtons());
		leftSide.add(Box.createRigidArea(new Dimension(5, 5)));
		leftSide.add(new JScrollPane(tree));

		Box rightSide = Box.createVerticalBox();
		TitledBorder rightTitle = BorderFactory
				.createTitledBorder("Transformation");
		rightSide.setBorder(rightTitle);
		rightSide.add(transPanel);

		Box controlButtons = Box.createHorizontalBox();
		JButton button;

		button = new JButton("Reset");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = confirmReset();
				if (result == JOptionPane.YES_OPTION) {
					cleanUpParameterPanel();
					prepareTree();
					generateXSLT();
					texteditor.setText(getGeneratedXSLTString());
					tree.updateUI();
				}
			}
		});
		controlButtons.add(button);
		controlButtons.add(Box.createRigidArea(new Dimension(5, 5)));

		button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (tabs.getSelectedIndex()) {
				case 0: // First tab
					if (checkValidity()) { // Valid
						generateXSLT();
						xsltString = getGeneratedXSLTString();
						dispose();
					} else {
						int result = confirmOK();
						if (result == JOptionPane.YES_OPTION) {
							generateXSLT();
							xsltString = getGeneratedXSLTString();
							dispose();
						}
					}
					break;
				case 1: // Second tab
					xsltString = texteditor.getText();
					dispose();
					break;
				}

				OWLDatatypeProperty xsltTransStrProp = (OWLDatatypeProperty) okb
						.getOWLProperty("grounding:xsltTransformationString");
				if (!OWLUtils.setPropertyValueInHomeStore(m_owlInst,
						xsltTransStrProp, xsltString))
					showKBErrorDialog();

				/*
				 * XMLTreeTest xt = new XMLTreeTest (xsltString);
				 * DefaultTreeModel tm = (DefaultTreeModel) xt.getModel();
				 * Object root = tm.getRoot(); printNode (tm, root);
				 */
			}
		});
		controlButtons.add(button);
		controlButtons.add(Box.createRigidArea(new Dimension(5, 5)));

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		controlButtons.add(button);

		Box upperStuff = Box.createHorizontalBox();
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftSide, rightSide);
		split.setDividerLocation(0.40);
		upperStuff.add(split);
		tabs = new JTabbedPane();
		JPanel page1 = new JPanel(new BorderLayout());
		JPanel page2 = new JPanel(new BorderLayout());
		tabs.addChangeListener(this);
		page1.add(upperStuff, BorderLayout.CENTER);
		texteditor = new JTextArea(17, 36);
		page2.add(new JScrollPane(texteditor));
		tabs.addTab("Visual Editor", page1);
		tabs.addTab("Text Editor", page2);
		pane.add(tabs);
		pane.add(Box.createRigidArea(new Dimension(5, 5)));
		pane.add(controlButtons);
		pane.add(Box.createRigidArea(new Dimension(5, 5)));
		pack();
		tree.updateUI();
	}

	/*
	 * private void printNode (DefaultTreeModel xt, Object node) {
	 * //System.out.println ("count = " + xt.getChildCount(node)); for (int i =
	 * 0; i < xt.getChildCount (node); i++) { Object o = xt.getChild (node, i);
	 * printNode (xt, o); } }
	 */
	private int confirmReset() {
		return JOptionPane.showConfirmDialog(this,
				"Do you really want to reset the XSLT tree?", "Reset Tree",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	}

	private int confirmOK() {
		return JOptionPane.showConfirmDialog(this,
				"Some names or parameters are not specified. Proceed anyway?",
				"Validation Error", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
	}

	public String getGeneratedXSLTString() {
		return xsltString;
	}

	protected boolean checkValidity() {
		// Read the last panel before doing the validation:
		XSLTNode lastnode = (XSLTNode) tree.getLastSelectedPathComponent();
		if (lastnode != null)
			transPanel.writeFromPanel2Node(lastnode);
		// Now validate it:
		XSLTNode root = (XSLTNode) tree.getModel().getRoot();
		return root.isValid();
	}

	protected Node getXMLNode(NodeList children, String name, String attr,
			String value) {
		Node result = null;
		if (children == null)
			return null;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().toLowerCase().indexOf(name.toLowerCase()) > -1) {
				if (attr != null) {
					NamedNodeMap attributes = child.getAttributes();
					for (int j = 0; j < attributes.getLength(); j++) {
						Node attribute = attributes.item(j);
						if (attribute.getNodeName().toLowerCase()
								.equals(attr.toLowerCase())
								&& attribute.getNodeValue().equals(value)) {
							result = child;
							break;
						}
					}
				} else {
					result = child;
					break;
				}
			}
		}
		return result;
	}

	public static String prettyPrintQName(QName name) {
		return name.getNamespaceURI() + "#" + name.getLocalPart();
	}

	/**
	 * Returns the Node of the definition of the type of the part from the
	 * <types> section in the WSDL file. If it is not there (i.e. it is a simple
	 * type or it is defined elsewhere) this returns null.
	 */
	protected Node getTypeNode(Part part) {
		String partname = part.getTypeName().getLocalPart();

		NodeList children = wsdlDoc.getDocumentElement().getChildNodes();
		Node node_types = getXMLNode(children, "types", null, null);
		if (node_types == null)
			return null;
		Node node_schema = getXMLNode(node_types.getChildNodes(), "schema",
				null, null);
		if (node_schema == null)
			return null;
		Node node_complex = getXMLNode(node_schema.getChildNodes(),
				"complexType", "name", partname);
		return node_complex;
	}

	protected String getXMLSchemaText(Part part) {
		String xmlSchemaText = null;
		Node node = getTypeNode(part);
		if (node == null)
			return prettyPrintQName(part.getTypeName());
		Serializer serialzer = SerializerFactory.getSerializerFactory(
				Method.XML).makeSerializer(new OutputFormat());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialzer.setOutputByteStream(out);
		try {
			serialzer.asDOMSerializer().serialize((Element) node);
			xmlSchemaText = out.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// Remove the first line, return the rest.
		return xmlSchemaText.substring(xmlSchemaText.indexOf('\n') + 1);
	}

	public void stateChanged(ChangeEvent e) {
		int selected = tabs.getSelectedIndex();
		switch (selected) {
		case 0:
			break;
		case 1:
			if (checkValidity()) { // Valid
				generateXSLT();
				texteditor.setText(getGeneratedXSLTString());
			} else {
				int result = JOptionPane
						.showConfirmDialog(
								this,
								"Some names or parameters are not specified. Proceed anyway?",
								"Validation Error", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					generateXSLT();
					texteditor.setText(getGeneratedXSLTString());
				} else
					tabs.setSelectedIndex(0);
			}
			break;
		}
	}

}
