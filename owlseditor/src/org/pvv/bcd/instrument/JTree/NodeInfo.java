package org.pvv.bcd.instrument.JTree;

/**
 * <p>
 * This is a specialized user object for use in conjunction with
 * <code>MutableTreeNode</code> or <code>DefaultMutableTreeNode</code>
 * type tree nodes.
 * </p>
 *
 * <p>
 * Objects implementing <code>NodeInfo</code> are useful when you want to
 * build instrumented trees with specialized tree nodes. While you
 * will be best served to use <code>DefaultMutableTreeNode</code> type nodes,
 * the <code>NodeInfo</code> interface
 * gives you complete freedom in what kind of
 * user objects to put into those nodes.
 * </p>
 *
 * <p>
 * You will often want your <code>NodeInfo</code>
 * classes to also implement <code>Copyable</code>.
 * This will allow undo/redo and copy/paste to function properly in the
 * instrumented tree.
 * </p>
 *
 * @see NodeInfo
 * @see Copyable
 */
public interface NodeInfo
   extends Cloneable
{
   /** Returns the title of this node as a human-presentable string */
   String getTitle();

   /** Returns the contents of the node
    * CHANGED from String to Object /Daniel */
   Object getContents();

   /** Returns the data model of the contents */
   //Object getContentsModel();
}
