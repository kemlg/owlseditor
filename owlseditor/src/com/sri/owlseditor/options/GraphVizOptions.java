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
package com.sri.owlseditor.options;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ComponentFactory;

/**
 * @author Daniel Elenius
 */
public class GraphVizOptions extends OptionPanel {
	public final static String OWLSEDITOR_GRAPHVIZ_PATH_KEY = "OWL-S Editor GraphViz path";
	public final static String DEFAULT_GRAPHVIZ_PATH = "C:\\Program Files\\ATT\\Graphviz\\bin\\dot.exe";

	private JTextField pathfield = new JTextField();
	private Project project;
	
	public GraphVizOptions(Project project){
		super();
		this.project = project;
		//Container contents = options.getContentPane();
    	//JPanel panel = ComponentFactory.createPanel();
    	//contents.add(panel);
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	JPanel labelpanel = ComponentFactory.createPanel();
    	labelpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	JLabel label = ComponentFactory.createLabel("Path to GraphViz");
    	label.setSize(new Dimension(350,20));
    	labelpanel.add(label);
    	add(labelpanel);
    	JPanel pathpanel = ComponentFactory.createPanel();
    	pathpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    	pathfield = ComponentFactory.createTextField();
    	pathpanel.add(pathfield);

    	// Set up the path in the text field
    	String path = (String)project.getClientInformation(OWLSEDITOR_GRAPHVIZ_PATH_KEY);
    	if (path == null)
    		pathfield.setText(DEFAULT_GRAPHVIZ_PATH);
    	else
    		pathfield.setText(path);
    	
    	class PathChooserAction extends AbstractAction{
    		Component parent;
    		JTextField pathfield;
    		
    		public PathChooserAction(Component parent, JTextField pathfield){
    			this.parent = parent;
    			this.pathfield = pathfield;
    		}
    		
    		public void actionPerformed(ActionEvent e){
    			JFileChooser chooser = ComponentFactory.createFileChooser("Choose a file", "");
    			int returnval = chooser.showOpenDialog(parent);
    			if (returnval == JFileChooser.APPROVE_OPTION){
    				File file = chooser.getSelectedFile();
    				try{
    					pathfield.setText(file.getCanonicalPath());
    				}
    				catch(IOException io){
    					System.out.println("ERROR! Couldn't open GraphViz executable.");
    					io.printStackTrace();
    				}
    			}
    		}
    	}
    	JButton pathbutton = ComponentFactory.createButton(new PathChooserAction(this, pathfield));
    	pathbutton.setText("Browse...");
    	pathpanel.add(pathbutton);

    	add(pathpanel);
	}
	
	public void commitChanges(){
		project.setClientInformation(OWLSEDITOR_GRAPHVIZ_PATH_KEY, pathfield.getText());
	}
	
}
