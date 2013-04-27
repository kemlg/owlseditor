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
package com.sri.owlseditor.widgets.dataflow;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sri.owlseditor.cmp.tree.AnyOrderNode;
import com.sri.owlseditor.cmp.tree.ChoiceNode;
import com.sri.owlseditor.cmp.tree.IfThenElseNode;
import com.sri.owlseditor.cmp.tree.OWLSTreeNode;
import com.sri.owlseditor.cmp.tree.PerformNode;
import com.sri.owlseditor.cmp.tree.ProduceNode;
import com.sri.owlseditor.cmp.tree.RepeatUntilNode;
import com.sri.owlseditor.cmp.tree.RepeatWhileNode;
import com.sri.owlseditor.cmp.tree.SequenceNode;
import com.sri.owlseditor.cmp.tree.SplitJoinNode;
import com.sri.owlseditor.cmp.tree.SplitNode;
import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;

/**
 * This singleton class maintains a mapping from each Perform and Produce to the composite process 
 * that the Perform/Produce is part of, and to a list of the Perform's/Produce's predecessors in the
 * composition tree.
 * 
 * This is used by the HasDataFromWidget and ProducedBindingWidget. Only these predecessors are shown in the 
 * "from perform" combo box, since a Perform can only bind inputs from the outputs 
 * of (unconditional) predecessors in the composition graph.
 * 
 * Also, this mapping is used by the HasDataFromWidget to decode TheParentPerform of a perform.
 * 
 * The mappings are built up in the graph() methods of the OWLSTreeNodes. This means that the widgets
 * that depend on this will not work correctly before the graph has been drawn. But they probably don't
 * need to either. 
 * 
 * 
 *  * @author Daniel Elenius
 */
public class PerformTreeMapper implements CleanerListener{
	private static PerformTreeMapper instance = null;
	private HashMap map = new HashMap();

	private PerformTreeMapper(){
		Cleaner.getInstance().registerCleanerListener(this);
	}
	
	public static PerformTreeMapper getInstance(){
		if (instance == null)
			instance = new PerformTreeMapper();
		return instance;
	}
	
	public void cleanup(){
		instance = null;
	}
	
	public void put(OWLIndividual perform, OWLIndividual composite){
		if (map.containsKey(perform)){
			//System.out.println("WARNING! PerformTreeMapper already contains key " + perform);
		}
		else{
			//System.out.println("PerformTreeMapper: Adding cell for instance " + perform.getName() +
			//					" on composite " + composite);
			MapCell newcell = new MapCell(composite);
			map.put(perform, newcell);
		}
	}
	
	/** Removes the entry for this perform */
	public void remove(OWLIndividual perform){
		map.remove(perform);
	}
	
	public boolean contains(OWLIndividual perform){
		return map.keySet().contains(perform);
	}
	
	public OWLIndividual getCompositeProcess(OWLIndividual perform){
		MapCell cell = (MapCell)map.get(perform);
		return cell.getComposite();
	}
	
	/** Returns the predecessor perform containing a given process */
	public OWLIndividual getPredecessorPerformWithProcess(OWLIndividual perform, OWLIndividual process){
		OWLModel model = perform.getOWLModel();

		if (process == getCompositeProcess(perform))
			return model.getOWLIndividual("process:TheParentPerform");

		OWLObjectProperty processProperty = model.getOWLObjectProperty("process:process"); 
		
		Set predecessors = getPredecessors(perform);
		Iterator it = predecessors.iterator();
		while (it.hasNext()){
			OWLIndividual predPerform = (OWLIndividual)it.next();
			OWLIndividual predProcess = (OWLIndividual)predPerform.getPropertyValue(processProperty);
			if (predProcess == process)
				return predPerform;
		}
		return null;
	}
		
	public Set getPredecessors(OWLIndividual perform){
		//System.out.println("getPredecessors called on perform " + perform);
		//System.out.println("map is " + map);
		MapCell cell = (MapCell)map.get(perform);
		//Vector preds = cell.getPredecessors();
		//if (preds == null){
		//		Instance composite = cell.getComposite();
		//	preds = generatePredecessors(perform, composite);
		//}
		//return preds;
		return cell.getPredecessors();
	}
	
	public void addPredecessor(OWLIndividual perform, OWLIndividual pred){
		MapCell cell = (MapCell)map.get(perform);
		cell.addPredecessor(pred);
	}

	public void addPredecessors(OWLIndividual perform, Set preds){
		MapCell cell = (MapCell)map.get(perform);
		//System.out.println("PerformTreeMapper.addPredecessors() adding " + preds);
		//System.out.println("  to " + perform.getName());
		cell.addPredecessors(preds);
	}

