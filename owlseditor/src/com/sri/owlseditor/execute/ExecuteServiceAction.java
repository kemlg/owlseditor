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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.mindswap.utils.SwingUtils;

import com.sri.owlseditor.ServiceList;
import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
//import com.sri.owlseditor.execute.ExecuteFrame;

public class ExecuteServiceAction extends AbstractAction{
	private OWLModel okb;
	private JFrame frame;
	private OWLIndividual selectedInstance = null; 
    public static boolean RIGHT_TO_LEFT = false;
    private ServiceList list;
    
	public ExecuteServiceAction(OWLModel okb, ServiceList list) {
		super("", OWLSIcons.getExecuteIcon());
		this.okb = okb;
		this.list = list;
	}
    
	public void actionPerformed(ActionEvent e) {
		ExecuteFrame frame = new ExecuteFrame(okb, (OWLIndividual)list.getSelectedInstance());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if ( frame.Launch() ) {
			//Display the window.
		    frame.pack();
		    frame.AdjustFrame();
		    frame.setVisible(true);
			SwingUtils.centerFrame(frame);
		} else {
			JOptionPane.showMessageDialog(Application.getMainWindow(), "Error reading the process or its parameters.","Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}