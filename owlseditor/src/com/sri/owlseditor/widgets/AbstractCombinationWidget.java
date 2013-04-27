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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.widget.AbstractPropertyWidget;

/**
 * This is a superclass of HasDataFromWidget and WsdlMessageMapWidget, which behave
 * very similarly.
 * 
 * @author Daniel Elenius
 */
public abstract class AbstractCombinationWidget extends AbstractPropertyWidget {
    // GUI components
    private LabeledComponent lc;
    private SelectableList list = new SelectableList();
    private JComboBox selectionCombo;
    private JPanel selectionComboContainer;
    private JPanel rightSidePanel;
    private JPanel editorContainer;  // The contents of this changes depending on selectionCombo selection
    private JButton addButton;
    private JButton removeButton;
	
    // Actions
    private AbstractAction addAction;
    private AllowableAction removeAction;
    private Action viewAction;

    // Internal data
    private HashMap clsesMap;
    private DefaultListModel listModel;
    private ItemListener selectionComboListener;
	
    /**
     * Note: This method is responsible for updating the combo box! 
     * This only gets called when an item is selected, not when items are unselected.
     * When that happens, i.e. when no list item is selected, the editor field gets
     * emptied.
     */
    public abstract void listSelectionChanged(Object selectedItem); 	
	
    /**
     * The component of the editorContainer will already be changed when
     * this is called.
     * 
     * This is only called when the USER changes the combo box selection.
     * 
     * @param selectedItem
     */
    public abstract void comboSelectionChanged(Object selectedItem);
	
    /**  
     * @param parent The parent component is passed so that implementors can pop up a
     * pick dialog.
     * @return The object that should be added to the list. Null if no object. This item
     * will become selected if it is non-null.
     */
    public abstract Object addListItem(Component parent);

    /** Return true if the item really should be deleted */
    public abstract boolean removeListItem(Object listItem);
	
    /**
     * 
     * @param listLabel
     * @param addToolTip
     * @param removeToolTip
     * @param comboLabel
     * @param clsesMap Keys should be the strings (or other Objects) to appear in the combo box, 
     * and values should be the JPanels associated with those labels.
     */
    public void initialize(String listLabel, 
			   String addToolTip, 
			   String removeToolTip, 
			   String comboLabel,
			   HashMap clsesMap) {
		
	this.clsesMap = clsesMap;
	listModel = new DefaultListModel();
	list.setModel(listModel);
	setPreferredColumns(5);
	setPreferredRows(4);
	
	setBorder(BorderFactory.createEmptyBorder(0,7,0,0));
	viewAction = createViewAction();
	list = ComponentFactory.createSelectableList(viewAction);
	list.setCellRenderer(new FrameRenderer());

	lc = createLabeledComponent(listLabel, addToolTip, removeToolTip);
		
	selectionCombo = createSelectionComboBox(comboLabel, clsesMap.keySet());
	selectionComboListener = new ItemListener() {
		public void itemStateChanged (ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.SELECTED) {
			JPanel comp = (JPanel)getClsesMap().get (getComboSelection());
			setEditorComponent (comp);
			comboSelectionChanged (getComboSelection());
		    }
		}
	    };
	enableSelectionComboListener();
		
