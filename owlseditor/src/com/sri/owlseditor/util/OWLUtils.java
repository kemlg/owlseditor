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
package com.sri.owlseditor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;

public class OWLUtils {

	public static RDFResource getParameterType(OWLIndividual param, OWLModel okb){
		RDFResource type = null;
		if (param != null){
			OWLDatatypeProperty paramType = okb.getOWLDatatypeProperty("process:parameterType");
			RDFSLiteral literal = (RDFSLiteral)param.getPropertyValueLiteral(paramType);
			if (literal != null){
				String uri = literal.getString();
				String resourceName = okb.getResourceNameForURI(uri);
				if (resourceName != null)
					type = okb.getRDFResource(resourceName);
			}
		}
		return type;
	}
	
	/** Sorts a Collection of Frames alphabetically. */
	public static Collection sortFrameCollection(Collection c){
		ArrayList list = new ArrayList(c);
		Collections.sort(list, new FrameComparator());
		return list;
	}
	
	public static boolean setPropertyValueInHomeStore(OWLIndividual inst, RDFProperty prop, Object value){
    	TripleStoreModel ts = inst.getOWLModel().getTripleStoreModel();
    	TripleStore homeStore = ts.getHomeTripleStore(inst);
    	TripleStore currentStore = ts.getActiveTripleStore();
    	if (currentStore != homeStore){
    		if (ts.isEditableTripleStore(homeStore)){
    			//ts.setActiveTripleStore(homeStore);
    			//inst.setPropertyValue (prop, value);
    			//ts.setActiveTripleStore(currentStore);
    			homeStore.add(inst, prop, value);
    			return true;
    		}
    	}
    	else{
    		inst.setPropertyValue(prop, value);
    		return true;
    	}
    	return false;
	}
	
	/** @deprecated */
	public static void removeNamedSlotValue(OWLIndividual inst,
											String slotname,
											OWLModel okb){
		OWLProperty slot = okb.getOWLProperty(slotname);
		Iterator it = inst.getPropertyValues(slot, true).iterator();
		while (it.hasNext()){
			Object value = it.next();
			inst.removePropertyValue(slot, value);
		}
	}
	
	/* This method assumes the slot takes only one value, so it
	 * removes any previous values for the slot.
	 */
	/** @deprecated */
	public static void setNamedSlotValue(OWLIndividual inst, 
										 String slotname, 
										 Object value,
										 OWLModel okb){
		/* First remove any previous occurence of the slot */
		OWLProperty slot = okb.getOWLProperty(slotname);
		Iterator it = inst.getPropertyValues(slot, true).iterator();
		while (it.hasNext()){
			Object oldvalue = it.next();
			inst.removePropertyValue(slot, oldvalue);
		}
		
		/* Now add the new slot value */
		inst.addPropertyValue(slot, value);
	}
	
	/** @deprecated */
	public static Object getNamedSlotValue(OWLIndividual inst, String slotname, OWLModel okb){
		OWLProperty slot = okb.getOWLProperty(slotname);
		return inst.getPropertyValue(slot);
	}
	
	/** @deprecated */
	public static String getClassNameOfInstance(OWLIndividual inst){
		return inst.getRDFType().getName();
	}
	
}
