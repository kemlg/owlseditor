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
package com.sri.owlseditor.cmp.tree;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/* This class is the panel containing the tree where composite processes are edited, and 
 * its associated buttons.
 * @author Daniel Elenius <elenius@csl.sri.com>
 *
 */
public class CompositionTreePanel extends JPanel{
	
	private OWLSTree currentTree = null;
	private JToolBar buttonpanel;
	private JScrollPane treescrollpane;
	
	// The buttons for all the control constructs
	private JButton performButton;
	private JButton sequenceButton;
	private JButton splitButton;
	private JButton splitjoinButton;
	private JButton anyorderButton;
	private JButton choiceButton;
	private JButton ifthenelseButton;
	private JButton repeatuntilButton;
	private JButton repeatwhileButton;
	private JButton deleteButton;
	private JButton produceButton;
	
	// The actions for all the buttons
	private AbstractAction performAction;
	private AbstractAction sequenceAction;
	private AbstractAction splitAction;
	private AbstractAction splitjoinAction;
	private AbstractAction anyorderAction;
	private AbstractAction choiceAction;
	private AbstractAction ifthenelseAction;
	private AbstractAction repeatuntilAction;
	private AbstractAction repeatwhileAction;
	private AbstractAction deleteAction;
	private AbstractAction produceAction;

	public CompositionTreePanel(OWLModel model){
		// Set up the buttons to create the control constructs, with
		// their action handlers, icons, and tool tips.
		buttonpanel = ComponentFactory.createToolBar();
		setupButtons();
		
		// Set up the tree
		treescrollpane = new JScrollPane();
				
		// Add button panel and tree to this tree panel
		setLayout(new BorderLayout());
		add(treescrollpane, BorderLayout.CENTER);
		add(buttonpanel, BorderLayout.NORTH);
	}

	public void disableDeleteButton(){
		deleteButton.setEnabled(false);
	}

	public void enableDeleteButton(){
		deleteButton.setEnabled(true);
	}
	
	public void disableCreateButtons(){
		performAction.setEnabled(false);
		sequenceAction.setEnabled(false);
		splitAction.setEnabled(false);
		splitjoinAction.setEnabled(false);
		anyorderAction.setEnabled(false);
		choiceAction.setEnabled(false);
		ifthenelseAction.setEnabled(false);
		repeatuntilAction.setEnabled(false);
		repeatwhileAction.setEnabled(false);
		produceAction.setEnabled(false);
	}
	
	public void enableCreateButtons(){
		performAction.setEnabled(true);
		sequenceAction.setEnabled(true);
		splitAction.setEnabled(true);
		splitjoinAction.setEnabled(true);
		anyorderAction.setEnabled(true);
		choiceAction.setEnabled(true);
		ifthenelseAction.setEnabled(true);
		repeatuntilAction.setEnabled(true);
		repeatwhileAction.setEnabled(true);
		produceAction.setEnabled(true);
	}

	/** Changes the contents of the tree depending on which composite process is chosen */
	public void setTree(OWLSTree newTree){
		if (currentTree != null){
			treescrollpane.remove(currentTree);
		}
		newTree.selectRoot();
		treescrollpane.setViewportView(newTree);
		currentTree = newTree;
	}
	
	/** Set up the buttons to create the control constructs, with
	 * their action handlers, icons, and tool tips.
	 *	Note: Protege uses this style:
	 *  createAction.putValue(Action.SMALL_ICON, OWLIcons.getCreateIcon("Individual"));
	 *  and probably something similar for tooltips. We should maybe change to that.
	 */
	private void setupButtons(){
		class PerformAction extends AbstractAction{
			PerformAction(){
				super("", OWLSIcons.getPerformIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Perform");
			}
		}
		performAction = new PerformAction();
		performButton = buttonpanel.add(performAction);
		performButton.setToolTipText("Create Perform");
				
		class SequenceAction extends AbstractAction{
			SequenceAction(){
				super("", OWLSIcons.getSequenceIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Sequence");
			}
		}
		sequenceAction = new SequenceAction();
		sequenceButton = buttonpanel.add(sequenceAction);
		sequenceButton.setToolTipText("Create Sequence");
		
		class SplitAction extends AbstractAction{
			SplitAction(){
				super("", OWLSIcons.getSplitIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Split");
			}
		}
		splitAction = new SplitAction();
		splitButton = buttonpanel.add(splitAction);
		splitButton.setToolTipText("Create Split");
		
		class SplitJoinAction extends AbstractAction{
			SplitJoinAction(){
				super("", OWLSIcons.getSplitJoinIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Split-Join");
			}
		}
		splitjoinAction = new SplitJoinAction();
		splitjoinButton = buttonpanel.add(splitjoinAction);
		splitjoinButton.setToolTipText("Create Split+Join");
		
		class AnyOrderAction extends AbstractAction{
			AnyOrderAction(){
				super("", OWLSIcons.getAnyOrderIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Any-Order");
			}
		}
		anyorderAction = new AnyOrderAction();
		anyorderButton = buttonpanel.add(anyorderAction);
		anyorderButton.setToolTipText("Create Any-Order");
		
		class ChoiceAction extends AbstractAction{
			ChoiceAction(){
				super("", OWLSIcons.getChoiceIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Choice");
			}
		}
		choiceAction = new ChoiceAction();
		choiceButton = buttonpanel.add(choiceAction);
		choiceButton.setToolTipText("Create Choice");
		
		class IfThenElseAction extends AbstractAction{
			IfThenElseAction(){
				super("", OWLSIcons.getIfThenElseIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:If-Then-Else");
			}
		}
		ifthenelseAction = new IfThenElseAction();
		ifthenelseButton = buttonpanel.add(ifthenelseAction);
		ifthenelseButton.setToolTipText("Create If-Then-Else");
		
		class RepeatUntilAction extends AbstractAction{
			RepeatUntilAction(){
				super("", OWLSIcons.getRepeatUntilIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Repeat-Until");
			}
		}
		repeatuntilAction = new RepeatUntilAction();
		repeatuntilButton = buttonpanel.add(repeatuntilAction);
		repeatuntilButton.setToolTipText("Create Repeat-Until");
		
		class RepeatWhileAction extends AbstractAction{
			RepeatWhileAction(){
				super("", OWLSIcons.getRepeatWhileIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Repeat-While");
			}
		}
		repeatwhileAction = new RepeatWhileAction();
		repeatwhileButton = buttonpanel.add(repeatwhileAction);
		repeatwhileButton.setToolTipText("Create Repeat-While");

		class ProduceAction extends AbstractAction{
			ProduceAction(){
				super("", OWLSIcons.getProduceIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.addNode("process:Produce");
			}
		}
		produceAction = new ProduceAction();
		produceButton = buttonpanel.add(produceAction);
		produceButton.setToolTipText("Create Produce");
		
		class DeleteAction extends AbstractAction{
			DeleteAction(){
				super("", OWLSIcons.getDeleteControlConstructIcon());
			}
			public void actionPerformed(ActionEvent e){
				currentTree.deleteNode(currentTree.getSelectedNode());
			}
		}
		deleteAction = new DeleteAction();
		deleteButton = buttonpanel.add(deleteAction);
		deleteButton.setToolTipText("Delete control construct");
	}
	
}