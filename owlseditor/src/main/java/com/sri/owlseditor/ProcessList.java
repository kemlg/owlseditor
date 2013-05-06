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

import java.awt.event.MouseListener;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

public class ProcessList extends BoldableOWLSInstanceList {

	public ProcessList(Project project) {
		super(project, "process:Process");
	}
	
	public void update(RDFIndividual instance) {
		// System.out.println("ProcessList.update()");
		boldItems.clear();
		if (instance == null) {
			clearSelection();
		} else if (instance.hasRDFType(service, true)) {
			clearSelection();
			// describedBy: service -> process
			boldItems.addAll(instance.getPropertyValues(describedBy));
		} else if (instance.hasRDFType(profile, true)) {
			clearSelection();
			// has_process: profile -> process
			boldItems.addAll(instance.getPropertyValues(has_process));
		} else if (instance.hasRDFType(process, true)) {
			setSelectedInstance(instance);
			boldItems.add(instance);
		} else if (instance.hasRDFType(grounding, true)) {
			clearSelection();
			// hasAtomicProcessGrounding: grounding -> process
			boldItems.addAll(instance
					.getPropertyValues(hasAtomicProcessGrounding));
		}
	}

}
