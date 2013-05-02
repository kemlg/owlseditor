package org.pvv.bcd.instrument.JTree;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragGestureListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

/**
 * <p>
 * This class provides instrumentation of JTree objects. What this means is that
 * by associating a JTree with an instance of the Instrumenter class, you can
 * get features such as drag-and-drop, import/export XML, tree structure
 * rearranging and editing of node names pretty much for free. While some of
 * these features require you to do some adaption coding (particularly if you're
 * not 100% happy with the default behaviour), such adaption is designed to be
 * reasonably easy and we also provide means of disabling features that you do
 * not desire.
 * </p>
 * 
 * <p>
 * Typical use of this class is as follows:
 * 
 * <pre>
 * JTree tree = new JTree(someModel);
 * Instrumenter instrument = Instrumenter.createInstrument(tree);
 * </pre>
 * 
 * You now proceed to put your tree into your GUI and will immediately reap the
 * benefit of the instrumenter's many features. If you intend to interact with
 * the instrument in the future, you will want to keep a reference to it. If
 * not, you do not need to do so.
 * </p>
 * 
 * <p>
 * The instrumenter works primarily by attaching listeners to the input tree,
 * but will also do some tweaking of the tree. Refer to the documentation for
 * the various <code>setUseDefaultXxx()</code> methods for details on which
 * features are available, what they do to your tree, and how to disable them.
 * </p>
 * 
 * <p>
 * To get the maximum usability with the least amount of fuss, it is recommended
 * that you use tree nodes of type DefaultMutableTreeNode. The instrumenter
 * expects this type for a variety of its features. It should be evident in the
 * API documentation what features that require this. If you find any
 * discrepancies, please let us know.
 * </p>
 * 
 * <p>
 * The instrument can be further customised by calling the various public
 * methods in it. Refer to the API documentation for details.
 * </p>
 * 
 * <p>
 * Instances of Instrumenter can only be obtained by calling one of its factory
 * methods;
 * 
 * @link{#createInstrument(JTree) ,
 * @link{#createInstrument(JTree), boolean)},
 * @link{#createInstrument(JTree), boolean, boolean)}
 *                                 </p>
 */
public class Instrumenter {
	private JTree m_tree;
	private SubTreeNode[] m_arrCopiedNodes = null;
	private UndoManager m_undoManager;
	private UndoableEditSupport m_undoSupport = new UndoableEditSupport(this);
	private NodeFactory m_nodeFactory;

	/* Listeners etc */
	TreeDropTargetListener dl;
	DropTarget dt;
	ChangeListener cl;
	DragGestureRecognizer dgr;
	TreeDragGestureListener td;
	private Vector ceListeners = new Vector();
	private Vector undoListeners = new Vector();
	private PropertyChangeListener prop;
	private PropertyChangeListener prop2;
	private javax.swing.event.TreeSelectionListener tl;

	/**
	 * Use createInstrument() to obtain instances of this class.
	 * 
	 * @see #createInstrument(JTree)
	 * @see #createInstrument(JTree,boolean)
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	private Instrumenter() {
		createDefaultActionMap();
		createDefaultInputMap();
	}

	public void removeListeners() {
		m_tree.removePropertyChangeListener(prop);
		m_tree.removePropertyChangeListener(prop2);
		m_tree.removeTreeSelectionListener(tl);

		for (Enumeration e = undoListeners.elements(); e.hasMoreElements();) {
			removeUndoableEditListener((UndoableEditListener) e.nextElement());
		}

		for (Enumeration e = ceListeners.elements(); e.hasMoreElements();) {
			((DefaultTreeCellEditor) m_tree.getCellEditor())
					.removeCellEditorListener((CellEditorListener) e
							.nextElement());
		}

		((InfoUndoManager) m_undoManager).removeChangeListener(cl);

		dgr.removeDragGestureListener((DragGestureListener) td);

		dt.removeDropTargetListener(dl);
	}

	/**
	 * Factory method for instrumenting a tree and returning the instrument.
	 * 
	 * @param tree
	 *            Tree to instrument. It must already have been constructed
	 *            (i.e., null pointer not allowed).
	 * @param full_instrumentation
	 *            If true, will install all instrumentation features. If false,
	 *            will install only those essential for the correct operation of
	 *            the instrumentation. If set to false, you may want to install
	 *            a subset of the full instrumentation. This is accomplished by
	 *            calling one or more of the setUseXxxx methods with "true" as
	 *            parameter.
	 * @param clear
	 *            If true, will clear the contents of the tree and create a
	 *            default root node of type DefaultMutableTreeNode. If false,
	 *            will keep any old contents.
	 * 
	 * @see #createInstrument(JTree,boolean)
	 * @see #createInstrument(JTree)
	 * @see #setUseDefaultKeyboardShortcuts
	 * @see #setUseDefaultActionMap
	 * @see #setUseDefaultUndoManager
	 * @see #setUseDefaultCellEditor
	 * @see #setUseDefaultNodeFactory
	 * @see #setUseDefaultTreeConfiguration
	 */
	public static Instrumenter createInstrument(JTree tree,
			boolean full_instrumentation, boolean clear) {
		if (clear)
			tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		Instrumenter retval = new Instrumenter();
		retval.instrument(tree, full_instrumentation);
		if (clear && retval.getNodeFactory() != null)
			((DefaultTreeModel) tree.getModel()).setRoot(retval
					.getNodeFactory().createNode(null));
		return retval;
	}

	/**
	 * Convenience factory method instrumenting a tree while keeping the tree's
	 * contents.
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	public static Instrumenter createInstrument(JTree tree,
			boolean full_instrumentation) {
		return createInstrument(tree, full_instrumentation, false);
	}

	/**
	 * Convenience factory method to fully instrument a tree with all available
	 * functionality, keeping its contents.
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 * @see #createInstrument(JTree,boolean
	 */
	public static Instrumenter createInstrument(JTree tree) {
		return createInstrument(tree, true);
	}

