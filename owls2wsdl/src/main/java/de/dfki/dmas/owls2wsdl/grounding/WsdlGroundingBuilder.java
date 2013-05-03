/*
 * WsdlGroundingBuilder.java
 *
 * Created on 5. März 2007, 15:25
 *
 * Copyright (C) 2007
 * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
 * Hochschule fuer Technik und Wirtschaft (HTW) des Saarlandes
 * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
 *
 * The code is free for non-commercial use only.
 * You can redistribute it and/or modify it under the terms
 * of the Mozilla Public License version 1.1  as
 * published by the Mozilla Foundation at
 * http://www.mozilla.org/MPL/MPL-1.1.txt
 */

package de.dfki.dmas.owls2wsdl.grounding;

import de.dfki.dmas.owls2wsdl.core.AbstractService;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

import java.util.Vector;
import java.util.Iterator;
import java.net.URI;
import javax.xml.namespace.QName;

//import impl.jena.OWLDataTypeImpl;

import org.mindswap.wsdl.*;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.vocabulary.OWL;
import org.mindswap.owl.vocabulary.XSD;
import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.grounding.Grounding;
import org.mindswap.owls.grounding.JavaAtomicGrounding;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.Input;
import org.mindswap.owls.process.Output;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.owls.service.Service;
import org.mindswap.query.ValueMap;
import org.mindswap.utils.QNameProvider;
import org.mindswap.utils.URIUtils;


/**
 *
 * @author Oliver Fourman */
public class WsdlGroundingBuilder {
    
    //private String baseURL;// = "http://dmas.dfki.de/ont/owl-s/";
    //private String baseURI = baseURL + "MyTestService#";
    
    /** Creates a new instance */
    public WsdlGroundingBuilder() {
        //this.baseURL = OWLS2WSDLSettings.getInstance().getProperty("BASE");
    }
    
    
    public WSDLTranslator translate(
            javax.wsdl.Definition wsdlDef,
            AbstractService       aService
            ) throws Exception 
    {           
        //String baseURI = this.baseURL+aService.getLocalFilename()+"#";
                
        URI baseURI = URI.create(aService.getBase());
        if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_BASE").equals("yes")) {
            String owls_base = OWLS2WSDLSettings.getInstance().getProperty("BASE");
            baseURI = URI.create(owls_base+aService.getLocalFilename());
        }
        
