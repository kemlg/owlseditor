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

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;

public class RepeatUntilNode extends ConditionalNode {
	public RepeatUntilNode(OWLSTreeNodeInfo ni){
		super(ni, true, "process:untilCondition"); 
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
        if (getChildCount() == 0)
			pw.println("node [color=" + GraphProcessModel.ERROR_NODE_EDGE_COLOR + 
					   ", fontcolor=" + GraphProcessModel.ERROR_NODE_FONT_COLOR + 
					   "]; edge [color=" + GraphProcessModel.ERROR_EDGE_COLOR + "];");
		
		// Create the condition node
        UniqueName condNodeName = printCondNode(pw, nameSet);

		Enumeration e = children();
		GraphNodeInfo processPair = null;
		if (e.hasMoreElements()){
			OWLSTreeNode processNode = (OWLSTreeNode)e.nextElement(); 
			processPair = processNode.graph(nameSet, pw, newClusterNumber++, selectedNode);
			newClusterNumber = processPair.clusterNumber;
			//pw.println(startNodeName.getUniqueName() + "->" + processPair.firstNode +
			//			processPair.createInEdgeAttr(""));
			pw.println(processPair.lastNode + "->" + condNodeName.getUniqueName() +
						processPair.createOutEdgeAttr(""));
			// Connect the condition-node to the process node
			pw.println(condNodeName.getUniqueName() + "->" + processPair.firstNode + 
						"[label=\"false\"];");

			pw.println("}");
			return new GraphNodeInfo(processPair.firstNode,
					 				 condNodeName.getUniqueName(), 
									 processPair.inEdgeAttr, "label=\"true\"",
									 newClusterNumber);
		}
		else{
			pw.println("}");
			return new GraphNodeInfo(condNodeName.getUniqueName(),
					 				 condNodeName.getUniqueName(), 
									 "", "label=\"true\"",
									 newClusterNumber);
		}
	}

	private OWLIndividual getUntilProcess(){
		OWLIndividual inst = (OWLIndividual)OWLUtils.getNamedSlotValue(getInstance(), 
															 "process:untilProcess",
															 getOWLModel());
		return inst;
	}

	public void updateKBAfterMove(OWLSTreeNode node){
		OWLUtils.removeNamedSlotValue(getInstance(), 
										"process:untilProcess", 
										getOWLModel());
	}

	public void updateKBAfterDelete(OWLSTreeNode node){
		//System.out.println(this.getClass() + ".updateKBAfterDelete(" + node +")");
		//Instance construct = getUntilProcess();
		
		//getOWLModel().deleteInstance(construct);   // this is actually a control construct and
															// not a process
		//getOWLModel().deleteInstance(node.getInstance());
		node.getInstance().delete();
	}

	/** Updates the KB after adding a node.
	 */
	public void updateKBAfterInsert(OWLSTreeNode newnode){
		OWLUtils.setNamedSlotValue(getInstance(), 
									"process:untilProcess", 
									newnode.getInstance(),
									getOWLModel());
	}

	public String toString(){
		return "Repeat-Until";
	}

	public boolean acceptsChild(){
		if (this.getChildCount() < 1)
			return true;
		else
			return false;
	}

}
