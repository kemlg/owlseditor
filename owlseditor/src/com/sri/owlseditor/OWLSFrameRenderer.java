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
package com.sri.owlseditor;

import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/** A modification of FrameRenderer, which is normally used to render
    the items in the instances selectors. This supports displaying
    some of the items in bold, which is used to show which instances
    are related to each other. See the OWLSInstanceList 
    class for more details */
public class OWLSFrameRenderer extends FrameRenderer{

	protected OWLNamedClass atomicProcess;
	protected OWLNamedClass simpleProcess;
	protected OWLNamedClass compositeProcess;

    /* The InstanceList that this renderer renders for */
    private BoldableOWLSInstanceList _instlist;

    public OWLSFrameRenderer(OWLModel model, BoldableOWLSInstanceList instlist){
    	super();
    	_instlist = instlist;
    	atomicProcess = model.getOWLNamedClass("process:AtomicProcess");
    	simpleProcess = model.getOWLNamedClass("process:SimpleProcess");
    	compositeProcess = model.getOWLNamedClass("process:CompositeProcess");
    }

    /* Returns true if the instance is in the list of instances
       that should currently be painted in bold face */
    private boolean boldItem(OWLIndividual inst){
		Collection boldList = _instlist.getBoldItems();
		Iterator it = boldList.iterator();
		while (it.hasNext()){
			OWLIndividual boldinst = (OWLIndividual) it.next();
			if (inst != null)
				if (boldinst != null)
					if (inst.getName().equals(boldinst.getName()))
						return true;
		}
		return false;
    }


    /** This adds a small A, S or C next to the process name, for
	Atomic, Simple, or Composite Processes */
    private void addProcessTypeMiniIcon(OWLIndividual inst){ 
    	if (inst.hasRDFType(atomicProcess, true)){
			this.appendIcon(OWLSIcons.getAtomicProcessIcon());
    	}
    	else if (inst.hasRDFType(simpleProcess, true)){
			this.appendIcon(OWLSIcons.getSimpleProcessIcon());
    	}
    	else if (inst.hasRDFType(compositeProcess, true)){
			this.appendIcon(OWLSIcons.getCompositeProcessIcon());
    	}
    }

    /** This is where we change the font to bold if the item value is
	in the list of values that should be in bold face. This method
	is a slightly modified version of the setup method in the superclass.
	
	value -- this is an Instance (actually a DefaultSimpleOWLInstance here) */
    protected Component setup(Component c, Object value, boolean hasFocus, boolean isSelected) {

	//System.out.println("--- value is of class: " + value.getClass().getName());

        _grayedText = false;
        Font font = c.getFont();
        if (font.isBold()) {
            font = font.deriveFont(Font.PLAIN);
        }

	/* Set the font to bold or not bold depending on the current
	   selection */
        if (boldItem((OWLIndividual) value))
        	setFont(font.deriveFont(Font.BOLD));
        else
	    setFont(font);

        _hasFocus = hasFocus;
        _isSelected = isSelected;
	
        _elements.clear();
        if (value == null) {
            loadNull();
        } else if (value instanceof LazyTreeNode) {
            load(((LazyTreeNode) value).getUserObject());
        } else {
            load(value);
        }
	
        _fontMetrics = getFontMetrics(getFont());
        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        if (currentLookAndFeel != _cachedLookAndFeel) {
            loadTreeColors();
            _cachedLookAndFeel = currentLookAndFeel;
        }
        checkDropTarget(c, value);
        if (value != null) 
        	addProcessTypeMiniIcon((OWLIndividual)value);
        return this;
    }
    
    
}
