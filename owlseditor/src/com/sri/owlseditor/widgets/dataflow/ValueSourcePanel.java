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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

/**
 * @author Daniel Elenius
 *
 */
public class ValueSourcePanel extends AbstractBindingPanel {
	private JComboBox fromProcessChooser;
	private JComboBox fromParameterChooser;

	private OWLProperty hasInputSlot;
	private OWLProperty hasOutputSlot;
	private OWLProperty processSlot;
	private OWLProperty fromProcessSlot;
	private OWLProperty theVarSlot;
	private OWLProperty valueSourceSlot;
	private OWLProperty valueFormSlot;
	private OWLProperty valueDataSlot;
	private OWLProperty valueFunctionSlot;
	private OWLProperty valueTypeSlot;
	
	private OWLNamedClass performCls;
	private OWLNamedClass valueOfCls;
	
	private OWLIndividual theParentPerform;
	
	private OWLIndividual currentBinding;
	private OWLIndividual currentPerform;
	private OWLIndividual currentFromPerform;
	
	private ItemListener fromProcessListener;
	private ItemListener fromParameterListener;
	
	public ValueSourcePanel(OWLModel okb){
		super(okb);
		setupSlotsAndClasses();

		fromProcessChooser = ComponentFactory.createComboBox();
		fromProcessListener = new FromProcessListener(okb, fromParameterChooser);
		enableFromProcessListener();
		
		fromParameterChooser = ComponentFactory.createComboBox();;
		fromParameterListener = new FromParameterListener(okb);
		enableFromParameterListener();
		
		setLayout(new GridBagLayout());
		GridBagConstraints box_constraints = new GridBagConstraints();
		box_constraints.gridx = 0;
		box_constraints.fill = GridBagConstraints.BOTH;
		Box box = new Box(BoxLayout.Y_AXIS);
		add(box, box_constraints);
		
		JPanel fromPerformLabelPane = ComponentFactory.createPanel();
		fromPerformLabelPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		fromPerformLabelPane.add(ComponentFactory.createLabel("From Perform"));
		box.add(fromPerformLabelPane);
		
		box.add(fromProcessChooser);
		box.add(Box.createVerticalStrut(20));
		
		JPanel fromParameterLabelPane = ComponentFactory.createPanel();
		fromParameterLabelPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		fromParameterLabelPane.add(ComponentFactory.createLabel("From Parameter"));
		box.add(fromParameterLabelPane);
		
		box.add(fromParameterChooser);
	}
	
	private void enableFromProcessListener(){
		fromProcessChooser.addItemListener(fromProcessListener);
	}
	
	private void disableFromProcessListener(){
		fromProcessChooser.removeItemListener(fromProcessListener);
	}
	
	private void enableFromParameterListener(){
		fromParameterChooser.addItemListener(fromParameterListener);
	}

	private void disableFromParameterListener(){
		fromParameterChooser.removeItemListener(fromParameterListener);
	}

	/* Helper class to set valueSource.
	 * Since only one of the properties
	 * valueSource, valueForm, valueFunction, valueData or valueType
	 * is allowed at any time, we have to look for and remove all the other
	 * ones.
	 */
	private void setValueSource(Object value){
		// First, we delete the old binding. We have to check for all the types.
		removeValueSpecifier(valueSourceSlot);
		removeValueSpecifier(valueFormSlot);
		removeValueSpecifier(valueFunctionSlot);
		removeValueSpecifier(valueTypeSlot);
		removeValueSpecifier(valueDataSlot);
		// Add the new binding
		currentBinding.addPropertyValue(valueSourceSlot, value);
		//System.out.println("setValueSource: Added value " + ((Instance)value).getName());
	}

	/* Helper method for setValueSource */
	private void removeValueSpecifier(OWLProperty valueSpecifier){
		OWLIndividual oldValueSpecifier = (OWLIndividual)currentBinding.getPropertyValue(valueSpecifier);
		if (oldValueSpecifier != null){
			currentBinding.removePropertyValue(valueSpecifier, oldValueSpecifier);
		}
	}
	
	/* Called when the Perform selection changes. We need to update 
	 * the combo boxes.
	 */
	public void setPerform(OWLIndividual perform){
		// add all parent Performs to the fromProcessChooser
		//System.out.println("ValueSourcePanel: setPerform " + perform.getName());
		
		currentPerform = perform;
		fromProcessChooser.removeAllItems();

		// Add performs to the From Process chooser
		Collection performs = getPredecessorPerforms(perform);
		Iterator it = performs.iterator();
		while (it.hasNext()){
			OWLIndividual pred = (OWLIndividual)it.next();
			fromProcessChooser.addItem(pred.getName());
			//System.out.println("ValueSourcePanel.setPerform(): Adding predecessor Perform " + pred.getName());
		}
	}
	
