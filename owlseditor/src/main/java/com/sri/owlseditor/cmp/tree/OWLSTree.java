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

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.pvv.bcd.instrument.JTree.Instrumenter;
import org.pvv.bcd.instrument.JTree.VetoException;
import org.pvv.bcd.instrument.JTree.VetoableTreeStructureChangeEvent;
import org.pvv.bcd.instrument.JTree.VetoableTreeStructureChangeListener;

import com.sri.owlseditor.cmp.GraphUpdateManager;
import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;
import com.sri.owlseditor.widgets.dataflow.PerformTreeMapper;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * This class extends JTree with some methods to keep the underlying protege kb
 * in sync with the tree, and to use our custom tree classes (OWLSTreeNode,
 * OWLSTreeNodeInfo, OWLSTreeMapper, etc).
 * 
 * @author Daniel Elenius
 * 
 */
public class OWLSTree extends JTree implements CleanerListener,
		VetoableTreeStructureChangeListener {

	private Instrumenter instrument;
	private OWLModel okb;
	private OWLIndividual selectedCompositeProcess;
	private DataFlavor OWLSFlavor;
	private GraphUpdateManager mgr;

	public OWLSTree(OWLModel okb, DefaultTreeModel treemodel,
			GraphUpdateManager mgr) {
		this.okb = okb;
		this.mgr = mgr;
		setModel(treemodel);
		instrument = Instrumenter.createInstrument(this);
		instrument.setNodeFactory(OWLSTreeNodeFactory.getInstance(okb));
		setRootVisible(false);
		setShowsRootHandles(true);
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		// we just make sure this gets initialized
		OWLSDataFlavorProvider dummy = OWLSDataFlavorProvider.getInstance();
		Cleaner.getInstance().registerCleanerListener(this);
		setCellRenderer(new OWLSTreeCellRenderer());
		addListeners();
	}

	public void addListeners() {
		instrument.addVetoableTreeStructureChangeListener(this);
		mgr.addListeners();
		// getModel().addTreeModelListener(this);
		// The following is not needed anymore. We do this in the listeners in
		// CompositionEditor.
		// okb.addPropertyValueListener(processListener);
	}

	public void removeListeners() {
		instrument.removeVetoableTreeStructureChangeListener(this);
		mgr.removeListeners();
		// getModel().removeTreeModelListener(this);
		// okb.removePropertyValueListener(processListener);
	}

	/** Kill of some listener threads when we close the project */
	public void cleanup() {
		// instrument.removeVetoableTreeStructureChangeListeners();
		instrument.removeListeners(); // make sure this one also removes the
										// vetoable... listeners
		removeListeners();
		// getModel().removeTreeModelListener(this);
		instrument = null;
	}

	public OWLIndividual getSelectedProcess() {
		return selectedCompositeProcess;
	}

	public OWLSTreeNode getRoot() {
		return (OWLSTreeNode) getModel().getRoot();
	}

	public void selectRoot() {
		TreePath rootPath = new TreePath(getRoot().getPath());
		setSelectionPath(rootPath);
	}

	/**
	 * We override this and call setExpandedState, since expandState doesn't
	 * seem to work.
	 */
	public void expandPath(TreePath path) {
		setExpandedState(path, true);
	}

	/* Expands all branches of the tree */
	public void expandAll() {
		expandSubTree(getRoot());
	}

	private void expandSubTree(OWLSTreeNode root) {
		for (Enumeration e = root.children(); e.hasMoreElements();) {
			OWLSTreeNode node = (OWLSTreeNode) e.nextElement();
			if (node.getChildCount() == 0) {
				TreePath path = new TreePath(node.getPath());
				expandPath(new TreePath(node.getPath()));
			} else
				expandSubTree(node);
		}
	}

	/**
	 * This makes sure that the tree is unselected when the user clicks outside
	 * of the tree. What actually happens is that the (invisible) root is
	 * selected, so that any subsequent node creation will take place under the
	 * root.
	 * 
	 * Big thanks to Bent Dalager, bcd@pvv.ntnu.no, for this code.
	 */
	public void processMouseEvent(MouseEvent ev) {
		if (ev.getID() == MouseEvent.MOUSE_CLICKED) {
			TreePath path = getClosestPathForLocation(ev.getX(), ev.getY());
			Rectangle bounds = getPathBounds(path);
			if (bounds == null) {
				selectRoot();
				return;
			}
			Rectangle nodeHandles = new Rectangle(0, 0, bounds.x, bounds.y);
			Rectangle nodeRect = bounds.union(nodeHandles);
			if (!nodeRect.contains(ev.getPoint())) {
				// setSelectionPath(new TreePath(getModel().getRoot()));
				selectRoot();
			}
		}
		// TODO: this causes a NullPointerException when the user first deletes
		// the root construct, and then clicks on the background.
		//
		// Fixed - I think.
		//
		// But also when root construct is dragged into second construct, and
		// then
		// something is clicked. Odd!
		super.processMouseEvent(ev);
	}

	public void setSelectedCompositeProcess(OWLIndividual inst) {
		selectedCompositeProcess = inst;
	}

	public OWLSTreeNode getSelectedNode() {
		TreePath path = getSelectionPath();
		if (path == null)
			// Nothing selected
			return null;
		else
			return (OWLSTreeNode) path.getLastPathComponent();
	}

	private OWLIndividual createControlConstructInstance(String type) {
		OWLNamedClass choiceCls = okb.getOWLNamedClass(type);
		OWLIndividual inst = choiceCls.createOWLIndividual(null);
		// System.out.println(type + " created with name " + inst.getName());
		return inst;
	}

	/**
	 * Creates a node of a specified type and adds it as the last child of the
	 * currently selected node in the tree. Used by the add buttons.
	 */
	public void addNode(String type) {
		OWLSTreeNode selectedNode = getSelectedNode();
		if (selectedNode == null || selectedNode.acceptsChild()) {
			OWLSTreeNode parent;
			DefaultTreeModel model = (DefaultTreeModel) getModel();
			if (selectedNode == null) {
				parent = (OWLSTreeNode) model.getRoot();
			} else {
				parent = selectedNode;
			}
			removeListeners();
			OWLIndividual ccinst = createControlConstructInstance(type);
			OWLSTreeNodeInfo ni = new OWLSTreeNodeInfo(ccinst, okb);
			OWLSTreeNode newnode = OWLSTreeNodeFactory.createTreeNode(ni);
			model.insertNodeInto(newnode, parent, parent.getChildCount());
			addListeners();
			nodeAdded(newnode);
		} else {
			// Selected node cannot take more children
			// Display useful warning
			JOptionPane.showMessageDialog(null, "Cannot create " + type
					+ " construct at the selected location.",
					"Error creating control construct!",
					JOptionPane.ERROR_MESSAGE);
		}
		mgr.updateGraph();
	}

	/**
	 * Delete a node and the whole tree starting at this node (depth-first),
	 * both the tree objects and the protege objects. Called when the delete
	 * button is clicked.
	 */
	public void deleteNode(OWLSTreeNode node) {
		removeListeners();

		// Delete all children
		int c = node.getChildCount();
		for (int i = 0; i < c; i++) {
			/*
			 * We keep deleting child with index 0, which will range over all
			 * child nodes.
			 */
			OWLSTreeNode child = (OWLSTreeNode) node.getChildAt(0);
			deleteNode(child);
		}

		// Update Protege KB
		nodeDeleted(node);

		// Delete this node from the tree model
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.removeNodeFromParent(node); // does this delete whole subtree?

		// Delete this node from the tree model
		// DefaultTreeModel model = (DefaultTreeModel)getModel();
		// model.removeNodeFromParent(node); // does this delete whole subtree?

		// Change selection to root node
		selectRoot();

		mgr.updateGraph();
		addListeners();
	}

	/**
	 * Adds an existing OWLSTreeNode at a specified place in the tree. Called by
	 * both addNode() (manual node creation) and nodeCreation() (dnd node move).
	 */
	public void nodeAdded(OWLSTreeNode node) {
		OWLIndividual newinst = node.getInstance();
		OWLSTreeNode parentnode = (OWLSTreeNode) node.getParent();
		OWLIndividual parent = parentnode.getInstance();
		parentnode.updateKBAfterInsert(node);

		if (((node instanceof PerformNode) || (node instanceof ProduceNode))
				&& !creationPerformed) {
			OWLSTreeNode root = getRoot();
			OWLIndividual perform = node.getInstance();
			PerformTreeMapper.getInstance().generatePredecessors(root, perform,
					getSelectedProcess());
		}

		TreePath path = new TreePath(node.getPath());
		setSelectionPath(path); // change selection to the newly added node
		expandPath(path);
		scrollPathToVisible(path);
	}

	/** Removes all dataflow with the given perform as its source. */
	protected void removeOutgoingDataflow(OWLIndividual theperform) {
		OWLNamedClass bindingCls = okb.getOWLNamedClass("process:Binding");
		OWLObjectProperty valueSource = okb
				.getOWLObjectProperty("process:valueSource");
		OWLObjectProperty fromProcess = okb
				.getOWLObjectProperty("process:fromProcess");

		Collection bindings = bindingCls.getInstances(true);
		Iterator it = bindings.iterator();
		while (it.hasNext()) {
			OWLIndividual binding = (OWLIndividual) it.next();
			OWLIndividual valueOf = (OWLIndividual) binding
					.getPropertyValue(valueSource);
			if (valueOf != null) {
				OWLIndividual fromPerform = (OWLIndividual) valueOf
						.getPropertyValue(fromProcess);
				if (fromPerform == theperform) {
					// This binding has this perform as its source, so delete it
					valueOf.delete();
					binding.delete();
				}
			}
		}
	}

	/** This is only called by deleteNode(), i.e. manual node deletion */
	public void nodeDeleted(OWLSTreeNode node) {
		OWLSTreeNode parent = (OWLSTreeNode) node.getParent();

		// System.out.println("Deleting node " + node + " with parent " +
		// parent);

		boolean regenerate_preds = false;

		if (node instanceof PerformNode) {
			((PerformNode) node).deleteBindings();
			if (PerformTreeMapper.getInstance().contains(node.getInstance()))
				regenerate_preds = true;
			removeOutgoingDataflow(node.getInstance());
		}

		if (node instanceof ProduceNode) {
			((ProduceNode) node).deleteBindings();
			if (PerformTreeMapper.getInstance().contains(node.getInstance()))
				regenerate_preds = true;
			removeOutgoingDataflow(node.getInstance());
		}

		if (parent != null)
			// parent.updateKBAfterMove(node);
			parent.updateKBAfterDelete(node);
		else
			System.out.println("ERROR! Tried to delete root node!");

		if (regenerate_preds) {
			OWLSTreeNode root = getRoot();
			PerformTreeMapper.getInstance().generatePredecessors(root,
					node.getInstance(), getSelectedProcess());
		}
	}

	/** This is only called by nodeDeleted, i.e. dnd move. */
	private void nodeMoved(OWLSTreeNode node) {
		OWLSTreeNode parent = (OWLSTreeNode) node.getParent();
		boolean regenerate_preds = false;

		if (node instanceof PerformNode || node instanceof ProduceNode) {
			if (PerformTreeMapper.getInstance().contains(node.getInstance()))
				regenerate_preds = true;
		}

		if (parent != null)
			parent.updateKBAfterMove(node);
		else
			System.out.println("ERROR! Tried to move root node!");

		if (regenerate_preds) {
			OWLSTreeNode root = getRoot();
			PerformTreeMapper.getInstance().generatePredecessors(root,
					node.getInstance(), getSelectedProcess());
		}
	}

	/* --- Listeners --- */

	/*
	 * VetoableTreeStructureChangeListener methods. These are _only_ called from
	 * IJTree methods, and only happen on drag-and-drop. Since there can only be
	 * one drag-and-drop going on at a time, we can use an instance variable to
	 * keep track of the state of the dnd.
	 * 
	 * Vetoing a change is done by throwing a VetoException.
	 */

	private boolean nodeCreationInProgress = false;
	private boolean vetoCurrentDnd = false;
	private boolean creationPerformed = false;

	/* This method only gets the NewIndex and NewParent in the event */
	public void nodeCreation(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		nodeCreationInProgress = true;
		vetoCurrentDnd = false;

		// System.out.println("Vetoable node creation");
		OWLSTreeNode node = (OWLSTreeNode) e.getNode();
		int index = e.getNewIndex();
		OWLSTreeNode parent = (OWLSTreeNode) e.getNewParent();
		// System.out.println("New index: " + index + ", New parent: " +
		// parent);
		// System.out.println("Node " + node);

		if (parent.acceptsChild()) {
			parent.insert(node, index);
			nodeAdded(node);
			creationPerformed = true;
			nodeCreationInProgress = false;
		} else {
			// Selected node cannot take more children
			// Display useful warning
			JOptionPane.showMessageDialog(null,
					"Cannot move control construct to the selected location.",
					"Error moving control construct!",
					JOptionPane.ERROR_MESSAGE);
			vetoCurrentDnd = true;
			creationPerformed = false;
			nodeCreationInProgress = false;
			throw new VetoException(parent + " cannot take more child nodes.");
		}
	}

	/* This method only gets the OldIndex and OldParent in the event */
	public void nodeDeletion(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		if (nodeCreationInProgress) {
			// System.out.print("."); // spinlock, maybe not too clean
		}

		// System.out.println("Vetoable node deletion");
		OWLSTreeNode node = (OWLSTreeNode) e.getNode();
		// System.out.println("Old index: " + e.getOldIndex() + ", Old parent: "
		// + e.getOldParent());
		// System.out.println("Node " + node);

		if (vetoCurrentDnd) {
			creationPerformed = false;
			vetoCurrentDnd = false;
			throw new VetoException(
					"nodeDeletion() vetoed because node creation was"
							+ " vetoed.");
		} else if (!creationPerformed) {
			JOptionPane.showMessageDialog(null,
					"Nodes cannot be dragged into their own children.",
					"Error moving control construct!",
					JOptionPane.ERROR_MESSAGE);
			throw new VetoException(
					"nodeDeletion() vetoed because nodes cannot be dragged "
							+ " into their own children.");
		} else {
			creationPerformed = false;
			nodeMoved(node);
		}
	}

	/*
	 * This is never called. A drag-and-drop node move constists of a
	 * nodeCreation followed by a node deletion.
	 */
	public void nodeMove(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		// System.out.println("Vetoable node move");
		throw new VetoException("This operation is not supported!");
	}

	public void nodeRename(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		// System.out.println("Vetoable node rename");
		throw new VetoException("This operation is not supported!");
	}

}
