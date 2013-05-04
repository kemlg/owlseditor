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
package com.sri.owlseditor.cmp;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.ui.resourcedisplay.ResourceDisplay;

/**
 * The component holding the composite process editor. This is the main file of
 * this sub-package. It is a tabbed pane. One pane is the normal protege
 * properties editor for the selected instance. The other is our "visual editor"
 * for composite processes, the CompositionEditor class. It contains the tree
 * view (based on ijtree) and its associated buttons, The graphical visualizer,
 * and the control construct properties pane.
 */
public class CompositeProcessPane extends JTabbedPane {

	private ResourceDisplay properties;
	private CompositionEditor cmped;

	public CompositeProcessPane(Project project) {
		super();
		properties = new ResourceDisplay(project);
		cmped = new CompositionEditor(project);
		addTab("Visual Editor", cmped);
		addTab("Properties", properties);
	}

	/**
	 * This is a work-around for a protege 3.0 beta bug: It doesn't generate
	 * updates as it should, so we have to do it manually
	 */
	public void updatePropertiesPanel() {
		properties.update(properties.getGraphics());
	}

	/**
	 * Create and view the graph for inst, which must be a CompositeProcess
	 * Instance
	 */
	public void setInstance(OWLIndividual inst) {
		// System.out.println("Setting instance of CompositeProcessPane to " +
		// inst.getName());
		properties.setInstance(inst);
		cmped.setInstance(inst);
		// updatePropertiesPanel();
		// graphmodel.insertCompositeProcessVertex(inst, null);
	}
}
