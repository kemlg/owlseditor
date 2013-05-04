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

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class registers classes that need to do some cleanup when the Project is
 * closed. It is activated from the dispose() method of the Owlstab class. Any
 * class that creates threads which manipulate the KB must register here, and
 * stop those threads in their cleanup() method. Otherwise, they will continue
 * to run when we close the project and open a new one, and we will get
 * "RuntimeException: Method called on closed knowledge base".
 * 
 * @author Daniel Elenius
 * 
 */
public class Cleaner {
	private Vector listeners = new Vector();
	private static Cleaner instance;

	private Cleaner() {
	}

	public static Cleaner getInstance() {
		if (instance == null) {
			instance = new Cleaner();
		}
		return instance;
	}

	public void registerCleanerListener(CleanerListener listener) {
		listeners.add(listener);
	}

	public void removeCleanerListener(CleanerListener listener) {
		listeners.remove(listener);
	}

	/** Activates the cleanup() method of all registered CleanerListeners */
	public void fire() {
		for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
			((CleanerListener) e.nextElement()).cleanup();
		}
		// Cleaner finishes by cleaning itself up
		instance = null;
	}

}
