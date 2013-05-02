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
package com.sri.owlseditor.execute;

import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.query.ValueMap;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class ExecutionEngine extends Thread {
	final static int EXECUTEION_FAILED = 0;
	final static int EXECUTEION_SUCCESSFUL = 1;
	private OWLModel okb;
	private ExecuteFrame frame;
	private org.mindswap.owls.process.Process process;
	private ValueMap input, output;

	public ExecutionEngine(ExecuteFrame f, org.mindswap.owls.process.Process p,
			ValueMap v) {
		super("Execution Engine");
		frame = f;
		process = p;
		input = v;
	}

	public void run() {
		Log("Creating the execution engine.");
		ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		Log("Execution started.");
		try {
			output = exec.execute(process, input);
		} catch (java.lang.Exception ee) {
			Log("Error occured during execution.");
			Log(ee.toString());
			ee.printStackTrace();
			frame.OnExecutionEvent(EXECUTEION_FAILED);
			return;
		}
		frame.OnExecutionEvent(EXECUTEION_SUCCESSFUL);
	}

	private void Log(String s) {
		frame.Log(s);
	}

	public ValueMap GetOutput() {
		return output;
	}

	public org.mindswap.owls.process.Process GetProcess() {
		return process;
	}

}