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
package com.sri.owlseditor.matchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.sri.owlseditor.widgets.dataflow.PerformTreeMapper;

import edu.stanford.smi.protegex.owl.inference.dig.exception.DIGReasonerException;
import edu.stanford.smi.protegex.owl.inference.dig.reasoner.DIGReasonerIdentity;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ProtegeOWLReasoner;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTaskAdapter;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTaskListener;
import edu.stanford.smi.protegex.owl.inference.util.ReasonerPreferences;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;

/**
 * The algorithm used here lets the same parameter be bound to/compared with several
 * parameters in the other process. Perhaps we should add an option to require one-to-one
 * matching.
 * 
 * @author Daniel Elenius
 */
public class LocalMatchProvider implements MatchProvider {
	private OWLNamedClass processClass;
	private OWLNamedClass inputCls;
	private OWLNamedClass outputCls;
	private OWLObjectProperty inputProperty;
	private OWLObjectProperty outputProperty;
	private OWLObjectProperty describes;
	private OWLObjectProperty presents;
	private OWLObjectProperty processProperty;
	private OWLDatatypeProperty parameterType;
	private OWLDatatypeProperty textDescription;
	
	private OWLModel model;
	private ReasonerManager reasonerManager;
	private ProtegeOWLReasoner reasoner;

	public LocalMatchProvider(OWLModel model){
		this.model = model;
		setupClassesAndProperties();
		reasonerManager = ReasonerManager.getInstance(); 
		reasoner = reasonerManager.getReasoner(model);
	}
	
	public String getName(){
		return "Local Protege KB MatchProvider";
	}
	
	private void setupClassesAndProperties(){
		processClass = model.getOWLNamedClass("process:Process");
		inputCls = model.getOWLNamedClass("process:Input");
		outputCls = model.getOWLNamedClass("process:Output");

		processProperty = model.getOWLObjectProperty("process:process");
		inputProperty = model.getOWLObjectProperty("process:hasInput");
		outputProperty = model.getOWLObjectProperty("process:hasOutput");
		parameterType = model.getOWLDatatypeProperty("process:parameterType");
		textDescription = model.getOWLDatatypeProperty("profile:textDescription");
		describes = model.getOWLObjectProperty("service:describes");
		presents = model.getOWLObjectProperty("service:presents");
	}
	
	private boolean connectedToReasoner(){
		// TODO: Get URL from Protege-OWL
		reasoner.setURL(ReasonerPreferences.getInstance().getReasonerURL()); 
		boolean connected = reasoner.isConnected(); 
		if(connected) { 
		    // Get the reasoner identity - this contains information 
		    // about the reasoner, such as its name and version, 
		    // and the tell and ask operations that it supports. 
		    DIGReasonerIdentity reasonerIdentity = reasoner.getIdentity(); 
		    System.out.println("  LocalMatchProvider connected to DL reasoner: " + reasonerIdentity.getName()); 
		}
		return connected;
	}
	
	/** Goes to the service of this process, and then to all profiles of the service,
	 * until a profile:textDescription is found. */
	private String getTextDescription(OWLIndividual process){
		Collection services = process.getPropertyValues(describes);
		Iterator it = services.iterator();
		while (it.hasNext()){
			OWLIndividual service = (OWLIndividual)it.next();
			Collection profiles = service.getPropertyValues(presents);
			Iterator it2 = profiles.iterator();
			while (it2.hasNext()){
				OWLIndividual profile = (OWLIndividual)it2.next();
				String text = (String)profile.getPropertyValue(textDescription);
				if (text != null)
					return text;
			}
		}
		return null;
	}
	
	/** For the local MatchProvider, this method is really simple. For remote
	 * ones, there is some more work to do here.
	 */ 
	public OWLIndividual getProcess(MatchResult match){
		return model.getOWLIndividual(match.getProcessString());
	}
	
