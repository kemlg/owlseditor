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
package com.sri.owlseditor.cmp.tree;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;

import com.sri.owlseditor.cmp.graph.GraphNodeInfo;
import com.sri.owlseditor.cmp.graph.GraphProcessModel;
import com.sri.owlseditor.cmp.graph.UniqueName;
import com.sri.owlseditor.util.OWLSList;

public class AnyOrderNode extends BagOrTreeNode {
	public AnyOrderNode(OWLSTreeNodeInfo ni) {
		super(ni, OWLSList.CC_BAG);
	}

	public GraphNodeInfo graph(HashSet nameSet, PrintWriter pw,
			int clusterNumber, OWLSTreeNode selectedNode) {
		UniqueName controlConstructName = new UniqueName(getInstance()
				.getName(), nameSet);
		int newClusterNumber = clusterNumber;
		Integer clusterInt = new Integer(newClusterNumber++);

		pw.println("subgraph " + clusterInt.toString() + " {");

		// If this node is the selected one, 'highlight' it.
		if (this.equals(selectedNode)) {
			pw.println("node [fillcolor="
					+ GraphProcessModel.SELECTED_NODE_FILL_COLOR + ", color="
					+ GraphProcessModel.SELECTED_NODE_EDGE_COLOR + "];");
			pw.println("edge [color=" + GraphProcessModel.SELECTED_EDGE_COLOR
					+ "];");
		}

		// Create the in node
		UniqueName inNodeName = new UniqueName("inNode", nameSet);
		pw.println(inNodeName.getUniqueName()
				+ " [shape=point, label=\"Any-Order\"];");

		Enumeration e = children();
		if (!e.hasMoreElements()) {
			// nothing inside this construct
			pw.println("}");
			return new GraphNodeInfo(inNodeName.getUniqueName(),
					inNodeName.getUniqueName(), "", "", newClusterNumber);
		}

		// Create the out node
		UniqueName outNodeName = new UniqueName("outNode", nameSet);
		pw.println(outNodeName.getUniqueName()
				+ " [shape=point, label=\"/Any-Order\"];");

		String newNodeName;
		GraphNodeInfo thisPair = null;
		while (e.hasMoreElements()) {
			OWLSTreeNode child = (OWLSTreeNode) e.nextElement();
			thisPair = child.graph(nameSet, pw, newClusterNumber++,
					selectedNode);
			newClusterNumber = thisPair.clusterNumber;
			// connect new node to the split and join nodes
			pw.println(inNodeName.getUniqueName() + "->" + thisPair.firstNode
					+ thisPair.createInEdgeAttr(""));
			pw.println(thisPair.lastNode + "->" + outNodeName.getUniqueName()
					+ thisPair.createOutEdgeAttr(""));
		}

		pw.println("}");
		return new GraphNodeInfo(inNodeName.getUniqueName(),
				outNodeName.getUniqueName(), "", "", newClusterNumber);
	}

	/*
	 * Old "box" version UniqueName controlConstructName = new
	 * UniqueName(getInstance().getName(), nameSet); int newClusterNumber =
	 * clusterNumber; Integer clusterInt= new Integer(newClusterNumber++);
	 * String clusterName = "cluster" + clusterInt.toString();
	 * 
	 * pw.println("subgraph \"" + clusterName + "\" {label=\"Any-Order\";" +
	 * " labelloc=top;" + " style=dotted;");
	 * 
	 * // If this node is the selected one, 'highlight' it. if
	 * (this.equals(selectedNode)){ pw.println("node [fillcolor=" +
	 * GraphProcessModel.SELECTED_NODE_FILL_COLOR + ", color=" +
	 * GraphProcessModel.SELECTED_NODE_EDGE_COLOR +"];");
	 * pw.println("edge [color=" + GraphProcessModel.SELECTED_EDGE_COLOR +
	 * "];"); }
	 * 
	 * // Create the invisible in node UniqueName inNodeName = new
	 * UniqueName("inNode", nameSet); pw.println(inNodeName.getUniqueName() +
	 * " [shape=point, label=\"\"];");
	 * 
	 * // Create the invisible out node UniqueName outNodeName = new
	 * UniqueName("outNode", nameSet); pw.println(outNodeName.getUniqueName() +
	 * " [shape=point, label=\"\"];");
	 * 
	 * String newNodeName; GraphNodeInfo thisPair = null; for (Enumeration e =
	 * children() ; e.hasMoreElements() ;) { OWLSTreeNode child =
	 * (OWLSTreeNode)e.nextElement(); thisPair = child.graph(nameSet, pw,
	 * newClusterNumber++, selectedNode); newClusterNumber =
	 * thisPair.clusterNumber; // connect new node to the in and out nodes using
	 * invisible edges pw.println(inNodeName.getUniqueName() + "->" +
	 * thisPair.firstNode + thisPair.createInEdgeAttr("style=invis"));
	 * pw.println(thisPair.lastNode + "->" + outNodeName.getUniqueName() +
	 * thisPair.createOutEdgeAttr("style=invis")); }
	 * 
	 * pw.println("}"); return new GraphNodeInfo(inNodeName.getUniqueName(),
	 * outNodeName.getUniqueName(), "", "", newClusterNumber); }
	 */

	public String toString() {
		return "Any-Order";
	}
}
