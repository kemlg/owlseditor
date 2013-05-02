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

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/**
 * @author Daniel Elenius
 */
public class MatchingProcessFinder extends MatchFinder {

	private static MatchingProcessFinder instance;
	private GraphUpdateManager mgr;

	private MatchingProcessFinder(GraphUpdateManager mgr) {
		super("Find Matching Processes");
		this.mgr = mgr;
	}

	public static MatchingProcessFinder getInstance(GraphUpdateManager mgr) {
		if (instance == null)
			instance = new MatchingProcessFinder(mgr);
		return instance;
	}

	public List getMatches(OWLIndividual perform) {
		Matchmaker matchMaker = Matchmaker.getInstance(perform.getOWLModel());
		return matchMaker.findMatchingProcesses(perform);
	}

	/**
	 * Creates a new Perform instances with copies of the dataflow in the old
	 * one, in new InputBinding and ValueOf instances. This can then be sent to
	 * updateDataFlow without changing the original perform.
	 */
	private OWLIndividual copyPerform(OWLIndividual oldPerform) {
		OWLIndividual newPerform = (OWLIndividual) performCls
				.createInstance(null);
		Iterator it = oldPerform.getPropertyValues(hasDataFrom).iterator();
		while (it.hasNext()) {
			OWLIndividual oldInputBinding = (OWLIndividual) it.next();
			OWLIndividual oldToParam = (OWLIndividual) oldInputBinding
					.getPropertyValue(toParam);
			OWLIndividual oldValueOf = (OWLIndividual) oldInputBinding
					.getPropertyValue(valueSource);
			OWLIndividual oldFromPerform = (OWLIndividual) oldValueOf
					.getPropertyValue(fromProcess);
			OWLIndividual oldFromParam = (OWLIndividual) oldValueOf
					.getPropertyValue(theVar);

			OWLIndividual newInputBinding = (OWLIndividual) inputBindingCls
					.createInstance(null);
			newPerform.addPropertyValue(hasDataFrom, newInputBinding);
			newInputBinding.setPropertyValue(toParam, oldToParam);
			OWLIndividual newValueOf = (OWLIndividual) valueOfCls
					.createInstance(null);
			newValueOf.setPropertyValue(fromProcess, oldFromPerform);
			newValueOf.setPropertyValue(theVar, oldFromParam);
			newInputBinding.setPropertyValue(valueSource, newValueOf);
		}
		return newPerform;
	}

	/** Changes the dataflow declarations in the given perform */
	protected void updateIncomingDataFlow(MatchResult mResult,
			OWLIndividual theperform) {
		// System.out.println("Updating dataflow of " + theperform.getName());

		// Look at incoming the dataflow of the perform
		Collection dataBindings = theperform.getPropertyValues(hasDataFrom);
		Iterator it = dataBindings.iterator();
		while (it.hasNext()) {
			OWLIndividual inputBinding = (OWLIndividual) it.next();
			OWLIndividual targetParam = (OWLIndividual) inputBinding
					.getPropertyValue(toParam);
			// Remove the old target parameter and put in the new one
			// if there was a match for it.
			MatchPair pair = mResult.getInputMatch(targetParam);
			if (pair != null) {
				if (pair.getMatchType() != Matchmaker.FAIL) {
					inputBinding.removePropertyValue(toParam, targetParam);
					inputBinding.setPropertyValue(toParam,
							model.getRDFResource(pair.getNewParameter()));
				} else {
					// There is no new binding to replace this old one, so we
					// delete it
					OWLIndividual valueOf = (OWLIndividual) inputBinding
							.getPropertyValue(valueSource);
					valueOf.delete();
					inputBinding.delete();
				}
			} else
				System.out
						.println("ERROR! MatchingProcessFinder.java: Target parameter "
								+ targetParam.getName() + " not accounted for!");
		}
	}

	/**
	 * Outgoing dataflow from the Perform. Any InputBinding with this perform as
	 * its source must be updated. If mResult is null, the outgoing dataflow
	 * will simply be removed.
	 */
	protected void updateOutgoingDataFlow(MatchResult mResult,
			OWLIndividual theperform) {
		Collection bindings = bindingCls.getInstances(true);
		Iterator it = bindings.iterator();
		while (it.hasNext()) {
			OWLIndividual binding = (OWLIndividual) it.next();
			OWLIndividual valueOf = (OWLIndividual) binding
					.getPropertyValue(valueSource);
			if (valueOf != null) {
				OWLIndividual fromPerform = (OWLIndividual) valueOf
						.getPropertyValue(fromProcess);
				OWLIndividual fromParam = (OWLIndividual) valueOf
						.getPropertyValue(theVar);

				if (fromPerform == theperform) {
					// This binding has this perform as its source
					if (mResult != null) {
						// Remove the old target parameter and put in the new
						// one
						// if there was a match for it.
						MatchPair pair = mResult.getOutputMatch(fromParam);
						valueOf.removePropertyValue(theVar, fromParam);
						if (pair != null) {
							OWLIndividual newParam = (OWLIndividual) model
									.getRDFResource(pair.getNewParameter());
							valueOf.setPropertyValue(theVar, newParam);
						}
						// else
						// System.out.println("No match for " +
						// fromParam.getName());
					} else {
						// Just remove the dataflow (the source is no longer an
						// unconditional predecessor perform)
						valueOf.delete();
						binding.delete();
					}
				}
			}
		}
	}

