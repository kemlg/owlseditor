/*
"The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See
the
License for the specific language governing rights and limitations
under
the License.

The Original Code is OWL-S Editor for Protege.

The Initial Developer of the Original Code is SRI International.
Portions created by the Initial Developer are Copyright (C) 2004 the
Initial Developer.  All Rights Reserved.
 */

package com.sri.owlseditor.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.sri.owlseditor.ServiceSelectorListener;
import com.sri.owlseditor.options.GraphVizOptions;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protege.model.Project;

import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.GrappaSupport;
import att.grappa.Parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

//--------------------------------------------------------------------------------------------

public class GraphDisplay extends JFrame implements ServiceSelectorListener,
		GrappaConstants {
	private OWLModel _okb;
	private OWLIndividual _inst;
	private GraphObject root;
	private ArrayList sceneGraph;
	private ArrayList dotNodeList;
	private static PrintWriter pw;
	private GrappaPanel grappaPanel;
	private Graph grappaGraph;
	private JScrollPane jPane;
	private String outputFileName;

	// Following set of variables are used to specify the graphviz rank for each
	// type of element in the scenegraph
	private String serviceRank = "min";
	private String profileRank = "min";
	private String processRank = "min";
	private String groundingRank = "min";
	private String defaultRank = "min";

	private static final String NODE_COLOR_SERVICE = "lightgoldenrodyellow";
	private static final String NODE_COLOR_PROFILE = "palegoldenrod";
	private static final String NODE_COLOR_PROCESS = "cornsilk";
	private static final String NODE_COLOR_GROUNDING = "moccasin";
	private static final String NODE_COLOR_DEFAULT = "lightgoldenrodyellow";

	// --------------------------------------------------------------------------------------------

	public GraphDisplay(OWLModel okb, OWLIndividual initInstance) {
		super("Graph Overview");

		_okb = okb;
		outputFileName = "GraphOverview.dot";
		sceneGraph = new ArrayList();
		dotNodeList = new ArrayList();

		// createInstanceRelations(initInstance, _okb); <------ is this
		// necessary?

		createSceneGraph(_okb);

		createDotFile(_okb);

		jPane = new JScrollPane(grappaPanel);

		displayGraph(outputFileName);

		getContentPane().add(jPane);
		setVisible(false);
		/*
		 * //Debug code remove before commit :)
		 * 
		 * OWLNamedClass owlPropertyClass = okb.getOWLObjectPropertyClass();
		 * 
		 * Collection properties = owlPropertyClass.getInstances(false);
		 * 
		 * Iterator propertiesIterator = properties.iterator();
		 * 
		 * while(propertiesIterator.hasNext()) { OWLObjectProperty property =
		 * (OWLObjectProperty)propertiesIterator.next();
		 * System.out.println("Property : " + property.getName()); }
		 */
	}

	// --------------------------------------------------------------------------------------------

	public void createSceneGraph(OWLModel okb) {
		Collection instances = _okb.getOWLIndividuals();
		Iterator instancesCollection = instances.iterator();
		while (instancesCollection.hasNext()) {
			Object tempInst = instancesCollection.next();
			if (tempInst instanceof OWLIndividual && tempInst != null) {
				OWLIndividual instance = (OWLIndividual) tempInst;
				// instanceCount++;
				root = createInstanceRelations(instance, _okb);

			}

		}

	}

	// --------------------------------------------------------------------------------------------

	public String getGraphVizPath() {
		Project project = _okb.getProject();
		String path = (String) project
				.getClientInformation(GraphVizOptions.OWLSEDITOR_GRAPHVIZ_PATH_KEY);
		if (path == null)
			return GraphVizOptions.DEFAULT_GRAPHVIZ_PATH;
		else
			return path;
	}

	// --------------------------------------------------------------------------------------------

	public void setGraph(Graph graph) {
		// graph may be null
		grappaGraph = graph;

		if (grappaGraph == null) {
			grappaPanel = null;
			jPane.setViewportView(null);
		} else {
			grappaPanel = new GrappaPanel(grappaGraph);

			jPane.setViewportView(grappaPanel);
		}
	}

	// --------------------------------------------------------------------------------------------

	public void displayGraph(String FileName) {

		FileInputStream input;

		try {
			input = new FileInputStream(FileName);

			Parser program = new Parser(input, System.err);
			program.parse();

			Object connector = null;

			try {
				connector = Runtime.getRuntime().exec(getGraphVizPath());
			} catch (Exception e) {
				System.out
						.println("ERROR! Could not execute GraphViz at the given path: "
								+ getGraphVizPath());
				return;
			}

			Graph graph = null;
			graph = program.getGraph();

			if (connector != null) {
				if (!GrappaSupport.filterGraph(graph, connector)) {
					System.err.println("ERROR: somewhere in filterGraph");
				}
			}

			graph.setEditable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));
			setGraph(graph);

		}

		catch (FileNotFoundException fnf) {
			System.err.println(fnf.toString());
		}

		catch (Exception ex) {
			System.err.println("Exception: " + ex.getMessage());
			ex.printStackTrace(System.err);
		}

		repaint();
	}

	// --------------------------------------------------------------------------------------------

	public void updateNodeList(GraphObject node) {

		if (dotNodeList.size() == 0) // Empty nodelist
		{ // Add elements directly
			// to dotnodelist
			String nodeText = node.getParentName();
			String nodeName = "node" + dotNodeList.size();
			DotNode pNode = new DotNode(nodeName, nodeText); // Add parent node
			pNode.setColor(node.getParentColor());
			dotNodeList.add(pNode);
			node.setParentNodeNum(dotNodeList.size() - 1);

			nodeText = node.getChildName(); // Add child node
			nodeName = "node" + dotNodeList.size();
			DotNode dNode = new DotNode(nodeName, nodeText);
			dNode.setColor(node.getChildColor());
			dotNodeList.add(dNode);
			node.setChildNodeNum(dotNodeList.size() - 1);
		}

		else {
			boolean foundChildNode = false;
			boolean foundParentNode = false;

			for (int p = 0; p < dotNodeList.size(); p++) // Search the nodelist
			{ // to see if the child node
				DotNode d = (DotNode) dotNodeList.get(p); // or the parent node
															// has
				String dotNodeText = d.getNodeText(); // been added previously

				if (node != null && node.getChildName() != null) {
					if (node.getChildName().equals(dotNodeText))
						foundChildNode = true;
					if (node.getParentName().equals(dotNodeText))
						foundParentNode = true;
				}
			}

			if (!foundParentNode) {
				String nodeText = node.getParentName();
				String nodeName = "node" + dotNodeList.size();
				DotNode pNode = new DotNode(nodeName, nodeText); // Add parent
																	// node
				pNode.setColor(node.getParentColor());
				dotNodeList.add(pNode);
				node.setParentNodeNum(dotNodeList.size() - 1);
			}
			if (!foundChildNode) {
				String nodeText = node.getChildName();
				String nodeName = "node" + dotNodeList.size();
				DotNode dNode = new DotNode(nodeName, nodeText); // Add parent
																	// node
				dNode.setColor(node.getChildColor());
				dotNodeList.add(dNode);
				node.setChildNodeNum(dotNodeList.size() - 1);
			}
		}

	}

	// --------------------------------------------------------------------------------------------

	public void setNodeColors(GraphObject node, String property) {
		if (property.equals("service:presents")) {
			node.setChildColor(NODE_COLOR_PROFILE);
			node.setParentColor(NODE_COLOR_SERVICE);
		} else if (property.equals("service:supports")) {
			node.setChildColor(NODE_COLOR_GROUNDING);
			node.setParentColor(NODE_COLOR_SERVICE);
		} else if (property.equals("profile:has_process")) {
			node.setChildColor(NODE_COLOR_PROCESS);
			node.setParentColor(NODE_COLOR_PROFILE);
		} else if (property.equals("grounding:hasAtomicProcessGrounding")) {
			node.setChildColor(NODE_COLOR_GROUNDING);
			node.setParentColor(NODE_COLOR_GROUNDING);
		} else if (property.equals("service:describedBy")) {
			node.setChildColor(NODE_COLOR_PROCESS);
			node.setParentColor(NODE_COLOR_GROUNDING);
		} else {
			node.setChildColor(NODE_COLOR_DEFAULT);
			node.setParentColor(NODE_COLOR_DEFAULT);
		}

	}

	// --------------------------------------------------------------------------------------------
	public void buildRelationship(OWLIndividual parent, String property) {
		OWLObjectProperty owlProperty = _okb.getOWLObjectProperty(property);
		Collection propertyValues = parent.getPropertyValues(owlProperty);
		Iterator propertyIterator = propertyValues.iterator();

		while (propertyIterator.hasNext()) {
			Object temp = propertyIterator.next();
			if (temp instanceof OWLIndividual) {
				OWLIndividual child = (OWLIndividual) temp;
				GraphObject node = new GraphObject(parent.getName(),
						child.getName(), property);
				setNodeColors(node, property);
				// System.out.println("P : "+parent.getName() +" C : " +
				// child.getName()+ "prop : " + property);

				boolean found = false;

				for (int p = 0; p < sceneGraph.size(); p++) {
					GraphObject g = (GraphObject) sceneGraph.get(p);
					if (g.toString().equals(node.toString()))
						found = true;
				}

				if (!found) // This child-parent relationship
				{ // does not exist in the scenegraph
					updateNodeList(node); // Add child and/or parent to nodelist
					sceneGraph.add(node); // Add node to scenegraph
					// nodeCount++;
				}

			}

		}
	}

	// --------------------------------------------------------------------------------------------

	public GraphObject createInstanceRelations(OWLIndividual parent,
			OWLModel okb) {
		int level = 0;
		int nodeCount = 0;

		if (parent != null) {
			buildRelationship(parent, "service:presents");
			buildRelationship(parent, "service:supports");
			buildRelationship(parent, "profile:has_process");
			buildRelationship(parent, "grounding:hasAtomicProcessGrounding");
			buildRelationship(parent, "service:describedBy");
			buildRelationship(parent, "service:provides");
		}
		/*
		 * OWLNamedClass owlPropertyClass = okb.getOWLObjectPropertyClass();
		 * 
		 * Collection properties = owlPropertyClass.getInstances(false);
		 * 
		 * Iterator propertiesIterator = properties.iterator();
		 * 
		 * if (parent != null) { while(propertiesIterator.hasNext()) {
		 * OWLObjectProperty property =
		 * (OWLObjectProperty)propertiesIterator.next();
		 * buildRelationship(parent,property.getName()); } }
		 */

		// To display the scene graph uncomment this block
		/*
		 * for(int p=0; p<sceneGraph.size(); p++) { GraphObject g =
		 * (GraphObject)sceneGraph.get(p); System.out.println(g.toString());
		 * 
		 * }
		 */
		return root;
	}

	// --------------------------------------------------------------------------------------------

	public OWLIndividual searchTree(OWLIndividual inst) {
		return inst;
	}

	// --------------------------------------------------------------------------------------------

	public void createDotFile(OWLModel okb) {
		try {
			pw = new PrintWriter(new FileWriter(outputFileName));

			String kbName = okb.getName();
			pw.println("digraph \"" + kbName + "GraphOverview\" {");

			// Comment the following for vertical ranking
			pw.println("rankdir=LR;");

			// Comment the following for vertical spacing to be default
			pw.println("ranksep=.1;");

			for (int p = 0; p < dotNodeList.size(); p++) {
				DotNode dNode = (DotNode) dotNodeList.get(p);
				String nodeName = dNode.getDotNodeName();
				String nodelabel = dNode.getNodeText();
				String nodeText = nodeName + " [color=" + dNode.getColor()
						+ ",style=filled" + ",label=\"" + nodelabel + "\""
						+ "];";
				// System.out.println(nodeText);
				pw.println(nodeText);
			}

			for (int q = 0; q < sceneGraph.size(); q++) {
				GraphObject gNode = (GraphObject) sceneGraph.get(q);
				DotNode parentNode = (DotNode) dotNodeList.get(gNode
						.getParentNodeNum());
				DotNode childNode = (DotNode) dotNodeList.get(gNode
						.getChildNodeNum());

				int parentNum = 0;
				int childNum = 0;

				String parentName = gNode.getParentName();
				String childName = gNode.getChildName();
				String labelText = gNode.getArrowText();

				boolean found = false;
				for (int i = 0; i < dotNodeList.size() && !found; i++) {
					DotNode pNode = (DotNode) dotNodeList.get(i);
					if (parentName.equals(pNode.getNodeText())) {
						found = true;
						parentNum = i;
					}

				}
				found = false;
				for (int i = 0; i < dotNodeList.size() && !found; i++) {
					DotNode cNode = (DotNode) dotNodeList.get(i);
					if (childName.equals(cNode.getNodeText())) {
						found = true;
						childNum = i;
					}

				}

				String parentNodeName = "node" + parentNum;
				String childNodeName = "node" + childNum;

				String s = parentNodeName + " -> " + childNodeName
						+ " [label=\"" + labelText + "\"];";
				// System.out.println(s);
				pw.println(s);

			}
			pw.println("}"); // end digraph
			pw.flush();
			pw.close();

		} catch (Exception e1) {
			System.out.println("Couldn't create GraphProcessModel");
			e1.printStackTrace();
			pw.flush();
			pw.close();
		}

	}

	// --------------------------------------------------------------------------------------------

	public void setInstance(OWLIndividual inst) {
		_inst = inst;

		int instanceCount = 0;

		createSceneGraph(_okb);

		createDotFile(_okb);

		displayGraph(outputFileName);

		repaint();

	}
}
