package org.pvv.bcd.instrument.JTree;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Black magic time. This will be documented as time permits. Nag if you really
 * need/want it documented.
 */
public class TreeDragSourceListener implements DragSourceListener {
	private JTree m_tree;
	private Instrumenter m_instrument;
	private DefaultMutableTreeNode[] m_nodes;

	public TreeDragSourceListener(JTree tree, Instrumenter instrument,
			DefaultMutableTreeNode[] nodes) {
		m_tree = tree;
		m_instrument = instrument;
		m_nodes = nodes;
	}

	public void dragDropEnd(DragSourceDropEvent dropEvent) {
		if (dropEvent.getDropAction() == DnDConstants.ACTION_MOVE) {
			boolean added_undo = false;
			boolean veto = false;
			try {
				for (int i = 0; i < m_nodes.length; ++i) {
					m_instrument.doDndCompoundDelete(
							m_instrument.getMyInstrumentId(), m_nodes[i]);
					added_undo = true;
				}
			} catch (VetoException ex) {
				veto = true;
			} finally {
				m_instrument.finishDndOperation(
						m_instrument.getMyInstrumentId(), true);
			}
			if (veto && added_undo) {
				m_instrument.getUndoManager().undo();
			}
		}
	}

	public void dragEnter(DragSourceDragEvent dragEvent) {
	}

	public void dragExit(DragSourceEvent dragEvent) {
	}

	public void dragOver(DragSourceDragEvent dragEvent) {
	}

	public void dropActionChanged(DragSourceDragEvent dragEvent) {
	}
}
