/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License
at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See
the License for the specific language governing rights and limitations
under the License.

The Original Code is the OWL/S Visualizer.

The Initial Developer of the Original Code is DCS Corporation.
Portions created by the Initial Developer are Copyright (C) 2004
the Initial Developer.  All Rights Reserved.

Contributor(s): SRI International

The following notice applies to the Original Code:

   Unlimited Rights assigned to the U.S. Government.
   This material may be reproduced by or for the U.S Government
   pursuant to the copyright license under the clause at DFARS
   227-7203-5(a), DFARS 227.7103-5(a), DFARS 252.227-7013(b)(1)(June
   1995), DFARS 252.227-7014 (June 1995), and FAR 52.227-14(a).
   This notice must appear in all copies of this file and its
   derivatives.
 */

package com.sri.owlseditor.cmp.graph;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;

import com.sri.owlseditor.cmp.tree.OWLSTreeNode;

import edu.stanford.smi.protege.model.KnowledgeBase;

public class GraphProcessModel {
	static PrintWriter pw;
	static KnowledgeBase kb;
	static int clusterNumber = 0;
	static boolean graphClusters = false;
	public HashSet nameSet;
	private OWLSTreeNode node;

	public static final String NODE_EDGE_COLOR = "lemonchiffon4";
	public static final String NODE_FILL_COLOR = "lemonchiffon1";
	public static final String EDGE_COLOR = "lemonchiffon4";
	public static final String ERROR_NODE_EDGE_COLOR = "red";
	public static final String ERROR_NODE_FONT_COLOR = "red";
	public static final String ERROR_EDGE_COLOR = "red";
	public static final String SELECTED_NODE_EDGE_COLOR = "black";
	public static final String SELECTED_NODE_FILL_COLOR = "lemonchiffon2";
	public static final String SELECTED_EDGE_COLOR = "black";
	public static final String DATAFLOW_EDGE_COLOR = "blue";
	public static final String DATAFLOW_FONT_COLOR = "blue";
	public static final String DATAFLOW_NODE_COLOR = "blue";

	public GraphProcessModel(OWLSTreeNode rootConstruct,
			OWLSTreeNode selectedNode, String outputFileName, KnowledgeBase inkb) {
		node = rootConstruct;
		nameSet = new HashSet();
		kb = inkb;

		try {
			pw = new PrintWriter(new FileWriter(outputFileName));

			String kbName = kb.getName();
			pw.println("digraph \"" + kbName + "\" {");

			pw.println("node [shape=box, fillcolor=" + NODE_FILL_COLOR
					+ ", style=filled, color=" + NODE_EDGE_COLOR + "];");
			pw.println("edge [color=" + EDGE_COLOR + "];");
			pw.println("compound=true;");
			// pw.println("ranksep=\"1.5\";");
			pw.println("labelloc=t;");
			pw.println("subgraph Top {rank=source;");
			pw.println("Start [shape=ellipse, label=\"Start/In\"];");
			pw.println("}");

			if (rootConstruct != null) {
				pw.println("subgraph Main {");
				GraphNodeInfo nodeInfo = node.graph(nameSet, pw, 0,
						selectedNode);
				pw.println("}");
				pw.println("Start->" + nodeInfo.firstNode
						+ nodeInfo.createInEdgeAttr(""));
				pw.println(nodeInfo.lastNode + "->Finish"
						+ nodeInfo.createOutEdgeAttr(""));
			} else
				pw.println("Start->Finish");

			pw.println("subgraph End {rank=sink");
			pw.println("Finish [shape=ellipse, label=\"Finish/Out\"];");
			pw.println("}");

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
}