	public void removePredecessors(OWLIndividual perform){
		MapCell cell = (MapCell)map.get(perform);
		if (cell != null)
			cell.removePredecessors();
	}
	
	/** Regenerates the predecessors list for all the Performs.
	 * If the Perform is not in the PerformTreeMapper (i.e. it is newly created) 
	 * it will be added to it.
	 * @param node
	 * @param cp The composite process that this perform is a part of
	 */
	public void generatePredecessors(OWLSTreeNode root, OWLIndividual perform, OWLIndividual cp){
		//System.out.println("generatePredecessors for perform " + perform);

		if (!contains(perform)){
			put(perform, cp);
		}
		regeneratePreds((OWLSTreeNode)root.getFirstChild(), new HashSet());
	}
	
	/* Regenerates the predecessors for ALL performs in the PerformTreeMapper.
	 * We can't just look for the newly created/moved one, because moving one perform
	 * causes changes in the predecessors of others.
	 */
	private void regeneratePreds(OWLSTreeNode current, Set preds){
		Enumeration children = current.children();

		if (current instanceof ProduceNode){
			OWLIndividual thisproduce = current.getInstance();
			removePredecessors(thisproduce);
			addPredecessors(thisproduce, preds);
			OWLModel okb = (OWLModel)thisproduce.getOWLModel();
			
			OWLIndividual theParentPerform = okb.getOWLIndividual("process:TheParentPerform");
			addPredecessor(thisproduce, theParentPerform);
			//preds.add(thisproduce);
			removeIllegalOutputBindings(thisproduce);
		}
		else if (current instanceof PerformNode){
			OWLIndividual thisperform = current.getInstance();
			removePredecessors(thisperform);
			addPredecessors(thisperform, preds);
			OWLModel okb = (OWLModel)thisperform.getOWLModel();
			
			OWLIndividual theParentPerform = okb.getOWLIndividual("process:TheParentPerform");
			addPredecessor(thisperform, theParentPerform);
			preds.add(thisperform);
			removeIllegalInputBindings(thisperform);
		}
		else if (current instanceof SequenceNode){
			while (children.hasMoreElements()){
				OWLSTreeNode child = (OWLSTreeNode)children.nextElement();
				regeneratePreds(child, preds);
			}
		}
		else if (current instanceof SplitNode ||
				 current instanceof ChoiceNode){
			while (children.hasMoreElements()){
				HashSet mypreds = new HashSet(preds);
				OWLSTreeNode child = (OWLSTreeNode)children.nextElement();
				regeneratePreds(child, mypreds);
			}
		}	
		else if (current instanceof SplitJoinNode ||
				 current instanceof AnyOrderNode){
			HashSet predsIn = new HashSet(preds);
			while (children.hasMoreElements()){
				HashSet mypreds = new HashSet(predsIn);
				OWLSTreeNode child = (OWLSTreeNode)children.nextElement();
				regeneratePreds(child, mypreds);
				preds.addAll(mypreds);
			}
		}	
		// We put the then-node first and the else-node second as a convention
		// This may need to be handled better (marking the nodes somehow).
		else if (current instanceof IfThenElseNode){
			if (current.getChildCount() > 0){
				OWLSTreeNode thennode = (OWLSTreeNode)current.getChildAt(0);
				if (thennode != null){
					HashSet mypreds = new HashSet(preds);
					regeneratePreds(thennode, mypreds);

					if (current.getChildCount() > 1){
						OWLSTreeNode elsenode = (OWLSTreeNode)current.getChildAt(1);
						if (elsenode != null){
							mypreds = new HashSet(preds);
							regeneratePreds(elsenode, mypreds);
						}
					}
				}
			}
		}
		else if (current instanceof RepeatWhileNode){
			if (current.getChildCount() > 0){
				OWLSTreeNode process = (OWLSTreeNode)current.getChildAt(0);
				if (process != null){
					HashSet mypreds = new HashSet(preds);
					regeneratePreds(process, mypreds);
				}
			}
		}	
		else if (current instanceof RepeatUntilNode){
			if (current.getChildCount() > 0){
				OWLSTreeNode process = (OWLSTreeNode)current.getChildAt(0);
				if (process != null){
					regeneratePreds(process, preds);
				}
			}
		}	
		else{
			System.out.println("Unsupported control construct on node " + current);
		}
	}	
	
