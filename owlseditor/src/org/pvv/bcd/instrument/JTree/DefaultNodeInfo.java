package org.pvv.bcd.instrument.JTree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Default implementation of the info present on a single node. It bases its
 * contents model on a <code>DefaultStyledDocument</code>.
 * 
 * @see javax.swing.text.DefaultStyledDocument
 */
public class DefaultNodeInfo implements Serializable, NodeInfo, Copyable,
		DragOverData {

	static int INITIALSIZE = 5;
	static int SIZEINCREMENT = 10;

	// private DefaultStyledDocument m_document = new DefaultStyledDocument();
	private Object userObject;
	private String m_header;
	transient private int m_nDragOverEffect = DragOverData.DRAG_NONE;

	/**
	 * Creates a new default node. It has a title of "<unnamed node>" and an
	 * empty string for its textual contents.
	 */
	public DefaultNodeInfo() {
		this("<unnamed node>", "");
	}

	/**
	 * Creates a new node with the given title and an empty string for contents.
	 * 
	 * @param newhead
	 *            Name for new node
	 */
	public DefaultNodeInfo(String newhead) {
		this(newhead, "");
	}

	/**
	 * Creates a new node with the given title and textual contents.
	 * 
	 * @param newhead
	 *            Name for new node
	 * @param newtxt
	 *            Textual content of new node CHANGED from String to Object
	 *            /Daniel
	 */
	public DefaultNodeInfo(String newhead, Object userObject) {
		setTitle(newhead);
		setContents(userObject);
	}

	/**
	 * Copy constructor. Does a deep copy.
	 * 
	 * @param ni
	 *            Node to copy
	 */
	public DefaultNodeInfo(NodeInfo ni) {
		this(ni.getTitle(), ni.getContents());
	}

	/**
	 * Retrieves the textual representation of the contents model.
	 * 
	 * @return Our textual contents or an empty string if there was none CHANGED
	 *         from String to Object /Daniel
	 */
	public Object getContents() {
		/*
		 * try { return m_document.getText(0, m_document.getLength()); } catch
		 * (BadLocationException ex){} return "";
		 */
		return userObject;
	}

	/**
	 * Sets the contents by specifying its textual contents.
	 * 
	 * @param newtxt
	 *            New textual contents
	 */
	public void setContents(Object userObject) {
		/*
		 * try { if (newtxt==null) newtxt = ""; m_document.remove(0,
		 * m_document.getLength()); m_document.insertString(0, newtxt, null); }
		 * catch (BadLocationException ex){}
		 */
		this.userObject = userObject;
	}

	/**
	 * Returns the data model of the node contents. This is a
	 * <code>DefaultStyledDocument</code>.
	 * 
	 * @return Data model of our contents
	 */
	/*
	 * public Object getContentsModel() { return m_document; }
	 */

	/**
	 * Returns the title of the node.
	 * 
	 * @return Our title
	 */
	public String getTitle() {
		return m_header;
	}

	/**
	 * Fills data into myself from another node. Does a deep copy.
	 * 
	 * @param ni
	 *            Node to copy from
	 */
	public void copyFrom(NodeInfo ni) {
		setTitle(ni.getTitle());
		setContents(ni.getContents());
	}

	/**
	 * Sets my title.
	 * 
	 * @param newhead
	 *            New title
	 */
	public void setTitle(String newhead) {
		if (newhead == null)
			newhead = "";
		m_header = newhead;
	}

	/**
	 * Returns my title.
	 * 
	 * @return My title
	 */
	public String toString() {
		return m_header;
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

	/**
	 * Makes a new copy of myself. Does a deep copy.
	 * 
	 * @return Copy of myself in a newly allocated object
	 */
	public Copyable copy() {
		return new DefaultNodeInfo(this);
	}

	/**
	 * Retrieves the current drag-over effect for this node. Useful in
	 * conjunction with graphical presentation.
	 * 
	 * @return Current drag-over effect.
	 */
	public int getCurrentEffect() {
		return m_nDragOverEffect;
	}

	/**
	 * Sets the current drag-over effect for this node. Useful in conjunction
	 * with graphical presentation.
	 * 
	 * @param effect
	 *            New drag-over effect.
	 */
	public void setCurrentEffect(int effect) {
		m_nDragOverEffect = effect;
	}
}
