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
package com.sri.owlseditor.wsdl;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.mindswap.utils.SwingUtils;

import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
//import edu.stanford.smi.protegex.owl.jena.URIResolver;
import edu.stanford.smi.protegex.owl.jena.parser.ProtegeOWLParser;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStore;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;
//import edu.stanford.smi.protegex.owl.ui.jena.OntPolicyManager;

/** The action handler for the Generate from WSDL toolbar button */
public class GenerateFromWSDLAction extends AbstractAction{
	private OWLModel okb;
	//private Project project;
	//private NamespaceManager nsm;
	private WSDL2OWLS wsdl2owls;
	private JDialog wsdlImport;
	public GenerateFromWSDLAction(OWLModel okb) {
		super("", OWLSIcons.getGenerateFromWSDLIcon());
		this.okb = okb;
		//project = okb.getProject();
		//nsm = okb.getNamespaceManager();
	}
	
	/*
	public class ChoicesPanel extends JPanel implements ActionListener {
		private JDialog parentFrame;
		private int choiceNumber = 1; 
		public ChoicesPanel(JDialog theParent) {
			super();
			choiceNumber = 1;
			parentFrame = theParent;
			JButton nextButton = new JButton("Next...");
			nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {			
					parentFrame.dispose();
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {				
					choiceNumber = 0;
					parentFrame.dispose();
				}
			});
			JRadioButton buildRadio = new JRadioButton("Build new project and import the WSDL file",true);
			JRadioButton importRadio = new JRadioButton("Import the WSDL file to the current project");
			buildRadio.setActionCommand("build");
			importRadio.setActionCommand("import");
			ButtonGroup theChoicesGroup = new ButtonGroup();
			theChoicesGroup.add(buildRadio);
			theChoicesGroup.add(importRadio);
			buildRadio.addActionListener(this);
			importRadio.addActionListener(this);
	        //Put the radio buttons in a column in a panel.
	        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
	        radioPanel.add(buildRadio);
	        radioPanel.add(importRadio);
	        add(radioPanel, BorderLayout.LINE_START);
	        //add(picture, BorderLayout.CENTER);
	        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			this.add(nextButton);
			this.add(cancelButton);
		}
	    public void actionPerformed(ActionEvent e) {
	    	if ( e.getActionCommand() == "build" ) choiceNumber = 1;
	    	else if ( e.getActionCommand() == "import" ) choiceNumber = 2;
	    }
	    public int getChoiceNumber() {
	    	return choiceNumber; 
	    }
	}
	*/
	
	public void actionPerformed(ActionEvent e) {
    	wsdlImport = new JDialog((java.awt.Frame) Application.getMainWindow(),"Import a WSDL File",true);
		wsdlImport.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		wsdlImport.setSize(700, 500);
		SwingUtils.centerFrame(wsdlImport);
		wsdl2owls = new WSDL2OWLS();
		wsdl2owls.theParent = wsdlImport;
		wsdlImport.getContentPane().add(wsdl2owls);
		wsdlImport.show();
		String defNameSpace = wsdl2owls.getDefaultNamespace();
		if ( wsdl2owls.isSuccessful() ) {
			TripleStoreModel tsm = okb.getTripleStoreModel();
		    TripleStore top = tsm.getTopTripleStore();
		    TripleStore current = tsm.getActiveTripleStore();
		    if (current != top){
		        tsm.setActiveTripleStore(top);
		        GenerateFromWSDLAction.importOWLFile(wsdl2owls.getOwlFileName(),okb,"wi",defNameSpace);
		        tsm.setActiveTripleStore(current);
		    }
		    else {
		    	GenerateFromWSDLAction.importOWLFile(wsdl2owls.getOwlFileName(),okb,"wi",defNameSpace);
		    }
		    //tsm.endTripleStoreChanges();
		}
	}

	static public boolean importOWLFile(String filename, OWLModel okb, String pref, String dns) {
		JenaOWLModel model = (JenaOWLModel)okb;
		NamespaceManager nsm = model.getNamespaceManager();
		Project project = model.getProject();
		boolean successful = false;
		//File file = new File(filename);

		String defaultNS = dns;
        successful = true;
        String uri = defaultNS;
        if (defaultNS.endsWith("#")) {
        	uri = defaultNS.substring(0, defaultNS.length() - 1);
        }
        //URI altURI = file.toURI();
        // Make a Prefix:
        String prefix = null;
        int index = 1;
        do {
        	prefix = pref + index++;
        } while (nsm.getNamespaceForPrefix(prefix) != null);

//        URIResolver uriresolver = model.getURIResolver();
        nsm.setPrefix(defaultNS,prefix);
//        uriresolver.setPhysicalURL(URI.create(uri),filename);
        try {
//        		OntPolicyManager.saveCurrentModel(project);
        		model.getDefaultOWLOntology().addImports(uri);
//        		ProtegeOWLParser.addImport(model,URI.create(uri));
        		model.getTripleStoreModel().updateEditableResourceState();
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
        //ProjectManager.getProjectManager().reloadUI(true);
        return successful;
	}
}