	/* Checks all the input bindings of this perform against its predecessors,
	 * and removes all input bindings that are not legal.
	 */
	private void removeIllegalInputBindings(OWLIndividual perform){
		OWLModel okb = (OWLModel)perform.getOWLModel();
		OWLProperty hasDataFromSlot = okb.getOWLProperty("process:hasDataFrom");
		OWLProperty valueSourceSlot = okb.getOWLProperty("process:valueSource");
		OWLProperty fromProcessSlot = okb.getOWLProperty("process:fromProcess");
		OWLProperty theVarSlot = okb.getOWLProperty("process:theVar");
		
		Collection inputBindings = perform.getPropertyValues(hasDataFromSlot, true);
		Iterator it = inputBindings.iterator();
		while (it.hasNext()){
			OWLIndividual inputBinding = (OWLIndividual)it.next();
			OWLIndividual valueOf = (OWLIndividual)inputBinding.getPropertyValue(valueSourceSlot);
			OWLIndividual fromProcess = (OWLIndividual)valueOf.getPropertyValue(fromProcessSlot);
			OWLIndividual theVar = (OWLIndividual)valueOf.getPropertyValue(theVarSlot);
			if (!getPredecessors(perform).contains(fromProcess)){
				perform.removePropertyValue(hasDataFromSlot, inputBinding);
				inputBinding.removePropertyValue(valueSourceSlot, valueOf);
				valueOf.removePropertyValue(fromProcessSlot, fromProcess);
				valueOf.removePropertyValue(theVarSlot, theVar);
			}
		}
	}

	/* Checks all the input bindings of this perform against its predecessors,
	 * and removes all input bindings that are not legal.
	 */
	private void removeIllegalOutputBindings(OWLIndividual perform){
		OWLModel okb = (OWLModel)perform.getOWLModel();
		OWLProperty producedBindingSlot = okb.getOWLProperty("process:producedBinding");
		OWLProperty valueSourceSlot = okb.getOWLProperty("process:valueSource");
		OWLProperty fromProcessSlot = okb.getOWLProperty("process:fromProcess");
		OWLProperty theVarSlot = okb.getOWLProperty("process:theVar");
		
		Collection outputBindings = perform.getPropertyValues(producedBindingSlot, true);
		Iterator it = outputBindings.iterator();
		while (it.hasNext()){
			OWLIndividual outputBinding = (OWLIndividual)it.next();
			OWLIndividual valueOf = (OWLIndividual)outputBinding.getPropertyValue(valueSourceSlot);
			OWLIndividual fromProcess = (OWLIndividual)valueOf.getPropertyValue(fromProcessSlot);
			OWLIndividual theVar = (OWLIndividual)valueOf.getPropertyValue(theVarSlot);
			if (!getPredecessors(perform).contains(fromProcess)){
				perform.removePropertyValue(producedBindingSlot, outputBinding);
				outputBinding.removePropertyValue(valueSourceSlot, valueOf);
				valueOf.removePropertyValue(fromProcessSlot, fromProcess);
				valueOf.removePropertyValue(theVarSlot, theVar);
			}
		}
	}

	/* Prints all the Performs and their predecessors. */
	public void printAll(){
		System.out.println("---PerformTreeMapper---");
		Collection performs = map.keySet();
		Iterator performsIt = performs.iterator();
		while (performsIt.hasNext()){
			OWLIndividual perform = (OWLIndividual)performsIt.next();
			System.out.println("  " + perform.getName());
			Set preds = getPredecessors(perform);
			Iterator predsIt = preds.iterator();
			while (predsIt.hasNext()){
				OWLIndividual predPerform = (OWLIndividual)predsIt.next();
				System.out.println("    " + predPerform.getName());
			}
		}
		System.out.println("---End PerformTreeMapper---");
	}
}

class MapCell{
	private OWLIndividual composite;
	//private Vector preds = null;
	private HashSet preds = new HashSet();
	private OWLNamedClass performClass;
	
	MapCell(OWLIndividual composite){
		this.composite = composite;
		OWLModel model = composite.getOWLModel();
		performClass = model.getOWLNamedClass("process:Perform");
	}
	
	public OWLIndividual getComposite(){
		return composite;
	}
	
	public Set getPredecessors(){
		// Only return performs, not produces
		Iterator it = preds.iterator();
		HashSet performpreds = new HashSet();
		
		while (it.hasNext()){
			OWLIndividual inst = (OWLIndividual)it.next();
			if (inst.hasRDFType(performClass))
				performpreds.add(inst);
		}
		return performpreds;
	}
	
	public void addPredecessor(OWLIndividual pred){
		//System.out.println("MapCell.addPredecessor() adding predecessor " + pred.getName()); 
		preds.add(pred);
		//System.out.println("MapCell.addPredecessor() predecessors are now " + preds);
	}
	
	public void addPredecessors(Set preds){
		//System.out.println("MapCell.addPredecessors() adding predecessors " + preds);
		this.preds.addAll(preds);
		//System.out.println("MapCell.addPredecessor() predecessors are now " + preds);
	}
	
	public void removePredecessors(){
		preds.clear();
	}
}