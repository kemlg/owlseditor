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
package com.sri.owlseditor.xslt.xml2owl;

import com.sri.owlseditor.xslt.XSLTNode;

import edu.stanford.smi.protegex.owl.model.RDFResource;

/**
 * @author Daniel Elenius
 */
public abstract class OWLNode extends XSLTNode {

	public abstract RDFResource getResource();

	public OWLNode(String s) {
		super(s);
	}

}
