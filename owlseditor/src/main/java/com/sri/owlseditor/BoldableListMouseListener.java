package com.sri.owlseditor;

import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;

public class BoldableListMouseListener implements SelectionListener {
	private BoldableOWLSInstanceList list;

	public BoldableListMouseListener(
			BoldableOWLSInstanceList list) {
		this.list = list;
	}

	@Override
	public void selectionChanged(SelectionEvent arg0) {
		System.out.println(list.clsName + " : " + arg0.getSelectable().getSelection().size());
		if(arg0.getSelectable().getSelection().size() > 0) {
			System.out.println("New last clicked: " + list);
			BoldableOWLSInstanceList.lastClicked = list;
		}
	}
}
