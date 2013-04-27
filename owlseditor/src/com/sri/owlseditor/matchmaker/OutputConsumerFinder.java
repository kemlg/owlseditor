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
package com.sri.owlseditor.matchmaker;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeModel;

import com.sri.owlseditor.cmp.GraphUpdateManager;
import com.sri.owlseditor.cmp.tree.OWLSTree;
import com.sri.owlseditor.cmp.tree.OWLSTreeMapper;
import com.sri.owlseditor.cmp.tree.OWLSTreeNode;
import com.sri.owlseditor.util.OWLSList;
import com.sri.owlseditor.widgets.dataflow.PerformTreeMapper;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;


/**
 * @author Daniel Elenius
 */
public class OutputConsumerFinder extends MatchFinder {

	private static OutputConsumerFinder instance;
	private GraphUpdateManager mgr;
	
	private OutputConsumerFinder(GraphUpdateManager mgr){
		super("Find Output Consumers");
		this.mgr = mgr;
	}
	
	public static OutputConsumerFinder getInstance(GraphUpdateManager mgr){
		if (instance == null)
			instance = new OutputConsumerFinder(mgr);
		return instance;
	}
	
	public List getMatches(OWLIndividual perform){
		Matchmaker matchMaker = Matchmaker.getInstance(perform.getOWLModel()); 
		return matchMaker.findOutputConsumers(perform);
	}

	private void createOutgoingDataFlow(MatchResult mResult, 
										OWLIndividual oldPerform,
										OWLIndividual newPerform){
		//System.out.println("Adding data flow from " + oldPerform.getName() +
		//					" to " + newPerform.getName());
		
		OWLIndividual newProcess = (OWLIndividual)perform.getPropertyValue(processProperty);
		Collection outputs = process.getPropertyValues(hasOutputProperty);
		Iterator it = outputs.iterator();
		while (it.hasNext()){
			OWLIndividual output = (OWLIndividual)it.next();
			
			//System.out.println("Source output: " + output.getName());
			
			MatchPair pair = mResult.getOutputMatch(output);
			if (pair != null){
				String inputstring = pair.getNewParameter();
				
				//System.out.println("Target input: " + inputstring);
				
				if (inputstring != null){
					OWLIndividual input = (OWLIndividual)model.getRDFResource(inputstring);
					if (input != null){
						if (newPerform.hasRDFType(performCls, true)){
							OWLIndividual inputBinding = inputBindingCls.createOWLIndividual(null);
							inputBinding.setPropertyValue(toParam, input);
							OWLIndividual valueOf = valueOfCls.createOWLIndividual(null);
							valueOf.setPropertyValue(fromProcess, oldPerform);
							valueOf.setPropertyValue(theVar, output);
							inputBinding.setPropertyValue(valueSource, valueOf);
							newPerform.addPropertyValue(hasDataFrom, inputBinding);
						}
						else if (newPerform.hasRDFType(produceCls, true)){
							OWLIndividual outputBinding = outputBindingCls.createOWLIndividual(null);
							outputBinding.setPropertyValue(toParam, input);
							OWLIndividual valueOf = valueOfCls.createOWLIndividual(null);
							valueOf.setPropertyValue(fromProcess, oldPerform);
							valueOf.setPropertyValue(theVar, output);
							outputBinding.setPropertyValue(valueSource, valueOf);
							newPerform.addPropertyValue(producedBinding, outputBinding);
						}
					}
				}
			}
		}
	}
	
	/** Adds the match in a Sequence after the old perform, and updates
	 * the dataflow between the two */
	private void addAfter(OWLIndividual oldPerform, MatchResult mResult){
		OWLIndividual newProcess = mResult.getProcess();
		OWLModel model = newProcess.getOWLModel();
		OWLSTree tree = OWLSTreeMapper.getInstance(model).getTree(parent, mgr);
		
		OWLSTreeNode oldPerformNode = getPerformNode(tree.getRoot());
		OWLSTreeNode parentNode = (OWLSTreeNode)oldPerformNode.getParent();
		DefaultTreeModel treemodel = (DefaultTreeModel)tree.getModel();
		int index = treemodel.getIndexOfChild(parentNode, oldPerformNode);
		OWLIndividual parentConstruct = parentNode.getInstance();

		// NOTE: After this, we can no longer use the tree or the nodes!
		
		//System.out.println("addAfter: parent construct is " + parentConstruct.getName() +
		//					" of class " + parentConstruct.getRDFType().getName());
		
		OWLIndividual newPerform = null;
		if (newProcess == PerformTreeMapper.getInstance().getCompositeProcess(oldPerform)){
			// In this case "newPerform" is actually a Produce.
			newPerform = produceCls.createOWLIndividual(null);
		}
		else{	
			newPerform = performCls.createOWLIndividual(null);
		}
		
		// Old perform is root construct
		if (parentConstruct == null){
			// Set up the instances
			OWLIndividual sequenceConstruct = sequenceCls.createOWLIndividual(null);
			OWLNamedClass cclistCls = model.getOWLNamedClass(OWLSList.CC_LIST);
			
			// Make the sequence the main construct of the composite process
			parent.removePropertyValue(composedOfProperty, oldPerform);
			parent.setPropertyValue(composedOfProperty, sequenceConstruct);

			// Set up the list
			OWLSList sequenceList = new OWLSList(sequenceConstruct, model);
			
			// Manipulate the lists

			//System.out.println("Adding old perform to sequence");
			sequenceList.insertAtIndex(oldPerform, 0);
			
			//System.out.println("Adding new perform to sequence");
			sequenceList.insertAtIndex(newPerform, 1);
		}
		// If the parent isn't already a Sequence, we create a new Choice parent
		else if (!(parentConstruct.hasRDFType(sequenceCls, true))){
			// Set up the instances
			OWLIndividual sequenceConstruct = sequenceCls.createOWLIndividual(null);
			OWLNamedClass cclistCls = model.getOWLNamedClass(OWLSList.CC_LIST);
			
			// Set up the lists
			OWLSList sequenceList = new OWLSList(sequenceConstruct, model);
			OWLSList parentList = new OWLSList(parentConstruct, model);
			
			// Manipulate the lists

			//System.out.println("Adding sequence");
			parentList.insertAtIndex(sequenceConstruct, index);

			//System.out.println("Adding old perform to sequence");
			sequenceList.insertAtIndex(oldPerform, 0);
			
			//System.out.println("Adding new perform to sequence");
			sequenceList.insertAtIndex(newPerform, 1);
		}
		else{
			OWLSList parentList = new OWLSList(parentConstruct, model);
			//System.out.println("Adding new perform to sequence");
			parentList.insertAtIndex(newPerform, index+1);
		}
		// We do this last to trigger a tree recreate()
		newPerform.setPropertyValue(processProperty, newProcess);
		createOutgoingDataFlow(mResult, oldPerform, newPerform);
	}
	
	protected void setupButtonPanel(){
		buttonPanel = new JPanel();
		
		class AddAfterAction extends AbstractAction{
			public void actionPerformed(ActionEvent e){
				MatchResult mResult = getSelection(); 
				if (mResult != null)
					addAfter(perform, mResult);
			}
		}
		JButton addAfterButton = new JButton(new AddAfterAction());
		addAfterButton.setText("Add after in Sequence");
		buttonPanel.add(addAfterButton);

		buttonPanel.add(getDetailsButton());
		
		buttonPanel.add(getHelpButton());
	}

}
