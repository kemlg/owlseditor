/*
 * ProjectLoadingThread.java
 *
 * Created on 13. März 2007, 11:53
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

import java.io.File;
import java.io.FileReader;

import java.util.Vector;
import java.util.Iterator;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;

import de.dfki.dmas.owls2wsdl.gui.GUIActionListener;
import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;

import java.util.concurrent.locks.*;  // Java 1.5

/**
 * Loading of a project file.
 * @author Oliver Fourman
 */
public class ProjectLoadingThread extends Thread {
    
    private File _file;
    private GUIActionListener _listenerRef = null;
//    final Lock lock = new ReentrantLock(); 
    
    /** Creates a new instance of ProjectLoadingThread */
    public ProjectLoadingThread(File file) {
        System.out.println("[C] ProjectLoadingThread(File)");
        this._file = file;
    }
    
    public void setFile(File file) {
        this._file = file;
    }
    
    public void setGUIActionListenerRef(GUIActionListener actionListener) {
        this._listenerRef = actionListener;
    }
    
    public void run() {
        System.out.println("[ProjectLoadingThread] run()");
        try {
            System.out.println("[ProjectLoadingThread] LOAD PROJECT FILE: "+this._file.getPath());
            
            Project p = ProjectManager.getInstance().loadPersistentProject(this._file);
            if(p!=null) 
            {
//                lock.lock();
                RuntimeModel.getInstance().setProject(p);
                p.determineAllDependecyTypes();
//                lock.unlock();                 
                RuntimeModel.getInstance().setRuntimeAndNotify(RuntimeModel.PROJECT_LOADED, "project "+this._file.getName()+" loaded");                
                RuntimeModel.getInstance().setRuntimeAndNotify(RuntimeModel.SERVICE_MODEL_CHANGED);
                RuntimeModel.getInstance().setRuntimeAndNotify(RuntimeModel.DATATYPE_MODEL_CHANGED); // status table
            }                        
            
            if(_listenerRef != null) {
                this._listenerRef.getMainWindowRef().setTitle("OWLS2WSDL Tool - "+p.getProjectName());                
                this._listenerRef.updateDatatypeList();                
                //this._listenerRef.updateServiceList();                
                this._listenerRef.showProjectLoadedMessage(p.getProjectName());
            }
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        File f = new File("D:\\tmp\\OWLS2WSDL-project-car_price_service\\car_price_service.xml");
        ProjectLoadingThread t = new ProjectLoadingThread(f);
        t.setFile(f);
        t.run();
        
        Project p = RuntimeModel.getInstance().getProject();
        //p.determineAllDependecyTypes();
        
        System.out.println("COUNT: "+p.getServiceDependencyTypes().size());
        for(Iterator it=p.getServiceDependencyTypes().iterator(); it.hasNext(); ) {
            String uri = it.next().toString();
            System.out.println(".) "+uri);
            
//            if(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(uri)) {
//                System.out.println(".) "+uri);
//            }
//            else {
//                System.out.println(".) "+uri+" ???");
//            }
        }
        
        System.out.println("MISSING TYPES");
        for(Iterator it=p.getServiceMissingTypes().iterator(); it.hasNext(); ) {            
            System.out.println(".) "+it.next().toString());
        }
        
        
        //Vector undefinedList = RuntimeModel.getInstance().getProject().collectUndefinedDatatypes();
//        for(int i=0; i<undefinedList.size(); i++) {
//            System.out.println("");
//        }
    }
    
}
