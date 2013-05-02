package org.pvv.bcd.instrument.JTree;

import java.awt.datatransfer.Transferable;

import javax.swing.tree.TreeNode;

import org.pvv.bcd.Util.FeatureNotSupportedException;

/**
 * <p>
 * Objects implementing <code>NodeFactory</code> are useful when you want to
 * build instrumented trees with specialized tree nodes. While you
 * will be best served to use <code>DefaultMutableTreeNode</code> type nodes,
 * the <code>NodeFactory</code> gives you complete freedom in what kind of
 * user objects to put into those nodes. The Instrument/JTree classes use
 * node factories throughout when building new nodes.
 * </p>
 *
 * <p>
 * If a <code>NodeFactory</code> implements {@link XmlNodeFactory},
 * then it can build nodes from imported XML.
 * </p>
 *
 * <p>
 * If a <code>NodeFactory</code> implements {@link UserObjectFactory},
 * then the user will be able to rename the nodes if he so wishes.
 * </p>
 *
 * @see NodeInfo
 * @see UserObjectFactory
 * @see XmlNodeFactory
 * @see DefaultNodeFactory
 */
public interface NodeFactory
{
   /**
    * Creates a new node that is identical to another. Should create
    * a deep copy of the original, unless there are specific reasons to
    * want it otherwise.
    *
    * @param node Node to make a copy of.
    *
    * @return Reference to new copied node. Should <em>not</em> return
    * the same object as given in the <code>node</code> parameter.
    */
   TreeNode cloneNode(TreeNode node);

   /**
    * Creates a default empty node.
    *
    * @param parent Reference to the parent that the new node will be put
    * under. This parameter may be null. Implementations are quite free to
    * ignore this parameter if they do not need it.
    *
    * @return Reference to created node.
    */
   TreeNode createNode(TreeNode parent);

   /**
    * Creates a new node with a given user object.
    *
    * @return Reference to created node.
    */
   TreeNode createNode(Object ob);

   /**
    * Creates a new node with a given title and contents.
    *
    * @return Reference to created node.
    */
   TreeNode createNode(String title, Object contents);

   /**
    * Will create and return a suitable <code>Transferable</code> carrying
    * data for one node.
    *
    * @param node Node to create a <code>Transferable</code> for.
    * @param Unique DND id associated with this transfer.
    *
    * @return A <code>Transferable</code> suitable for transferring
    * the given node data.
    *
    * @throws FeatureNotSupportedException if data transfer is not supported
    * by this node factory.
    *
    * @see #createTransferable(Object[], DndId)
    * @see DefaultTransferable
    */
   Transferable createTransferable(Object node, DndId dndId)
      throws FeatureNotSupportedException;

   /**
    * Will create and return a suitable <code>Transferable</code> carrying
    * data for multiple nodes.
    *
    * @param nodes Nodes to create a <code>Transferable</code> for.
    * @param Unique DND id associated with this transfer.
    *
    * @return A <code>Transferable</code> suitable for transferring the
    * given node data.
    *
    * @throws FeatureNotSupportedException if data transfer is not supported
    * by this node factory.
    *
    * @see #createTransferable(Object, DndId)
    * @see DefaultTransferable
    */
   Transferable createTransferable(Object[] nodes, DndId dndId)
      throws FeatureNotSupportedException;
}
