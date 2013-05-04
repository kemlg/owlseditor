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

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.RDFResource;

/**
 * @author Daniel Elenius
 */
public class MatchPair {
	OWLIndividual oldParameter;
	RDFResource oldType;
	String newParameter;
	String newType;
	int matchType;

	MatchPair(OWLIndividual oldParameter, RDFResource oldType,
			String newParameter, String newType, int matchType) {
		this.oldParameter = oldParameter;
		this.oldType = oldType;
		this.newParameter = newParameter;
		this.newType = newType;
		this.matchType = matchType;
	}

	private String matchTypeString(int matchType) {
		switch (matchType) {
		case 0:
			return "EQUIVALENT";
		case 1:
			return "SUBSUMES";
		case 2:
			return "SUBSUMED";
		case 3:
			return "FAIL";
		case 4:
			return "EXTRA";
		}
		return null;
	}

	public String toString() {
		if (oldParameter != null)
			return "\n  Original parameter " + oldParameter.getName()
					+ "\n  New parameter " + newParameter + "\n  Match type "
					+ matchTypeString(matchType);
		else
			return "\n  Original parameter NULL" + "\n  New parameter "
					+ newParameter + "\n  Match type "
					+ matchTypeString(matchType);
	}

	public OWLIndividual getOriginalParameter() {
		return oldParameter;
	}

	public RDFResource getOldType() {
		return oldType;
	}

	public String getNewParameter() {
		return newParameter;
	}

	public String getNewType() {
		return newType;
	}

	public int getMatchType() {
		return matchType;
	}
}
