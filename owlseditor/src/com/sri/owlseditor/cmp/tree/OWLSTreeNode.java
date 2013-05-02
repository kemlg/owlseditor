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

import javax.swing.tree.DefaultMutableTreeNode;

import com.sri.owlseditor.cmp.graph.GraphNodeInfo;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * This class (and its subclasses) handle the UI of OWL-S tree nodes, as well as
 * the Protege KB manipulation that needs to occur on node manipulation.
 * 
 * @author Daniel Elenius <elenius@csl.sri.com>
 */
public abstract class OWLSTreeNode extends DefaultMutableTreeNode {
	protected OWLSTreeNodeInfo ni;

	/** Creates a node using an existing Instance */
	public OWLSTreeNode(OWLSTreeNodeInfo ni, boolean childrenAllowed) {
		super(ni, childrenAllowed);
		this.ni = ni;
	}

	/** Creates the GraphViz graph for this node and its sub-nodes. */
	public abstract GraphNodeInfo graph(HashSet nameSet, PrintWriter pw,
			int clusterNumber, OWLSTreeNode selectedNode);

	/**
	 * Subclasses of OWLSTreeNode must implement their own logic to handle the
	 * synchronization with the protege kb after node deletion.
	 * 
	 * @param node
	 */
	public abstract void updateKBAfterDelete(OWLSTreeNode node);

	/**
	 * Subclasses of OWLSTreeNode must implement their own logic to handle the
	 * synchronization with the protege kb after moving nodes (i.e.
	 * drag-and-drop).
	 * 
	 * @param node
	 */
	public abstract void updateKBAfterMove(OWLSTreeNode node);

	/**
	 * Subclasses of OWLSTreeNode must implement their own logic to handle the
	 * synchronization with the protege kb after node insertion.
	 * 
	 * @param node
	 */
	public abstract void updateKBAfterInsert(OWLSTreeNode newnode);

	/**
	 * Subclasses of OWLSTreeNode must implement this method according to their
	 * specific rules for when to accept a new child. This is used when the user
	 * clicks the create buttons or drags a node on top of this one.
	 * 
	 * @param o
	 * @return
	 */
	public abstract boolean acceptsChild();

	public OWLModel getOWLModel() {
		return ni.getOWLModel();
	}

	public OWLIndividual getInstance() {
		return ni.getInstance();
	}

}
