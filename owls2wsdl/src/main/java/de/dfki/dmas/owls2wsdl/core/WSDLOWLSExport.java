/*
 * WSDLOWLSExport.java
 *
 * Created on 12. März 2007, 11:58
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

package de.dfki.dmas.owls2wsdl.core;

import java.util.Vector;
import java.util.Iterator;
import java.util.Date;
import java.text.SimpleDateFormat;
        
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

import de.dfki.dmas.owls2wsdl.gui.ExportWSDLFrame;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

import de.dfki.dmas.owls2wsdl.grounding.*;
import org.mindswap.wsdl.WSDLTranslator;


/**
 * 
 * @author Oliver Fourman
 */
public class WSDLOWLSExport implements Runnable {
    
    private boolean EXPORT_WSDL = false;
    private boolean EXPORT_OWLS = false;
    
    private Vector  _selectedServices;
    private Project _project = null;
    
    private ExportWSDLFrame _guiRef;
    
    private File exportDirectory;
            
    private String common;
    
    /** Creates a new instance of WSDLOWLSExporter */
    public WSDLOWLSExport() {
        this._selectedServices = new Vector();
        this.exportDirectory = null;
        
        if(OWLS2WSDLSettings.getInstance().containsKey("EXPORT_PATH")) {
            exportDirectory = new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH"));
            System.out.println("[WSDLOWLSExport] EXPORT_PATH: "+exportDirectory.toString());
        }
    }
    
    /** Creates a new instance of WSDLOWLSExporter */
    public WSDLOWLSExport(ExportWSDLFrame dialog) {
        this();        
        this._guiRef = dialog;
    }
    
    public void setProject(Project project) {
        this._project = project;
    }
    
    public void setWSDLExport(boolean stat) {
        this.EXPORT_WSDL = stat;
    }
    
    public void setOWLSExport(boolean stat) {
        this.EXPORT_OWLS = stat;
    }
    
    public void setSelectedServices(Vector services) {
        this._selectedServices = services;
    }
    
    public void addService(AbstractService aService) {
        this._selectedServices.add(aService);
    }
    
    public void setExportDirectory(String path) {        
        File newDirectory = new File(path);
        if(newDirectory.exists() && newDirectory.isDirectory()) {
            this.exportDirectory = newDirectory;
            System.out.println("[WSDLOWLSExport] EXPORT_PATH: "+exportDirectory.toString());
        }
        else {
            System.out.println("[WSDLOWLSExport] can't set EXPORT_PATH to "+newDirectory.getAbsolutePath());
        }
    }
    
