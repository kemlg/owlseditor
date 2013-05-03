/*
 * AbstractDatatypeMapper.java
 *
 * Created on 18. September 2006, 14:52
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

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.URI;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.util.LocalConfiguration;

/**
 *
 * @author Oliver
 */
public class AbstractDatatypeMapper {
    
    /* Singleton
     */
    private static AbstractDatatypeMapper instance;
    
    /** Creates a new instance of AbstractDatatypeMapper */
    private AbstractDatatypeMapper() {
    }
    
    public static AbstractDatatypeMapper getInstance() {
        if(instance == null) {
            instance = new AbstractDatatypeMapper();
        }
        return instance;
    } 
    
    public void mapAbstractDatatype(AbstractDatatype datatype) 
    {        
        try {
            Mapping mapping = new Mapping();
            
            // 1. Load the mapping information from the file
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            
            //LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.xml.saveMapKeys", "false");
            
            Marshaller marshaller = new Marshaller(new OutputStreamWriter(System.out, "UTF8"));
            marshaller.setMapping(mapping);
            //marshaller.setSuppressXSIType( true ) ;
            //marshaller.setSuppressNamespaces( true ) ;
            marshaller.marshal(datatype);
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    public void mapAbstractDatatypeKB(AbstractDatatypeKBData kb_data, OutputStream out, boolean prettyprint) 
    {        
        try {
            Mapping mapping = new Mapping();
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            
            //LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.xml.saveMapKeys", "false");
            
            if(prettyprint) {
                LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
            }
            Marshaller marshaller = new Marshaller(new OutputStreamWriter(out, "UTF8"));
            marshaller.setMapping(mapping);
            marshaller.marshal(kb_data);
            if(prettyprint) {
                LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "false");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    public void loadAbstractDatatypeKB(File file) throws java.lang.Exception {

        Mapping mapping = new Mapping();
        String mapping_file = "castor-mapping.xml";
        mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
        //LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.xml.saveMapKeys", "false");

        FileReader reader = new FileReader(file);

        Unmarshaller unmarshaller = new Unmarshaller(AbstractDatatypeKBData.class);
        unmarshaller.setMapping(mapping);

        AbstractDatatypeKBData data = (AbstractDatatypeKBData)unmarshaller.unmarshal(reader);
        AbstractDatatypeKB.getInstance().setAbstractDatatypeKBData(data);
    }
    
    public void importAbstractDatatypeKB(File file) {
        try {
            Mapping mapping = new Mapping();
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            
            FileReader reader = new FileReader(file);
            
            Unmarshaller unmarshaller = new Unmarshaller(AbstractDatatypeKBData.class);
            unmarshaller.setMapping(mapping);
            
            AbstractDatatypeKBData newdata = (AbstractDatatypeKBData)unmarshaller.unmarshal(reader);
            AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().importDatatypes(newdata);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void loadAbstractDatatypeKB(String path) {
        try {
            File file = new File(URI.create(path));
            this.loadAbstractDatatypeKB(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
    public static void main(String[] args) {
        System.out.println("LOAD KB");
        
        File kb_file = new File("D:\\tmp\\AbstractDatatypesKB2.xml");
        if( kb_file.canRead() )
            System.out.println("OK. File is readable.");
        
        //AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB(kb_file);
        AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/AbstractDatatypesKB2.xml");
        AbstractDatatypeKB.getInstance().printRegisteredDatatypes();
        //AbstractDatatypeKB.getInstance().marshallAsXML(System.out);
    }
}
