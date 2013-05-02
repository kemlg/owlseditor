/*
 "The contents of this file are subject to the Mozilla Public License  
 Version 1.1 (the "License"); you may not use this file except in 
 compliance with the License.  You may obtain a copy of the License at 
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" 
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See 
 the 
 License for the specific language governing rights and limitations 
 under 
 the License.

 The Original Code is OWL-S Editor for Protege.

 The Initial Developer of the Original Code is SRI International. 
 Portions created by the Initial Developer are Copyright (C) 2004 the 
 Initial Developer.  All Rights Reserved.
 */

package com.sri.owlseditor.graph;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.WindowConstants;

import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protegex.owl.model.OWLModel;

/** The action handler for the GraphView toolbar button */
public class GraphViewAction extends AbstractAction {

	private OWLModel okb;
	private GraphDisplay _graphDisplay;

	/*
	 * public GraphViewAction(OWLModel okb) { super("",
	 * OWLSIcons.getGraphViewIcon());
	 * System.out.println("GRPH DISPLAY ------------>>>>>>>>>>>>> knowledgeBase = "
	 * + okb); }
	 */

	public GraphViewAction(GraphDisplay graphDisplay) {
		super("", OWLSIcons.getGraphViewIcon());
		// System.out.println("GraphViewAction() ---> GraphDisplay = " +
		// graphDisplay);
		_graphDisplay = graphDisplay;
	}

	public void actionPerformed(ActionEvent e) {
		// System.out.println("Graph Display selected");
		if (_graphDisplay != null) {
			_graphDisplay.setSize(1000, 400);
			_graphDisplay
					.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			_graphDisplay.setVisible(true);
		} else
			System.out.println("Graph Display is null");

	}

}
