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
package com.sri.owlseditor.widgets.wsdl;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JList;
import javax.swing.JPanel;

public class WSDLOwlSParamPanel extends JPanel {

	private WSDLOwlSParamWidget m_owlSParamWidget;

	public WSDLOwlSParamPanel() {
		m_owlSParamWidget = new WSDLOwlSParamWidget();

		JPanel upperBlank = new JPanel();
		upperBlank.setPreferredSize(new Dimension(180, 40));
		JPanel j2 = new JPanel();
		j2.setLayout(new GridLayout(0, 1));
		j2.add(upperBlank);
		j2.add(m_owlSParamWidget);
		m_owlSParamWidget.initialize();
		add(j2);
	}

	public JList getOwlSParamList() {
		return m_owlSParamWidget.getList();
	}

	public WSDLOwlSParamWidget getOwlSParamWidget() {
		return m_owlSParamWidget;
	}

	public void setPanelSize(Dimension d) {
		m_owlSParamWidget.setPreferredSize(new Dimension(d.width, 60));
	}
}
