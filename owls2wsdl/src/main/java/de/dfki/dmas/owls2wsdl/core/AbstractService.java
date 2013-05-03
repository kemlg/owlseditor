/*
 * AbstractService.java
 *
 * Created on 28. August 2006, 14:11
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

import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.net.URI;
import java.io.OutputStream;

/**
 *
 * @author Oliver
 */
public class AbstractService implements java.io.Serializable {
    
    private String     _filename = null;
    private String     _version = null;
    private String     _qname = null;
    private String     _id = null;
    private String     _name = null;
    private String     _description = null;
    
    private HashMap    namespaceEntries;
    private Vector     importedOWLFiles;
    
    private Vector    inputParameter;
    private Vector    outputParameter;
    
    private HashMap    inputLabel;
    private HashMap    outputLabel;
    
    // BESSER: http://www.castor.org/api/org/exolab/castor/util/OrderedHashMap.html
    //private Vector     inputOrder;
    //private Vector     outputOrder;
    
    private int        inputCount;
    private int        outputCount;
        
    /** Creates a new instance of ProcessContainer */
    public AbstractService() {
        this.namespaceEntries = new HashMap();
        this.importedOWLFiles = new Vector();        
        
        this.inputParameter   = new Vector();
        this.outputParameter  = new Vector();
                
        this.inputLabel       = new HashMap();
        this.outputLabel      = new HashMap();
//        this.inputOrder       = new Vector();
//        this.outputOrder      = new Vector();
        this.inputCount  = 0;
        this.outputCount = 0;
    }
    
    /** Creates a new instance of ProcessContainer */
    public AbstractService(String filename, String qname, String name, String description) {
        this();
        /* check begin of strings for CR
        if("\b\t\n\f\r\"\\".indexOf (name.charAt(0)) >= 0) {
            name = name.substring(1);
        }
        // check end of strings for CR
        if("\b\t\n\f\r\"\\".indexOf (name.charAt(name.length()-1)) >= 0) {
            name = name.substring(0,name.length()-1);
        } */
        
        this._filename = filename;
        this._qname = qname;
        this._name = name.trim();
        this._description = description.trim();
    }
    
    public String getFilename() { return this._filename; }
    public String getVersion() { return this._version; }
    public String getQname() { return this._qname; }
    public String getID() { return this._id; }
    public String getName() { return this._name; }
    public String getDescription() { return this._description; }
    
    public HashMap getNamespaceEntries() { return this.namespaceEntries; }
    public Vector getImportedOWLFiles() { return this.importedOWLFiles; }
            
    public Vector getInputParameter() { return this.inputParameter; }
    public Vector getOutputParameter() { return this.outputParameter; }
    
