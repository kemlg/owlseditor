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

import com.sri.owlseditor.GroundingList;
import com.sri.owlseditor.ProcessList;
import com.sri.owlseditor.ProfileList;
import com.sri.owlseditor.ServiceList;
import com.sri.owlseditor.iopr.InputList;
import com.sri.owlseditor.iopr.OutputList;
import com.sri.owlseditor.iopr.PreconditionList;
import com.sri.owlseditor.iopr.ResultList;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/**
 * @author Daniel Elenius
 */
public class OWLSInstanceListFactory {
	
public static OWLSInstanceList create(Project project, OWLNamedClass cls){
//	System.out.println(cls.getName());
	if(cls == null)
		return null;
    if (cls.getPrefixedName().equals("service:Service"))// || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Service.owl#Service"))
	    return new ServiceList(project);
	else if (cls.getPrefixedName().equals("profile:Profile"))// || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Profile.owl#Profile"))
	    return new ProfileList(project);
	else if (cls.getPrefixedName().equals("process:Process"))// || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Process.owl#Process"))
	    return new ProcessList(project);
	else if (cls.getPrefixedName().equals("grounding:WsdlGrounding"))// || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Grounding.owl#WsdlGrounding"))
	    return new GroundingList(project);
	else if (cls.getPrefixedName().equals(InputList.INPUT))// || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Process.owl#Input"))
	    return new InputList (project);
	else if (cls.getPrefixedName().equals(OutputList.OUTPUT)) // || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Process.owl#Output"))
	    return new OutputList (project);
	else if (cls.getPrefixedName().equals(PreconditionList.PRECONDITION)) // || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/generic/Expression.owl#Condition"))
	    return new PreconditionList (project);
	else if (cls.getPrefixedName().equals(ResultList.RESULT)) // || cls.getName().equals("http://www.daml.org/services/owl-s/1.2/Process.owl#Result"))
	    return new ResultList (project);
	else
	    return null;
    }
}