        int index = aService.getBase().lastIndexOf(".");        
        String wsdldoc = aService.getBase().substring(0,index)+".wsdl";        
        if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_TNS").equals("yes")) {
            String tns_basepath = OWLS2WSDLSettings.getInstance().getProperty("TNS_BASEPATH");
            index = aService.getLocalFilename().lastIndexOf(".");
            String localFilename = aService.getLocalFilename().substring(0, index)+".wsdl";
            wsdldoc = tns_basepath+localFilename;
        }
        
        if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_WSDLPATH").equals("yes")) {
            String wsdl_basepath = OWLS2WSDLSettings.getInstance().getProperty("WSDLPATH");
            index = aService.getLocalFilename().lastIndexOf(".");
            String localFilename = aService.getLocalFilename().substring(0, index)+".wsdl";
            wsdldoc = wsdl_basepath+localFilename;
        } 
        
        URI wsdldocURI = URI.create(wsdldoc);
        
        System.out.println("[WSDLTranslator] getDocumentBaseURI: " + wsdlDef.getDocumentBaseURI());
        System.out.println("[WSDLTranslator] getTargetNamespace: " + wsdlDef.getTargetNamespace());
        System.out.println("[WSDLTranslator] getQName          : " + wsdlDef.getQName().toString());
        System.out.println("[WSDLTranslator] ServiceName       : " + aService.getName());
        System.out.println("[WSDLTranslator] ServiceDescr.     : " + aService.getDescription());
        System.out.println("====================================================");
        
        WSDLService wsdl = new WSDLService(wsdlDef, URI.create(wsdlDef.getTargetNamespace()));
        WSDLOperation wsdlOp = (WSDLOperation) wsdl.getOperations().get(0);
        
        System.out.println("[WSDLTranslator] WSDLOperation     : " + wsdlOp.getName());        
        System.out.println("[WSDLTranslator] wsdl.getFileURI   : " + wsdl.getFileURI());
        System.out.println("====================================================");
                                                
        WSDLTranslator t = new WSDLTranslator(
                wsdlOp,
                baseURI,
                aService.getReformatedServiceId4Translator(),
                wsdldocURI);
        
        t.setServiceName(aService.getName()); // aService.getID().replaceAll(" ", " _")        
        t.setTextDescription(aService.getDescription());
        
        QNameProvider qnames = new QNameProvider();
        qnames.setMapping("soapEnc", WSDLConsts.soapEnc + "#");
        //qnames.getPrefixSet();

        //
        // == OWL-WSDL MAPPING
        //    
        try {
            Vector importedOntList = aService.getImportedOWLFiles(true);
            int i=0;
            for(Iterator it=importedOntList.iterator(); it.hasNext(); ) {
                String path = it.next().toString();
                System.out.println("[ont] add imported ont: "+path);
                //t.addImportEntry(OWLFactory.createOntology(URI.create(path)));
                t.addImportEntry(path);
//                qnames.setMapping("ns"+Integer.toString(i), path+"#");
//                i++;
            }
        }
        catch(java.net.URISyntaxException uriSE) {
            System.out.println("URISyntaxException: "+uriSE);
        }
        catch(java.io.FileNotFoundException fnfE) {
            System.out.println("FileNotFoundException: "+fnfE);
        }
        
        // == INPUT PARAMS
        Vector inputs = wsdlOp.getInputs();
        for(int i = 0; i < inputs.size(); i++) 
        {
            WSDLParameter p = wsdlOp.getInput(i);            
            String paramName = URIUtils.getLocalName(p.getName());
            
            QName paramType = (p.getType() == null)
                ? new QName(WSDLConsts.xsdURI, "any")
                : p.getType();
                        
            String wsdlType = paramType.getNamespaceURI() + "#" + paramType.getLocalPart();
            System.out.println("[i] WSDL input type: "+wsdlType);
            
            // By default use owl:Thing as param type
            String owlType = OWL.Thing.toString();
            
            if(paramType.getNamespaceURI().equals(WSDLConsts.soapEnc) ||
              (paramType.getNamespaceURI().equals(WSDLConsts.xsdURI) && !paramType.getLocalPart().equals("any"))) {                
                owlType = XSD.ns + paramType.getLocalPart();                
            }
            else {
                System.out.println("WSDLParameter Name: "+p.getName());
                System.out.println("WSDLParameter Type: Ns:"+p.getType().getNamespaceURI()+" Prefix:"+p.getType().getPrefix()+" Local:"+p.getType().getLocalPart());
                owlType = aService.getParameterType( URIUtils.getLocalName(p.getName()) );
                System.out.println("ABSTRACT TYPE FOR "+p.getName());
            }

            System.out.println("OWLTYPE        : "+owlType);            
            System.out.println("OWLTYPE (short): "+qnames.shortForm(owlType));

//            System.out.println("URISET: "+qnames.getURISet().size());
//            for(Iterator it=qnames.getURISet().iterator(); it.hasNext(); ) {
//                System.out.println("URISET: "+it.next().toString());
//            }
//            System.out.println("PREFIXSET: "+qnames.getPrefixSet().size());                        
            
            
            URI paramTypeURI = URI.create(owlType);
            
            String xsltTransformation = "None (XSL)";
            
            t.addInput(p, paramName, paramTypeURI, xsltTransformation);
        }

        // == OUTPUT PARAMS
        Vector outputs = wsdlOp.getOutputs();
        for(int i = 0; i < outputs.size(); i++) 
        {
            WSDLParameter p = wsdlOp.getOutput(i);            
            String paramName = URIUtils.getLocalName(p.getName());
            
            QName paramType = (p.getType() == null)
                ? new QName(WSDLConsts.xsdURI, "any")
                : p.getType();
                        
            String wsdlType = paramType.getNamespaceURI() + "#" + paramType.getLocalPart();
            System.out.println("[i] WSDL output type: "+wsdlType);
            
            // By default use owl:Thing as param type
            String owlType = OWL.Thing.toString();
            
            if(paramType.getNamespaceURI().equals(WSDLConsts.soapEnc) ||
              (paramType.getNamespaceURI().equals(WSDLConsts.xsdURI) && !paramType.getLocalPart().equals("any"))) {                
                owlType = XSD.ns + paramType.getLocalPart();                
            }
            else {
                System.out.println("WSDLParameter Name: "+p.getName());
                System.out.println("WSDLParameter Type: Ns:"+p.getType().getNamespaceURI()+" Prefix:"+p.getType().getPrefix()+" Local:"+p.getType().getLocalPart());
                owlType = aService.getParameterType( URIUtils.getLocalName(p.getName()) );
                System.out.println("ABSTRACT TYPE FOR "+p.getName());
            }

            System.out.println("OWLTYPE        : "+owlType);            
            System.out.println("OWLTYPE (short): "+qnames.shortForm(owlType));
            
            URI paramTypeURI = URI.create(owlType);
            
            String xsltTransformation = "None (XSL)";
            
            t.addOutput(p, paramName, paramTypeURI, xsltTransformation);
        }
        
        return t;
    }
    
    //public static void main(String[] args) throws Exception {
    //}
}
