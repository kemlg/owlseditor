/*****************************************************************************************
"The contents of this file are subject to the Mozilla Public License  Version 1.1 
(the "License"); you may not use this file except in compliance with the License.  
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License for the specific 
language governing rights and limitations under the License.

The Original Code is OWL-S Editor for Protege.

The Initial Developer of the Original Code is SRI International. 
Portions created by the Initial Developer are Copyright (C) 2004 the Initial Developer.  
All Rights Reserved.
 ******************************************************************************************/
package com.sri.owlseditor.cmp.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.pvv.bcd.instrument.JTree.DragOverData;

import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;

public class OWLSTreeCellRenderer extends DefaultTreeCellRenderer {
	private final static int CELL_WIDTH = 150;
	private int m_nDndEffect;
	private OWLSTreeNode m_node;

	public OWLSTreeCellRenderer() {
		super();
	}

	/*
	 * public Dimension getPreferredSize(){ // We set a very big size here, and
	 * then reduce it later, in the paint() method, // since we can't increase
	 * the size of the Graphics object given to paint(). Dimension size =
	 * super.getPreferredSize(); if (size.getWidth() < CELL_WIDTH) return new
	 * Dimension(CELL_WIDTH,15); return size; }
	 */

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof OWLSTreeNode
				&& ((OWLSTreeNode) value).getUserObject() instanceof DragOverData) {
			m_node = (OWLSTreeNode) value;
			m_nDndEffect = ((DragOverData) m_node.getUserObject())
					.getCurrentEffect();
		} else
			m_nDndEffect = DragOverData.DRAG_NONE;

		return super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, hasFocus);
	}

	/* Helper method to add the mini icon to the right of the Perform node */
	protected void paintIcon(Graphics graphics, Icon icon, Point position,
			Dimension size) {
		int y = (size.height - icon.getIconHeight()) / 2;
		icon.paintIcon(this, graphics, position.x, y);
		position.x += icon.getIconWidth();
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
		/*
		 * JLabel dummy = new JLabel(m_node.toString()); Dimension size =
		 * dummy.getPreferredSize(); System.out.println("Node label is " +
		 * dummy.getText() + ", preferrred size is " + size); Dimension newsize
		 * = new Dimension(); if (m_node instanceof PerformNode) // Performs
		 * need room for the mini-icons
		 * newsize.setSize(size.getWidth()+getLeafIcon().getIconWidth()+20,
		 * size.getHeight()); else
		 * newsize.setSize(size.getWidth()+getOpenIcon().getIconWidth()+5,
		 * size.getHeight()); setSize(new Dimension(newsize));
		 */
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

		// Now we add the mini-icon if this is a Perform node
		int iconPos = getWidth() - 15;

		if (m_node instanceof PerformNode) {
			/**
			 * This adds a small A, S or C next to the process name, for Atomic,
			 * Simple, or Composite Processes
			 */
			Instance inst = ((PerformNode) m_node).getProcess();
			if (inst != null) {
				Cls cls = inst.getDirectType();
				Vector supers = new Vector(cls.getSuperclasses());
				supers.add(cls);
				Iterator it = supers.iterator();
				while (it.hasNext()) {
					Cls type = (Cls) it.next();
					if (type.getName().equals("process:AtomicProcess")) {
						paintIcon(g, OWLSIcons.getAtomicProcessIcon(),
								new Point(iconPos, 0), new Dimension(15, 16));
						break;
					} else if (type.getName()
							.equals("process:CompositeProcess")) {
						paintIcon(g, OWLSIcons.getCompositeProcessIcon(),
								new Point(iconPos, 0), new Dimension(15, 16));
						break;

					} else if (type.getName().equals("process:SimpleProcess")) {
						paintIcon(g, OWLSIcons.getSimpleProcessIcon(),
								new Point(iconPos, 0), new Dimension(15, 16));
						break;
					}
				}
			}
		}
	}

}
