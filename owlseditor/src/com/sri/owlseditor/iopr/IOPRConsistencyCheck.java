// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License.  You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
// License for the specific language governing rights and limitations under
// the License.
//
// The Original Code is OWL-S Editor for Protege.
//
// The Initial Developer of the Original Code is SRI International.
// Portions created by the Initial Developer are Copyright (C) 2004 the
// Initial Developer.  All Rights Reserved.

package com.sri.owlseditor.iopr;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;

import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import com.sri.owlseditor.consistency.ConsistencyCheckDisplay;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;

public class IOPRConsistencyCheck {
	private static IOPRConsistencyCheck instance;
	private OWLModel _okb;
	private Project _project;
	private IOPRSelector m_ioprList;
	private ConsistencyCheckDisplay m_ccd;
	private Vector m_vgc;

	private IOPRConsistencyCheck(OWLModel okb, IOPRSelector ioprList) {
		_okb = okb;
		_project = _okb.getProject();
		m_ccd = ConsistencyCheckDisplay.getInstance();
		m_ioprList = ioprList;
	}

	public static IOPRConsistencyCheck getInstance(OWLModel okb,
			IOPRSelector ioprList) {
		if (instance == null)
			instance = new IOPRConsistencyCheck(okb, ioprList);
		return instance;
	}

	public void checkIOPRProcessAndProfiles(Vector vGlobalConsistency) {
		m_vgc = vGlobalConsistency;
		Collection profileInsts = _okb.getOWLNamedClass("profile:Profile")
				.getInstances(true);

		if (profileInsts != null) {
			Iterator it = profileInsts.iterator();
			OWLObjectProperty has_process = _okb
					.getOWLObjectProperty("profile:has_process");

			while (it.hasNext()) { // iterate over all profiles
				OWLIndividual profileInst = (OWLIndividual) it.next();
				OWLIndividual processInst = (OWLIndividual) profileInst
						.getPropertyValue(has_process);

				if (processInst != null) {
					Collection ioprProperties = m_ioprList.getIOPRproperties();
					Iterator i2 = ioprProperties.iterator();

					while (i2.hasNext()) { // iterate over ioprProperties
											// (hasInput, hasOutput, etc)
						String ioprProperty = (String) i2.next();
						Collection profileIOPRs = profileInst
								.getPropertyValues(
										_okb.getOWLProperty("profile"
												+ ioprProperty), true);
						Collection processIOPRs = processInst
								.getPropertyValues(
										_okb.getOWLProperty("process"
												+ ioprProperty), true);
						Iterator itt = profileIOPRs.iterator();

						while (itt.hasNext()) { // iterate over listitems that
												// have profile checked
							RDFIndividual profileIOPR = (RDFIndividual) itt
									.next();
							boolean isMatch = checkForMatch(profileIOPR,
									processIOPRs);
							if (!isMatch) {
								createInconsistencyEntry1(profileIOPR,
										profileInst, processInst, ioprProperty);
							}
						}
					}
				}
			}
		}
	}

	public void checkIOPRProcessParameterType(Vector vGlobalConsistency) {
		m_vgc = vGlobalConsistency;

		checkOWLNamedClass(InputList.INPUT);
		checkOWLNamedClass(OutputList.OUTPUT);
	}

