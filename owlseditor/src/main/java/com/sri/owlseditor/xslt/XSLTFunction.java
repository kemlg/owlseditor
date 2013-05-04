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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class XSLTFunction extends Object {
	private String functionName = "";
	private List param;
	private int minParam = 0, maxParam = 0;

	public XSLTFunction(XSLTFunction f) {
		functionName = f.getFunctionName();
		minParam = f.getMinParameters();
		maxParam = f.getMaxParameters();
		param = new ArrayList();
		for (int i = 0; i < f.getParameterNumbers(); i++)
			param.add(f.getParameter(i));
	}

	public XSLTFunction(String n, int minparam, int maxparam) {
		functionName = n;
		minParam = minparam;
		maxParam = maxparam;
		param = new ArrayList();
		for (int i = 0; i < minParam; i++)
			param.add("");
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String s) {
		functionName = s;
	}

	public int getMinParameters() {
		return minParam;
	}

	public int getParameterNumbers() {
		return param.size();
	}

	public int getMaxParameters() {
		return maxParam;
	}

	public void removeLastParameter() {
		if (param.size() > 0)
			param.remove(param.size() - 1);
	}

	public String toString() {
		return functionName;
	}

	public void setParameter(int par, String value) {
		if (par >= 0 && par < maxParam) {
			while (param.size() <= par)
				param.add("");
			param.set(par, value);
		}
	}

	public String getParameter(int par) {
		if (par >= 0 && par < param.size())
			return param.get(par).toString();
		return "";
	}

	public String function2String() {
		boolean comma = false;
		String p;
		String temp = getFunctionName();
		if (temp.equals(""))
			return getParameter(0); // No Function, return the first parameter.
		temp += "(";
		for (int i = 0; i < maxParam; i++) {
			p = getParameter(i);
			if (!p.equals("")) {
				if (comma)
					temp += ", ";
				temp += p;
				comma = true;
			}
		}
		temp += ")";
		return temp;
	}

	public void resetParameters() {
		param.clear();
		for (int i = 0; i < minParam; i++)
			param.add("");
	}

	public static XSLTFunction createDefaultFunction() {
		return new XSLTFunction("", 1, 1);
	}

	public static Vector getXSLTFunctions() {
		Vector functions = new Vector();
		functions.add(createDefaultFunction());
		functions.add(new XSLTFunction("concat", 2, 100));
		functions.add(new XSLTFunction("copy", 1, 1));
		functions.add(new XSLTFunction("contains", 2, 2));
		functions.add(new XSLTFunction("copy-of", 3, 3));
		functions.add(new XSLTFunction("generate-id", 1, 1));
		functions.add(new XSLTFunction("format-number", 2, 3));
		functions.add(new XSLTFunction("normalize-space", 1, 1));
		functions.add(new XSLTFunction("substring", 3, 3));
		functions.add(new XSLTFunction("substring-before", 2, 2));
		functions.add(new XSLTFunction("substring-after", 2, 2));
		functions.add(new XSLTFunction("string-length", 1, 1));
		functions.add(new XSLTFunction("string", 1, 1));
		functions.add(new XSLTFunction("starts-with", 2, 2));
		functions.add(new XSLTFunction("translate", 3, 3));
		functions.add(new XSLTFunction("sum", 1, 1));
		functions.add(new XSLTFunction("floor", 1, 1));
		functions.add(new XSLTFunction("round", 1, 1));
		functions.add(new XSLTFunction("number", 0, 1));
		functions.add(new XSLTFunction("ceiling", 1, 1));
		return functions;
	}
}