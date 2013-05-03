/*
 * WSDL2JavaWrapper.java
 *
 * Created on 12. August 2007, 13:05
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

package de.dfki.dmas.owls2wsdl.utils;

import java.net.URI;
import java.net.URL;
import java.io.File;

import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;
import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;
import de.dfki.dmas.owls2wsdl.gui.OWLS2WSDLGui;
import de.dfki.dmas.owls2wsdl.core.*;
import org.apache.axis.wsdl.WSDL2Java;
import org.apache.axis.wsdl.toJava.*;
import org.apache.axis.constants.*;

import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Wrapper class for Axis WSDL2Java emitter framework.
 * @author Oliver Fourman
 */
public class WSDL2JavaWrapper implements Runnable {
    
    private String emitterPath = "";
    private OWLS2WSDLGui mainWindowRef;
    
    private static final String GENOP_AXIS_ONLY   = "Axis Generator only";
    private static final String GENOP_AXIS_CASTOR = "Axis and Castor Generator";
    
    public static final String genops[] = {
        GENOP_AXIS_ONLY,
        GENOP_AXIS_CASTOR
    };
    
    private String genid = null;
    
    public static String[] AXISLIBS = 
        { "activation.jar","axis-ant.jar","axis.jar", "commons-discovery.jar",
          "commons-logging.jar", "dsn.jar", "imap.jar", "jaxrpc.jar",
          "junit-3.8.1.jar", "log4j-1.2.8.jar", "mail.jar", "mailapi.jar",
          "pop3.jar", "saaj.jar", "smtp.jar", "wsdl4j-1.5.1.jar" };
    
    /** Creates a new instance of AxisClientBuilder */
    public WSDL2JavaWrapper() {
        System.out.println("[C] [WSDL2JavaWrapper]");
        System.out.println(">>> "+System.getProperty("java.class.path"));
        if(OWLS2WSDLSettings.getInstance().containsKey("APP_PATH")) {
            this.emitterPath = OWLS2WSDLSettings.getInstance().getProperty("APPL_PATH");
        }
        else {
            this.emitterPath = System.getProperty("user.home")+File.separator+"owls2wsdl";
        }
        System.out.println("[WSDL2JavaWrapper] APPL_PATH: "+this.emitterPath);
        System.out.println("[WSDL2JavaWrapper] user.home >>> "+System.getProperty("user.home"));
        
        //this.emitterPath = "C:\\Temp";
        
        File tf = new File(this.emitterPath);
        if(tf.exists() && tf.isDirectory()) {
            System.out.println("[WSDL2JavaWrapper] export path: "+tf.getAbsolutePath());
        }
    }
    
    public WSDL2JavaWrapper(String id, OWLS2WSDLGui winref) {
        this();
        this.genid = id;
        this.mainWindowRef = winref;
    }
    
    public void setExportDirectory() {
        
    }
    
    public void options() throws Exception {
        //String[] args = {};
        
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(this.emitterPath));
        fc.setDialogTitle("Select WSDL file.");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);            
        fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".wsdl") || f.isDirectory();
                }
                public String getDescription() {
                    return "wsdl file (*.wsdl)";
                }
            });
        int option = fc.showOpenDialog(null);
        File wsdlFile = null;
        if(option == JFileChooser.APPROVE_OPTION)
        {
            wsdlFile = fc.getSelectedFile();
        }
        
        System.out.println("[WSDL2JavaWrapper] wsdlFile       : "+wsdlFile.getPath());
        System.out.println("[WSDL2JavaWrapper] wsdlFile Parent: "+wsdlFile.getParentFile().getAbsolutePath());
//        System.setProperty("user.dir", wsdlFile.getParentFile().getAbsolutePath());
        
        String[] args = {"-v",
                         "-D",
                         "-s",
                         "-pde.dfki.dmas.owls2wsdl.service",
                         "-o"+this.emitterPath,
                         "-t",
                         "-dRequest",
                         wsdlFile.getPath()};
        
        String argsString = "";
        for(int i=0; i<args.length; i++) {
            argsString += args[i]+" ";
        }
        argsString.trim();
        
        System.out.println("[CMD] wsdl2java "+argsString);
        
//        WSDL2Java.main( args );
        
        Emitter emitter = new Emitter();
        emitter.setVerbose(true);
        emitter.setDebug(true);
        emitter.setOutputDir(this.emitterPath);        
        emitter.setSkeletonWanted(true);
        emitter.setServerSide(true);
        emitter.setScope(Scope.REQUEST);// getScope("Request"));
        emitter.setBuildFileWanted(true);
        emitter.setTestCaseWanted(true);
        emitter.setAllWanted(false);
        emitter.setHelperWanted(false);
        emitter.setPackageName("de.dfki.dmas.owls2wsdl.service.blub");
        
