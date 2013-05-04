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

import java.io.PrintWriter;
import java.util.HashSet;

import com.sri.owlseditor.cmp.graph.UniqueName;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

/**
 * @author Daniel Elenius
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ConditionalNode extends OWLSTreeNode {
	private OWLProperty conditionSlot;

	public ConditionalNode(OWLSTreeNodeInfo ni, boolean childrenAllowed,
			String slottype) {
		super(ni, childrenAllowed);
		conditionSlot = getOWLModel().getOWLProperty(slottype);
	}

	// This creates a new Condition if there is none.
	public OWLIndividual getCondition() {
		OWLModel okb = getOWLModel();
		OWLNamedClass condCls = okb.getOWLNamedClass("expr:Condition");

		OWLIndividual thisInst = getInstance();
		OWLIndividual cond = (OWLIndividual) thisInst
				.getPropertyValue(conditionSlot);
		if (cond == null) {
			cond = (OWLIndividual) condCls.createInstance(null);
			// System.out.println("ConditionalNode.getCondition() created new Condition: "
			// +
			// cond.getName() + cond);
			thisInst.addPropertyValue(conditionSlot, cond);
		}
		return cond;
	}

	/*
	 * If there is an rdfs:label on the Condition, we use that as the node
	 * label, otherwise we use the local part of the instance name.
	 */
	public String getConditionLabel() {
		OWLModel okb = getOWLModel();
		RDFProperty label = okb.getRDFProperty("rdfs:label");

		RDFResource cond = (RDFResource) getCondition();
		String labelString = (String) cond.getPropertyValue(label);
		if (labelString == null)
			return cond.getLocalName();
		return labelString;
	}

	/* Creates the condition node, and returns the UniqueName for it */
	public UniqueName printCondNode(PrintWriter pw, HashSet nameSet) {
		Instance condition = getCondition();
		UniqueName condNodeName = new UniqueName(condition.getName(), nameSet);
		pw.println(condNodeName.getUniqueName()
				+ " [shape=diamond, fixedsize=\"true\", label=\""
				+ getConditionLabel() + "\"];");
		return condNodeName;
	}

}
