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
import java.util.HashMap;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;

/**
 * MatchProvider.findMatchingProcesses() returns a Collection of these. 
 * Each MatchResult gives information on how the provided process matches 
 * against one other process.
 * 
 * Note that we cannot use KB objects for the matching process, because if we
 * use non-local MatchProviders, we do not have it in the local kb yet. In such
 * cases, getProcess() has to be used to retrieve and import the ontology containing
 * the process. The process and IO names should be in prefix:localname format, and
 * if necessary, the prefix declarations should be added to the local kb by the find*
 * methods in MatchProvider before the results are returned and displayed.
 * 
 * @author Daniel Elenius
 */
public class MatchResult {

	private OWLIndividual originalProcess;  // the process to find matches for
	private String newProcess;				// the process that is matched against
	private String textDescription;
	private MatchProvider provider;			// the provider of this match
	private double score;

	// Extra IOs in the found process (gives negative score)
	// On an InputProvider match, only extra inputs are considered
	private Collection extraInputs = new ArrayList();		
	private Collection extraOutputs = new ArrayList();
	
	// The IOs of the original process are divided into three
	// disjoint categories
	// 
	// On a MatchingProcess match, IOs are matched Input-Input and Output-Output.
	// On an InputProvider match, IOs are matched Original Input - New Output
	/*
	private Collection perfectInputMatches = new ArrayList();  // perfectly matched IOs (Inputs)
	private Collection subsumesInputMatches = new ArrayList();  // subsumed-mathed IOs (Inputs)
	private Collection subsumedInputMatches = new ArrayList();  // subsumed-mathed IOs (Inputs)
	private Collection nonInputMatches = new ArrayList();		// IOs (Inputs) that are not at all in the found process 

	private Collection perfectOutputMatches = new ArrayList();  // perfectly matched IOs (Inputs)
	private Collection subsumesOutputMatches = new ArrayList();  // subsumed-mathed IOs (Inputs)
	private Collection subsumedOutputMatches = new ArrayList();  // subsumed-mathed IOs (Inputs)
	private Collection nonOutputMatches = new ArrayList();		// IOs (Inputs) that are not at all in the found process 
	*/
	
	private HashMap inputMatches = new HashMap();
	private HashMap outputMatches = new HashMap();
	
	private int perfectInputCount = 0;
	private int subsumesInputCount = 0;
	private int subsumedInputCount = 0;
	private int nonInputCount = 0;
	private int extraInputCount = 0;
	private int perfectOutputCount = 0;
	private int subsumesOutputCount = 0;
	private int subsumedOutputCount = 0;
	private int nonOutputCount = 0;
	private int extraOutputCount = 0;
	
	public MatchResult(OWLIndividual originalProcess,
					   String newProcess,
					   MatchProvider provider){
		
		this.originalProcess = originalProcess;
		this.newProcess = newProcess;
		this.provider = provider;
	}

	public MatchProvider getProvider(){
		return provider;
	}

	public OWLIndividual getOriginalProcess(){
		return originalProcess;
	}
	
	/** Note that this will retrieve and import the process if it is
	 * not in the local kb.
	 */
	public OWLIndividual getProcess(){
		return provider.getProcess(this);
	}
	
	public String getProcessString(){
		return newProcess;
	}

	/** This could be the profile:textDescription property value of a related Profile, but
	 * it doesn't have to be. */
	public void setTextDescription(String text){
		textDescription = text;
	}
	