    /**
     * Avoids duplicate ids in WSDL message. (WSDL validation)
     */
    public boolean hasDuplicateInputParameter() {
        if(this.inputParameter.size() > 1) {
            AbstractServiceParameterComparator comp = new AbstractServiceParameterComparator();
            for(int i=1; i<this.inputParameter.size(); i++) {
                if(comp.compare(this.inputParameter.get(0), this.inputParameter.get(i)) == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Avoids duplicate ids in WSDL message. (WSDL validation)
     */
    public boolean hasDuplicateOutputParameter() {
        if(this.outputParameter.size() > 1) {
            AbstractServiceParameterComparator comp = new AbstractServiceParameterComparator();
            for(int i=1; i<this.outputParameter.size(); i++) {
                if(comp.compare(this.outputParameter.get(0), this.outputParameter.get(i)) == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void setFilename(String filename) {
        this._filename = filename;
    }
    
    public void setVersion(String version) {
        this._version = version;
    }
    
    public void setQname(String qname) {
        this._qname = qname;
    }
    
    public void setID(String id) {
        this._id = id;
    }
    
    public void setName(String name) {
        this._name = name.trim();
    }
    
    public void setDescription(String description) {
        this._description = description.trim();
    }
    
    public void addInputParameter(String name, String paramType) {
        this.inputCount++;
        AbstractServiceParameter param = new AbstractServiceParameter(name, paramType, this.inputCount);
        //System.out.println("PARAM-I: "+param.toString());
        //this.inputParameter.put(name, param);
        this.inputParameter.add(param);
    }
    
    public void addOutputParameter(String name, String paramType) {
        this.outputCount++;
        AbstractServiceParameter param = new AbstractServiceParameter(name, paramType, this.outputCount);
        //System.out.println("PARAM-O: "+param.toString());
        //this.outputParameter.put(name, param);
        this.outputParameter.add(param);
    }
    
    public void addInputLabel(String name, String label) {
        this.inputLabel.put(name, label);
    }
    
    public void addOuputLabel(String name, String label) {
        this.outputLabel.put(name, label);
    }
    
    public void addNamespaceEntry(String key, String val) {
        this.namespaceEntries.put(key,val);
    }
    
    public void addImportedOWLFile(String path) {
        this.importedOWLFiles.add(path);
    }
    
    public String getBase() {
        return this.namespaceEntries.get("xml:base").toString();
    }
    
    public String getBasePath() {
        String basepath = this.getBase();
        int index = basepath.lastIndexOf("/");
        return basepath.substring(0,index);
    }
    
    public void printInfo() {
        System.out.println("FILE    : "+this._filename);
        System.out.println("VERSION : "+this._version);
        System.out.println("QNAME   : "+this._qname);
        System.out.println("ID      : "+this._id);
        System.out.println("PROFILE : ServiceName: ("+this._name+"), Description: ("+this._description+")");

//        Iterator it = this.namespaceEntries.keySet().iterator();
//        while(it.hasNext()) {

        for(Iterator it = this.namespaceEntries.keySet().iterator(); it.hasNext(); ) {
            String key = (String)it.next();
            System.out.println("NS ENTRY: "+key+" = "+this.namespaceEntries.get(key));
        }        
        for(int i=0; i<this.importedOWLFiles.size();i++) {
            System.out.println("IMPORT  : "+this.importedOWLFiles.get(i));
        }       
        for(Iterator it = this.inputParameter.iterator(); it.hasNext(); ) {
            System.out.println("INPUT   : "+it.next().toString());
        }
        for(Iterator it = this.outputParameter.iterator(); it.hasNext(); ) {
            System.out.println("OUTPUT  : "+it.next().toString());
        }
    }
    
    public String toString() {
        return this._id+" Name: "+this._name+" ("+this._version+") "+this._filename;
    }
    
    /**
     * Checks all AbstractServiceParameter (input/output) for validation attribute
     * attention: attributes are not persistent
     * @return    status if service is translatable
     */
    public boolean istranslatable() {
        boolean returnVal = true;
        for(Iterator it=this.getInputParameter().iterator(); it.hasNext(); ) {
            AbstractServiceParameter param = (AbstractServiceParameter)it.next();
            if(!param.isValid() && !param.isPrimitiveXsdType()) {
                returnVal = false;
            }            
        }
        for(Iterator it=this.getOutputParameter().iterator(); it.hasNext(); ) {
            AbstractServiceParameter param = (AbstractServiceParameter)it.next();
            if(!param.isValid() && !param.isPrimitiveXsdType()) {
                returnVal = false;
            }
        }
        
        return returnVal;
    }    
    
    public Object[] checkInput4InvalidNCNames() {
        Vector nameList = new Vector();
        for(Iterator it=this.getInputParameter().iterator(); it.hasNext(); ) {
            AbstractServiceParameter param = (AbstractServiceParameter)it.next();
            if(param.isValidNCName()) {
                nameList.add(param.getUri());
            }
        }
        return nameList.toArray();
    }
    
    public Object[] checkOutput4InvalidNCNames() {
        Vector nameList = new Vector();
        for(Iterator it=this.getOutputParameter().iterator(); it.hasNext(); ) {
            AbstractServiceParameter param = (AbstractServiceParameter)it.next();
            if(param.isValidNCName()) {
                nameList.add(param.getUri());
            }
        }
        return nameList.toArray();
    }
    
    public Vector getImportedOWLFiles(boolean filterOWLSImports) {
        Vector filteredFileList = new Vector();
        for(Iterator it=this.importedOWLFiles.iterator(); it.hasNext(); ) {
            String path = it.next().toString();
            if(     path.endsWith("Service.owl") ||
                    path.endsWith("Process.owl") ||
                    path.endsWith("Profile.owl") ||
                    path.endsWith("Grounding.owl") ) {
                // Filter !
            }
            else {
                filteredFileList.add(path);
            }
        }
        return filteredFileList;
    }
    
    public String getParameterType(String localName) {
        for(Iterator it=this.inputParameter.iterator(); it.hasNext(); ) {
            AbstractServiceParameter aParm = (AbstractServiceParameter)it.next();
            if( aParm.getID().equals(localName) ) {
                return aParm.getUri();
            }
        }
        for(Iterator it=this.outputParameter.iterator(); it.hasNext(); ) {
            AbstractServiceParameter aParm = (AbstractServiceParameter)it.next();
            if( aParm.getID().equals(localName) ) {
                return aParm.getUri();
            }
        }
        return null;
    }
    
    public String getLocalFilename() {
        String parsedSep = "/";
        if(this._filename.contains("\\")) {
            System.out.println("[i] filename DOS/WINDOWS formated");
            parsedSep="\\";
        }
        int index = this._filename.lastIndexOf(parsedSep);
        return this._filename.substring(index+1);
    }
    
    public String getReformatedServiceId4Translator() {        
        String temp = this._id.toLowerCase();
        if(temp.endsWith("service")) {
            int pos = this._id.length() - 7;
            return this._id.substring(0, pos);
        }        
        return this._id;
    }
    
    public String getReformatedServicename4Gen() {
        String temp = this._name.toLowerCase();
        temp = temp.replaceAll(" ","");
        temp = temp.replaceAll("_","");
        if(temp.endsWith("service")) {
            return temp.substring(0, temp.length()-7);
        } 
        return temp;
    }
    
    /**
     * WSDL4J
     */
    public boolean marshallToWSDL(OutputStream out, XsdSchemaGenerator xsdgen) 
    {        
        if(WSDLBuilder.getInstance().validateServiceParameterTypes(this)) 
        {
            try {
                System.out.println("[marshallAbstractServiceAsWSDL] "+this.toString());
                javax.wsdl.Definition def = WSDLBuilder.getInstance().buildDefinition(this, xsdgen);
                WSDLBuilder.getInstance().printSchema(def, out);
            }
            catch(javax.wsdl.WSDLException e) {
                e.printStackTrace();
            }
            catch(java.lang.Exception e) {
                System.err.println("Error: "+e.toString());
            }            
            return true;
        }
        else {
            System.err.println("[er] one or more parameter types not registered.");
            return false;
        }        
    }
}