/*
 * ServiceListModel.java
 *
 * Created on 21. September 2006, 23:52
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

package de.dfki.dmas.owls2wsdl.gui.models;

import de.dfki.dmas.owls2wsdl.gui.*;
import javax.swing.AbstractListModel;
import java.util.Vector;
import de.dfki.dmas.owls2wsdl.core.AbstractService;
import de.dfki.dmas.owls2wsdl.core.Project;

/**
 *
 * @author Oliver Fourman
 */
public class ServiceListModel extends AbstractListModel {
    
    //private AbstractServiceCollection serviceCollection;
    
    private Project projectRef;
    private Vector services;
    
    /** Creates a new instance of ServiceListModel */
    public ServiceListModel() {
        System.out.println("[C] ServiceListModel");
        this.services = new Vector();
    }
    
    public ServiceListModel(Project project) {
        if(project != null) {
            this.services = project.getAbstractServiceCollection().getServiceCollection();
        }
        else {
            this.services = new Vector();
        }
    }
    
//    public void addToModel(String path) throws java.lang.Exception {
//        RuntimeModel.getInstance().getProject().importServices(path);
//        this.serviceCollection = RuntimeModel.getInstance().getProject().getAbstractServiceCollection();
//        this.serviceCollection.sortData();
//    }
    
    public void syncWithProject(Project project) {
        this.services = project.getAbstractServiceCollection().getServiceCollection();
        
        //this.serviceCollection = RuntimeModel.getInstance().getProject().getAbstractServiceCollection();
        //this.serviceCollection.sortData();
    }
    
    public void updateModel() {
        this.services = RuntimeModel.getInstance().getProject().getAbstractServiceCollection().getServiceCollection();
    }
    
//    public void setModel(AbstractServiceCollection curServiceCollection) {
//        this.serviceCollection = curServiceCollection;
//    }
    
    public void addAbstractService(AbstractService aService) {
        this.services.addElement(aService);
    }
    
    public Object getElementAt(int i) {
        //return RuntimeModel.getInstance().project.getAbstractServiceCollection().getAbstractService(i).getName();
        return ((AbstractService)this.services.get(i)).getName();
    }    
        
    public AbstractService getAbstractServiceAt(int i) {
        return (AbstractService)this.services.get(i);
    }
    
    public Vector getServiceData() {
        return this.services;
    }

    public int getSize() {
        return this.services.size();
    }   
}