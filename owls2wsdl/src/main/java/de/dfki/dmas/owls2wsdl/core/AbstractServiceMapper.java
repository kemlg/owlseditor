/*
 * AbstractServiceMapper.java
 *
 * Created on 11. September 2006, 15:02
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
import org.exolab.castor.xml.Unmarshaller;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import org.exolab.castor.util.LocalConfiguration;


/**
 *
 * @author Oliver
 */
public class AbstractServiceMapper {
    
    private static AbstractServiceMapper instance;   
    
    /** Creates a new instance of AbstractServiceMapper */
    private AbstractServiceMapper() {
    }
    
    public static AbstractServiceMapper getInstance() {
        if(instance == null) {
            instance = new AbstractServiceMapper();
        }
        return instance;
    } 
    
    public void mapAbstractService(AbstractService service) 
    {        
        try {
            Mapping mapping = new Mapping();
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());          
            Marshaller marshaller = new Marshaller(new OutputStreamWriter(System.out, "UTF8"));
            marshaller.setMapping(mapping);
            marshaller.marshal(service);
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    public void mapAbstractServiceCollection(AbstractServiceCollection serviceCollection, OutputStream out, boolean prettyprint)
    {
        try {
            Mapping mapping = new Mapping();            
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            
            if(prettyprint) {
                LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
            }            
            Marshaller marshaller = new Marshaller(new OutputStreamWriter(out, "UTF8"));
            marshaller.setMapping(mapping);
            marshaller.marshal(serviceCollection);
            if(prettyprint) {
                LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "false");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } 
    }       
    
    public void saveAbstractServiceCollection(AbstractServiceCollection serviceCollection, String path) {
        try {
            Mapping mapping = new Mapping();            
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            
            File f = new File( path );
            if (! f.exists() ) {
                f.createNewFile();
            }
            
            FileWriter writer = new FileWriter(f);            
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(mapping);
            marshaller.marshal(serviceCollection);
        } catch (Throwable t) {
            t.printStackTrace();
        } 
    }
    
    public AbstractServiceCollection loadAbstractServiceCollection(File file) {
        AbstractServiceCollection data = null;
        try {
            Mapping mapping = new Mapping();
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            FileReader reader = new FileReader(file);            
            Unmarshaller unmarshaller = new Unmarshaller(AbstractServiceCollection.class);
            unmarshaller.setMapping(mapping);                        
            data = (AbstractServiceCollection)unmarshaller.unmarshal(reader);            
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return data;
    }
    
}
