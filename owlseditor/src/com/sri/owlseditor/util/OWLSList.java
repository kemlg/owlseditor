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

/*
 * Created on Oct 8, 2004
 *
 * This class wraps the list classes used in OWL-S so that they can more easily 
 * be used programmatically.
 *
 *  */
package com.sri.owlseditor.util;

import java.util.Iterator;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * @author Daniel Elenius <elenius@csl.sri.com>
 *
 * TODO
 */
public class OWLSList implements Iterator {
	public static final String LIST = "list:List";
	public static final String CC_LIST = "process:ControlConstructList";
	public static final String CC_BAG = "process:ControlConstructBag";
	// Note that the following two are deprecated, so we'll proably never use them.
	public static final String WSDL_IN = "grounding:WsdlInputMessageMapList";
	public static final String WSDL_OUT = "grounding:WsdlOutputMessageMapList";
	
	private String type;	// the list (sub-)class we are dealing with 
	private OWLIndividual next;  // should always point to the next element in the list
	private OWLModel okb;
	private OWLIndividual controlConstruct;

	private OWLObjectProperty componentsProperty;
	private OWLNamedClass sequenceCls;
	private OWLObjectProperty firstProperty;
	private OWLObjectProperty restProperty;
	
	/*
	public OWLSList(OWLIndividual inst, String type, OWLModel okb) {
		next = inst;
		this.okb = okb;
		this.type = type;
		
		// This seemed like a good idea, but it breaks down for empty lists,
		// because they always start with the nil element, which is of type List.
		//type = OWLUtils.getClassNameOfInstance(inst);
		
		//System.out.println("Creating OWLSList for " + inst.getName() +
		//					" of type " + type);
	}
	*/
	
	public OWLSList(OWLIndividual controlConstruct, OWLModel model){
		this.okb = model;
		this.controlConstruct = controlConstruct;

		//System.out.println("OWLSList creating list for control construct " +
		//					controlConstruct.getName() + " of class " + 
		//					controlConstruct.getRDFType().getName());
		
		setupClassesAndProperties();
		
		OWLIndividual firstCell = (OWLIndividual)controlConstruct.
									getPropertyValue(componentsProperty);
		if (firstCell == null){
			firstCell = getNil();
			controlConstruct.setPropertyValue(componentsProperty, firstCell);
		}
		next = firstCell;
		
		if (controlConstruct.hasRDFType(sequenceCls, true))
			type = CC_LIST; 
		else
			type = CC_BAG;
	}

	private void setupClassesAndProperties(){
		sequenceCls = okb.getOWLNamedClass("process:Sequence");
		componentsProperty = okb.getOWLObjectProperty("process:components");
		firstProperty = okb.getOWLObjectProperty("list:first");
		restProperty = okb.getOWLObjectProperty("list:rest");
	}
	
	private OWLIndividual getFirst(OWLIndividual cell){
		return (OWLIndividual)cell.getPropertyValue(firstProperty);
	}

	private OWLIndividual getRest(OWLIndividual cell){
		return (OWLIndividual)cell.getPropertyValue(restProperty);
	}

	private void setFirst(OWLIndividual cell, OWLIndividual value){
		cell.setPropertyValue(firstProperty,value);
	}

	private void setRest(OWLIndividual cell, OWLIndividual value){
		cell.setPropertyValue(restProperty,value);
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		// TODO Auto-generated method stub

	}

	/** Returns the list:nil instance */
	public OWLIndividual getNil(){
		return okb.getOWLIndividual("list:nil");
	}
	
	/*
	public static Instance createEmptyCell(String type, OWLModel okb){
		OWLNamedClass listCls = okb.getOWLNamedClass(type);
		Instance newcell = listCls.createDirectInstance(null);
		OWLUtils.setNamedSlotValue(newcell, "list:rest", okb.getInstance("list:nil"), okb);
		return newcell;
	}
	*/
	
	/* Creates a list cell for the correct type of list/bag, and
	 * puts inst as the value of the list:first property
	 */
	private OWLIndividual createCell(OWLIndividual inst){
		OWLNamedClass listCls = okb.getOWLNamedClass(type);
		OWLIndividual newcell = listCls.createOWLIndividual(null);
		
		setFirst(newcell, inst);
		setRest(newcell, getNil());
		//System.out.println("Creating new list cell with name " + newcell.getName() +
		//					" for instance " + inst.getName());				
		return newcell;
	}
	
	private void removeCell(OWLIndividual cell, boolean deleteInst){
		OWLIndividual inst = getFirst(cell);
		//System.out.println("removeCell() removing list cell " + cell.getName());
		if (deleteInst){
			//System.out.println(" and its element " + inst.getName());
			inst.delete();
		}
		cell.delete();
	}
	
	/* Prints all cells starting at the current one */
	public void printAll(){
		while (hasNext()){
			printCell(next);
			OWLIndividual dummy = (OWLIndividual)next(); 
		}
	}
		
