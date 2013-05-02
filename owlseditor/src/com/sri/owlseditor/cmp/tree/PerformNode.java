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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.sri.owlseditor.cmp.graph.GraphNodeInfo;
import com.sri.owlseditor.cmp.graph.GraphProcessModel;
import com.sri.owlseditor.cmp.graph.UniqueName;
import com.sri.owlseditor.util.OWLUtils;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;

public class PerformNode extends OWLSTreeNode {
	public static final String ATOMIC_PERFORM_PREFIX = "Prfrm_A_";
	public static final String SIMPLE_PERFORM_PREFIX = "Prfrm_S_";
	public static final String COMPOSITE_PERFORM_PREFIX = "Prfrm_C_";
	public static final String PERFORM_PREFIX = "Prfrm_";

	OWLProperty hasDataFromSlot;
	OWLProperty valueSourceSlot;
	OWLProperty fromProcessSlot;
	OWLProperty theVarSlot;
	OWLProperty toParamSlot;
	OWLNamedClass valueOfCls;
	OWLModel model;

	public PerformNode(OWLSTreeNodeInfo ni) {
		super(ni, false); // Performs can not have children, they are leaves
		setupClassesAndProperties();
	}

	private void setupClassesAndProperties() {
		model = getOWLModel();
		hasDataFromSlot = model.getOWLProperty("process:hasDataFrom");
		valueSourceSlot = model.getOWLProperty("process:valueSource");
		fromProcessSlot = model.getOWLProperty("process:fromProcess");
		theVarSlot = model.getOWLProperty("process:theVar");
		toParamSlot = model.getOWLProperty("process:toParam");
		valueOfCls = model.getOWLNamedClass("process:ValueOf");
	}

	public GraphNodeInfo graph(HashSet nameSet, PrintWriter pw,
			int clusterNumber, OWLSTreeNode selectedNode) {
		OWLIndividual inst;

		int newClusterNumber = clusterNumber;
		Integer clusterInt = new Integer(newClusterNumber++);
		pw.println("subgraph " + clusterInt.toString() + " {");

		// If this node is the selected one, 'highlight' it.
		if (this.equals(selectedNode)) {
			pw.println("node [fillcolor="
					+ GraphProcessModel.SELECTED_NODE_FILL_COLOR + ", color="
					+ GraphProcessModel.SELECTED_NODE_EDGE_COLOR + "];");
			pw.println("edge [color=" + GraphProcessModel.SELECTED_EDGE_COLOR
					+ "];");
		}

		String nodename = "";
		UniqueName unodename = null;

		// If there is an error, paint it red.
		if (getProcess() == null) {
			// No process for this Perform. Draw it in red.
			pw.println("node [color=" + GraphProcessModel.ERROR_NODE_EDGE_COLOR
					+ ", fontcolor=" + GraphProcessModel.ERROR_NODE_FONT_COLOR
					+ "];");
			unodename = new UniqueName(
					PERFORM_PREFIX + getInstance().getName(), nameSet);
			nodename = unodename.getUniqueName();
			pw.println(nodename + "[label=\"?\"];");
		} else if (performsCompositeProcess()) {
			unodename = new UniqueName(COMPOSITE_PERFORM_PREFIX
					+ getInstance().getName(), nameSet);
			nodename = unodename.getUniqueName();
			pw.println(nodename + "[peripheries=2, label=\""
					+ getProcess().getName() + "\"];");
		} else if (performsAtomicProcess()) {
			unodename = new UniqueName(ATOMIC_PERFORM_PREFIX
					+ getInstance().getName(), nameSet);
			nodename = unodename.getUniqueName();
			pw.println(nodename + "[label=\"" + getProcess().getName() + "\"];");
		} else { // simple process
			unodename = new UniqueName(SIMPLE_PERFORM_PREFIX
					+ getInstance().getName(), nameSet);
			nodename = unodename.getUniqueName();
			pw.println(nodename + "[label=\"" + getProcess().getName() + "\"];");
		}
		pw.println("}");

		// Now add the dataflow arrows
		addInputBindings(pw, nameSet);

		return new GraphNodeInfo(nodename, nodename, "", "", newClusterNumber);
	}

	private void addInputBindings(PrintWriter pw, HashSet nameSet) {

		Collection inputBindings = getInstance().getPropertyValues(
				hasDataFromSlot, true);
		Iterator it = inputBindings.iterator();
		Bindings bindings = new Bindings(getInstance(), nameSet);
		while (it.hasNext()) {
			OWLIndividual inputBinding = (OWLIndividual) it.next();
			OWLIndividual valueOf = (OWLIndividual) inputBinding
					.getPropertyValue(valueSourceSlot);
			if (valueOf != null) {
				OWLIndividual fromProcess = (OWLIndividual) valueOf
						.getPropertyValue(fromProcessSlot);
				OWLIndividual theVar = (OWLIndividual) valueOf
						.getPropertyValue(theVarSlot);
				OWLIndividual toParam = (OWLIndividual) inputBinding
						.getPropertyValue(toParamSlot);
				if (fromProcess != null && theVar != null && toParam != null) {
					// We have a valid valueSource input binding, so we draw it
					bindings.addBinding(fromProcess, theVar, toParam);
				}
			}
		}
		bindings.graphBindings(pw);
	}