	private void checkOWLNamedClass(String namedClass) {
		OWLNamedClass cls = _okb.getOWLNamedClass(namedClass);
		Iterator it = cls.getInstances(true).iterator();
		OWLDatatypeProperty paramType = _okb
				.getOWLDatatypeProperty("process:parameterType");

		while (it.hasNext()) {
			RDFResource rdfr = (RDFResource) it.next();
			RDFSLiteral literal = (RDFSLiteral) rdfr
					.getPropertyValue(paramType);

			// check for null
			if (literal == null) {
				createInconsistencyEntry2(rdfr, "a null");
				continue;
			}

			// check for illegal URI syntax
			String uriStr = literal.getString();

			try {
				URI uri = new URI(uriStr);
				if (uri.getFragment() == null) {
					createInconsistencyEntry2(rdfr, "an illegal");
					continue;
				}
			} catch (Exception URISyntaxException) {
				createInconsistencyEntry2(rdfr, "an illegal");
				continue;
			}

			// check for instance of class
			String resourceName = _okb.getResourceNameForURI(uriStr);
			RDFResource resource = _okb.getRDFResource(resourceName);
			if (!(resource instanceof RDFSClass)
					&& !(resource instanceof RDFSDatatype)) {
				createInconsistencyEntry3(rdfr, resourceName);
			}
		}
	}