	/** Adds the given process as a choice to the original parent */
	private void addAsChoice(OWLIndividual oldPerform, MatchResult mResult) {
		OWLIndividual newProcess = mResult.getProcess();

		// System.out.println("Adding " + newProcess.getName() +
		// " as Choice to " + process.getName());
		OWLModel model = newProcess.getOWLModel();
		OWLSTree tree = OWLSTreeMapper.getInstance(model).getTree(parent, mgr);

		OWLSTreeNode oldPerformNode = getPerformNode(tree.getRoot());
		OWLSTreeNode parentNode = (OWLSTreeNode) oldPerformNode.getParent();
		DefaultTreeModel treemodel = (DefaultTreeModel) tree.getModel();
		int index = treemodel.getIndexOfChild(parentNode, oldPerformNode);
		OWLIndividual parentConstruct = parentNode.getInstance();

		// Remove the tree before we start messing around with the performs.
		// Otherwise we will trigger some unnecessary events.
		// The tree will be recreated by event handlers anyway.
		//
		// NOTE: After this, we can no longer use the tree or the nodes!
		// OWLSTreeMapper.getInstance(model).removeTree(parent);

		OWLIndividual newPerform = copyPerform(oldPerform);
		updateIncomingDataFlow(mResult, newPerform);
		updateOutgoingDataFlow(null, newPerform);
		updateOutgoingDataFlow(null, oldPerform);

		// Old perform is root construct
		if (parentConstruct == null) {
			// Set up the instances
			OWLIndividual choiceConstruct = choiceCls.createOWLIndividual(null);
			OWLNamedClass ccbagCls = model.getOWLNamedClass(OWLSList.CC_BAG);

			// Make the sequence the main construct of the composite process
			parent.removePropertyValue(composedOfProperty, oldPerform);
			parent.setPropertyValue(composedOfProperty, choiceConstruct);

			// Set up the list
			OWLSList sequenceList = new OWLSList(choiceConstruct, model);

			// Manipulate the lists

			// System.out.println("Adding old perform to sequence");
			sequenceList.insertAtIndex(oldPerform, 0);

			// System.out.println("Adding new perform to sequence");
			sequenceList.insertAtIndex(newPerform, 1);
		}
		// If the parent isn't already a Choice, we create a new Choice parent
		else if (!(parentConstruct.hasRDFType(choiceCls, true))) {
			// Set up the instances
			OWLIndividual choiceConstruct = choiceCls.createOWLIndividual(null);
			OWLNamedClass ccbagCls = model.getOWLNamedClass(OWLSList.CC_BAG);

			// Set up the lists
			OWLSList choiceList = new OWLSList(choiceConstruct, model);
			OWLSList parentList = new OWLSList(parentConstruct, model);

			// Manipulate the lists
			// System.out.println("Removing old perform");
			parentList.removeAtIndex(index, false); // we keep the perform
													// instance

			// System.out.println("Adding choice");
			parentList.insertAtIndex(choiceConstruct, index);

			// System.out.println("Adding old perform to choice");
			choiceList.insertAtIndex(oldPerform, 0);

			// System.out.println("Adding new perform to choice");
			choiceList.insertAtIndex(newPerform, 0);
		} else {
			OWLSList parentList = new OWLSList(parentConstruct, model);
			// System.out.println("Adding new perform to choice");
			parentList.insertAtIndex(newPerform, 0);
		}

		// We do this last to trigger a tree recreate()
		newPerform.setPropertyValue(processProperty, newProcess);
	}

	/** Substitutes the given process for the original process */
	private void replaceProcess(OWLIndividual theperform, MatchResult mResult) {
		OWLIndividual newProcess = mResult.getProcess();

		updateIncomingDataFlow(mResult, theperform);
		updateOutgoingDataFlow(mResult, theperform);
		// this will also cause the graph to be redrawn
		theperform.setPropertyValue(processProperty, newProcess);
	}

	protected void setupButtonPanel() {
		buttonPanel = new JPanel();

		class AddChoiceAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				MatchResult mResult = getSelection();
				if (mResult != null)
					addAsChoice(perform, mResult);
			}
		}
		JButton addChoiceButton = new JButton(new AddChoiceAction());
		addChoiceButton.setText("Add as Choice");
		buttonPanel.add(addChoiceButton);

		class ReplaceAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				MatchResult mResult = getSelection();
				if (mResult != null)
					replaceProcess(perform, mResult);
			}
		}
		JButton replaceButton = new JButton(new ReplaceAction());
		replaceButton.setText("Replace original");
		buttonPanel.add(replaceButton);

		buttonPanel.add(getDetailsButton());

		buttonPanel.add(getHelpButton());
	}

}
