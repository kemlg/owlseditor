package org.pvv.bcd.instrument.JTree;

import java.awt.datatransfer.Transferable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.pvv.bcd.Util.FeatureNotSupportedException;

/**
 * The default node factory will create <code>DefaultMutableTreeNode</code> type
 * nodes with DefaultNodeInfo type user objects.
 * 
 * @see DefaultNodeInfo
 * @see javax.swing.tree.DefaultMutableTreeNode
 */
public class DefaultNodeFactory implements NodeFactory, UserObjectFactory {

	/**
	 * Default constructor, doesn't do anything.
	 */
	public DefaultNodeFactory() {
	}

	/**
	 * Will clone a node. This is <em>not<em> accomplished by calling
	 * <code>clone()</code>
	 * on the node. Rather, we use our own logic for achieving this. If
	 * the node to be cloned is of type
	 * <code>DefaultMutableTreeNode</code>, the new
	 * node will copy the user object also. If the user object is
	 * <code>Copyable</code>,
	 * a new object is created by calling its <code>copy()</code> method.
	 * If it is not,
	 * the new node will have the exact same user object as the original.
	 * Calls <code>createNode()</code> to achieve this.
	 * 
	 * @param node
	 *            <code>Node</code> to clone
	 * 
	 * @return Newly created node
	 * 
	 * @see #createNode(Object)
	 */
	public TreeNode cloneNode(TreeNode node) {
		if (node instanceof DefaultMutableTreeNode) {
			Object uo = ((DefaultMutableTreeNode) node).getUserObject();
			if (uo instanceof Copyable) {
				return createNode(((Copyable) uo).copy());
			} else {
				return createNode(uo);
			}
		} else {
			return createNode(null);
		}
	}

	/**
	 * Will create a default empty node. In the default implementaiton, this is
	 * a node with a default <code>DefaultNodeInfo</code> object as its user
	 * object.
	 * 
	 * @param parent
	 *            Parent of the new node to create, not used in the default
	 *            implementation
	 * 
	 * @return Newly created node. The default implementation will return a
	 *         <code>DefaultMutableTreeNode</code>
	 * 
	 * @see #createNode(Object)
	 * @see #createNode(String, String)
	 */
	public TreeNode createNode(TreeNode parent) {
		// This needs to be changed to produce OWLSTreeNodeInfo objects
		System.out.println("DefaultNodeFactory.createNode(TreeNode)."
				+ " This should not be used. Use OWLSTreeNodeFactory instead.");
		return createNode(new DefaultNodeInfo(""));
	}

	/**
	 * Will create a new node with a given user object.
	 * 
	 * @param ob
	 *            User object to insert into node
	 * 
	 * @return Newly created node. The default implementation will return a
	 *         <code>DefaultMutableTreeNode</code>
	 * 
	 * @see #createNode(String, String)
	 * @see #createNode(TreeNode)
	 */
	public TreeNode createNode(Object ob) {
		// needs to be changed to produce OWLSTreeNode objects
		System.out.println("DefaultNodeFactory.createNode(Object)."
				+ " This should not be used. Use OWLSTreeNodeFactory instead.");
		DefaultMutableTreeNode retval = new DefaultMutableTreeNode();
		retval.setUserObject(ob);
		return retval;
	}

	/**
	 * Will create a new node based on XML data. Uses
	 * 
	 * @link{#createNode(Object) to accomplish this.
	 * 
	 * @param title
	 *            Title of new node
	 * @param contents
	 *            Textual contents of new node
	 * 
	 * @return Newly created node. The default implementation will return a
	 *         <code>DefaultMutableTreeNode</code>
	 * 
	 * @see #createNode(Object)
	 * @see #createNode(TreeNode)
	 */
	public TreeNode createNode(String title, Object contents) {
		System.out.println("DefaultNodeFactory.createNode(String, Object)."
				+ " This should not be used. Use OWLSTreeNodeFactory instead.");
		return createNode(new DefaultNodeInfo(title, contents));
	}

	/**
	 * Sets the displayable title of a node. Typically called when the user
	 * changes the node title by use of a CellEditor or similar.
	 * 
	 * @param userobject
	 *            Node to set title on. The default implementation expects this
	 *            to be a <code>DefaultNodeInfo</code> type object.
	 * @param title
	 *            New title to set on the node.
	 */
	public void setUserObjectTitle(Object userobject, String title) {
		DefaultNodeInfo dni = (DefaultNodeInfo) userobject;
		dni.setTitle(title);
	}

	/**
	 * Will create and return a suitable <code>Transferable</code> carrying data
	 * for one node.
	 * 
	 * @param node
	 *            Node to create a <code>Transferable</code> for. The default
	 *            implementation expects this to be a
	 *            <code>DefaultMutableTreeNode</code>.
	 * @param Unique
	 *            DND id associated with this transfer.
	 * 
	 * @return A <code>Transferable</code> suitable for transferring the given
	 *         node data. The default implementation will return a
	 *         <code>DefaultTransferable</code>.
	 * 
	 * @throws FeatureNotSupportedException
	 *             if data transfer is not supported by this node factory.
	 * 
	 * @see #createTransferable(Object[], DndId)
	 * @see DefaultTransferable
	 */
	public Transferable createTransferable(Object node, DndId dndId)
			throws FeatureNotSupportedException {
		return new DefaultTransferable((DefaultMutableTreeNode) node, dndId);
	}

	/**
	 * Will create and return a suitable <code>Transferable</code> carrying data
	 * for multiple nodes.
	 * 
	 * @param nodes
	 *            Nodes to create a <code>Transferable</code> for. The default
	 *            implementation expects these to be
	 *            <code>DefaultMutableTreeNode</code>.
	 * @param Unique
	 *            DND id associated with this transfer.
	 * 
	 * @return A <code>Transferable</code> suitable for transferring the given
	 *         node data. The default implementation will return a
	 *         <code>DefaultTransferable</code>.
	 * 
	 * @throws FeatureNotSupportedException
	 *             if data transfer is not supported by this node factory.
	 * 
	 * @see #createTransferable(Object, DndId)
	 * @see DefaultTransferable
	 */
	public Transferable createTransferable(Object[] nodes, DndId dndId)
			throws FeatureNotSupportedException {
		return new DefaultTransferable((DefaultMutableTreeNode[]) nodes, dndId);
	}
}
