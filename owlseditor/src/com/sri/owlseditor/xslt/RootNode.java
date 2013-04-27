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
package com.sri.owlseditor.xslt;

import javax.swing.Icon;

import com.sri.owlseditor.util.OWLSIcons;

/**
 * @author Daniel Elenius
 */
public class RootNode extends XSLTNode {

	public RootNode(){
		super("");
		setAllowsChildren(true);
	}
	
	public String getXSLTString(int indentnum) {
		return "";
	}
	
	public Icon getIcon(){
		return OWLSIcons.getXMLRootNodeIcon();
	}
}
