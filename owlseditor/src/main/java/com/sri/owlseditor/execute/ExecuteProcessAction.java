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

import com.sri.owlseditor.ProcessList;
import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

//import com.sri.owlseditor.execute.ExecuteFrame;

public class ExecuteProcessAction extends AbstractAction {
	private OWLModel okb;
	private JFrame frame;
	private OWLIndividual selectedInstance = null;
	public static boolean RIGHT_TO_LEFT = false;
	private ProcessList list;

	public ExecuteProcessAction(OWLModel okb, ProcessList list) {
		super("", OWLSIcons.getExecuteIcon());
		this.okb = okb;
		this.list = list;
	}

	public void actionPerformed(ActionEvent e) {
		/*
		 * JOptionPane.showMessageDialog( (java.awt.Frame)
		 * Application.getMainWindow(),
		 * "This Feature is still under construction.\nYou will be able to execute atomic and composite\nprocesses using this feature."
		 * ,"Execution",JOptionPane.CLOSED_OPTION);
		 */
		ExecuteFrame frame = new ExecuteFrame(okb,
				(OWLIndividual) list.getSelectedInstance());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Display the window.
		frame.pack();
		// frame.setSize(600,550);
		frame.setVisible(true);
		SwingUtils.centerFrame(frame);
	}

	public void setProcess(OWLIndividual selectedInst) {
		selectedInstance = selectedInst;
	}
}