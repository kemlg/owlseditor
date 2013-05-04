package org.pvv.bcd.instrument.JTree;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class NodeInfoTreeCellRenderer extends DefaultTreeCellRenderer {
	private int m_nDndEffect;

	public NodeInfoTreeCellRenderer() {
		super();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof DefaultMutableTreeNode
				&& ((DefaultMutableTreeNode) value).getUserObject() instanceof DragOverData) {
			m_nDndEffect = ((DragOverData) ((DefaultMutableTreeNode) value)
					.getUserObject()).getCurrentEffect();
		} else
			m_nDndEffect = DragOverData.DRAG_NONE;

		return super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, hasFocus);
	}

	/**
	 * Calls <code>super.paint()</code> and then proceed to draw black lines
	 * indicating the drag-over effect on the node. Specifically, if the
	 * drag-over effect is "drop above", paints a horizontal black line across
	 * the top of the component. If the effect is "drop below", paints a
	 * horizontal black line across the bottom of the component. If the effect
	 * is "drop into (as child)", paints a vertical black line along the left
	 * side of the component. All these lines are two pixels wide and not
	 * particularly pretty.
	 */
	public void paint(Graphics g) {
		super.paint(g);

		Rectangle bounds = g.getClipBounds();
		switch (m_nDndEffect) {
		case DragOverData.DRAG_BOTTOM: {
			g.fillRect(0, bounds.y + bounds.height - 2, bounds.width, 2);
			break;
		}
		case DragOverData.DRAG_TOP: {
			g.fillRect(0, 0, bounds.width, 2);
			break;
		}
		case DragOverData.DRAG_CENTER: {
			g.fillRect(0, 0, 2, bounds.height);
			break;
		}
		default:
			break;
		}
	}
}