	public List findMatchingProcesses(OWLIndividual originalPerform) {
		OWLIndividual originalProcess = (OWLIndividual)originalPerform.
											getPropertyValue(processProperty);
		OWLIndividual parentProcess = PerformTreeMapper.getInstance().
										getCompositeProcess(originalPerform);
		
		//System.out.println("findMatchingProcesses");
		if (connectedToReasoner()){
			ArrayList allProcesses = new ArrayList(processClass.getInstances(true));
			allProcesses.remove(originalProcess);
			allProcesses.remove(parentProcess);
			ArrayList results = new ArrayList();
			Iterator it = allProcesses.iterator();
			while (it.hasNext()){
				OWLIndividual newProcess = (OWLIndividual)it.next();
				//System.out.println("  comparing with " + newProcess.getName());
				results.add(getMatchingProcessMatchResult(originalProcess, newProcess));
			}
			return results;
		}
		System.out.println("  ERROR! LocalMatchProvider could not connect to reasoner.");
		return null;
	}

	public List findInputProviders(OWLIndividual originalPerform) {
		OWLIndividual originalProcess = (OWLIndividual)originalPerform.
											getPropertyValue(processProperty);

		//System.out.println("findInputProviders");
		if (connectedToReasoner()){
			ArrayList allProcesses = new ArrayList(processClass.getInstances(true));
			allProcesses.remove(originalProcess);
			ArrayList results = new ArrayList();
			Iterator it = allProcesses.iterator();
			while (it.hasNext()){
				OWLIndividual newProcess = (OWLIndividual)it.next();
				results.add(getInputProviderMatchResult(originalPerform, newProcess));
			}
			return results;
		}
		System.out.println("  ERROR! LocalMatchProvider could not connect to reasoner.");
		return null;
	}

	public List findOutputConsumers(OWLIndividual originalPerform) {
		OWLIndividual originalProcess = (OWLIndividual)originalPerform.
											getPropertyValue(processProperty);
		//System.out.println("findOutputConsumers");
		if (connectedToReasoner()){
			ArrayList allProcesses = new ArrayList(processClass.getInstances(true));
			allProcesses.remove(originalProcess);
			ArrayList results = new ArrayList();
			Iterator it = allProcesses.iterator();
			while (it.hasNext()){
				OWLIndividual newProcess = (OWLIndividual)it.next();
				results.add(getOutputConsumerMatchResult(originalPerform, newProcess));
			}
			return results;
		}
		System.out.println("  ERROR! LocalMatchProvider could not connect to reasoner.");
		return null;
	}
	
	/** Returns an RDFSDatatype or an RDFSClass */
	private RDFResource getParameterType(RDFResource parameter){
		DefaultRDFSLiteral literal = (DefaultRDFSLiteral)parameter.getPropertyValue(parameterType);
	    if (literal != null){
	    	String uri = literal.getString();
	    	String resourceName = model.getResourceNameForURI(uri);
	    	if (resourceName != null)
	    		return model.getRDFResource(resourceName);
	    }
		return null;
	}
	
