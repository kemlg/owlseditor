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

public class SequenceNode extends BagOrTreeNode {
	public SequenceNode(OWLSTreeNodeInfo ni) {
		super(ni, OWLSList.CC_LIST);
	}

	public GraphNodeInfo graph(HashSet nameSet, PrintWriter pw,
			int clusterNumber, OWLSTreeNode selectedNode) {
		UniqueName controlConstructName = new UniqueName(getInstance()
				.getName(), nameSet);
		int newClusterNumber = clusterNumber;
		Integer clusterInt = new Integer(newClusterNumber++);
		// boolean error = true;

		pw.println("subgraph " + clusterInt.toString() + " {");

		// If this node is the selected one, 'highlight' it.
		if (this.equals(selectedNode)) {
			pw.println("node [fillcolor="
					+ GraphProcessModel.SELECTED_NODE_FILL_COLOR + ", color="
					+ GraphProcessModel.SELECTED_NODE_EDGE_COLOR + "];");
			pw.println("edge [color=" + GraphProcessModel.SELECTED_EDGE_COLOR
					+ "];");
		}

		Enumeration e = children();
		if (!e.hasMoreElements()) {
			// nothing inside this construct
			UniqueName dummyNodeName = new UniqueName("dummySequenceNode",
					nameSet);
			pw.println(dummyNodeName.getUniqueName()
					+ " [shape=point, label=\"\"];");
			pw.println("}");
			return new GraphNodeInfo(dummyNodeName.getUniqueName(),
					dummyNodeName.getUniqueName(), "", "", newClusterNumber);
		}
		// if (error)
		// pw.println("node [color=red, fontcolor=red]; edge [color=red];");

		String inNode;
		GraphNodeInfo firstPair = null;
		// String previousNode = null;
		// We have to treat the first node as a special case
		if (e.hasMoreElements()) {
			OWLSTreeNode firstChild = (OWLSTreeNode) e.nextElement();
			firstPair = firstChild.graph(nameSet, pw, newClusterNumber++,
					selectedNode);
			newClusterNumber = firstPair.clusterNumber;
			inNode = firstPair.firstNode;
			// previousNode = firstPair.lastNode;
		}
		GraphNodeInfo thisPair = firstPair;
		GraphNodeInfo previousPair = thisPair;
		// The rest of the nodes
		while (e.hasMoreElements()) {
			OWLSTreeNode child = (OWLSTreeNode) e.nextElement();
			thisPair = child.graph(nameSet, pw, newClusterNumber++,
					selectedNode);
			newClusterNumber = thisPair.clusterNumber;
			pw.println(previousPair.lastNode + "->" + thisPair.firstNode
					+ thisPair.createInEdgeAttr(previousPair.outEdgeAttr)); // a
																			// bit
																			// ugly
			previousPair = thisPair;
			// previousNode = thisPair.lastNode;
		}

		pw.println("}");

		return new GraphNodeInfo(firstPair.firstNode, thisPair.lastNode,
				firstPair.inEdgeAttr, thisPair.outEdgeAttr, newClusterNumber);
	}

	public String toString() {
		return "Sequence";
	}

}
