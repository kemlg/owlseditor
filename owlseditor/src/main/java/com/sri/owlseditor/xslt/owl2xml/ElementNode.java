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
package com.sri.owlseditor.xslt.owl2xml;

import java.util.Enumeration;

import com.sri.owlseditor.xslt.XSLTNode;

/**
 * @author Daniel Elenius
 */
public class ElementNode extends XSLTNode {
	public final static String DEFAULT_ELEMENT_NAME = "Element";

	public ElementNode() {
		this(DEFAULT_ELEMENT_NAME);
	}

	public ElementNode(String name) {
		super(name);
		setAllowsChildren(true);
	}

	public String getXSLTString(int indentnum) {
		String str = "";
		String indent = "";
		for (int i = 0; i < indentnum; i++)
			indent += TAB;

		str = indent + "<" + getNodeName() + ">" + NL;
		boolean notFirstAttribute = false;
		for (Enumeration e = children(); e.hasMoreElements();) {
			XSLTNode child = (XSLTNode) e.nextElement();
			str += child.getXSLTString(indentnum + 1);
		}
		str += indent + "</" + getNodeName() + ">" + NL;

		return str;
	}

}
