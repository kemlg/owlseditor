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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * This is a static class serving as a central hub for the matchmaking functionality.
 * It lets different MatchProvider modules register themselves, and for each match query,
 * it returns results from all registered modules. For now, we implement a LocalMatchProvider
 * module, which does matchmaking on local processes. But we can extend this to provide
 * UDDIMatchProvider, P2PMatchProvider, etc.
 * 
 * @author Daniel Elenius
 */
public class Matchmaker implements CleanerListener {
	// Parameters for the scoring mechanism
	//  At 1.0, the extra parameters will have the same weight as non-matched
	//  parameters. At 0.0, they will have no effect.
	private static final double EXTRA_INPUT_FACTOR = 0.3;
	private static final double EXTRA_OUTPUT_FACTOR = 0.3;
	//  At 1.0, the subsumes/subsumed parameters will have the same weight
	//  as equivalent matches. At 0.0, they will have the same weight as
	//  non-matched parameters. For finding matching processes, we prefer the
	//  type of the new parameter to be more generic than the old one. For
	//  finding input providers, we prefer the new one to be more specific.
	//  See the calculate* methods to find out how these are used.
	private static final double LOW_SUBSUME_FACTOR = 0.2;
	private static final double HIGH_SUBSUME_FACTOR = 0.8;
	
	public static final int EQUIVALENT = 0;
	public static final int SUBSUMES = 1;
	public static final int SUBSUMED = 2;
	public static final int FAIL = 3;
	public static final int EXTRA = 4; 
	
	private static Matchmaker instance;
	private Vector matchProviders = new Vector();
	private OWLModel model;
	
	/** We sort in reverse because we want best hits first */
	private class MatchResultComparator implements Comparator{
		public int compare(Object o1, Object o2){
			MatchResult m1 = (MatchResult)o1;
			MatchResult m2 = (MatchResult)o2;
			if (m1.getScore() > m2.getScore())
				return -1;
			else if (m1.getScore() < m2.getScore())
				return 1;
			else
				return 0;
		}
	}
	
	private MatchResultComparator comparator = new MatchResultComparator();
	
	private Matchmaker(OWLModel model){
		this.model = model;
		registerMatchProvider(new LocalMatchProvider(model));
		Cleaner.getInstance().registerCleanerListener(this);
	}
	
	public void cleanup(){
		instance = null;
	}
	
	public static Matchmaker getInstance(OWLModel model){
		if (instance == null)
			instance = new Matchmaker(model);
		return instance;
	}

	public void registerMatchProvider(MatchProvider provider){
		matchProviders.add(provider);
	}
	
	/** Returns a list of matches for the process */
	public List findMatchingProcesses(OWLIndividual perform){
		OWLModel model = perform.getOWLModel();
		OWLObjectProperty processProperty = (OWLObjectProperty)model.
												getOWLObjectProperty("process:process");
		OWLIndividual process = (OWLIndividual) perform.getPropertyValue(processProperty);
		
		System.out.println("Matchmaker looking for matching processs for " + process.getName());
		
		// Get all matches from all registered MatchProviders 
		Iterator it = matchProviders.iterator();
		ArrayList matchResults = new ArrayList();
		while (it.hasNext()){
			MatchProvider provider = (MatchProvider)it.next();
			System.out.println("  Calling provider: " + provider.getName());
			matchResults.addAll(provider.findMatchingProcesses(perform));
		}	

		// Calculate the scores for all the matches
		ArrayList scoredResults = new ArrayList(matchResults);
		it = matchResults.iterator();
		while (it.hasNext()){
			MatchResult mResult = (MatchResult)it.next();
			double score = calculateMatchingProcessScore(mResult);
			if (score > 0.0)
				mResult.setScore(score);
			else
				scoredResults.remove(mResult);
		}
		
		// Sort them according to score
		Collections.sort(scoredResults, comparator);

		// Return the sorted list
		return scoredResults;
	}
	
	/** Returns a list of matches that have outputs
	 * to match the provided process's inputs. */
	public List findInputProviders(OWLIndividual perform){
		OWLModel model = perform.getOWLModel();
		OWLObjectProperty processProperty = (OWLObjectProperty)model.
												getOWLObjectProperty("process:process");
		OWLIndividual process = (OWLIndividual) perform.getPropertyValue(processProperty);
		
		System.out.println("Matchmaker looking for input providers for " + process.getName());
		
		// Get all matches from all registered MatchProviders 
		Iterator it = matchProviders.iterator();
		ArrayList matchResults = new ArrayList();
		while (it.hasNext()){
			MatchProvider provider = (MatchProvider)it.next();
			System.out.println("  Calling provider: " + provider.getName());
			matchResults.addAll(provider.findInputProviders(perform));
		}	

		// Calculate the scores for all the matches
		ArrayList scoredResults = new ArrayList(matchResults);
		it = matchResults.iterator();
		while (it.hasNext()){
			MatchResult mResult = (MatchResult)it.next();
			// First, we need to remove already bound inputs
			removeBoundInputs(mResult, perform);
			
			//System.out.println("Scoring " + mResult);
			
			double score = calculateInputProviderScore(mResult);
			if (score > 0.0)
				mResult.setScore(score);
			else
				scoredResults.remove(mResult);
		}
		
		// Sort them according to score
		Collections.sort(scoredResults, comparator);

		// Return the sorted list
		return scoredResults;
	}

