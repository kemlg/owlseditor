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

import java.util.Enumeration;

import javax.swing.Icon;

import com.sri.owlseditor.xslt.XSLTNode;
import com.sri.owlseditor.xslt.owl2xml.AttributeNode;

import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/**
 * @author Daniel Elenius
 */
public class ObjectPropertyNode extends OWLNode {
	private OWLObjectProperty prop;

	public ObjectPropertyNode(OWLObjectProperty prop) {
		super(prop.getName());
		this.prop = prop;
		setAllowsChildren(true);
	}

	public Icon getIcon() {
		return OWLIcons.getImageIcon(OWLIcons.OWL_OBJECT_PROPERTY);
	}

	public RDFResource getResource() {
		return prop;
	}

	public String getXSLTString(int indentnum) {
		String str = "";
		String indent = "";
		for (int i = 0; i < indentnum; i++)
			indent += TAB;

		str = indent + "<" + getNodeName();
		String strBody = "";
		boolean notFirstAttribute = false;
		for (Enumeration e = children(); e.hasMoreElements();) {
			XSLTNode child = (XSLTNode) e.nextElement();

			if (child instanceof AttributeNode) {
				if (notFirstAttribute)
					str += NL + indent;// else str += " ";
				str += child.getXSLTString(indentnum);
				notFirstAttribute = true;
			} else
				strBody += child.getXSLTString(indentnum + 1);
		}
		str += ">" + NL;
		str += strBody;
		str += indent + "</" + getNodeName() + ">" + NL;

		return str;
	}

}