	rightSidePanel = ComponentFactory.createPanel();
	rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.Y_AXIS));
	rightSidePanel.setBorder(BorderFactory.createCompoundBorder(
								    BorderFactory.createEmptyBorder(0, 7, 0, 0),
								    BorderFactory.createEtchedBorder()));
	rightSidePanel.add(selectionComboContainer);
	editorContainer = ComponentFactory.createPanel();
	editorContainer.setLayout(new BorderLayout());
	rightSidePanel.add(editorContainer);
		
	setLayout(new GridBagLayout());
	GridBagConstraints lc_gbc = new GridBagConstraints();
	lc_gbc.gridx = 0;
	lc_gbc.fill = GridBagConstraints.BOTH;
	add(lc, lc_gbc);
	GridBagConstraints rsp_gbc = new GridBagConstraints();
	rsp_gbc.gridx = GridBagConstraints.RELATIVE;  // just to the right of the previous component
	rsp_gbc.fill = GridBagConstraints.BOTH;
	rsp_gbc.weightx = 1.0;
	rsp_gbc.weighty = 1.0;
	add(rightSidePanel, rsp_gbc);
    }

    protected void enableSelectionComboListener(){
	selectionCombo.addItemListener(selectionComboListener);
    }
	
    protected void disableSelectionComboListener(){
	selectionCombo.removeItemListener(selectionComboListener);
    }
	
    private HashMap getClsesMap(){
	return clsesMap;
    }
	
    /** Changes the contents of the bottom-right fields of the widget */
    public void setEditorComponent(JPanel component){
	//System.out.println("setEditorComponent(): Changing component to " + component);
	editorContainer.removeAll();
	if (component != null){
	    component.setVisible (false);
	    editorContainer.add(component, BorderLayout.CENTER);
	    component.setVisible(true);
	}
	repaint();
    }

    /* Should be fixed to only return true in the correct cases */
    public static boolean isSuitable(Cls cls, Slot slot, Facet facet){
	return true;
    }

    public Object getSelectedListValue(){
	Collection c = list.getSelection();
	Iterator it = c.iterator();
	if (it.hasNext()) 
	    return it.next();
	return null;
    }
	
    public void setSelectedListIndex(int index){
	list.setSelectedIndex(index);
    }

    public void setSelectedListValue(Object value){
	list.setSelectedValue(value);
    }

    public void setListValues(Set values){
	ComponentUtilities.setListValues(list, values);
    }

    public Collection getListValues(){
	return ComponentUtilities.getListValues(list);
    }
	
    public String getComboSelection(){
	return (String)selectionCombo.getSelectedItem();
    }
	
    public void setComboSelection(Object value){
	selectionCombo.setSelectedItem(value);
    }

    public Dimension getEditorPanelSize()
    {
	return editorContainer.getPreferredSize();
    }
	
    private JComboBox createSelectionComboBox(String comboLabel, Set labels){
	JComboBox combo = ComponentFactory.createComboBox(); 
	Iterator it = labels.iterator();
	while (it.hasNext()){
	    String label = (String)it.next();
	    combo.addItem(label);
	}
	FlowLayout selectionComboLayout = new FlowLayout(FlowLayout.LEFT);
	//bindingTypeFlowLayout.setVgap(2);
	selectionComboContainer = ComponentFactory.createPanel();
	selectionComboContainer.setLayout(selectionComboLayout);
	selectionComboContainer.add(ComponentFactory.createLabel(comboLabel));
	selectionComboContainer.add(combo);
	return combo;
    }
	
    private Action createViewAction() {
        return new ViewAction("View Instance", this) {
		public void onView(Object o) {
		    getProject().show((Instance) o);
		}
	    };
    }	
	
    private LabeledComponent createLabeledComponent(String listLabel, 
						    String addToolTip, 
						    String removeToolTip){
	LabeledComponent lc = ComponentFactory.createLabeledScrollComponent(
									    listLabel,
									    list,
									    new Dimension(ComponentUtilities.getStandardColumnWidth()*2,
											  ComponentUtilities.getStandardRowHeight()*3),
									    null,
									    null,
									    null);
		
	class AddAction extends AbstractAction{
	    private Component parent;

	    public AddAction(Component parent){
		this.parent = parent;
	    }
			
	    public void actionPerformed(ActionEvent e){
		Object value = addListItem(parent);
		if (value != null){
			ComponentUtilities.addListValue (list, value);
			setSelectedListValue(value);
		}
	    }
	}
	addAction = new AddAction(this);
	addAction.putValue(Action.SMALL_ICON, OWLIcons.getAddIcon(OWLIcons.RDF_INDIVIDUAL));
	addButton = lc.addHeaderButton(addAction);
	addButton.setToolTipText(addToolTip);
		
	class RemoveAction extends AllowableAction{
	    private Component parent;
			
	    public RemoveAction(Component parent, String name, Selectable selectable){
		super(name, selectable);
		this.parent = parent;
	    }
			
	    public void actionPerformed(ActionEvent e){
		Collection c = getSelection();
				
		Iterator it = c.iterator();
		if (it.hasNext()){
		    Object listItem = it.next();
		    if (removeListItem(listItem))
			ComponentUtilities.removeListValue(list, listItem);						
		}
	    }
	}
	removeAction = new RemoveAction(this, removeToolTip, list);
	removeAction.putValue(Action.SMALL_ICON, OWLIcons.getRemoveIcon(OWLIcons.RDF_INDIVIDUAL));
	removeButton = lc.addHeaderButton(removeAction);
			
	class ListSelectionListener implements SelectionListener{
	    public void selectionChanged(SelectionEvent e){
		Object selectedValue = getSelectedListValue(); 
				
		disableSelectionComboListener();
				
		if (selectedValue != null)
		    listSelectionChanged(selectedValue);
		else
		    editorContainer.removeAll();
				
		enableSelectionComboListener();
	    }
	}
	list.addSelectionListener(new ListSelectionListener());

	return lc;
    }

}
