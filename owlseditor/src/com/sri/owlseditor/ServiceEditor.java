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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.sri.owlseditor.cmp.CompositeProcessPane;
import com.sri.owlseditor.util.OWLSResourceDisplay;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.ResourceDisplay;

/**
 * This class is the right portion of the OWL-S tab. It changes contents
 * depending on the selections made in the ServiceSelector on the left. The
 * ServiceEditor is where most of the real work happens.
 */
public class ServiceEditor extends JPanel implements OWLSResourceDisplay {

	JComponent comp;
	ResourceDisplay properties;
	CompositeProcessPane comppane;
	OWLModel model;
	int currentMode;
	static final int SERVICE = 1;
	static final int PROFILE = 2;
	static final int PROCESS = 3;
	static final int COMPOSITE = 4;
	static final int GROUNDING = 5;

	protected OWLNamedClass service;
	protected OWLNamedClass profile;
	protected OWLNamedClass compprocess;
	protected OWLNamedClass process;
	protected OWLNamedClass grounding;

	public ServiceEditor(Project project) {
		model = (OWLModel) project.getKnowledgeBase();
		setLayout(new BorderLayout());
		properties = new ResourceDisplay(project);
		comppane = new CompositeProcessPane(project);

		comp = properties;
		add(comp, BorderLayout.CENTER);

		setupClasses();

		// we start with an empty properties display in SERVICE mode
		setServiceMode(null);
	}

	/* Empty the editor pane */
	public void clearSelection() {
		// properties.setInstance(null);
		properties.clearSelection();
		// splitpane.setTopComponent(null);
	}

	public int getMode() {
		return currentMode;
	}

	private void setupClasses() {
		service = model.getOWLNamedClass("service:Service");
		profile = model.getOWLNamedClass("profile:Profile");
		compprocess = model.getOWLNamedClass("process:CompositeProcess");
		process = model.getOWLNamedClass("process:Process");
		grounding = model.getOWLNamedClass("grounding:WsdlGrounding");
	}

	public void setInstance(RDFIndividual instance) {
		OWLIndividual inst = (OWLIndividual) instance;
		if (inst == null) {
			clearSelection();
		} else if (inst.hasRDFType(service, true)) {
			setServiceMode(inst);
		} else if (inst.hasRDFType(profile, true)) {
			setProfileMode(inst);
		} else if (inst.hasRDFType(compprocess, true)) {
			setCompositeProcessMode(inst);
		} else if (inst.hasRDFType(process, true)) {
			setProcessMode(inst);
		} else if (inst.hasRDFType(grounding, true)) {
			setGroundingMode(inst);
		}
	}

	/*
	 * Change the editor and options panes to show the properties and options
	 * for the selected instance. The reason for the paintAll calls below is
	 * that there is something strange going on with the repainting in the new
	 * Protege 3 UI.
	 */
	public void setServiceMode(OWLIndividual inst) {
		if (inst == null)
			properties.clearSelection();
		else
			properties.setInstance(inst);
		if (currentMode == COMPOSITE) {
			comp = properties;
			removeAll();
			add(comp, BorderLayout.CENTER);
		}
		paintAll(this.getGraphics());
		currentMode = SERVICE;
	}

	public void setProfileMode(OWLIndividual inst) {
		properties.setInstance(inst);
		if (currentMode == COMPOSITE) {
			comp = properties;
			removeAll();
			add(comp, BorderLayout.CENTER);
		}
		paintAll(this.getGraphics());
		currentMode = PROFILE;
	}

	public void setProcessMode(OWLIndividual inst) {
		properties.setInstance(inst);
		if (currentMode == COMPOSITE) {
			comp = properties;
			removeAll();
			add(comp, BorderLayout.CENTER);
		}
		paintAll(this.getGraphics());
		currentMode = PROCESS;
	}

	public void setGroundingMode(OWLIndividual inst) {
		properties.setInstance(inst);
		if (currentMode == COMPOSITE) {
			comp = properties;
			removeAll();
			add(comp, BorderLayout.CENTER);
		}
		paintAll(this.getGraphics());
		currentMode = GROUNDING;
	}

	public void setCompositeProcessMode(OWLIndividual inst) {
		comppane.setInstance(inst);
		if (currentMode != COMPOSITE) {
			comp = comppane;
			removeAll();
			add(comp, BorderLayout.CENTER);
		}
		paintAll(this.getGraphics());
		currentMode = COMPOSITE;
	}

}