	private void createInconsistencyEntry1(RDFIndividual inst,
			OWLIndividual profile, OWLIndividual process, String ioprProperty) {
		// label object followed by problem object followed by vector of n
		// solution objects
		Vector vProblem = new Vector();
		Vector vSolution = new Vector();

		vProblem.addElement(" Parameter ");
		vProblem.addElement(inst);
		vProblem.addElement(" in Profile ");
		vProblem.addElement(profile);
		vProblem.addElement(" should also be in Process ");
		vProblem.addElement(process);

		Vector vSol = new Vector();
		ActionListener commitListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addRemovePropertyValue(this, "process");
				m_ccd.removeInconsistencyFromDisplay(this, 0);
			}
		};

		Vector vInst = new Vector();
		vInst.addElement(inst);
		vInst.addElement(process);
		vInst.addElement(ioprProperty);

		vSol.addElement(commitListener);
		vSol.addElement(vInst);
		vSol.addElement("Add Parameter ");
		vSol.addElement(inst);
		vSol.addElement(" to Process ");
		vSol.addElement(process);
		vSolution.addElement(vSol);

		vSol = new Vector();
		commitListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addRemovePropertyValue(this, "profile");
				m_ccd.removeInconsistencyFromDisplay(this, 0);
			}
		};
		vInst = new Vector();
		vInst.addElement(inst);
		vInst.addElement(profile);
		vInst.addElement(ioprProperty);

		vSol.addElement(commitListener);
		vSol.addElement(vInst);
		vSol.addElement("Remove Parameter ");
		vSol.addElement(inst);
		vSol.addElement(" from Profile ");
		vSol.addElement(profile);
		vSolution.addElement(vSol);

		ActionListener ignoreListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_ccd.removeInconsistencyFromDisplay(this, 1);
			}
		};

		Vector vEntry = new Vector();
		vEntry.addElement("IOPR: ");
		vEntry.addElement(vProblem);
		vEntry.addElement(vSolution);
		vEntry.addElement(ignoreListener);

		m_vgc.addElement(vEntry);
	}

	private void createInconsistencyEntry2(RDFResource rdfr, String msgStr) {
		Vector vProblem = new Vector();
		vProblem.addElement(rdfr);
		vProblem.addElement(" in ");
		vProblem.addElement(rdfr.getRDFType());
		vProblem.addElement(" has " + msgStr + " URI parameterType");

		Vector vSolution = new Vector();
		Vector vSol = new Vector();
		vSol.addElement(null); // null listener
		vSol.addElement(null); // null user vector
		vSol.addElement("Add a valid URI parameterType in the ");

		Vector vColor = new Vector();
		vColor.addElement("process:parameterType");
		vColor.addElement(new Color(0, 150, 50));
		vSol.addElement(vColor);
		vSol.addElement(" widget");

		vSolution.addElement(vSol);

		ActionListener ignoreListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_ccd.removeInconsistencyFromDisplay(this, 1);
			}
		};

		Vector vEntry = new Vector();
		vEntry.addElement("IOPR: ");
		vEntry.addElement(vProblem);
		vEntry.addElement(vSolution);
		vEntry.addElement(ignoreListener);

		m_vgc.addElement(vEntry);
	}

	private void createInconsistencyEntry3(RDFResource rdfr, String resourceName) {
		Vector vProblem = new Vector();
		vProblem.addElement(rdfr);
		vProblem.addElement(" in ");
		vProblem.addElement(rdfr.getRDFType());
		vProblem.addElement(" has a parameterType that is not an OWL Class or XSD Datatype");

		ActionListener commitListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				constructLegitParamType(this);
				m_ccd.removeInconsistencyFromDisplay(this, 0);
			}
		};
		Object[] userObj = new Object[2];
		userObj[0] = rdfr;
		userObj[1] = resourceName;

		Vector vSolution = new Vector();
		Vector vSol = new Vector();
		vSol.addElement(commitListener); // listener
		vSol.addElement(userObj); // user vector
		vSol.addElement("Add ");
		vSol.addElement(resourceName);
		vSol.addElement(" in the ");

		Vector vColor = new Vector();
		vColor.addElement("process:parameterType");
		vColor.addElement(new Color(0, 150, 50));
		vSol.addElement(vColor);
		vSol.addElement(" widget");
		vSolution.addElement(vSol);

		ActionListener ignoreListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_ccd.removeInconsistencyFromDisplay(this, 1);
			}
		};

		Vector vEntry = new Vector();
		vEntry.addElement("IOPR: ");
		vEntry.addElement(vProblem);
		vEntry.addElement(vSolution);
		vEntry.addElement(ignoreListener);

		m_vgc.addElement(vEntry);
	}

	private boolean checkForMatch(RDFIndividual profileVal,
			Collection processVals) {
		Iterator itt = processVals.iterator();
		while (itt.hasNext()) {
			RDFIndividual inst = (RDFIndividual) itt.next();
			if (inst.getName().equals(profileVal.getName()))
				return true;
		}

		return false;
	}

	private void addRemovePropertyValue(ActionListener lst, String typeStr) {
		Vector vInst = (Vector) getUserObject(lst);
		if (vInst == null)
			return;

		RDFIndividual inst = (RDFIndividual) vInst.elementAt(0);
		OWLIndividual processOrProfile = (OWLIndividual) vInst.elementAt(1);
		String ioprProperty = (String) vInst.elementAt(2);

		String propertyName = typeStr + ioprProperty;
		RDFProperty rdfp = (RDFProperty) _okb.getOWLProperty(propertyName);

		if (typeStr.equals("process")) // add process
			processOrProfile.addPropertyValue(rdfp, inst);
		else
			// must be profile, so remove profile
			processOrProfile.removePropertyValue(rdfp, inst);

		m_ioprList.refreshIOPRDisplay();
	}

	private void constructLegitParamType(ActionListener lst) {
		Object[] userObj = (Object[]) getUserObject(lst);
		OWLNamedClass onc = _okb.createOWLNamedClass((String) userObj[1]);
		OWLDatatypeProperty paramType = _okb
				.getOWLDatatypeProperty("process:parameterType");
		RDFResource rdfr = (RDFResource) userObj[0];
		DefaultRDFSLiteral literal = (DefaultRDFSLiteral) _okb
				.createRDFSLiteral(onc.getURI(), _okb
						.getRDFSDatatypeByURI(XSDDatatype.XSDanyURI.getURI()));
		rdfr.setPropertyValue(paramType, literal);
	}

	private Object getUserObject(ActionListener lst) {
		for (int i = 0; i < m_vgc.size(); i++) {
			Vector vEntry = (Vector) m_vgc.elementAt(i);
			Vector vSolution = (Vector) vEntry.elementAt(2);

			for (int j = 0; j < vSolution.size(); j++) {
				Vector vSol = (Vector) vSolution.elementAt(j);
				ActionListener lst2 = (ActionListener) vSol.elementAt(0);

				if (lst == lst2)
					return vSol.elementAt(1);
			}
		}

		return null;
	}
}