	private void instrument(JTree tree, boolean full_instrumentation) {
		m_tree = tree;

		setUseDefaultTreeConfiguration(full_instrumentation);
		setUseDefaultUndoManager(full_instrumentation);
		prop = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(JTree.TREE_MODEL_PROPERTY)) {
					if (m_undoManager != null) {
						m_undoManager.discardAllEdits();
					}
					m_arrCopiedNodes = null;
					updateActionObjects();
				}
			}
		};

		m_tree.addPropertyChangeListener(prop);

		setUseDefaultKeyboardShortcuts(full_instrumentation);
		setUseDefaultActionMap(full_instrumentation);
		setUseDefaultDnd(full_instrumentation);

		tl = new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				infoTree_valueChanged(e);
			}
		};
		m_tree.addTreeSelectionListener(tl);
		setUseDefaultCellEditor(full_instrumentation);

		configureCellEditor();

		prop2 = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(JTree.CELL_EDITOR_PROPERTY)) {
					configureCellEditor();
				}
			}
		};
		m_tree.addPropertyChangeListener(prop2);
		setUseDefaultNodeFactory(full_instrumentation);

		// Make sure our actions start out in the correct state
		updateActionObjects();
	}

	/**
	 * <p>
	 * This method tweaks a cell editor so that we can use it. Specifically, we
	 * add a CellEditorListener to it so we can handle focus when editing stops.
	 * </p>
	 * 
	 * <p>
	 * NOTE that when the cell editor changes, we make no attempt to remove the
	 * listener from the old cell editor. This is a potential source of memory
	 * leaks.
	 * </p>
	 * 
	 * @todo Remove our listener on the previous cell editor when the editor
	 *       changes
	 */
	protected void configureCellEditor() {
		CellEditorListener ce = new CellEditorListener() {
			public void editingStopped(ChangeEvent e) {
				getFocusForTree();
			}

			public void editingCanceled(ChangeEvent e) {
				getFocusForTree();
			}
		};

		((DefaultTreeCellEditor) m_tree.getCellEditor())
				.addCellEditorListener(ce);
		ceListeners.add(ce);
	}

	/**
	 * <p>
	 * Will configure the tree to use the default Drag And Drop implementation.
	 * This will work for many applications, and when it does not is is
	 * generally easy to make it work. If Drag And Drop is not wanted, however,
	 * it may make sense to disable it.
	 * </p>
	 * This method is called automatically if you choose full instrumentation in
	 * the call to createInstrument.
	 * 
	 * @param b
	 *            If true, uses our default tree config. If false, does nothing.
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	public void setUseDefaultDnd(boolean b) {
		if (b) {
			td = new TreeDragGestureListener(m_tree, this);

			dgr = DragSource.getDefaultDragSource()
					.createDefaultDragGestureRecognizer(m_tree,
							DnDConstants.ACTION_COPY_OR_MOVE, td);

			dl = new TreeDropTargetListener(m_tree, this);
			dt = new DropTarget(m_tree, dl);
			m_tree.setCellRenderer(new NodeInfoTreeCellRenderer());
		}
	}

	/**
	 * <p>
	 * Will configure the tree for optimal instrumented operation. If you do not
	 * call this method (it is called automatically if you choose full
	 * instrumentation in the call to createInstrument), take note of the
	 * following;
	 * <ul>
	 * <li>The cell editor may not behave as you wish unless you call
	 * setInvokesStopCellEditing(true) on your tree</li>
	 * <li>The tree editing functions will be disabled unless you call
	 * setEditable(true) on your tree</li>
	 * <li>Scrolling of the tree may not function as you wish unless you call
	 * setAutoscrolls(true) on your tree</li>
	 * </ul>
	 * </p>
	 * 
	 * @param b
	 *            If true, uses our default tree config. If false, does nothing.
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	public void setUseDefaultTreeConfiguration(boolean b) {
		if (b) {
			m_tree.setRootVisible(false);
			m_tree.setInvokesStopCellEditing(true);
			m_tree.setEditable(true);
			m_tree.setShowsRootHandles(true);
			m_tree.setAutoscrolls(true);
		}
	}

	/**
	 * Configures whether or not to use the default node factory. The default
	 * node factory has no particular expectations of the type of nodes or their
	 * contents but creates DefaultMutableTreeNode objects with DefaultNodeInfo
	 * contents when constructing new nodes.
	 * 
	 * @param b
	 *            If true, will use our default node factory. If false, does
	 *            nothing.
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	public void setUseDefaultNodeFactory(boolean b) {
		if (b)
			m_nodeFactory = new DefaultNodeFactory();
	}

	/**
	 * Configures whether or not to use the default Infobank undo manager. The
	 * default undo manager has not particular expectations of the nodes or
	 * their contents.
	 * 
	 * @param b
	 *            If true, will call setUndoManager() with a new
	 *            InfoUndoManager. If false, does nothing.
	 * 
	 * @see #setUndoManager
	 * @see InfoUndoManager
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	public void setUseDefaultUndoManager(boolean b) {
		if (b) {
			setUndoManager(new InfoUndoManager());
		}
	}

	/**
	 * Configures whether to use our default keyboard shortcuts. Some of the
	 * default shortcuts (i.e., Ctrl-Up, Ctrl-Down, Space) conflict with and
	 * override default Swing JTree shortcuts. The keyboard shortcuts will not
	 * work properly unless you also install the default action map (or the
	 * subset of it that is of interest to you).
	 * 
	 * @param b
	 *            If true, will install our keyboard shortcuts. If false,
	 *            removes them (only really useful if you've installed them
	 *            previously and don't want them anymore).
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 * @see #setUseDefaultActionMap
	 */
	public void setUseDefaultKeyboardShortcuts(boolean b) {
		InputMap map = m_tree.getInputMap();
		Enumeration enum1 = m_hashDefaultInputMap.keys();
		while (enum1.hasMoreElements()) {
			Object key = enum1.nextElement();
			if (b)
				map.put((KeyStroke) key, m_hashDefaultInputMap.get(key));
			else
				map.remove((KeyStroke) key);
		}
	}

	/**
	 * Configures whether to use our default actions. While these should not
	 * conflict with existing ones, you don't really need to install them if you
	 * don't intend to use them (i.e., if you haven't installed the default
	 * keyboard map). The keyboard shortcuts will not work properly unless you
	 * also install the default action map (or the subset of it that is of
	 * interest to you).
	 * 
	 * @param b
	 *            If true, will install our actions. If false, removes them
	 *            (only really useful if you've installed them previously and
	 *            don't want them anymore).
	 * 
	 * @see #createInstrument(JTree,boolean,boolean)
	 * @see #setUseDefaultKeyboardShortcuts
	 */
	public void setUseDefaultActionMap(boolean b) {
		ActionMap map = m_tree.getActionMap();
		Enumeration enum1 = m_hashDefaultActionMap.keys();
		while (enum1.hasMoreElements()) {
			Object key = enum1.nextElement();
			if (b)
				map.put(key, (Action) m_hashDefaultActionMap.get(key));
			else
				map.remove(key);
		}
	}

	/**
	 * Configures whether to use our default treecell editor. The default editor
	 * expects the nodes to be DefaultMutableTreeNode type objects and their
	 * userObject to be DefaultNodeInfo type objects. If this does not hold,
	 * expect ClassCastExceptions.
	 * 
	 * @param b
	 *            If true, will install the default editor. If false, does
	 *            nothing.
	 * 
	 * @see NodeInfoTreeCellEditor
	 * @see #createInstrument(JTree,boolean,boolean)
	 */
	private void setUseDefaultCellEditor(boolean b) {
		if (b)
			m_tree.setCellEditor(new NodeInfoTreeCellEditor(m_tree,
					(javax.swing.tree.DefaultTreeCellRenderer) m_tree
							.getCellRenderer(), this));
	}

	/**
	 * Sets the object to use for creating new nodes in the tree. Extend
	 * NodeFactory and implement your own methods if you need specialized
	 * behaviour.
	 * 
	 * @see NodeFactory
	 */
	public void setNodeFactory(NodeFactory factory) {
		m_nodeFactory = factory;
	}

	/**
	 * Retrieve the currently active node factory.
	 * 
	 * @see #setNodeFactory
	 */
	public NodeFactory getNodeFactory() {
		return m_nodeFactory;
	}

	/**
	 * Adds listeners for undoableedit events.
	 * 
	 * @see javax.swing.undo.UndoableEdit
	 */
	public void addUndoableEditListener(UndoableEditListener listener) {
		m_undoSupport.addUndoableEditListener(listener);
		undoListeners.add(listener);
	}

	/**
	 * Removes an UndoableEdit listener.
	 * 
	 * @see javax.swing.undo.UndoableEdit
	 */
	public void removeUndoableEditListener(UndoableEditListener listener) {
		m_undoSupport.removeUndoableEditListener(listener);
		undoListeners.remove(listener);
	}

	/**
	 * Returns a reference to the currently active undo manager, if I know about
	 * it. If I do not know about it, returns null.
	 */
	public UndoManager getUndoManager() {
		return m_undoManager;
	}

	/**
	 * Registers a new undo manager with me. I take care of getting rid of any
	 * old one (removing it as listener) and making the new one a listener. I
	 * will also subscribe to its changeEvents if it is a "InfoUndoManager" or
	 * subclass. If so, I will correctly update my undo and redo actions. If I
	 * do not know about the current undo manager (i.e., this attribute has been
	 * set to null) the actions will always be disabled. If I think I know about
	 * it but do not (i.e., I have wrong information) the state of these actions
	 * may be wrong.
	 * 
	 * @see #getUndoAction
	 * @see #getRedoAction
	 */
	public void setUndoManager(UndoManager manager) {
		m_undoSupport.removeUndoableEditListener(m_undoManager);
		m_undoManager = manager;
		if (m_undoManager != null) {
			m_undoSupport.addUndoableEditListener(m_undoManager);
			if (m_undoManager instanceof InfoUndoManager) {
				cl = new ChangeListener() {
					public void stateChanged(ChangeEvent ev) {
						updateActionObjects();
					}
				};

				((InfoUndoManager) m_undoManager).addChangeListener(cl);
			}
		}
		updateActionObjects();
	}

	/**
	 * Empties the undo buffer. Only works if I know about the current undo
	 * manager.
	 * 
	 * @see #setUndoManager
	 */
	public void purgeUndoBuffer() {
		if (m_undoManager != null)
			m_undoManager.discardAllEdits();
	}

	/**
	 * Attempts to obtain focus for "my" tree. This is not guaranteed to
	 * succeed.
	 * 
	 * @see javax.swing.JTree#requestFocus()
	 */
	private void getFocusForTree() {
		m_tree.requestFocus();
	}

	/** Moves selected nodes one position towards the top */
	public void moveNodesUp() {
		moveNodes(true);
	};

	/** Moves selected nodes one position towards the bottom */
	public void moveNodesDown() {
		moveNodes(false);
	};

	/** Adds a new node above the selection, or at top if no selection */
	public void addNodeAbove() throws VetoException {
		m_undoSupport.postEdit(addNode(true));
	}

	/** Adds a new node below the selection, or at bottom if no selection */
	public void addNodeBelow() throws VetoException {
		m_undoSupport.postEdit(addNode(false));
	}

	/** Adds a new node at the end of the selected node's children */
	public void addChildNode() throws VetoException {
		m_undoSupport.postEdit(addChildNode(false, null));
	}

	/**
	 * Adds nodes to the current selection in the tree.
	 * 
	 * @param nodes
	 *            List of nodes to add to the selection. The nodes need to be
	 *            DefaultMutableTreeNode or descendants.
	 */
	private void setSelection(Vector nodes) {
		if (nodes.size() == 0)
			return;
		Enumeration en = nodes.elements();
		while (en.hasMoreElements())
			m_tree.addSelectionPath(new TreePath(((DefaultTreeModel) m_tree
					.getModel()).getPathToRoot((DefaultMutableTreeNode) en
					.nextElement())));
		m_tree.scrollPathToVisible(new TreePath(((DefaultTreeModel) m_tree
				.getModel()).getPathToRoot((DefaultMutableTreeNode) nodes
				.firstElement())));
	}

	/**
	 * Returns an array of all selected nodes, sorted by their row number.
	 * 
	 * @param ascending
	 *            If true, nodes in low-numbered rows will be sorted first, then
	 *            nodes with increasing row numbers. If false, sort order will
	 *            be reverse of this.
	 * 
	 * @return Array of sorted selected nodes.
	 */
	TreePath[] getSortedSelection(boolean ascending) {
		TreePath[] retval = m_tree.getSelectionPaths();
		if (retval == null)
			return new TreePath[0];
		final boolean asc = ascending;
		Arrays.sort(retval, new Comparator() {
			public int compare(Object a, Object b) {
				return (m_tree.getRowForPath((TreePath) a) - m_tree
						.getRowForPath((TreePath) b)) * (asc ? 1 : -1);
			}
		});
		return retval;
	}

	/**
	 * Starts the node editor at the current lead selection path
	 */
	private void startEdit() {
		m_tree.startEditingAtPath(m_tree.getLeadSelectionPath());
	}

	/**
	 * Moves a node in the tree. Makes sure that an undo object is created and
	 * stored.
	 * 
	 * @param node_to_move
	 *            Reference to the node to move
	 * @param dest_node
	 *            New parent for the node to move
	 * @param dest_index
	 *            Index within dest_node that node_to_move should end up in
	 * 
	 * @throws VetoException
	 *             The move was vetoed by one of the veto listeners
	 */
	public void moveNode(DefaultMutableTreeNode node_to_move,
			DefaultMutableTreeNode dest_node, int dest_index)
			throws VetoException {
		m_undoSupport.postEdit(doMoveNode(node_to_move, dest_node, dest_index));
	}

	/**
	 * Moves all selected nodes one step. Makes sure undo objects are created
	 * and posted. Expects nodes to be of type DefaultMutableTreeNode.
	 * 
	 * @param If
	 *            true, moves nodes upwards (towards row 0), if false moves
	 *            downwards.
	 */
	private void moveNodes(boolean up) {
		boolean veto = false;
		boolean added = false;
		Vector nodeV = new Vector(m_tree.getSelectionCount());
		TreePath[] selrange = getSortedSelection(up);

		m_undoSupport.beginUpdate();
		try {
			for (int i = 0; i < selrange.length; ++i) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selrange[i]
						.getLastPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
						.getParent();
				if (parent != null) {
					int ndx = parent.getIndex(node);
					if ((up && (ndx > 0))
							|| (!up && (ndx < parent.getChildCount() - 1))) {
						m_undoSupport.postEdit(doMoveNode(node, parent, ndx
								+ (up ? -1 : 1)));
						added = true;
					} else {
						return;
					}
				}
				nodeV.addElement(node);
			}
		} catch (VetoException ex) {
			veto = true;
		} finally {
			m_undoSupport.endUpdate();
		}

		if (veto && added) {
			m_undoManager.undo();
		} else {
			setSelection(nodeV);
		}
	}

	/**
	 * Moves all selected nodes one position to the "left" on the tree. This has
	 * the effect of making the node the parent of what used to be the siblings
	 * below it in the tree. If the node is at root level, nothing happens to
	 * it. Expects nodes to be of type DefaultMutableTreeNode.
	 */
	public void makeAncestor() {
		boolean veto = false;
		boolean added = false;
		Vector nodeV = new Vector(m_tree.getSelectionCount());
		// Get list of all selected nodes sorted by row#
		TreePath lastsel[] = getSortedSelection(true);

		// Cycle through all selected nodes
		m_undoSupport.beginUpdate();
		try {
			for (int i = 0; i < lastsel.length; ++i) {
				DefaultMutableTreeNode sel = (DefaultMutableTreeNode) lastsel[i]
						.getLastPathComponent();
				nodeV.addElement(sel);
				DefaultMutableTreeNode oldparent = (DefaultMutableTreeNode) sel
						.getParent();
				if (oldparent == null)
					continue;
				DefaultMutableTreeNode newparent = (DefaultMutableTreeNode) oldparent
						.getParent();
				if (newparent == null)
					continue;
				// Get list of all children that are to be moved to new ancestor
				Vector newchildren = new Vector();
				DefaultMutableTreeNode thisChild = (DefaultMutableTreeNode) oldparent
						.getChildAfter(sel);
				DefaultMutableTreeNode nextChild;
				// Cycle through children and move them
				if (thisChild != null)
					do {
						nextChild = (DefaultMutableTreeNode) oldparent
								.getChildAfter(thisChild);
						m_undoSupport.postEdit(doMoveNode(thisChild, sel,
								sel.getChildCount()));
						added = true;
					} while ((thisChild = nextChild) != null);
				// Then move the new ancestor
				m_undoSupport.postEdit(doMoveNode(sel, newparent,
						newparent.getIndex(oldparent) + 1));
				added = true;
				m_tree.makeVisible(new TreePath(((DefaultTreeModel) m_tree
						.getModel()).getPathToRoot(sel)));
				if (sel.getChildCount() > 0)
					m_tree.makeVisible(new TreePath(((DefaultTreeModel) m_tree
							.getModel()).getPathToRoot(sel.getFirstChild())));
			}
		} catch (VetoException ex) {
			veto = true;
		} finally {
			m_undoSupport.endUpdate();
		}

		if (veto && added) {
			m_undoManager.undo();
		} else {
			setSelection(nodeV);
		}
	}

	/**
	 * Moves all selected nodes one position to the "right" in the tree. This
	 * has the effect of making a node the child of what used to be the sibling
	 * just above it in the tree. If the node is topmost among its siblings,
	 * nothing happens to it. Expects nodes to be of type
	 * DefaultMutableTreeNode.
	 */
	public void makeChild() {
		boolean veto = false;
		boolean added = false;
		Vector nodeV = new Vector(m_tree.getSelectionCount());
		TreePath lastsel[] = m_tree.getSelectionPaths();
		if (lastsel == null || lastsel.length == 0)
			return;

		m_undoSupport.beginUpdate();
		try {
			for (int i = 0; i < lastsel.length; ++i) {
				DefaultMutableTreeNode sel = (DefaultMutableTreeNode) lastsel[i]
						.getLastPathComponent();
				nodeV.addElement(sel);
				DefaultMutableTreeNode oldparent = (DefaultMutableTreeNode) sel
						.getParent();
				if (oldparent == null)
					continue;
				DefaultMutableTreeNode newparent = (DefaultMutableTreeNode) oldparent
						.getChildBefore(sel);
				if (newparent == null)
					continue;
				m_undoSupport.postEdit(doMoveNode(sel, newparent,
						newparent.getChildCount()));
				added = true;
				m_tree.makeVisible(new TreePath(((DefaultTreeModel) m_tree
						.getModel()).getPathToRoot(sel)));
			}
		} catch (VetoException ex) {
			veto = true;
		} finally {
			m_undoSupport.endUpdate();
		}

		if (veto && added) {
			m_undoManager.undo();
		} else {
			setSelection(nodeV);
		}
	}

	/**
	 * Adds a set of nodes to the tree. Makes sure to update undo information.
	 * 
	 * @param parent
	 *            Node to place new nodes within
	 * @param nodes
	 *            Array of nodes to add
	 * @param parent_index
	 *            Index in parent into which the first node in the node array
	 *            will be placed. Each following node in the array will be
	 *            placed at the next index.
	 */
	public void addNodes(DefaultMutableTreeNode parent,
			DefaultMutableTreeNode[] nodes, int parent_index) {
		boolean veto = false;
		boolean added = false;
		m_undoSupport.beginUpdate();

		try {
			for (int i = nodes.length - 1; i >= 0; i--) {
				m_undoSupport
						.postEdit(doAddNode(parent, nodes[i], parent_index));
				added = true;
			}
		} catch (VetoException ex) {
			veto = true;
		} finally {
			m_undoSupport.endUpdate();
		}

		if (veto && added) {
			m_undoManager.undo();
		}
	}

	/**
	 * Adds a new child node to the selected node.
	 * 
	 * @param above
	 *            If "true", will add to the top of the node's child array. If
	 *            false, will add to the bottom of it.
	 * 
	 * @throws VetoException
	 *             If the operation was vetoed by a
	 *             VetoableTreeStructureChangeListener. In this case, the
	 *             operation did not happen and any changes that were
	 *             temporarily in effect got rolled back.
	 */
	private UndoableEdit addChildNode(boolean top, Object newnodeinfo)
			throws VetoException {
		int placetoput = 0;
		DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) m_tree
				.getLeadSelectionPath().getLastPathComponent();

		if (parentnode == null)
			return null;

		if (!top) {
			placetoput = parentnode.getChildCount();
		}
		UndoableEdit retval;

		boolean initiate_edit = false;
		DefaultMutableTreeNode newnode;
		if (newnodeinfo == null) {
			initiate_edit = true;
			newnode = (DefaultMutableTreeNode) m_nodeFactory
					.createNode(parentnode);
		} else {
			if (newnodeinfo instanceof SubTreeNode) {
				newnode = ((SubTreeNode) newnodeinfo).createNode();
			} else
				newnode = (DefaultMutableTreeNode) m_nodeFactory
						.createNode(newnodeinfo);
		}
		retval = doAddNode(parentnode, newnode, placetoput);
		if (initiate_edit) {
			TreePath newpath = new TreePath(
					((DefaultTreeModel) m_tree.getModel())
							.getPathToRoot(newnode));
			m_tree.setSelectionPath(newpath);
			m_tree.scrollPathToVisible(newpath);
			m_tree.startEditingAtPath(newpath);
		}
		return retval;
	}

	/**
	 * Convenience method for new node with empty node info.
	 * 
	 * @see #addNode(boolean,Object)
	 * 
	 * @throws VetoException
	 *             If the operation was vetoed by a
	 *             VetoableTreeStructureChangeListener. In this case, the
	 *             operation did not happen and any changes that were
	 *             temporarily in effect got rolled back.
	 */
	private UndoableEdit addNode(boolean above) throws VetoException {
		return addNode(above, null);
	}

	/**
	 * Adds a new node into the tree. Expects the tree to consist of
	 * DefaultMutableTreeNodes. Uses the node factory to create new nodes.
	 * 
	 * @param above
	 *            true to add above selection (or at top if no selection), false
	 *            to add below selection (or at bottom if no selection).
	 * @param newnodeinfo
	 *            Object to store as user object in new node, or null for no
	 *            contents.
	 * 
	 * @return the UndoableEdit corresponding to the addition. This must be
	 *         registered with the undo system for correct operation.
	 * 
	 * @see #setNodeFactory
	 * @see #setUseDefaultNodeFactory
	 * 
	 * @throws VetoException
	 *             If the operation was vetoed by a
	 *             VetoableTreeStructureChangeListener. In this case, the
	 *             operation did not happen and any changes that were
	 *             temporarily in effect got rolled back.
	 */
	private UndoableEdit addNode(boolean above, Object newnodeinfo)
			throws VetoException {
		int placetoput = 0;
		DefaultMutableTreeNode parentnode = null;
		int numsel = m_tree.getSelectionCount();
		UndoableEdit retval;

		if (numsel > 0) {
			// Place above or below selection, as appropriate
			int selrows[] = m_tree.getSelectionRows();
			int minsel = java.lang.Integer.MAX_VALUE;
			int maxsel = java.lang.Integer.MIN_VALUE;
			for (int i = 0; i < selrows.length; ++i) {
				if (selrows[i] > maxsel)
					maxsel = selrows[i];
				if (selrows[i] < minsel)
					minsel = selrows[i];
			}
			DefaultMutableTreeNode actsel;
			actsel = (DefaultMutableTreeNode) m_tree.getPathForRow(
					above ? minsel : maxsel).getLastPathComponent();
			parentnode = (DefaultMutableTreeNode) actsel.getParent();
			if (parentnode != null) {
				placetoput = parentnode.getIndex(actsel) + (above ? 0 : 1);
			} else {
				placetoput = 0;
			}
		} else if (((DefaultMutableTreeNode) m_tree.getModel().getRoot())
				.getChildCount() == 0) {
			// No nodes in tree whatsoever - add one attached to the root.
			parentnode = (DefaultMutableTreeNode) m_tree.getModel().getRoot();
			placetoput = 0;
		} else {
			// No selection, but there are nodes in the tree. Add Above adds to
			// the top of the tree, Add Below adds to the end of the tree.
			parentnode = (DefaultMutableTreeNode) m_tree.getModel().getRoot();
			if (above)
				placetoput = 0;
			else
				placetoput = parentnode.getChildCount();
		}

		if (parentnode == null)
			return null;

		boolean initiate_edit = false;
		DefaultMutableTreeNode newnode;
		if (newnodeinfo == null) {
			initiate_edit = true;
			newnode = (DefaultMutableTreeNode) m_nodeFactory
					.createNode(parentnode);
		} else {
			if (newnodeinfo instanceof SubTreeNode) {
				newnode = ((SubTreeNode) newnodeinfo).createNode();
			} else
				newnode = (DefaultMutableTreeNode) m_nodeFactory
						.createNode(newnodeinfo);
		}
		retval = doAddNode(parentnode, newnode, placetoput);
		if (initiate_edit) {
			TreePath newpath = new TreePath(
					((DefaultTreeModel) m_tree.getModel())
							.getPathToRoot(newnode));
			m_tree.setSelectionPath(newpath);
			m_tree.scrollPathToVisible(newpath);
			m_tree.startEditingAtPath(newpath);
		}
		return retval;
	}

	/**
	 * Utility method to find one of a node's ancestors within a hash of nodes.
	 * 
	 * @param node
	 *            Node the ancestors of which to search for.
	 * @param hash
	 *            Hash to look for ancestors in. Will search the hash's keys for
	 *            ancestors, not the contents.
	 * 
	 * @return true if one of the node's ancestors was found in the hash, false
	 *         if not.
	 */
	static private boolean isNodeParentInHash(DefaultMutableTreeNode node,
			Hashtable hash) {
		DefaultMutableTreeNode iter = (DefaultMutableTreeNode) node.getParent();
		boolean parent_in_selection = false;
		while (iter != null) {
			if (hash.get(iter) != null) {
				parent_in_selection = true;
				break;
			}
			iter = (DefaultMutableTreeNode) iter.getParent();
		}
		return parent_in_selection;
	}

	/**
	 * Deletes currently selected nodes with a "delete" semantic (as opposed to
	 * a "backspace" semantic).
	 * 
	 * @see #deleteSelection(boolean)
	 */
	public void deleteSelectedNodes() {
		deleteSelection(true);
	}

	/**
	 * Reduces a set of tree nodes to the minimum set that includes them all.
	 * That is, if node n2 is a child of node n1 and both are included in the
	 * input parameter, then node n2 will not be part of the return value. n2 is
	 * implicitly included by the fact that n1 is included with all its
	 * children. This is a utility method.
	 * 
	 * @param original
	 *            Set of tree nodes to reduce.
	 * 
	 * @return Minimum set of tree nodes that includes all nodes in the original
	 *         when a node's children are assumed to be included implicitly if
	 *         the node is included.
	 */
	public static TreePath[] reduceSelection(TreePath[] original) {
		if (original == null)
			return new TreePath[0];
		Vector retvect = new Vector(original.length / 2);
		Hashtable hash = new Hashtable(original.length);
		for (int i = 0; i < original.length; ++i)
			hash.put(original[i].getLastPathComponent(), Boolean.TRUE);

		for (int i = 0; i < original.length; ++i) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) original[i]
					.getLastPathComponent();

			if (isNodeParentInHash(node, hash))
				continue;
			retvect.addElement(original[i]);
		}

		TreePath[] retarr;
		if ((retvect.size() == 1)
				&& (((TreeNode) ((TreePath) retvect.firstElement())
						.getLastPathComponent()).getParent()) == null) {
			// Can't delete root node, so insert all its children in stead
			TreeNode root = (TreeNode) ((TreePath) retvect.firstElement())
					.getLastPathComponent();

			retarr = new TreePath[root.getChildCount()];
			Enumeration enum1 = root.children();
			int i = 0;
			while (enum1.hasMoreElements()) {
				TreePath path = new TreePath(new TreeNode[] { root,
						(TreeNode) enum1.nextElement() });
				retarr[i++] = path;
			}
		} else {
			retarr = new TreePath[retvect.size()];
			retvect.copyInto(retarr);
		}
		return retarr;
	}

	/**
	 * Deletes currently selected nodes with either a "delete" semantic or a
	 * "backspace" semantic. Nodes are deleted unconditionally. Expects the
	 * selected nodes to be of type DefaultMutableTreeNode.
	 * 
	 * @param remain
	 *            If true, will delete with "delete" semantic. This means that
	 *            after deletion, the selected node will be at the same location
	 *            as the lead selection was at before deletion. If false, will
	 *            delete with a "backspace" semantic. That is, after the
	 *            deletion, the selected node is at the location immediately
	 *            prior to what used to be the location of the previous lead
	 *            selection.
	 */
	void deleteSelection(boolean remain) {
		boolean veto = false;
		boolean added = false;
		TreePath lastsel[] = m_tree.getSelectionPaths();
		lastsel = reduceSelection(lastsel);

		if (lastsel == null)
			return;
		int lead = m_tree.getLeadSelectionRow();
		if (remain)
			++lead;
		m_undoSupport.beginUpdate();
		try {
			for (int i = 0; i < lastsel.length; ++i) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastsel[i]
						.getLastPathComponent();

				if (lead >= m_tree.getRowForPath(lastsel[i])) {
					--lead;
				}
				if (node.getParent() != null) {
					m_undoSupport.postEdit(doDeleteNode(node));
					added = true;
				}
			}
		} catch (VetoException ex) {
			veto = true;
		} finally {
			m_undoSupport.endUpdate();
		}

		if (veto && added) {
			m_undoManager.undo();
		}

		if (lead >= m_tree.getRowCount())
			lead = m_tree.getRowCount() - 1;
		if (lead < 0)
			lead = 0;
		m_tree.setSelectionRow(lead);
	}

	/**
	 * Moves a node and creates an appropriate undo-action for it. Expects the
	 * tree model to be of type DefaultTreeModel.
	 * 
	 * @param node_to_move
	 *            Reference to the node to be moved. This node should already be
	 *            in the tree. This can <em>NOT</em> be the root node - this
	 *            will cause NullPointerException to be thrown.
	 * @param dest_node
	 *            Reference to the new parent of the moving node. The parent
	 *            should be somewhere in the tree.
	 * @param dest_index
	 *            The index in the dest_node into which the node_to_move should
	 *            end up. Already existing nodes at and after this index all get
	 *            moved back one position.
	 * @param undovec
	 *            Vector into which to add the appropriate Undo-action for this
	 *            operation. If null, then adds the Undo-action to the tree's
	 *            undo vector. If not null, you <em>must</em> take care of
	 *            adding this vector to the tree's undo vector yourself. If you
	 *            do not, undo/redo is going to behave very strangely.
	 * 
	 * @return the UndoableEdit corresponding to the addition. This must be
	 *         registered with the undo system for correct operation.
	 */
	private UndoableEdit doMoveNode(DefaultMutableTreeNode node_to_move,
			DefaultMutableTreeNode dest_node, int dest_index)
			throws VetoException {
		fireNodeMove(new VetoableTreeStructureChangeEvent(this, node_to_move,
				node_to_move.getParent(), node_to_move.getParent().getIndex(
						node_to_move), dest_node, dest_index));

		UndoableEdit unedit = new UndoableInfoEdit(
				(DefaultTreeModel) m_tree.getModel(),
				UndoableInfoEdit.TYPE_MOVE,
				(DefaultMutableTreeNode) node_to_move.getParent(), node_to_move
						.getParent().getIndex(node_to_move), dest_node,
				dest_index, node_to_move, false);

		((DefaultTreeModel) m_tree.getModel())
				.removeNodeFromParent(node_to_move);
		((DefaultTreeModel) m_tree.getModel()).insertNodeInto(node_to_move,
				dest_node, dest_index);

		return unedit;
	}

	/**
	 * Adds a node to the tree and creates an appropriate undo-action for it.
	 * Expects the tree model to be of type DefaultTreeModel.
	 * 
	 * @param parent
	 *            Reference to the parent of the new node. The parent should be
	 *            somewhere in the tree.
	 * @param node
	 *            Node to add to the tree. It should <em>not</em> be in the tree
	 *            already.
	 * @param index
	 *            The index in the parent into which the node should end up.
	 *            Already existing nodes at and after this index all get moved
	 *            back one position.
	 * @param undovec
	 *            Vector into which to add the appropriate Undo-action for this
	 *            operation. If null, then adds the Undo-action to the tree's
	 *            undo vector. If not null, you <em>must</em> take care of
	 *            adding this vector to the tree's undo vector yourself. If you
	 *            do not, undo/redo is going to behave very strangely.
	 * 
	 * @return the UndoableEdit corresponding to the addition. This must be
	 *         registered with the undo system for correct operation.
	 */
	private UndoableEdit doAddNode(DefaultMutableTreeNode parent,
			DefaultMutableTreeNode node, int index) throws VetoException {
		fireNodeCreation(new VetoableTreeStructureChangeEvent(this, node, null,
				0, parent, index));

		UndoableEdit unedit = new UndoableInfoEdit(
				(DefaultTreeModel) m_tree.getModel(),
				UndoableInfoEdit.TYPE_ADD, null, 0, parent, index, node, true);
		((DefaultTreeModel) m_tree.getModel()).insertNodeInto(node, parent,
				index);

		return unedit;
	}

	/**
	 * Deletes a node from the tree and creates an appropriate undo-action for
	 * it.
	 * 
	 * @param node
	 *            Node to delete from the tree. This can <em>NOT</em> be the
	 *            root node - this will cause NullPointerException to be thrown.
	 * @param undovec
	 *            Vector into which to add the appropriate Undo-action for this
	 *            operation. If null, then adds the Undo-action to the tree's
	 *            undo vector. If not null, you <em>must</em> take care of
	 *            adding this vector to the tree's undo vector yourself. If you
	 *            do not, undo/redo is going to behave very strangely.
	 * 
	 * @return the UndoableEdit corresponding to the addition. This must be
	 *         registered with the undo system for correct operation.
	 */
	private UndoableEdit doDeleteNode(DefaultMutableTreeNode node)
			throws VetoException {
		fireNodeDeletion(new VetoableTreeStructureChangeEvent(this, node,
				node.getParent(), node.getParent().getIndex(node), null, 0));

		UndoableEdit unedit = new UndoableInfoEdit(
				(DefaultTreeModel) m_tree.getModel(),
				UndoableInfoEdit.TYPE_DELETE,
				(DefaultMutableTreeNode) node.getParent(), node.getParent()
						.getIndex(node), null, 0, node, true);
		((DefaultTreeModel) m_tree.getModel()).removeNodeFromParent(node);

		return unedit;
	}

	/**
	 * Will update Action objects based upon a selection change in the tree
	 * 
	 * @param e
	 *            Event to respond to
	 * 
	 * @see #updateActionObjects()
	 */
	void infoTree_valueChanged(TreeSelectionEvent e) {
		updateActionObjects();
	}

	/**
	 * Will ensure that the Action objects we have created are in sync with the
	 * selection state of the tree. This method should be called whenever there
	 * is reason to believe that the selection in the tree has changed.
	 */
	protected void updateActionObjects() {
		boolean enable = m_tree.getSelectionCount() != 0;
		for (int i = 0; i < m_arrSelectionDependentActions.length; ++i) {
			if (enable != m_arrSelectionDependentActions[i].isEnabled())
				m_arrSelectionDependentActions[i].setEnabled(enable);
		}
		m_actUndo.setEnabled(m_undoManager != null
				&& m_undoManager instanceof InfoUndoManager
				&& m_undoManager.canUndo());
		m_actRedo.setEnabled(m_undoManager != null
				&& m_undoManager instanceof InfoUndoManager
				&& m_undoManager.canRedo());
		m_actPaste.setEnabled(m_arrCopiedNodes != null);
	}

	// ----------------------------------------------------------------------
	// Drag and Drop Functionality follows
	// ----------------------------------------------------------------------

	/**
	 * Utility method that hands out unique IDs to use for identifying drag and
	 * drop operations. If you do not intend to do some low-level DND coding,
	 * you do not need this method.
	 * 
	 * @return A new DND ID that is guaranteed to be unique for this instance of
	 *         Instrumenter. Note that the unique id is implemented as a long,
	 *         so the potential for a wrap-around and following non-uniqueness
	 *         of the ids is present.
	 */
	public synchronized DndId getNextDndId() {
		return new DndId(m_nMyInstrumentId, m_nDndOperationId++);
	}

	/**
	 * Helper class to keep track of Drag And Drop operations
	 */
	class DndOperations {
		public Vector m_ops;
		public boolean m_bSourceDone;
		public boolean m_bTargetDone;

		public DndOperations() {
			m_ops = new Vector();
		}
	}

	/**
	 * Hash to keep track of Drag And Drop operations
	 */
	private Hashtable m_hashDndOperations = new Hashtable();

	/**
	 * Finishes a Drag And Drop operation. If you do not intend to do some
	 * low-level DND coding, you do not need this method.
	 * 
	 * @param op_id
	 *            Operation ID of DND operation to finish
	 * @param source
	 *            Set to true if the source is done handling the DND operation,
	 *            false if it is not done.
	 */
	public void finishDndOperation(long op_id, boolean source) {
		Long op_id_ob = new Long(op_id);
		synchronized (m_hashDndOperations) {
			DndOperations op = (DndOperations) m_hashDndOperations
					.get(op_id_ob);
			if (op != null) {
				if (source)
					op.m_bSourceDone = true;
				else
					op.m_bTargetDone = true;

				// When the source is done, all local target operations are
				// already done.
				if (op.m_bSourceDone) {
					m_undoSupport.beginUpdate();
					for (int i = 0; i < op.m_ops.size(); ++i)
						m_undoSupport.postEdit((UndoableEdit) op.m_ops
								.elementAt(i));
					m_undoSupport.endUpdate();
					m_hashDndOperations.remove(op_id_ob);
				}
			}
		}
	}

	/**
	 * The purpose and workings of this method are shrouded in the mists of
	 * time. It is, however, vital to make drag and drop behave correctly. It
	 * will be investigated and documented as time permits.
	 */
	public void doDndCompoundAdd(long op_id, DefaultMutableTreeNode parent,
			DefaultMutableTreeNode node, int index) throws VetoException {
		// System.out.println("doDndCompoundAdd(): Adding " + node +
		// " to parent " + parent);
		UndoableEdit unedit = doAddNode(parent, node, index);
		Long op_id_ob = new Long(op_id);
		synchronized (m_hashDndOperations) {
			DndOperations dndop = (DndOperations) m_hashDndOperations
					.get(op_id_ob);
			if (dndop == null) {
				dndop = new DndOperations();
				m_hashDndOperations.put(op_id_ob, dndop);
			}
			dndop.m_ops.addElement(unedit);
		}
	}

	/**
	 * The purpose and workings of this method are shrouded in the mists of
	 * time. It is, however, vital to make drag and drop behave correctly. It
	 * will be investigated and documented as time permits.
	 */
	public void doDndCompoundDelete(long op_id, DefaultMutableTreeNode node)
			throws VetoException {
		UndoableEdit unedit;
		if (node.getParent() != null) {
			unedit = doDeleteNode(node);
			Long op_id_ob = new Long(op_id);
			synchronized (m_hashDndOperations) {
				DndOperations dndop = (DndOperations) m_hashDndOperations
						.get(op_id_ob);
				if (dndop == null) {
					dndop = new DndOperations();
					m_hashDndOperations.put(op_id_ob, dndop);
				}
				dndop.m_ops.addElement(unedit);
			}
		}
	}

	// ----------------------------------------------------------------------
	// Copy/Paste Functionality follows
	// ----------------------------------------------------------------------

	/**
	 * Deletes the selected nodes and puts them into the local clipboard. Does
	 * <em>not</em> put them into the system clipboard. Expects nodes to be of
	 * type DefaultMutableTreeNode.
	 * 
	 * @see #m_arrCopiedNodes
	 */
	public void cutNode() {
		copyNode();
		deleteSelectedNodes();
	}

	/**
	 * Puts the selected nodes into the local clipboard. Does <em>not</em> put
	 * them into the system clipboard. Expects nodes to be of type
	 * DefaultMutableTreeNode.
	 * 
	 * @see #m_arrCopiedNodes
	 */
	public void copyNode() {
		TreePath[] paths = getSortedSelection(true);
		if (paths == null || paths.length == 0)
			return;
		paths = reduceSelection(paths);

		Vector copiedNodes = new Vector(paths.length);
		for (int i = 0; i < paths.length; ++i) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i]
					.getLastPathComponent();
			copiedNodes.addElement(new SubTreeNode(
					(DefaultMutableTreeNode) paths[i].getLastPathComponent(),
					true));
		}
		m_arrCopiedNodes = new SubTreeNode[copiedNodes.size()];
		copiedNodes.copyInto(m_arrCopiedNodes);
		updateActionObjects();
	}

	/**
	 * Makes copies of the nodes in the local clipboard (<em>not</em> the system
	 * clipboard) in the currently focused location. Expects nodes to be of type
	 * DefaultMutableTreeNode.
	 * 
	 * @see #m_arrCopiedNodes
	 */
	public void pasteNode() {
		pasteNode(m_arrCopiedNodes);
	}

	/**
	 * Copies nodes into the currently focused location. Expects nodes to be of
	 * type DefaultMutableTreeNode.
	 * 
	 * @param nodes
	 *            Nodes to be inserted.
	 * 
	 * @see #pasteNode()
	 */
	public void pasteNode(SubTreeNode[] nodes) {
		boolean veto = false;
		boolean added = false;

		if (nodes == null || nodes.length == 0)
			return;
		m_undoSupport.beginUpdate();

		try {
			for (int i = 0; i < nodes.length; ++i) {
				m_undoSupport.postEdit(addNode(true, nodes[i]));
				added = true;
			}
		} catch (VetoException ex) {
			veto = true;
		} finally {
			m_undoSupport.endUpdate();
		}

		if (veto && added) {
			m_undoManager.undo();
		}

		m_tree.scrollPathToVisible(m_tree.getLeadSelectionPath());
	}

	// ----------------------------------------------------------------------
	// XML conversions follow
	// ----------------------------------------------------------------------

	/**
	 * <p>
	 * Reads text from the given inputstream, parses it according to
	 * infobank.dtd and creates a new tree based upon the information. The
	 * existing tree is deleted unconditionally.
	 * </p>
	 * <p>
	 * <b>Note</b> for this to work, you need to make the JDOM package available
	 * in your distribution. Consult http://www.jdom.org for latest releases
	 * etc.
	 * </p>
	 * 
	 * @param is
	 *            Inputstream to read XML from
	 * 
	 * @return Error message, or null if no error
	 */
	/*
	 * public String createFromXml(InputStream is) { try { SAXBuilder builder =
	 * new SAXBuilder(true); org.jdom.Document doc = builder.build(is);
	 * org.jdom.Element root = doc.getRootElement(); TreeNode new_root_node =
	 * makeNodeFromXMLRecursive(root, true); DefaultTreeModel model =
	 * (DefaultTreeModel)m_tree.getModel(); model.setRoot(new_root_node);
	 * purgeUndoBuffer(); } catch (NoClassDefFoundError ex) { return
	 * "Could not find JDOM library."; } catch (org.jdom.JDOMException ex2) {
	 * return ex2.getMessage(); }
	 * 
	 * return null; }
	 */

	/*
	 * protected MutableTreeNode makeNodeFromXMLRecursive( org.jdom.Element
	 * xml_elem, boolean is_root) { DefaultMutableTreeNode new_node; Vector
	 * children = new Vector(); String title="", contents="";
	 * 
	 * java.util.List childlist = xml_elem.getChildren(); int num_children =
	 * childlist.size(); for (int i=0; i<num_children; ++i) { org.jdom.Element
	 * elem = (org.jdom.Element)childlist.get(i); if (is_root) title = null;
	 * else { if (elem.getName().equals("name")) title = elem.getText(); else if
	 * (elem.getName().equals("contents")) contents = elem.getText(); } if
	 * (elem.getName().equals("node"))
	 * children.addElement(makeNodeFromXMLRecursive(elem, false)); }
	 * 
	 * if (title==null) new_node = (DefaultMutableTreeNode)
	 * m_nodeFactory.createNode(null); else new_node = (DefaultMutableTreeNode)
	 * ((XmlNodeFactory)m_nodeFactory).createNode(title, contents);
	 * 
	 * for (int i=0; i<children.size(); ++i)
	 * new_node.add((MutableTreeNode)children.elementAt(i));
	 * 
	 * return new_node; }
	 */
	/**
	 * <p>
	 * Builds an XML representation of the tree, as defined by infobank.dtd.
	 * Sends the textual data to the outputstream given.
	 * </p>
	 * <p>
	 * <b>Note</b> for this to work, you need to make the JDOM package available
	 * in your distribution. Consult http://www.jdom.org for latest releases
	 * etc.
	 * </p>
	 * 
	 * @param os
	 *            Outputstrem to send XML to
	 */
	/*
	 * public void outputXml(OutputStream os) throws IOException { XMLOutputter
	 * outputter = new XMLOutputter("   ", true, "ISO-8859-1");
	 * outputter.setLineSeparator("\n"); org.jdom.DocType type = new
	 * org.jdom.DocType("tree", "infobank.dtd"); org.jdom.Element root =
	 * makeXMLNodeRecursive(m_tree.getModel().getRoot(), true);
	 * org.jdom.Document doc = new org.jdom.Document(root, type);
	 * outputter.output(doc, os); }
	 * 
	 * protected org.jdom.Element makeXMLNodeRecursive(Object node, boolean
	 * is_root) { org.jdom.Element xml_node = new
	 * org.jdom.Element(is_root?"tree":"node"); if (node==null) { return
	 * xml_node; }
	 * 
	 * DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)node; if
	 * (!is_root) { NodeInfo info = (NodeInfo)tnode.getUserObject();
	 * 
	 * if (((NodeInfo)info).getTitle().length()>0) { org.jdom.Element name = new
	 * org.jdom.Element("name"); name.addContent(info.getTitle());
	 * xml_node.addContent(name); }
	 * 
	 * if (info.getContents().length()>0) { org.jdom.Element contents = new
	 * org.jdom.Element("contents"); contents.addContent(info.getContents());
	 * xml_node.addContent(contents); } }
	 * 
	 * for (int i=0; i<tnode.getChildCount(); ++i)
	 * xml_node.addContent(makeXMLNodeRecursive(tnode.getChildAt(i), false));
	 * 
	 * return xml_node; }
	 */
	// ----------------------------------------------------------------------
	// Action objects and handling follows
	// ----------------------------------------------------------------------

	/**
	 * @return Action object suitable for triggering an "add node above"
	 */
	public Action getAddAboveAction() {
		return m_actAddAbove;
	}

	/**
	 * @return Action object suitable for triggering an "add node below"
	 */
	public Action getAddBelowAction() {
		return m_actAddBelow;
	}

	/**
	 * @return Action object suitable for triggering an "add child node"
	 */
	public Action getAddChildAction() {
		return m_actAddChild;
	}

	/**
	 * @return Action object suitable for triggering a "delete node"
	 */
	public Action getDeleteAction() {
		return m_actDelete;
	}

	/**
	 * @return Action object suitable for triggering a "move node left"
	 */
	public Action getMakeAncestorAction() {
		return m_actMakeAncestor;
	}

	/**
	 * @return Action object suitable for triggering a "move node right"
	 */
	public Action getMakeChildAction() {
		return m_actMakeChild;
	}

	/**
	 * @return Action object suitable for triggering a "move node down"
	 */
	public Action getMoveDownAction() {
		return m_actMoveDown;
	}

	/**
	 * @return Action object suitable for triggering a "move node up"
	 */
	public Action getMoveUpAction() {
		return m_actMoveUp;
	}

	/**
	 * @return Action object suitable for triggering an "undo"
	 */
	public Action getUndoAction() {
		return m_actUndo;
	}

	/**
	 * @return Action object suitable for triggering a "redo last undo"
	 */
	public Action getRedoAction() {
		return m_actRedo;
	}

	/**
	 * @return Action object suitable for triggering a "copy selection"
	 */
	public Action getCopyAction() {
		return m_actCopy;
	}

	/**
	 * @return Action object suitable for triggering a "cut selection"
	 */
	public Action getCutAction() {
		return m_actCut;
	}

	/**
	 * @return Action object suitable for triggering a "paste"
	 */
	public Action getPasteAction() {
		return m_actPaste;
	}

	/**
	 * @return Action object suitable for triggering an "change node name"
	 */
	public Action getStartEditAction() {
		return m_actStartEdit;
	}

	protected AbstractAction m_actAddAbove = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			try {
				addNodeAbove();
			} catch (VetoException ex) {
			}
		}

		public boolean isEnabled() {
			return true;
		}

		public void setEnabled(boolean b) {
		}
	};
	protected AbstractAction m_actAddBelow = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			try {
				addNodeBelow();
			} catch (VetoException ex) {
			}
		}

		public boolean isEnabled() {
			return true;
		}

		public void setEnabled(boolean b) {
		}
	};
	protected AbstractAction m_actAddChild = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			try {
				addChildNode();
			} catch (VetoException ex) {
			}
		}
	};
	protected AbstractAction m_actMakeAncestor = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			makeAncestor();
		}
	};
	protected AbstractAction m_actMakeChild = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			makeChild();
		}
	};
	protected AbstractAction m_actUndo = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			if (m_undoManager != null && m_undoManager.canUndo())
				m_undoManager.undo();
		}
	};
	protected AbstractAction m_actRedo = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			if (m_undoManager != null && m_undoManager.canRedo())
				m_undoManager.redo();
		}
	};
	protected AbstractAction m_actDelete = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			deleteSelectedNodes();
		}
	};
	protected AbstractAction m_actMoveUp = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			moveNodesUp();
		}
	};
	protected AbstractAction m_actMoveDown = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			moveNodesDown();
		}
	};
	protected AbstractAction m_actCopy = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			copyNode();
		}
	};
	protected AbstractAction m_actCut = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			cutNode();
		}
	};
	protected AbstractAction m_actPaste = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			pasteNode();
		}
	};
	protected AbstractAction m_actStartEdit = new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
			startEdit();
		}
	};

	/**
	 * An array of all actions that are enabled only when 1+ nodes are selected
	 */
	protected Action[] m_arrSelectionDependentActions = new Action[] {
			m_actDelete, m_actMoveUp, m_actMoveDown, m_actMakeAncestor,
			m_actMakeChild, m_actCopy, m_actCut, m_actStartEdit, m_actAddChild };

	/** Action name object for "Move Up" action */
	public static final String ACTION_MOVE_UP = "Move Nodes Up";
	/** Action name object for "Move Down" action */
	public static final String ACTION_MOVE_DOWN = "Move Nodes Down";
	/** Action name object for "Move Left" action */
	public static final String ACTION_MAKE_ANCESTOR = "Make Nodes Ancestors";
	/** Action name object for "Move Right" action */
	public static final String ACTION_MAKE_CHILD = "Make Nodes Children";
	/** Action name object for "Copy Selection" action */
	public static final String ACTION_COPY = "Copy Nodes";
	/** Action name object for "Cut Selection" action */
	public static final String ACTION_CUT = "Cut Nodes";
	/** Action name object for "Paste" action */
	public static final String ACTION_PASTE = "Paste Nodes";
	/** Action name object for "Undo Edit" action */
	public static final String ACTION_UNDO = "Undo Edit";
	/** Action name object for "Redo Undo" action */
	public static final String ACTION_REDO = "Redo Undo";
	/** Action name object for "Add Node Above" action */
	public static final String ACTION_ADD_ABOVE = "Add Node Above";
	/** Action name object for "Add Node Below" action */
	public static final String ACTION_ADD_BELOW = "Add Node Below";
	/** Action name object for "Add Child Node" action */
	public static final String ACTION_ADD_CHILD = "Add Child Node";
	/** Action name object for "Delete Selection" action */
	public static final String ACTION_DELETE = "Delete Nodes";
	/** Action name object for "Edit Node Name" action */
	public static final String ACTION_START_EDIT = "Start Node Edit";

	public final Hashtable m_hashDefaultActionMap = new Hashtable();
	public final Hashtable m_hashDefaultInputMap = new Hashtable();

	private void createDefaultActionMap() {
		m_hashDefaultActionMap.put(ACTION_MOVE_UP, m_actMoveUp);
		m_hashDefaultActionMap.put(ACTION_MOVE_DOWN, m_actMoveDown);
		m_hashDefaultActionMap.put(ACTION_MAKE_CHILD, m_actMakeChild);
		m_hashDefaultActionMap.put(ACTION_MAKE_ANCESTOR, m_actMakeAncestor);
		m_hashDefaultActionMap.put(ACTION_ADD_ABOVE, m_actAddAbove);
		m_hashDefaultActionMap.put(ACTION_ADD_BELOW, m_actAddBelow);
		m_hashDefaultActionMap.put(ACTION_ADD_CHILD, m_actAddChild);
		m_hashDefaultActionMap.put(ACTION_COPY, m_actCopy);
		m_hashDefaultActionMap.put(ACTION_CUT, m_actCut);
		m_hashDefaultActionMap.put(ACTION_PASTE, m_actPaste);
		m_hashDefaultActionMap.put(ACTION_UNDO, m_actUndo);
		m_hashDefaultActionMap.put(ACTION_REDO, m_actRedo);
		m_hashDefaultActionMap.put(ACTION_DELETE, m_actDelete);
		m_hashDefaultActionMap.put(ACTION_START_EDIT, m_actStartEdit);
	}

	// ----------------------------------------------------------------------
	// Keyboard handling follows
	// ----------------------------------------------------------------------

	private void createDefaultInputMap() {
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_MASK),
				ACTION_MOVE_UP);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_MASK),
				ACTION_MOVE_DOWN);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK),
				ACTION_MAKE_ANCESTOR);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK),
				ACTION_MAKE_CHILD);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_MASK),
				ACTION_ADD_BELOW);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK),
				ACTION_COPY);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK),
				ACTION_PASTE);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK),
				ACTION_CUT);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK),
				ACTION_UNDO);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK),
				ACTION_REDO);

		m_hashDefaultInputMap
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,
						KeyEvent.SHIFT_MASK), ACTION_ADD_ABOVE);

		m_hashDefaultInputMap
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
						ACTION_ADD_CHILD);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_DELETE);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true),
				ACTION_START_EDIT);
		m_hashDefaultInputMap.put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "none");
	}

	private static long m_nNextInstrumentId = 0;
	private static Object m_obNextInstrumentIdSemaphore = new Object();
	private final long m_nMyInstrumentId = getNextInstrumentId();
	private long m_nDndOperationId = 0;
	private transient Vector vetoableTreeStructureChangeListeners;

	/**
	 * Generates a unique ID. The main purpose is to generate unique instrument
	 * IDs for each instantiated instrument for use with Drag And Drop
	 * operations.
	 * 
	 * @return A new unique instrument ID (unless we have wrapped the long)
	 * 
	 * @see #getMyInstrumentId()
	 */
	public static long getNextInstrumentId() {
		synchronized (m_obNextInstrumentIdSemaphore) {
			return m_nNextInstrumentId++;
		}
	}

	/**
	 * Retrieves my unique instrument ID
	 * 
	 * @return Unique instrument ID (unless we have wrapped the long)
	 * 
	 * @see #getNextInstrumentId()
	 */
	public long getMyInstrumentId() {
		return m_nMyInstrumentId;
	}

	// ----------------------------------------------------------------------
	// Events follow
	// ----------------------------------------------------------------------

	/**
	 * Removes a tree structure change listener.
	 * 
	 * @param l
	 *            Listener to remove
	 */
	public synchronized void removeVetoableTreeStructureChangeListener(
			VetoableTreeStructureChangeListener l) {
		if (vetoableTreeStructureChangeListeners != null
				&& vetoableTreeStructureChangeListeners.contains(l)) {
			Vector v = (Vector) vetoableTreeStructureChangeListeners.clone();
			v.removeElement(l);
			vetoableTreeStructureChangeListeners = v;
		}
	}

	/**
	 * Adds a listener to changes in the tree structure. The listener can veto
	 * any changes.
	 * 
	 * @param l
	 *            Listener to add
	 */
	public synchronized void addVetoableTreeStructureChangeListener(
			VetoableTreeStructureChangeListener l) {
		Vector v = vetoableTreeStructureChangeListeners == null ? new Vector(2)
				: (Vector) vetoableTreeStructureChangeListeners.clone();
		if (!v.contains(l)) {
			v.addElement(l);
			vetoableTreeStructureChangeListeners = v;
		}
	}

	/**
	 * Fires a Node Creation event
	 * 
	 * @param Event
	 *            to send
	 * 
	 * @throws VetoException
	 *             if one of the listeners didn't like the change. In this case,
	 *             the change should not take place.
	 */
	protected void fireNodeCreation(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		if (vetoableTreeStructureChangeListeners != null) {
			Vector listeners = vetoableTreeStructureChangeListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++) {
				((VetoableTreeStructureChangeListener) listeners.elementAt(i))
						.nodeCreation(e);
			}
		}
	}

	/**
	 * Fires a Node Deletion event
	 * 
	 * @param Event
	 *            to send
	 * 
	 * @throws VetoException
	 *             if one of the listeners didn't like the change. In this case,
	 *             the change should not take place.
	 */
	protected void fireNodeDeletion(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		if (vetoableTreeStructureChangeListeners != null) {
			Vector listeners = vetoableTreeStructureChangeListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++) {
				((VetoableTreeStructureChangeListener) listeners.elementAt(i))
						.nodeDeletion(e);
			}
		}
	}

	/**
	 * Fires a Node Move event
	 * 
	 * @param Event
	 *            to send
	 * 
	 * @throws VetoException
	 *             if one of the listeners didn't like the change. In this case,
	 *             the change should not take place.
	 */
	protected void fireNodeMove(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		if (vetoableTreeStructureChangeListeners != null) {
			Vector listeners = vetoableTreeStructureChangeListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++) {
				((VetoableTreeStructureChangeListener) listeners.elementAt(i))
						.nodeMove(e);
			}
		}
	}

	/**
	 * Fires a Node Rename event
	 * 
	 * @param Event
	 *            to send
	 * 
	 * @throws VetoException
	 *             if one of the listeners didn't like the change. In this case,
	 *             the change should not take place.
	 */
	protected void fireNodeRename(VetoableTreeStructureChangeEvent e)
			throws VetoException {
		if (vetoableTreeStructureChangeListeners != null) {
			Vector listeners = vetoableTreeStructureChangeListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++) {
				((VetoableTreeStructureChangeListener) listeners.elementAt(i))
						.nodeRename(e);
			}
		}
	}
}
