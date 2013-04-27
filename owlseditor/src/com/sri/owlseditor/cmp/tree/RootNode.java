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
import java.util.HashSet;
import java.util.Vector;

import com.sri.owlseditor.cmp.graph.GraphNodeInfo;
import com.sri.owlseditor.util.OWLUtils;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * This class represents the root of the control flow tree of an OWL-S composite process.
 * It has no direct correlate in the OWL-S ontology, but is necessary for the GUI, and to perform
 * some Protege bookkeeping.
 * 
 * OWL-S processes can only have one root control construct, i.e. the value of the process:composedOf
 * property. But we allow users to create several constructs in the GUI. Only the first one will be
 * used as the value of composedOf, but it can be useful to be able to create several constructs, and
 * drag and drop around until satisfaction occurs.
 * 
 * These "secondary control constructs" will be generated in the protege KB, of course, and they will
 * be associated with the current composite process by saving these mappings in the Protege pprj file.
 * 
 * We should probably make the non-primary constructs show up in a different color or something. 
 * 
 * @author Daniel Elenius
 */
public class RootNode extends OWLSTreeNode {

	private OWLIndividual cp;  // the composite process that this node is the root of
	private OWLModel okb;
	private Vector extraConstructs;
	
	public RootNode(OWLIndividual cp, OWLModel okb){
		super(null, true);
		this.cp = cp;
		this.okb = okb;
		extraConstructs = new Vector();
	}
	
	public GraphNodeInfo graph(HashSet nameSet, PrintWriter pw, int clusterNumber,
								OWLSTreeNode selectedNode){
		// this will never be called.
		return null;
	}
	
	private String generateKey(OWLIndividual cp){
		return cp.getName() + ".EXTRAS";
	}
	
	/* Adds a mapping in the project file from the composite process to
	 * a control construct.
	 */
	private void addMapping(OWLIndividual construct){
		/*
		System.out.println("RootNode: Adding mapping for " + construct);
		extraConstructs.add(construct);
		construct.getProject().setClientInformation(generateKey(cp), extraConstructs);
		
		System.out.println("Mappings are now:");
		Object extras = construct.getProject().getClientInformation(generateKey(cp));
		Vector v = (Vector)extras; 
		for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
			System.out.println((Instance)e.nextElement());
		}
		*/
	}
	
	/* Removes a mapping in the project file from the composite process to
	 * a control construct.
	 */
	private void removeMapping(OWLIndividual construct){
		/*
		System.out.println("RootNode: Removing mapping for " + construct);
		extraConstructs.remove(construct);
		// not sure if this is the way to remove these things
		construct.getProject().setClientInformation(generateKey(cp), extraConstructs);   

		System.out.println("Mappings are now:");
		Object extras = construct.getProject().getClientInformation(generateKey(cp));
		Vector v = (Vector)extras; 
		for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
			System.out.println((Instance)e.nextElement());
		}
		*/
	}
	
	
	/* (non-Javadoc)
	 * @see com.sri.owlseditor.cmp.tree.OWLSTreeNode#updateKBAfterDelete(com.sri.owlseditor.cmp.tree.OWLSTreeNode)
	 */
	public void updateKBAfterDelete(OWLSTreeNode node) {
		int index = getIndex(node);
		if (index == 0){
			if (getChildCount() > 1){
				// need to put index 1 node as index 0 node
				OWLSTreeNode newfirst = (OWLSTreeNode)getChildAt(1);
				removeMapping(newfirst.getInstance());
				updateCompositeProcess(newfirst.getInstance());
			}
			else{
				// remove the composedOf property since we're deleting the last construct
				updateCompositeProcess(null);
			}
		}
		else{
			removeMapping(node.getInstance());
		}
		//getOWLModel().deleteInstance(node.getInstance());
		
		System.out.println("RootNode.updateKBAfterDelete: deleting " + 
								node.getInstance().getName());
		
		node.getInstance().delete();
	}

	/* (non-Javadoc)
	 * @see com.sri.owlseditor.cmp.tree.OWLSTreeNode#updateKBAfterMove(com.sri.owlseditor.cmp.tree.OWLSTreeNode)
	 */
	public void updateKBAfterMove(OWLSTreeNode node) {
		int index = getIndex(node);
		if (index == 0){
			if (getChildCount() > 1){
				// need to put index 1 node as index 0 node
				OWLSTreeNode newfirst = (OWLSTreeNode)getChildAt(1);
				removeMapping(newfirst.getInstance());
				updateCompositeProcess(newfirst.getInstance());
			}
			else{
				// remove the composedOf property since we're deleting the last construct
				updateCompositeProcess(null);
			}
		}
		else{
			removeMapping(node.getInstance());
		}
	}

	/* (non-Javadoc)
	 * @see com.sri.owlseditor.cmp.tree.OWLSTreeNode#updateKBAfterInsert(com.sri.owlseditor.cmp.tree.OWLSTreeNode)
	 */
	public void updateKBAfterInsert(OWLSTreeNode newnode) {
		int index = getIndex(newnode);
		
		if (index == 0){
			// need to update composedOf property
			updateCompositeProcess(newnode.getInstance());
			if (getChildCount() > 1){
				// previous index 0 node must be put as an "extra construct"
				addMapping(((OWLSTreeNode)getChildAt(1)).getInstance());
			}
		}
		else{
			// this is not the index 0 node, so put it as an "extra construct"
			addMapping(newnode.getInstance());
		}
	}

	/* Called after a new main control construct is set on a composite process.
	 * Updates the composedOf property on the c.p.
	 */
	private void updateCompositeProcess(OWLIndividual construct){
		//System.out.println("Updating composite process " + cp +
		//					" with process:composedOf = " + construct.getName());
		if (construct != null){
			OWLUtils.setNamedSlotValue(cp, 
					"process:composedOf",
					construct,
					getOWLModel());
		}
		else{
			OWLUtils.removeNamedSlotValue(cp, "process:composedOf", getOWLModel());
		}
	}

	/* (non-Javadoc)
	 * @see com.sri.owlseditor.cmp.tree.OWLSTreeNode#acceptsChild()
	 */
	public boolean acceptsChild() {
		return true;
	}

	public OWLModel getOWLModel(){
		return okb;
	}

	public String toString(){
		return "Root of " + cp.getName();
	}
	
	/* Hopefully no-one will call this */
	public OWLIndividual getInstance(){
		return null;
	}
}
