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
package com.sri.owlseditor.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JComboBox;

import com.sri.owlseditor.util.OWLUtils;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.widget.AbstractPropertyWidget;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMetadata;

public class ExprLangWidget extends AbstractPropertyWidget implements
		OWLWidgetMetadata {
	private JComboBox comboBox;
	private final String sDRS = "DRS";
	private final String sKIF = "KIF";
	private final String sSWRL = "SWRL";
	private OWLModel m_okb = null;

	private OWLIndividual inst_DRS;
	private OWLIndividual inst_KIF;
	private OWLIndividual inst_SWRL;

	public int getSuitability(RDFSNamedClass cls, RDFProperty property) {
		if (property.getName().equals("expr:expressionLanguage"))
			return OWLWidgetMetadata.DEFAULT + 1;
		else
			return OWLWidgetMetadata.NOT_SUITABLE;
	}

	// initialization
	public void initialize() {
		// get knowledge base
		m_okb = (OWLModel) getKnowledgeBase();

		// inst_DRS = OWLUtils.getInstanceOfClass ("expr:DRS",
		// "expr:LogicLanguage", m_okb);
		// inst_KIF = OWLUtils.getInstanceOfClass ("expr:KIF",
		// "expr:LogicLanguage", m_okb);
		// inst_SWRL = OWLUtils.getOWLIndividualOfClass ("expr:SWRL",
		// "expr:LogicLanguage", m_okb);
		inst_DRS = m_okb.getOWLIndividual("expr:DRS");
		inst_KIF = m_okb.getOWLIndividual("expr:KIF");
		inst_SWRL = m_okb.getOWLIndividual("expr:SWRL");

		// create comboBox
		comboBox = ComponentFactory.createComboBox();
		comboBox.addItem(sDRS);
		comboBox.addItem(sKIF);
		comboBox.addItem(sSWRL);

		// action listener
		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				valueChanged();
			}
		};
		comboBox.addActionListener(lst);

		LabeledComponent c = new LabeledComponent(getLabel(), comboBox);
		add(c);
	}

	// return the current value displayed by the widget
	public Collection getValues() {
		OWLIndividual inst = (OWLIndividual) getEditedResource();
		OWLNamedClass cls = (OWLNamedClass) getCls();
		// System.out.println ("getValues Instance = " + inst.getName() +
		// "  cls = " + cls.getName());

		String s = (String) comboBox.getSelectedItem();
		if (s.equals(sDRS))
			inst = inst_DRS;
		else if (s.equals(sKIF))
			inst = inst_KIF;
		else if (s.equals(sSWRL))
			inst = inst_SWRL;
		else
			// should not get here
			inst = null;

		return CollectionUtilities.createCollection(inst);
	}

	// initialize the display value
	public void setWidgetValues() {
		OWLIndividual inst = (OWLIndividual) getEditedResource();
		OWLNamedClass cls = (OWLNamedClass) getCls();
		if (inst == null || cls == null)
			return;

		String clsName = cls.getName();
		String selItem = null;

		if (clsName.equals("expr:Expression")
				|| clsName.equals("expr:Condition")) {
			// SimpleInstance si = (SimpleInstance) OWLUtils.getNamedSlotValue
			// (inst, getSlot().getName());
			OWLIndividual si = (OWLIndividual) inst
					.getPropertyValue((OWLProperty) getRDFProperty());
			String siName = (si == null) ? "" : si.getName();

			if (siName.equals("expr:DRS"))
				selItem = sDRS;
			else if (siName.equals("expr:KIF"))
				selItem = sKIF;
			else {
				selItem = sSWRL;
			}

			comboBox.setEnabled(true);
		} else if (clsName.equals("expr:DRS-Condition")
				|| clsName.equals("expr:DRS-Expression")) {
			selItem = sDRS;
			comboBox.setEnabled(false);
		} else if (clsName.equals("expr:KIF-Condition")
				|| clsName.equals("expr:KIF-Expression")) {
			selItem = sKIF;
			comboBox.setEnabled(false);
		} else if (clsName.equals("expr:SWRL-Condition")
				|| clsName.equals("expr:SWRL-Expression")) {
			selItem = sSWRL;
			comboBox.setEnabled(false);
		}

		if (selItem != null) {
			comboBox.setSelectedItem(selItem);

			OWLIndividual value = null;

			if (selItem.equals(sDRS))
				value = inst_DRS;
			else if (selItem.equals(sKIF))
				value = inst_KIF;
			else if (selItem.equals(sSWRL))
				value = inst_SWRL;

			OWLUtils.setNamedSlotValue(inst, getSlot().getName(), value, m_okb);
		}
	}

	// change whether or not the user can modify the displayed value
	public void setEditable(boolean editable) {
		comboBox.setEnabled(true);
	}

	// indicate whether an instance of this class can handle the Class-Slot
	// binding.
	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		ValueType type = cls.getTemplateSlotValueType(slot);
		boolean allowsMultipleValues = cls
				.getTemplateSlotAllowsMultipleValues(slot);
		return type == ValueType.INSTANCE && !allowsMultipleValues;
	}

	// method to allow easy debuging
	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}
}
