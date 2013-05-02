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

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

public class ProfileList extends BoldableOWLSInstanceList {
	// private SearchManager searchManager;
	// private SearchAction searchAction;

	public ProfileList(Project project) {
		super(project, "profile:Profile");
		// searchManager = new SearchManager(project);
		// searchAction = new SearchAction(project, this, searchManager);
		// addHeaderButton(searchAction);
		// searchAction.setEnabled(false);
	}

	public void update(RDFIndividual instance) {
		boldItems.clear();
		if (instance == null) {
			clearSelection();
			// searchAction.setEnabled(false);
		} else if (instance.hasRDFType(service, true)) {
			clearSelection();
			// presents: service -> profile
			boldItems.addAll(instance.getPropertyValues(presents));
			// searchAction.setEnabled(false);
		} else if (instance.hasRDFType(profile, true)) {
			setSelectedInstance(instance);
			// searchAction.setEnabled(true);
			boldItems.add(instance);
		} else if (instance.hasRDFType(process, true)) {
			clearSelection();
			// searchAction.setEnabled(false);
		} else if (instance.hasRDFType(grounding, true)) {
			clearSelection();
			// searchAction.setEnabled(false);
		}
	}

}