	/* Just for debugging */
	private void printCell(OWLIndividual cell){
		if (cell == getNil())
			System.out.println("list:nil");
		else{
			OWLIndividual first = (OWLIndividual)getFirst(cell);
			OWLIndividual rest = (OWLIndividual)getRest(cell);
			System.out.println("Cell " + cell.getName() + " of type " + type);
			System.out.println("  element (list:first value): " + first.getName());
			System.out.println("  next: " + rest.getName());
		}
	}
	
	/* Creates a new list cell of the correct type, puts inst as the value of
	 * the list:first property on the list cell, and updates the list:rest pointers
	 * of the list to give this cell its correct position in the list. 
	 * 
	 * If the index to insert at is 0, the return value will be the new list cell
	 * for inst. This is needed so that the caller can update the process:components
	 * property (or similar property) of the containing control construct. If index is
	 * not 0, the return value should be null.
	 * 
	 * */ 
	public void insertAtIndex(OWLIndividual inst, int index){
		//System.out.println("inserting " + inst.getName() + " at index " + index);
		
		int i = 0;
		OWLIndividual dummy = null;   // we don't use this
		OWLIndividual cell = null;
		
		// reset list
		first();
		
		//System.out.println("Going through list");
		while (hasNext() && i < index){
			cell = next;
			dummy = (OWLIndividual)next();
			//printCell(cell);
			i++;
		}
		if (i < index){
			/* We could not reach the node before the new one in the list,
			   something has gone wrong */
			System.out.println("WARNING: insertAtIndex() could not reach the desired node index: " + index);
			//return null;
			return;
		}
		else{
			// create new cell
			OWLIndividual newcell = createCell(inst);
			//System.out.println("Created new cell:");
			//printCell(newcell);

			// set list:rest property on the new cell
			
			//System.out.println("Setting rest of new cell to:");
			//printCell(next);
			
			setRest(newcell, next);
							
			// Set list:rest property on the previous cell
			if (i == 0){
				//System.out.println("New cell is first in list, no previous cell to modify");
				// We return the new cell so caller can update containing control construct
				controlConstruct.setPropertyValue(componentsProperty, newcell);
				//return newcell;
				return;
			}
			else{
				//System.out.println("New cell is not first in list, modifying previous cell");
				setRest(cell, newcell);
			}
		}
		//return null;
	}
	
	/** 
	 * Remove the list cell at the given index, and the control constructs
	 * and list cells below it, but not processes pointed to by Performs, etc.
	 * 
	 * If the index to remove at is 0, the return value will be the new first list cell
	 * This is needed so that the caller can update the process:components
	 * property (or similar property) of the containing control construct. If index is
	 * not 0, the return value should be null.
	 * 
	 * @param deleteInst If true, the control construct contained in the node will also be
	 * deleted. If false, only the list cell will be removed. The latter case is used when
	 * a node is moved using drag and drop.
	 *
	 */ 
	public void removeAtIndex(int index, boolean deleteInst){
		//System.out.println("Removing element at index " + index + " in OWL-S list.");
		if (index == 0){
			/* this requires some special handling
			 (setting the components property of the containing 
			 control construct to point to the cell on index 1) */
			//System.out.println("Deleting first element in list.");
			//Instance construct = getContainingConstruct(next);
			OWLIndividual nextvalue = (OWLIndividual)getRest(next);
			controlConstruct.setPropertyValue(componentsProperty, nextvalue);
			removeCell(next, deleteInst);
			//return nextvalue;  // caller must update containing control construct
		}
		else{
			int i = 0;
			OWLIndividual dummy = null;   // we don't use this
			OWLIndividual cell = null;

			// reset list
			first();
			
			//System.out.println("Going through list");
			while (hasNext() && i < index){
					cell = next;
					dummy = (OWLIndividual)next();
					//System.out.println("old cell is:");
					//printCell(cell);
					i++;
			}
			if (i < index){
				/* We could not reach the node before the new one in the list,
				 something has gone wrong */
				System.out.println("WARNING: removeAtIndex() could not reach the desired node index: " + index);
			}
			else{
				// get the node following the one to be deleted
				//System.out.println("Deleting cell");
				//printCell(next);
				OWLIndividual followingcell = (OWLIndividual)getRest(next);
				if (followingcell == null){
					System.out.println("ERROR! following cell is null! This cannot happen");
					//return null;
					return;
				}
					
				// set list:rest pointer on previous node to following node
				//System.out.println("Setting list:rest on previous cell: " + cell.getName() + 
				//					" to " + followingcell.getName());
				setRest(cell, followingcell);
								
				// delete the cell to be deleted
				removeCell(next, deleteInst);
			}
		}
		//return null;
	}
	
	/* Resets the next pointer and returns the first list element */
	public OWLIndividual first(){
		OWLIndividual first = (OWLIndividual)controlConstruct.getPropertyValue(componentsProperty); 
		next = first;
		return first;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (next.getName().equals("list:nil"))
			return false;
		else
			return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		Object retval = (Object) getFirst(next);
		next = (OWLIndividual) getRest(next);
		//System.out.println("OWLSList.next() returning " + ((Instance)retval).getName());
		//System.out.println("OWLSList.next() setting next to " + next.getName());
		return retval;
	}

}
