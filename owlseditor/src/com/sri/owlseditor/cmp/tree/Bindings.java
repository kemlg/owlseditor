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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.sri.owlseditor.cmp.graph.GraphProcessModel;
import com.sri.owlseditor.cmp.graph.UniqueName;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/* Helper class */
class BindingPair {
	OWLIndividual fromParam;
	OWLIndividual toParam;

	BindingPair(OWLIndividual fromParam, OWLIndividual toParam) {
		this.fromParam = fromParam;
		this.toParam = toParam;
	}
}

/**
 * Used by PerformNode and ProduceNode.
 * 
 * @author Daniel Elenius
 */
public class Bindings {
	private HashMap bindingMap = new HashMap();
	private HashSet nameSet;
	private OWLNamedClass atomicProcess;
	private OWLNamedClass compositeProcess;
	private OWLNamedClass simpleProcess;
	private OWLObjectProperty processProperty;
	private OWLIndividual perform; // this can also be a produce
	private OWLIndividual process;
	private OWLNamedClass produceCls;
	private OWLNamedClass performCls;
	private OWLModel model;

	Bindings(OWLIndividual perform, HashSet nameSet) {
		this.perform = perform;
		this.nameSet = nameSet;
		setupClassesAndProperties();
		if (isPerform())
			process = (OWLIndividual) perform.getPropertyValue(processProperty);
	}

	private boolean isPerform() {
		return perform.hasRDFType(performCls, true);
	}

	private void setupClassesAndProperties() {
		model = perform.getOWLModel();
		atomicProcess = model.getOWLNamedClass("process:AtomicProcess");
		compositeProcess = model.getOWLNamedClass("process:CompositeProcess");
		simpleProcess = model.getOWLNamedClass("process:SimpleProcess");
		processProperty = model.getOWLObjectProperty("process:process");
		produceCls = model.getOWLNamedClass("process:Produce");
		performCls = model.getOWLNamedClass("process:Perform");
	}

	public void addBinding(OWLIndividual fromProcess, OWLIndividual theVar,
			OWLIndividual toParam) {
		// For each fromProcess, we have a Vector of BindindPairs
		Vector bindings = (Vector) bindingMap.get(fromProcess);
		if (bindings == null) {
			bindings = new Vector();
			bindingMap.put(fromProcess, bindings);
		}
		bindings.add(new BindingPair(theVar, toParam));
	}

	private UniqueName getPerformNodeName(String nodename,
			OWLIndividual theprocess) {
		if (theprocess.hasRDFType(atomicProcess, true))
			return new UniqueName(PerformNode.ATOMIC_PERFORM_PREFIX + nodename,
					new HashSet());
		else if (theprocess.hasRDFType(compositeProcess, true))
			return new UniqueName(PerformNode.COMPOSITE_PERFORM_PREFIX
					+ nodename, new HashSet());
		else if (theprocess.hasRDFType(simpleProcess, true))
			return new UniqueName(PerformNode.SIMPLE_PERFORM_PREFIX + nodename,
					new HashSet());

		System.out.println("ERROR! Unrecognized process type.");
		return null;
	}

	private String getFromNodeName(OWLIndividual fromPerform) {
		String fromPerformName = fromPerform.getName();
		OWLIndividual fromProcess = (OWLIndividual) fromPerform
				.getPropertyValue(processProperty);
		// If the fromProcess is TheParentPerform or ThisPerform,
		// we need some special handling
		if (fromPerformName.equals("process:TheParentPerform"))
			return "Start";
		else if (fromPerformName.equals("process:ThisPerform")) {
			fromPerformName = perform.getName();
		}

		UniqueName fromNode = getPerformNodeName(fromPerformName, fromProcess);
		return fromNode.getUniqueName();
	}

	private String getToNodeName() {
		String performName = perform.getName();
		UniqueName toNode = null;
		if (isPerform()) {
			toNode = getPerformNodeName(performName, process);
		} else {
			toNode = new UniqueName(performName, new HashSet());
		}
		return toNode.getUniqueName();
	}

	// 0.1, 0.3 is a decent setting, so is 0.5, 0.5
	private String getDataFlowInEdgeAttributes() {
		return " [color=" + GraphProcessModel.DATAFLOW_EDGE_COLOR +
		// ", constraint=\"false\"" +
				", weight=\"0.5\"" + "];";
	}

	private String getDataFlowOutEdgeAttributes() {
		return " [color=" + GraphProcessModel.DATAFLOW_EDGE_COLOR +
		// ", constraint=\"false\"" +
				", weight=\"0.5\"" + "];";
	}

	public void graphBindings(PrintWriter pw) {
		Set fromProcesses = bindingMap.keySet();
		Iterator it = fromProcesses.iterator();

		while (it.hasNext()) {
			OWLIndividual fromProcess = (OWLIndividual) it.next();

			// Generate the data flow node
			// Ideally, this node should NOT be in the Perform's
			// subgraph, but in the top-level subgraph.

			String fromNodeName = getFromNodeName(fromProcess);
			String toNodeName = getToNodeName();
			UniqueName dfnode = new UniqueName("DataFlow"
					+ fromProcess.getName(), nameSet);

			// Put in the connections to/from the data flow node
			pw.println(fromNodeName + "->" + dfnode.getUniqueName()
					+ getDataFlowInEdgeAttributes());
			pw.println(dfnode.getUniqueName() + "->" + toNodeName
					+ getDataFlowOutEdgeAttributes());

			pw.print(dfnode.getUniqueName() + " [shape=record"
					+ ", label=\"{From");

			Vector bindings = (Vector) bindingMap.get(fromProcess);
			Iterator it2 = bindings.iterator();
			while (it2.hasNext()) {
				BindingPair pair = (BindingPair) it2.next();
				pw.print("|" + pair.fromParam.getName());
			}
			pw.print("}|{To");
			it2 = bindings.iterator();
			while (it2.hasNext()) {
				BindingPair pair = (BindingPair) it2.next();
				pw.print("|" + pair.toParam.getName());
			}
			pw.print("}\", ");
			pw.print("color=" + GraphProcessModel.DATAFLOW_NODE_COLOR + ", ");
			pw.print("fillcolor=" + GraphProcessModel.NODE_FILL_COLOR + ", "); // this
																				// is
																				// so
																				// it
																				// doesn't
																				// get
																				// highlighted
			pw.println("fontcolor=" + GraphProcessModel.DATAFLOW_FONT_COLOR
					+ "];");
		}
	}
}
