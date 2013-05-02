package org.pvv.bcd.instrument.JTree;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * <p>
 * A specialized action-wrapper class that handles both the state imposed by the
 * tree and the state imposed from the outside. That is, even if you call
 * <code>setEnabled(true)</code> on an action, it
 * may not return true from <code>isEnabled()</code>.
 * For instance, if the action is a "Copy", the instrumenter will not allow
 * it to be enabled unless there is an active selection in the tree.
 * </p>
 * <p>
 * Two states are taken into account to achieve this. One is the
 * <code>user_state</code>. The other is the <code>tree_state</code>.
 * The user state is set by calling <code>setEnabled()</code>
 * on the <code>InfoBankTreeAction</code>. The
 * tree state depends on the state of the instrumented tree and is supplied
 * by the <code>Action</code> object passed to
 * the constructor. <code>isEnabled()</code>
 * in this class effectively returns
 * <code>(user_state &amp;&amp; tree_state)</code>.
 * </p>
 */
public class InfoBankTreeAction
   extends AbstractAction
   implements PropertyChangeListener
{
   private Action m_act;

   /**
    * Creates the action. Adds myself as property change listener to the
    * given action object so I can react to enablement changes.
    *
    * @param title Title, passed to super.
    * @param act Action object to instrument with dual-state functionality.
    */
   public InfoBankTreeAction(String title, Action act)
   {
      super(title);
      m_act = act;
      m_act.addPropertyChangeListener(this);
   }

   /**
    * Catches the "enabled" property change in the wrapped action object.
    * Fires an "enabled" property change based upon the new joint state of
    * this action.
    *
    * @param ev Event to react to.
    *
    * @todo Replace hardcoded "enabled" string with a public static String
    * defined in Swing for this property name. At time of writing, such a
    * constant was not available.
    */
   public void propertyChange(PropertyChangeEvent ev)
   {
      if (ev.getPropertyName().equalsIgnoreCase("enabled"))
      {
         boolean was =
            ((Boolean)ev.getOldValue()).booleanValue() && super.isEnabled();
         boolean is =
            ((Boolean)ev.getNewValue()).booleanValue() && super.isEnabled();
         firePropertyChange("enabled",
            was ? Boolean.TRUE : Boolean.FALSE,
            is ? Boolean.TRUE : Boolean.FALSE);
      }
   }

   /**
    * Forwards actionPerformed() to my wrapped action.
    *
    * @param ev Event to forward
    */
   public void actionPerformed(ActionEvent ev)
   {
      m_act.actionPerformed(ev);
   }

   /**
    * Returns true only if both I and the wrapped action are both
    * enabled.
    *
    * @return Enabled status of this action
    */
   public boolean isEnabled()
   {
      return super.isEnabled() && m_act.isEnabled();
   }
}


