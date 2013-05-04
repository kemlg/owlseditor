//The contents of this file are subject to the Mozilla Public License
//Version 1.1 (the "License"); you may not use this file except in
//compliance with the License.  You may obtain a copy of the License at
//http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS"
//basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
//License for the specific language governing rights and limitations under
//the License.
//
//The Original Code is OWL-S Editor for Protege.
//
//The Initial Developer of the Original Code is SRI International.
//Portions created by the Initial Developer are Copyright (C) 2004 the
//Initial Developer.  All Rights Reserved.
package com.sri.owlseditor.iopr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.event.*;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.*;
import java.awt.*;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;

/**
 * A panel with two rows of checkboxes. Used by the OWLSDirectInstancesList
 * class.
 * 
 * This class maintains a mapping from the instances in the
 * OWLSDirectInstancesList and the corresponding checkboxes. Adding/deleting
 * instances add/deletes a pair of checkboxes and updates the mapping.
 * 
 * The action handlers for the checkboxes, modifying the KB, are taken care of
 * by this class itself.
 * 
 * * @author Daniel Elenius
 */
public class ListAndCheckboxPanel extends JPanel implements ComboListener {
	private OWLModel model;
	private OWLIndividual left = null;
	private OWLIndividual right = null;
	private HashMap checkBoxLines = new HashMap();
	private Box checkPanel = Box.createVerticalBox();
	private JList jcb1 = new JList(new DefaultListModel());
	private JList jcb2 = new JList(new DefaultListModel());
	DefaultListModel dlm1;
	DefaultListModel dlm2;
	private SelectableList list;
	private String m_propertyName;
	private OWLNamedClass m_profile;
	private OWLNamedClass m_process;
	private boolean m_cbListenersEnabled = true;

	private final static int LEFT = 0;
	private final static int RIGHT = 1;

