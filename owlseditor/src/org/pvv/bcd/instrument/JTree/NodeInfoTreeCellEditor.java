package org.pvv.bcd.instrument.JTree;

import java.awt.Component;
import java.awt.Container;
import java.awt.TextComponent;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Specialized tree cell editor to give us some extra functionality. Most
 * importantly, it creates <code>DefaultNodeInfo</code>
 * objects in stead of the standard String objects.
 *
 * @see DefaultNodeInfo
 */
public class NodeInfoTreeCellEditor
   extends DefaultTreeCellEditor
{
   private Object m_object;
   private Instrumenter m_instr;

   public NodeInfoTreeCellEditor(
      JTree tree,
      DefaultTreeCellRenderer btcr,
      Instrumenter instrument )
   {
      super(tree, btcr);
      m_instr = instrument;
   }

   /**
    * Returns DefaultNodeInfo objects.
    *
    * @see DefaultNodeInfo
    */
   public Object getCellEditorValue()
   {
      boolean veto = false;
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)m_object;
      Object userobject = node.getUserObject();
      String newname = (String)super.getCellEditorValue();

      try
      {
         m_instr.fireNodeRename(new VetoableTreeStructureChangeEvent(
            this,
            node,
            node.toString(),
            newname));
      }
      catch (VetoException ex)
      {
         veto = true;
      }

      if (!veto)
      {
         ((UserObjectFactory) m_instr.getNodeFactory()).setUserObjectTitle(
            userobject,
            newname );
      }

      return userobject;
   }

   /**
    * Provides the extra functionality of selecting all the
    * text in the cell when edit starts.
    */
   public Component getTreeCellEditorComponent(JTree tree, Object value,
      boolean isSelected,
      boolean expanded,
      boolean leaf,
      int row)
   {
      m_object = value;
      Component c = super.getTreeCellEditorComponent(
         tree, value, isSelected, expanded, leaf, row);

      // A bit of magic to get the text to start out selected
      Vector comps = new Vector();
      if (realEditor instanceof DefaultCellEditor)
         getAllComps(((DefaultCellEditor)realEditor).getComponent(), comps);

      for (int i=0; i<comps.size(); ++i)
      {
         if (comps.elementAt(i) instanceof TextComponent)
            ((TextComponent)comps.elementAt(i)).selectAll();
         if (comps.elementAt(i) instanceof JTextComponent)
            ((JTextComponent)comps.elementAt(i)).selectAll();
      }
      return c;
   }

   /**
    * Utility method to recursively retrieve all components in
    * a container.
    */
   protected void getAllComps(Component c, Vector v)
   {
      v.addElement(c);
      if (c instanceof Container)
      {
         for (int i=0; i<((Container)c).getComponentCount(); ++i)
            getAllComps(((Container)c).getComponent(i), v);
      }
   }
}

