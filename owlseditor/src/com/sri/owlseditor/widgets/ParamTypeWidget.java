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
package com.sri.owlseditor.widgets;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JList;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.RemoveAction;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protege.widget.AbstractListWidget;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.ui.ResourceRenderer;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMetadata;

/**
 * This uses StringListWidget, which is in Protege-core rather than Protege-OWL,
 * but there's nothing we can do about that right now.
 */
public class ParamTypeWidget extends AbstractListWidget implements
		OWLWidgetMetadata {
	private OWLModel m_okb = null;
	private AllowableAction _createAction;
	private AllowableAction _removeAction;
	private JList _list;
	private HashMap listeners;
	private Collection datatypes;

	public int getSuitability(RDFSNamedClass cls, RDFProperty property) {
		System.out.println("ParamTypeWidget.getSuitability: "
				+ property.getName());
		if (property.getPrefixedName().equals("process:parameterType"))
			return OWLWidgetMetadata.DEFAULT + 1;
		else
			return OWLWidgetMetadata.NOT_SUITABLE;
	}

	// initialization
	public void initialize() {
		// super.initialize ();
		Action editAction = getEditAction();
		super.initialize(editAction);
		addButton(editAction);
		addButton(getCreateClassAction());
		addButton(getCreateXSDAction());
		addButton(getRemoveAction());

		setPreferredColumns(3);
		setPreferredRows(2);
		m_okb = (OWLModel) getKnowledgeBase();
		_list = getList();

		datatypes = getSortedDatatypes();
		listeners = new HashMap();
	}

	/** Override to get rid of red-painted values */
	public void setInstance(Instance instance) {
		super.setInstance(instance);
		_list.setCellRenderer(new ResourceRenderer());
	}

	/*
	 * This method traps calls to addItem in the parent class, and first adds a
	 * listener for class delete events.
	 */
	public void addItem(Object o) {
		if (o instanceof RDFSClass) {
			RDFSClass cls = (RDFSClass) o;
			ModelAdapter listener = new ModelAdapter() {
				OWLDatatypeProperty parameterType = m_okb
						.getOWLDatatypeProperty("process:parameterType");

				public void classDeleted(RDFSClass cls) {
					((RDFResource) getInstance()).removePropertyValue(
							parameterType, cls.getURI());
				}
			};
			m_okb.addModelListener(listener);
			listeners.put(cls, listener);
		}
		super.addItem(o);
	}

	private Action getCreateClassAction() {
		_createAction = new CreateAction("Add OWL Class",
				OWLIcons.getAddIcon(OWLIcons.PRIMITIVE_OWL_CLASS)) {
			public void onCreate() {
				handleCreateClassAction();
			}
		};
		return _createAction;
	}

	private Action getCreateXSDAction() {
		_createAction = new CreateAction("Add XSD Datatype",
				OWLIcons.getAddIcon(OWLIcons.RDF_DATATYPE)) {
			public void onCreate() {
				handleCreateXSDAction();
			}
		};
		return _createAction;
	}

	private Action getEditAction() {
		return new ViewAction(ResourceKey.VALUE_VIEW, this) {
			public void onView(Object o) {
				handleViewAction((Cls) o);
			}
		};
	}

	private Action getRemoveAction() {
		_removeAction = new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
			public void onRemove(Collection strings) {
				handleRemoveAction(strings);
			}
		};
		return _removeAction;
	}

	protected void handleCreateClassAction() {
		Collection c = CollectionUtilities.createCollection(m_okb
				.getOWLThingClass());
		OWLNamedClass cls = (OWLNamedClass) DisplayUtilities.pickCls(
				(Component) ParamTypeWidget.this, m_okb, c, "Select Cls");
		if (cls != null && itemInList(cls) == -1) {
			System.out.println("Cls picked: " + cls);
			addItem(cls);
		}
	}

	protected void handleCreateXSDAction() {
		// RDFSNamedClass datatype =
		// m_okb.getRDFSNamedClass(RDFSNames.Cls.DATATYPE);
		// Collection datatypes = sortDatatypes(datatype.getInstances(true));
		RDFSDatatype type = (RDFSDatatype) DisplayUtilities
				.pickInstanceFromCollection(this, getSortedDatatypes(), 0,
						"Pick a datatype");
		if (type != null && itemInList(type) == -1) {
			addItem(type);
		}
	}

	private Collection getSortedDatatypes() {
		Vector values = new Vector();
		values.add(m_okb.getXSDboolean());
		values.add(m_okb.getXSDfloat());
		values.add(m_okb.getXSDint());
		values.add(m_okb.getXSDstring());
		java.util.List ds = new ArrayList(m_okb.getRDFSDatatypes());
		ds.removeAll(values);
		Collections.sort(ds, new FrameComparator());
		// values.add(" ");
		values.addAll(ds);
		return values;
	}

	protected void handleViewAction(Cls cls) {
		System.out.println("handleViewAction");
	}

	protected void handleRemoveAction(Collection strings) {
		Iterator iter = strings.iterator();
		while (iter.hasNext()) {
			RDFResource item = (RDFResource) iter.next();
			int indx = itemInList(item);
			if (indx != -1) {
				SimpleListModel slmodel = (SimpleListModel) _list.getModel();
				slmodel.removeValue(item);
				listeners.remove(item);
				_list.setSelectedIndex(Math.min(indx, _list.getModel()
						.getSize() - 1));
			}
		}
		// removeItems (strings);
	}

	// return the current value displayed by the widget
	public Collection getValues() {
		if (_list == null)
			return Collections.EMPTY_LIST;

		Collection coll = new ArrayList();

		for (int i = 0; i < _list.getModel().getSize(); i++) {
			RDFResource item = (RDFResource) _list.getModel().getElementAt(i);
			DefaultRDFSLiteral literal = (DefaultRDFSLiteral) m_okb
					.createRDFSLiteral(item.getURI(), m_okb
							.getRDFSDatatypeByURI(XSDDatatype.XSDanyURI
									.getURI()));
			// System.out.println("ParamTypeWidget.getValues(): Type of literal is "
			// + m_okb.getRDFSDatatypeOfValue(literal));
			// System.out.println("getValues() returning " +
			// literal.getRawValue());
			coll.add(literal);
		}
		return coll;
	}

	// initialize the display value
	public void setValues(Collection c) {
		removeAllItems();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			RDFSLiteral literal;
			Object o = iter.next();
			System.out.println(o.getClass());
			if(o instanceof String) {
				literal = new DefaultRDFSLiteral(m_okb, (String)o);
			}
			else {
				literal = (RDFSLiteral)o;
			}
			// System.out.println("parameterType.setValues() got a " +
			// o.getClass() + " with value " + o);
			// if (o instanceof RDFSLiteral)
			// System.out.println("raw value: " +
			// ((DefaultRDFSLiteral)o).getRawValue());

			// System.out.println("ParamTypeWidget.setValues(): Type of literal is "
			// + (Object)m_okb.getRDFSDatatypeOfValue(o));
			String uri = literal.getString(); // ?
			String resourceName = m_okb.getResourceNameForURI(uri);
			RDFResource resource = m_okb.getRDFResource(resourceName);
			addItem(resource);
		}
	}

	/* TODO: Should be red if there is no value. */
	protected void updateBorder(Collection values) {
		setNormalBorder();
		repaint();
	}

	private int itemInList(Instance item) {
		int indx = -1;

		for (int i = 0; i < _list.getModel().getSize(); i++) {
			Instance listitem = (Instance) _list.getModel().getElementAt(i);
			if (listitem.equals(item)) {
				indx = i;
				break;
			}
		}

		return indx;
	}

	// change whether or not the user can modify the displayed value
	/*
	 * public void setEditable (boolean editable) {
	 * 
	 * }
	 */

	// indicate whether an instance of this class can handle the Class-Slot
	// binding.
	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		// boolean isSuitable;
		// if (cls == null || slot == null) {
		// isSuitable = false;
		// }
		// else {
		// boolean isString = equals (cls.getTemplateSlotValueType (slot),
		// ValueType.STRING);
		// boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues (slot);
		// isSuitable = isString && isMultiple;
		// }
		// System.out.println("ParamTypeWidget.isSuitable[" + cls + "," + slot +
		// "," + facet + "]: " + isSuitable);
		//
		// return isSuitable;
		return true;
	}

	// method to allow easy debuging
	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}
}
