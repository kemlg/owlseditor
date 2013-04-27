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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.sri.owlseditor.OWLSFrameRenderer;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

public class OWLSIcons{

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = OWLSFrameRenderer.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    /* Mini-icons for the process instance view */
    public static Icon getAtomicProcessIcon(){
	return createImageIcon("images/Atomic.png");
    }

    public static Icon getCompositeProcessIcon(){
	return createImageIcon("images/Composite.png");
    }

    public static Icon getSimpleProcessIcon(){
	return createImageIcon("images/Simple.png");
    }

    /* Icons for the main toolbar items */
    public static Icon getSearchIcon(){
	return createImageIcon("images/Search.png");
    }
    
    public static Icon getExecuteIcon(){
    	return createImageIcon("images/Execute.png");
    }
    
    public static Icon getIOPRManagerIcon(){
	return createImageIcon("images/IOPR-2.png");
    }

    public static Icon getGenerateFromWSDLIcon(){
	return createImageIcon("images/WSDL.png");
    }

    public static Icon getGenerateFromBPELIcon(){
	return createImageIcon("images/BPEL.png");
    }

    public static Icon getConsistencyIcon(){
    	return OWLIcons.getCheckConsistencyIcon();
    }
    
    public static Icon getOptionsIcon(){
	//return createImageIcon("images/Options.png");
	return OWLIcons.getPreferencesIcon();
    }

    public static Icon getConsistencyCheckIcon() {
	return OWLIcons.getCheckConsistencyIcon();
    }

    public static Icon getCopyLeftToRightIcon() {
	return OWLIcons.getCopyIcon();
    }

    public static Icon getCopyRightToLeftIcon() {
	return OWLIcons.getCopyIcon();
    }

    public static Icon getGraphViewIcon(){
	return createImageIcon("images/GraphView.png");
    }
    
    /* Icons for the Control Construct buttons */
    
    /*
    public static Icon getCreateIcon(String baseIconName) {
        return new OverlayIcon(baseIconName, 5, 4, "CreateStar", 13, 1);
    }
    */
    private static Icon getCreateIcon(String icon){
    	/*
    	return new OverlayIcon(createImageIcon(icon).getImage(), 
				   5, 
				   4, 
				   createImageIcon("images/CreateStar.gif").getImage(), 
				   13, 
				   1);
		*/
    	return createImageIcon(icon);
    }
    
    public static Icon getPerformIcon(){
    	return getCreateIcon("images/PerformIcon.png");
    }
    
    public static Icon getSequenceIcon(){
    	return getCreateIcon("images/SequenceIcon.png");
    
    }
    public static Icon getSplitIcon(){
       	return getCreateIcon("images/SplitIcon.png");
    }
    
    public static Icon getSplitJoinIcon(){
       	return getCreateIcon("images/SplitJoinIcon.png");
    }
    
    public static Icon getChoiceIcon(){
       	return getCreateIcon("images/ChoiceIcon.png");
    }
    
    public static Icon getAnyOrderIcon(){
       	return getCreateIcon("images/AnyOrderIcon.png");
    }
    
    public static Icon getIfThenElseIcon(){
       	return getCreateIcon("images/IfIcon.png");
    }
    
    public static Icon getRepeatUntilIcon(){
       	return getCreateIcon("images/RepeatUntilIcon.png");
    }
    
    public static Icon getRepeatWhileIcon(){
       	return getCreateIcon("images/RepeatWhileIcon.png");
    }

    public static Icon getProduceIcon(){
       	return getCreateIcon("images/ProduceIcon.png");
    }

    public static Icon getDeleteControlConstructIcon(){
    	return OWLIcons.getDeleteIcon();
    }
    
    /* Icons for the composite process graph toolbar */
    public static Icon getZoomInIcon(){
    	return createImageIcon("images/zoomin.png");
    }

    public static Icon getZoomOutIcon(){
    	return createImageIcon("images/zoomout.png");
    }

    // The "zoom fast" ones are not implemented for now
    public static Icon getZoomInFastIcon(){
    	//return createImageIcon("images/zoominfast.png");
    	return getSearchIcon();
    }

    public static Icon getZoomOutFastIcon(){
    	//return createImageIcon("images/zoomoutfast.png");
    	return getSearchIcon();
    }

    public static Icon getResetZoomIcon(){
    	return createImageIcon("images/zoomnormal.png");
   }

    public static Icon getPrintIcon(){
    	return createImageIcon("images/print.png");
    }

    public static Icon getSVGExportIcon(){
    	return Icons.getSaveProjectIcon();
    }
    
    public static Icon getXMLRootNodeIcon(){
    	return createImageIcon("images/R.png");
    }
    public static Icon getXMLAttributeIcon(){
    	return createImageIcon("images/A.png");
    }    
    public static Icon getXSLTVariableIcon(){
    	return createImageIcon("images/V.png");
    }
}