    private void searchCommonPath() 
    {
        Vector translatableServiceList = new Vector();
        for(Iterator it=this._selectedServices.iterator(); it.hasNext(); ) {
            AbstractService aService = (AbstractService)it.next();
            translatableServiceList.add(aService);
        }        
        try {
            if(translatableServiceList.size() > 0) {
                this.common = de.dfki.dmas.owls2wsdl.utils.TranslationUtils.findCommonRoot(translatableServiceList);
            }
        }
        catch(Exception e) {
            System.err.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }
        
        if(!common.startsWith("/")) {
            common = "/";
        }
        System.out.println("[i] Common: "+common);        
    }
    
    
    public void run()
    {
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd-HHmm" );
        
        System.out.println( "Thread: "+Thread.currentThread().getName() );
        System.out.println( "Thread: "+Thread.currentThread().getPriority() );
        
        System.out.println( "ExportDir: "+this.exportDirectory.getAbsolutePath());
        
        File reportFile = new File(exportDirectory.getPath()+File.separator+"report-"+df.format(now)+".txt"); 
        try {            
            reportFile.createNewFile();
    //        if(reportFile.exists()) {
    //            reportFile.delete();
    //        }
        
            FileWriter reportWriter = new FileWriter(reportFile); 

            reportWriter.write("================================================================================\n");
            reportWriter.write("=== EXPORT LOG "+df.format(now)+" === Project: "+this._project.getProjectName()+"\n");
            reportWriter.write("================================================================================\n"); 
            for(Iterator it=this._project.getAbstractDatatypeKBData().getOntologyURIs().iterator(); it.hasNext(); ) {
                reportWriter.write("=== Parsed ontology: "+it.next().toString()+"\n");
            }
            reportWriter.write("=== Project contents:\n");
            reportWriter.write("===   "+this._project.getAbstractDatatypeKBData().getRegisteredDatatypes().size()+" registered datatypes\n");
            reportWriter.write("===   1.0 services: "+this._project.getAbstractServiceCollection().getCount10()+"\n");
            reportWriter.write("===   1.1 services: "+this._project.getAbstractServiceCollection().getCount11()+"\n");
            reportWriter.write("=== Settings:\n"); 
            reportWriter.write("===   Recursion depth           : "+this._project.getElementDepth()+"\n");
            reportWriter.write("===   Default XSD type          : "+this._project.getDefaultXsdType()+"\n");        
            reportWriter.write("===   Type inheritance behaviour: "+this._project.getTypeInheritanceBehaviour()+"\n");
            if(OWLS2WSDLSettings.getInstance().getProperty("xsdgen").equals("hierarchy")) {
                reportWriter.write("===   Use specialization pattern (aka hierarchy pattern /xfront)\n");
            }
            if(OWLS2WSDLSettings.getInstance().getProperty("owlinfo").equals("yes")) {
                reportWriter.write("===   Add additional information about owl definition to enhance WSDL matching\n");
            }
            reportWriter.write("================================================================================\n");
            reportWriter.close();
            
            int counter = 0;
            for(Iterator it=this._selectedServices.iterator(); it.hasNext(); ) {
                counter ++;
                AbstractService aService = (AbstractService)it.next();
                
                reportWriter = new FileWriter(reportFile, true); 
                reportWriter.write("Service ("+counter+") "+aService.getName()+" (Vers.: "+aService.getVersion()+")\n");                
                if(this._guiRef != null) {
                    this._guiRef.appendLogMsg("Service ("+counter+") "+aService.getName()+" (Vers.: "+aService.getVersion()+")\n");
                }

                if(aService.istranslatable()) 
                {                 
                    //XsdSchemaGenerator xsdgen = RuntimeModel.getInstance().getProject().buildXsdSchemaGenerator();
                    XsdSchemaGenerator xsdgen = this._project.buildXsdSchemaGenerator();

                    int idx1 = aService.getFilename().lastIndexOf("/");
                    int idx2 = aService.getFilename().lastIndexOf(".");

                    String wsdlFilename = aService.getFilename().substring(idx1+1,idx2)+".wsdl";
                    System.out.println("[WSDL] "+wsdlFilename);                        
                    System.out.println("[COMMON] ("+this.common+")");

        //            if(this.common.equals("/")) {
        //                
        //            }
        //            else {
        //                URI uri = URI.create(aService.getFilename());
        //                String path = uri.getPath().substring(uri.getPath().indexOf(":")+1);
        //                int index = path.indexOf(this.common);
        //                String path2File = path.substring(index+this.common.length());                
        //                this.exportLog.append(exportdir.getAbsolutePath()+"/"+path2File+"\n");
        //            }

                    try { 
                        javax.wsdl.Definition wsdlDef = WSDLBuilder.getInstance().buildDefinition(aService, xsdgen);
                        
                        // == WSDL File (WSDL2OWL-S)
                        if(this.EXPORT_WSDL) {
                            File wsdlFile = new File(exportDirectory.getPath()+File.separator+wsdlFilename);                
                            System.out.println("[wsdl] "+wsdlFile.toString());
                            if(wsdlFile.createNewFile()) {                
                                FileOutputStream outStream = new FileOutputStream(wsdlFile);
                                WSDLBuilder.getInstance().printSchema(wsdlDef, outStream);
                                outStream.close();
                                if(this._guiRef != null) {
                                    this._guiRef.appendLogMsg("\t"+exportDirectory.toString()+File.separator+wsdlFilename+"\n");
                                }
                                reportWriter.write("Service ("+counter+") "+aService.getName()+" (Vers.: "+aService.getVersion()+")\n");
                                reportWriter.write("\t"+exportDirectory.toString()+File.separator+wsdlFilename+" created.\n");                            
                            }
                            else {
                                if(this._guiRef != null) {
                                    this._guiRef.appendLogMsg("\t"+wsdlFile.toString() + " already exists.\n");
                                }
                                reportWriter.write("Service ("+counter+") "+aService.getName()+" (Vers.: "+aService.getVersion()+")\n");
                                reportWriter.write("\t"+wsdlFile.toString() + " already exists.\n");
                            }
                        }
                        reportWriter.close(); // before WSDLTranslator started.

                        // == OWLS Translation (WSDL2OWL-S)
                        if(this.EXPORT_OWLS) {
                            
                            String owlsFilename = aService.getFilename().substring(idx1+1,idx2)+".owls";
                            System.out.println("[OWLS] "+owlsFilename);
                            
//                            String wsdlPath = OWLS2WSDLSettings.getInstance().getProperty("WSDLPATH");
//                            if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_WSDLPATH").equals("no")) {
//                                wsdlPath = owlsFilename.substring(0,idx1);
//                            }                                                        
//                            URI wsdlURI = URI.create(wsdlPath+wsdlFilename);
                                                                                    
                            WsdlGroundingBuilder groundingBuilder = new WsdlGroundingBuilder();
                            WSDLTranslator t = groundingBuilder.translate(wsdlDef, aService);
                            
                            File owlsFile = new File(exportDirectory.getPath()+File.separator+owlsFilename);
                            System.out.println("[owls] "+owlsFile.toString());
                            if(owlsFile.createNewFile()) {
                                FileOutputStream outStream = new FileOutputStream(owlsFile);
                                t.writeOWLS(outStream);
                                outStream.close();
                                System.out.println("[owls] Saved to " + owlsFile.toURI());
                                if(this._guiRef!=null) {
                                    this._guiRef.appendLogMsg("\t"+exportDirectory.toString()+File.separator+owlsFilename+"\n");
                                }
                                
                                reportWriter = new FileWriter(reportFile, true); 
                                reportWriter.write("Service ("+counter+") "+aService.getName()+" (Vers.: "+aService.getVersion()+")\n");
                                reportWriter.write("\t"+exportDirectory.toString()+File.separator+owlsFilename+" created.\n");
                                reportWriter.close();
                            }
                            else {
                                if(this._guiRef!=null) {
                                    this._guiRef.appendLogMsg("\t"+owlsFile.toString() + " already exists.\n");
                                }
                                
                                reportWriter = new FileWriter(reportFile, true); 
                                reportWriter.write("Service ("+counter+") "+aService.getName()+" (Vers.: "+aService.getVersion()+")\n");
                                reportWriter.write("\t"+owlsFile.toString() + " already exists.\n");
                                reportWriter.close();
                            }                    
                        }                
                    }
                    catch(javax.wsdl.WSDLException wsdlExc) {
                        System.err.println("[e] WSDLException: "+wsdlExc.getMessage());
                    }            
                    catch(java.lang.Exception exc) {
                        System.err.println("[e] Exception: "+exc.getMessage());
                        exc.printStackTrace();
                    }     
                }
                else {                
                    for(Iterator paramIt=aService.getInputParameter().iterator(); paramIt.hasNext(); ){
                        AbstractServiceParameter param = (AbstractServiceParameter)paramIt.next();
                        if( !param.isInKB() ) {
                            reportWriter.write("\tInput-Parameter\n\t"+param.getUri()+"\n\tnot in knowledgebase (KB).\n");
                            if(this._guiRef != null) {
                                this._guiRef.appendLogMsg("\tInput-Parameter "+param.getUri()+"\n\tnot in knowledgebase (KB).\n");
                            }
                        }
                        else if( !param.isValidNCName() ) {
                            reportWriter.write("\tInput-Paramater\n\t"+param.getUri()+"\n\thas invalid NCName.\n");
                            if(this._guiRef != null) {
                                this._guiRef.appendLogMsg("\tInput-Paramater\n\t"+param.getUri()+"\n\thas invalid NCName.\n");
                            }
                        }
                    }           
                    for(Iterator paramIt=aService.getOutputParameter().iterator(); paramIt.hasNext(); ){
                        AbstractServiceParameter param = (AbstractServiceParameter)paramIt.next();
                        if( !param.isInKB() ) {
                            reportWriter.write("\tOutput-Parameter\n\t"+param.getUri()+"\n\tnot in knowledgebase (KB).\n");
                            if(this._guiRef != null) {
                                this._guiRef.appendLogMsg("\tOutput-Parameter\n\t"+param.getUri()+"\n\tnot in knowledgebase (KB).\n");
                            }
                        }
                        else if( !param.isValidNCName() ) {
                            reportWriter.write("\tOutput-Paramater\n\t"+param.getUri()+"has invalid NCName.\n");
                            if(this._guiRef != null) {
                                this._guiRef.appendLogMsg("\tOutput-Paramater\n\t"+param.getUri()+"\n\thas invalid NCName.\n");
                            }
                        }
                    }                    
                }
                reportWriter.close();
            }

            reportWriter = new FileWriter(reportFile, true);
            reportWriter.write("================================================================================\n"); 
            reportWriter.write("=== summary for selected services ===  [T]:translatable, [N]:nontranslatable\n");
            reportWriter.write("================================================================================\n"); 
            for(Iterator it=this._selectedServices.iterator(); it.hasNext(); ) {            
                AbstractService aService = (AbstractService)it.next();
                if(aService.istranslatable()) {
                    reportWriter.write("=== [T] Service: "+aService.getBase()+"\n");
                } 
                else {
                    reportWriter.write("=== [N] Service: "+aService.getBase()+"\n");
                }
            }       
            reportWriter.write("================================================================================\n"); 
            reportWriter.close();
        }
        catch(java.io.IOException ioe) {
            ioe.printStackTrace();
        }        
    }}
