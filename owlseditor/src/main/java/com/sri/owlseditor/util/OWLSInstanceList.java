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
package com.sri.owlseditor.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ListModel;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.ClassAdapter;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/* 
 * This class extends ModifiedDirectInstancesList with
 * 1) One-class support (from DirectInstancesListUtilites)
 * 2) getSelectedInstance() method
 * 
 * @author Daniel Elenius
 *
 */
public class OWLSInstanceList extends OWLSDirectInstancesList {
	private ArrayList headerButtons;

	protected Project project;
	protected OWLModel model;
	protected String clsName;
	protected OWLNamedClass cls;
	
	static List<OWLSInstanceList>	instances;
	
	static {
		instances = new LinkedList<OWLSInstanceList>();
	}

	public OWLSInstanceList(Project project, String clsName, boolean checkboxes) {
		super(project, checkboxes);
		this.project = project;
		model = (OWLModel) project.getKnowledgeBase();
		this.clsName = clsName;
		cls = model.getOWLNamedClass(clsName);
		setupOneClassMode();
		setupHeaderButtons();
		instances.add(this);
	}

	public RDFIndividual getSelectedInstance() {
		Iterator it = getSelection().iterator();
		if (it.hasNext())
			return (RDFIndividual) it.next();
		else
			return null;
	}

	public void update(RDFIndividual instance) {
		// System.out.println("OWLSInstanceList.update()");
		if (instance == null)
			clearSelection();
		else if (instance.hasRDFType(cls, true))
			setSelectedInstance(instance);
		else
			clearSelection();
	}

	/**
	 * Sets up the DirectInstancesList to work on only one class (and its
	 * subclasses).
	 * 
	 */
	private void setupOneClassMode() {
		ArrayList clses = new ArrayList();
		OWLNamedClass cls = model.getOWLNamedClass(clsName);
		clses.add(cls);
		ArrayList subclasses = new ArrayList(cls.getNamedSubclasses(true));
		if (subclasses.contains(cls))
			subclasses.remove(cls);
		clses.addAll(subclasses);
		setClses(clses);
		OWLSModelAdapter modellistener = new OWLSModelAdapter(clses, this);
		model.addModelListener(modellistener);
		OWLSClassAdapter classlistener = new OWLSClassAdapter(this, model, cls);
		model.addClassListener(classlistener);
	}

	/** Adds a button to the left of the existing ones. */
	protected void addHeaderButton(Action newAction) {
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c instanceof LabeledComponent) {
				LabeledComponent lc = (LabeledComponent) c;

				// remove the existing buttons
				while (lc.getHeaderButtons().size() > 0) {
					lc.removeHeaderButton(0);
				}

				// add the new one
				lc.addHeaderButton(newAction);

				// put the old ones back
				Iterator it = headerButtons.iterator();
				while (it.hasNext()) {
					Action oldAction = (Action) it.next();
					lc.addHeaderButton(oldAction);
				}

				// store the new action in our list
				headerButtons.add(0, newAction);
			}
		}
	}

	/**
	 * Change from protege-standard to protege-owl looks and behavior.
	 */
	private void setupHeaderButtons() {
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c instanceof LabeledComponent) {
				LabeledComponent lc = (LabeledComponent) c;
				java.util.List actions = new ArrayList(
						lc.getHeaderButtonActions());
				// viewAction = (Action) actions.get(0);
				// Action viewReferencesAction = (Action) actions.get(1);
				Action createAction = (Action) actions.get(2);
				createAction.putValue(Action.SMALL_ICON,
						OWLIcons.getCreateIcon(OWLIcons.RDF_INDIVIDUAL));
				Action copyAction = (Action) actions.get(3);
				Action deleteAction = (Action) actions.get(4);
				deleteAction.putValue(Action.SMALL_ICON,
						OWLIcons.getDeleteIcon());
				while (lc.getHeaderButtons().size() > 0) {
					lc.removeHeaderButton(0);
				}
				lc.addHeaderButton(createAction);
				lc.addHeaderButton(copyAction);
				lc.addHeaderButton(deleteAction);

				headerButtons = new ArrayList();
				headerButtons.add(createAction);
				headerButtons.add(copyAction);
				headerButtons.add(deleteAction);
			}
		}
	}

} // end of class OWLSInstanceList

/**
 * A Listener that reacts to: 1) Renaming of classes in this instance pane 2)
 * Adding of subclasses to this instance pane 3) Removing of subclasses to this
 * instance pane
 */
class OWLSModelAdapter extends ModelAdapter {
	private Collection _clses;
	private OWLSInstanceList ilist;

	public OWLSModelAdapter(Collection _clses, OWLSInstanceList list) {
		this._clses = _clses;
		ilist = list;
	}

