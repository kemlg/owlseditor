/*
 * AbstractDatatypeKB.java
 *
 * Created on 5. September 2006, 01:49
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
import java.io.OutputStream;
// XSD generation
import org.exolab.castor.xml.schema.Schema;

/**
 * Raw XSD datatype information class without jena libs.
 * @author Oliver
 */
public class AbstractDatatypeKB implements java.io.Serializable { //extends java.util.Observable
    
    public AbstractDatatypeKBData data;    
    private XsdSchemaGenerator gen = null;
    
    /* Singleton 
     */
    private static AbstractDatatypeKB instance = null;
    
    /** Creates a new instance of AbstractDatatypeRegistry */
    private AbstractDatatypeKB() {
        System.out.println("[C] AbstractDatatypeKB");
        data = new AbstractDatatypeKBData();
    }
    
    public static AbstractDatatypeKB getInstance() {
        if(instance == null) {
            instance = new AbstractDatatypeKB();
        }
        return instance;
    }
    
    public void initNewData() {
        data = new AbstractDatatypeKBData();
    }
    
    public void setAbstractDatatypeKBData(AbstractDatatypeKBData data) {
        this.data = data;
    }
    
    public AbstractDatatypeKBData getAbstractDatatypeKBData() {
        return this.data;
    }
    
    public void addDatatype(AbstractDatatype atype) {
        this.data.addDatatype(atype);    
        //this.setChanged();  // notify nachdem alle neuen Typen eingepflegt worden sind.
    }
        
    public void printFullStatus() {
        System.out.println("Registered Datatypes: "+this.data.getRegisteredDatatypes().size());
        System.out.println("ErroneousDatatypes  : "+this.data.getErroneousDatatypes().size());
        System.out.println("MetaDatatypes       : "+this.data.getMetaDatatypes().size());
        System.out.println("Modification Time   : "+this.data.getModificationTime());
        for(Iterator it=this.data.getRegisteredDatatypes().values().iterator(); it.hasNext();) {
            ((AbstractDatatype)it.next()).printDatatype();
        }
        System.out.println("META DATATYPES___________________________________");
        for(Iterator it=this.data.getMetaDatatypes().values().iterator(); it.hasNext();) {
            ((AbstractDatatype)it.next()).printDatatype();
        }

    }
    
    public void printRegisteredDatatypes() {
        for(Iterator it=this.data.getRegisteredDatatypes().values().iterator(); it.hasNext();) {
            System.out.println(((AbstractDatatype)it.next()));
        }
    }
    
    public void printErroneousDatatypes() {
        Vector list = new Vector(this.data.getErroneousDatatypes().keySet());
        for(Iterator it=list.iterator(); it.hasNext(); ) {
            String name=it.next().toString();
            System.out.println(name+": "+this.data.getErroneousDatatypes().get(name));
        }
    }
    
    public void printMetaDatatypes() {
        Vector list = new Vector(this.data.getMetaDatatypes().keySet());
        System.out.println("MetaDatatypes Count: "+list.size());
        for(Iterator it=list.iterator(); it.hasNext(); ) {
            String name=it.next().toString();
            System.out.println("METADATA: "+name+": "+((AbstractDatatype)this.data.getMetaDatatypes().get(name)).getLocalName());
        }        
    }       
    
    // =========================================================================
    // XML SCHEMA
    //    
    public void toXSD(String uri, boolean useHierarchyPattern, int deepness, String xsdInheritance, String xsdDefaultType) throws Exception
    {
        if(this.data.getRegisteredDatatypes().containsKey(uri)) {
            System.out.println("[toXSD] ENTHALTEN: "+uri);
            AbstractDatatype curtype = (AbstractDatatype)this.data.getRegisteredDatatypes().get(uri);
            
            // new SchemaGenerator
            gen = new XsdSchemaGenerator("WSDLTYPE", useHierarchyPattern, deepness, xsdInheritance, xsdDefaultType);
            gen.toXSD(curtype);
        }
        else {
            System.out.println("[toXSD] NICHT REGISTRIERT: "+uri);
        }
    }
    
    public void toXSD(  String uri,
                        XsdSchemaGenerator xsdgen,
                        OutputStream out) throws Exception {
        if(this.data.getRegisteredDatatypes().containsKey(uri)) {
            System.out.println("[toXSD] ENTHALTEN: "+uri);
            AbstractDatatype curtype = (AbstractDatatype)this.data.getRegisteredDatatypes().get(uri);
            xsdgen.toXSD(curtype);
            xsdgen.printXSD(out);
        }
        else {
            System.out.println("[toXSD] NICHT ENTHALTEN: "+uri);
        }
    }
    
    public void toXSD(  String uri,
                        boolean useHierarchyPattern,
                        int deepness,
                        String xsdInheritance,
                        String xsdDefaultType,
                        OutputStream out) throws Exception {
        this.toXSD(uri, useHierarchyPattern, deepness, xsdInheritance, xsdDefaultType);
        this.printXSD(out);
    }
    
    
    
    public void appendToSchema(String uri) throws Exception 
    {
        if(this.data.getRegisteredDatatypes().containsKey(uri)) {
            System.out.println("[toXSD] ENTHALTEN, APPEND: "+uri);
            AbstractDatatype curtype = (AbstractDatatype)this.data.getRegisteredDatatypes().get(uri);
            gen.appendToSchema(curtype);
        }
        else {
            System.out.println("[toXSD] NICHT REGISTRIERT: "+uri);
        }
    }
    
    public void printXSD(OutputStream out) {
        gen.printXSD(out);
    }
    
    public Schema getXmlSchemaElement(String url, boolean useHierarchyPattern, int deepness, String xsdInheritance) throws Exception
    {
        Schema curSchema = null;
        if(this.data.getRegisteredDatatypes().containsKey(url)) {
            AbstractDatatype curtype = (AbstractDatatype)this.data.getRegisteredDatatypes().get(url);
            
            XsdSchemaGenerator gen = new XsdSchemaGenerator("WSDLTYPE", useHierarchyPattern, deepness, xsdInheritance);
            gen.toXSD(curtype);
            curSchema = gen.getSchema();
        }
        else {
            System.out.println("!!!! "+url+" not registered.");
        }
        return curSchema;
    }
    
    //
    // XML MARSHALLING
    //    
    public void marshallAsXML(OutputStream out, boolean prettyprint) 
    {
        AbstractDatatypeMapper.getInstance().mapAbstractDatatypeKB(data, out, prettyprint);
    }        
}


//    public boolean containsKey(String key) {
//        return this._registeredDatatypes.containsKey(key);
//    }
//    
//    public void addDatatype(AbstractDatatype datatype) {
//        this._registeredDatatypes.put(datatype.getUrl(), datatype);
//    }
//    
//    public void addError(AbstractDatatype datatype, Exception e) {
//        this._erroneousDatatypes.put(datatype.getUrl(), e);
//    }
//    
//    public AbstractDatatype get(String key) {
//        return (AbstractDatatype)this._registeredDatatypes.get(key);
//    }

