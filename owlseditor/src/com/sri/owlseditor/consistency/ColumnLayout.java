package com.sri.owlseditor.consistency;

import java.awt.*;
import java.util.*;

import javax.swing.*;

/**
 * Layout which places components in vertical column.
 */
public class ColumnLayout implements LayoutManager {
	public static final int LAYOUT_LEFT = 0;
	public static final int LAYOUT_RIGHT = 1;
	public static final int LAYOUT_CENTER = 2;
	public static final int LAYOUT_STRETCH = 3;

	protected int m_vGap = 5;
	protected int m_alignment = LAYOUT_LEFT;

	public ColumnLayout() {
	}

	public ColumnLayout(int vGap) {
		m_vGap = vGap;
	}

	public ColumnLayout(int vGap, int alignment) {
		m_vGap = vGap;
		m_alignment = alignment;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public Dimension preferredLayoutSize(Container parent) {
		int w = 0;
		int h = 0;

		for (int k = 0; k < parent.getComponentCount(); k++) {
			Component comp = parent.getComponent(k);
			Dimension d = comp.getPreferredSize();
			w = Math.max(w, d.width);
			h += d.height + m_vGap;
		}

		Insets insets = parent.getInsets();
		return new Dimension(w + insets.left + insets.right, h - m_vGap
				+ insets.top + insets.bottom);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int x = 0;
		int y = insets.top;
		Dimension dp = preferredLayoutSize(parent);

		for (int k = 0; k < parent.getComponentCount(); k++) {
			Component comp = parent.getComponent(k);
			Dimension d = comp.getPreferredSize();
			int wMax = parent.getWidth();

			switch (m_alignment) {
			case LAYOUT_LEFT:
				x = insets.left;
				break;
			case LAYOUT_CENTER:
				x = (wMax - d.width) / 2;
				break;
			case LAYOUT_RIGHT:
				x = wMax - d.width - insets.right;
				break;
			case LAYOUT_STRETCH:
				x = insets.left;
				d.width = wMax - insets.left - insets.right;
				break;
			}
			x = Math.max(x, 0);
			comp.setBounds(x, y,
					Math.min(d.width, wMax - insets.left - insets.right),
					d.height);
			y += d.height + m_vGap;
		}
	}

	public String toString() {
		return getClass().getName() + "[vgap=" + m_vGap + " alignment="
				+ m_alignment + "]";
	}
}
