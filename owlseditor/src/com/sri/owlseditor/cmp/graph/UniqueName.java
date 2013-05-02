/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License
at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See
the License for the specific language governing rights and limitations
under the License.

The Original Code is the OWL/S Visualizer.

The Initial Developer of the Original Code is DCS Corporation.
Portions created by the Initial Developer are Copyright (C) 2004
the Initial Developer.  All Rights Reserved.

Contributor(s): SRI International

The following notice applies to the Original Code:

   Unlimited Rights assigned to the U.S. Government.
   This material may be reproduced by or for the U.S Government
   pursuant to the copyright license under the clause at DFARS
   227-7203-5(a), DFARS 227.7103-5(a), DFARS 252.227-7013(b)(1)(June
   1995), DFARS 252.227-7014 (June 1995), and FAR 52.227-14(a).
   This notice must appear in all copies of this file and its
   derivatives.
 */
package com.sri.owlseditor.cmp.graph;

import java.util.*;

public class UniqueName {
	String originalName;
	String uniqueName;
	HashSet mySet;

	public UniqueName(String name, HashSet nameSet) {
		mySet = nameSet;
		originalName = name;
		uniqueName = name;
		int count = 0;
		uniqueName = name.replace('-', '_');
		uniqueName = uniqueName.replace(':', '_');
		while (!mySet.add(uniqueName + "->" + originalName)) {
			uniqueName = uniqueName + "UN" + String.valueOf(count++);
		}
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public static String findOriginalName(String uniqueName, HashSet set) {

		String pName = "Not found";
		Iterator j = set.iterator();
		while (j.hasNext()) {
			pName = (String) j.next();
			if (pName.startsWith(uniqueName + "->"))
				break;
		}
		return pName.replaceAll(uniqueName + "->", "");
	}

}