	public ListAndCheckboxPanel(SelectableList list, OWLModel model) {
		super();
		this.list = list;
		this.model = model;

		setLayout(new BorderLayout());
		add(list, BorderLayout.CENTER);
		JPanel jp = new JPanel(new GridLayout(1, 2));
		jp.add(jcb1);
		jp.add(jcb2);
		add(jp, BorderLayout.WEST);

		jcb1.setCellRenderer(new CheckBoxRenderer());
		jcb2.setCellRenderer(new CheckBoxRenderer());
		jcb1.setFixedCellHeight(list.getFixedCellHeight());
		jcb2.setFixedCellHeight(list.getFixedCellHeight());
		jcb1.setFixedCellWidth(20);
		jcb2.setFixedCellWidth(20);

		jcb1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				int selectedIndex = jcb1.locationToIndex(me.getPoint());
				if (selectedIndex < 0)
					return;
				JCheckBox cb = (JCheckBox) jcb1.getModel().getElementAt(
						selectedIndex);
				cb.setSelected(!cb.isSelected());
				jcb1.repaint();
			}
		});

		jcb2.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				int selectedIndex = jcb2.locationToIndex(me.getPoint());
				if (selectedIndex < 0)
					return;
				JCheckBox cb = (JCheckBox) jcb2.getModel().getElementAt(
						selectedIndex);
				cb.setSelected(!cb.isSelected());
				jcb2.repaint();
			}
		});

		dlm1 = (DefaultListModel) jcb1.getModel();
		dlm2 = (DefaultListModel) jcb2.getModel();
		jp.setBackground(Color.WHITE);
	}

	/** Should only be called once, does not remove old contents of the box */
	public void setClassAndProperty(OWLNamedClass cls, String property) {
		m_profile = model.getOWLNamedClass("profile:Profile");
		m_process = model.getOWLNamedClass("process:Process");

		m_propertyName = property;

		Iterator it = cls.getInstances(true).iterator();
		while (it.hasNext()) {
			makeCheckBoxLine((RDFIndividual) it.next());
		}
		/*
		 * SimpleListModel slm = (SimpleListModel) list.getModel(); for (int i =
		 * 0; i < slm.getSize(); i++) { ((JCheckBox)
		 * dlm1.getElementAt(i)).setEnabled (false); ((JCheckBox)
		 * dlm2.getElementAt(i)).setEnabled (false); }
		 */
	}

	public void makeCheckBoxLine(RDFIndividual inst) {
		JCheckBox checkBox1 = ComponentFactory.createCheckBox();
		JCheckBox checkBox2 = ComponentFactory.createCheckBox();

		class CheckboxListener implements ItemListener {
			private RDFIndividual inst;
			private int lr;

			public CheckboxListener(RDFIndividual inst, int lr) {
				this.inst = inst;
				this.lr = lr;
			}

			public void itemStateChanged(ItemEvent e) {
				if (!m_cbListenersEnabled)
					return;

				JCheckBox jcb = (JCheckBox) e.getItem();

				if (lr == LEFT) {
					addRemovePropertyValue(left, jcb, dlm1, dlm2);
				} else {
					addRemovePropertyValue(right, jcb, dlm2, dlm1);
				}
			}
		}

		ItemListener leftlistener = new CheckboxListener(inst, LEFT);
		ItemListener rightlistener = new CheckboxListener(inst, RIGHT);

		checkBox1.addItemListener(leftlistener);
		checkBox2.addItemListener(rightlistener);

		checkBox1.setEnabled(left != null);
		checkBox2.setEnabled(right != null);

		dlm1.addElement(checkBox1);
		dlm2.addElement(checkBox2);
	}

	private void addRemovePropertyValue(OWLIndividual owlInst, JCheckBox jcb,
			DefaultListModel dlm, DefaultListModel dlmx) {
		String propertyName = getPropertyName((OWLIndividual) owlInst);
		if (propertyName == null)
			return;

		RDFProperty rdfp = (RDFProperty) model.getOWLProperty(propertyName);
		SimpleListModel slm = (SimpleListModel) list.getModel();

		int indx = dlm.indexOf(jcb);
		if (indx == -1)
			return;

		RDFIndividual inst = (RDFIndividual) slm.getElementAt(indx);

		if (owlInst != null && inst != null) {
			if (jcb.isSelected())
				owlInst.addPropertyValue(rdfp, inst);
			else
				owlInst.removePropertyValue(rdfp, inst);
		}

		if (left == right) {
			boolean currentState = m_cbListenersEnabled;
			m_cbListenersEnabled = false;
			((JCheckBox) dlmx.getElementAt(indx)).setSelected(jcb.isSelected());
			m_cbListenersEnabled = currentState;
			jcb1.repaint();
			jcb2.repaint();
		}
	}

	public void copyPropertyValue(OWLIndividual leftInst,
			OWLIndividual rightInst, boolean leftToRight) {
		/*
		 * String leftPropertyName = getPropertyName ((OWLIndividual) leftInst);
		 * if (leftPropertyName == null) return;
		 * 
		 * String rightPropertyName = getPropertyName ((OWLIndividual)
		 * rightInst); if (rightPropertyName == null) return;
		 */
		// RDFProperty rdfp = (RDFProperty) model.getOWLProperty
		// (leftPropertyName);
		SimpleListModel slm = (SimpleListModel) list.getModel();
		for (int i = 0; i < slm.getSize(); i++) {
			if (leftToRight) {
				boolean isSel = ((JCheckBox) dlm1.getElementAt(i)).isSelected();
				((JCheckBox) dlm2.getElementAt(i)).setSelected(isSel);
			} else {
				boolean isSel = ((JCheckBox) dlm2.getElementAt(i)).isSelected();
				((JCheckBox) dlm1.getElementAt(i)).setSelected(isSel);
			}
		}

		if (leftToRight)
			jcb2.repaint();
		else
			jcb1.repaint();
	}

	public void deleteCheckBoxLine(RDFIndividual inst) {
		int indx = list.getSelectedIndex();
		if (indx != -1) {
			dlm1.remove(indx);
			dlm2.remove(indx);
		}
	}

	public void leftComboSelectionChanged(OWLIndividual inst) {
		left = inst;

		m_cbListenersEnabled = false;
		processCheckBoxState(inst, dlm1);
		m_cbListenersEnabled = true;

		jcb1.repaint();
	}

	public void rightComboSelectionChanged(OWLIndividual inst) {
		right = inst;

		m_cbListenersEnabled = false;
		processCheckBoxState(inst, dlm2);
		m_cbListenersEnabled = true;

		jcb2.repaint();
	}

	public void refreshIOPRDisplay() {
		m_cbListenersEnabled = false;
		processCheckBoxState(left, dlm1);
		processCheckBoxState(right, dlm2);
		m_cbListenersEnabled = true;

		jcb1.repaint();
		jcb2.repaint();
	}

	private void processCheckBoxState(OWLIndividual inst, DefaultListModel dlm) {
		// clear check boxes
		SimpleListModel slm = (SimpleListModel) list.getModel();
		if (slm.getSize() == 0)
			return;

		// uncheck all checkboxes
		for (int i = 0; i < slm.getSize(); i++)
			((JCheckBox) dlm.getElementAt(i)).setSelected(false);

		// disable all checkboxes if null property item in combobox
		String propertyName = getPropertyName(inst);
		if (propertyName == null) {
			for (int i = 0; i < slm.getSize(); i++)
				((JCheckBox) dlm.getElementAt(i)).setEnabled(false);

			return;
		}

		if (!((JCheckBox) dlm.getElementAt(0)).isEnabled()) {
			for (int i = 0; i < slm.getSize(); i++)
				((JCheckBox) dlm.getElementAt(i)).setEnabled(true);
		}

		Collection c = inst.getPropertyValues(
				model.getOWLProperty(propertyName), true);

		Iterator it = c.iterator();
		while (it.hasNext()) {
			RDFIndividual inst2 = (RDFIndividual) it.next();
			updateCheckboxState(inst2, dlm);
		}
	}

	public String getPropertyName(OWLIndividual inst) {
		if (inst == null)
			return null;

		String propertyName = null;

		if (inst.hasRDFType(m_process, true))
			propertyName = "process" + m_propertyName;
		else if (inst.hasRDFType(m_profile, true))
			propertyName = "profile" + m_propertyName;

		return propertyName;
	}

	/*
	 * private int getCBIndx (JCheckBox jcb, DefaultListModel dlm) { for (int i
	 * = 0; i < dlm.getSize(); i++) { JCheckBox jcbx = (JCheckBox)
	 * dlm.getElementAt(i); if (jcbx == jcb) return i; }
	 * 
	 * return -1; }
	 */

	private void updateCheckboxState(RDFIndividual inst, DefaultListModel dlm) {
		SimpleListModel slm = (SimpleListModel) list.getModel();

		for (int i = 0; i < slm.getSize(); i++) {
			if (inst.getName().equals(
					((RDFIndividual) slm.getElementAt(i)).getName()))
				((JCheckBox) dlm.getElementAt(i)).setSelected(true);
		}
	}

	class CheckBoxRenderer extends JCheckBox implements ListCellRenderer {
		public CheckBoxRenderer() {
			setBackground(Color.WHITE);
		}

		public Component getListCellRendererComponent(JList list, Object obj,
				int indx, boolean isChecked, boolean hasFocus) {
			if (((JCheckBox) obj).isEnabled())
				setSelected(((JCheckBox) obj).isSelected());
			else
				setSelected(false);
			return this;
		}
	}
}
