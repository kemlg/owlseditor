/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Protege-2000.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2001.  All Rights Reserved.
 *
 * Protege-2000 was developed by Stanford Medical Informatics
 * (http://www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the National Library of Medicine, the National Science
 * Foundation, and the Defense Advanced Research Projects Agency.  Current
 * information about Protege can be obtained at http://protege.stanford.edu
 *
 * Contributor(s):
 */

package com.sri.owlseditor.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.ListModel;

import com.sri.owlseditor.iopr.ListAndCheckboxPanel;
import com.sri.owlseditor.widgets.swrl.SWRLWidget;

import edu.stanford.smi.protege.action.DeleteInstancesAction;
import edu.stanford.smi.protege.action.MakeCopiesAction;
import edu.stanford.smi.protege.action.ReferencersAction;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Colors;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.ui.ListFinder;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtomList;

/**
 * This is a customized version of edu.stanford.smi.protege.ui.DirectInstancesList
 * We need to change some internals, so pure subclassing is not enough. Changes to this file
 * are kept to a minimum, and the subclass OWLSInstanceList is
 * used when possible. That way, we can diff this file with the original DirectInstancesList.java
 * and integrate future changes to it easily.
 * 
 * 1) Commented out stuff that didn't compile (see comments below)
 * 2) Changed class labels to only list main class, not all subclasses (updateLabel() method).
 * 3) Added implements CommonOWLSInstancesList
 * 4) Removed the toppanel
 * 5) Added a getClses() method, as it is needed by some subclasses.
 * 6) Support for checkbox panel for the IOPR manager.
 * 
 * ---
 * The panel that holds the list of direct instances of one or more classes. If
 * only one class is chosen then you can also create new instances of this
 * class.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 * @author Daniel Elenius <elenius@csl.sri.com>
 */
public class OWLSDirectInstancesList extends SelectableContainer implements
        Disposable {
    private Collection _clses = Collections.EMPTY_LIST;
    private SelectableList _list;
    private Project _project;
    private AllowableAction _createAction;
    private AllowableAction _copyAction;
    private AllowableAction _deleteAction;
    private HeaderComponent _header;
    private Collection listenedToInstances = new ArrayList();
    private static final int SORT_LIMIT;
    private boolean _showSubclassInstances;
    private LabeledComponent _labeledComponent;

    // Added for checkbox support
    private ListAndCheckboxPanel checkboxPanel;
    private boolean checkboxpanel;
    
    static {
        SORT_LIMIT = ApplicationProperties.getIntegerProperty(
                "ui.DirectInstancesList.sort_limit", 1000);
    }

    private ClsListener _clsListener = new ClsAdapter() {
        public void directInstanceAdded(ClsEvent event) {
            Instance instance = event.getInstance();
            ComponentUtilities.addListValue(_list, instance);
	    if (checkboxpanel){
	    	checkboxPanel.makeCheckBoxLine ((RDFIndividual) instance);
		    _list.setSelectedValue (instance, true);
	    }
	    instance.addFrameListener(_instanceFrameListener);
        }

        public void directInstanceRemoved(ClsEvent event) {
            Instance instance = event.getInstance();
            removeInstance (instance);
	    if (checkboxpanel)
		checkboxPanel.deleteCheckBoxLine ((RDFIndividual) instance);
        }
    };

    private FrameListener _clsFrameListener = new FrameAdapter() {
        public void ownSlotValueChanged(FrameEvent event) {
            super.ownSlotValueChanged(event);
            updateButtons();
        }
    };

    private FrameListener _instanceFrameListener = new FrameAdapter() {
        public void browserTextChanged(FrameEvent event) {
            super.browserTextChanged(event);
            // Log.enter(this, "browserTextChanged", event);
            sort();
            repaint();
        }
    };

    /** Create the instance list without the checkboxes */
    public OWLSDirectInstancesList(Project project) {
    	this(project, false);
    }


    /**
     * 
     * @param project
     * @param checkboxes true if checkboxes are desired, false otherwise.
     */
    public OWLSDirectInstancesList(Project project, boolean checkboxes) {
    	checkboxpanel = checkboxes;
    	
    	_project = project;
        Action viewAction = createViewAction();

        _list = ComponentFactory.createSelectableList(viewAction);
        _list.setCellRenderer(FrameRenderer.createInstance());

        if (checkboxes){
	        // Added stuff for checkbox panel
	        checkboxPanel = new ListAndCheckboxPanel(_list, (OWLModel)_project.getKnowledgeBase());
	        _labeledComponent = new LabeledComponent(null, ComponentFactory.createScrollPane(checkboxPanel));
        }
        else{
            // setLayout(new BorderLayout(10, 10));
            _labeledComponent = new LabeledComponent(null, ComponentFactory.createScrollPane(_list));
        }

        addButtons(viewAction, _labeledComponent);
        _labeledComponent.setFooterComponent(new ListFinder(_list, ResourceKey.INSTANCE_SEARCH_FOR));
        _labeledComponent.setBorder(ComponentUtilities.getAlignBorder());
        add(_labeledComponent, BorderLayout.CENTER);
        
        JPanel toppanel = new JPanel(new BorderLayout());
        toppanel.add(createHeader(), BorderLayout.NORTH);
        // panel.add(_setDisplaySlotPanel, BorderLayout.CENTER);
        //add(toppanel, BorderLayout.NORTH);    // we don't need this

        setSelectable(_list);
    }

 
    public ListAndCheckboxPanel getCheckboxPanel(){
    	return checkboxPanel;
    }
    
    private void updateLabel() {
    	/*
    	String text;
        Cls cls = getSoleAllowedCls();
        BrowserSlotPattern pattern = (cls == null) ? null : cls.getBrowserSlotPattern();
        if (pattern == null) {
            text = null;
        } else {
	        // text = "Instances by ";
	        if (pattern.isSimple()) {
	            text = pattern.getFirstSlot().getBrowserText();
	        } else {
	            text = "multiple slots";
	        }
        }
        _labeledComponent.setHeaderLabel(text);
        */
        Cls firstcls = (Cls)_clses.iterator().next();
        _labeledComponent.setHeaderLabel(firstcls.getName());
    }

    private HeaderComponent createHeader() {
        JLabel label = ComponentFactory.createLabel();
        String instanceBrowserLabel = LocalizedText.getText(ResourceKey.INSTANCE_BROWSER_TITLE);
        String forClassLabel = LocalizedText.getText(ResourceKey.CLASS_EDITOR_FOR_CLASS_LABEL);
        _header = new HeaderComponent(instanceBrowserLabel, forClassLabel, label);
        _header.setColor(Colors.getInstanceColor());
        return _header;
    }

    private void fixRenderer() {
        FrameRenderer frameRenderer = (FrameRenderer) _list.getCellRenderer();
        frameRenderer.setDisplayType(_showSubclassInstances);
    }

    protected void addButtons(Action viewAction, LabeledComponent c) {
        c.addHeaderButton(viewAction);
        c.addHeaderButton(createReferencersAction());
        c.addHeaderButton(createCreateAction());
        c.addHeaderButton(createCopyAction());
        c.addHeaderButton(createDeleteAction());
        //c.addHeaderButton(createConfigureAction());  
    }

    private void addClsListeners() {
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.addClsListener(_clsListener);
            cls.addFrameListener(_clsFrameListener);
        }
    }

    private void addInstanceListeners() {
        ListModel model = _list.getModel();
        int start = _list.getFirstVisibleIndex();
        int stop = _list.getLastVisibleIndex();
        for (int i = start; i < stop; ++i) {
            Instance instance = (Instance) model.getElementAt(i);
            addInstanceListener(instance);

        }
    }

    private void removeInstanceListeners() {
        Iterator i = listenedToInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            instance.removeFrameListener(_instanceFrameListener);
        }
        listenedToInstances.clear();
    }

    private void addInstanceListener(Instance instance) {
        instance.addFrameListener(_instanceFrameListener);
        listenedToInstances.add(instance);
    }
    
    protected Action createCreateAction() {
        _createAction = new CreateAction(ResourceKey.INSTANCE_CREATE) {
            public void onCreate() {
            	Cls cls = null;
                KnowledgeBase kb = _project.getKnowledgeBase();
            	// Added support for choosing subclass
                if (_clses.size()>1){
            		ArrayList rootClses = new ArrayList();
            		cls = (Cls) CollectionUtilities.getFirstItem(_clses);
            		rootClses.add(cls);
            		cls = DisplayUtilities.pickCls(_labeledComponent, 
            				 					   kb,
												   rootClses,
												   "Choose a class");
            	}
            	else{
            		cls = (Cls) CollectionUtilities.getFirstItem(_clses);
            	}

                if (cls != null) {
                    Instance instance = kb.createInstance(null, cls);

                    // Added for checkbox support
                    //if (checkboxpanel)
		    //checkboxPanel.makeCheckBoxLine((RDFIndividual)instance);

                    if (instance instanceof Cls) {
                        Cls newCls = (Cls) instance;
                        if (newCls.getDirectSuperclassCount() == 0) {
                            newCls.addDirectSuperclass(kb.getRootCls());
                        }
                    }
                    _list.setSelectedValue(instance, true);
                }
            }
        };
        return _createAction;
    }

    /*
    protected Action createConfigureAction() {
        return new ConfigureAction() {
            public void loadPopupMenu(JPopupMenu menu) {
                menu.add(createSetDisplaySlotAction());
                menu.add(createShowAllInstancesAction());
            }
        };
    }
    */
    
    protected JMenuItem createShowAllInstancesAction() {
        Action action = new AbstractAction("Show Subclass Instances") {
            public void actionPerformed(ActionEvent event) {
                _showSubclassInstances = !_showSubclassInstances;
                reload();
                fixRenderer();
            }
        };
        JMenuItem item = new JCheckBoxMenuItem(action);
        item.setSelected(_showSubclassInstances);
        return item;
    }
    
    protected Cls getSoleAllowedCls() {
        Cls cls;
        if (_clses.size() == 1) {
            cls = (Cls) CollectionUtilities.getFirstItem(_clses);
        } else {
            cls = null;
        }
        return cls;
    }

    /* We don't need this method, and it relies on createSetDisplaySlotMultipleAction(), which
     * has problems.
    protected JMenu createSetDisplaySlotAction() {
        JMenu menu = ComponentFactory.createMenu("Set Display Slot");
        boolean enabled = false;
        Cls cls = getSoleAllowedCls();
        if (cls != null) {
            BrowserSlotPattern pattern = cls.getBrowserSlotPattern();
            Slot browserSlot = (pattern != null && pattern.isSimple()) ? pattern.getFirstSlot() : null;
	        Iterator i = cls.getVisibleTemplateSlots().iterator();
	        while (i.hasNext()) {
	            Slot slot = (Slot) i.next();
	            JRadioButtonMenuItem item = new JRadioButtonMenuItem(createSetDisplaySlotAction(slot));
	            if (slot.equals(browserSlot)) {
	                item.setSelected(true);
	            }
	            menu.add(item);
	            enabled = true;
	        }
	        JRadioButtonMenuItem item = new JRadioButtonMenuItem(createSetDisplaySlotMultipleAction());
	        if (browserSlot == null) {
	            item.setSelected(true);
	        }
	        menu.add(item);
        }
        menu.setEnabled(enabled);
        return menu;
    }
    */
    
    protected Action createSetDisplaySlotAction(final Slot slot) {
        return new AbstractAction(slot.getBrowserText(), slot.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                getSoleAllowedCls().setDirectBrowserSlot(slot);
                updateLabel();
                repaint();
            }
        };
    }
    
    /* MultiSlotPanel doesn't resolve, and we don't need this anyway
    protected Action createSetDisplaySlotMultipleAction() {
        return new AbstractAction("Multiple Slots...") {
            public void actionPerformed(ActionEvent event) {
                Cls cls = getSoleAllowedCls();
                BrowserSlotPattern currentPattern = getSoleAllowedCls().getBrowserSlotPattern();
                MultiSlotPanel panel = new MultiSlotPanel(currentPattern, cls);
                int rval = ModalDialog.showDialog(ModifiedDirectInstancesList.this, panel, "Multislot Display Pattern", ModalDialog.MODE_OK_CANCEL);
                if (rval == ModalDialog.OPTION_OK) {
                    BrowserSlotPattern pattern = panel.getBrowserTextPattern();
                    if (pattern != null) {
                        cls.setDirectBrowserSlotPattern(pattern);
                     }
                }
                updateLabel();
                repaint();
            }
        };
    }
    */
    
    protected Action createDeleteAction() {
        _deleteAction = new DeleteInstancesAction(this){
        	// Added to support checkbox panel
        	protected void onAboutToDelete(Object o){
        		//if (checkboxpanel)
        		//  checkboxPanel.deleteCheckBoxLine((RDFIndividual)o);

        		/* This makes sure that contents of Expressions are deleted when 
        		 * the expressions themselves are deleted. */
				if (o instanceof OWLIndividual){
        			OWLIndividual inst = (OWLIndividual)o;
        			OWLModel model = inst.getOWLModel();
        			OWLNamedClass expressionCls = model.getOWLNamedClass("expr:Expression");
        			if (inst.hasRDFType(expressionCls, true)){
        				OWLObjectProperty expressionObject = model.getOWLObjectProperty("expr:expressionObject");
        				RDFResource expr = (RDFResource)inst.getPropertyValue(expressionObject);
        				if (expr != null && expr instanceof SWRLAtomList)
    						SWRLWidget.deleteAtomList((SWRLAtomList)expr, model);
        			}
        		}
        	}
        };
        return _deleteAction;
    }

    protected Action createCopyAction() {
        _copyAction = new MakeCopiesAction(ResourceKey.INSTANCE_COPY, this) {
            protected Instance copy(Instance instance, boolean isDeep) {
                Instance copy = super.copy(instance, isDeep);
		//if (checkboxpanel)
		//checkboxPanel.makeCheckBoxLine ((RDFIndividual) copy);
                setSelectedInstance (copy);
                return copy;
            }
        };
        return _copyAction;
    }

    protected Action createReferencersAction() {
        return new ReferencersAction(ResourceKey.INSTANCE_VIEW_REFERENCES, this);
    }

    protected Action createViewAction() {
        return new ViewAction(ResourceKey.INSTANCE_VIEW, this) {
            public void onView(Object o) {
                _project.show((Instance) o);
            }
        };
    }

    public void dispose() {
        removeClsListeners();
        removeInstanceListeners();
    }

    public JComponent getDragComponent() {
        return _list;
    }

    private SimpleListModel getModel() {
        return (SimpleListModel) _list.getModel();
    }

    private boolean isSelectionEditable() {
        boolean isEditable = true;
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isEditable()) {
                isEditable = false;
                break;
            }
        }
        return isEditable;
    }

    public void onSelectionChange() {
        // Log.enter(this, "onSelectionChange");
        boolean editable = isSelectionEditable();
        ComponentUtilities.setDragAndDropEnabled(_list, editable);
        updateButtons();
    }

    private void removeInstance(Instance instance) {
        ComponentUtilities.removeListValue(_list, instance);
        instance.removeFrameListener(_instanceFrameListener);
    }

    private void removeClsListeners() {
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            cls.removeClsListener(_clsListener);
            cls.removeFrameListener(_clsFrameListener);
        }
    }

    public Collection getClses(){
    	return _clses;
    }
    
    public void setClses(Collection newClses) {
        removeClsListeners();
        _clses = new ArrayList(newClses);
        reload();
        updateButtons();
        addClsListeners();
    }

    public void reload() {
        removeInstanceListeners();
        Object selectedValue = _list.getSelectedValue();
        ArrayList instances = new ArrayList();
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            instances.addAll(getInstances(cls));
        }
        if (instances.size() <= SORT_LIMIT) {
            Collections.sort(instances, new FrameComparator());
        }
        getModel().setValues(instances);
        if (instances.contains(selectedValue)) {
            _list.setSelectedValue(selectedValue, true);
        } else if (!instances.isEmpty()) {
            _list.setSelectedIndex(0);
        }
        addInstanceListeners();
        reloadHeader(_clses);
        updateLabel();
    }

    private void reloadHeader(Collection clses) {
    	StringBuffer text = new StringBuffer();
        Icon icon = null;
        Iterator i = clses.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (icon == null) {
                icon = cls.getIcon();
            }
            if (text.length() != 0) {
                text.append(", ");
            }
            text.append(cls.getName());
        }
        JLabel label = (JLabel) _header.getComponent();
        label.setText(text.toString());
        label.setIcon(icon);
    }

    private Collection getInstances(Cls cls) {
        Collection instances;
        if (_showSubclassInstances) {
            instances = cls.getInstances();
        } else {
            instances = cls.getDirectInstances();
        }
        if (!_project.getDisplayHiddenFrames()) {
            instances = removeHiddenInstances(instances);
        }
        return instances;
    }

    private Collection removeHiddenInstances(Collection instances) {
        Collection visibleInstances = new ArrayList(instances);
        Iterator i = visibleInstances.iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isVisible()) {
                i.remove();
            }
        }
        return visibleInstances;
    }

    public void sort() {
        _list.setListenerNotificationEnabled(false);
        Object selectedValue = _list.getSelectedValue();
        List instances = new ArrayList(getModel().getValues());
        if (instances.size() <= SORT_LIMIT) {
            Collections.sort(instances, new FrameComparator());
        }
        getModel().setValues(instances);
        _list.setSelectedValue(selectedValue);
        _list.setListenerNotificationEnabled(true);
    }

    public void setSelectedInstance(Instance instance) {
        _list.setSelectedValue(instance, true);
        updateButtons();
    }

    private void updateButtons() {
        Cls cls = (Cls) CollectionUtilities.getFirstItem(_clses);
        _createAction.setEnabled(cls == null ? false : cls.isConcrete());
        Instance instance = (Instance) getSoleSelection();
        boolean allowed = instance != null && instance instanceof SimpleInstance;
        _copyAction.setAllowed(allowed);
    }

    /**
     * Does nothing anymore. This functionality moved to the menu button.
     * @deprecated
     */
    public void setShowDisplaySlotPanel(boolean b) {
        
    }
}