	/** Updates the MatchResult provided with the best matches in newParameters for all the 
	 * originalParameters */
	private void addBestMatches(MatchResult matchResult, 
						   		Collection originalParameters,
								Collection newParameters){
		ArrayList unusedParameters = new ArrayList(newParameters);

		// Find out if these are inputs or outputs
		Iterator it = originalParameters.iterator();
		boolean input = false;
		if (it.hasNext()){
			OWLIndividual parameter = (OWLIndividual)it.next();
			if (parameter.hasRDFType(inputCls))
				input = true;
		}
		
		it = originalParameters.iterator();
		while (it.hasNext()){
			OWLIndividual originalParameter = (OWLIndividual)it.next();
			RDFResource oldParameterType = getParameterType(originalParameter);
			MatchPair bestMatch = new MatchPair(null, null, null, null, Matchmaker.FAIL);

			if (oldParameterType != null){
				//System.out.println("    Looking at parameter " + originalParameter.getName());
				
				Iterator it2 = newParameters.iterator();
				while (it2.hasNext() && bestMatch.getMatchType() != Matchmaker.EQUIVALENT){
					OWLIndividual newParameter = (OWLIndividual)it2.next();
					String newParameterName = newParameter.getName();
					//System.out.println("    Comparing with parameter " + newParameterName);
					RDFResource newParameterType = getParameterType(newParameter);

					if (newParameterType != null){
						int result = compare(oldParameterType, newParameterType);
						if (result == Matchmaker.EQUIVALENT){
							bestMatch = new MatchPair(originalParameter,
													  oldParameterType,
													  newParameterName, 
													  newParameterType.getName(), 
													  Matchmaker.EQUIVALENT);
						}
						else if (result == Matchmaker.SUBSUMES){
							//System.out.println("SUBSUMES match " + originalParameter.getName() +
							//					", " + newParameterName);
							bestMatch = new MatchPair(originalParameter,
													  oldParameterType,
													  newParameterName, 
													  newParameterType.getName(),
													  Matchmaker.SUBSUMES);
						}
						else if (result == Matchmaker.SUBSUMED){
							//System.out.println("SUBSUMED match " + originalParameter.getName() +
							//		", " + newParameterName);
							if (bestMatch.getMatchType() != Matchmaker.SUBSUMES){
								bestMatch = new MatchPair(originalParameter,
													   	  oldParameterType,
														  newParameterName, 
														  newParameterType.getName(),
														  Matchmaker.SUBSUMED);
							}
						}
					}
				}
			}
			int bestResult = bestMatch.getMatchType();
			if (input){
				if (bestResult != Matchmaker.FAIL){
					matchResult.addInputMatch(bestMatch);
					unusedParameters.remove(model.getRDFResource(bestMatch.getNewParameter()));
						//System.out.println("Removed " + bestMatch.getNewParameter()
						//					+ " from unusedParameters");
				}
				else
					matchResult.addInputMatch(new MatchPair(originalParameter,
															oldParameterType,
															null, null, 
															Matchmaker.FAIL));
			}
			else{
				if (bestResult != Matchmaker.FAIL){
					matchResult.addOutputMatch(bestMatch);
					unusedParameters.remove(model.getRDFResource(bestMatch.getNewParameter()));
						//System.out.println("Removed " + bestMatch.getNewParameter()
							//				+ " from unusedParameters");
				}
				else
					matchResult.addOutputMatch(new MatchPair(originalParameter,
															 oldParameterType,
															 null, null, 
															 Matchmaker.FAIL));
			}
		}
		
		it = unusedParameters.iterator();
		if (input){
			while (it.hasNext()){
				OWLIndividual newInput = (OWLIndividual)it.next();
				RDFResource type = getParameterType(newInput);
				if (type != null)
					matchResult.addExtraInput(newInput.getName(), type.getName());
				else
					matchResult.addExtraInput(newInput.getName(), null);
			}
		}
		else{
			while (it.hasNext()){
				OWLIndividual newOutput = (OWLIndividual)it.next();
				RDFResource type = getParameterType(newOutput);
				if (type != null)
					matchResult.addExtraOutput(newOutput.getName(), type.getName());
				else
					matchResult.addExtraOutput(newOutput.getName(), null);
			}
		}
	}
						   
	
	private MatchResult getMatchingProcessMatchResult(OWLIndividual originalProcess,
												      OWLIndividual newProcess){
		MatchResult matchResult = new MatchResult(originalProcess, newProcess.getName(), this);

		Collection originalInputs = originalProcess.getPropertyValues(inputProperty);
		Collection newInputs = newProcess.getPropertyValues(inputProperty);
		addBestMatches(matchResult, originalInputs, newInputs);
		
		Collection originalOutputs = originalProcess.getPropertyValues(outputProperty);
		Collection newOutputs = newProcess.getPropertyValues(outputProperty);
		addBestMatches(matchResult, originalOutputs, newOutputs);

		matchResult.setTextDescription(getTextDescription(newProcess));
		return matchResult;
	}

	
	/** This is very similar to getMatchingProcessMatchResult, but for this one, we
	 * 1) don't care about the outputs of the original process, and
	 * 2 compare the inputs of the original with the outputs of the new process 
	 * @param originalProcess
	 * @param newProcess
	 * @return
	 */
	private MatchResult getInputProviderMatchResult(OWLIndividual originalPerform,
													OWLIndividual newProcess){
		OWLIndividual originalProcess = (OWLIndividual)originalPerform.
											getPropertyValue(processProperty);
		OWLIndividual parentProcess = PerformTreeMapper.getInstance().
										getCompositeProcess(originalPerform);
		
		MatchResult matchResult = new MatchResult(originalProcess, newProcess.getName(), this);

		if (newProcess == parentProcess){
			Collection originalInputs = originalProcess.getPropertyValues(inputProperty);
			Collection parentInputs = newProcess.getPropertyValues(inputProperty);
			addBestMatches(matchResult, originalInputs, parentInputs);
		}
		else{
			Collection originalInputs = originalProcess.getPropertyValues(inputProperty);
			Collection newOutputs = newProcess.getPropertyValues(outputProperty);
			addBestMatches(matchResult, originalInputs, newOutputs);
		}

		matchResult.setTextDescription(getTextDescription(newProcess));
		return matchResult;
	}

