package org.pvv.bcd.instrument.JTree;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.sri.owlseditor.cmp.tree.OWLSDataFlavorProvider;

/**
 * Black magic time. This will be documented as time permits. Nag if you
 * really need/want it documented.
 */
public class TreeDropTargetListener
   implements DropTargetListener
{
   private JTree m_tree;
   private Instrumenter m_instrument;
   private DefaultMutableTreeNode m_dragOverNode;
   private Timer m_timerScroll = new Timer(100,
      new ActionListener()
      {
         public void actionPerformed(ActionEvent ev)
         {
            maybeScrollList();
         }
      });
   private Timer m_timerExpand = new Timer(1500,
      new ActionListener()
      {
         public void actionPerformed(ActionEvent ev)
         {
            if (m_pathToExpand!=null)
            {
		if (m_bExpandActionIsExpand){
                  m_tree.expandPath(m_pathToExpand);
		  System.out.println("Expanding tree");
		}
		else{
                  m_tree.collapsePath(m_pathToExpand);
		  System.out.println("Collapsing tree");
		}
               m_bAllowExpand = false;
            }
         }
      });
   private TreePath m_pathToExpand;
   private boolean m_bExpandActionIsExpand = true;
   private boolean m_bAllowExpand = true;
   private TreePath m_lastPath;

   private Point m_lastMouseLocation;
   private int m_nLastDropEffect = DragOverData.DRAG_CENTER;

   private static DataFlavor m_textPlainMimeType = null;

   static
   {
      try
      {
         m_textPlainMimeType = new DataFlavor("text/plain");
      }
      catch (ClassNotFoundException ex) { /*System.out.println("NO!");*/}
   }

   public TreeDropTargetListener(JTree tree, Instrumenter instrument)
   {
      m_tree = tree;
      m_instrument = instrument;
      m_timerScroll.setInitialDelay(0);
   }

   protected boolean checkFlavourSupported(DataFlavor[] flavours)
   {
     /* for (int i=0; i<flavours.length; ++i)
         if (
            flavours[i].equals(DefaultTransferable.DEFAULT_NODE_INFO_FLAVOUR)
            || flavours[i].equals(DataFlavor.stringFlavor)
            || flavours[i].equals(DataFlavor.javaFileListFlavor)
            || flavours[i].equals(DataFlavor.getTextPlainUnicodeFlavor())
            || ( flavours[i].isMimeTypeEqual("text/plain"))
            )
            */	
            return true;
/*      System.out.println("----------------------------------------");
      System.out.println(
         "Textplain:"
         +DataFlavor.getTextPlainUnicodeFlavor().getMimeType()
         +" - Primary="
         +DataFlavor.getTextPlainUnicodeFlavor().getPrimaryType()
         +" - Secondary="
         +DataFlavor.getTextPlainUnicodeFlavor().getSubType()
         );
      for (int i=0; i<flavours.length; ++i)
         System.out.println("Offers: "+flavours[i]
            +" MIME=\""+flavours[i].getMimeType()+"\"");*/
      //return false;
   }

   public void dragEnter(DropTargetDragEvent dropDragEvent)
   {
      DataFlavor[] flavours = dropDragEvent.getCurrentDataFlavors();
      if (checkFlavourSupported(flavours))
         dropDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      else
         dropDragEvent.rejectDrag();
   }

   public void dragExit(DropTargetEvent dropEvent)
   {
      checkDragOver(null);
   }

   public void dragOver(DropTargetDragEvent dropDragEvent)
   {
      checkDragOver(dropDragEvent);
   }

   public void dropActionChanged(DropTargetDragEvent dropDragEvent) {}

   public synchronized void drop(DropTargetDropEvent dropDropEvent)
   {
	try
      {
	  m_timerExpand.stop();
         DefaultMutableTreeNode parentnode;
         int parentindex = 0;
         Point loc = dropDropEvent.getLocation();
         TreePath targetpath =
            m_tree.getClosestPathForLocation(loc.x, loc.y);
         if (targetpath!=null)
         {
            parentnode =  (DefaultMutableTreeNode)
               targetpath.getLastPathComponent();

	    // Added by elenius@csl.sri.com -- effect on drop target did not
	    // go away.
	    activateNewDragOverEffect(parentnode, DragOverData.DRAG_NONE);            
	    
	    if (parentnode.getParent() != null)
            {
               parentindex = parentnode.getParent().getIndex(parentnode);
            }
            else
            {
               // For the root node
               if (m_nLastDropEffect != DragOverData.DRAG_CENTER)parentindex = 0;
            }
            if (m_nLastDropEffect != DragOverData.DRAG_CENTER)
            {
               parentnode = (DefaultMutableTreeNode)parentnode.getParent();
               if (m_nLastDropEffect == DragOverData.DRAG_BOTTOM)
                  ++parentindex;
            }
            else
            {
		//parentindex = 0;
		parentindex = parentnode.getChildCount();
            }
         }
         else
         {
            parentnode = (DefaultMutableTreeNode)m_tree.getModel().getRoot();
            parentindex = 0;
         }

         if (parentnode != null)
         {
            //if (dropDropEvent.getTransferable().isDataFlavorSupported(
            //   DefaultTransferable.DEFAULT_NODE_INFO_FLAVOUR))
         	if (true)
            {
               int accept_action;
               if (dropDropEvent.isLocalTransfer())
                  accept_action = DnDConstants.ACTION_MOVE;
               else
                  accept_action = DnDConstants.ACTION_COPY;

               dropDropEvent.acceptDrop(accept_action);

               DefaultTransferInfo info =
                  (DefaultTransferInfo)dropDropEvent.getTransferable().
                     //getTransferData(DefaultTransferable.DEFAULT_NODE_INFO_FLAVOUR);
                  getTransferData(OWLSDataFlavorProvider.getInstance().getOWLSDataFlavor());	

               boolean is_same_tree =
                  dropDropEvent.isLocalTransfer() &&
                  (m_instrument.getMyInstrumentId()
                  == info.getDndId().getInstrumentId());

               SubTreeNode[] nodes = info.getNodes();

               boolean drop_failed = false;
               boolean added_undo = false;

               if (is_same_tree)
               {
                  // Source and destination tree is the same - move the node

                  // First check if I am trying to move a node into itself in
                  // some fashion. This must not be allowed as the results
                  // of doing so are weird.
                  // We assume that the nodes involved in the DnD operation
                  // are the ones that are still selected in the tree. If this
                  // does not hold, this algorithm will fail.
                  DefaultMutableTreeNode iter = parentnode;
                  do
                  {
                     if (m_tree.isPathSelected(new TreePath(iter.getPath())))
                     {
                        return;
                     }
                  } while (
                     (iter = (DefaultMutableTreeNode)iter.getParent())
                        != null);

                  // Then do the move.
                  DefaultMutableTreeNode[] newnodes =
                     new DefaultMutableTreeNode[nodes.length];
                  for (int i=nodes.length-1; i>=0; --i)
                  {
                  	//System.out.println("Node: " + nodes[i] + " of " + nodes[i].getClass() +
                  	//					". Contents: " + nodes[i].getContents() + " of " +
					//					nodes[i].getContents().getClass());
                  	try
                     {
			 m_instrument.doDndCompoundAdd(
                           m_instrument.getMyInstrumentId(),
                           parentnode,
                           nodes[i].createNode(),    // !!
                           parentindex);
                        added_undo = true;
                     }
                     catch (VetoException ex)
                     {
                        drop_failed = true;
                     }
                  }
                  // If the drop failed, we fake being the source since
                  // the source won't be told about it and so won't be
                  // able to complete the DndOperation on its end
                  m_instrument.finishDndOperation(
                     m_instrument.getMyInstrumentId(),
                     false || drop_failed);
               }
               else
               {
               	System.out.println("TreeDropTargetListener.drop(): Moving nodes to new array");	                  // Source and destination are different trees - copy
                  // and insert new nodes into myself
                  DefaultMutableTreeNode[] newnodes =
                     new DefaultMutableTreeNode[nodes.length];
                  for (int i=0; i<nodes.length; i++)
                     newnodes[i] = nodes[i].createNode();
                  m_instrument.addNodes(
                     parentnode,
                     newnodes,
                     parentindex);
               }
               dropDropEvent.getDropTargetContext().dropComplete(!drop_failed);
               if (drop_failed && added_undo)
               {
                  m_instrument.getUndoManager().undo();
               }
            }
            else if (dropDropEvent.getTransferable().isDataFlavorSupported(
               DataFlavor.stringFlavor))
            {
               dropDropEvent.acceptDrop(DnDConstants.ACTION_COPY);

               String info =
                  (String)dropDropEvent.getTransferable().
                     getTransferData(
                        DataFlavor.stringFlavor);
               BufferedReader br = new BufferedReader(new StringReader(info));
               Vector newnodes = new Vector();
               while (br.ready())
               {
                  newnodes.addElement(m_instrument.getNodeFactory()
                     .createNode(br.readLine(), ""));
               }
               DefaultMutableTreeNode[] arr_newnodes =
                  new DefaultMutableTreeNode[newnodes.size()];
               newnodes.copyInto(arr_newnodes);
               m_instrument.addNodes(
                  parentnode,
                  arr_newnodes,
                  parentindex);
               dropDropEvent.getDropTargetContext().dropComplete(true);
            }
            else if (dropDropEvent.getTransferable().isDataFlavorSupported(
               DataFlavor.javaFileListFlavor))
            {
               dropDropEvent.acceptDrop(DnDConstants.ACTION_COPY);

               List info =
                  (List)dropDropEvent.getTransferable().
                     getTransferData(
                        DataFlavor.javaFileListFlavor);
               DefaultMutableTreeNode[] newnodes =
                  new DefaultMutableTreeNode[info.size()];
               Iterator iter = info.iterator();
               int i=0;
               while (iter.hasNext())
               {
                  File file = (File)iter.next();
                  DefaultMutableTreeNode node = makeFileNode(file);
                  newnodes[i++] = node;
               }
               m_instrument.addNodes(
                  parentnode,
                  newnodes,
                  parentindex);
               dropDropEvent.getDropTargetContext().dropComplete(true);
            }
            else if (
               checkForMimeType(
                  dropDropEvent.getTransferable().getTransferDataFlavors(),
                  "text/plain") != null
               )
            {
               dropDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
               DataFlavor flavour =
                  checkForMimeType(
                     dropDropEvent.getTransferable().getTransferDataFlavors(),
                     "text/plain");

               Reader info =
                  flavour.getReaderForText(dropDropEvent.getTransferable());
               BufferedReader br = new BufferedReader(info);
               Vector newnodes = new Vector();
               while (br.ready())
               {
                  Object ob = m_instrument.getNodeFactory()
                     .createNode(br.readLine(), "");
                  if (br.ready())
                     newnodes.addElement(ob);
               }
               DefaultMutableTreeNode[] arr_newnodes =
                  new DefaultMutableTreeNode[newnodes.size()];
               newnodes.copyInto(arr_newnodes);
               m_instrument.addNodes(
                  parentnode,
                  arr_newnodes,
                  parentindex);
               dropDropEvent.getDropTargetContext().dropComplete(true);
            }
            else
            {
               dropDropEvent.rejectDrop();
            }
         }
         else
         {
            dropDropEvent.rejectDrop();
         }
      }
      catch (IOException ex1)
      {
         ex1.printStackTrace();
         dropDropEvent.rejectDrop();
      }
      catch (UnsupportedFlavorException ex2)
      {
         ex2.printStackTrace();
         dropDropEvent.rejectDrop();
      }
   }

   private void checkDragOver(DropTargetDragEvent ev)
   {
      boolean clear_old_node = false;
      DefaultMutableTreeNode new_node = null;
      int new_effect = DragOverData.DRAG_NONE;

      if (ev==null)
      {
	  if (m_dragOverNode != null)
            clear_old_node = true;
         m_timerScroll.stop();
         m_timerExpand.stop();
      }
      else
      {
         int old_row = -1;
         int old_effect = 0;
         Point loc = ev.getLocation();
         m_lastMouseLocation = loc;
         TreePath path = m_tree.getPathForLocation(loc.x, loc.y);
         if (path==null || !path.equals(m_lastPath))
         {
            m_lastPath = path;
            m_bAllowExpand = true;
         }

         // Scroll tree up or down if dragging over the extremities
         if ( (locIsOnUppermostRow(loc) && !checkScrollBarAtTop())
            || (locIsOnLowermostRow(loc)&& !checkScrollBarAtBottom()))
         {
            m_timerExpand.stop();

            if (!m_timerScroll.isRunning())
            {
               m_timerScroll.start();
            }
         }
         else
         {
            m_timerScroll.stop();

            // If the node under us is expandable, expand/collapse as
            // appropriate after a short delay
            if (path!=null
               && !m_tree.getModel().isLeaf(path.getLastPathComponent()))
            {
               boolean expand = !m_tree.isExpanded(path);
               if (m_bAllowExpand
                  && (!m_timerExpand.isRunning()
                     || !path.equals(m_pathToExpand)
                     || m_bExpandActionIsExpand!=expand))
               {
                  m_timerExpand.stop();
                  m_bExpandActionIsExpand = expand;
                  m_pathToExpand = path;
                  m_timerExpand.start();
               }
            }
            else
               m_timerExpand.stop();
         }

         // Paint a small insertion marker over, on or under the node
         int row = m_tree.getClosestRowForLocation(loc.x, loc.y);
         if (m_dragOverNode != null)
         {
            old_row =
               m_tree.getRowForPath(new TreePath(m_dragOverNode.getPath()));
            if (m_dragOverNode.getUserObject() instanceof DragOverData)
            {
               old_effect =
                  ((DragOverData)m_dragOverNode.getUserObject())
                     .getCurrentEffect();
            }
            if (old_row != row)
		// This should definitely execute, but it doesn't
               clear_old_node = true;
         }
         if (row!=-1)
         {
            new_node = (DefaultMutableTreeNode)
               m_tree.getPathForRow(row).getLastPathComponent();
            new_effect = determineEffect(loc, row);
            if (row==old_row && new_effect==old_effect)
            {
               new_node = null;
            }
         }
      }

      if (clear_old_node)
      {
	  activateNewDragOverEffect(m_dragOverNode, DragOverData.DRAG_NONE);
         m_dragOverNode = null;
      }
      if (new_node!=null)
      {
         activateNewDragOverEffect(new_node, new_effect);
         m_dragOverNode = new_node;
         m_nLastDropEffect = new_effect;
      }
   }

   private int determineEffect(Point loc, int row)
   {
      Rectangle bounds = m_tree.getRowBounds(row);
      Point t_loc = new Point(loc);
      t_loc.translate(-bounds.x, -bounds.y);
      if (t_loc.y<0.25*bounds.height)
         return DragOverData.DRAG_TOP;
      if (t_loc.y>0.75*bounds.height)
         return DragOverData.DRAG_BOTTOM;
      return DragOverData.DRAG_CENTER;
   }

   private void activateNewDragOverEffect(
      DefaultMutableTreeNode node,
      int effect)
   {
      if (node.getUserObject() instanceof DragOverData)
      {
         DragOverData dod = (DragOverData)node.getUserObject();
         dod.setCurrentEffect(effect);
         m_tree.getModel().valueForPathChanged(
            new TreePath(node.getPath()),
            dod);
      }
   }

   private boolean locIsOnUppermostRow(Point loc)
   {
      if (loc==null)
         return false;
      int height = m_tree.getRowHeight();
      if (height<=0)
         height = 10; // Best guess
      int top_row = m_tree.getClosestRowForLocation(
         0,
         m_tree.getVisibleRect().y
            +height);
      if (top_row!=-1)
      {
         Rectangle bounds = m_tree.getRowBounds(top_row);
         return (loc.y <= bounds.y+bounds.height);
      }
      return false;
   }

   private boolean locIsOnLowermostRow(Point loc)
   {
      if (loc==null)
         return false;
      int height = m_tree.getRowHeight();
      if (height<=0)
         height = 10; // Best guess
      int bot_row = m_tree.getClosestRowForLocation(
         0,
         m_tree.getVisibleRect().y
            +m_tree.getVisibleRect().height
            -height);
      if (bot_row!=-1)
      {
         Rectangle bounds = m_tree.getRowBounds(bot_row);
         return (loc.y >= bounds.y);
      }
      return false;
   }

   private JScrollBar fetchScrollBar()
   {
      Component parent = m_tree.getParent();
      if (parent==null
         || ! (parent instanceof JViewport))
         return null;
      parent = parent.getParent();
      if (parent==null
         || ! (parent instanceof JScrollPane))
         return null;

      JScrollPane pane = (JScrollPane)parent;
      return pane.getVerticalScrollBar();
   }

   private boolean checkScrollBarAtTop()
   {
      JScrollBar bar = fetchScrollBar();
      if (bar == null) return true;

      return (bar.getValue() == bar.getMinimum());
   }

   private boolean checkScrollBarAtBottom()
   {
      JScrollBar bar = fetchScrollBar();
      if (bar == null) return true;

      return (bar.getValue() >= (bar.getMaximum() - bar.getVisibleAmount()));
   }

   private void maybeScrollList()
   {
      JScrollBar bar = fetchScrollBar();
      if (bar == null) return;

      if (locIsOnLowermostRow(m_lastMouseLocation))
      {
         bar.setValue(bar.getValue()+bar.getBlockIncrement());
      }
      else if (locIsOnUppermostRow(m_lastMouseLocation))
      {
         bar.setValue(bar.getValue()-bar.getBlockIncrement());
      }
   }

   protected DefaultMutableTreeNode makeFileNode(File file)
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_instrument.getNodeFactory().createNode(file.getName(), "");
      if (file.isDirectory())
      {
         File[] children = file.listFiles();
         for (int i=0; i<children.length; ++i)
            node.add(makeFileNode(children[i]));
      }
      return node;
   }

   protected DataFlavor checkForMimeType(DataFlavor[] flavours, String mimetype)
   {
      DataFlavor found = null;
      for (int i=0; i<flavours.length; ++i)
         if (flavours[i].isMimeTypeEqual(mimetype))
         {
            if (found==null
               || flavours[i].getParameter("charset")
                  .equalsIgnoreCase("unicode"))
            found = flavours[i];
         }
      return found;
   }
}
