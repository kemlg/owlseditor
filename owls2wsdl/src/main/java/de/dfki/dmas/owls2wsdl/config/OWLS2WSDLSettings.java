/*
 * OWLS2WSDLSettings.java
 *
 * Created on 2. Januar 2007, 23:35
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

package de.dfki.dmas.owls2wsdl.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Class handles persistent configuration information.
 * @author Oliver Fourman
 */
public class OWLS2WSDLSettings {
    
    private String homepath;
    private File settingsFile;
    private Properties properties = null;
    
    /* Singleton 
     */
    private static OWLS2WSDLSettings instance = null;
    
    public static OWLS2WSDLSettings getInstance() {
        if(instance == null) {
            instance = new OWLS2WSDLSettings();
        }
        return instance;
    }
    
    /** Creates a new instance of OWLS2WSDLSettings */
    private OWLS2WSDLSettings() {
        System.out.println("[C] OWLS2WSDLSettings");
//        this.homepath = System.getenv("HOMEDRIVE")+  deprecated
//                        System.getenv("HOMEPATH");
        this.homepath = System.getProperty("user.home");        
        this.settingsFile = new File(this.homepath+File.separator+"owls2wsdl.properties");
        this.properties = new Properties();
        
        //this.settingsFile.delete();
        
        this.loadPropertyFile();
    }    
    
    private void initProperties() {
        this.properties.setProperty("lang", "en");
        this.properties.setProperty("laf", "plastic");
        this.properties.setProperty("JCONSOLE", "no");
        this.properties.setProperty("logfile", new File(this.homepath+"/owls2wsdl.log").getPath());
        this.properties.setProperty("APPL_PATH", new File(this.homepath+"/owls2wsdl").getPath());        
        this.properties.setProperty("depth", "0");
        this.properties.setProperty("xsdgen", "short");
        this.properties.setProperty("owlinfo", "no");
        this.properties.setProperty("annotations", "no");
        this.properties.setProperty("projectslot", ""); // Idee: durch Komma getrennte liste
        this.properties.setProperty("EXPORT_PATH", new File(this.homepath+"/owls2wsdl").getPath());
        this.properties.setProperty("EXPORT_MODE", "relative");
        this.properties.setProperty("RELEVANT_DATATYPES_ONLY", "no");
        // wsdl and owl-s building (re-engineering)
        this.properties.setProperty("CHANGE_BASE", "no");
        this.properties.setProperty("BASE", "http://dmas.dfki.de/owls/");
        this.properties.setProperty("CHANGE_TNS", "yes");
        this.properties.setProperty("TNS_BASEPATH", "http://dmas.dfki.de/axis/services/");
        this.properties.setProperty("CHANGE_WSDLPATH","yes");
        this.properties.setProperty("WSDLPATH","http://127.0.0.1/grounding/");
        // für GUI zusätzlich recent_service_dirs und recent_ontology_dirs
    }
    
    public void loadPropertyFile() {
        System.out.println("lookup FILE "+this.settingsFile.toURI());
        if(!this.settingsFile.exists()) 
        {               
            System.out.println("SETTINGS FILE NOT EXISTS, INITIAL CREATED.");
            this.initProperties();
            try {
                this.properties.storeToXML(new FileOutputStream(this.settingsFile), "OWLS2WSDL properties", "UTF-8"); // java 1.5
                //this.properties.store(new FileOutputStream(this.settingsFile), "OWLS2WSDL properties"); // java .1.4
            }
            catch(java.io.IOException ioe) {
                System.err.println("Can't save "+this.settingsFile.toURI());
            }
        }        
        try {
            System.out.println("LOADING SETTINGS FILE");
            this.properties.loadFromXML(new FileInputStream(this.settingsFile)); // java 1.5
            //this.properties.load(new FileInputStream(this.settingsFile)); // java 1.4
        }
        catch(java.io.FileNotFoundException fne) {
            System.err.println("Can't find "+this.settingsFile.toURI());
        }
        catch(java.io.IOException ioe) {
            System.err.println("Can't open "+this.settingsFile.toURI());
        }            
    }
    
    public void savePropertyFile() {        
        try {
            java.util.Date now = new java.util.Date();            
            this.properties.setProperty("lastedited", now.toString());
            this.properties.storeToXML(new FileOutputStream(this.settingsFile), "OWLS2WSDL properties", "UTF-8"); // java 1.5
            //this.properties.store(new FileOutputStream(this.settingsFile), "OWLS2WSDL properties"); // java 1.4
            System.out.println("[i] properties in "+this.settingsFile.getName()+" saved.");
        }
        catch(java.io.IOException ioe) {
            System.err.println("Can't save "+this.settingsFile.toURI());
        }
    }
    
    public boolean containsKey(String key) {
        Enumeration names = this.properties.propertyNames();
        boolean found = false;
        while(names.hasMoreElements()) {
            String name = names.nextElement().toString();         
            if(name.equals(key)) { found = true; }
        }
        return found;
    }
    
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }
    
    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }
    
    public Vector getRecentValues(String key) {
        Vector list = new Vector();
        if(this.containsKey(key)) {
            String[] templist = this.properties.getProperty(key).split(",");
            for(int i=0; i<templist.length; i++) {
                list.add(templist[i].trim());
            }
        }
        return list;
    }
    
    public void addValue(String key, String value) {
        if(this.containsKey(key)) {
            String temp = this.properties.getProperty(key);
            if(temp.contains(",")) {
                boolean found = false;
                String[] templist = temp.split(",");
                for(int i=0; i<templist.length; i++) {
                    if(templist[i].trim().equals(value)) { found = true; }
                }
                if(!found) {
                    this.properties.setProperty(key, temp+","+value);
                }
            }
            else {            
                this.properties.setProperty(key, temp+","+value);
            }
        }
        else {
            this.properties.setProperty(key, value);
        }
    }
    
    public void printProperties(PrintStream out) {
        this.properties.list(out);
    }
    
    public static void main(String[] args) {
        OWLS2WSDLSettings settings = new OWLS2WSDLSettings();
        settings.printProperties(System.out);
        
//        OWLS2WSDLSettings.getInstance().printProperties(System.out);
//        OWLS2WSDLSettings.getInstance().addValue("recent_service_dirs", "S1");
//        OWLS2WSDLSettings.getInstance().addValue("recent_service_dirs", "S2");
//        OWLS2WSDLSettings.getInstance().addValue("recent_service_dirs", "S3");
//        OWLS2WSDLSettings.getInstance().addValue("recent_service_dirs", "S2");
//        OWLS2WSDLSettings.getInstance().addValue("recent_service_dirs", "S4");
//        OWLS2WSDLSettings.getInstance().printProperties(System.out);
//        Vector vals = OWLS2WSDLSettings.getInstance().getRecentValues("recent_service_dirs");
//        System.out.println("VALS: "+vals.size());
//        
//        OWLS2WSDLSettings.getInstance().savePropertyFile();
        
//        java.util.Properties p = System.getProperties();
//        java.util.Enumeration keys = p.keys();
//        while(keys.hasMoreElements()) {
//            System.out.println(keys.nextElement());
//        }
        
    }
}
