/*
 * AbstractDatatypeKBData.java
 *
 * Created on 27. September 2006, 14:36
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
import java.util.Date;

/**
 * Klasse kapselt den Daten und das Marshallen/Unmarshallen der Daten nach XML
 * Die Klasse wurde nötig, weil Castor beim Mappen einen öffentlichen -
 * Konstrukor benötigt und AbstractDatatypeKB als Singleton keinen anbietet.
 * Alternativ: http://www.castor.org/tips-tricks.html
 * @author Oliver
 */
public class AbstractDatatypeKBData implements java.io.Serializable {
    
    private HashMap  _registeredDatatypes = new HashMap();
    private HashMap  _erroneousDatatypes  = new HashMap();
    private HashMap  _metaDatatypes = new HashMap();
    private Date     _modificationTime;
    private Vector   _ontologyURIs = new Vector();
    
    private Vector   _missingDataTypes = new Vector(); // no persistency
    
    /** Creates a new instance of NewClass */
    public AbstractDatatypeKBData() {
        System.out.println("[C] AbstractDatatypeKBData "+this.getClass().cast(this)); // java 1.5
        this._registeredDatatypes = new HashMap();
        this._erroneousDatatypes = new HashMap();
        this._metaDatatypes = new HashMap();
        this._modificationTime = new java.util.Date();
        this._ontologyURIs = new Vector();
    }
    
    protected void finalize() {
        System.out.println("[D] AbstractDatatypeKBData "+this.getClass().cast(this)); // java 1.5
    }
    
    public HashMap getRegisteredDatatypes() { return this._registeredDatatypes; }
    public HashMap getErroneousDatatypes() { return this._erroneousDatatypes; }
    public HashMap getMetaDatatypes() { return this._metaDatatypes; }
    public Date    getModificationTime() { return this._modificationTime; }
    public Vector  getOntologyURIs() { return this._ontologyURIs; }
    public Vector  getMissingDatatypeURIs() {
        return this._missingDataTypes;
    }
    
    public void setRegisteredDatatypes(HashMap registeredTypes) {
        this._registeredDatatypes = registeredTypes;
    }
    public void setErroneousDatatypes(HashMap erroneousDatatypes) {
        this._erroneousDatatypes = erroneousDatatypes;
    }
    public void setMetaDatatypes(HashMap metaDatatypes) {
        this._metaDatatypes = metaDatatypes;
    }
    public void setModificationTime(Date now) {
        now = new java.util.Date();
        this._modificationTime = now;
    }
    public void setOntologyURIs(Vector importedOntologyURIs) {
        this._ontologyURIs = importedOntologyURIs;
    }    
    public void addOntologyURIs(Vector importedOntologyURIs) {
        for(Iterator it=importedOntologyURIs.iterator(); it.hasNext(); ) {
            String uri = it.next().toString();
            System.out.println("[i] add ImportedOnotologyURI: "+uri);
            this._ontologyURIs.add(uri);
        }        
    }
    public void addMissingDatatypeURI(String uri) {        
        System.out.println(">> MISSING: "+uri);
        if(!this._missingDataTypes.contains(uri)) {
            this._missingDataTypes.add(uri);            
        }
    }
    
    public void importDatatypes(AbstractDatatypeKBData data) {
        System.out.println("[i] import datatypes");
        this._registeredDatatypes.putAll(data.getRegisteredDatatypes());
        this._erroneousDatatypes.putAll(data.getErroneousDatatypes());
        this._metaDatatypes.putAll(data.getMetaDatatypes());
        this._modificationTime = new java.util.Date();
        
        for(Iterator it=data.getOntologyURIs().iterator(); it.hasNext(); ) {
            String ontURI = it.next().toString();
            if(!this._ontologyURIs.contains(ontURI)) {
                this._ontologyURIs.add(ontURI);
            }
        }
    }
    
    public void importDatatypes(AbstractDatatypeKBData data, Vector greenlist) {
        System.out.println("[i] import referenced datatypes");
        boolean CHANGE_FLAG = false;
        for(Iterator it=data.getRegisteredDatatypes().keySet().iterator(); it.hasNext(); ) {
            String key = it.next().toString();
            if(greenlist.contains(key)) {
                this._registeredDatatypes.put(key, data.get(key));
                System.out.println("[i] load from persitence storage: "+key);
                CHANGE_FLAG = true;
            }
        }
        this._erroneousDatatypes.putAll(data.getErroneousDatatypes());
        this._metaDatatypes.putAll(data.getMetaDatatypes());
        if(CHANGE_FLAG) {
            this._modificationTime = new java.util.Date();
        }
        
        for(Iterator it=data.getOntologyURIs().iterator(); it.hasNext(); ) {
            String ontURI = it.next().toString();
            if(!this._ontologyURIs.contains(ontURI)) {
                this._ontologyURIs.add(ontURI);
            }
        }
    }
    
    public boolean containsKey(String key) {
        return this._registeredDatatypes.containsKey(key) || this._metaDatatypes.containsKey(key);
    }
    
    public boolean containsMetaKey(String key) {
        return this._metaDatatypes.containsKey(key);
    }
    
    public void addDatatype(AbstractDatatype datatype) {
        this._registeredDatatypes.put(datatype.getUrl(), datatype);
    }
    
    public void addError(AbstractDatatype datatype, Exception e) {
        this._erroneousDatatypes.put(datatype.getUrl(), e);
    }
    
    public void addMetaDatatype(AbstractDatatype datatype) {
        this._metaDatatypes.put(datatype.getLocalName(), datatype);
    }
    