	/* Helper method for addInputBindings */
	/*
	 * private String createInputBindingString(OWLIndividual fromProcess,
	 * OWLIndividual theVar, OWLIndividual toParam){ String fromProcessName =
	 * fromProcess.getName();
	 * 
	 * // If the fromProcess is TheParentPerform or ThisPerform, // we need some
	 * special handling if (fromProcessName.equals("process:TheParentPerform"))
	 * fromProcessName = "Start"; else if
	 * (fromProcessName.equals("process:ThisPerform")) fromProcessName =
	 * getInstance().getName();
	 * 
	 * UniqueName fromProcessUName = new UniqueName(fromProcessName, new
	 * HashSet()); UniqueName toProcessUName = new
	 * UniqueName(getInstance().getName(), new HashSet());
	 * 
	 * return fromProcessUName.getUniqueName() + "->" +
	 * toProcessUName.getUniqueName() + " [color=" +
	 * GraphProcessModel.DATAFLOW_EDGE_COLOR + ", " +
	 * //"tailport=e, headport=w, " + //"taillabel=\"" + theVar.getName() +
	 * "\", " + //"headlabel=\"" + toParam.getName() + "\", " + //"label=\"" +
	 * ((OWLInstance)theVar).getLocalName() + "->" +
	 * //((OWLInstance)toParam).getLocalName() + "\", " + "comment=\"" +
	 * theVar.getName() + " -> " + toParam.getName() + "\", " +
	 * "labelfontsize=\"8\", " + //"labelfloat=true, " + //"style=invis, " +
	 * "labelfontname=Arial, " + "labelfontcolor=\"" +
	 * GraphProcessModel.DATAFLOW_FONT_COLOR + "\"" + "constraint=\"false\"];";
	 * //"weight=\"0.2\"];"; }
	 */

	/** Deletes ValueOf and InputBinding instances of this perform. */
	public void deleteBindings() {
		OWLIndividual perform = getInstance();

		System.out.println("Deleting bindings on " + perform.getName());

		Collection bindings = perform.getPropertyValues(hasDataFromSlot);

		System.out.println("Number of bindings: " + bindings.size());

		Iterator it = bindings.iterator();
		while (it.hasNext()) {
			OWLIndividual inputBinding = (OWLIndividual) it.next();
			OWLIndividual valueOf = (OWLIndividual) inputBinding
					.getPropertyValue(valueSourceSlot);

			System.out.println("Deleting InputBinding "
					+ inputBinding.getName());
			System.out.println("Deleting ValueOf " + valueOf.getName());

			valueOf.delete();
			inputBinding.delete();
		}
	}

	public void updateKBAfterDelete(OWLSTreeNode node) {
		// This should never be called as a Perform cannot have children
		// deleted
	}

	public void updateKBAfterMove(OWLSTreeNode node) {
		;
	}

	/**
	 * Updates the KB after adding a node.
	 */
	public void updateKBAfterInsert(OWLSTreeNode newnode) {
		// This should never be called as a Perform cannot have children
		// inserted
	}

	/**
	 * For Performs, we print "Perform x", where x is the name of the process
	 * that is performed.
	 * 
	 * We add some whitespace after the name, to leave room for the process type
	 * mini-icon (see OWLSTreeCellRenderer).
	 */
	public String toString() {
		OWLIndividual inst = getProcess();
		if (inst == null)
			// No value for process property yet
			return "Perform -";
		else
			return "Perform " + inst.getName() + "    ";
	}

	/**
	 * Returns true if the Perform performs a CompositeProcess, false otherwise.
	 * Performs with no process are thus treated as non-composite.
	 */
	public boolean performsCompositeProcess() {
		OWLIndividual process = getProcess();
		if (process == null)
			return false;
		else if (process.hasRDFType(getOWLModel().getOWLNamedClass(
				"process:CompositeProcess")))
			return true;
		else
			return false;
	}

	/**
	 * Returns true if the Perform performs an AtomicProcess, false otherwise.
	 * Performs with no process are thus treated as non-atomic.
	 */
	public boolean performsAtomicProcess() {
		OWLIndividual process = getProcess();
		if (process == null)
			return false;
		else if (process.hasRDFType(getOWLModel().getOWLNamedClass(
				"process:AtomicProcess")))
			return true;
		else
			return false;
	}

	public OWLIndividual getProcess() {
		return (OWLIndividual) OWLUtils.getNamedSlotValue(getInstance(),
				"process:process", getOWLModel());
	}

	public boolean acceptsChild() {
		return false;
	}

}
