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
package com.sri.owlseditor.cmp.tree;

import java.awt.datatransfer.DataFlavor;

import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;

/**
 * Singleton class which provides the dataflavor for our tree nodes.
 * Used by TreeDragSourceGestureListener in IJTree.
 * 
 * @author Daniel Elenius
 *
 */
public class OWLSDataFlavorProvider implements CleanerListener{
	private static OWLSDataFlavorProvider instance;
	private DataFlavor OWLSDataFlavor;
	
	private OWLSDataFlavorProvider(){
		//System.out.println("Creating DataFlavorProvider");
		/* This is needed for the drag and drop stuff */
		//String flavor = DataFlavor.javaJVMLocalObjectMimeType + 
		//"; class=org.pvv.bcd.instrument.JTree.DefaultNodeInfo";
		try{
			//OWLSDataFlavor = new DataFlavor(flavor, "DefaultNodeInfo");
			OWLSDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		}
		catch (java.lang.Exception e){
			e.printStackTrace();
		}
		Cleaner.getInstance().registerCleanerListener(this);
	}

	public void cleanup(){
		instance = null;
	}
	
	public static OWLSDataFlavorProvider getInstance(){
		if (instance == null){
			instance = new OWLSDataFlavorProvider();
		}
		return instance;
	}
	
	public DataFlavor getOWLSDataFlavor(){
		return OWLSDataFlavor; 
	}
}
