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

import javax.swing.Icon;

import com.sri.owlseditor.util.OWLSIcons;
import com.sri.owlseditor.xslt.XSLTFunction;
import com.sri.owlseditor.xslt.XSLTNode;

/**
 * @author Daniel Elenius
 */
public class AttributeNode extends XSLTNode {
	public final static String DEFAULT_ATTRIBUTE_NAME = "Attribute";

	public AttributeNode(){
		super("Attribute");
		setAllowsChildren(false);
	}
	
	public Icon getIcon(){
		return OWLSIcons.getXMLAttributeIcon();
	}
	
	public String getXSLTString(int indentnum) {
		String indent = "";
		for (int i=0;i<indentnum;i++) indent += TAB;
		String str = indent + "<xsl:attribute name=\"" + getNodeName();
		
		XSLTFunction function = getXSLTFunction();
		if ( !function.getFunctionName().equals("") )
			str += " select=\"" + function.function2String() + "\"/>" + NL;
		else{
			str += "\">" + NL;
			if (!function.getParameter(0).equals(""))
				str += indent + TAB + function.getParameter(0) + NL;
		}	
		str += indent + "</xsl:attribute>" + NL;
		return str;
	}

}
