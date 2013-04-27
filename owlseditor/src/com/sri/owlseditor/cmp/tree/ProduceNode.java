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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.sri.owlseditor.cmp.graph.GraphNodeInfo;
import com.sri.owlseditor.cmp.graph.GraphProcessModel;
import com.sri.owlseditor.cmp.graph.UniqueName;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;

/**
 * @author Daniel Elenius
 */
public class ProduceNode extends OWLSTreeNode {

	OWLProperty producedBindingSlot;
	OWLProperty valueSourceSlot;
	OWLProperty fromProcessSlot;
	OWLProperty theVarSlot;
	OWLProperty toParamSlot;
	OWLNamedClass valueOfCls;
	OWLModel model;
	
	public ProduceNode(OWLSTreeNodeInfo ni){
		super(ni, false);    // Produces can not have children, they are leaves
		setupClassesAndProperties();
	}

	private void setupClassesAndProperties(){
		model = getOWLModel();
		producedBindingSlot = model.getOWLProperty("process:producedBinding");
		valueSourceSlot = model.getOWLProperty("process:valueSource");
		fromProcessSlot = model.getOWLProperty("process:fromProcess");
		theVarSlot = model.getOWLProperty("process:theVar");
		toParamSlot = model.getOWLProperty("process:toParam");
		valueOfCls = model.getOWLNamedClass("process:ValueOf");
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

        // If there is an error, paint it red.
        /*
        if (getProcess() == null){
        	// No process for this Perform. Draw it in red.
        	pw.println("node [color=" + GraphProcessModel.ERROR_NODE_EDGE_COLOR + 
        			   ", fontcolor=" + GraphProcessModel.ERROR_NODE_FONT_COLOR + "];");
        	pw.println(controlConstructName.getUniqueName()+"[label=\"?\"];");
        }
		else if (performsCompositeProcess())
        	pw.println(controlConstructName.getUniqueName()+"[peripheries=2, label=\"" +
        				getProcess().getName() +"\"];");
		else // atomic or simple process
        	pw.println(controlConstructName.getUniqueName()+"[label=\"" +
    				getProcess().getName() +"\"];");
        */

        pw.println(controlConstructName.getUniqueName()+"[shape=\"circle\", height=\"0.1\", " +
        												"width=\"0.1\", label=\"P\"];");
        
        pw.println("}");
        
        // Now add the dataflow arrows
        addOutputBindings(pw, nameSet);
        
        return new GraphNodeInfo(controlConstructName.getUniqueName(),
        						 controlConstructName.getUniqueName(), 
								 "", "", newClusterNumber);
	}

	private void addOutputBindings(PrintWriter pw, HashSet nameSet){
		Collection outputBindings = getInstance().getPropertyValues(producedBindingSlot, true);
		Iterator it = outputBindings.iterator();
		Bindings bindings = new Bindings(getInstance(), nameSet);
		while (it.hasNext()){
			OWLIndividual outputBinding = (OWLIndividual)it.next();
			OWLIndividual valueOf = (OWLIndividual)outputBinding.getPropertyValue(valueSourceSlot);
			if (valueOf != null){
				OWLIndividual fromProcess = (OWLIndividual)valueOf.getPropertyValue(fromProcessSlot);
				OWLIndividual theVar = (OWLIndividual)valueOf.getPropertyValue(theVarSlot);
				OWLIndividual toParam = (OWLIndividual)outputBinding.getPropertyValue(toParamSlot);
				if (fromProcess != null && theVar != null && toParam != null){
					// We have a valid valueSource input binding, so we draw it
					bindings.addBinding(fromProcess, theVar, toParam);
				}
			}
		}
		bindings.graphBindings(pw);
	}
	
	/** Deletes ValueOf and InputBinding instances of this perform. */
	public void deleteBindings(){
		OWLIndividual produce = getInstance();

		System.out.println("Deleting bindings on " + produce.getName());

		Iterator bindings = produce.getPropertyValues(producedBindingSlot).iterator();
		while (bindings.hasNext()){
			OWLIndividual outputBinding = (OWLIndividual)bindings.next();
			OWLIndividual valueOf = (OWLIndividual)outputBinding.getPropertyValue(valueSourceSlot);
			valueOf.delete();
			outputBinding.delete();
		}
	}

	/* Helper method for addInputBindings */
	/*
	private String createOutputBindingString(OWLIndividual fromProcess, 
											OWLIndividual theVar, 
											OWLIndividual toParam){
		String fromProcessName = fromProcess.getName();

		// If the fromProcess is TheParentPerform or ThisPerform,
		// we need some special handling
		if (fromProcessName.equals("process:TheParentPerform"))
			fromProcessName = "Start";
		else if (fromProcessName.equals("process:ThisPerform"))
			fromProcessName = getInstance().getName();

		UniqueName fromProcessUName = new UniqueName(fromProcessName, new HashSet());
		UniqueName toProcessUName = new UniqueName(getInstance().getName(), new HashSet());
		
		return fromProcessUName.getUniqueName() + "->" + toProcessUName.getUniqueName() + 
				" [color=" + GraphProcessModel.DATAFLOW_EDGE_COLOR + ", " + 
				//"tailport=e, headport=w, " +
				//"taillabel=\"" + theVar.getName() + "\", " + 
				//"headlabel=\"" + toParam.getName() + "\", " +
				//"label=\"" + ((OWLInstance)theVar).getLocalName() + "->" + 
				//((OWLInstance)toParam).getLocalName() + "\", " +
				"comment=\"" + theVar.getName() + " -> " + toParam.getName() + "\", " +
				"labelfontsize=\"8\", " +
				//"labelfloat=true, " +
				//"style=invis, " +
				"labelfontname=Arial, " +
				"labelfontcolor=\"" + GraphProcessModel.DATAFLOW_FONT_COLOR + "\"" +
				"constraint=\"false\"];";
		        //"weight=\"0.2\"];";
	}
	*/
	
	public void updateKBAfterDelete(OWLSTreeNode node){
		// This should never be called as a Produce cannot have children
		// deleted
	}

	public void updateKBAfterMove(OWLSTreeNode node){
		;
	}

	/** Updates the KB after adding a node.
	 */
	public void updateKBAfterInsert(OWLSTreeNode newnode){
		// This should never be called as a Perform cannot have children
		// inserted
	}
	
	public String toString(){
		return "Produce";
	}

	public boolean acceptsChild(){
		return false;
	}
	
}
