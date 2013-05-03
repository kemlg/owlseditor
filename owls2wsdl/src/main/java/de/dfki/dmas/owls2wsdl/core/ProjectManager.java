/*
 * ProjectManager.java
 *
 * Created on 2. Januar 2007, 23:34
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
 * Manage persistent project files.
 * @author Oliver Fourman
 */
public class ProjectManager {
    
    /* Singleton
     */
    private static ProjectManager instance;
    
    /** Creates a new instance of ProjectManager */
    private ProjectManager() {
    }
    
    public static ProjectManager getInstance() {
        if(instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }
    
    public void mapProject(Project project, OutputStream out, boolean prettyprint) 
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
            marshaller.marshal(project);
            if(prettyprint) {
                LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "false");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    public Project loadPersistentProject(File file) {
        Project project = null;
        try {
            Mapping mapping = new Mapping();
            String mapping_file = "castor-mapping.xml";
            
            System.out.println("LOAD Mapping File: "+this.getClass().getResource(mapping_file).toString());            
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());            
            
            FileReader reader = new FileReader(file);            
            
            Unmarshaller unmarshaller = new Unmarshaller(Project.class);
            unmarshaller.setMapping(mapping);
            
            project = (Project)unmarshaller.unmarshal(reader);
            
            AbstractDatatypeKB.getInstance().setAbstractDatatypeKBData(project.getAbstractDatatypeKBData());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return project;
    }
    
    public Project loadPersistentProject(String path) {
        File file = new File(URI.create(path));
        return this.loadPersistentProject(file);
    }
    
   
    
    public static void main(String[] args) 
    {
        Project project = null;
        
        try {
            project = ProjectManager.getInstance().loadPersistentProject(new File("D:\\tmp\\Project_1.xml"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        if(project!=null) {
            project.getAbstractDatatypeKBData().printRegisteredDatatypes();
        }
        
        System.out.println(">>> "+project.getAbstractDatatypeKBData().getClass().cast(project.getAbstractDatatypeKBData())); // java 1.5
    }
}