	/** Returns a list of matches that have inputs
	 * to match the provided process's outputs. */
	public List findOutputConsumers(OWLIndividual perform){
		OWLModel model = perform.getOWLModel();
		OWLObjectProperty processProperty = (OWLObjectProperty)model.
												getOWLObjectProperty("process:process");
		OWLIndividual process = (OWLIndividual) perform.getPropertyValue(processProperty);
		
		System.out.println("Matchmaker looking for output consumers for " + process.getName());
		
		// Get all matches from all registered MatchProviders 
		Iterator it = matchProviders.iterator();
		ArrayList matchResults = new ArrayList();
		while (it.hasNext()){
			MatchProvider provider = (MatchProvider)it.next();
			System.out.println("  Calling provider: " + provider.getName());
			matchResults.addAll(provider.findOutputConsumers(perform));
		}	

		// Calculate the scores for all the matches
		ArrayList scoredResults = new ArrayList(matchResults);
		it = matchResults.iterator();
		while (it.hasNext()){
			MatchResult mResult = (MatchResult)it.next();
			// First, we need to remove already bound inputs
			removeBoundOutputs(mResult, perform);
			
			//System.out.println("Scoring " + mResult);
			
			double score = calculateOutputConsumerScore(mResult);
			if (score > 0.0)
				mResult.setScore(score);
			else
				scoredResults.remove(mResult);
		}
		
		// Sort them according to score
		Collections.sort(scoredResults, comparator);

		// Return the sorted list
		return scoredResults;
	}
	
	/** Any input for which there already exists an input binding is removed
	 * from the matches. This is done before scoring, to reflect the fact that
	 * a match is not of much use if we already have a provide for the parameter.
	 * This also means that the user can search for input providers several times,
	 * and get successively more specific results. If new matches are desired for
	 * inputs that are already bound (e.g. the existing match is a subsume match
	 * and an equivalent match is desired) then the user should first delete the
	 * existing match.  
	 */
	private void removeBoundInputs(MatchResult mResult, OWLIndividual perform){
		OWLIndividual originalProcess = mResult.getOriginalProcess();
		OWLModel model = originalProcess.getOWLModel();
		OWLObjectProperty hasInput = model.getOWLObjectProperty("process:hasInput");
		OWLObjectProperty hasDataFrom = model.getOWLObjectProperty("process:hasDataFrom");
		OWLObjectProperty toParam = model.getOWLObjectProperty("process:toParam");

		//System.out.println("removeBoundInputs on " + mResult.getProcessString());
		
		Iterator it = mResult.getOriginalProcess().getPropertyValues(hasInput).iterator();
		while (it.hasNext()){
			OWLIndividual input = (OWLIndividual)it.next();
			
			//System.out.println("Looking at input " + input.getName());
			
			MatchPair pair = mResult.getInputMatch(input);
			Iterator it2 = perform.getPropertyValues(hasDataFrom).iterator();
			while (it2.hasNext()){
				OWLIndividual inputBinding = (OWLIndividual)it2.next();
				OWLIndividual targetInput = (OWLIndividual)inputBinding.getPropertyValue(toParam);
				if (targetInput != null){
					//System.out.println("Looking at input binding with target input " + 
					//					targetInput.getName());
					if (pair.getOriginalParameter() == targetInput){
						//System.out.println("Removing input match for " + targetInput.getName()); 
						mResult.removeInputMatch(targetInput);
						break;
					}
				}
			}
		}
	}

	/** Removes from mResult any output matches that already have dataflow
	 * declarations.
	 */
	private void removeBoundOutputs(MatchResult mResult, OWLIndividual theperform){
		OWLIndividual originalProcess = mResult.getOriginalProcess();
		OWLModel model = originalProcess.getOWLModel();
		OWLNamedClass bindingCls = model.getOWLNamedClass("process:Binding");
		OWLObjectProperty valueSource = model.getOWLObjectProperty("process:valueSource");
		OWLObjectProperty fromProcess = model.getOWLObjectProperty("process:fromProcess");
		OWLObjectProperty theVar = model.getOWLObjectProperty("process:theVar");
		OWLObjectProperty hasOutput = model.getOWLObjectProperty("process:hasOutput");

		Collection originalOutputs = mResult.getOriginalProcess().getPropertyValues(hasOutput);

		Collection bindings = bindingCls.getInstances(true);
		Iterator it = bindings.iterator();
		while (it.hasNext()){
			OWLIndividual binding = (OWLIndividual)it.next();
			OWLIndividual valueOf = (OWLIndividual)binding.getPropertyValue(valueSource);
			if (valueOf != null){
				OWLIndividual fromPerform = (OWLIndividual)valueOf.getPropertyValue(fromProcess);
				OWLIndividual fromParam = (OWLIndividual)valueOf.getPropertyValue(theVar);
				
				if (fromPerform == theperform && originalOutputs.contains(fromParam))
					mResult.removeOutputMatch(fromParam);
			}
		}
	}

