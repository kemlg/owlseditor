package org.pvv.bcd.instrument.JTree;

/**
 * Interface to implement for NodeFactory classes that want to offer
 * node renaming capabilities.
 *
 * @see NodeFactory
 * @see DefaultNodeFactory
 */
public interface UserObjectFactory
{
   /**
    * Changes the name of a given user object.
    *
    * @param userobject User object to change name of
    * @param title Title to set on user object
    */
   public void setUserObjectTitle(Object userobject, String title);
}
