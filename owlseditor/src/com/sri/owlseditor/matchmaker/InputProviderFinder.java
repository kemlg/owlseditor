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
public class InputProviderFinder extends MatchFinder {

	private static InputProviderFinder instance;
	private GraphUpdateManager mgr;
	
	private InputProviderFinder(GraphUpdateManager mgr){
		super("Find Input Providers");
		this.mgr = mgr;
	}
	
	public static InputProviderFinder getInstance(GraphUpdateManager mgr){
		if (instance == null)
			instance = new InputProviderFinder(mgr);
		return instance;
	}
	
	public List getMatches(OWLIndividual perform){
		Matchmaker matchMaker = Matchmaker.getInstance(perform.getOWLModel()); 
		return matchMaker.findInputProviders(perform);
	}

	private void createIncomingDataFlow(MatchResult mResult, 
										OWLIndividual oldPerform,
										OWLIndividual newPerform){
		//System.out.println("Adding data flow from " + newPerform.getName() +
		//					" to " + oldPerform.getName());
		
		Collection inputs = process.getPropertyValues(hasInputProperty);
		Iterator it = inputs.iterator();
		while (it.hasNext()){
			OWLIndividual input = (OWLIndividual)it.next();
			
			//System.out.println("Target input: " + input.getName());
			
			MatchPair pair = mResult.getInputMatch(input);
			if (pair != null){
				String outputstring = pair.getNewParameter();
				
				//System.out.println("Source output: " + outputstring);
				
				if (outputstring != null){
					OWLIndividual output = (OWLIndividual)model.getRDFResource(outputstring);
					if (output != null){
						OWLIndividual inputBinding = inputBindingCls.createOWLIndividual(null);
						inputBinding.setPropertyValue(toParam, input);
						OWLIndividual valueOf = valueOfCls.createOWLIndividual(null);
						valueOf.setPropertyValue(fromProcess, newPerform);
						valueOf.setPropertyValue(theVar, output);
						inputBinding.setPropertyValue(valueSource, valueOf);
						oldPerform.addPropertyValue(hasDataFrom, inputBinding);
					}
				}
			}
		}
	}
	
	/** Adds the match in a Sequence before the old perform, and updates
	 * the dataflow between the two */
	private void addBefore(OWLIndividual oldPerform, MatchResult mResult){
		OWLIndividual newProcess = mResult.getProcess();
		OWLModel model = newProcess.getOWLModel();
		OWLSTree tree = OWLSTreeMapper.getInstance(model).getTree(parent, mgr);
		
		OWLSTreeNode oldPerformNode = getPerformNode(tree.getRoot());
		OWLSTreeNode parentNode = (OWLSTreeNode)oldPerformNode.getParent();
		DefaultTreeModel treemodel = (DefaultTreeModel)tree.getModel();
		int index = treemodel.getIndexOfChild(parentNode, oldPerformNode);
		OWLIndividual parentConstruct = parentNode.getInstance();

		// NOTE: After this, we can no longer use the tree or the nodes!
		
		//System.out.println("addBefore: parent construct is " + parentConstruct.getName() +
		//					" of class " + parentConstruct.getRDFType().getName());
		
		// See if the matching proces is already in a predecessor Perform
		OWLIndividual newPerform = PerformTreeMapper.getInstance().
									getPredecessorPerformWithProcess(oldPerform, newProcess);
		// If it is not, then we have to manipulate the control flow a bit.
		if (newPerform == null){
			System.out.println("Process not in predecessor perform");
			
			newPerform = performCls.createOWLIndividual(null);
			//OWLIndividual parentComponents = (OWLIndividual)parentConstruct.
			//									getPropertyValue(componentsProperty);
	
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
				sequenceList.insertAtIndex(newPerform, 0);
			}
			// Non-Sequence parent, create a new Sequence parent and put it
			// inside the old parent
			else if (!(parentConstruct.hasRDFType(sequenceCls, true))){
				// Set up the instances
				OWLIndividual sequenceConstruct = sequenceCls.createOWLIndividual(null);
				OWLNamedClass cclistCls = model.getOWLNamedClass(OWLSList.CC_LIST);
				
				// Set up the lists
				OWLSList sequenceList = new OWLSList(sequenceConstruct, model);
				OWLSList parentList = new OWLSList(parentConstruct, model);
				
				// Manipulate the lists
	
				//System.out.println("Removing old perform");
				parentList.removeAtIndex(index, false);

				//System.out.println("Adding sequence");
				parentList.insertAtIndex(sequenceConstruct, index);
	
				//System.out.println("Adding old perform to sequence");
				sequenceList.insertAtIndex(oldPerform, 0);
				
				//System.out.println("Adding new perform to sequence");
				sequenceList.insertAtIndex(newPerform, 0);
			}
			// Parent was already a Sequence
			else{
				OWLSList parentList = new OWLSList(parentConstruct, model);
				//System.out.println("Adding new perform to choice");
				parentList.insertAtIndex(newPerform, index);
			}
		}
		createIncomingDataFlow(mResult, oldPerform, newPerform);
		// We do this last to trigger a tree recreate()
		newPerform.setPropertyValue(processProperty, newProcess);
	}
	
	protected void setupButtonPanel(){
		buttonPanel = new JPanel();
		
		class AddBeforeAction extends AbstractAction{
			public void actionPerformed(ActionEvent e){
				MatchResult mResult = getSelection(); 
				if (mResult != null)
					addBefore(perform, mResult);
			}
		}
		JButton addBeforeButton = new JButton(new AddBeforeAction());
		addBeforeButton.setText("Add before in Sequence");
		buttonPanel.add(addBeforeButton);

		buttonPanel.add(getDetailsButton());
		
		buttonPanel.add(getHelpButton());
	}

}
