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
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

public class GroundingList extends BoldableOWLSInstanceList {
	
	public GroundingList(Project project){
		super(project, "grounding:WsdlGrounding");
    	OWLModel model = (OWLModel)project.getKnowledgeBase();
	}
	
	public void update(RDFIndividual instance){
		boldItems.clear();
		if (instance == null){
			clearSelection();
		}
		else if (instance.hasRDFType(service, true)){
			clearSelection();
			// supports: service -> grounding
			boldItems.addAll(instance.getPropertyValues(supports));
		}
		else if (instance.hasRDFType(profile, true)){
			clearSelection();
			// no property profile -> grounding
		}
		else if (instance.hasRDFType(process, true)){
			clearSelection();
			// no property process -> grounding
		}
		else if (instance.hasRDFType(grounding, true)){
			setSelectedInstance(instance);
			boldItems.add(instance);
		}
	}
	
}



