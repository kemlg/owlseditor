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

package com.sri.owlseditor.consistency;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import java.util.Vector;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import com.sri.owlseditor.iopr.*;
import javax.swing.JOptionPane;
import com.sri.owlseditor.util.OWLSIcons;

public class ConsistencyCheckAction extends AbstractAction {

    OWLModel m_okb;

    public ConsistencyCheckAction (OWLModel okb) {
	super ("", OWLSIcons.getConsistencyIcon());
	m_okb = okb;
    }
	
    public void actionPerformed (ActionEvent e) {
	Vector vGlobalConsistency = new Vector();
        IOPRConsistencyCheck.getInstance(m_okb, null).checkIOPRProcessAndProfiles (vGlobalConsistency);
	IOPRConsistencyCheck.getInstance(m_okb, null).checkIOPRProcessParameterType (vGlobalConsistency);

	if (vGlobalConsistency.size() == 0)
	    JOptionPane.showMessageDialog (null, "Consistency Check Completed - No Inconsistencies Found");
	else
	    ConsistencyCheckDisplay.getInstance().updateDisplay (vGlobalConsistency);
    }
}
