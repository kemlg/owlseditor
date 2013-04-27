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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.ListModel;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;

/**
 * A JPanel with an array of instance panes, of which only one instance can be chosen.
 * Common superclass for IOPRSelector and ServiceSelector. A ResourceDisplay is associated
 * with the MultipleInstanceSelector, and it will show the selected instance.
 * 
 *  * @author Daniel Elenius
 */
public class MultipleInstanceSelector extends JPanel {
    private Project                _project;
    private OWLModel       			_okb;
    private OWLSResourceDisplay     _editor;

    protected ArrayList instancePanes;   // the GUI components
    private ArrayList classes; 		   // the OWL classes in the instance lists
    
    private RDFIndividual          selected;
    
    public MultipleInstanceSelector(OWLModel okb, Project project, OWLSResourceDisplay editor) {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	
		_okb = okb;
		_project = project;
		_editor = editor;
    }

    public void setClasses(Collection classes){
    		this.classes = new ArrayList(classes);
		instancePanes = new ArrayList();
		
		createInstancePanes();

		// Select the first item in the first list
		selectFirst();
    }
    
    /** Untested! */
    public void setSelectedInstance(OWLIndividual instance){
    	Iterator it = instancePanes.iterator();
    	while (it.hasNext()){
    		OWLSInstanceList list = (OWLSInstanceList)it.next();
    		Collection clses = list.getClses();
    		Iterator it2 = clses.iterator();
    		while (it2.hasNext()){
    			OWLNamedClass cls = (OWLNamedClass)it2.next();
    			if (instance.hasRDFType(cls)){
    				list.setSelectedInstance(instance);
    				return;
    			}
    		}
    	}
    }
    
    /* Creates one instance pane for each class using the factory, and adds them to this
     * JPanel. */
    private void createInstancePanes(){
    	Iterator it = classes.iterator();
    	while (it.hasNext()){
    		OWLNamedClass cls = (OWLNamedClass)it.next();
    		OWLSInstanceList list = OWLSInstanceListFactory.create(_project, cls);
    		instancePanes.add(list);
            
    		class OWLSInstanceListListener implements SelectionListener{
    			private OWLSInstanceList list;
    			
    			public OWLSInstanceListListener(OWLSInstanceList list){
    				this.list = list;
    			}

        		public void selectionChanged(SelectionEvent event) {
        			RDFIndividual inst = list.getSelectedInstance();
        			updateInstanceLists(inst);
        		}

    		}
    		
    		list.addSelectionListener(new OWLSInstanceListListener(list));
    		add(list);
    	}
    }
    
    private void selectFirst(){
    	Iterator it = instancePanes.iterator();
    	boolean selectionMade = false;
    	
    	while (it.hasNext() && !selectionMade){
    		OWLSDirectInstancesList list = (OWLSDirectInstancesList)it.next();
    		SelectableList selectable = (SelectableList)list.getSelectable();
    		ListModel listModel = selectable.getModel();

    		if (listModel.getSize() > 0){
    			RDFIndividual inst = (RDFIndividual)listModel.getElementAt(0);
        		selectable.setSelectedIndex(0);
        		updateInstanceLists(inst);
        		selectionMade = true;
    		}
    	}
    	
    	if (!selectionMade)
    		updateInstanceLists(null);
    }

    protected void updateInstanceLists(RDFIndividual instance){
    	if (instance != null){
        	Iterator it = instancePanes.iterator();
        	selected = instance;
        	while (it.hasNext()){
        		OWLSInstanceList list = (OWLSInstanceList)it.next();
        		list.update(instance);
        		list.repaint();
        	}
        	_editor.setInstance(instance);
    	}
    	else
    		_editor.clearSelection();
    }

    /** Returns the current selection from the four panes */
    public RDFIndividual getSelectedInstance(){
    	return selected;
    }

}

