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

import java.util.List;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;

/**
 * 
 * @author Daniel Elenius
 */
public interface MatchProvider {

	/** Returns a list of MatchResults for the process. */
	public List findMatchingProcesses(OWLIndividual perform);

	/**
	 * Returns a list of MatchResults that have outputs to match the provided
	 * process's inputs.
	 */
	public List findInputProviders(OWLIndividual perform);

	/**
	 * Returns a list of MatchResults that have inputs to match the provided
	 * process's outputs.
	 */
	public List findOutputConsumers(OWLIndividual perform);

	/**
	 * Returns the kb object corresponding to a certain match. If the
	 * MatchProvider is non-local, this is where it has to retrieve and import
	 * the ontology containing the process. When this method has returned, all
	 * IOs in this process and their parameterType classes must also be in the
	 * local kb, under the same names that they use in the MatchResult data
	 * structures.
	 */
	public OWLIndividual getProcess(MatchResult match);

	/** Returns the name of this MatchProvider */
	public String getName();
}
