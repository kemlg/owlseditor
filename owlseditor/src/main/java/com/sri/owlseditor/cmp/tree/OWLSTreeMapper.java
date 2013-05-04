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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import com.sri.owlseditor.cmp.GraphUpdateManager;
import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;
import com.sri.owlseditor.util.OWLSList;
import com.sri.owlseditor.util.OWLUtils;
import com.sri.owlseditor.widgets.dataflow.PerformTreeMapper;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * This class maintains the mapping between composite processes and their
 * OWLSTrees, so that we don't have to recreate trees each time we switch
 * composite process. This class follows the Singleton design pattern.
 * 
 * This class implements the CleanerListener interface to remove all
 * TreeModelListeners from all its TreeModels when the thing shuts down, and to
 * create a new instance for the new KB (see below).
 * 
 */
public class OWLSTreeMapper implements CleanerListener {
	private HashMap process2tree;
	private HashMap tree2process;
	private OWLModel okb;
	private static OWLSTreeMapper instance;

	private OWLSTreeMapper(OWLModel okb) {
		process2tree = new HashMap();
		tree2process = new HashMap();
		Cleaner.getInstance().registerCleanerListener(this);
		this.okb = okb;
	}

	/** The instance is generated on first use */
	public static OWLSTreeMapper getInstance(OWLModel okb) {
		if (instance == null) {
			instance = new OWLSTreeMapper(okb);
			return instance;
		} else {
			if (instance.okb != okb) {
				System.out
						.println("ERROR! This OWLSTreeMapper was created for a different"
								+ " KB "
								+ instance.okb
								+ ", and is now being used with " + okb);
				return null;
			} else
				return instance;
		}
	}

	/**
	 * Remove all TreeModelListeners of all TreeModels in the models we have,
	 * and reset the instance pointer, so that a new Mapper is created for the
	 * new KB.
	 */
	public void cleanup() {
		// System.out.println("OWLSTreeMapper: removing TreeModelListeners");
		Iterator it = tree2process.keySet().iterator();
		while (it.hasNext()) {
			OWLSTree tree = (OWLSTree) it.next();
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			TreeModelListener listeners[] = model.getTreeModelListeners();
			int i = 0;
			for (TreeModelListener listener = listeners[i]; i < listeners.length; i++) {
				// System.out.println(listener);
				model.removeTreeModelListener(listener);
			}
		}
		// Reset the mapper.
		instance = null;
	}

	/** Adds mappings both ways process <-> tree */
	public void addMapping(OWLIndividual process, OWLSTree tree) {
		process2tree.put(process, tree);
		tree2process.put(tree, process);
	}

	private String generateKey(OWLIndividual cp) {
		return cp.getName() + ".EXTRAS";
	}

	/* Removes the tree for this composite process (i.e. sets it to null) */
	public void removeTree(OWLIndividual process) {
		OWLSTree tree = (OWLSTree) process2tree.get(process);
		tree.removeListeners();
		process2tree.remove(process);
	}

	/**
	 * Returns the OWLSTree associated with a given composite process. If there
	 * is no tree, one will be created (that is the whole beauty of this class).
	 * 
	 * @param process
	 * @param listener
	 * @return
	 */
	public OWLSTree getTree(OWLIndividual process, GraphUpdateManager mgr) {
		OWLSTree tree = (OWLSTree) process2tree.get(process);
		if (tree == null) {
			/*
			 * We have no model for this process, so we create one and add a
			 * mapping for it
			 */
			// System.out.println("OWLSTreeMapper.getTree, creating tree for " +
			// process.getName());

			tree = createTree(process, mgr);
			tree.expandAll(); // Newly created trees are always fully expanded.
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			// model.addTreeModelListener(tree);
			addMapping(process, tree);
		} else {
			Vector extraConstructs = (Vector) okb
					.getClientInformation(generateKey(process));
			if (extraConstructs != null) {
				// There are extra constructs stored in the project file, so we
				// have
				// to add those to the model

				System.out.println("Process " + process
						+ " has extra constructs.");

				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				OWLSTreeNode root = (OWLSTreeNode) model.getRoot();
				for (Enumeration e = extraConstructs.elements(); e
						.hasMoreElements();) {
					OWLIndividual construct = (OWLIndividual) e.nextElement();
					OWLSTreeNodeInfo ni = new OWLSTreeNodeInfo(construct, okb);
					OWLSTreeNode newnode = OWLSTreeNodeFactory
							.createTreeNode(ni);
					System.out.println("Looking at " + newnode);
					model.insertNodeInto(newnode, root, root.getChildCount());
				}
			}
		}
		tree.setSelectedCompositeProcess(process);
		return tree;
	}

