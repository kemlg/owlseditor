package org.pvv.bcd.instrument.JTree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

/**
 * Class representing an undoable edit operation on an instrumented tree.
 * This class assumes that the nodes
 * in the tree are <code>DefaultMutableTreeNode</code>
 * type objects and that the tree model is a <code>DefaultTreeModel</code>.
 */
public class UndoableInfoEdit
   extends AbstractUndoableEdit
{
   /** Identifier for the "add node" undoable edit */
   public static final short TYPE_ADD       = 1;
   /** Identifier for the "delete node" undoable edit */
   public static final short TYPE_DELETE    = 2;
   /** Identifier for the "move node" undoable edit */
   public static final short TYPE_MOVE      = 3;

   protected int[] m_arrSourcePath;
   protected int[] m_arrDestPath;
   protected short m_nType;
   protected SubTreeNode m_tree;
   protected DefaultTreeModel m_model;

   /**
    * Creates a new undoable edit.
    *
    * @param model Tree model that originated the undoable edit.
    * @param type Type of edit. Either
    * @link{#TYPE_ADD}, @link{#TYPE_DELETE} or @link{#TYPE_MOVE}.
    * @param sourceNode The parent of the original node location.
    * If this is an "add" type edit, <code>sourceNode</code> is null.
    * @param sourceIndex Child index of node in sourceNode.
    * If <code>sourceNode</code> is
    * null, this value is disregarded.
    * @param destNode The parent of the final node location.
    * If this is a "delete" type edit, <code>destNode</code> is null.
    * @param destIndex Child index of node in <code>destNode</code>.
    * If <code>destNode</code> is null,
    * this value is disregarded.
    * @param include_subtree If true, will include the given node and its
    * subtree in the operation. If false, will only include the node itself.
    */
   public UndoableInfoEdit(
      DefaultTreeModel model,
      short type,
      DefaultMutableTreeNode sourceNode,
      int sourceIndex,
      DefaultMutableTreeNode destNode,
      int destIndex,
      DefaultMutableTreeNode node,
      boolean include_subtree)
   {
       //System.out.println("UndoableInfoEdit() with destNode " + node);
   		if (sourceNode!=null)
         m_arrSourcePath =
            makePath(
               (DefaultMutableTreeNode)sourceNode.getRoot(),
               sourceNode,
               sourceIndex);
      if (destNode!=null)
         m_arrDestPath =
            makePath(
               (DefaultMutableTreeNode)destNode.getRoot(),
               destNode,
               destIndex);
      m_model = model;
      m_nType = type;
      makeTree(node, include_subtree);
   }

   /**
    * Releases resources.
    */
   public void die()
   {
      super.die();
      m_tree = null;
      m_arrSourcePath = null;
      m_arrDestPath = null;
      m_model = null;
   }

   /**
    * <p>
    * Returns an undoable edit name suitable for presentation to the user.
    * The name is based on the type of the edit operation and the title
    * of the node. If the node has no title, it is only based on the
    * edit operation.
    * </p>
    * <p>
    * <em>Examples:</em><br>
    * For a node with title "Expenses 2001"
    * <pre>
    * Add Node Expenses 2001
    * Delete Node Expenses 2001
    * Move Node Expenses 2001
    * </pre>
    * </p>
    *
    * @return Our presentation name
    */
   public String getPresentationName()
   {
      String title;
      if (m_tree!=null
         && m_tree.getContents() != null
         && m_tree.getContents() instanceof NodeInfo
         && ((NodeInfo)m_tree.getContents()).getTitle()!= null
         && ((NodeInfo)m_tree.getContents()).getTitle().length()>0)
      {
         title = " "+((NodeInfo)m_tree.getContents()).getTitle();
      }
      else
         title = "";
      switch (getType())
      {
         case TYPE_ADD:
            return "Add Node"+title;
         case TYPE_DELETE:
            return "Delete Node"+title;
         case TYPE_MOVE:
            return "Move Node"+title;
         default:
            return "";
      }
   }

   /**
    * Returns the edit type.
    *
    * @see #TYPE_ADD
    * @see #TYPE_DELETE
    * @see #TYPE_MOVE
    */
   public short getType() { return m_nType; }

   /**
    * Returns the user object of the root node in the undoable edit
    * tree stored by myself.
    */
   public Object getContents() { return m_tree.getContents(); }
   private void setContents(Object ob) { m_tree.setContents(ob); }

   protected int[] makePath(
      DefaultMutableTreeNode root,
      DefaultMutableTreeNode parent,
      int index)
   {
      if (parent==null) return null;
      int[] retval = new int[parent.getLevel()-root.getLevel()+2];
      retval[retval.length-1] = index;
      int i=retval.length-2;
      TreeNode iter_node = parent;
      do
      {
         if (iter_node.getParent()==null)
            retval[i]=0;
         else
         {
            retval[i] = iter_node.getParent().getIndex(iter_node);
         }
         iter_node = iter_node.getParent();
      }
      while (i-->0);
      return retval;
   }

   protected void makeTree(DefaultMutableTreeNode node, boolean include_subtree)
   {
      m_tree = new SubTreeNode(node, include_subtree);
   }

   protected void moveNode(
      int[] from,
      int[] to)
   {
      DefaultMutableTreeNode node_to_move =
         (DefaultMutableTreeNode)m_model.getRoot();

      // Find node to move
      for (int i=1; i<from.length; ++i)
      {
         node_to_move =
            (DefaultMutableTreeNode)node_to_move
               .getChildAt(from[i]);
      }

      // Find parent to insert into
      DefaultMutableTreeNode new_parent =
         (DefaultMutableTreeNode)m_model.getRoot();

      // Find parent node to insert into
      int i;
      for (i=1; i<to.length-1; ++i)
      {
         new_parent =
            (DefaultMutableTreeNode)new_parent.getChildAt(to[i]);
      }

      // Then move the node
      m_model.removeNodeFromParent(node_to_move);
      m_model.insertNodeInto(
         node_to_move,
         new_parent,
         to[i]);
   }

   protected void removeNode(
      int[] path)
   {
      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)m_model.getRoot();

      // Find node to remove
      for (int i=1; i<path.length; ++i)
      {
         node = (DefaultMutableTreeNode)node.getChildAt(path[i]);
      }

      // Remove it
      m_model.removeNodeFromParent(node);
   }

   protected void insertNode(
      int[] path)
   {
      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)m_model.getRoot();

      // Find parent node to insert into
      int i;
      for (i=1; i<path.length-1; ++i)
      {
         node = (DefaultMutableTreeNode)node.getChildAt(path[i]);
      }

      // Reconstruct node to insert
      DefaultMutableTreeNode newnode = m_tree.createNode();

      // Insert node into tree
      m_model.insertNodeInto(
         newnode,
         node,
         path[i]);
   }

   /**
    * Saves the NodeInfo from the node at "path" into myself. The node at
    * "path" should be the node that I represent an undoable edit for! It is
    * very rarely necessary to change this information.
    *
    * @param tree Reference to the tree model to find the node in
    * @param path Array of integer values specifying the path to take from
    * the root node to find the node to copy from
    */
   protected void saveNodeInfo(int[] path)
   {
      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)m_model.getRoot();

      // Find node
      int i;
      for (i=1; i<path.length; ++i)
      {
         node = (DefaultMutableTreeNode)node.getChildAt(path[i]);
      }

      setContents(node.getUserObject());
   }

   public void undo()
      throws CannotUndoException
   {
      super.undo();
      switch (getType())
      {
         case TYPE_MOVE:
            moveNode(m_arrDestPath, m_arrSourcePath);
            break;
         case TYPE_ADD:
         {
            // We save the Node Info in case we are asked to "redo" this undo
            saveNodeInfo(m_arrDestPath);
            removeNode(m_arrDestPath);
            break;
         }
         case TYPE_DELETE:
            insertNode(m_arrSourcePath);
            break;
      }
   }

   public void redo()
      throws CannotUndoException
   {
      super.redo();
      switch (getType())
      {
         case TYPE_MOVE:
            moveNode(m_arrSourcePath, m_arrDestPath);
            break;
         case TYPE_DELETE:
            removeNode(m_arrSourcePath);
            break;
         case TYPE_ADD:
            insertNode(m_arrDestPath);
            break;
      }
   }
}

