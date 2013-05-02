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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.sri.owlseditor.util.MultipleInstanceSelector;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueAdapter;


/**
   The pane on the left of the OWL-S Tab, where the user selects which Service
   instance, or associated profile, process or grounding, to edit.
 */
public class ServiceSelector extends MultipleInstanceSelector{
    
	private OWLModel model;
    private ServiceEditor _editor;
	private Vector listeners = new Vector();
   
	private Collection owlsProperties;
	private Collection owlsClasses;
	

    /**
     */
    public ServiceSelector(OWLModel okb, Project project, ServiceEditor editor){
    	super(okb, project, editor);
    	
    	this.model = okb;
    	setupClassesAndProperties();
    	
    	setClasses(owlsClasses);
    	
    	/* A listener that reacts when describedBy, presents,
    	   support, etc. slots are modified on an instance, so that the
    	   bold-status remains correct. */
    	model.addPropertyValueListener(new PropertyValueAdapter(){
    		public void propertyValueChanged(RDFResource resource, RDFProperty property, java.util.Collection oldValues){
    			if (resource == getSelectedInstance())
    				if (owlsProperties.contains(property))
    						updateInstanceLists((OWLIndividual)resource);
    		}
    	});
    	
    }
    
	private void setupClassesAndProperties(){
    	ArrayList classes = new ArrayList();

		OWLNamedClass service = model.getOWLNamedClass("service:Service");
		System.out.println("service:Service " + service + " : " + service.getPrefixedName());
		OWLNamedClass profile = model.getOWLNamedClass("profile:Profile");
		System.out.println("profile:Profile " + profile);
		OWLNamedClass process = model.getOWLNamedClass("process:Process");
		System.out.println("process:Process " + process);
		OWLNamedClass grounding = model.getOWLNamedClass("grounding:WsdlGrounding");
		System.out.println("grounding:WsdlGrounding " + grounding);
    	
    	classes.add(service);
    	classes.add(profile);
    	classes.add(process);
    	classes.add(grounding);

    	owlsClasses = classes;
    	
    	Iterator it = owlsClasses.iterator();
    	while(it.hasNext())
    	{
    		System.out.println(it.next());
    	}

		ArrayList properties = new ArrayList();
		
		OWLObjectProperty describedBy = model.getOWLObjectProperty("service:describedBy");
		OWLObjectProperty presents = model.getOWLObjectProperty("service:presents");
		OWLObjectProperty supports = model.getOWLObjectProperty("service:supports");
		OWLObjectProperty presentedBy = model.getOWLObjectProperty("service:presentedBy");
		OWLObjectProperty has_process = model.getOWLObjectProperty("profile:has_process");
		OWLObjectProperty describes = model.getOWLObjectProperty("service:describes");
		OWLObjectProperty supportedBy = model.getOWLObjectProperty("service:supportedBy");
		OWLObjectProperty hasAtomicProcessGrounding = model.getOWLObjectProperty("grounding:hasAtomicProcessGrounding");
		
		properties.add(describedBy);
		properties.add(presents);
		properties.add(supports);
		properties.add(presentedBy);
		properties.add(has_process);
		properties.add(describes);
		properties.add(supportedBy);
		properties.add(hasAtomicProcessGrounding);

		owlsProperties = properties;
	}

    public void addServiceSelectorListener(ServiceSelectorListener listener){
    	listeners.add(listener);
    }
    
    public void removeServiceSelectorListener(ServiceSelectorListener listener){
    	listeners.remove(listener);
    }

    /* Called when the selection changes, to notify all listeners */
    private void fireSelectionEvent(OWLIndividual inst){
    	for (Enumeration e = listeners.elements() ; e.hasMoreElements() ;) {
			((ServiceSelectorListener)e.nextElement()).setInstance(inst);
		}
    }

    private void updateInstanceLists(OWLIndividual instance){
    	super.updateInstanceLists(instance);
    	if (instance != null)
    		fireSelectionEvent(instance);
    }
    
}