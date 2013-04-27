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
package com.sri.owlseditor.cmp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.sri.owlseditor.cmp.graph.GraphPanel;
import com.sri.owlseditor.cmp.tree.CompositionTreePanel;
import com.sri.owlseditor.cmp.tree.OWLSTree;
import com.sri.owlseditor.cmp.tree.OWLSTreeMapper;
import com.sri.owlseditor.cmp.tree.OWLSTreeNode;
import com.sri.owlseditor.cmp.tree.PerformNode;
import com.sri.owlseditor.cmp.tree.ProduceNode;
import com.sri.owlseditor.cmp.tree.RootNode;
import com.sri.owlseditor.cmp.tree.SequenceNode;
import com.sri.owlseditor.util.OWLSList;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueAdapter;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.ResourceDisplay;

/* This class is the "visual editor" for composite processes.
 * It holds the ijtree and its associated buttons, the graphical visualizer,
 * and the control construct properties editor.
 * @author Daniel Elenius <elenius@csl.sri.com>
 */
public class CompositionEditor extends JPanel implements TreeSelectionListener, 
														 GraphUpdateManager {
	private static CompositionEditor instance;
	
	private JSplitPane splitpane;
	private ResourceDisplay ccinstance; 	// The control construct properties editor at the bottom
	private CompositionTreePanel comptree;		// The tree view and its associated buttons
    
    private GraphPanel itsGraphPanel;
    private JScrollPane graphScrollPane;
    private JTabbedPane graphAndPropertiesPane;
    
    private OWLModel okb;
    private OWLIndividual selectedProcess;

	private class GraphPropertyListener extends PropertyValueAdapter{
		public void propertyValueChanged(RDFResource resource, RDFProperty property, 
				 java.util.Collection oldValues){
			if (property.getName().equals("process:process")){
				recreateTree();
			}
			else if (property.getName().equals("process:theVar")){
				updateGraph();
			}
			else if (property.getName().equals("process:fromProcess")){
				updateGraph();
			}
			else if (resource.hasRDFType(conditionCls, true) &&
					property.getName().equals("rdfs:label")){
				updateGraph();
			}
		}
	}

	private class RenameAdapter extends ModelAdapter{

		public void resourceNameChanged(RDFResource resource, String oldName){
			if (resource.hasRDFType(processCls, true) ||
				resource.hasRDFType(produceCls, true) ||
				resource.hasRDFType(performCls, true)){
				//System.out.println("Process or Perform renamed, recreating tree and graph");
				recreateTree();
			}
			else if(resource.hasRDFType(conditionCls, true) ||
					resource.hasRDFType(parameterCls, true)){
				//System.out.println("Condition or Parameter renamed, redrawing graph");
				updateGraph();
			}
		}
	}
	
    private GraphPropertyListener graphPropertyListener;
    private RenameAdapter renameAdapter;

    private OWLSTree currentTree;
    
	private MouseListener ml;
	private JPopupMenu rightClickMenu;
	private JPopupMenu compositeRightClickMenu;
	private OWLSTreeNode rightClickedNode;
	
	private OWLNamedClass performCls;
	private OWLNamedClass produceCls;
	private OWLNamedClass conditionCls;
	private OWLNamedClass parameterCls;
	private OWLNamedClass processCls;
	private OWLNamedClass compositeProcessCls;
	private OWLNamedClass valueOfCls;
	private OWLNamedClass inputCls;
	private OWLNamedClass outputCls;
	private OWLNamedClass inputBindingCls;
	private OWLNamedClass outputBindingCls;
	private OWLNamedClass sequenceCls;

	private OWLObjectProperty composedOfProperty;
	private OWLObjectProperty processProperty;
	private OWLObjectProperty hasDataFrom;
	private OWLObjectProperty valueSource;
	private OWLObjectProperty fromProcess;
	private OWLObjectProperty theVar;
	private OWLObjectProperty hasInput;
	private OWLObjectProperty hasOutput;
	private OWLObjectProperty producedBinding;
	private OWLObjectProperty toParam;
	
	private OWLIndividual theParentPerform;
	
	public CompositionEditor(Project project){
		super(new BorderLayout());
		this.okb = (OWLModel)project.getKnowledgeBase();
		ccinstance = new ResourceDisplay(project);
		comptree = new CompositionTreePanel(okb);
		itsGraphPanel = new GraphPanel(project, this);
		graphAndPropertiesPane = new JTabbedPane();
		graphAndPropertiesPane.addTab("Process graph", itsGraphPanel);
		graphAndPropertiesPane.addTab("Properties", ccinstance);
		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitpane.setLeftComponent(comptree);
		splitpane.setRightComponent(graphAndPropertiesPane);
		add(splitpane, BorderLayout.CENTER);
		
		setupClassesAndProperties();
		
		graphPropertyListener = new GraphPropertyListener();
		renameAdapter = new RenameAdapter();
		
		// Right-click menu (for all construct except composite Performs)
		rightClickMenu = new JPopupMenu();
		JMenuItem wrapAsCompositeItem = new JMenuItem("Wrap as Composite Process"); 
		wrapAsCompositeItem.addActionListener(new ActionListener(){
		 	public void actionPerformed(ActionEvent ae) {
		 		wrapAsComposite();
		 	}
		});
		rightClickMenu.add(wrapAsCompositeItem); 

		// Right-click menu for composite Performs
		compositeRightClickMenu = new JPopupMenu();
		JMenuItem unwrapCompositeItem = new JMenuItem("Unwrap Composite Process"); 
		unwrapCompositeItem.addActionListener(new ActionListener(){
		 	public void actionPerformed(ActionEvent ae) {
		 		unwrapComposite();
		 	}
		});
		compositeRightClickMenu.add(unwrapCompositeItem); 

		ml = new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
				int selRow = currentTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = currentTree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if(e.getClickCount() == 1 &&
					   e.getButton() == MouseEvent.BUTTON3) {
						rightClickedNode = (OWLSTreeNode)selPath.getLastPathComponent();
						OWLIndividual construct = rightClickedNode.getInstance();
						if (construct.hasRDFType(performCls, true)){
							OWLIndividual process = (OWLIndividual)construct.
														getPropertyValue(processProperty);
							if (process != null)
								if (process.hasRDFType(compositeProcessCls, true)){
									compositeRightClickMenu.show(currentTree, (int)e.getX(), (int)e.getY());
									return;
								}
						}
						rightClickMenu.show(currentTree, (int)e.getX(), (int)e.getY());
					}	
				}
			}
		};

		//addListeners();
	}
	
	private void setupClassesAndProperties(){
		performCls = okb.getOWLNamedClass("process:Perform");
		processCls = okb.getOWLNamedClass("process:Process");
		conditionCls = okb.getOWLNamedClass("expr:Condition");
		compositeProcessCls = okb.getOWLNamedClass("process:CompositeProcess");
		parameterCls = okb.getOWLNamedClass("process:Parameter");
		produceCls = okb.getOWLNamedClass("process:Produce");
		valueOfCls = okb.getOWLNamedClass("process:ValueOf");
		inputCls = okb.getOWLNamedClass("process:Input");
		outputCls = okb.getOWLNamedClass("process:Output");
		inputBindingCls = okb.getOWLNamedClass("process:InputBinding");
		outputBindingCls = okb.getOWLNamedClass("process:OutputBinding");
		sequenceCls = okb.getOWLNamedClass("process:Sequence");
		
		processProperty = okb.getOWLObjectProperty("process:process");
		composedOfProperty = okb.getOWLObjectProperty("process:composedOf");
		hasDataFrom = okb.getOWLObjectProperty("process:hasDataFrom");
		valueSource = okb.getOWLObjectProperty("process:valueSource");
		fromProcess = okb.getOWLObjectProperty("process:fromProcess");
		theVar = okb.getOWLObjectProperty("process:theVar");
		hasInput = okb.getOWLObjectProperty("process:hasInput");
		hasOutput = okb.getOWLObjectProperty("process:hasOutput");
		producedBinding = okb.getOWLObjectProperty("process:producedBinding");
		toParam = okb.getOWLObjectProperty("process:toParam");
		
		theParentPerform = okb.getOWLIndividual("process:TheParentPerform");
	}
	
	private OWLSTreeNode getNodeForPerform(OWLSTreeNode root, OWLIndividual perform){
		if (root instanceof PerformNode){
			OWLIndividual construct = root.getInstance();
			if (construct == perform)
				return root;
		}
		else{
			int c = root.getChildCount();
			for (int i=0; i<c; i++){
				OWLSTreeNode child = (OWLSTreeNode)root.getChildAt(i);
				OWLSTreeNode foundPerform = getNodeForPerform(child, perform);
				if (foundPerform != null)
					return foundPerform;
			}
		}
		return null;
	}

	private OWLSTreeNode getNodeForProduce(OWLSTreeNode root, OWLIndividual produce){
		if (root instanceof ProduceNode){
			OWLIndividual construct = root.getInstance();
			if (construct == produce)
				return root;
		}
		else{
			int c = root.getChildCount();
			for (int i=0; i<c; i++){
				OWLSTreeNode child = (OWLSTreeNode)root.getChildAt(i);
				OWLSTreeNode foundProduce = getNodeForProduce(child, produce);
				if (foundProduce != null)
					return foundProduce;
			}
		}
		return null;
	}
	
	/* Populates the set with all the performs in the tree
	 * below the node.
	 */
	private Set getPerformsInSubtree(OWLSTreeNode node){
		HashSet returnSet = new HashSet();
		
		int c = node.getChildCount();
		for (int i=0; i<c; i++){
			OWLSTreeNode child = (OWLSTreeNode)node.getChildAt(i);
			returnSet.addAll(getPerformsInSubtree(child));
		}

		if (node instanceof PerformNode){
			OWLIndividual perform = node.getInstance();
			returnSet.add(perform);
		}
		
		return returnSet;
	}

	/* Populates the set with all the produces in the tree
	 * below the node.
	 */
	private Set getProducesInSubtree(OWLSTreeNode node){
		HashSet returnSet = new HashSet();
		
		int c = node.getChildCount();
		for (int i=0; i<c; i++){
			OWLSTreeNode child = (OWLSTreeNode)node.getChildAt(i);
			returnSet.addAll(getProducesInSubtree(child));
		}

		if (node instanceof ProduceNode){
			OWLIndividual produce = node.getInstance();
			returnSet.add(produce);
		}
		
		return returnSet;
	}

	private void wrapFixIncomingDataflow(OWLIndividual valueOf, 
									 OWLIndividual originalSourcePerform,
									 OWLIndividual originalSourceParam,
									 OWLIndividual compositeProcess, 
									 OWLIndividual compositePerform){
		// Create a new input and add it to the composite process		
		OWLIndividual newInput = (OWLIndividual)inputCls.createInstance(null);
		compositeProcess.addPropertyValue(hasInput, newInput);
		
		// Change the dataflow to point to the parent perform input
		valueOf.setPropertyValue(fromProcess, theParentPerform);
		valueOf.setPropertyValue(theVar, newInput);
		
		// Add a new dataflow from the original source perform to the new composite perform
		OWLIndividual newInputBinding = (OWLIndividual)inputBindingCls.createInstance(null);
		newInputBinding.setPropertyValue(toParam, newInput);
		OWLIndividual newValueOf = (OWLIndividual)valueOfCls.createInstance(null);
		newValueOf.setPropertyValue(fromProcess, originalSourcePerform);
		newValueOf.setPropertyValue(theVar, originalSourceParam);
		newInputBinding.setPropertyValue(valueSource, newValueOf);
		compositePerform.addPropertyValue(hasDataFrom, newInputBinding);
	}
	
	/* Checks all dataflow going *to* nodes in the tree below the node. If the source is now
	 * no longer a valid predecessor (i.e. outside the subtree), the following happens:
	 * 1) A corresponding Input is added to the new compositeProcess.
	 * 2) Dataflow is added from the original source to the new Input on compositeProcess
	 * 3) Dataflow is added from the new Input on the compositeProcess to the original target. 
	 */
	private void wrapUpdateIncomingDataflow(OWLSTreeNode node, 
										OWLIndividual compositeProcess,
										OWLIndividual compositePerform,
										Set subtreePerforms,
										Set outsidePerforms){
		Iterator it = subtreePerforms.iterator();
		while (it.hasNext()){
			OWLIndividual subtreePerform = (OWLIndividual)it.next();
			Collection inputBindings = subtreePerform.getPropertyValues(hasDataFrom);
			Iterator it2 = inputBindings.iterator();
			while (it2.hasNext()){
				OWLIndividual inputBinding = (OWLIndividual)it2.next();
				OWLIndividual valueOf = (OWLIndividual)inputBinding.getPropertyValue(valueSource);
				if (valueOf != null){
					OWLIndividual fromPerform = (OWLIndividual)valueOf.getPropertyValue(fromProcess);
					if (outsidePerforms.contains(fromPerform)){
						OWLIndividual fromParam = (OWLIndividual)valueOf.getPropertyValue(theVar);
						wrapFixIncomingDataflow(valueOf, fromPerform, fromParam, 
											compositeProcess, compositePerform);
					}
				}
			}
		}
	}

	/* Fix a specific dataflow coming out of the subtree */
	private void wrapFixOutgoingDataflow(OWLIndividual valueOf, 
			 					     OWLIndividual originalSourcePerform,
									 OWLIndividual originalSourceParam,
									 OWLIndividual compositeProcess, 
									 OWLIndividual compositePerform,
									 OWLSTreeNode node){
		// Create a new output and add it to the composite process		
		OWLIndividual newOutput = (OWLIndividual)outputCls.createInstance(null);
		compositeProcess.addPropertyValue(hasOutput, newOutput);
		
		// Change the dataflow source to point to the new composite perform output
		valueOf.setPropertyValue(fromProcess, compositePerform);
		valueOf.setPropertyValue(theVar, newOutput);
		
		// Add a new dataflow from the original source perform to the new composite perform,
		// using Produce
		OWLIndividual newOutputBinding = (OWLIndividual)outputBindingCls.createInstance(null);
		newOutputBinding.setPropertyValue(toParam, newOutput);
		OWLIndividual newValueOf = (OWLIndividual)valueOfCls.createInstance(null);
		newOutputBinding.setPropertyValue(valueSource, newValueOf);
		newValueOf.setPropertyValue(fromProcess, originalSourcePerform);
		newValueOf.setPropertyValue(theVar, originalSourceParam);
		
		OWLIndividual produce = (OWLIndividual)produceCls.createInstance(null);
		produce.setPropertyValue(producedBinding, newOutputBinding);
		
		// Add the Produce to the (subtree) composite process
		OWLSTree subtree = OWLSTreeMapper.getInstance(okb).getTree(compositeProcess, this);
		OWLSTreeNode sourceNode = getNodeForPerform(node, originalSourcePerform);
		OWLSTreeNode parent = (OWLSTreeNode)sourceNode.getParent();
		OWLIndividual parentConstruct = parent.getInstance();
		int index = parent.getIndex(sourceNode);
		OWLSList parentList = new OWLSList(parentConstruct, okb);

		if (parent instanceof SequenceNode){
			parentList.insertAtIndex(produce, index+1);
		}
		else{
			// If the parent is not a sequence, one will be added
			parentList.removeAtIndex(index, false);
			OWLIndividual sequence = (OWLIndividual)sequenceCls.createInstance(null);
			OWLSList sequenceList = new OWLSList(sequence, okb);
			sequenceList.insertAtIndex(originalSourcePerform, 0);
			sequenceList.insertAtIndex(produce, 1);
			parentList.insertAtIndex(sequence, index);
		}
	}

	/* Checks all dataflow going *from* nodes in the tree below the node. The following things
	 * happen for such bindings:
	 * 1) A corresponding Output is added to the new compositeProcess
	 * 2) The source of the dataflow is changed to the new output on the composite process
	 * 3) A Produce construct is added to the compositeProcess to bind data to the new output.
	 */
	private void wrapUpdateOutgoingDataflow(OWLSTreeNode node, 
										OWLIndividual compositeProcess,
										OWLIndividual compositePerform,
										Set subtreePerforms,
										Set outsidePerforms){
		Iterator it = outsidePerforms.iterator();
		while (it.hasNext()){
			OWLIndividual outsidePerform = (OWLIndividual)it.next();
			Collection inputBindings = outsidePerform.getPropertyValues(hasDataFrom);
			Iterator it2 = inputBindings.iterator();
			while (it2.hasNext()){
				OWLIndividual inputBinding = (OWLIndividual)it2.next();
				OWLIndividual valueOf = (OWLIndividual)inputBinding.getPropertyValue(valueSource);
				if (valueOf != null){
					OWLIndividual fromPerform = (OWLIndividual)valueOf.getPropertyValue(fromProcess);
					if (subtreePerforms.contains(fromPerform)){
						OWLIndividual fromParam = (OWLIndividual)valueOf.getPropertyValue(theVar);
						wrapFixOutgoingDataflow(valueOf, fromPerform, fromParam,
											compositeProcess, compositePerform, node);
					}
				}
			}
		}
	}

	private void unwrapFixIncomingDataflow(OWLIndividual valueOf, 
										   OWLIndividual originalSourceParam,
										   OWLIndividual compositeProcess, 
										   OWLIndividual compositePerform){
		Collection compositeInputs = inputCls.getInstances(true);
		Iterator it = compositeInputs.iterator();
		while (it.hasNext()){
			OWLIndividual input = (OWLIndividual)it.next();
			if (input == originalSourceParam){
				Collection inputBindings = compositePerform.getPropertyValues(hasDataFrom);
				Iterator it2 = inputBindings.iterator();
				while (it2.hasNext()){
					OWLIndividual inputBinding = (OWLIndividual)it2.next();
					OWLIndividual compositeValueOf = (OWLIndividual)inputBinding.
														getPropertyValue(valueSource);
					OWLIndividual fromPerform = (OWLIndividual)compositeValueOf.
													getPropertyValue(fromProcess);
					OWLIndividual fromParam = (OWLIndividual)compositeValueOf.
												getPropertyValue(theVar);
					
					// Change the original valueOf to point to the source
					// of this dataflow
					valueOf.setPropertyValue(fromProcess, fromPerform);
					valueOf.setPropertyValue(theVar, fromParam);
					
					// Delete the composite Input, ValueOf, and InputBinding
					compositeValueOf.delete();
					input.delete();
					inputBinding.delete();
					
					return;
				}
			}
		}

		// No input binding found, so we reset the ValueOf 
		// TODO: Ideally, we should remove the input binding
		valueOf.setPropertyValue(fromProcess, null);
		valueOf.setPropertyValue(theVar, null);
	}

	private void unwrapUpdateIncomingDataflow(OWLSTreeNode node, 
											  OWLIndividual compositeProcess,
											  OWLIndividual compositePerform,
											  Set subtreePerforms,
											  Set outsidePerforms){
		Iterator it = subtreePerforms.iterator();
		while (it.hasNext()){
			OWLIndividual subtreePerform = (OWLIndividual)it.next();
			Collection inputBindings = subtreePerform.getPropertyValues(hasDataFrom);
			Iterator it2 = inputBindings.iterator();
			while (it2.hasNext()){
				OWLIndividual inputBinding = (OWLIndividual)it2.next();
				OWLIndividual valueOf = (OWLIndividual)inputBinding.getPropertyValue(valueSource);
				if (valueOf != null){
					OWLIndividual fromPerform = (OWLIndividual)valueOf.getPropertyValue(fromProcess);
					if (fromPerform == theParentPerform){
						OWLIndividual fromParam = (OWLIndividual)valueOf.getPropertyValue(theVar);
						unwrapFixIncomingDataflow(valueOf, fromParam, 
												  compositeProcess, compositePerform);
					}
				}
			}
		}
	}

	/* Fix a specific dataflow coming out of the subtree */
	private void unwrapFixOutgoingDataflow(OWLIndividual valueOf, 
									 	   OWLIndividual compositeParam,
										   OWLSTreeNode node,
										   Collection produces){
		// We have to find the OutputBinding that generates this Output
		Iterator it = produces.iterator();
		while (it.hasNext()){
			OWLIndividual produce = (OWLIndividual)it.next();
			OWLIndividual outputBinding = (OWLIndividual)produce.getPropertyValue(producedBinding);
			OWLIndividual produceToParam = (OWLIndividual)outputBinding.getPropertyValue(toParam);
			if (produceToParam == compositeParam){
				// This Produce binds to the composite Output that we're looking at
				OWLIndividual produceValueOf = (OWLIndividual)outputBinding.getPropertyValue(valueSource);
				OWLIndividual produceFromParam = (OWLIndividual)produceValueOf.getPropertyValue(theVar);
				OWLIndividual produceFromPerform = (OWLIndividual)produceValueOf.getPropertyValue(fromProcess);
				
				// Change the source of the binding to the source of the Produce
				valueOf.setPropertyValue(fromProcess, produceFromPerform);
				valueOf.setPropertyValue(theVar, produceFromParam);
				
				// Delete the composite Output, the Produce, and the Produce's OutputBinding
				// and ValueOf
				OWLSTreeNode produceNode = getNodeForProduce(node, produce);
				OWLSTreeNode parent = (OWLSTreeNode)produceNode.getParent();
				OWLIndividual parentConstruct = parent.getInstance();
				int index = parent.getIndex(produceNode);
				OWLSList parentList = new OWLSList(parentConstruct, okb);
				parentList.removeAtIndex(index, true);
				
				outputBinding.delete();
				produceValueOf.delete();
				produceToParam.delete();
				
				return;
			}
		}
		
		// No Produce found, so we reset the ValueOf 
		// TODO: Ideally, we should remove the input binding
		valueOf.setPropertyValue(fromProcess, null);
		valueOf.setPropertyValue(theVar, null);
	}

	private void unwrapUpdateOutgoingDataflow(OWLSTreeNode node, 
											  OWLIndividual compositeProcess,
											  OWLIndividual compositePerform,
											  Set subtreePerforms,
											  Set outsidePerforms){
		Collection produces = getProducesInSubtree(node);
		Iterator it = outsidePerforms.iterator();
		while (it.hasNext()){
			OWLIndividual outsidePerform = (OWLIndividual)it.next();
			Collection inputBindings = outsidePerform.getPropertyValues(hasDataFrom);
			Iterator it2 = inputBindings.iterator();
			while (it2.hasNext()){
				OWLIndividual inputBinding = (OWLIndividual)it2.next();
				OWLIndividual valueOf = (OWLIndividual)inputBinding.getPropertyValue(valueSource);
				if (valueOf != null){
					OWLIndividual fromPerform = (OWLIndividual)valueOf.getPropertyValue(fromProcess);
					if (fromPerform == compositePerform){
						OWLIndividual fromParam = (OWLIndividual)valueOf.getPropertyValue(theVar);
						unwrapFixOutgoingDataflow(valueOf, fromParam, node, produces);
					}
				}
			}
		}
	}

	private void wrapAsComposite(){
 		removeListeners();
 		
 		OWLIndividual newPerform = (OWLIndividual)performCls.createInstance(null);
 		OWLIndividual newComposite = (OWLIndividual)compositeProcessCls.createInstance(null);
 		newPerform.setPropertyValue(processProperty, newComposite);
 		OWLIndividual clickedConstruct = rightClickedNode.getInstance();
 		newComposite.setPropertyValue(composedOfProperty, clickedConstruct);

 		OWLSTreeNode parent = (OWLSTreeNode)rightClickedNode.getParent();
 		if (parent instanceof RootNode){
 			selectedProcess.setPropertyValue(composedOfProperty, newPerform);
 		}
 		else{
 	 		OWLIndividual parentConstruct = parent.getInstance();
 	 		OWLSList parentList = new OWLSList(parentConstruct, okb);
 	 		int index = parent.getIndex(rightClickedNode);
 	 		parentList.removeAtIndex(index, false);
 	 		parentList.insertAtIndex(newPerform, index);

 	 		Set subtreePerforms = getPerformsInSubtree(rightClickedNode);
 	 		OWLSTreeNode rootNode = (OWLSTreeNode)rightClickedNode.getRoot();
 	 		Set outsidePerforms = getPerformsInSubtree(rootNode);
 	 		outsidePerforms.removeAll(subtreePerforms);
 	 		wrapUpdateIncomingDataflow(rightClickedNode,
 	 							   newComposite,
								   newPerform,
								   subtreePerforms,
								   outsidePerforms);
 	 		OWLSTreeMapper.getInstance(okb).removeTree(newComposite);
 	 		wrapUpdateOutgoingDataflow(rightClickedNode,
					   			   newComposite,
								   newPerform,
								   subtreePerforms,
								   outsidePerforms);
 	 		OWLSTreeMapper.getInstance(okb).removeTree(newComposite);
 		}
 		addListeners();
 		recreateTree();
	}

	/* Note: this deletes the composite sub-process. */
	private void unwrapComposite(){
		removeListeners();
		
		OWLIndividual clickedPerform = rightClickedNode.getInstance();
		OWLIndividual clickedComposite = (OWLIndividual)clickedPerform.
											getPropertyValue(processProperty);
		OWLIndividual compositeConstruct = (OWLIndividual)clickedComposite.
											getPropertyValue(composedOfProperty);
		OWLSTree subTree = OWLSTreeMapper.getInstance(okb).getTree(clickedComposite, this);

		OWLSTreeNode parent = (OWLSTreeNode)rightClickedNode.getParent();
 		if (parent instanceof RootNode){
 			selectedProcess.setPropertyValue(composedOfProperty, compositeConstruct);
 		}
 		else{
 	 		OWLIndividual parentConstruct = parent.getInstance();
 	 		OWLSList parentList = new OWLSList(parentConstruct, okb);
 	 		int index = parent.getIndex(rightClickedNode);
 	 		parentList.removeAtIndex(index, false);
 	 		parentList.insertAtIndex(compositeConstruct, index);

 	 		OWLSTreeNode rootNode = (OWLSTreeNode)rightClickedNode.getRoot();
 	 		OWLSTreeNode subtreeRoot = (OWLSTreeNode)subTree.getRoot();
 	 		Set subtreePerforms = getPerformsInSubtree(subtreeRoot);
 	 		Set outsidePerforms = getPerformsInSubtree(rootNode);
 	 		outsidePerforms.removeAll(subtreePerforms);
 	 		unwrapUpdateIncomingDataflow(subtreeRoot,
 	 							         clickedComposite,
										 clickedPerform,
										 subtreePerforms,
										 outsidePerforms);
 	 		OWLSTreeMapper.getInstance(okb).removeTree(clickedComposite);
 	 		unwrapUpdateOutgoingDataflow(subtreeRoot,
					   			       	 clickedComposite,
										 clickedPerform,
										 subtreePerforms,
										 outsidePerforms);
 	 		OWLSTreeMapper.getInstance(okb).removeTree(clickedComposite);
 		}
		
 		clickedComposite.delete();
		
		addListeners();
		recreateTree();
	}
	
	
	/** Called when the selected composite process changes */
	public void setInstance(OWLIndividual inst){
		selectedProcess = inst;
		
		OWLSTree newTree = OWLSTreeMapper.getInstance(okb).getTree(inst, this);
		removeListeners();
		comptree.setTree(newTree);
		currentTree = newTree;
		addListeners();
		
		itsGraphPanel.setSelectedProcess(inst);
		// When the composite process selection is changed, the root is
		// selected. Ideally, we should remember node selection for each
		// composite process.
		itsGraphPanel.viewSelectedCompositeProcess((OWLSTreeNode)currentTree.getModel().getRoot());
	}
	
	/** This is called whenever the tree selection changes. We need to
	 * update the ccinstance pane, and gray out buttons in some cases. */ 
	public void valueChanged(TreeSelectionEvent e){
		TreePath path = e.getNewLeadSelectionPath();
		if (path == null){
			// No selection
			//System.out.println("No Selection");
			ccinstance.clearSelection();
		}
		else{
			OWLSTreeNode o = (OWLSTreeNode)path.getLastPathComponent();
			if (o instanceof RootNode){
				ccinstance.clearSelection();
				comptree.enableCreateButtons();
				comptree.disableDeleteButton();
			}
			else{
				ccinstance.setInstance(o.getInstance());
				if (!o.acceptsChild()){
					comptree.disableCreateButtons();
				}
				else{
					comptree.enableCreateButtons();
				}
				comptree.enableDeleteButton();
				// Update the graph
			}
			itsGraphPanel.viewSelectedCompositeProcess(o);
		}
	}
	
	/** This should be called after the tree has been changed, to make sure the
	 * graph gets updated.
	 */
	public void updateGraph(){
		// For some reason, we get one event with tree = null, and one correct
		// event.
		if (currentTree != null){
			OWLSTreeNode root = currentTree.getRoot();
			// Similarly, we (sometimes?) get two events, one of which returns a
			// DefaultMutableTreeNode as root(!), and the other (correctly) the
			// RootNode.
			if (root instanceof RootNode)
				itsGraphPanel.viewSelectedCompositeProcess((OWLSTreeNode)root);
		}
		currentTree.repaint();
	}
	
	/* This is needed when the processes of performs change, in order to make
	 * the width of the tree cells adjust.
	 */
	public void recreateTree(){
		// First remove the old tree
		OWLSTreeMapper.getInstance(okb).removeTree(selectedProcess);
		// Then make sure a new one gets created
		setInstance(selectedProcess);
		// Now update the graph normally
		updateGraph();
	}
	
	/* These listeners make sure the graph is redrawn when something that
	 * it depends on changes.
	 */
	public void addListeners(){
		// This needs to be disabled during  node deletion...
		okb.addPropertyValueListener(graphPropertyListener);
		okb.addModelListener(renameAdapter);
		
		if (currentTree != null){
			currentTree.addTreeSelectionListener(this);
			currentTree.addMouseListener(ml);
		}
	}
	
	public void removeListeners(){
		okb.removePropertyValueListener(graphPropertyListener);
		okb.removeModelListener(renameAdapter);

		if (currentTree != null){
			currentTree.removeTreeSelectionListener(this);
			currentTree.removeMouseListener(ml);
		}
	}
	
}