	/**
	 * Returns the composite process Instance associated with a given OWLSTree.
	 * 
	 * @param tree
	 * @return
	 */
	public OWLIndividual getProcess(OWLSTree tree) {
		return (OWLIndividual) tree2process.get(tree);
	}

	/**
	 * Creates a complete OWLSTree for a composite process instance
	 * 
	 * @param inst
	 * @return
	 */
	private OWLSTree createTree(OWLIndividual inst, GraphUpdateManager mgr) {
		OWLSTreeNode root = new RootNode(inst, okb);
		OWLSTreeNode children = getCompositionTreeNodes(inst);
		if (children != null)
			root.add(children);
		DefaultTreeModel treemodel = new DefaultTreeModel(root, true);
		OWLSTree tree = new OWLSTree(okb, treemodel, mgr);
		// tree.setModel(treemodel);
		return tree;
	}

	/**
	 * Creates the tree for a composite process instance, and returns the root
	 * node of this tree.
	 */
	private OWLSTreeNode getCompositionTreeNodes(OWLIndividual inst) {
		OWLIndividual i = (OWLIndividual) OWLUtils.getNamedSlotValue(inst,
				"process:composedOf", okb);

		if (i != null) {
			OWLSTreeNodeInfo newni = new OWLSTreeNodeInfo(i, okb);
			OWLSTreeNode node = createTree(newni, inst, new HashSet());
			// PerformTreeMapper mapper = PerformTreeMapper.getInstance();
			// mapper.printAll();
			return node;
		} else
			return null;
	}

	/*
	 * Returns the process:components value for a Control Construct. If it is
	 * null, it will be set to list:nil, and this will be returned.
	 */
	private OWLIndividual getComponents(OWLIndividual inst) {
		OWLIndividual components = (OWLIndividual) OWLUtils.getNamedSlotValue(
				inst, "process:components", okb);
		if (components == null) {
			components = okb.getOWLIndividual("list:nil");
			OWLUtils.setNamedSlotValue(inst, "process:components", components,
					okb);
		}
		return components;
	}

	/*
	 * This is just a record type return value for the createTree method. We
	 * need to return both the OWLSTreeNode created, and the Collection of
	 * predecesors.
	 */
	/*
	 * class CreateInfo{ private OWLSTreeNode node; private Vector preds;
	 * 
	 * CreateInfo(OWLSTreeNode node, Vector preds){ this.node = node; this.preds
	 * = preds; }
	 * 
	 * OWLSTreeNode getNode(){ return node; }
	 * 
	 * Vector getPredecessors(){ return preds; } }
	 */

