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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JPanel;

import com.sri.owlseditor.widgets.AbstractCombinationWidget;

import com.sri.owlseditor.xslt.xml2owl.W2OXSLTDialog;
import com.sri.owlseditor.xslt.XSLTException;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
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

public class WSDLOutputWidget extends AbstractCombinationWidget implements
		OWLWidgetMetadata {

	// GUI components
	private WSDLXSLTButtonPanel wsdlXSLTButton;
	private WSDLStringPanel wsdlURIEditor;
	private WSDLStringPanel wsdlMsgEditor;

	public final String MSG_PART = "Message Part";
	public final String XSLT_STR = "XSLT String";
	public final String XSLT_URI = "XSLT URI";

	// KB
	private OWLModel m_okb;
	private OWLObjectProperty wsdlOutputSlot;
	private OWLDatatypeProperty wsdlMessagePartSlot;
	private OWLObjectProperty wsdlOwlSParameterSlot;
	private OWLDatatypeProperty xsltTransformationStringSlot;
	private OWLDatatypeProperty xsltTransformationURISlot;
	private OWLNamedClass wsdlOutputMessageMapCls;
	private OWLNamedClass processOutputCls;
	private OWLIndividual m_inst;
	private Object m_selItem;

	private AllowableAction _addAction;
	private AllowableAction _removeAction;
	private HashMap m_msgMap;
	private HashMap m_clsesMap;

	public int getSuitability(RDFSNamedClass cls, RDFProperty property) {
		if (property.getName().equals("grounding:wsdlOutput"))
			return DEFAULT + 1;
		return NOT_SUITABLE;
	}

	public void initialize() {
		super.initialize("WSDL Outputs", "Add", "Remove", "Wsdl Options",
				createClsesMap());
		m_msgMap = new HashMap();
		setupSlotsAndClasses();
		setPreferredRows(4);
		setEditorComponent(wsdlMsgEditor);
		wsdlURIEditor.setPanelSize(getEditorPanelSize());
		wsdlMsgEditor.setPanelSize(getEditorPanelSize());
	}

	private void setupSlotsAndClasses() {
		wsdlOutputMessageMapCls = m_okb
				.getOWLNamedClass("grounding:WsdlOutputMessageMap");
		processOutputCls = m_okb.getOWLNamedClass("process:Output");

		wsdlOutputSlot = (OWLObjectProperty) m_okb
				.getOWLProperty("grounding:wsdlOutput");
		wsdlMessagePartSlot = (OWLDatatypeProperty) m_okb
				.getOWLProperty("grounding:wsdlMessagePart");
		wsdlOwlSParameterSlot = (OWLObjectProperty) m_okb
				.getOWLProperty("grounding:owlsParameter");
		xsltTransformationStringSlot = (OWLDatatypeProperty) m_okb
				.getOWLProperty("grounding:xsltTransformationString");
		xsltTransformationURISlot = (OWLDatatypeProperty) m_okb
				.getOWLProperty("grounding:xsltTransformationURI");
	}

	private HashMap createClsesMap() {
		m_okb = (OWLModel) getKnowledgeBase();
		m_clsesMap = new HashMap();

		wsdlMsgEditor = new WSDLStringPanel();
		wsdlMsgEditor.getTextArea()
				.addFocusListener(new WSDLMsgFocusListener());

		wsdlXSLTButton = new WSDLXSLTButtonPanel();

		wsdlURIEditor = new WSDLStringPanel();
		wsdlURIEditor.getTextArea()
				.addFocusListener(new WSDLURIFocusListener());

		m_clsesMap.put(MSG_PART, wsdlMsgEditor);
		m_clsesMap.put(XSLT_STR, wsdlXSLTButton);
		m_clsesMap.put(XSLT_URI, wsdlURIEditor);

		ActionListener wsdlXSLTButtonListener = new ActionListener() {
			/*
			 * String xsltString = wsdlStrEditor.getTextArea().getText();
			 * DefaultRDFSLiteral l =
			 * (DefaultRDFSLiteral)m_okb.createRDFSLiteral(xsltString,
			 * m_okb.getRDFXMLLiteralType()); updateKB (owlInst, XSLT_STR, l);
			 */

			public void actionPerformed(ActionEvent evt) {
				OWLDatatypeProperty wsdlDocumentProp = (OWLDatatypeProperty) m_okb
						.getOWLProperty("grounding:wsdlDocument");
				RDFSLiteral wsdlDocument = (RDFSLiteral) m_inst
						.getPropertyValue(wsdlDocumentProp);

				OWLDatatypeProperty wsdlOutputMessageProp = (OWLDatatypeProperty) m_okb
						.getOWLProperty("grounding:wsdlOutputMessage");
				RDFSLiteral wsdlOutputMessage = (RDFSLiteral) m_inst
						.getPropertyValue(wsdlOutputMessageProp);

				OWLObjectProperty owlsProcessProp = (OWLObjectProperty) m_okb
						.getOWLProperty("grounding:owlsProcess");
				OWLIndividual atomicProcess = (OWLIndividual) getEditedResource()
						.getPropertyValue(owlsProcessProp);

				OWLIndividual outputMessageMap = (OWLIndividual) m_msgMap
						.get(m_selItem);
				try {
					W2OXSLTDialog dlg = new W2OXSLTDialog(m_okb, atomicProcess,
							wsdlDocument.toString(),
							wsdlOutputMessage.toString(), outputMessageMap);
					dlg.show();
					// SwingUtils.centerFrame (dlg);
				} catch (XSLTException e) {
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

		while (it.hasNext()) {
			RDFIndividual inst = (RDFIndividual) it.next();
			Object owlsProp = inst.getPropertyValue(wsdlOwlSParameterSlot);
			if (!m_msgMap.containsKey(owlsProp))
				m_msgMap.put(owlsProp, inst);
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
			if (strID.equals(MSG_PART)) {
				owlInst.setPropertyValue(wsdlMessagePartSlot, propVal);
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
			wsdlMsgEditor.getTextArea().setText("");
			wsdlMsgEditor.getTextArea().repaint();
		}
		/*
		 * if (propVal == null || strID.equals(MSG_PART) ||
		 * strID.equals(XSLT_URI)) { wsdlStrEditor.getTextArea().setText("");
		 * wsdlStrEditor.getTextArea().repaint(); }
		 */
		if (propVal == null || strID.equals(MSG_PART) || strID.equals(XSLT_STR)) {
			wsdlURIEditor.getTextArea().setText("");
			wsdlURIEditor.getTextArea().repaint();
		}
	}

	private void clearPropertyVals(OWLIndividual owlInst) {
		// clear all possible property vals for this instance

		RDFSLiteral lit = (RDFSLiteral) owlInst
				.getPropertyValue(wsdlMessagePartSlot);
		if (lit != null)
			owlInst.removePropertyValue(wsdlMessagePartSlot, lit);

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
		RDFSLiteral msgPart = (RDFSLiteral) owlInst
				.getPropertyValue(wsdlMessagePartSlot);
		if (msgPart != null)
			return MSG_PART;

		RDFSLiteral xsltStr = (RDFSLiteral) owlInst
				.getPropertyValue(xsltTransformationStringSlot);
		if (xsltStr != null)
			return XSLT_STR;

		RDFSLiteral xsltURI = (RDFSLiteral) owlInst
				.getPropertyValue(xsltTransformationURISlot);
		if (xsltURI != null)
			return XSLT_URI;

		return null;
	}

	// Called when the add button is clicked on the LabeledComponent

	public Object addListItem(Component parent) {
		Collection c = CollectionUtilities.createCollection(processOutputCls);
		RDFIndividual inst = (RDFIndividual) DisplayUtilities.pickInstance(
				(Component) WSDLOutputWidget.this, c);
		if (m_msgMap.containsKey(inst))
			return null;

		OWLIndividual wsdlInst = (OWLIndividual) wsdlOutputMessageMapCls
				.createInstance(null);
		wsdlInst.setPropertyValue(wsdlOwlSParameterSlot, inst);
		m_msgMap.put(inst, wsdlInst);
		updateKB(wsdlInst, MSG_PART, null);
		repaint();
		valueChanged();
		return inst;
	}

	/* Called when the remove button is clicked on the LabeledComponent */
	public boolean removeListItem(Object listItem) {
		OWLIndividual owlInst = (OWLIndividual) m_msgMap.get(listItem);
		// updateKB (owlInst, MSG_PART, null);
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
			strID = MSG_PART;

		setComboSelection(strID);
		setEditorComponent((JPanel) m_clsesMap.get(strID));

		if (strID.equals(MSG_PART)) {
			RDFSLiteral l = (RDFSLiteral) owlInst
					.getPropertyValue(wsdlMessagePartSlot);
			if (l != null)
				wsdlMsgEditor.getTextArea().setText(l.getString());
		} else if (strID.equals(XSLT_STR)) {
			/*
			 * RDFSLiteral l = (RDFSLiteral)owlInst.getPropertyValue
			 * (xsltTransformationStringSlot); if (l != null)
			 * wsdlStrEditor.getTextArea().setText (l.getString());
			 */
		} else if (strID.equals(XSLT_URI)) {
			RDFSLiteral l = (RDFSLiteral) owlInst
					.getPropertyValue(xsltTransformationURISlot);
			if (l != null)
				wsdlURIEditor.getTextArea().setText(l.getString());
		}
	}

	private class WSDLMsgFocusListener extends FocusAdapter {
		Object selectedListValue;

		public void focusGained(FocusEvent event) {
			selectedListValue = getSelectedListValue();
		}

		public void focusLost(FocusEvent event) {
			OWLIndividual owlInst = (OWLIndividual) m_msgMap
					.get(selectedListValue);
			String mpartString = wsdlMsgEditor.getTextArea().getText();
			DefaultRDFSLiteral l = (DefaultRDFSLiteral) m_okb
					.createRDFSLiteral(mpartString,
							m_okb.getRDFSDatatypeByName("xsd:anyURI"));
			updateKB(owlInst, MSG_PART, l);
		}
	}

	private class WSDLURIFocusListener extends FocusAdapter {
		Object selectedListValue;

		public void focusGained(FocusEvent event) {
			selectedListValue = getSelectedListValue();
		}

		public void focusLost(FocusEvent event) {
			OWLIndividual owlInst = (OWLIndividual) m_msgMap
					.get(selectedListValue);
			String xslturiString = wsdlMsgEditor.getTextArea().getText();
			DefaultRDFSLiteral l = (DefaultRDFSLiteral) m_okb
					.createRDFSLiteral(xslturiString,
							m_okb.getRDFSDatatypeByName("xsd:anyURI"));
			updateKB(owlInst, XSLT_URI, l);
		}
	}
}
