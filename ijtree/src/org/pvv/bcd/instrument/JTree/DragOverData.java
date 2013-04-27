package org.pvv.bcd.instrument.JTree;

/**
 * This interface should be implemented by any custom <code>NodeInfo</code>
 * that wants drag-over effects to be apparant when the user drags nodes
 * around within the tree.
 *
 * @see NodeInfoTreeCellRenderer#paint()
 */
public interface DragOverData
{
   /** Constant used for no drag effect */
   public final static int DRAG_NONE   = 0;
   /** Constant used for "will be dropped as child of this node" */
   public final static int DRAG_CENTER = 1;
   /** Constant used for "will be dropped as sibling above this node" */
   public final static int DRAG_TOP    = 2;
   /** Constant used for "will be dropped as sibling below this node" */
   public final static int DRAG_BOTTOM = 3;

   /**
    * Retrieves the current drag-over effect for this node. Useful in
    * conjunction with graphical presentation.
    *
    * @return Current drag-over effect.
    */
   int getCurrentEffect();

   /**
    * Sets the current drag-over effect for this node. Useful in
    * conjunction with graphical presentation.
    *
    * @param effect New drag-over effect.
    */
   void setCurrentEffect(int effect);
}
