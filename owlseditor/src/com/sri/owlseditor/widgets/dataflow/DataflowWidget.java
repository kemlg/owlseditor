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

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sri.owlseditor.widgets.AbstractCombinationWidget;

import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

/**
 * This widget helps users with the horribly complicated task of managing data flow declarations.
 * 
 * @author Daniel Elenius
 */
public abstract class DataflowWidget extends AbstractCombinationWidget {

	// GUI components
	ValueSourcePanel valueSourceEditor;
	ValueTextPanel valueTextEditor;
	
	// KB
	OWLModel okb;
	OWLProperty hasDataFromSlot;
	OWLProperty hasParameterSlot;
	OWLProperty hasInputSlot;
	OWLProperty hasOutputSlot;
	OWLProperty processSlot;
	OWLProperty toParamSlot;
	OWLProperty valueSourceSlot;
	OWLProperty valueFormSlot;
	OWLProperty valueDataSlot;
	OWLProperty valueTypeSlot;
	OWLProperty valueFunctionSlot;
	OWLNamedClass performCls;
	OWLNamedClass produceCls;
	OWLNamedClass bindingCls;
	OWLNamedClass inputBindingCls;
	OWLNamedClass outputBindingCls;
	OWLNamedClass inputCls;
	OWLNamedClass outputCls;
	OWLNamedClass valueOfCls;

	// Internal data
	// This HashSet has InputBindings and OutputBindings as values, and their
	// toParam Inputs as keys. 
	HashMap bindings;
		
	private HashMap createClsesMap(){
		okb = (OWLModel)getKnowledgeBase();
		HashMap clsesMap = new HashMap();
		valueSourceEditor = new ValueSourcePanel(okb);
		valueTextEditor = new ValueTextPanel(okb);
		clsesMap.put("valueSource", valueSourceEditor);
		clsesMap.put("valueFunction", valueTextEditor);
		clsesMap.put("valueData", valueTextEditor);
		return clsesMap;
	}
	
	public void initialize(String addString, String removeString) {
		super.initialize("To Parameter",
						 addString,
						 removeString,
						 "Binding type",
						 createClsesMap());
		bindings = new HashMap();
		setupSlotsAndClasses();
		//setEditorComponent(valueSourceEditor);
	}
	
	public Collection getValues(){
		return bindings.values();
	}
	
	/* Protege calls this when the user clicks on a Perform instance */
	public void setValues(Collection values){
		bindings.clear();
		Iterator it = values.iterator();
		RDFIndividual instIO = null;
		OWLIndividual binding = null;
		while (it.hasNext()){
			binding = (OWLIndividual)it.next();
			instIO = (RDFIndividual)binding.getPropertyValue(toParamSlot);
			if (!bindings.containsKey(instIO))
				bindings.put(instIO, binding);
		}
		setListValues(bindings.keySet());
		
		// If the bindings list is non-empty we select the first one
		it = bindings.entrySet().iterator();
		if (it.hasNext())
			setSelectedListIndex(0);
		
		//valueSourceEditor.setPerform(getPerform());
	}
	
	/** Returns a list of all potential target parameters that do
	 * not already have a binding.
	 */
	protected abstract List getToParameters();
	
	/** Returns a new Binding of the appropriate type */
	protected abstract OWLIndividual createBindingInstance();
	
	/* We look these up just once, and reuse them */
	private void setupSlotsAndClasses(){
		hasDataFromSlot = okb.getOWLProperty("process:hasDataFrom");
		processSlot = okb.getOWLProperty("process:process");
		hasParameterSlot = okb.getOWLProperty("process:hasParameter");
		hasInputSlot = okb.getOWLProperty("process:hasInput");
		hasOutputSlot = okb.getOWLProperty("process:hasOutput");
		toParamSlot = okb.getOWLProperty("process:toParam");
		valueSourceSlot = okb.getOWLProperty("process:valueSource");
		valueFormSlot = okb.getOWLProperty("process:valueForm");
		valueDataSlot = okb.getOWLProperty("process:valueData");
		valueTypeSlot = okb.getOWLProperty("process:valueType");
		valueFunctionSlot = okb.getOWLProperty("process:valueFunction");

		performCls = okb.getOWLNamedClass("process:Perform");
		produceCls = okb.getOWLNamedClass("process:Produce");
		bindingCls = okb.getOWLNamedClass("process:Binding");
		inputBindingCls = okb.getOWLNamedClass("process:InputBinding");
		outputBindingCls = okb.getOWLNamedClass("process:OutputBinding");
		inputCls = okb.getOWLNamedClass("process:Input");
		outputCls = okb.getOWLNamedClass("process:Output");
		valueOfCls = okb.getOWLNamedClass("process:ValueOf");
	}

