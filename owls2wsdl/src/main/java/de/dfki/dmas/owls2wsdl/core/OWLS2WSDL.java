/*
 * OWLS2WSDL.java
 *
 * Created on 27. März 2007, 15:50
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

import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;
import de.dfki.dmas.owls2wsdl.gui.*;
import de.dfki.dmas.owls2wsdl.parser.DatatypeParser;
import org.apache.commons.cli.*;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Wrapper class to switch between gui and command line mode.
 * @author Oliver Fourman
 */
public class OWLS2WSDL {
    
    /** Creates a new instance of OWL2WSDL */
    public OWLS2WSDL() {
    }
    
    private static void printDefaultHelpMessage() {
        System.out.println("usage: owls2wsdl [-help] (FILE.owl|FILE.xml)");
        System.out.println(" FILE.owl     parse ontology file and generate persisten knowledgebase");
        System.out.println(" FILE.xml     load knowledgebase and generate XML Schema");
        System.out.println(" without arguments start GUI");
    }
    
    public static void main(String[] args) {
        // http://jakarta.apache.org/commons/cli/usage.html
        System.out.println("ARG COUNT: "+args.length);
        
        OWLS2WSDLSettings.getInstance();
        
        Options options = new Options();
        Option help = new Option("help", "print help message" );
        options.addOption(help);
        
        // create the parser
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine = null;
        
        for(int i=0; i<args.length; i++) {
            System.out.println("ARG: "+args[i].toString());
        }

        if(args.length>0) {
            if(args[args.length-1].toString().endsWith(".owl")) {
                // -kbdir d:\tmp\KB http://127.0.0.1/ontology/ActorDefault.owl
                Option test  = new Option( "test", "parse only, don't save");
                Option kbdir = OptionBuilder.withArgName( "dir" )
                                    .hasArg()
                                    .withDescription( "knowledgebase directory; necessary")
                                    .create( "kbdir");
                options.addOption(test);
                options.addOption(kbdir);
                
                try {
                    cmdLine = parser.parse( options, args );
                    if(cmdLine.hasOption("help")) {
                        HelpFormatter formatter = new HelpFormatter();
                        formatter.printHelp( "owls2wsdl [options] FILE.owl", options);
                        System.exit(0);
                    }
                }
                catch( ParseException exp ) {
                    // oops, something went wrong
                    System.out.println( "Error: Parsing failed, reason: " + exp.getMessage() );
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "owls2wsdl", options, true );
                }
                
                if(args.length == 1) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "owls2wsdl [options] FILE.owl", options);
                    System.out.println("Error: Option -kbdir is missing.");
                    System.exit(0);
                }
                else {                    
                    String ontURI = args[args.length-1].toString();
                    String persistentName = "KB_"+ontURI.substring(ontURI.lastIndexOf("/")+1,ontURI.lastIndexOf("."))+"-MAP.xml";
                    
                    try {
                        if(cmdLine.hasOption("kbdir")) { 
                            String kbdirValue= cmdLine.getOptionValue("kbdir");
                            if(new File(kbdirValue).isDirectory()) {
                                DatatypeParser p = new DatatypeParser();
                                p.parse(args[args.length-1].toString());
                                p.getAbstractDatatypeKBData();
                                if(!cmdLine.hasOption("test")) {
                                    System.out.println("AUSGABE: "+kbdirValue+File.separator+persistentName);
                                    FileOutputStream ausgabeStream = new FileOutputStream(kbdirValue+File.separator+persistentName);
                                    AbstractDatatypeKB.getInstance().marshallAsXML(ausgabeStream, true); //System.out);
                                }
                            }
                        }
                    }
                    catch(java.net.MalformedURLException murle) {
                        System.err.println("MalformedURLException: "+murle.getMessage());
                        System.err.println("Try something like: http://127.0.0.1/ontology/my_ontology.owl");                        
                    }
                    catch(Exception e) {
                        System.err.println("Exception: "+e.toString());
                    }
                }
            }
            else if(args[args.length-1].toString().endsWith(".xml")) {
                // -owlclass http://127.0.0.1/ontology/Student.owl#HTWStudent  -xsd -d 1 -h D:\tmp\KB\KB_Student-MAP.xml
                Option xsd   = new Option( "xsd", "generate XML Schema");
                Option info  = new Option( "info", "print datatype information");
                Option owlclass = OptionBuilder.withArgName( "class" )
                                    .hasArg()
                                    .withDescription( "owl class to translate; necessary")
                                    .create( "owlclass");
                Option keys  = new Option( "keys", "list all owlclass keys");                
                options.addOption(keys);
                options.addOption(owlclass);
                options.addOption(info);
                options.addOption(xsd);
                options.addOption("h", "hierarchy", false, "use hierarchy pattern");
                options.addOption("d", "depth", true, "set recursion depth");
                options.addOption("b", "behavior", true, "set inheritance bevavior");
                options.addOption("p", "primitive", true, "set default primitive type");
                
                try {
                    cmdLine = parser.parse( options, args );
                    if(cmdLine.hasOption("help")) {
                        HelpFormatter formatter = new HelpFormatter();
                        formatter.printHelp( "owls2wsdl [options] FILE.xml", options);
                        System.exit(0);
                    }
                }
                catch( ParseException exp ) {
                    // oops, something went wrong
                    System.out.println( "Error: Parsing failed, reason: " + exp.getMessage() );
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "owls2wsdl", options, true );
                }
                
                if(args.length == 1) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "owls2wsdl [options] FILE.xml", options);
                    System.exit(0);
                }
                else if(cmdLine.hasOption("keys")) {
                    File f = new File(args[args.length-1].toString());
                    try {
                        AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB(f);
                    }
                    catch(Exception e) {
                        System.err.println("Error: "+e.getMessage());
                        System.exit(1);
                    }
                    AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().printRegisteredDatatypes();
                    System.exit(0);
                }
                else if(!cmdLine.hasOption("owlclass")) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "owls2wsdl [options] FILE.xml", "", options, "Info: owl class not set.\n e.g. -owlclass http://127.0.0.1/ontology/Student.owl#Student");
                    System.exit(0);
                }
                else {
                    File f = new File(args[args.length-1].toString());
                    try {
                        AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB(f);
                    }
                    catch(Exception e) {
                        System.err.println("Error: "+e.getMessage());
                        System.exit(1);
                    }
                    
                    String owlclassString = cmdLine.getOptionValue("owlclass");
                    if(!AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(owlclassString)) {
                        System.err.println("Error: Key "+owlclassString+" not in knowledgebase.");
                        System.exit(1);
                    }
                    
                    if(cmdLine.hasOption("info")) {
                        AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(owlclassString).printDatatype();
                    }
                    if(cmdLine.hasOption("xsd")) {
                        boolean hierachy = false;
                        int     depth = 0;
                        String  inheritanceBehavior = AbstractDatatype.InheritanceByNone;
                        String  defaultPrimitiveType = "http://www.w3.org/2001/XMLSchema#string";
                        
                        if(cmdLine.hasOption("h")) { hierachy = true; }
                        if(cmdLine.hasOption("d")) { depth = Integer.parseInt(cmdLine.getOptionValue("d")); }
                        if(cmdLine.hasOption("b")) { 
                            System.out.print("Set inheritance behaviour to: ");
                            if(cmdLine.getOptionValue("b").equals(AbstractDatatype.InheritanceByParentsFirstRDFTypeSecond)) {                                
                                System.out.println(AbstractDatatype.InheritanceByParentsFirstRDFTypeSecond);
                                inheritanceBehavior = AbstractDatatype.InheritanceByNone;
                            }
                            else if(cmdLine.getOptionValue("b").equals(AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond)) {
                                System.out.println(AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond);
                                inheritanceBehavior = AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond;
                            }
                            else if(cmdLine.getOptionValue("b").equals(AbstractDatatype.InheritanceByRDFTypeOnly)) {
                                System.out.println(AbstractDatatype.InheritanceByRDFTypeOnly);
                                inheritanceBehavior = AbstractDatatype.InheritanceByRDFTypeOnly;
                            }
                            else if(cmdLine.getOptionValue("b").equals(AbstractDatatype.InheritanceBySuperClassOnly)) {
                                System.out.println(AbstractDatatype.InheritanceBySuperClassOnly);
                                inheritanceBehavior = AbstractDatatype.InheritanceBySuperClassOnly;
                            }
                            else {
                                System.out.println(AbstractDatatype.InheritanceByNone);
                                inheritanceBehavior = AbstractDatatype.InheritanceByNone;
                            }
                        }
                        if(cmdLine.hasOption("p")) { 
                            defaultPrimitiveType = cmdLine.getOptionValue("p");
                            if(defaultPrimitiveType.split("#")[0].equals("http://www.w3.org/2001/XMLSchema")) {
                                System.err.println("Error: Primitive Type not valid: "+defaultPrimitiveType);
                                System.exit(1);
                            }
                        }
                        XsdSchemaGenerator xsdgen = new XsdSchemaGenerator("SCHEMATYPE",
                                hierachy,
                                depth,
                                inheritanceBehavior,
                                defaultPrimitiveType);
                        
                        try {
                            AbstractDatatypeKB.getInstance().toXSD(owlclassString, xsdgen, System.out);
                        }
                        catch(Exception e) {
                            System.err.println("Error: "+e.getMessage());
                        }
                    }
                    
                }
            }
            else {
                try {
                    cmdLine = parser.parse( options, args );
                    if(cmdLine.hasOption("help")) {
                        printDefaultHelpMessage();
                    }
                }
                catch( ParseException exp ) {
                    // oops, something went wrong
                    System.out.println( "Error: Parsing failed, reason: " + exp.getMessage() );
                    printDefaultHelpMessage();
                }                
            }
        }
        else {
            OWLS2WSDLGui.createAndShowGUI();                        
        }








//        for(Iterator it=options.getOptions().iterator(); it.hasNext(); ) {
//            String optString =  ((Option)it.next()).getOpt();
//            if(cmdLine.hasOption(optString)) {
//                System.out.println("Option set: "+optString);
//            }
//        }

    
                    
                
            
    }
}
