//The MIT License
//
// Copyright (c) 2004 Evren Sirin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.wsdl;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLType;
import org.mindswap.owls.grounding.AtomicGrounding;
import org.mindswap.owls.grounding.Grounding;
import org.mindswap.owls.grounding.WSDLAtomicGrounding;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.Input;
import org.mindswap.owls.process.Output;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.service.Service;
import org.mindswap.utils.URIUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFSyntax;
import impl.jena.*;

public class WSDLTranslator {      
    private Service service;    
    private OWLOntology ont;
    private URI _baseURI;
    
    /**
     * WSDLTranslator generates OWL-S definition with grounding!
     */
    public WSDLTranslator(WSDLOperation op, URI baseURI, String prefix, URI wsdldocURI) 
    {
        this._baseURI = baseURI;
        System.out.println("[WSDLTranslator] baseURI   : "+this._baseURI.toString());
        System.out.println("[WSDLTranslator] prefix    : "+prefix);
        System.out.println("[WSDLTranslator] wsdldocURI: "+wsdldocURI.toString());
        
        OWLKnowledgeBase kb = OWLFactory.createKB();
        //ont = kb.createOntology(true);
        
        //boolean create OWL-S
        ont = OWLFactory.createOntology(true);
                 
        service = ont.createService(URIUtils.createURI(_baseURI, prefix + "Service"));

        Profile profile = ont.createProfile(URIUtils.createURI(_baseURI, prefix + "Profile"));
        AtomicProcess process = ont.createAtomicProcess(URIUtils.createURI(_baseURI, prefix + "Process"));
        Grounding grounding = ont.createGrounding(URIUtils.createURI(_baseURI, prefix + "Grounding"));
        WSDLAtomicGrounding ag = ont.createWSDLAtomicGrounding(URIUtils.createURI(_baseURI, prefix + "AtomicProcessGrounding"));
        
        process.setLabel(process.getURI().getFragment());
        
        // set the links between structures
        service.setProfile(profile);
        service.setProcess(process);
        service.setGrounding(grounding);
                
        ag.setWSDL(wsdldocURI);
        
        // add the WSDL details to the atomic grounding
        ag.setProcess(process);
        ag.setOperation(URI.create(op.getOperationName()));
        ag.setPortType(URI.create(op.getPortName()));
        ag.setInputMessage(URI.create(op.getInputMessageName()));
        ag.setOutputMessage(URI.create(op.getOutputMessageName()));
        
        // add the atomic process grounding to service grounding
        grounding.addGrounding(ag);	
    }
    
    public void setServiceName(String serviceName) {
        service.getProfile().setServiceName(serviceName);
    }
    
    public void setTextDescription(String textDescription) {
        service.getProfile().setTextDescription(textDescription);
    }
    
    public void addImportEntry(String myURI) throws java.net.URISyntaxException, java.io.FileNotFoundException {
        //this.ont.addImport(importedOnt);
        System.out.println("FILEURI: "+this.ont.getKB().read(myURI).getFileURI());
        this.ont.addImport(this.ont.getKB().read(myURI));
    }
    

    public void addInput(WSDLParameter param, String paramName, URI paramType, String xsltTransformation) {
		Profile profile = service.getProfile();
		AtomicProcess process = (AtomicProcess) service.getProcess();
		
		// create process param
		Input input = ont.createInput(URIUtils.createURI(_baseURI, paramName));
		input.setLabel(paramName);
				
		OWLType type = ont.getType(paramType);
		input.setParamType(type == null ? ont.createClass(paramType) : type);
			
		// add the param to process and profile
		process.addInput(input);
		profile.addInput(input);
		
		AtomicGrounding grounding = process.getGrounding();
		// create grounding message map
		grounding.addMessageMap(input, param.getName(), xsltTransformation);
    }
   
    public void addOutput(WSDLParameter param, String paramName, URI paramType, String xsltTransformation) {
		Profile profile = service.getProfile();
		AtomicProcess process = (AtomicProcess) service.getProcess();
		
		// create process param
		Output output = ont.createOutput(URIUtils.createURI(_baseURI, paramName));
		output.setLabel(paramName);
		
		OWLType type = ont.getType(paramType);
		output.setParamType(type == null ? ont.createClass(paramType) : type);
		
		// add the param to process and profile
		process.addOutput(output);
		profile.addOutput(output);
		
		AtomicGrounding grounding = process.getGrounding();
		// create grounding message map
		grounding.addMessageMap(output, param.getName(), xsltTransformation);     
    }
	
    public void writeOWLS(Writer out) {
        ont.write(out, _baseURI);
    }
   
    public void writeOWLS(OutputStream out) {
        ont.write(out, _baseURI);
    }
}