    public void removeAllDatatypes() {
        this._registeredDatatypes.clear();
        this._erroneousDatatypes.clear();
        this._metaDatatypes.clear();
        this._modificationTime = new java.util.Date();
    }
    
    public AbstractDatatype get(String key) 
    {        
//        // Bug beim OldSchool-Mapping von HashMaps
//        Object[] keys = this._registeredDatatypes.keySet().toArray();
//        for(int i=0; i<keys.length; i++) {
//            System.out.println("KEY: ("+keys[i]+")");
//            System.out.println("VAL: ("+this._registeredDatatypes.get(keys[i].toString())+")");
//        }
        return (AbstractDatatype)this._registeredDatatypes.get(key);
    }
    
    /**
     * Remove AbstractDatatype entry from registered datatypes
     * including all references to metatypes.
     * @param key of AbstractDatatype (URL)
     */
    public void removeRegisteredDatatype(String key) {
        this._registeredDatatypes.remove(key);
    }
    
    public AbstractDatatype getMeta(String key) {
        return (AbstractDatatype)this._metaDatatypes.get(key);
    }
        
    private Vector collectParents(String uri)
    {   
        Vector temp = new Vector(); 
        if(this.containsKey(uri)) {
            for(int i=0; i<this.get(uri).getParentList().size(); i++) {
                String parent = this.get(uri).getParentList().get(i).toString();
//                System.out.println("PARENT: "+parent);
                if(this.containsKey(parent)) {
                    if(!this.get(parent).getParentList().isEmpty()) {
                        Vector temp2 = this.collectParents(parent);            
                        for(int j=0; j<temp2.size(); j++) {
                            if(!temp.contains(temp2.get(j))) {
                                temp.add(temp2.get(j).toString());
                            }
                        }
                    }
                }

                if(!temp.contains(parent)) {
//                    System.out.println("ADD PARENT: "+uri);
                    temp.add(parent);
                }
            }
        }
        
        if(!temp.contains(uri)) {
            //System.out.println("ADD DIRECT: "+uri);
            temp.add(uri);            
        }
        return temp;
    }
    
    public HashMap collectAllDependencyTypes(String uri) 
    {
        HashMap loadedDependencyTypes = new HashMap();
        
        Vector parentList = this.collectParents(uri);
        for(int i=0; i<parentList.size(); i++) 
        {            
            if( !this.containsKey(parentList.get(i).toString()) ) {
                if(!loadedDependencyTypes.keySet().contains(parentList.get(i).toString())) {
                    loadedDependencyTypes.put(parentList.get(i).toString(), "0");
                    this.addMissingDatatypeURI(parentList.get(i).toString());
                    //System.out.println(i+".) (?) "+parentList.get(i).toString());
                }

                continue;
            }
                            
            AbstractDatatype parentType = this.get(parentList.get(i).toString());
            loadedDependencyTypes.put(parentType.getUrl(), "1");
            
            for(Iterator it=parentType.getIntersectionList().iterator(); it.hasNext();) {
                String intersectionString = it.next().toString();
                if(!loadedDependencyTypes.keySet().contains(intersectionString)) {
                    //System.out.println("New (intersectionString): "+intersectionString);
                    if( this.containsKey(intersectionString) ) {                        
                        loadedDependencyTypes.put(intersectionString, "1");
                    }
                    else {
                        this.addMissingDatatypeURI(intersectionString);
                        loadedDependencyTypes.put(intersectionString, "0");
                    }
                }
            }
            for(Iterator it=parentType.getTypeList().iterator(); it.hasNext();) {
                String rdfTypeString = it.next().toString();
                if(!loadedDependencyTypes.keySet().contains(rdfTypeString)) {
                    //System.out.println("New (rdfTypeString): "+rdfTypeString);                
                    if( this.containsKey(rdfTypeString) ) {                        
                        loadedDependencyTypes.put(rdfTypeString, "1");
                    }
                    else {
                        loadedDependencyTypes.put(rdfTypeString, "0");
                        this.addMissingDatatypeURI(rdfTypeString);
                    }
                }
            }
            for(Iterator it=parentType.getProperties().iterator(); it.hasNext(); ) {
                AbstractDatatypeElement element = (AbstractDatatypeElement)it.next();
//                System.out.println("ELEMENT: ");
//                element.printData();
//                System.out.println("_____________________");
                if(!element.isPrimitive()) {                    
                    if(!loadedDependencyTypes.keySet().contains(element.getType())) {
                        //System.out.println("New (element Type): "+element.getType());
                        if(this.containsKey(element.getType())) {
                            loadedDependencyTypes.put(element.getType(), "1");
                        }
                        else {
                            loadedDependencyTypes.put(element.getType(), "0");
                            this.addMissingDatatypeURI(element.getType());
                        }
                    }
                }
            }
        }
        return loadedDependencyTypes;
    }
    
        
    public void printRegisteredDatatypes() {
        for(Iterator it=_registeredDatatypes.values().iterator(); it.hasNext(); ) {
            System.out.println("AbstractDatatype: "+((AbstractDatatype)it.next()).getUrl());
        }
    }
    
//    public Vector getRegisteredDatatypes4Mapping() {
//        return new Vector(this._registeredDatatypes.values());
//    }
//    public void setRegisteredDatatypes4Mapping(Vector importedTypes) {
//        for(int i=0; i<importedTypes.size(); i++) {
//            AbstractDatatype atype = (AbstractDatatype)importedTypes.get(i);
//            this._registeredDatatypes.put(atype.getUrl(), atype);
//        }
//    }
}