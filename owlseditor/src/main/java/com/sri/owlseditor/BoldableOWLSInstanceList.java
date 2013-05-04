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
package com.sri.owlseditor;

import java.util.ArrayList;
import java.util.Collection;

import com.sri.owlseditor.util.OWLSInstanceList;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

/**
 * This class extends OWLSInstanceList with support for making a subset of the
 * instances in the display appear in boldface font. This is used to show
 * related instances between the four OWL-S instance displays. Subclasses must
 * implement the boldify() method.
 * 
 * @author Daniel Elenius
 */
public abstract class BoldableOWLSInstanceList extends OWLSInstanceList {
	// private Project project;
	// private SelectableList list;
	// protected OWLModel model;
	protected ArrayList boldItems = new ArrayList();

	protected OWLNamedClass service;
	protected OWLNamedClass profile;
	protected OWLNamedClass process;
	protected OWLNamedClass atomicProcess;
	protected OWLNamedClass compositeProcess;
	protected OWLNamedClass grounding;

	protected OWLObjectProperty describedBy;
	protected OWLObjectProperty presents;
	protected OWLObjectProperty supports;

	protected OWLObjectProperty presentedBy;
	protected OWLObjectProperty has_process;

	protected OWLObjectProperty describes;

	protected OWLObjectProperty supportedBy;
	protected OWLObjectProperty hasAtomicProcessGrounding;

	public BoldableOWLSInstanceList(Project project, String clsName) {
		super(project, clsName, false);
		// this.model = (OWLModel)project.getKnowledgeBase();
		SelectableList list = (SelectableList) getSelectable();
		list.setCellRenderer(new OWLSFrameRenderer(model, this));
		setupClassesAndProperties();
	}

	/**
	 * Used by the OWLSFrameRenderer to check which items should be painted in
	 * boldface font.
	 * 
	 * @return
	 */
	public Collection getBoldItems() {
		return boldItems;
	}

	/**
	 * This overrides the parent method with the same name, to ensure that
	 * subclasses of this class implement this method.
	 */
	public abstract void update(RDFIndividual individual);

	private void setupClassesAndProperties() {
		service = model.getOWLNamedClass("service:Service");
		profile = model.getOWLNamedClass("profile:Profile");
		process = model.getOWLNamedClass("process:Process");
		atomicProcess = model.getOWLNamedClass("process:AtomicProcess");
		compositeProcess = model.getOWLNamedClass("process:CompositeProcess");
		grounding = model.getOWLNamedClass("grounding:WsdlGrounding");

		describedBy = model.getOWLObjectProperty("service:describedBy");
		presents = model.getOWLObjectProperty("service:presents");
		supports = model.getOWLObjectProperty("service:supports");

		presentedBy = model.getOWLObjectProperty("service:presentedBy");
		has_process = model.getOWLObjectProperty("profile:has_process");

		describes = model.getOWLObjectProperty("service:describes");

		supportedBy = model.getOWLObjectProperty("service:supportedBy");

		hasAtomicProcessGrounding = model
				.getOWLObjectProperty("grounding:hasAtomicProcessGrounding");
	}

}
