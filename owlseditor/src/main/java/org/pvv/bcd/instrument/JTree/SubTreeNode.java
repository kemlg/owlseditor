package org.pvv.bcd.instrument.JTree;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sri.owlseditor.cmp.tree.OWLSTreeNode;
import com.sri.owlseditor.cmp.tree.OWLSTreeNodeFactory;
import com.sri.owlseditor.cmp.tree.OWLSTreeNodeInfo;

/**
 * Used for storing the user objects of a complete subtree. Useful in connection
 * with caching, copying and remembering undo information. Very much a utility
 * class.
 */
public class SubTreeNode implements Serializable {
	protected Object m_contents;
	protected SubTreeNode[] m_arrChildren;

	public Object getContents() {
		return m_contents;
	}

	void setContents(Object ob) {
		m_contents = ob;
	}

	/**
	 * Creates the subtree. Stores away all user objects in the nodes that are
	 * copied.
	 * 
	 * @param node
	 *            Node to copy
	 * @param include_subtree
	 *            If true, will copy the node and all its children. If false,
	 *            will only copy the node itself.
	 */
	public SubTreeNode(DefaultMutableTreeNode node, boolean include_subtree) {
		// System.out.println("SubTreeNode() with node " + node +
		// ", include_subtree " + include_subtree);

		m_contents = node.getUserObject();
		if (include_subtree) {
			m_arrChildren = new SubTreeNode[node.getChildCount()];
			for (int i = 0; i < node.getChildCount(); ++i) {
				m_arrChildren[i] = new SubTreeNode(
						(DefaultMutableTreeNode) node.getChildAt(i), true);
			}
		}
	}

	/**
	 * Recreates this node and its entire subtree, if any. If the original
	 * contents of the copied tree nodes implements Copyable, will make a copy
	 * of the contents objects for the recreated tree. If not, the new tree will
	 * contain the same contents references as the original.
	 * 
	 * @return The recreated subtree
	 * 
	 * @see Copyable
	 */
	public DefaultMutableTreeNode createNode() {
		Object new_contents;
		// if (getContents() instanceof Copyable){
		if (false) { // we don't want to make a copy, we'll keep the original
			System.out
					.println("SubTreeNode.createNode(): Making a copy of node contents.");
			new_contents = ((Copyable) getContents()).copy();
		} else
			new_contents = getContents();

		// Modified to create OWLSTreeNodes
		// DefaultMutableTreeNode retval =
		// new DefaultMutableTreeNode(new_contents);

		// This should use Instrumenter.getNodeFactory() instead,
		// but I'm not sure how to pass the Instrumenter instance to
		// this class
		// System.out.println("SubTreeNode.createNode(): new_contents is of " +
		// new_contents.getClass());
		// new_contents is a DefaultNodeInfo, not an OWLSTreeNodeInfo. Not good.
		OWLSTreeNodeInfo ni = (OWLSTreeNodeInfo) new_contents; // this is the
																// Instance
		// Object ob = di.getInstance();
		// System.out.println("Contents of this object is: " + ob);
		OWLSTreeNode owlsnode = OWLSTreeNodeFactory.createTreeNode(ni);
		DefaultMutableTreeNode retval = (DefaultMutableTreeNode) owlsnode;

		if (m_arrChildren != null)
			for (int i = 0; i < m_arrChildren.length; ++i) {
				retval.insert(m_arrChildren[i].createNode(), i);
			}

		return retval;
	}

	public File toFile(File parent) throws IOException {
		File f = new File(parent, getContents().toString());
		f.createNewFile();

		if (m_arrChildren.length > 0) {
			f.mkdir();
			for (int i = 0; i < m_arrChildren.length; ++i) {
				m_arrChildren[i].toFile(f);
			}
		}
		return f;
	}

	public String toString() {
		if (m_contents != null)
			return m_contents.toString();
		else
			return super.toString();
	}

	/**
	 * Serialisation, delegates to ObjectOutputStream.
	 */
	void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Serialisation, delegates to ObjectOutputStream.
	 */
	void readObject(ObjectInputStream ois) throws ClassNotFoundException,
			IOException {
		ois.defaultReadObject();
	}

}