//        emitter.setDeploy(true);  // causes an error
        // Run the emitter
        try {
            emitter.run(wsdlFile.getPath());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


// API error solved:
// main method of WSDL2Java class executes with FileException using whitespaces in cmd flags
// Workaround (with whitespaces) was a system call:        
//        String[] args = {"-v",
//                         "-D",
//                         "-s",
//                         "-p de.dfki.dmas.owls2wsdl.service",
//                         "-o \""+this.emitterPath+"\"",
//                         "-t",
//                         "-d Request",
//                         "\""+wsdlFile.getPath()+"\""};
        
//        String cmd = "java -cp " + System.getProperty("java.class.path") + " org.apache.axis.wsdl.WSDL2Java " + argsString;
//        System.out.println("[CMD] "+cmd);
//        try {
//            Runtime.getRuntime().exec(cmd);
//        }
//        catch(java.io.IOException ioex) {
//            System.out.println("[WSDL2JavaWrapper] IOException: "+ioex.getMessage());
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }        
    }
    
    /**
     * Simple Wrapper for WSDL Builder.
     */
    private void useExportThread(String path, Project p, AbstractService s) {        
        WSDLOWLSExport exportThread = new WSDLOWLSExport();        
        exportThread.setProject(p);
        exportThread.setExportDirectory(path);
        exportThread.addService(s);
        exportThread.setWSDLExport(true);
        exportThread.run();
    }    
    
    /**
     * Create WSDL and use AXIS WSDL2Java tool.
     */
    public void run(){
        assert RuntimeModel.getInstance().getProject() != null;        
        
        Project p = RuntimeModel.getInstance().getProject();
        AbstractService s = RuntimeModel.getInstance().getSelectedService();
                
        if(p != null && s!=null && this.genid.equals(GENOP_AXIS_ONLY)) 
        {
            int idx1 = s.getFilename().lastIndexOf("/");
            int idx2 = s.getFilename().lastIndexOf(".");
            String wsdlFilename = s.getFilename().substring(idx1+1,idx2)+".wsdl";
            File wsdlFile = new File(this.emitterPath+File.separator+wsdlFilename);            
            
            // 1st: generate WSDL file
            this.useExportThread(this.emitterPath, p, s);
            
            // 2nd: generate Java environment
            String reformatedServiceName = s.getReformatedServicename4Gen();// s.getName(),                        
            System.out.println("[WSDL2JavaWrapper] getReformatedServicename4Gen: ("+reformatedServiceName+")");
            String pkgname = "de.dfki.dmas.owls2wsdl.service."+reformatedServiceName;
            
            //System.setProperty("user.dir", this.emitterPath);
            
            String[] args = {"-v",
                             "-D",
                             "-s",
                             "-p"+pkgname,
                             "-o"+this.emitterPath,
                             "-t",
                             "-dRequest",
                             wsdlFile.getPath()};
            
            String argsString = "";
            for(int i=0; i<args.length; i++) {
                argsString += args[i]+" ";
            }
            argsString.trim();
            
            System.out.println("[CMD] WSDL2Java "+argsString);
            
//            WSDL2Java.main(args); // problem with System.exit(0)
            
            Emitter emitter = new Emitter();
            emitter.setVerbose(true);
            emitter.setDebug(true);
            emitter.setOutputDir(this.emitterPath);        
            emitter.setSkeletonWanted(true);
            emitter.setServerSide(true);
            emitter.setScope(Scope.REQUEST);// getScope("Request"));
            emitter.setBuildFileWanted(true);
            emitter.setTestCaseWanted(true);
            emitter.setAllWanted(false);
            emitter.setHelperWanted(false);
            emitter.setPackageName(pkgname);
            emitter.setTypeMappingVersion("1.2");
            
//            emitter.setNamespaceIncludes(nsIncludes);
//            emitter.setNamespaceExcludes(nsExcludes);
//            emitter.setProperties(properties);
//            emitter.setImports(!noImports);
//            emitter.setQuiet(false);            
//            emitter.setNowrap(false);
//            emitter.setAllowInvalidURL(false);
            
//            emitter.setDeploy(true);   // causes an error
            
            // Run the emitter
            try {
                emitter.run(wsdlFile.getPath());
                if(this.mainWindowRef!=null) {
                    JOptionPane.showMessageDialog(
                        this.mainWindowRef,
                        "Java files created in:\n"+this.emitterPath+"\nPackage:\n"+pkgname,
                        "WSDL2Java Emitter",
                        JOptionPane.INFORMATION_MESSAGE );
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }            
        }
        else if(p != null && s!=null && this.genid.equals(GENOP_AXIS_CASTOR)) {
            JOptionPane.showMessageDialog(
                    this.mainWindowRef,
                    "Option "+GENOP_AXIS_CASTOR+" not yet implemented.\n" +
                    "Information about this concept in german Javamag. 4.2007",
                    "WSDL2Java",
                    JOptionPane.INFORMATION_MESSAGE);            
        }
        else {            
            if(p==null) {
                System.err.println("[e] no project loaded");
            }
            if(s==null) {
                System.err.println("[e] no service selected");
            }                       
        }        
    }
    
    public static void main(String args[]) {
        WSDL2JavaWrapper w = new WSDL2JavaWrapper();
        try {
            w.options();                
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
//ARG 0: -o/D:/tmp/wa-clients/
//ARG 1: -H
//ARG 2: -pauto_price_service.wsdl
//ARG 3: /D:/tmp/wa-clients/auto_price_service.wsdl

    }
}