	public String getTextDescription(){
		return textDescription;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public double getScore(){
		return score;
	}
	
	private void incrementInputCount(MatchPair pair){
		int matchType = pair.getMatchType();
		switch(matchType){
		case Matchmaker.FAIL:
			nonInputCount++;
		break;
		case Matchmaker.SUBSUMES:
			subsumesInputCount++;
		break;
		case Matchmaker.SUBSUMED:
			subsumedInputCount++;
		break;
		case Matchmaker.EQUIVALENT:
			perfectInputCount++;
		break;
		case Matchmaker.EXTRA:
			extraInputCount++;
		break;
		}
	}

	private void incrementOutputCount(MatchPair pair){
		int matchType = pair.getMatchType();
		switch(matchType){
		case Matchmaker.FAIL:
			nonOutputCount++;
		break;
		case Matchmaker.SUBSUMES:
			subsumesOutputCount++;
		break;
		case Matchmaker.SUBSUMED:
			subsumedOutputCount++;
		break;
		case Matchmaker.EQUIVALENT:
			perfectOutputCount++;
		break;
		case Matchmaker.EXTRA:
			extraOutputCount++;
		break;
		}
	}

	private void decrementInputCount(MatchPair pair){
		int matchType = pair.getMatchType();
		switch(matchType){
		case Matchmaker.FAIL:
			nonInputCount--;
		break;
		case Matchmaker.SUBSUMES:
			subsumesInputCount--;
		break;
		case Matchmaker.SUBSUMED:
			subsumedInputCount--;
		break;
		case Matchmaker.EQUIVALENT:
			perfectInputCount--;
		break;
		case Matchmaker.EXTRA:
			extraInputCount--;
		break;
		}
	}

	private void decrementOutputCount(MatchPair pair){
		int matchType = pair.getMatchType();
		switch(matchType){
		case Matchmaker.FAIL:
			nonOutputCount--;
		break;
		case Matchmaker.SUBSUMES:
			subsumesOutputCount--;
		break;
		case Matchmaker.SUBSUMED:
			subsumedOutputCount--;
		break;
		case Matchmaker.EQUIVALENT:
			perfectOutputCount--;
		break;
		case Matchmaker.EXTRA:
			extraOutputCount--;
		break;
		}
	}
	
	public int getPerfectInputCount(){
		return perfectInputCount;
	}

	public int getSubsumesInputCount(){
		return subsumesInputCount;
	}

	public int getSubsumedInputCount(){
		return subsumedInputCount;
	}

	public int getNonInputCount(){
		return nonInputCount;
	}

	public int getExtraInputCount(){
		return extraInputCount;
	}

	public int getPerfectOutputCount(){
		return perfectOutputCount;
	}

	public int getSubsumesOutputCount(){
		return subsumesOutputCount;
	}

	public int getSubsumedOutputCount(){
		return subsumedOutputCount;
	}

	public int getNonOutputCount(){
		return nonOutputCount;
	}

	public int getExtraOutputCount(){
		return extraOutputCount;
	}

	public void addExtraInput(String newInput, String newType){
		extraInputs.add(new MatchPair(null, null, newInput, newType, Matchmaker.EXTRA));
	}

	public Collection getExtraInputs(){
		return extraInputs;
	}
	
	public void addExtraOutput(String newOutput, String newType){
		extraOutputs.add(new MatchPair(null, null, newOutput, newType, Matchmaker.EXTRA));
	}
	
	public Collection getExtraOutputs(){
		return extraOutputs;
	}
	
	public void addInputMatch(MatchPair pair){
		inputMatches.put(pair.getOriginalParameter(), pair);
		incrementInputCount(pair);
	}
	
	public void removeInputMatch(OWLIndividual input){
		MatchPair pair = (MatchPair)inputMatches.remove(input);
		decrementInputCount(pair);
	}
	
	public Collection getInputMatches(){
		return inputMatches.values(); 
	}
	
	public void addOutputMatch(MatchPair pair){
		outputMatches.put(pair.getOriginalParameter(), pair);
		incrementOutputCount(pair);
	}

	public void removeOutputMatch(OWLIndividual output){
		MatchPair pair = (MatchPair)outputMatches.remove(output);
		decrementOutputCount(pair);
	}

	public Collection getOutputMatches(){
		return outputMatches.values(); 
	}

	/* Returns the MatchPair where the given parameter is the original parameter.
	 * If it is a non-match, null is returned. */
	public MatchPair getInputMatch(OWLIndividual parameter){
		return (MatchPair) inputMatches.get(parameter);
		/*
		Iterator it = perfectInputMatches.iterator();
		while (it.hasNext()){
			MatchPair pair = (MatchPair)it.next();
			if (pair.getOriginalParameter() == parameter)
				return pair;
				
		}
		
		it = subsumesInputMatches.iterator();
		while (it.hasNext()){
			MatchPair pair = (MatchPair)it.next();
			if (pair.getOriginalParameter() == parameter)
				return pair;
		}

		it = subsumedInputMatches.iterator();
		while (it.hasNext()){
			MatchPair pair = (MatchPair)it.next();
			if (pair.getOriginalParameter() == parameter)
				return pair;
		}

		return null;
		*/
	}

	/* Returns the MatchPair where the given parameter is the original parameter.
	 * If it is a non-match, null is returned. */
	public MatchPair getOutputMatch(OWLIndividual parameter){
		return (MatchPair) outputMatches.get(parameter);
		/*
		Iterator it = perfectOutputMatches.iterator();
		while (it.hasNext()){
			MatchPair pair = (MatchPair)it.next();
			if (pair.getOriginalParameter() == parameter)
				return pair;
		}
		
		it = subsumesOutputMatches.iterator();
		while (it.hasNext()){
			MatchPair pair = (MatchPair)it.next();
			if (pair.getOriginalParameter() == parameter)
				return pair;
		}

		it = subsumedOutputMatches.iterator();
		while (it.hasNext()){
			MatchPair pair = (MatchPair)it.next();
			if (pair.getOriginalParameter() == parameter)
				return pair;
		}

		return null;
		*/
	}

	
	/*
	public void addPerfectInputMatch(MatchPair match){
		perfectInputMatches.add(match);
	}

	public Collection getPerfectInputMatches(){
		return perfectInputMatches;
	}
	
	public void addPerfectOutputMatch(MatchPair match){
		perfectOutputMatches.add(match);
	}

	public Collection getPerfectOutputMatches(){
		return perfectOutputMatches;
	}

	public void addSubsumesInputMatch(MatchPair match){
		subsumesInputMatches.add(match);
	}

	public Collection getSubsumesInputMatches(){
		return subsumesInputMatches;
	}

	public void addSubsumedInputMatch(MatchPair match){
		subsumedInputMatches.add(match);
	}

	public Collection getSubsumedInputMatches(){
		return subsumedInputMatches;
	}

	public void addSubsumesOutputMatch(MatchPair match){
		subsumesOutputMatches.add(match);
	}

	public Collection getSubsumesOutputMatches(){
		return subsumesOutputMatches;
	}

	public void addSubsumedOutputMatch(MatchPair match){
		subsumedOutputMatches.add(match);
	}

	public Collection getSubsumedOutputMatches(){
		return subsumedOutputMatches;
	}

	public void addNonInputMatch(OWLIndividual oldInput){
		nonInputMatches.add(oldInput);
	}

	public Collection getNonInputMatches(){
		return nonInputMatches;
	}

	public void addNonOutputMatch(OWLIndividual oldOutput){
		nonOutputMatches.add(oldOutput);
	}
	
	public Collection getNonOutputMatches(){
		return nonOutputMatches;
	}
*/
	
	public String toString(){
		return "\nMatchResult: " + newProcess +
				"\n Input Matches: " + inputMatches.values() +
				"\n Output Matches: " + outputMatches.values() +
				"\n Extra Inputs: " + extraInputs +
				"\n Extra Outputs: " + extraOutputs;
	}
}