	private MatchResult getOutputConsumerMatchResult(OWLIndividual originalPerform,
													 OWLIndividual newProcess){
		OWLIndividual originalProcess = (OWLIndividual)originalPerform.
											getPropertyValue(processProperty);
		OWLIndividual parentProcess = PerformTreeMapper.getInstance().
										getCompositeProcess(originalPerform);

		MatchResult matchResult = new MatchResult(originalProcess, newProcess.getName(), this);
		
		if (newProcess == parentProcess){
			Collection originalOutputs = originalProcess.getPropertyValues(outputProperty);
			Collection parentOutputs = newProcess.getPropertyValues(outputProperty);
			addBestMatches(matchResult, originalOutputs, parentOutputs);
		}
		else{
			Collection originalOutputs = originalProcess.getPropertyValues(outputProperty);
			Collection newInputs = newProcess.getPropertyValues(inputProperty);
			addBestMatches(matchResult, originalOutputs, newInputs);
		}
		
		matchResult.setTextDescription(getTextDescription(newProcess));
		return matchResult;
	}
	
	private int compare(RDFResource oldType, RDFResource newType){
		if (oldType instanceof OWLNamedClass &&
			newType instanceof OWLNamedClass){
			OWLNamedClass oldClass = (OWLNamedClass)oldType;
			OWLNamedClass newClass = (OWLNamedClass)newType;
		
			ReasonerTaskListener listener = new ReasonerTaskAdapter();
			boolean subsumes, subsumed;
			try{
				// New version
				int relationship = reasoner.getSubsumptionRelationship(oldClass, newClass, listener);
				
				// We use our own constants for more generality. For example, we may
				// want to use other reasoning interfaces.
				if (relationship == ProtegeOWLReasoner.CLS1_EQUIVALENT_TO_CLS2)
					return Matchmaker.EQUIVALENT;
				else if (relationship == ProtegeOWLReasoner.CLS1_SUBSUMES_CLS2)
					return Matchmaker.SUBSUMES;
				else if (relationship == ProtegeOWLReasoner.CLS1_SUBSUMED_BY_CLS2)
					return Matchmaker.SUBSUMED;

				/* -- old version
				subsumed = reasoner.isSubsumedBy(oldClass, newClass, listener);
				subsumes = reasoner.isSubsumedBy(newClass, oldClass, listener);

				if (subsumed)
					//System.out.println(oldClass.getName() + " is subsumed by " + newClass.getName());
				if (subsumes)
					//System.out.println(newClass.getName() + " is subsumed by " + oldClass.getName());
					
				if (subsumed && subsumes)
					return Matchmaker.EQUIVALENT;
				if (subsumed && !subsumes)
					return Matchmaker.SUBSUMED;
				if (!subsumed && subsumes)
					return Matchmaker.SUBSUMES;
				*/
			} catch (DIGReasonerException e){
				System.out.println("DIG Exception comparing " + oldType.getName() +
								   " with " + newType.getName() + ": " + e.getMessage());
			return Matchmaker.FAIL;
			}
		}
		// If they are datatypes, they have to be the same
		else if (oldType == newType){
			return Matchmaker.EQUIVALENT;
		}
		return Matchmaker.FAIL;
	}
	
}
