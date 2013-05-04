package org.pvv.bcd.instrument.JTree;

/**
 * Implemented by tree node contents (user objects) that can make copies of
 * themselves. This interface should not be confused with the standard java
 * interface Cloneable.
 * 
 * @see SubTreeNode#createNode
 */
public interface Copyable {
	Copyable copy();
}
