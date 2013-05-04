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

import com.sri.owlseditor.util.OWLSList;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * This is a common abstract superclass for all control constructs that have a
 * process:components property, with a ControlConstructBag or
 * ControlConstructList value.
 * 
 * All subclassers must implement toString(), but probably nothing else.
 * 
 * @author Daniel Elenius
 * 
 */
public abstract class BagOrTreeNode extends OWLSTreeNode {

	private String type;
	private OWLModel model;
	private OWLObjectProperty components;

	protected BagOrTreeNode(OWLSTreeNodeInfo ni, String type) {
		super(ni, true);
		this.type = type;
		model = getOWLModel();
		components = model.getOWLObjectProperty("process:components");
	}

	public void updateKBAfterDelete(OWLSTreeNode node) {
		// OWLIndividual list = getComponents();
		int index = getIndex(node);
		OWLSList olist = new OWLSList(getInstance(), getOWLModel());
		olist.removeAtIndex(index, true);
		// OWLSList olist = new OWLSList(list, type, getOWLModel());
		// OWLIndividual newcell = olist.removeAtIndex(node.getInstance(),
		// index, true);
		// if (index == 0){
		// getInstance().setPropertyValue(components, newcell);
		// }
	}

	public void updateKBAfterMove(OWLSTreeNode node) {
		// OWLIndividual list = getComponents();
		int index = getIndex(node);
		OWLSList olist = new OWLSList(getInstance(), getOWLModel());
		olist.removeAtIndex(index, false);
		// OWLSList olist = new OWLSList(list, type, getOWLModel());
		// OWLIndividual newcell = olist.removeAtIndex(node.getInstance(),
		// index, false);
		// if (index == 0){
		// getInstance().setPropertyValue(components, newcell);
		// }
	}

	/*
	 * Returns the ControlConstructBag or -List containing the components of
	 * this control construct, or creates a new one if there is none.
	 */
	protected OWLIndividual getComponents() {
		OWLModel model = getOWLModel();
		OWLIndividual bagInstance = getInstance();
		OWLIndividual bag = (OWLIndividual) bagInstance
				.getPropertyValue(components);

		if (bag == null) {
			// no list, we must create a new one
			bag = getOWLModel().getOWLIndividual("list:nil");
		}
		return bag;
	}

	/**
	 * Updates the KB after adding a node.
	 */
	public void updateKBAfterInsert(OWLSTreeNode newnode) {
		/*
		 * Now we need to put the Instance in the correct place in its
		 * ControlConstructBag
		 */
		// OWLIndividual list = getComponents();
		int newindex = getIndex(newnode);
		OWLSList olist = new OWLSList(getInstance(), getOWLModel());
		olist.insertAtIndex(newnode.getInstance(), newindex);
		// OWLSList olist = new OWLSList(list, type, getOWLModel());
		// OWLIndividual newcell = olist.insertAtIndex(newnode.getInstance(),
		// newindex);
		/*
		 * If we are inserting at the beginning of the bag/list, we need to
		 * update the control construct.
		 */
		// if (newindex == 0){
		// getInstance().setPropertyValue(components, newcell);
		// }
	}

	public boolean acceptsChild() {
		return true;
	}

}
