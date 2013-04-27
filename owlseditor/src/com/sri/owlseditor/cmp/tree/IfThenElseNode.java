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
import com.sri.owlseditor.util.OWLUtils;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;

public class IfThenElseNode extends ConditionalNode {
	public IfThenElseNode(OWLSTreeNodeInfo ni){
		super(ni, true, "process:ifCondition");
	}

	public GraphNodeInfo graph(HashSet nameSet, PrintWriter pw, int clusterNumber,
							   OWLSTreeNode selectedNode){
		OWLIndividual inst;

		UniqueName controlConstructName = new UniqueName(getInstance().getName(), nameSet);

		int newClusterNumber = clusterNumber;
		Integer clusterInt = new Integer(newClusterNumber++);
		pw.println("subgraph "+clusterInt.toString()+" {");

        // If this node is the selected one, 'highlight' it.
        if (this.equals(selectedNode)){
 			pw.println("node [fillcolor=" + GraphProcessModel.SELECTED_NODE_FILL_COLOR + 
 					   ", color=" + GraphProcessModel.SELECTED_NODE_EDGE_COLOR +"];");
 			pw.println("edge [color=" + GraphProcessModel.SELECTED_EDGE_COLOR + "];");
        }

        // If there is an error, draw things red.
        if (getThenProcess() == null){
			pw.println("node [color=" + GraphProcessModel.ERROR_NODE_EDGE_COLOR + 
					   ", fontcolor=" + GraphProcessModel.ERROR_NODE_FONT_COLOR + 
					   "]; edge [color=" + GraphProcessModel.ERROR_EDGE_COLOR + "];");
		}
		
		// Create the condition node
        UniqueName condNodeName = printCondNode(pw, nameSet);

		// Create the if-then-else end node
		UniqueName endNodeName = new UniqueName("IfEndNode", nameSet);
		pw.println(endNodeName.getUniqueName() + " [shape=point, label=\"\"];");
		
		// Connect the if-then-else-node to the condition-node
		//pw.println(startNodeName.getUniqueName() + "->" + condNodeName.getUniqueName());
		
		Enumeration e = children();
		if (e.hasMoreElements()){
			OWLSTreeNode ifNode = (OWLSTreeNode)e.nextElement(); 
			GraphNodeInfo ifPair = ifNode.graph(nameSet, pw, newClusterNumber++, selectedNode);
			pw.println(condNodeName.getUniqueName() + "->" + ifPair.firstNode +
						ifPair.createInEdgeAttr("label=\"true\""));
			pw.println(ifPair.lastNode + "->" + endNodeName.getUniqueName() + 
						ifPair.createOutEdgeAttr(""));
			newClusterNumber = ifPair.clusterNumber;
		}
		else{
			pw.println(condNodeName.getUniqueName() + "->" + endNodeName.getUniqueName());
		}
		if (e.hasMoreElements()){
			OWLSTreeNode elseNode = (OWLSTreeNode)e.nextElement(); 
			GraphNodeInfo elsePair = elseNode.graph(nameSet, pw, newClusterNumber++, selectedNode);
			pw.println(condNodeName.getUniqueName() + "->" + elsePair.firstNode +
						elsePair.createInEdgeAttr("label=\"false\""));
			pw.println(elsePair.lastNode + "->" + endNodeName.getUniqueName() +
						elsePair.createOutEdgeAttr(""));
			newClusterNumber = elsePair.clusterNumber;
		}
		else{
			// No else case, so we connect the false arrow to the out node
			pw.println(condNodeName.getUniqueName() + "->" + endNodeName.getUniqueName() +
						" [label=\"false\"];");
		}
		
		pw.println("}");
        return new GraphNodeInfo(condNodeName.getUniqueName(),
								 endNodeName.getUniqueName(), "", "", newClusterNumber);
	}
	
	public OWLIndividual getThenProcess(){
		return (OWLIndividual)OWLUtils.getNamedSlotValue(getInstance(), 
													"process:then",
													getOWLModel());
	}

	public OWLIndividual getElseProcess(){
		return (OWLIndividual)OWLUtils.getNamedSlotValue(getInstance(), 
													"process:else",
													getOWLModel());
	}

	
	public void updateKBAfterDelete(OWLSTreeNode node){
		int index = getIndex(node);
		/* For now, we do the same thing for both cases, but there should
		 * be some special handling of the various cases here.
		 */
		if (index == 0){
			// then case
			//getOWLModel().deleteInstance(node.getInstance());
			node.getInstance().delete();
		}
		else if (index == 1){
			// else case
			//getOWLModel().deleteInstance(node.getInstance());
			node.getInstance().delete();
		}
	}

	public void updateKBAfterMove(OWLSTreeNode node){
		int index = getIndex(node);
		/* For now, we do the same thing for both cases, but there should
		 * be some special handling of the various cases here.
		 */
		if (index == 0){
			// then case
			OWLUtils.removeNamedSlotValue(getInstance(), 
											"process:then", 
											getOWLModel());
		}
		else if (index == 1){
			// else case
			OWLUtils.removeNamedSlotValue(getInstance(), 
											"process:else", 
											getOWLModel());
		}
	}

	/** Updates the KB after adding a node.
	 */
	public void updateKBAfterInsert(OWLSTreeNode newnode){
		int newindex = getIndex(newnode);
		if (newindex == 0){
			// then-case
			OWLUtils.setNamedSlotValue(getInstance(), 
					"process:then", 
					newnode.getInstance(),
					getOWLModel());
		}
		else if (newindex == 1){
			// else-case
			OWLUtils.setNamedSlotValue(getInstance(), 
					"process:else", 
					newnode.getInstance(),
					getOWLModel());
		}
	}

	public String toString(){
		return "If-Then-Else";
	}

	public boolean acceptsChild(){
		if (this.getChildCount() < 2)
			return true;
		else
			return false;
	}

}