	public void resourceReplaced(RDFResource oldResource,
			RDFResource newResource, String oldName) {
		// Handle class renaming, because we need to keep _clses in sync
		// with the current names of user-defiend OWL-S subclasses
		if (oldResource instanceof OWLNamedClass) {
			Iterator it = _clses.iterator();
			while (it.hasNext()) {
				OWLNamedClass oldcls = (OWLNamedClass) it.next();
				if (oldcls.getName().equals(oldName)) {
					oldcls.rename(oldResource.getName());
				}
			}
		}
		// Redraw the instance list if one of the instances
		// in it has changed name
		else if (oldResource instanceof OWLIndividual) {
			// There should be a nicer way to do the following
			SimpleListModel	target;
			SelectableList list = (SelectableList) ilist.getSelectable();
			ListModel listModel = list.getModel();
			target = new SimpleListModel();
			System.out.println("oldResource is: " + oldResource);
			System.out.println("newResource is: " + newResource);
			for (int i = 0; i < listModel.getSize(); i++) {
				RDFIndividual inst = (RDFIndividual) listModel.getElementAt(i);
				System.out.println("inst: " + inst);
				if (inst.getName().equals(oldResource.getName())) {
					System.out.println("equals!!! " + inst.hashCode() + " : " + oldResource.hashCode());
					System.out.println("Removing " + i);
					System.out.println("new inst: " + inst);
					target.addValue(newResource, i);
				} else {
					target.addValue(inst, i);
				}
			}
			((SelectableList)ilist.getSelectable()).setModel(target);
			ilist.paint(ilist.getGraphics());
			
			Iterator<OWLSInstanceList> it = OWLSInstanceList.instances.iterator();
			while(it.hasNext()) {
				OWLSInstanceList	oil;
				
				oil = it.next();
				oil.reload();
			}
		}
	}

//	public void resourceNameChanged(RDFResource resource, String oldName) {
//		System.out.println("OWLSModelAdapter.resourceNameChanged()");
//		System.out.println(oldName + " to " + resource.getName());
//		System.out.println(resource);
//		// Handle class renaming, because we need to keep _clses in sync
//		// with the current names of user-defiend OWL-S subclasses
//		if (resource instanceof OWLNamedClass) {
//			Iterator it = _clses.iterator();
//			while (it.hasNext()) {
//				OWLNamedClass oldcls = (OWLNamedClass) it.next();
//				if (oldcls.getName().equals(oldName)) {
//					oldcls.rename(resource.getName());
//				}
//			}
//		}
//		// Redraw the instance list if one of the instances
//		// in it has changed name
//		else if (resource instanceof OWLIndividual) {
//			// There should be a nicer way to do the following
//			SelectableList list = (SelectableList) ilist.getSelectable();
//			ListModel listModel = list.getModel();
//			for (int i = listModel.getSize() - 1; i >= 0; i--) {
//				RDFIndividual inst = (RDFIndividual) listModel.getElementAt(i);
//				if (inst.getName().equals(resource.getName())) {
//					ilist.paint(ilist.getGraphics());
//					break;
//				}
//			}
//		}
//	}

	// See if a newly created class is a subclass of _cls.
	// If so, add it to _clses, and attach a class listener to it to detect if
	// it is moved outside of its OWL-S hierarchy.
	public void classCreated(RDFSClass cls) {
		Collection supers = cls.getNamedSuperclasses(true);
		Iterator it1 = supers.iterator();
		boolean is_subclass = false;
		while (it1.hasNext() && is_subclass == false) {
			OWLNamedClass supercls = (OWLNamedClass) it1.next();
			Iterator it2 = _clses.iterator();
			while (it2.hasNext() && is_subclass == false) {
				OWLNamedClass oldcls = (OWLNamedClass) it2.next();
				if (oldcls.getName().equals(supercls.getName()))
					is_subclass = true;
			}
		}
		if (is_subclass) {
			_clses.add(cls);
			ilist.setClses(_clses);
			// What is this??
			// ArrayList instances = new ArrayList(getModel().getValues());
			// instances.addAll(cls.getInstances(false));
			// setClses(instances);
		}
	}

	// See if a newly deleted class is in _clses. If so,
	// remove it from _clses. Note that we do not need to
	// delete instances, because a class with instances
	// cannot be deleted
	public void classDeleted(RDFSClass cls) {
		Iterator it = _clses.iterator();
		while (it.hasNext()) {
			OWLNamedClass oldcls = (OWLNamedClass) it.next();
			if (oldcls.getName() == null) {
				_clses.remove(cls);
				ilist.setClses(_clses);
				// What is this??
				// setClses(_cls.getInstances(true));
				break;
			}
		}
	}
};

/**
 * 
 * A listener that reacts to moving of classes into/out from the OWL-S class
 * hierarchy. Used by the four main OWL-S instance lists.
 * 
 */
class OWLSClassAdapter extends ClassAdapter {
	private OWLSInstanceList list;
	private OWLNamedClass owlsClass; // the OWL-S class to match against
										// (service, profile, etc)
	private OWLNamedClass service;
	private OWLNamedClass profile;
	private OWLNamedClass process;
	private OWLNamedClass grounding;

	public OWLSClassAdapter(OWLSInstanceList list, OWLModel model,
			OWLNamedClass cls) {
		this.list = list;
		owlsClass = cls;
		service = model.getOWLNamedClass("service:Service");
		profile = model.getOWLNamedClass("profile:Profile");
		process = model.getOWLNamedClass("process:Process");
		grounding = model.getOWLNamedClass("grounding:WsdlGrounding");
	}

	public void superclassRemoved(RDFSClass cls, RDFSClass superclass) {
		ArrayList supers = new ArrayList(superclass.getNamedSuperclasses(true));
		supers.add(superclass);
		if (supers.contains(owlsClass)) {
			ArrayList clses = new ArrayList(list.getClses());
			clses.remove(cls);
			list.setClses(clses);
		}
	}

	public void superclassAdded(RDFSClass cls, RDFSClass superclass) {
		ArrayList supers = new ArrayList(superclass.getNamedSuperclasses(true));
		supers.add(superclass);
		if (supers.contains(owlsClass)) {
			ArrayList clses = new ArrayList(list.getClses());
			if (!clses.contains(cls)) {
				clses.add(cls);
				list.setClses(clses);
			}
		}
	}
}
