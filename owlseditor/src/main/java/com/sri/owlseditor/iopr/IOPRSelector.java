// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License.  You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
// License for the specific language governing rights and limitations under
// the License.
//
// The Original Code is OWL-S Editor for Protege.
//
// The Initial Developer of the Original Code is SRI International.
// Portions created by the Initial Developer are Copyright (C) 2004 the
// Initial Developer.  All Rights Reserved.

package com.sri.owlseditor.iopr;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.sri.owlseditor.util.MultipleInstanceSelector;
import com.sri.owlseditor.util.OWLSInstanceList;
import com.sri.owlseditor.util.ResourceDisplayWrapper;
import com.sri.owlseditor.iopr.InputList;
import com.sri.owlseditor.iopr.OutputList;
import com.sri.owlseditor.iopr.PreconditionList;
import com.sri.owlseditor.iopr.ResultList;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/**
 * The left part of the IOPR Manager (including the checkboxes, the combo boxes,
 * and the bottom buttons). This is analogous to the ServiceSelector for the
 * main instance panes.
 */
public class IOPRSelector extends MultipleInstanceSelector implements
		ComboListener {

	private OWLModel model;

	private Collection ioprClasses;
	private Collection m_IOPRproperties;

	public IOPRSelector(OWLModel okb, Project project,
			ResourceDisplayWrapper editor) {
		super(okb, project, editor);
		model = okb;
		setLayout(new GridLayout(4, 1));

		setupClasses();
		setClasses(ioprClasses);
		setupProperties();
	}

	private void setupClasses() {
		OWLNamedClass input = model.getOWLNamedClass(InputList.INPUT);
		OWLNamedClass output = model.getOWLNamedClass(OutputList.OUTPUT);
		OWLNamedClass condition = model
				.getOWLNamedClass(PreconditionList.PRECONDITION);
		OWLNamedClass result = model.getOWLNamedClass(ResultList.RESULT);

		ArrayList classes = new ArrayList();
		classes.add(input);
		classes.add(output);
		classes.add(condition);
		classes.add(result);
		ioprClasses = classes;
	}

	private void setupProperties() {
		m_IOPRproperties = new ArrayList();
		m_IOPRproperties.add(InputList.INPUT_PROPERTY);
		m_IOPRproperties.add(OutputList.OUTPUT_PROPERTY);
		m_IOPRproperties.add(PreconditionList.PRECONDITION_PROPERTY);
		m_IOPRproperties.add(ResultList.RESULT_PROPERTY);
	}

	public void rightComboSelectionChanged(OWLIndividual inst) {
		Iterator it = instancePanes.iterator();
		while (it.hasNext()) {
			OWLSInstanceList list = (OWLSInstanceList) it.next();
			list.getCheckboxPanel().rightComboSelectionChanged(inst);
		}
	}

	public void leftComboSelectionChanged(OWLIndividual inst) {
		Iterator it = instancePanes.iterator();
		while (it.hasNext()) {
			OWLSInstanceList list = (OWLSInstanceList) it.next();
			list.getCheckboxPanel().leftComboSelectionChanged(inst);
		}
	}

	public void copyPropertyValue(OWLIndividual left, OWLIndividual right,
			boolean leftToRight) {
		Iterator it = instancePanes.iterator();
		while (it.hasNext()) {
			OWLSInstanceList list = (OWLSInstanceList) it.next();
			list.getCheckboxPanel().copyPropertyValue(left, right, leftToRight);
		}
	}

	public void refreshIOPRDisplay() {
		Iterator it = instancePanes.iterator();
		while (it.hasNext()) {
			OWLSInstanceList list = (OWLSInstanceList) it.next();
			list.getCheckboxPanel().refreshIOPRDisplay();
		}
	}

	public Collection getIOPRproperties() {
		return m_IOPRproperties;
	}
}
