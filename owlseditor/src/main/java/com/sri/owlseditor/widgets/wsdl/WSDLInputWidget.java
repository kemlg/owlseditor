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
package com.sri.owlseditor.widgets.wsdl;

import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.sri.owlseditor.widgets.AbstractCombinationWidget;
import com.sri.owlseditor.xslt.XSLTException;
import com.sri.owlseditor.xslt.owl2xml.O2WXSLTDialog;

import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.AddAction;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.RemoveAction;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMetadata;

public class WSDLInputWidget extends AbstractCombinationWidget implements
		OWLWidgetMetadata {

	// GUI components
	private WSDLXSLTButtonPanel wsdlXSLTButton;
	private WSDLStringPanel wsdlStringEditor;
	private WSDLOwlSParamPanel wsdlOwlSParamEditor;

	public final String OWLS_PARAM = "OWL-S Parameter";
	public final String XSLT_STR = "XSLT String";
	public final String XSLT_URI = "XSLT URI";

	// KB
	private OWLModel m_okb;
	private OWLObjectProperty wsdlInputSlot;
	private OWLDatatypeProperty wsdlMessagePartSlot;
	private OWLObjectProperty wsdlOwlSParameterSlot;
	private OWLDatatypeProperty xsltTransformationStringSlot;
	private OWLDatatypeProperty xsltTransformationURISlot;
	private OWLNamedClass wsdlInputMessageMapCls;
	private OWLNamedClass processInputCls;
	private OWLIndividual m_inst;
	private Object m_selItem;

	private AllowableAction _addAction;
	private AllowableAction _removeAction;
	private HashMap m_msgMap;
	private HashMap m_clsesMap;

	public int getSuitability(RDFSNamedClass cls, RDFProperty property) {
		if (property.getName().equals("grounding:wsdlInput"))
			return DEFAULT + 1;
		return NOT_SUITABLE;
	}

	public void initialize() {
		super.initialize("WSDL Inputs", "Add", "Remove", "Wsdl Options",
				createClsesMap());
		m_msgMap = new HashMap();
		setupSlotsAndClasses();
		setPreferredRows(4);
		setEditorComponent(wsdlOwlSParamEditor);
		// wsdlStringButton.setPanelSize (getEditorPanelSize());
		wsdlStringEditor.setPanelSize(getEditorPanelSize());
		wsdlOwlSParamEditor.setPanelSize(getEditorPanelSize());
	}

	private void setupSlotsAndClasses() {
		wsdlInputMessageMapCls = m_okb
				.getOWLNamedClass("grounding:WsdlInputMessageMap");
		processInputCls = m_okb.getOWLNamedClass("process:Input");

		wsdlInputSlot = (OWLObjectProperty) m_okb
				.getOWLProperty("grounding:wsdlInput");
		wsdlMessagePartSlot = (OWLDatatypeProperty) m_okb
				.getOWLProperty("grounding:wsdlMessagePart");
		wsdlOwlSParameterSlot = (OWLObjectProperty) m_okb
				.getOWLProperty("grounding:owlsParameter");
		xsltTransformationStringSlot = (OWLDatatypeProperty) m_okb
				.getOWLProperty("grounding:xsltTransformationString");
		xsltTransformationURISlot = (OWLDatatypeProperty) m_okb
				.getOWLProperty("grounding:xsltTransformationURI");
	}

	private void showWSDLErrorDialog(String message) {
		JOptionPane.showMessageDialog(this, message, "WSDL Error!",
				JOptionPane.ERROR_MESSAGE);
	}

	private HashMap createClsesMap() {
		m_okb = (OWLModel) getKnowledgeBase();
		m_clsesMap = new HashMap();

		wsdlXSLTButton = new WSDLXSLTButtonPanel();
		// wsdlXSLTButton.getXSLTButton().addActionListener
		// (wsdlXSLTButtonListener);
		wsdlStringEditor = new WSDLStringPanel();
		wsdlStringEditor.getTextArea().addFocusListener(
				new WSDLURIFocusListener());
		wsdlOwlSParamEditor = new WSDLOwlSParamPanel();

		wsdlOwlSParamEditor.getOwlSParamWidget().addAddHeaderButton(
				getAddAction());
		wsdlOwlSParamEditor.getOwlSParamWidget().addRemoveHeaderButton(
				getRemoveAction());

		m_clsesMap.put(OWLS_PARAM, wsdlOwlSParamEditor);
		m_clsesMap.put(XSLT_STR, wsdlXSLTButton);
		m_clsesMap.put(XSLT_URI, wsdlStringEditor);

		ActionListener wsdlXSLTButtonListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				OWLDatatypeProperty wsdlDocumentProp = (OWLDatatypeProperty) m_okb
						.getOWLProperty("grounding:wsdlDocument");
				RDFSLiteral wsdlDocument = (RDFSLiteral) m_inst
						.getPropertyValue(wsdlDocumentProp);

				OWLDatatypeProperty wsdlInputMessageProp = (OWLDatatypeProperty) m_okb
						.getOWLProperty("grounding:wsdlInputMessage");
				RDFSLiteral wsdlInputMessage = (RDFSLiteral) m_inst
						.getPropertyValue(wsdlInputMessageProp);

				OWLObjectProperty owlsProcessProp = (OWLObjectProperty) m_okb
						.getOWLProperty("grounding:owlsProcess");
				OWLIndividual atomicProcess = (OWLIndividual) getEditedResource()
						.getPropertyValue(owlsProcessProp);

				/*
				 * OWLDatatypeProperty wsdlMessagePartProp =
				 * (OWLDatatypeProperty) m_okb.getOWLProperty
				 * ("grounding:wsdlMessagePart"); RDFSLiteral wsdlMessagePart =
				 * (RDFSLiteral) m_inst.getPropertyValue (wsdlMessagePartProp);
				 */

				OWLIndividual inputMessageMap = (OWLIndividual) m_msgMap
						.get(m_selItem);
				String wsdlDocumentString = null;
				if (wsdlDocument != null)
					wsdlDocumentString = wsdlDocument.getString();
				String wsdlInputMessageString = null;
				if (wsdlInputMessage != null)
					wsdlInputMessageString = wsdlInputMessage.getString();
				O2WXSLTDialog dlg = null;
				try {
					dlg = new O2WXSLTDialog(m_okb, atomicProcess,
							wsdlDocumentString, wsdlInputMessageString,
							inputMessageMap);
					dlg.show();
				} catch (XSLTException e) {
					showWSDLErrorDialog(e.getMessage());
				}
			}
		};
		wsdlXSLTButton.getXSLTButton()
				.addActionListener(wsdlXSLTButtonListener);
		wsdlXSLTButton.getXSLTButton().setForeground(Color.blue);

		return m_clsesMap;
	}

	/*
	 * This can be useful for debugging If we get
	 * AbstractSlotWidget.setWidgetValues - ClassCastException, uncomment this.
	 * public void setWidgetValues() { Collection values; OWLIndividual
	 * grounding = (OWLIndividual)getInstance(); OWLObjectProperty map =
	 * (OWLObjectProperty)getSlot(); values = new
	 * ArrayList(grounding.getPropertyValues(map, true)); setValues(values); }
	 */

	public void setValues(Collection values) {
		m_msgMap.clear();
		Iterator it = values.iterator();
		OWLIndividual inst = null;

		while (it.hasNext()) {
			inst = (OWLIndividual) it.next();
			RDFSLiteral l = (RDFSLiteral) inst
					.getPropertyValue(wsdlMessagePartSlot);
			if (!m_msgMap.containsKey(l))
				m_msgMap.put(l, inst);
		}

		setListValues(m_msgMap.keySet());

		// If the msgMap list is non-empty we select the first one
		it = m_msgMap.entrySet().iterator();
		if (it.hasNext())
			setSelectedListIndex(0);

		m_inst = (OWLIndividual) getEditedResource();
		repaint();
	}

	public Collection getValues() {
		if (m_msgMap == null || m_msgMap.values().size() == 0)
			return Collections.EMPTY_LIST;

		Collection coll = new ArrayList(1);

		Iterator it = m_msgMap.values().iterator();

		while (it.hasNext()) {
			OWLIndividual owlInst = (OWLIndividual) it.next();
			coll.add(owlInst);
		}

		return coll;
	}

	private void updateKB(OWLIndividual owlInst, String strID, Object propVal) {
		clearPropertyVals(owlInst);

		// set new property value
		if (propVal != null) {
			if (strID.equals(OWLS_PARAM)) {
				owlInst.setPropertyValue(wsdlOwlSParameterSlot, propVal);
				wsdlOwlSParamEditor.getOwlSParamWidget().enableRemoveButton(
						true);
			}
			/*
			 * else if (strID.equals (XSLT_STR)) { owlInst.setPropertyValue
			 * (xsltTransformationStringSlot, propVal); }
			 */
			else if (strID.equals(XSLT_URI)) {
				owlInst.setPropertyValue(xsltTransformationURISlot, propVal);
			}
		}

		if (propVal == null || strID.equals(XSLT_STR) || strID.equals(XSLT_URI)) {
			JList jl = wsdlOwlSParamEditor.getOwlSParamList();
			Collection ci = CollectionUtilities.createCollection("");
			ComponentUtilities.setListValues(jl, ci);
			wsdlOwlSParamEditor.getOwlSParamWidget().enableRemoveButton(false);
			jl.repaint();
		}
		/*
		 * if (propVal == null || strID.equals(OWLS_PARAM) ||
		 * strID.equals(XSLT_URI)) { wsdlTextEditor.getTextArea().setText("");
		 * wsdlTextEditor.getTextArea().repaint(); }
		 */
		if (propVal == null || strID.equals(OWLS_PARAM)
				|| strID.equals(XSLT_STR)) {
			wsdlStringEditor.getTextArea().setText("");
			wsdlStringEditor.getTextArea().repaint();
		}
	}

	private void clearPropertyVals(OWLIndividual owlInst) {
		// clear all possible property vals for this instance

		RDFIndividual owlSParam = (RDFIndividual) owlInst
				.getPropertyValue(wsdlOwlSParameterSlot);
		if (owlSParam != null)
			owlInst.removePropertyValue(wsdlOwlSParameterSlot, owlSParam);

		String xsltStr = (String) owlInst
				.getPropertyValue(xsltTransformationStringSlot);
		if (xsltStr != null)
			owlInst.removePropertyValue(xsltTransformationStringSlot, xsltStr);

		String xsltURI = (String) owlInst
				.getPropertyValue(xsltTransformationURISlot);
		if (xsltURI != null)
			owlInst.removePropertyValue(xsltTransformationURISlot, xsltURI);
	}

	private String getPropertyID(OWLIndividual owlInst) {
		RDFIndividual owlSParam = (RDFIndividual) owlInst
				.getPropertyValue(wsdlOwlSParameterSlot);
		if (owlSParam != null)
			return OWLS_PARAM;

		String xsltStr = (String) owlInst
				.getPropertyValue(xsltTransformationStringSlot);
		if (xsltStr != null)
			return XSLT_STR;

		String xsltURI = (String) owlInst
				.getPropertyValue(xsltTransformationURISlot);
		if (xsltURI != null)
			return XSLT_URI;

		return null;
	}

	// Called when the add button is clicked on the LabeledComponent

	public Object addListItem(Component parent) {
		String s = DisplayUtilities.editString(parent, "Add URI String", null,
				null);
		DefaultRDFSLiteral l = (DefaultRDFSLiteral) m_okb.createRDFSLiteral(s,
				m_okb.getRDFSDatatypeByName("xsd:anyURI"));

		if (m_msgMap.containsKey(l))
			return null;

		OWLIndividual wsdlInst = (OWLIndividual) wsdlInputMessageMapCls
				.createInstance(null);
		wsdlInst.setPropertyValue(wsdlMessagePartSlot, l);
		m_msgMap.put(l, wsdlInst);
		updateKB(wsdlInst, OWLS_PARAM, null);
		repaint();
		valueChanged();
		return l;
	}

	/* Called when the remove button is clicked on the LabeledComponent */
	public boolean removeListItem(Object listItem) {
		OWLIndividual owlInst = (OWLIndividual) m_msgMap.get(listItem);
		// updateKB (owlInst, OWLS_PARAM, null);
		owlInst.delete();
		m_msgMap.remove(listItem);
		valueChanged();
		return true;
	}

	public void comboSelectionChanged(Object comboSelectedItem) {
		OWLIndividual owlInst = (OWLIndividual) m_msgMap
				.get(getSelectedListValue());
		updateKB(owlInst, (String) comboSelectedItem, null);
	}

	public void listSelectionChanged(Object selectedItem) {
		m_selItem = selectedItem;
		OWLIndividual owlInst = (OWLIndividual) m_msgMap.get(selectedItem);
		String strID = getPropertyID(owlInst);
		if (strID == null)
			strID = OWLS_PARAM;

		setComboSelection(strID);
		setEditorComponent((JPanel) m_clsesMap.get(strID));

		if (strID.equals(OWLS_PARAM)) {
			JList jl = wsdlOwlSParamEditor.getOwlSParamList();
			Collection ci = CollectionUtilities.createCollection(owlInst
					.getPropertyValue(wsdlOwlSParameterSlot));
			ComponentUtilities.setListValues(jl, ci);
			wsdlOwlSParamEditor.getOwlSParamWidget().enableRemoveButton(
					ci != null);
		} else if (strID.equals(XSLT_STR)) {
			// wsdlTextEditor.getTextArea().setText ((String)
			// owlInst.getPropertyValue (xsltTransformationStringSlot));
		} else if (strID.equals(XSLT_URI)) {
			wsdlStringEditor.getTextArea().setText(
					(String) owlInst
							.getPropertyValue(xsltTransformationURISlot));
		}
	}

	private Action getAddAction() {
		_addAction = new AddAction(ResourceKey.VALUE_ADD) {
			public void onAdd() {
				handleAddAction();
			}
		};
		return _addAction;
	}

	protected void handleAddAction() {
		Collection c = CollectionUtilities.createCollection(processInputCls);
		RDFIndividual inst = (RDFIndividual) DisplayUtilities.pickInstance(
				(Component) WSDLInputWidget.this, c);
		if (inst != null) {
			JList jl = wsdlOwlSParamEditor.getOwlSParamList();
			Collection ci = CollectionUtilities.createCollection(inst);
			ComponentUtilities.setListValues(jl, ci);
			OWLIndividual owlInst = (OWLIndividual) m_msgMap
					.get(getSelectedListValue());
			updateKB(owlInst, OWLS_PARAM, inst);
		}
	}

	private Action getRemoveAction() {
		_removeAction = new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
			public void onRemove(Collection c) {
				handleRemoveAction(c);
			}
		};
		return _removeAction;
	}

	protected void handleRemoveAction(Collection col) {
		JList jl = wsdlOwlSParamEditor.getOwlSParamList();
		Collection c = CollectionUtilities.createCollection(null);
		ComponentUtilities.setListValues(jl, c);
		OWLIndividual owlInst = (OWLIndividual) m_msgMap
				.get(getSelectedListValue());
		updateKB(owlInst, OWLS_PARAM, null);
	}

	/*
	 * private class WSDLXSLTButtonListener extends FocusAdapter { Object
	 * selectedListValue;
	 * 
	 * public void focusGained (FocusEvent event) { selectedListValue =
	 * getSelectedListValue(); }
	 * 
	 * public void focusLost (FocusEvent event) { OWLIndividual owlInst =
	 * (OWLIndividual) m_msgMap.get (selectedListValue); updateKB (owlInst,
	 * XSLT_STR, wsdlTextEditor.getTextArea().getText()); } }
	 */

	private class WSDLURIFocusListener extends FocusAdapter {
		Object selectedListValue;

		public void focusGained(FocusEvent event) {
			selectedListValue = getSelectedListValue();
		}

		public void focusLost(FocusEvent event) {
			OWLIndividual owlInst = (OWLIndividual) m_msgMap
					.get(selectedListValue);
			updateKB(owlInst, XSLT_URI, wsdlStringEditor.getTextArea()
					.getText());
		}
	}
}
