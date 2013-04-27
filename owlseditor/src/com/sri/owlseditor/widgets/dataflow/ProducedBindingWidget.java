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
package com.sri.owlseditor.widgets.dataflow;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMetadata;

/**
 * This widget helps users with the horribly complicated task of managing data flow declarations.
 * 
 * @author Daniel Elenius
 */
public class ProducedBindingWidget extends DataflowWidget implements OWLWidgetMetadata {

	public int getSuitability(RDFSNamedClass cls, RDFProperty property){
		if (cls.hasRDFType(produceCls, true) && 
				property == hasDataFromSlot)
			return OWLWidgetMetadata.DEFAULT+1;
		else	
			return OWLWidgetMetadata.NOT_SUITABLE;
	}
	
	/** Returns a List of all Outputs of the parent Composite Process,
	 *  except those that already have an OutputBinding. 
	 */
	protected List getToParameters(){
		OWLIndividual process = PerformTreeMapper.getInstance().getCompositeProcess(getProduce());
		if (process==null)
			return null;
		else{
			List inputs = new ArrayList();
			inputs.addAll(process.getPropertyValues(hasOutputSlot, false));
			inputs.removeAll(bindings.keySet());
			return inputs;
		}
	}

	/* Returns the Produce instance that this producedBinding slot is connected to */
	private OWLIndividual getProduce(){
		return (OWLIndividual)getEditedResource();
	}

	protected OWLIndividual createBindingInstance(){
		return (OWLIndividual)outputBindingCls.createInstance(null);
	}
	
	public void initialize() {
		super.initialize("Add Output Binding", "Add Output Binding");
	}
}
