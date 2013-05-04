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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.sri.owlseditor.util.CleanerListener;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ComponentFactory;

/**
 * @author Daniel Elenius
 */
public class OptionsManager implements CleanerListener {
	private Project project;
	private JFrame instance;

	public OptionsManager(Project project) {
		this.project = project;
	}

	public JFrame getOptionsWindow() {
		if (instance == null) {
			instance = createOptionsWindow();
		}
		return instance;
	}

	public void cleanup() {
		instance = null;
		project = null;
	}

	private JFrame createOptionsWindow() {
		JFrame options = ComponentFactory.createFrame();
		options.setTitle("Options");

		class CloseAction extends AbstractAction {
			private JTextField pathfield;
			private JFrame window;
			private OptionPanel graphvizOptions;
			private OptionPanel searchOptions;

			public CloseAction(JFrame window, OptionPanel graphvizOptions,
					OptionPanel searchOptions) {
				this.window = window;
				this.graphvizOptions = graphvizOptions;
				this.searchOptions = searchOptions;
			}

			public void actionPerformed(ActionEvent e) {
				graphvizOptions.commitChanges();
				searchOptions.commitChanges();
				window.setVisible(false);
			}
		}

		options.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		options.setSize(new Dimension(400, 350));
		Container contents = options.getContentPane();
		contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

		/* Create and add all the option panels for the different tabs */
		OptionPanel graphvizOptions = new GraphVizOptions(project);
		OptionPanel searchOptions = new SearchOptions(project);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Graph-drawing", graphvizOptions);
		tabbedPane.addTab("Service Search", searchOptions);

		JButton okbutton;
		okbutton = ComponentFactory.createButton(new CloseAction(options,
				graphvizOptions, searchOptions));
		okbutton.setText("OK");

		JPanel bottombuttonpanel = ComponentFactory.createPanel();
		bottombuttonpanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		bottombuttonpanel.add(okbutton, BorderLayout.SOUTH);
		contents.add(tabbedPane);
		contents.add(bottombuttonpanel);
		return options;
	}

}
