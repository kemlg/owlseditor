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
package com.sri.owlseditor.widgets.dataflow;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * @author Daniel Elenius
 *
 */
public class ValueTextPanel extends AbstractBindingPanel {

	public ValueTextPanel(OWLModel okb){
		super(okb);
		
		JLabel valueTextEditorLabel;
		JTextArea textArea = ComponentFactory.createTextArea();
		textArea.setPreferredSize(new Dimension(180,180));
		JScrollPane textPane = ComponentFactory.createScrollPane(textArea);
		add(textArea);
	}
	
	public void setBinding(OWLIndividual binding, OWLIndividual perform){

	}
	
	public void deleteValueSpecifier(OWLIndividual binding){
	}

}
