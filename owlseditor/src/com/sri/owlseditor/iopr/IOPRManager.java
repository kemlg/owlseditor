// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License.  You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
// License for the specific language governing rights and limitations under
// the License.
//
// The Original Code is OWL-S Editor for Protege.
//
// The Initial Developer of the Original Code is SRI International.
// Portions created by the Initial Developer are Copyright (C) 2004 the
// Initial Developer.  All Rights Reserved.


package com.sri.owlseditor.iopr;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.JSplitPane;
import java.awt.Dimension;

import com.sri.owlseditor.ServiceSelectorListener;
import com.sri.owlseditor.util.ResourceDisplayWrapper;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * We need to add the top (and bottom?) box to this class.
 * 
 *  A window containing an IOPRSelector and a PropertyEditor for the currently
 *  selected IOPR. See documentation in classes of member instances.
 */

public class IOPRManager implements ServiceSelectorListener {

    private OWLModel		_okb;
    private Project			_project;
    private OWLIndividual	_inst;

    private JFrame			frame;
    private JTabbedPane		wholePane;

    private JPanel 			leftpanel;
    private IOPRTopBox		topbox;
    private IOPRSelector	m_ioprList;
    private ResourceDisplayWrapper		propertyEditor;
    private JSplitPane jsp;

    public IOPRManager (OWLModel okb) {

	_okb = okb;
	_project = _okb.getProject();

	frame = ComponentFactory.createFrame();
	frame.setTitle ("IOPR Manager");
	frame.setSize (1000, 800);
	frame.setDefaultCloseOperation (WindowConstants.HIDE_ON_CLOSE);
	wholePane = ComponentFactory.createTabbedPane (true);
	propertyEditor = new ResourceDisplayWrapper (_project);
	m_ioprList = new IOPRSelector (_okb, _project, propertyEditor);
	topbox = new IOPRTopBox (_okb, m_ioprList);
	topbox.addComboListener (m_ioprList);
		
	leftpanel = new JPanel();
	leftpanel.setLayout (new BoxLayout (leftpanel, BoxLayout.Y_AXIS));
	leftpanel.add (topbox);
	leftpanel.add (m_ioprList);
	topbox.setMinimumSize (new Dimension (300, 90));
	topbox.setMaximumSize (new Dimension (900, 90));
		
	jsp = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, leftpanel, propertyEditor);
	jsp.setDividerLocation (Math.max (topbox.desiredWidth, 300));
	leftpanel.setMinimumSize (new Dimension (300, 0));
	propertyEditor.setMinimumSize (new Dimension (300, 0));

	frame.getContentPane().add (jsp);
	frame.setVisible (false);
    }


	/** Not sure why we need this -Daniel */
    public void setInstance (OWLIndividual inst) {
    	_inst = inst;
    	//ioprList.updateAllLists (inst);
    }
    

    public OWLIndividual getInstance() {
    	return _inst;
    }


    public IOPRSelector getIOPRList() {
	return m_ioprList;
    }


    public void show () {
    	frame.setVisible(true);
    }
}

