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

import java.awt.datatransfer.Transferable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.pvv.bcd.Util.FeatureNotSupportedException;
import org.pvv.bcd.instrument.JTree.DefaultTransferable;
import org.pvv.bcd.instrument.JTree.DndId;
import org.pvv.bcd.instrument.JTree.NodeFactory;

import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;
import com.sri.owlseditor.util.OWLUtils;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * All node creation should go through this class.
 * Some of the methods belong to the NodeFactory interface of IJTree.
 * 
 */
public class OWLSTreeNodeFactory implements NodeFactory, CleanerListener{
	private static OWLSTreeNodeFactory instance; 
	private OWLModel okb;
	
	/** The instance is generated on first use */
	public static OWLSTreeNodeFactory getInstance(OWLModel okb){
		if (instance == null){
			instance = new OWLSTreeNodeFactory(okb);
			return instance;
		}
		else{
			if (instance.okb != okb){
				System.out.println("This OWLSTreeNodeFactory was created with a different" +
									" KB: " + instance.okb + ", new KB is " + okb);
				return null;
			}
			else{
				return instance;
			}
		}
	}
	
	public void cleanup(){
		instance = null;
	}
	
	private OWLSTreeNodeFactory(OWLModel okb){
		this.okb = okb;
		Cleaner.getInstance().registerCleanerListener(this);
	}
	
	/* Methods from the NodeFactory interface */
	public TreeNode cloneNode(TreeNode node){
		System.out.println("WARNING! Call to clone node. " +
							"We do not support this.");
		return null;
	}
	
	public TreeNode createNode(TreeNode parent){
		System.out.println("WARNING! Call to create node without content. " +
							"We do not support this.");
		return null;
	}
	
	/* The string parameter is ignored */
	public TreeNode createNode(String title, Object contents){
		//System.out.println("OWLSTreeNodeFactory.createNode(String, Object)");
		return createNode(new OWLSTreeNodeInfo((OWLIndividual)contents, okb));
	}

	/* Assumes ob is an OWLSTreeNodeInfo */
	public TreeNode createNode(Object ob){
		//System.out.println("OWLSTreeNodeFactory.createNode(Object)");
		return createTreeNode((OWLSTreeNodeInfo)ob);
	}

	public Transferable createTransferable(Object node, DndId dndId)
	      throws FeatureNotSupportedException{
		//System.out.println("createTransferable called on single node");
		return new DefaultTransferable((DefaultMutableTreeNode)node, dndId);
	}

	public Transferable createTransferable(Object[] nodes, DndId dndId)
	      throws FeatureNotSupportedException{
		//System.out.println("createTransferable called on node array:");
		//for (int i=0; i<nodes.length; i++){
		//	System.out.println(nodes[i] + " of " + nodes[i].getClass());
		//}
		//System.out.println("---");
		//System.out.println("DndId is: " + dndId);
		//DataFlavor df = DefaultTranferable.DEFAULT_NODE_INFO_FLAVOUR;
		//System.out.println("The flavour of choice is: " + df);
		return new DefaultTransferable((DefaultMutableTreeNode[])nodes, dndId);
	}

	
	/** Returns an instance of one of the subclasses of OWLSTreeNode
	 * based on the type of control construct instance.
	 * @param inst
	 */
	//public static OWLSTreeNode createTreeNode(Instance inst, OWLModel okb){
	public static OWLSTreeNode createTreeNode(OWLSTreeNodeInfo ni){
		OWLModel okb = ni.getOWLModel();
		OWLIndividual inst = ni.getInstance();
		if (inst == null){
			System.out.println("ERROR! OWLSTreeNodeFactory.createTreeNode: inst is null");
		}
		String constructtype = OWLUtils.getClassNameOfInstance(inst);
		//System.out.println("Creating OWLSTreeNode for a " + constructtype);
		
		if (constructtype.equals("process:Perform")){
			return new PerformNode(ni);
		}
		else if (constructtype.equals("process:Produce")){
			return new ProduceNode(ni);
		}
		else if (constructtype.equals("process:Sequence")){
			return new SequenceNode(ni);
		}
		else if (constructtype.equals("process:Split")){
			return new SplitNode(ni);
		}	
		else if (constructtype.equals("process:Split-Join")){
			return new SplitJoinNode(ni);
		}
		else if (constructtype.equals("process:Any-Order")){
			return new AnyOrderNode(ni);
		}
		else if (constructtype.equals("process:Choice")){
			return new ChoiceNode(ni);
		}	
		else if (constructtype.equals("process:If-Then-Else")){
			return new IfThenElseNode(ni);
		}	
		else if (constructtype.equals("process:Repeat-While")){
			return new RepeatWhileNode(ni);
		}	
		else if (constructtype.equals("process:Repeat-Until")){
			return new RepeatUntilNode(ni);
		}	
		else{
			System.out.println("ERROR! Unsupported control construct");
			return null;
		}
	}
}
