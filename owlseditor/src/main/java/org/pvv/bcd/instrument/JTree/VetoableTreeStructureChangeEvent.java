package org.pvv.bcd.instrument.JTree;

import java.util.EventObject;

import javax.swing.tree.TreeNode;

/**
 * <p>
 * The event object for a tree structure change. Listeners to this event type
 * have the power to veto the change, in which case it will not happen.
 * </p>
 * 
 * <p>
 * In an unfortunate turn of events (some would call it a design glitch, others
 * will fess up and call it laziness), this event class is used for two distinct
 * types of tree structure changes. For each of these two, some of our
 * properties will make sense and others should be ignored. This is how it
 * works;
 * <ol>
 * <li>
 * When a node is renamed, an event is filled in with its <code>oldName</code>,
 * <code>newName</code>, <code>source</code> and <code>node</code> properties
 * set. All others should be ignored as they will generally by <code>null</code>
 * or zero or somesuch.</li>
 * <li>
 * When a node is moved, an event is filled in with its <code>oldParent</code>,
 * <code>oldIndex</code>, <code>newParent</code>, <code>newIndex</code>,
 * <code>source</code> and <code>node</code> properties set. All others should
 * be ignored as they will generally by <code>null</code> or zero or somesuch.</li>
 * </ol>
 * </p>
 * 
 * @todo Split this into two events, one "Node Moved" event and one
 *       "Node Renamed" event.
 * 
 * @todo We need a set of events that are <em>not</em> vetoable and that get
 *       fired after the vetoable events if the change actually took place. As
 *       it is now, early receivers of these events have no way of knowing
 *       whether or not later receivers will veto them and so don't really know
 *       whether it is happening or not.
 */
public class VetoableTreeStructureChangeEvent extends EventObject {
	private String m_newname;
	private String m_oldname;

	private TreeNode m_node;
	private TreeNode m_oldParent;
	private int m_nOldIndex;
	private TreeNode m_newParent;
	private int m_nNewIndex;

	/**
	 * Create an unspecified event.
	 */
	public VetoableTreeStructureChangeEvent(Object source) {
		super(source);
	}

	/**
	 * Create a "change name" event
	 * 
	 * @param source
	 *            Event source
	 * @param node
	 *            Node that changed
	 * @param oldname
	 *            Old name for node
	 * @param newname
	 *            New name for node
	 */
	public VetoableTreeStructureChangeEvent(Object source, TreeNode node,
			String oldname, String newname) {
		super(source);
		m_node = node;
		m_newname = newname;
		m_oldname = oldname;
	}

	/**
	 * Create a "node moved" event
	 * 
	 * @param source
	 *            Event source
	 * @param node
	 *            Node that got moved
	 * @param oldparent
	 *            Node we were moved from
	 * @param oldindex
	 *            Index we used to have in our old parent
	 * @param new parent Node we are being moved to
	 * @param newindex
	 *            Index we will be moved to in our new parent
	 */
	public VetoableTreeStructureChangeEvent(Object source, TreeNode node,
			TreeNode oldparent, int oldindex, TreeNode newparent, int newindex) {
		super(source);
		m_node = node;
		m_oldParent = oldparent;
		m_nOldIndex = oldindex;
		m_newParent = newparent;
		m_nNewIndex = newindex;
	}

	public String getOldName() {
		return m_oldname;
	}

	public String getNewName() {
		return m_newname;
	}

	public TreeNode getNode() {
		return m_node;
	}

	public TreeNode getOldParent() {
		return m_oldParent;
	}

	public int getOldIndex() {
		return m_nOldIndex;
	}

	public TreeNode getNewParent() {
		return m_newParent;
	}

	public int getNewIndex() {
		return m_nNewIndex;
	}
}
