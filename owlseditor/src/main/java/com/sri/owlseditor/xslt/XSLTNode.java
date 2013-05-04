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

import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class XSLTNode extends DefaultMutableTreeNode {
	private XSLTFunction function = null;
	public String NL = System.getProperty("line.separator");
	public String TAB = "    ";

	public XSLTNode(String s) {
		super(s);
		setUserObject(s);
		function = XSLTFunction.createDefaultFunction();
	}

	public abstract String getXSLTString(int indentnum);

	public Icon getIcon() {
		return null;
	}

	public boolean isValid() {
		if (getNodeName().equals("") && !isRoot())
			return false;

		if (getAllowsChildren()) {
			for (Enumeration e = children(); e.hasMoreElements();)
				if (!((XSLTNode) e.nextElement()).isValid())
					return false;
		} else {
			XSLTFunction function = getXSLTFunction();
			for (int i = 0; i < function.getParameterNumbers(); i++)
				if (function.getParameter(i).equals(""))
					return false;
		}
		return true;
	}

	public String getNodeName() {
		return this.getUserObject().toString();
	}

	public void setNodeName(String s) {
		setUserObject(s);
	}

	public XSLTFunction getXSLTFunction() {
		return function;
	}

	public void setXSLTFunction(XSLTFunction f) {
		function = new XSLTFunction(f);
	}
}