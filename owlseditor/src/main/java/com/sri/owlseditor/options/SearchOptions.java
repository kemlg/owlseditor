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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Project;

/**
 * @author Daniel Elenius
 * 
 */
public class SearchOptions extends OptionPanel {
	public final static String OWLSEDITOR_UDDI_INQUIRY_URL_KEY = "OWL-S Editor Inquiry URL";
	public final static String OWLSEDITOR_UDDI_PUBLISH_URL_KEY = "OWL-S Editor Publish URL";
	public final static String OWLSEDITOR_UDDI_USERNAME_KEY = "OWL-S Editor Registry Username";
	public final static String OWLSEDITOR_UDDI_PASSWORD_KEY = "OWL-S Editor Registry Password";

	public final static String DEFAULT_UDDI_INQUIRY_URL = "http://arta.cimds.ri.cmu.edu:7080/juddi/inquiry";
	public final static String DEFAULT_UDDI_PUBLISH_URL = "http://arta.cimds.ri.cmu.edu:7080/juddi/publish";
	public final static String DEFAULT_UDDI_USERNAME = "juddi";
	public final static String DEFAULT_UDDI_PASSWORD = "password";

	private Project project;
	private JTextField inquiryURLfield = new JTextField();
	private JTextField publishURLfield = new JTextField();
	private JTextField usernamefield = new JTextField();
	private JTextField passwordfield = new JTextField();

	public SearchOptions(Project project) {
		super();
		this.project = project;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(new JLabel("Inquiry URL"));
		add(inquiryURLfield);
		String inquiryURL = (String) project
				.getClientInformation(OWLSEDITOR_UDDI_INQUIRY_URL_KEY);
		if (inquiryURL == null)
			inquiryURLfield.setText(DEFAULT_UDDI_INQUIRY_URL);
		else
			inquiryURLfield.setText(inquiryURL);

		add(new JLabel("Publish URL"));
		add(publishURLfield);
		String publishURL = (String) project
				.getClientInformation(OWLSEDITOR_UDDI_PUBLISH_URL_KEY);
		if (publishURL == null)
			publishURLfield.setText(DEFAULT_UDDI_PUBLISH_URL);
		else
			publishURLfield.setText(publishURL);

		add(new JLabel("Registry username"));
		add(usernamefield);
		String username = (String) project
				.getClientInformation(OWLSEDITOR_UDDI_USERNAME_KEY);
		if (username == null)
			usernamefield.setText(DEFAULT_UDDI_USERNAME);
		else
			usernamefield.setText(username);

		add(new JLabel("Registry password"));
		add(passwordfield);
		String password = (String) project
				.getClientInformation(OWLSEDITOR_UDDI_PASSWORD_KEY);
		if (password == null)
			passwordfield.setText(DEFAULT_UDDI_PASSWORD);
		else
			passwordfield.setText(password);
	}

	public void commitChanges() {
		project.setClientInformation(OWLSEDITOR_UDDI_INQUIRY_URL_KEY,
				inquiryURLfield.getText());
		project.setClientInformation(OWLSEDITOR_UDDI_PUBLISH_URL_KEY,
				publishURLfield.getText());
		project.setClientInformation(OWLSEDITOR_UDDI_USERNAME_KEY,
				usernamefield.getText());
		project.setClientInformation(OWLSEDITOR_UDDI_PASSWORD_KEY,
				passwordfield.getText());
	}

}