	/* Called when the add button is clicked on the LabeledComponent */
	public Object addListItem(Component parent){
		List inputs = getToParameters();
		if (inputs != null){
			try{
				RDFIndividual instIO = (RDFIndividual) DisplayUtilities.pickInstanceFromCollection(
						parent,
						inputs,
						0,
						"Choose a target parameter");

				if (bindings.containsKey(instIO))
					return null;
				else{	
					// Create an anonymous new Binding instance
					OWLIndividual bindingInst = null;
					bindingInst = createBindingInstance();
					// Add the newly added instance to the toParam slot
					bindingInst.addPropertyValue(toParamSlot, instIO);
					// Also add this new binding to our internal list
					bindings.put(instIO, bindingInst);
					//System.out.println("Added " + bindingInst.getName());
					//System.out.println("bindings now contain " + bindings);
					valueChanged();
					return instIO;
				}
			}
			catch(Exception ex){
				//System.out.println("WARNING! No parameters on selected process.");
			}
		}
		else{
			System.out.println("WARNING! No parameters on selected process.");
		}
		return null;
	}

	/* Called when the remove button is clicked on the LabeledComponent */
	public boolean removeListItem(Object listItem){
		RDFIndividual instIO = (RDFIndividual)listItem;

		OWLIndividual bindingInst = (OWLIndividual)bindings.get(instIO);
			
		// Delete the ValueOf, etc
		if (getComboSelection().equals("valueSource"))
			valueSourceEditor.deleteValueSpecifier(bindingInst);
		else
			valueTextEditor.deleteValueSpecifier(bindingInst);
				
		bindingInst.delete();
            
		bindings.remove(instIO);
		//System.out.println("Removed " + instIO.getName());
		//System.out.println("bindings now contain " + bindings);
		
		setEditorComponent(null);
		valueChanged();
		return true;
	}

	/* TODO: Implement this method */
	public void comboSelectionChanged(Object selectedItem){
		System.out.println("comboSelectionChanged() - Not supported yet.");		
		/*
		if (selectedItem.equals("valueSource"))
			setEditorComponent(valueSourceEditor);
		else
			setEditorComponent(valueTextEditor);
		*/
	}
	
	/* Called when the selection in the toParam component changes 
	 */
	public void listSelectionChanged(Object selectedItem){
		//if (selectedItem == null)
		//	setEditorComponent(null);
		
		OWLIndividual perform = (OWLIndividual)getEditedResource();  // can also be a produce
		RDFIndividual instIO = (RDFIndividual)selectedItem;
		OWLIndividual binding = (OWLIndividual)bindings.get(instIO);
			
		OWLIndividual valueSource = (OWLIndividual)binding.getPropertyValue(valueSourceSlot);		
		OWLIndividual valueForm = (OWLIndividual)binding.getPropertyValue(valueFormSlot);
		OWLIndividual valueData = (OWLIndividual)binding.getPropertyValue(valueDataSlot);
		OWLIndividual valueType = (OWLIndividual)binding.getPropertyValue(valueTypeSlot);
		OWLIndividual valueFunction = (OWLIndividual)binding.getPropertyValue(valueFunctionSlot);		

		
		if (valueSource != null){
			valueSourceEditor.setBinding(binding, perform);
			setComboSelection("valueSource");
			setEditorComponent(valueSourceEditor);
		}
		else if (valueForm != null){
			valueTextEditor.setBinding(binding, perform);
			setEditorComponent(valueTextEditor);
		}
		else if (valueData != null){
			valueTextEditor.setBinding(binding, perform);
			setEditorComponent(valueTextEditor);
		}
		else if (valueType != null){
			valueTextEditor.setBinding(binding, perform);
			setEditorComponent(valueTextEditor);
		}
		else if (valueFunction != null){
			valueTextEditor.setBinding(binding, perform);
			setEditorComponent(valueTextEditor);						
		}
		else{
			// If there is no binding, we create a new valueSource binding
			OWLIndividual valueOf = (OWLIndividual) valueOfCls.createInstance(null);
			binding.setPropertyValue(valueSourceSlot, valueOf);
				
			valueSourceEditor.setBinding(binding, perform);
			setEditorComponent(valueSourceEditor);						
		}
	}
}
