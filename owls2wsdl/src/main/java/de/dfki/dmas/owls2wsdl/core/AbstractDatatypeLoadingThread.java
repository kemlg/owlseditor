/*
 * AbstractDatatypeLoadingThread.java
 *
 * Created on 13. März 2007, 11:25
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

import java.util.concurrent.locks.*;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;

import de.dfki.dmas.owls2wsdl.gui.GUIActionListener;
import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

/**
 * Class to load persistent datatype information.
 * Try to improve loading persitent datatype file.
 * @author cosmic
 */
public class AbstractDatatypeLoadingThread extends Thread {
    
    private File _file;
    private GUIActionListener _listenerRef = null;
    
    final Lock lock = new ReentrantLock(); 
    
    /** Creates a new instance of AbstractDatatypeLoadingThread */
    public AbstractDatatypeLoadingThread(File file) {
        System.out.println("[C] AbstractDatatypeLoadingThread(File)");
        this._file = file;
    }
    
    public void setFile(File file) {
        this._file = file;
    }
    
    public void setGUIActionListenerRef(GUIActionListener actionListener) {
        this._listenerRef = actionListener;
    }
    
    public void run() {
        try {
            Mapping mapping = new Mapping();
            String mapping_file = "castor-mapping.xml";
            mapping.loadMapping(this.getClass().getResource(mapping_file).toString());
            FileReader reader = new FileReader(this._file);
            
            Unmarshaller unmarshaller = new Unmarshaller(AbstractDatatypeKBData.class);
            unmarshaller.setMapping(mapping);
            
            AbstractDatatypeKBData data = (AbstractDatatypeKBData)unmarshaller.unmarshal(reader);
            if(OWLS2WSDLSettings.getInstance().getProperty("RELEVANT_DATATYPES_ONLY").equals("yes")) {                
                if(RuntimeModel.getInstance().getProject() != null) {
                    int dependencyCount  = RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size();
                    int missingTypeCount = RuntimeModel.getInstance().getProject().getServiceMissingTypes().size();
                    for(;;) {
                        System.out.println("[.] load DTs, count of dependency types: "+dependencyCount);
                        System.out.println("[.] load DTs, count of missing    types: "+missingTypeCount);
                        AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().importDatatypes(data, RuntimeModel.getInstance().getProject().getServiceMissingTypes());
                        RuntimeModel.getInstance().getProject().determineAllDependecyTypes();
                        if(RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size() > dependencyCount) {
                            dependencyCount = RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size();
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            else {
                AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().importDatatypes(data);
                RuntimeModel.getInstance().getProject().determineAllDependecyTypes();
            }                        
            
            //AbstractDatatypeKB.getInstance().printRegisteredDatatypes();
            if(_listenerRef != null) {
                lock.lock(); 
                RuntimeModel.getInstance().setStatusAndNotify("RUNTIME", RuntimeModel.DATATYPE_MODEL_CHANGED, "Imported from: "+this._file.getName());
                _listenerRef.updateDatatypeList();
                lock.unlock(); 
            }
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
    public static void main(String[] ars) {
        File f = new File("D:\\tmp\\KB\\KB_Student-MAP.xml");
        AbstractDatatypeLoadingThread t = new AbstractDatatypeLoadingThread(f);
        t.start();
    }
}