	/** Calculates and returns the score of a match */
	private double calculateInputProviderScore(MatchResult match){
		int perfectInputMatches = match.getPerfectInputCount();
		int subsumesInputMatches = match.getSubsumesInputCount();
		int subsumedInputMatches = match.getSubsumedInputCount();
		int nonInputMatches =  match.getNonInputCount();
		int totalParams = perfectInputMatches + 
						  subsumesInputMatches + 
						  subsumedInputMatches + 
						  nonInputMatches;
		int extraInputs = match.getExtraInputCount();
		
		/*
		System.out.println("\nScore calculation:" +
				   "\n Perfect: " + perfectInputMatches +
				   "\n Subsumes: " + subsumesInputMatches +
				   "\n Subsumed: " + subsumedInputMatches +
				   "\n Non: " + nonInputMatches +
				   "\n Total: " + totalParams +
				   "\n Extra In: " + extraInputs + "\n");
	    */
		
		double score =  (perfectInputMatches + 
					    (subsumesInputMatches * HIGH_SUBSUME_FACTOR) +
						(subsumedInputMatches * LOW_SUBSUME_FACTOR)) / 
						(totalParams + (extraInputs * EXTRA_INPUT_FACTOR));
		
		if (score > 0.0)
			return score;
		else
			return 0.0;
	}

	/** Calculates and returns the score of a match */
	private double calculateOutputConsumerScore(MatchResult match){
		int perfectMatches = match.getPerfectOutputCount();
		int subsumesMatches = match.getSubsumesOutputCount();
		int subsumedMatches = match.getSubsumedOutputCount();
		int nonMatches =  match.getNonOutputCount();
		int totalParams = perfectMatches + 
						  subsumesMatches + 
						  subsumedMatches + 
						  nonMatches;
		int extraOutputs = match.getExtraOutputCount();
		
		/*
		System.out.println("\nScore calculation:" +
				   "\n Perfect: " + perfectMatches +
				   "\n Subsumes: " + subsumesMatches +
				   "\n Subsumed: " + subsumedMatches +
				   "\n Non: " + nonMatches +
				   "\n Total: " + totalParams +
				   "\n Extra In: " + extraOutputs + "\n");
				   */
		
		double score =  (perfectMatches + 
					    (subsumesMatches * LOW_SUBSUME_FACTOR) +
						(subsumedMatches * HIGH_SUBSUME_FACTOR)) / 
						(totalParams + (extraOutputs * EXTRA_OUTPUT_FACTOR));
		
		if (score > 0.0)
			return score;
		else
			return 0.0;
	}

	/** Calculates and returns the score of a match.
	 *  */
	private double calculateMatchingProcessScore(MatchResult match){
		int perfectInputMatches = match.getPerfectInputCount();
		int perfectOutputMatches = match.getPerfectOutputCount();
		int subsumesInputMatches =  match.getSubsumesInputCount();
		int subsumedInputMatches =  match.getSubsumedInputCount();
		int subsumesOutputMatches =  match.getSubsumesOutputCount();
		int subsumedOutputMatches =  match.getSubsumedOutputCount();
		int nonInputMatches =  match.getNonInputCount();
		int nonOutputMatches =  match.getNonOutputCount();
		int perfectMatches = perfectInputMatches + perfectOutputMatches;
		int goodSubsumeMatches = subsumedInputMatches + subsumesOutputMatches;
		int badSubsumeMatches = subsumesInputMatches + subsumedOutputMatches;
		int nonMatches = nonInputMatches + nonOutputMatches;
		int totalParams = perfectMatches + goodSubsumeMatches + badSubsumeMatches + nonMatches; 
		int extraInputs = match.getExtraInputCount();
		int extraOutputs = match.getExtraOutputCount();
		
		/*
		System.out.println("\nScore calculation:" +
						   "\n Perfect: " + perfectMatches +
						   "\n Subsumes: " + subsumesMatches +
						   "\n Subsumed: " + subsumedMatches +
						   "\n Non: " + nonMatches +
						   "\n Total: " + totalParams +
						   "\n Extra In: " + extraInputs +
						   "\n Extra Out: " + extraOutputs + "\n");
		*/
		
		double score = (perfectMatches + 
					   (goodSubsumeMatches * HIGH_SUBSUME_FACTOR) +
					   (badSubsumeMatches * LOW_SUBSUME_FACTOR)) /
					   (totalParams +
					   (extraInputs * EXTRA_INPUT_FACTOR) +
					   (extraOutputs * EXTRA_OUTPUT_FACTOR));			
		
		if (score > 0.0)
			return score;
		else
			return 0.0;
	}

}
