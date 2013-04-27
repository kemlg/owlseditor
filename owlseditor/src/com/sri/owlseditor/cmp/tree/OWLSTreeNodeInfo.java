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

import org.pvv.bcd.instrument.JTree.DefaultNodeInfo;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Each OWLSTreeNode will have an instance of this class as its user-object.
 * This class implements the NodeInfo interface, so tree nodes with objects of
 * this class can be manipulated by the IJTree methods. 
 * 
 * @author Daniel Elenius
 *
 */
public class OWLSTreeNodeInfo extends DefaultNodeInfo {
	private OWLModel okb;	
	private OWLIndividual inst;
	
	public OWLSTreeNodeInfo(OWLIndividual inst, OWLModel okb){
		super("", inst);
		this.okb = okb;
		this.inst = inst;
	}	

	public OWLModel getOWLModel(){
		return okb;
	}
	
	public OWLIndividual getInstance(){
		return inst;
	}

	public String toString(){
		return "OWLSTreeNode for " + inst;
	}
	
}
