package org.pvv.bcd.instrument.JTree;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.pvv.bcd.Util.FeatureNotSupportedException;

/**
 * Black magic time. This will be documented as time permits. Nag if you
 * really need/want it documented.
 */
public class TreeDragGestureListener
   implements DragGestureListener
{
   private JTree m_tree;
   private Instrumenter m_instrument;
   private DragSource m_dragSource = DragSource.getDefaultDragSource();

   public TreeDragGestureListener(JTree tree, Instrumenter instrument)
   {
      m_tree = tree;
      m_instrument = instrument;
   }

   public void dragGestureRecognized( DragGestureEvent dragGestureEvent )
   {
      try
      {
      	TreePath[] paths = m_tree.getSelectionPaths();
         paths = Instrumenter.reduceSelection(paths);
         if (paths==null || paths.length==0)
         {
            ;
         }
         else
         {
            DefaultMutableTreeNode[] nodes =
               new DefaultMutableTreeNode[paths.length];
            for (int i=0; i<nodes.length; i++)
               nodes[i] =
                  (DefaultMutableTreeNode)paths[i].getLastPathComponent();
      		
      		//TreePath m_tree.getSelectionPath();
      		//DefaultMutableTreeNode[] nodes = 
      	
            // debugging
            NodeFactory nf = m_instrument.getNodeFactory();
            DndId id = m_instrument.getNextDndId();
            Transferable trans = nf.createTransferable(nodes, id);
            											
            m_dragSource.startDrag(
               dragGestureEvent,
               DragSource.DefaultCopyDrop,
               trans,
               new TreeDragSourceListener(m_tree, m_instrument, nodes));
         }
      }
      catch (FeatureNotSupportedException ex)
      {
         System.out.println(ex.toString());
      }
   }
}
