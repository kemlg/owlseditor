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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class IOPRBottomBox extends JPanel {

	private Box box;
	private JButton helpButton;
	private JButton validateButton;
	private JButton updateButton;
	private JButton cancelButton;

	IOPRBottomBox() {

		box = Box.createHorizontalBox();
		// box.add(Box.createHorizontalStrut(300));
		box.add(Box.createHorizontalGlue());

		helpButton = new JButton("Help");
		validateButton = new JButton("Validate");
		updateButton = new JButton("Update");
		cancelButton = new JButton("Cancel");
		box.add(helpButton);
		box.add(validateButton);
		box.add(updateButton);
		box.add(cancelButton);
	}

	JComponent getContentsComponent() {
		return box;
	}
}
