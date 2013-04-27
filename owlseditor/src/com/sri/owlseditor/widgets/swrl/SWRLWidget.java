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
package com.sri.owlseditor.widgets.swrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.triplestore.Triple;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtomList;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLIndividual;
import edu.stanford.smi.protegex.owl.ui.widget.AbstractPropertyWidget;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMetadata;

/**
 * @author Daniel Elenius
 */
public class SWRLWidget extends AbstractPropertyWidget implements OWLWidgetMetadata {
	private OWLModel model;
	private OWLNamedClass swrlExpression;
	private OWLObjectProperty expressionObject;
	
	private OWLS_SWRLTextAreaPanel swrlPanel;
	private SWRLFactory swrlFactory;
	private HashMap oldAtomLists = new HashMap();
	
	public void initialize(){
		//setPreferredRows(4);
		//setPreferredColumns(4);
		this.model = getOWLModel();
		swrlPanel = new OWLS_SWRLTextAreaPanel(model, null, this);
		swrlFactory = new SWRLFactory(model);
		add(swrlPanel);
	}
	
	public void dispose(){
		swrlPanel.removeListeners();
	}
	
	public void setValues(Collection values){
		//SWRLAtomList atomList = (SWRLAtomList)CollectionUtilities.getFirstItem(values);
		RDFResource atomList = (RDFResource)getEditedResource().getPropertyValue(getRDFProperty());
		if (atomList instanceof SWRLAtomList){
			swrlPanel.setAtomList((SWRLAtomList)atomList);
		}
		else{
			// It's the nil list
			swrlPanel.setAtomList(null);
		}
	}
	
	/** Deletes an AtomList and all its sub-elements. This is static because
	 * it also needs to be called from other places where atomlists need to be deleted
	 * (e.g. when a Condition is deleted). */
	public static void deleteAtomList(SWRLAtomList atomList, OWLModel model){
        Set ownInstances = new HashSet();
        atomList.getReferencedInstances(ownInstances);
        ownInstances.add(atomList);
        OWLNamedClass parameterCls = model.getOWLNamedClass("process:Parameter");

        for (Iterator it = ownInstances.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof SWRLIndividual) {
                SWRLIndividual swrlIndividual = (SWRLIndividual) o;
                
                if (!swrlIndividual.isDeleted()) {
                    Iterator references = model.getTripleStoreModel().getActiveTripleStore().listTriplesWithObject(swrlIndividual);
                    boolean hasExternalRef = false;
                    // Not sure if we ever need the following code.
                    while (references.hasNext()) {
                    	Triple triple = (Triple) references.next();
                    	RDFResource ref = (RDFResource)triple.getSubject();
                    	if (!ownInstances.contains(ref)){
                    		hasExternalRef = true;
                    	}
                    }
                    if (!hasExternalRef &&  // !swrlIndividual.equals(atomList) && 
                    		!swrlIndividual.hasRDFType(parameterCls, true)) {
                    	// We don't want to delete it if it is a process:Parameter,
                    	// only if it is a "local" swrl variable.
                    	swrlIndividual.delete();
                    }
                }
            }
        }
	}
                    
                    	

	/* Used for debugging */
	private static void printAtomList(SWRLAtomList atomList){
        Set ownInstances = new HashSet();
        if (atomList != null){
	        atomList.getReferencedInstances(ownInstances);
	
	        Iterator it0 = ownInstances.iterator();
	        	while(it0.hasNext()){
	        		Object o = it0.next();
	        		if (o instanceof SWRLIndividual){
	        			SWRLIndividual so = (SWRLIndividual)o;
	        			System.out.println(so + " " + so.getBrowserText());
	        		}
	        }
        }
        else{
	        System.out.println("SWRLAtomList is null");
        }
	}
	
	
	public Collection getValues(){
		SWRLAtomList newAtomList = swrlPanel.getAtomList();
		
		ArrayList list = new ArrayList();
		if (newAtomList != null){
			list.add(newAtomList);
		}

		// Remove any previous atomlist
		SWRLAtomList oldAtomList = (SWRLAtomList) oldAtomLists.get(getEditedResource());
		
		if (oldAtomList != null){
			//if (oldAtomList.equals(newAtomList))
			//	System.out.println("WARNING! About to delete the new atomlist!");
			oldAtomLists.remove(getEditedResource());
		}
		oldAtomLists.put(getEditedResource(),newAtomList);

		return list;
	}
	
	// TODO: This widget shows up on ALL expressionObject properties, not just SWRL
	// ones.
	public int getSuitability(RDFSNamedClass cls, RDFProperty property){
		// Our imports mat not be loaded when initialize() is called, so we have to do this here.
		swrlExpression = model.getOWLNamedClass("expr:SWRL-Expression");
		expressionObject = model.getOWLObjectProperty("expr:expressionObject");
		
		if (cls.hasRDFType(swrlExpression) && property.isSubpropertyOf(expressionObject, true))
			return DEFAULT + 1;
		else
			return NOT_SUITABLE;
	}
	
	public static boolean isSuitable(Cls cls, Slot slot, Facet facet){
		return true;
	}
	
}
