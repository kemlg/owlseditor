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

import java.awt.Font;
import java.util.Collection;

import javax.swing.JTextArea;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.widget.TextAreaWidget;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMetadata;

/**
 * Is there an OWL plugin widget we should use rather than the Protege-core
 * TextAreaWidget?
 */

public class ExprBodyWidget extends TextAreaWidget implements OWLWidgetMetadata {
	private JTextArea m_textArea = null;
	private OWLModel m_okb = null;

	public int getSuitability(RDFSNamedClass cls, RDFProperty property) {
		if (property.getName().equals("expr:expressionBody"))
			return OWLWidgetMetadata.DEFAULT + 1;
		else
			return OWLWidgetMetadata.NOT_SUITABLE;
	}

	// initialization
	public void initialize() {
		super.initialize(true, 6, 6);
		m_okb = (OWLModel) getKnowledgeBase(); // get knowledge base

		// create textArea
		m_textArea = getTextArea();
		m_textArea.setLineWrap(false);
		Font newFont = new Font("Monospaced", Font.PLAIN, 12);
		m_textArea.setFont(newFont);
	}

	// return the current value displayed by the widget
	// TODO: This causes an exception on getValues(), seems to be a protege bug.
	public Collection getValues() {
		String str = (String) m_textArea.getText();
		DefaultRDFSLiteral literal = (DefaultRDFSLiteral) m_okb
				.createRDFSLiteral(str, m_okb.getRDFXMLLiteralType());
		// System.out.println("ExpressionBodyWidget: Literal raw value is: " +
		// literal.getRawValue());
		return CollectionUtilities.createCollection(literal);
	}

	/*
	 * This can be useful for debugging If we get
	 * AbstractSlotWidget.setWidgetValues - ClassCastException, uncomment this.
	 * public void setWidgetValues() { Collection values; OWLIndividual
	 * expression = (OWLIndividual)getInstance(); OWLDatatypeProperty exprbody =
	 * (OWLDatatypeProperty)getSlot(); values = new
	 * ArrayList(expression.getPropertyValues(exprbody, true));
	 * setValues(values); }
	 */

	/* Stop the border from being painted red */
	protected void updateBorder(Collection values) {
		setNormalBorder();
		repaint();
	}

	// initialize the display value
	public void setValues(Collection c) {
		if (c != null && !c.isEmpty()) {
			RDFSLiteral l = (RDFSLiteral) CollectionUtilities.getFirstItem(c);
			String s = l.getString();
			if (s == null)
				s = "";

			int off;
			int ind1;
			int ind2;

			// skip over any leading "\n"

			for (off = 0; off < s.length(); off++)
				if (s.charAt(off) != '\n')
					break;
			s = s.substring(off, s.length());

			// establish offset for all rows

			off = findFirstNonBlank(s, 0);

			// loop over all text lines shifting them "off" chars to left

			if (off != -1) {
				s = shiftChars(s, 0, off);
				ind1 = s.indexOf('\n', 0);
				while (ind1 != -1) {
					ind2 = findFirstNonBlank(s, ind1 + 1);
					if (ind2 == -1)
						break;
					s = shiftChars(s, ind1 + 1, off);
					ind1 = s.indexOf('\n', ind2 - off);
				}
			}

			m_textArea.setText(s);
		}
	}

	// util to find offset of first non-blank char in a string
	private int findFirstNonBlank(String s, int offset) {
		for (int off = offset; off < s.length(); off++)
			if (s.charAt(off) != ' ')
				return off;

		return -1;
	}

	// util to shift chars in a string
	private String shiftChars(String s, int offset, int count) {
		int len = s.length();
		String s1 = s.substring(0, offset);
		String s2 = s.substring(offset + count, len);
		return s1 + s2;
	}

	// change whether or not the user can modify the displayed value
	public void setEditable(boolean editable) {
		m_textArea.setEnabled(true);
	}

	// indicate whether an instance of this class can handle the Class-Slot
	// binding.
	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		ValueType type = cls.getTemplateSlotValueType(slot);
		boolean allowsMultipleValues = cls
				.getTemplateSlotAllowsMultipleValues(slot);
		return type == ValueType.STRING && !allowsMultipleValues;
	}

	// method to allow easy debuging
	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}
}
