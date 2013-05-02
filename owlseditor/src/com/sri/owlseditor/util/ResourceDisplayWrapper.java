package com.sri.owlseditor.util;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.ResourceDisplay;

/**
 * This class just extends ResourceDisplay to implement the OWLSResourceDisplay
 * interface.
 * 
 * 
 * @author Daniel Elenius
 */
public class ResourceDisplayWrapper extends ResourceDisplay implements
		OWLSResourceDisplay {

	public ResourceDisplayWrapper(Project project) {
		super(project);
	}

	public void setInstance(RDFIndividual instance) {
		super.setInstance(instance);
	}

	public void clearSelection() {
		super.clearSelection();
	}

}
