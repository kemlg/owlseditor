package org.pvv.bcd.instrument.JTree;

import java.util.EventListener;

/**
 * Interface to implement for anyone who wants to be notified of
 * tree structure changes. It is also possible to veto these
 * events, which can be great fun.
 *
 * @todo We need a set of events that are <em>not</em> vetoable
 * and that get fired after the vetoable events if the change
 * actually took place. As it is now, early receivers of these events
 * have no way of knowing whether or not later receivers will veto
 * them and so don't really know whether it is happening or not.
 */
public interface VetoableTreeStructureChangeListener
   extends EventListener
{
   public void nodeCreation(VetoableTreeStructureChangeEvent e)
      throws VetoException;
   public void nodeDeletion(VetoableTreeStructureChangeEvent e)
      throws VetoException;
   public void nodeMove(VetoableTreeStructureChangeEvent e)
      throws VetoException;
   public void nodeRename(VetoableTreeStructureChangeEvent e)
      throws VetoException;
}
