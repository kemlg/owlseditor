/*
 * RuntimeModel.java
 *
 * Created on 4. Dezember 2006, 11:37
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

package de.dfki.dmas.owls2wsdl.gui;
import java.util.Observable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import de.dfki.dmas.owls2wsdl.core.AbstractService;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatype;
import de.dfki.dmas.owls2wsdl.core.Project;

/**
 * Singleton Observable Model
 * descripes GUI status: for SERVICE_VIEW and DATATYPE_VIEW
 * @author Oliver Fourman
 */
public class RuntimeModel extends Observable {
    
    // Main RunTime-Views
    public final static String SERVICE_VIEW  = "SERVICE view";
    public final static String DATATYPE_VIEW = "DATATYPE view";
    
    // RunTime stats
    public final static String MODELS_INITIATED           = "persistent model information loaded.";
    public final static String SERVICE_MODEL_CHANGED      = "service model changed.";
    public final static String DATATYPE_MODEL_CHANGED     = "datatype model changed.";
    
    public final static String SINGLE_SERVICE_SELECTED    = "service selection in list changed";
    public final static String MULTIPLE_SERVICES_SELECTED = "multiple services in list selected for batch routine";
    public final static String SINGLE_DATATYPE_SELECTED   = "datatype selection in list changed";
    
    public final static String PARAMETER_SELECTED         = "show datatype for parameter";
    
    public final static String PROPERTIES_CHANGED         = "properties in property frame saved";
    
    public final static String PROJECT_LOADED             = "project file loaded.";
    
    /** Project includes all services, datatypes and project-configurations */
    protected Project project;
    
    /** Status components (GUI, Project) registry */    
    private HashMap status;
    
    /** Singleton pattern */
    private static RuntimeModel instance = null;
    
    /** Creates a new instance of GuiStatusModel */
    private RuntimeModel() {
        System.out.println("[C] construct RuntimeModel (singleton)");
        
        this.project = null;
        this.status  = new HashMap();
        
        this.setStatus("VIEW",    RuntimeModel.SERVICE_VIEW);
        this.setStatus("RUNTIME", RuntimeModel.MODELS_INITIATED);
        
        this.status.put("OPENED_OWLS_FILES", new ArrayList());
        this.status.put("OPENED_OWL_FILES", new ArrayList());                
    }
    
    /** get single instance of model */
    public final static synchronized RuntimeModel getInstance()
    {
        if(instance==null) {
            instance = new RuntimeModel();
        }
        return instance;
    }
        
    public void setProject(Project p) {
        this.project = p;
    }
    public Project getProject() {
        return this.project;
    }

    
    public boolean hasStatus(String key) {
        return this.status.containsKey(key);
    }    
    /**
     * set status entries
     * @param String status key
     * @param String status value
     */
    public void setStatus(String key, String value) {
        this.status.put(key, value);
    }    
    /**
     * set status entries
     * @param String status key
     * @param String status value
     */
    public void setStatus(String key, Object value) {
        this.status.put(key, value);
    }    
    /**
     * set status entries and inform registered observers when runtime changes
     * @param String status key
     * @param String status value
     */
    public void setStatusAndNotify(String key, String value) {        
        this.status.put(key, value);
        this.setChanged();
        this.notifyObservers();
    }    
    public void setStatusAndNotify(String key, String value, String msg) {        
        this.status.put(key, value);
        this.setChanged();
        this.notifyObservers(msg);
    }
    
    public String getStatus(String key) {
        if(this.status.containsKey(key)) {
            return this.status.get(key).toString();
        }
        return null;
    }
    
    public void setRuntimeAndNotify(String runtimeValue) {
        this.status.put("RUNTIME", runtimeValue);
        this.setChanged();
        this.notifyObservers();
    }
    
    public void setRuntimeAndNotify(String runtimeValue, String msg) {
        this.status.put("RUNTIME", runtimeValue);
        this.setChanged();
        this.notifyObservers(msg);
    }
    
    public String getRuntime() {
        return this.status.get("RUNTIME").toString();
    }
    
    public Vector getOWLSURLDialogItems() {
        return (Vector)this.status.get("OWLS_URLDialogItems");
    }
    
    public Vector getOWLURLDialogItems() {
        return (Vector)this.status.get("OWL_URLDialogItems");
    }
    
    public void setSelectedService(AbstractService service) {
        this.status.put("SELECTED_SERVICE", service);
        this.setChanged();
        this.notifyObservers();
    }
   
    public void unsetSelectedService() {
        this.status.remove("SELECTED_SERVICE");
    }
    
    public AbstractService getSelectedService() {
        //System.out.println("RuntimeModel: getSelectedService");
        if(this.status.containsKey("SELECTED_SERVICE") ) //|| this.status.containsKey(RuntimeModel.SINGLE_SERVICE_SELECTED))
            return (AbstractService)this.status.get("SELECTED_SERVICE");
        else
            return null;
    }
        
    public void setSelectedDatatype(AbstractDatatype type) {
        this.status.put("SELECTED_DATATYPE", type);
        this.setChanged();
        this.notifyObservers();
    }
    
    public void unsetSelectedDatatype() {
        this.status.remove("SELECTED_DATATYPE");
    }
    
    public AbstractDatatype getSelectedDatatype() {
        if(this.status.containsKey("SELECTED_DATATYPE"))
            return (AbstractDatatype)this.status.get("SELECTED_DATATYPE");
        else
            return null;
    }
    
    public void printStatus() {       
        String tempkey;
        Iterator it = status.keySet().iterator();
        while(it.hasNext()) {
            tempkey = it.next().toString();
            System.out.println("[RuntimeModel] "+tempkey+"=("+status.get(tempkey).toString()+")");
        }
    }
}
