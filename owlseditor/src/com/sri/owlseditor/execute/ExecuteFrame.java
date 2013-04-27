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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.service.Service;
import org.mindswap.query.ValueMap;
import org.mindswap.swrl.Variable;

import com.hp.hpl.jena.ontology.OntModel;
import com.sri.owlseditor.util.OWLSInstanceList;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;

public class ExecuteFrame extends JFrame {
	private JenaOWLModel okb;
	private JButton executeButton, exitButton;
	private OWLIndividual theService = null;
	private Collection Inputs, Outputs;
	private OWLOntology ont = null;
	private OntModel ontmodel = null;
	private ExecutionEngine engine = null;
	private boolean launchFrame = false;
	private JPanel parameterpanel, controlpanel;
	private JScrollPane inputpanel, outputpanel;
	private JTextArea executeLog;
	private JSplitPane splitter;
	private JScrollPane executeLogPane;
    
	public ExecuteFrame(OWLModel okb, OWLIndividual selectedInst) {
		super("");
		this.okb = (JenaOWLModel) okb;	
		theService = selectedInst;
		this.setTitle("Executing " + theService.getName());
		Inputs = new ArrayList();
		Outputs = new ArrayList();
		if ( !FetchParametersFromProcess() ) return;
		Container pane = getContentPane();
	    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
	    // Parameter Panel	    
	    parameterpanel = new JPanel();
	    parameterpanel.setLayout(new BoxLayout(parameterpanel, BoxLayout.X_AXIS));
	    // Input and Output Panels
	    inputpanel = createInputOutputPanel(true);
	    outputpanel = createInputOutputPanel(false);
	    parameterpanel.add(inputpanel);
	    parameterpanel.add(outputpanel);
	    // Control Panel
	    executeButton = new JButton("Execute");
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				onExecute(e);
			}
		});		
		exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExit();
			}
		});
		executeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		Box buttonsbox = Box.createVerticalBox();
		buttonsbox.add(executeButton);
		buttonsbox.add(exitButton);	    
	    controlpanel = new JPanel();
	    controlpanel.setLayout(new BoxLayout(controlpanel,BoxLayout.X_AXIS));
	    TitledBorder title4;
	    title4 = BorderFactory.createTitledBorder("Control");
	    controlpanel.setBorder(title4);
	    executeLog = new JTextArea(8,50);
	    executeLogPane = new JScrollPane(executeLog);
	    executeLog.setEditable(false);
		controlpanel.add(executeLogPane,BorderLayout.LINE_START);
		controlpanel.add(Box.createRigidArea(new Dimension(15,0)));
		controlpanel.add(buttonsbox,BorderLayout.LINE_END);
		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,parameterpanel,controlpanel);
	    pane.add(splitter);
	    launchFrame = true; // Everything is ok, let's launch the frame.
	}
	public void AdjustFrame() {
		if ( launchFrame ) {
			Dimension dim = this.getSize();
			int height = (dim.height+20 < 500)? dim.height+20 : 500;
			int width = dim.width+10;
			this.setSize(width,height);
			splitter.setDividerLocation(0.7);
		}
	}
	
	JScrollPane createInputOutputPanel(boolean inputOroutput) {
		// inputOroutput=true for inputpanel....inputOroutput=false for outputpanel
		JPanel pp = new JPanel();
		pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
		pp.add(Box.createRigidArea(new Dimension(20,0)));
		Box mainbox = Box.createVerticalBox();
		TitledBorder title;
		Collection c;
		if ( inputOroutput )  {
			title = BorderFactory.createTitledBorder("Inputs");
			c = Inputs;
		} else {
			title = BorderFactory.createTitledBorder("Outputs");
			c = Outputs;
		}
	    pp.setBorder(title);
	    Iterator it = c.iterator();
	    while ( it.hasNext() ) {
	    	ProcessParameter x = (ProcessParameter) it.next();
	    	JLabel label1 = new JLabel(x.getparameterName() + ":", JLabel.TRAILING);
	    	label1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	mainbox.add(label1);
	    	JComponent cmp;
	    	JComponent container;
	    	String t = x.getparameterTypeString();
	    	if ( x.isXSDType() && inputOroutput ) {
	    		if ( t.equals("string") ) {
	    			cmp = new JTextField(30);
	    			cmp.setMaximumSize(new Dimension(1000,30));
	    		} else if ( t.equals("boolean") ) {
	    			cmp = new JCheckBox();
	    		} else if ( t.equals("decimal") || t.equals("integer") || t.equals("int") || t.equals("long") ) {
	    			cmp = new JSpinner();
	    			cmp.setMaximumSize(new Dimension(1000,30));
	    		} else {
	    			cmp = new JTextField(30);
	    			cmp.setMaximumSize(new Dimension(1000,30));
	    		}
	    		container = cmp;
	    	} else {
	    		if ( inputOroutput ) { // Instant Chooser for inputs
		    		Collection n = new ArrayList();
		    		String prefix = okb.getNamespaceManager().getPrefix(x.getparameterResource());
		    		String classname = x.getparameterTypeString();
		    		if ( prefix != null ) classname = prefix + ":" + classname;
		    		cmp = new OWLSInstanceList(okb.getProject(), classname ,false);
		    		OWLNamedClass cls = okb.getOWLNamedClass(classname);
		    		if ( cls != null ) { 
		    			n.add(cls);
		    			((OWLSInstanceList)cmp).setClses(n);
		    		}
		    		container = cmp;
	    		} else { // Textfield for outputs 
	    			cmp = new JTextArea(3,30);
	    			((JTextArea)cmp).setLineWrap(true);
	    			((JTextArea)cmp).setWrapStyleWord(true);
	    			((JTextArea)cmp).setEditable(false);
	    			container = new JScrollPane(cmp);
	    		}
	    	}
	    	//label1.setLabelFor(cmp);
	    	x.setJComponent(cmp);
	    	mainbox.add(container);
	    	mainbox.add(Box.createRigidArea(new Dimension(30,0)));
	    }
	    pp.add(mainbox);
	    pp.add(Box.createRigidArea(new Dimension(20,0)));
		return new JScrollPane(pp);
	}
	
	void onExecute(ActionEvent e) {
//		 create empty Kb
		OWLKnowledgeBase kb = OWLFactory.createKB();
		ontmodel = okb.getOntModel();
		ont = kb.createOntology(null, null, ontmodel);
		// Cleanup Outputs
		Iterator it = Outputs.iterator();
		while ( it.hasNext() ) {
			ProcessParameter x = (ProcessParameter) it.next();
			x.setValue("");
		}
		// get the service
		executeLog.setText("");
		Log("Getting the service from the knowledgebase.");
		List services = ont.getServices();
		Service service = null;
		it = services.iterator();
		while ( it.hasNext() ) {
			Service sTemp = (Service) it.next(); 
			if ( sTemp.getURI().toString().equals(theService.getURI().toString()) ) {
				service = sTemp; 
				break;
			}
		}
		if ( service == null ) {
			Log("Error: Service could not be found in the knoledgebase.");
			return;
		}
	    // get the process of the service
	    Log("Getting the process from the service.");
	    org.mindswap.owls.process.Process process = null;
	    process = service.getProcess();
	    // create an empty value map
	    ValueMap values = new ValueMap();
	    // set the value of input parameter
	    it = Inputs.iterator();
	    Log("Preparing the input parameters.");
	    while ( it.hasNext() ) {
	    	ProcessParameter x = (ProcessParameter) it.next();
	    	Object value = x.getValue();
	    	Variable p = process.getInputs().getParameter(x.getparameterName());
	    	if ( p == null ) {
	    		Log("Error: Parameter name is null.");
				return;
	    	}
	    	if ( value instanceof RDFIndividual ) {
	    		values.setValue(p, ont.getIndividual(URI.create(((RDFIndividual)value).getURI())));
	    	} else { 
	    		values.setDataValue(p, value.toString());
	    	}
	    }
	    // execute the process with the given input bindings
	    Log("Executing the service.");
	    engine = new ExecutionEngine(this, process, values);
	    engine.start();
	}
	
	void OnExecutionEvent(int event) {
		switch ( event ) {
			case ExecutionEngine.EXECUTEION_FAILED:
				
				break;
			case ExecutionEngine.EXECUTEION_SUCCESSFUL:
			    Iterator it = Outputs.iterator();
			    Log("Fetching the output.");
			    boolean reload = false;
			    Project project = okb.getProject();
			    while ( it.hasNext() ) {
			    	ProcessParameter x = (ProcessParameter) it.next();	    
			    	OWLValue outValue = engine.GetOutput().getValue(engine.GetProcess().getOutputs().getParameter(x.getparameterName()));
			    	/*if ( outValue instanceof OWLValue ) {
			    		reload = true;
						String data = ((org.mindswap.owl.OWLIndividual)outValue).toRDF();
						File instfile = null;
						try {
							instfile = File.createTempFile("Instance_",".owl",new File("."));
							FileOutputStream outstream = new FileOutputStream(instfile);
							outstream.write(data.getBytes());
							outstream.flush();
							outstream.close();
			    		} catch ( Exception e ) {
			    			e.printStackTrace();
			    			continue;
			    		}
			    		
			    		GenerateFromWSDLAction.importOWLFile(instfile.getName(),okb,"in",false);
			    	}*/
			    	x.setValue(outValue);
			    }
			    if ( reload ) OWLUtil.saveAndReloadProject();
			    Log("Done.");
			    break;
		}
	}
	
	void onExit() {
		dispose();
	}
	
	boolean FetchParametersFromProcess() {
		OWLProperty processes = okb.getOWLProperty("service:describedBy");
		OWLIndividual theProcess = (OWLIndividual) theService.getPropertyValue(processes);
		if ( theProcess == null ) return false;
		Collection slotvalues;
		Iterator it2;
		OWLIndividual slotvalue = null;
		OWLObjectProperty slot = okb.getOWLObjectProperty("process:hasInput");
		slotvalues = theProcess.getPropertyValues(slot, true);		
		it2 = slotvalues.iterator();
		while(it2.hasNext()) {
		    slotvalue = (OWLIndividual)it2.next();
			String slotname = slot.getName();
			OWLIndividual inst = slotvalue;
			ProcessParameter x = new ProcessParameter();
			OWLDatatypeProperty paramType = okb.getOWLDatatypeProperty("process:parameterType");
			if ( inst.getPropertyValue(paramType) == null ) return false;
			x.setparameterType(URI.create(inst.getPropertyValue(paramType).toString()));
			x.setparameterName(inst.getName());
			Inputs.add(x);
		}
		slot = okb.getOWLObjectProperty("process:hasOutput");
		slotvalues = theProcess.getPropertyValues(slot, true);		
		it2 = slotvalues.iterator();
		while(it2.hasNext()) {
		    slotvalue = (OWLIndividual)it2.next();
			String slotname = slot.getName();
			OWLIndividual inst = slotvalue;
			ProcessParameter x = new ProcessParameter();
			OWLDatatypeProperty paramType = okb.getOWLDatatypeProperty("process:parameterType");
			if ( inst.getPropertyValue(paramType) == null ) return false;
			x.setparameterType(URI.create(inst.getPropertyValue(paramType).toString()));
			x.setparameterName(inst.getName());
			Outputs.add(x);
		}
	    return true;
	}
	
	void Log(String s) {
		executeLog.append(s + "\n");
		executeLog.updateUI();
	}
	
	public boolean Launch() {
		return launchFrame;
	}
}