	/* Update the combo boxes to reflect the currently selected binding */
	public void setBinding(OWLIndividual binding, OWLIndividual perform) {
		//System.out.println("setBinding called with perform " + perform + " and binding " + binding);
		
		disableFromParameterListener();
		disableFromProcessListener();
		
		if (!perform.equals(currentPerform))
			setPerform(perform);

		if (binding != null){
			currentBinding = binding;
			
			OWLIndividual valueOf = (OWLIndividual)binding.getPropertyValue(valueSourceSlot);
			OWLIndividual fromProcess = (OWLIndividual)valueOf.getPropertyValue(fromProcessSlot);
			RDFIndividual fromParameter = (RDFIndividual)valueOf.getPropertyValue(theVarSlot);
			
			if (fromProcess != null){
				//System.out.println("setBinding: selecting fromProcess: " + fromProcess.getName());
				fromProcessChooser.setSelectedItem(fromProcess.getName());
				fromProcessChooser.repaint();
			}
			else{
				if (fromProcessChooser.getItemCount() > 0){
					fromProcess = okb.getOWLIndividual((String)fromProcessChooser.getItemAt(0));
					fromProcessChooser.setSelectedIndex(0);
					valueOf.addPropertyValue(fromProcessSlot, fromProcess);
				}
			}
			currentFromPerform = fromProcess;

			updateFromParameterChooser();
			if (fromParameter != null){
				//System.out.println("setBinding: selecting fromParameter: " + fromParameter.getName());
				fromParameterChooser.setSelectedItem(fromParameter.getName());
				fromParameterChooser.repaint();
			}
			else{
				if (fromParameterChooser.getItemCount() > 0){
					fromParameter = okb.getOWLIndividual((String)fromParameterChooser.getItemAt(0));
					fromParameterChooser.setSelectedIndex(0);
					valueOf.addPropertyValue(theVarSlot, fromParameter);
				}
			}
			//System.out.println("setBinding setting currentFromPerform to " + currentFromPerform);
		}
		enableFromProcessListener();
		enableFromParameterListener();
	}

	public void updateFromParameterChooser(){
		fromParameterChooser.removeAllItems();
		// Add the selected Perform's parameters to the From Parameter choose
		Collection parameters = getParameters(currentFromPerform);
		
		if (parameters != null){
			Iterator it = parameters.iterator();
			while (it.hasNext()){
				RDFIndividual parameter = (RDFIndividual)it.next();
				fromParameterChooser.addItem(parameter.getName());
			}
		}
	}
	
	/* Called when the from process selection changes */
	public void setFromPerform(OWLIndividual fromPerform){
		fromProcessChooser.setSelectedItem(fromPerform.getName());
		currentFromPerform = fromPerform; 
	}
	
	/* Returns all Performs that occur previous to this one. These are
	 * the ones we can create data flow bindings for.
	 */
	private Collection getPredecessorPerforms(OWLIndividual perform){
		Collection preds = PerformTreeMapper.getInstance().getPredecessors(perform);  
		preds.add(theParentPerform);
		return preds;
	}
	
	/* Returns the composite process of the currentPerform */
	private OWLIndividual getParentProcess(){
		OWLIndividual parent = PerformTreeMapper.getInstance().getCompositeProcess(currentPerform); 
		//System.out.println("The Parent composite process of this perform is: " + parent);
		return parent;
	}
	
	/* Returns the process of the current Perform */
	private OWLIndividual getThisProcess(){
		OWLIndividual thisProcess = (OWLIndividual)currentPerform.getPropertyValue(processSlot); 
		System.out.println("ThisProcess is: " + thisProcess);
		return thisProcess; 
	}
	
	/*
	 * This is almost the same as the DataFlowBinding.getParameters()
	 * 
	 * Returns a List of all Outputs of the process that this
	 * Perform performs.
	 */
	private List getParameters(OWLIndividual fromPerform){
		if (fromPerform == null)
			return null;
		
		OWLIndividual fromProcess = null;
		if (fromPerform.getName().equals("process:TheParentPerform"))
			fromProcess = getParentProcess();
		else if (fromPerform.getName().equals("process:ThisPerform"))
			fromProcess = getThisProcess();
		else
			fromProcess = getProcess(fromPerform);

		if (fromProcess==null)
			return null;
		else{
			if (fromPerform.getName().equals("process:TheParentPerform")){
				// return inputs
				return (List) fromProcess.getPropertyValues(hasInputSlot, false);
			}
			else{ 
				// return outputs
				return (List) fromProcess.getPropertyValues(hasOutputSlot, false);
			}
		}
	}

	/* Returns the Process that this Perform executes */
	private OWLIndividual getProcess(OWLIndividual perform){
		return (OWLIndividual)perform.getPropertyValue(processSlot); 
	}

