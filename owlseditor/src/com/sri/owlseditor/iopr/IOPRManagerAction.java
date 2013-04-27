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

package com.sri.owlseditor.iopr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.sri.owlseditor.util.OWLSIcons;
	
/** The action handler for the IOPR Manager toolbar button */
public class IOPRManagerAction extends AbstractAction {

    IOPRManager _ioprManager;

    public IOPRManagerAction(IOPRManager ioprManager) {
    	super("", OWLSIcons.getIOPRManagerIcon());
    	_ioprManager = ioprManager;
    }
	
    public void actionPerformed(ActionEvent e){
    	_ioprManager.show();
    }

}
