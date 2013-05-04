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
package com.sri.owlseditor.widgets.wsdl;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JList;

import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.ui.OWLLabeledComponent;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.widget.SingleResourceWidget;

public class WSDLOwlSParamWidget extends SingleResourceWidget {

	private JList _list;
	private OWLLabeledComponent _olc;
	private JButton _removeButton;

	public WSDLOwlSParamWidget() {
	}

	/**
	 * We need this here to stop super.dispose() from being called, because that
	 * causes a NullPointerException. Hopefully, this solution doesn't cause any
	 * memory leaks or other weirdness.
	 */
	public void dispose() {
	}

	public void initialize() {
		_list = createList();
		_olc = new OWLLabeledComponent("", _list);
		add(_olc);

		setPreferredColumns(2);
		setPreferredRows(1);
	}

	public JList getList() {
		return _list;
	}

	/* Copied from InstanceFieldWidget */
	private JList createList() {
		JList list = ComponentFactory
				.createSingleItemList(getDoubleClickAction());
		list.setCellRenderer(FrameRenderer.createInstance());
		return list;
	}

	public void addAddHeaderButton(Action action) {
		action.putValue(Action.SMALL_ICON,
				OWLIcons.getAddIcon(OWLIcons.RDF_INDIVIDUAL));
		_olc.addHeaderButton(action);
	}

	public void addRemoveHeaderButton(Action action) {
		action.putValue(Action.SMALL_ICON,
				OWLIcons.getRemoveIcon(OWLIcons.RDF_INDIVIDUAL));
		_removeButton = _olc.addHeaderButton(action);
	}

	public void enableRemoveButton(boolean enabled) {
		_removeButton.setEnabled(enabled);
	}
}
