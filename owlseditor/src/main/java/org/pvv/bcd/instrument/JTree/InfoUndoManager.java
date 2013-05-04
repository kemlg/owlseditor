package org.pvv.bcd.instrument.JTree;

import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

/**
 * <p>
 * A specialized undo manager that offers many capabilities beyond that of
 * Swing's regular undo manager. Much of this functionality was inspired (some
 * would say stolen) from the excellent book <b>JAVA 2 Developer's Handbook</b>
 * by Phillip Heller and Simon Roberts.
 * </p>
 * 
 * <p>
 * The most immediately interesting feature of this manager is that it fires
 * events whenever something happens to alter the undo status.
 * </p>
 * 
 * <p>
 * It will fire a <code>StateChangedEvent</code> whenever a redo or undo takes
 * place or when edits have been discarded. It will fire an
 * <code>UndoableEditEvent</code> whenever a new undo event is added to it. This
 * can be used to track undo/redo lists in the GUI etc.
 * </p>
 * 
 * <p>
 * The class also offers methods for retrieving lists of undos and redos.
 * </p>
 */
public class InfoUndoManager extends UndoManager {

	private Object source;
	private UndoableEditSupport m_undoSupport = new UndoableEditSupport() {
		public synchronized void postEdit(UndoableEdit ue) {
			realSource = source;
			super.postEdit(ue);
		}
	};
	private transient Vector changeListeners;

	public InfoUndoManager() {
		super();
	}

	public synchronized void undo() throws CannotUndoException {
		super.undo();
		fireStateChanged(new ChangeEvent(this));
	}

	public synchronized void undoTo(UndoableEdit edit) {
		super.undoTo(edit);
		fireStateChanged(new ChangeEvent(this));
	}

	public synchronized void redo() throws CannotRedoException {
		super.redo();
		fireStateChanged(new ChangeEvent(this));
	}

	public synchronized void redoTo(UndoableEdit edit) {
		super.redoTo(edit);
		fireStateChanged(new ChangeEvent(this));
	}

	public synchronized void undoOrRedo() throws CannotUndoException,
			CannotRedoException {
		super.undoOrRedo();
		fireStateChanged(new ChangeEvent(this));
	}

	/**
	 * Retrieves an array of all edits.
	 * 
	 * @return Array of undoable edits.
	 */
	public synchronized UndoableEdit[] getEdits() {
		UndoableEdit[] array = new UndoableEdit[edits.size()];
		edits.copyInto(array);
		return array;
	}

	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		fireStateChanged(new ChangeEvent(this));
	}

	/**
	 * Returns a list of all outstanding undoable edits. Useful, for instance,
	 * for presentation in a GUI so the user can see what has happened and what
	 * can be undone.
	 * 
	 * @return Array of undoable edits. Will contain only edits that can be
	 *         undone and that are "significant".
	 */
	public synchronized UndoableEdit[] getUndoableEdits() {
		int size = edits.size();
		Vector v = new Vector(size);
		for (int i = size - 1; i >= 0; --i) {
			UndoableEdit u = (UndoableEdit) edits.elementAt(i);
			if (u.canUndo() && u.isSignificant())
				v.addElement(u);
		}
		UndoableEdit[] array = new UndoableEdit[v.size()];
		v.copyInto(array);
		return array;
	}

	/**
	 * Returns a list of all outstanding redoable edits. Useful, for instance,
	 * for presentation in a GUI so the user can see what has happened and what
	 * can be redone.
	 * 
	 * @return Array of redoable edits. Will contain only edits that can be
	 *         redone and that are "significant".
	 */
	public synchronized UndoableEdit[] getRedoableEdits() {
		int size = edits.size();
		Vector v = new Vector(size);
		for (int i = 0; i < size; ++i) {
			UndoableEdit u = (UndoableEdit) edits.elementAt(i);
			if (u.canRedo() && u.isSignificant())
				v.addElement(u);
		}
		UndoableEdit[] array = new UndoableEdit[v.size()];
		v.copyInto(array);
		return array;
	}

	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean b = super.addEdit(anEdit);
		if (b)
			m_undoSupport.postEdit(anEdit);
		return b;
	}

	private static int count = 0;

	public synchronized void undoableEditHappened(UndoableEditEvent ev) {
		UndoableEdit ue = ev.getEdit();
		source = ev.getSource();
		addEdit(ue);
	}

	public synchronized void addUndoableEditListener(UndoableEditListener l) {
		m_undoSupport.addUndoableEditListener(l);
	}

	public synchronized void removeUndoableEditListener(UndoableEditListener l) {
		m_undoSupport.removeUndoableEditListener(l);
	}

	public synchronized void removeChangeListener(ChangeListener l) {
		if (changeListeners != null && changeListeners.contains(l)) {
			Vector v = (Vector) changeListeners.clone();
			v.removeElement(l);
			changeListeners = v;
		}
	}

	public synchronized void addChangeListener(ChangeListener l) {
		Vector v = changeListeners == null ? new Vector(2)
				: (Vector) changeListeners.clone();
		if (!v.contains(l)) {
			v.addElement(l);
			changeListeners = v;
		}
	}

	protected void fireStateChanged(ChangeEvent e) {
		if (changeListeners != null) {
			Vector listeners = changeListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++) {
				((ChangeListener) listeners.elementAt(i)).stateChanged(e);
			}
		}
	}

}