	/**
	 * Creates the tree from a ControlConstruct. It returns the root node
	 * created even though the caller already has it, since the method is
	 * recursive, and this is needed internally. This method is only called when
	 * the user switches to a different composite process, to recreate the whole
	 * tree.
	 * 
	 * Note: This method is not used to create RootNode instances - those are
	 * created directly (i.e. using new RootNode()).
	 * 
	 * We also generate Vectors of unconditional predecessor Performs, and
	 * associate those with the Perform constructs using the PerformTreeMapper
	 * class. See that class for more info.
	 * 
	 * */
	private OWLSTreeNode createTree(OWLSTreeNodeInfo ni, OWLIndividual cp,
			Set preds) {
		OWLIndividual inst = ni.getInstance();
		if (inst == null) {
			System.out
					.println("ERROR! OWLSTreeMapper.createTree(): inst is null!");
			return null;
		}

		OWLSTreeNode newnode = OWLSTreeNodeFactory.createTreeNode(ni);
		// System.out.println("Building tree for " + newnode.toString());

		if (newnode instanceof ProduceNode) {
			// Store the mapping from produce to composite process in the
			// PerformTreeMapper. This will allow us to generate the Produce's
			// parents, which we need for the ProducedBindingWidget.
			OWLIndividual produce = newnode.getInstance();
			PerformTreeMapper mapper = PerformTreeMapper.getInstance();
			mapper.put(produce, cp);
			mapper.addPredecessors(produce, preds);
		} else if (newnode instanceof PerformNode) {
			// Store the mapping from perform to composite process in the
			// PerformTreeMapper. This will allow us to generate the Performs's
			// parents, which we need for the HasDataFromWidget.
			OWLIndividual perform = newnode.getInstance();
			PerformTreeMapper mapper = PerformTreeMapper.getInstance();
			mapper.put(perform, cp);
			mapper.addPredecessors(perform, preds);
			preds.add(perform);
			// mapper.printAll();
		} else if (newnode instanceof SequenceNode) {
			OWLIndividual components = getComponents(inst);
			// OWLSList clist = new OWLSList(components, OWLSList.CC_LIST, okb);
			OWLSList clist = new OWLSList(inst, okb);
			while (clist.hasNext()) {
				OWLIndividual i = (OWLIndividual) clist.next();
				if (i == null)
					System.out.println("instance in list is null!");
				OWLSTreeNodeInfo newni = new OWLSTreeNodeInfo(i, okb);
				newnode.add(createTree(newni, cp, preds));
			}
		} else if (newnode instanceof SplitNode
				|| newnode instanceof ChoiceNode) {
			OWLIndividual components = getComponents(inst);
			// OWLSList clist = new OWLSList(components, OWLSList.CC_BAG, okb);
			OWLSList clist = new OWLSList(inst, okb);
			while (clist.hasNext()) {
				HashSet mypreds = new HashSet(preds);
				OWLIndividual i = (OWLIndividual) clist.next();
				OWLSTreeNodeInfo newni = new OWLSTreeNodeInfo(i, okb);
				newnode.add(createTree(newni, cp, mypreds));
			}
		} else if (newnode instanceof SplitJoinNode
				|| newnode instanceof AnyOrderNode) {
			OWLIndividual components = getComponents(inst);
			// OWLSList clist = new OWLSList(components, OWLSList.CC_BAG, okb);
			OWLSList clist = new OWLSList(inst, okb);
			HashSet predsIn = new HashSet(preds);
			while (clist.hasNext()) {
				OWLIndividual i = (OWLIndividual) clist.next();
				OWLSTreeNodeInfo newni = new OWLSTreeNodeInfo(i, okb);
				HashSet mypreds = new HashSet(predsIn);
				newnode.add(createTree(newni, cp, mypreds));
				preds.addAll(mypreds);
			}
		}
		// We put the then-node first and the else-node second as a convention
		// This may need to be handled better (marking the nodes somehow).
		else if (newnode instanceof IfThenElseNode) {
			OWLIndividual thenc = (OWLIndividual) OWLUtils.getNamedSlotValue(
					inst, "process:then", okb);
			OWLSTreeNodeInfo thenni = new OWLSTreeNodeInfo(thenc, okb);
			HashSet mypreds = new HashSet(preds);
			newnode.add(createTree(thenni, cp, mypreds));
			OWLIndividual elsec = (OWLIndividual) OWLUtils.getNamedSlotValue(
					inst, "process:else", okb);
			OWLSTreeNodeInfo elseni = new OWLSTreeNodeInfo(elsec, okb);
			if (elsec != null) {
				mypreds = new HashSet(preds);
				newnode.add(createTree(elseni, cp, mypreds));
			}
		} else if (newnode instanceof RepeatWhileNode) {
			OWLIndividual whileProcess = (OWLIndividual) OWLUtils
					.getNamedSlotValue(inst, "process:whileProcess", okb);
			OWLSTreeNodeInfo newni = new OWLSTreeNodeInfo(whileProcess, okb);
			HashSet mypreds = new HashSet(preds);
			newnode.add(createTree(newni, cp, mypreds));
		} else if (newnode instanceof RepeatUntilNode) {
			OWLIndividual untilProcess = (OWLIndividual) OWLUtils
					.getNamedSlotValue(inst, "process:untilProcess", okb);
			OWLSTreeNodeInfo newni = new OWLSTreeNodeInfo(untilProcess, okb);
			newnode.add(createTree(newni, cp, preds));
		} else {
			System.out.println("Unsupported control construct");
			return null;
		}
		return newnode;
	}

}