	private void setupSlotsAndClasses(){
		OWLModel okb = getOWLModel();
		
		fromProcessSlot = okb.getOWLProperty("process:fromProcess");
		theVarSlot = okb.getOWLProperty("process:theVar");
		hasInputSlot = okb.getOWLProperty("process:hasInput");
		hasOutputSlot = okb.getOWLProperty("process:hasOutput");
		processSlot =  okb.getOWLProperty("process:process");
		valueSourceSlot = okb.getOWLProperty("process:valueSource");
		valueFormSlot = okb.getOWLProperty("process:valueForm");
		valueDataSlot = okb.getOWLProperty("process:valueData");
		valueFunctionSlot = okb.getOWLProperty("process:valueFunction");
		valueTypeSlot = okb.getOWLProperty("process:valueType");
		
		performCls = okb.getOWLNamedClass("process:Perform");
		valueOfCls = okb.getOWLNamedClass("process:ValueOf");
		
		theParentPerform = okb.getOWLIndividual("process:TheParentPerform");
	}

	/* Called when the current binding is deleted. Deletes
	 * the ValueOf of this binding.
	 */
	public void deleteValueSpecifier(OWLIndividual binding){
		OWLIndividual valueOf = (OWLIndividual)binding.getPropertyValue(valueSourceSlot);
		valueOf.delete();
	}
	
	/* Called whenever the from perform selection is changed by the user. */
	private void fromPerformSelected(OWLIndividual fromPerform){
		disableFromParameterListener();
		
		//System.out.println("fromPerformSelected");
		currentFromPerform = fromPerform;
		updateFromParameterChooser();
		
		RDFIndividual fromParameter = null;
		if (fromParameterChooser.getItemCount() > 0){
			fromParameterChooser.setSelectedIndex(0);
			String parameterName = (String)fromParameterChooser.getItemAt(0);
			fromParameter = okb.getOWLIndividual(parameterName);
		}
		
		OWLIndividual valueOf = (OWLIndividual)currentBinding.getPropertyValue(valueSourceSlot);
		if (valueOf == null){
			valueOf = valueOfCls.createOWLIndividual(null);
		}

		OWLIndividual oldValue = (OWLIndividual)valueOf.getPropertyValue(fromProcessSlot);
		if (oldValue != null)
			valueOf.removePropertyValue(fromProcessSlot, oldValue);
		valueOf.addPropertyValue(fromProcessSlot, fromPerform);

		oldValue = (OWLIndividual)valueOf.getPropertyValue(theVarSlot);
		if (oldValue != null){
			valueOf.removePropertyValue(theVarSlot, oldValue);
		}
		if (fromParameter != null){
			valueOf.addPropertyValue(theVarSlot, fromParameter);
		}
		
		setValueSource(valueOf);		
		enableFromParameterListener();
	}

	/* Called whenever the from parameter selection is changed by the user. */
	public void fromParameterSelected(RDFIndividual fromParameter){
		//System.out.println("fromParameterSelected");
		OWLIndividual valueOf = (OWLIndividual)currentBinding.getPropertyValue(valueSourceSlot);

		RDFIndividual oldValue = (OWLIndividual)valueOf.getPropertyValue(theVarSlot);
		if (oldValue != null){
			valueOf.removePropertyValue(theVarSlot, oldValue);
		}
		if (fromParameter != null){
			valueOf.addPropertyValue(theVarSlot, fromParameter);
		}
		
		OWLIndividual fromProcess = (OWLIndividual)valueOf.getPropertyValue(fromProcessSlot);
		if (fromProcess == null)
			System.out.println("WARNING! fromParameterSelected setting ValueOf with NULL fromProcess!");
		
		setValueSource(valueOf);				
	}

	
	/* This listener makes sure that the parameter chooser gets updated when
	 * the selections of Perform changes, so that only the parameters of the
	 * currently selected perform are shown.
	 * 
	 * Also, changes the ValueOf of this binding to reflect the change.
	 * 
 	 * This should only respond to USER changes. Therefore, we need to call
	 * diableFromProcessListener() before doing any programmatic changes to
	 * the fromProcessChooser.
	 */
	class FromProcessListener implements ItemListener{
		private OWLModel okb;
		private JComboBox fromParameterChooser;
		
		public FromProcessListener(OWLModel okb, JComboBox fromParameterChooser){
			this.okb = okb;
			this.fromParameterChooser = fromParameterChooser;
		}
		
		public void itemStateChanged(ItemEvent e){
			String performName = (String)e.getItem();
			OWLIndividual fromPerform = okb.getOWLIndividual(performName);
			fromPerformSelected(fromPerform);
		}
	}

	/* This listener updates the value of the parameter in the KB whenever the
	 * value of the From Parameter chooser changes.
	 * 
	 * This should only respond to USER changes. Therefore, we need to call
	 * diableFromParameterListener() before doing any programmatic changes to
	 * the fromParameterChooser.
	 */
	class FromParameterListener implements ItemListener{
		private OWLModel okb;
		
		public FromParameterListener(OWLModel okb){
			this.okb = okb;
		}
		
		public void itemStateChanged(ItemEvent e){
			String parameterName = (String)e.getItem();
			OWLIndividual fromParameter = okb.getOWLIndividual(parameterName);
			fromParameterSelected(fromParameter);
		}
	}

}
