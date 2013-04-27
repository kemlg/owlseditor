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
package com.sri.owlseditor.execute;

import java.net.URI;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.JTextField;
import com.sri.owlseditor.util.OWLSInstanceList;
import org.mindswap.owl.OWLValue;
import org.mindswap.owl.OWLIndividual;

class ProcessParameter extends Object {
	private URI parameterTypeURI = null;
	private String parameterName = null;
	private Object value = null;
	private int inputOrOutput = 0; // 0 for input, 1 for output.
	private JComponent cmp;

	public void setparameterType(URI uri) {
		parameterTypeURI = uri;
	}
	public void setparameterName(String name) {
		parameterName = name;
	}

	public void setJComponent(JComponent j) {
		cmp = j;
	}
	public URI getparameterTypeURI() {
		return parameterTypeURI;
	}
	public String getparameterName() {
		if ( parameterName.indexOf(":") > -1 ) 
			return parameterName.substring(parameterName.lastIndexOf(":")+1);
		return parameterName;
	}

	public boolean isXSDType() {
		String t = parameterTypeURI.toString();
		//http://www.owl-ontologies.com/unnamed.owl#TheDate

		if ( t.indexOf("http://www.w3.org/2001/XMLSchema")>-1 ) return true;
		return false;
	}
	public String getparameterResource() {
		String t = parameterTypeURI.toString();
		if ( t.lastIndexOf("#") > -1 )
			return t.substring(0,t.lastIndexOf("#")+1);
		return t;
	}	
	public String getparameterTypeString() {
		String t = parameterTypeURI.toString();
		if ( t.lastIndexOf("#") > -1 )
			return t.substring(t.lastIndexOf("#")+1);
		return t;
	}
	public Object getValue() {
		if ( cmp instanceof JTextComponent ) {
			value = ((JTextComponent) cmp).getText();
		} else if ( cmp instanceof JCheckBox ) {
			value = new Boolean(((JCheckBox) cmp).isSelected());
		} else if ( cmp instanceof JSpinner ) {
			value = ((JSpinner) cmp).getValue();
		} else if ( cmp instanceof OWLSInstanceList ) {
			value = ((OWLSInstanceList) cmp).getSelectedInstance();
		} else
			value = new String("");
		return value;
	}

	public void setValue(Object obj) {
		if ( cmp instanceof JTextArea ) {
			if ( obj instanceof OWLIndividual )
				((JTextArea) cmp).setText(((OWLIndividual)obj).toRDF());
			else
				((JTextArea) cmp).setText(obj.toString());
		} else if ( cmp instanceof JTextField ) {
				((JTextField) cmp).setText(obj.toString());
		} else if ( cmp instanceof JCheckBox ) {
			((JCheckBox) cmp).setSelected(((Boolean)obj).booleanValue());
		} else if ( cmp instanceof JSpinner ) {
			((JSpinner) cmp).setValue(obj);
		} else {
		}
		value = obj;
	}
	public String toString() {
		return value.toString();
	}
}


