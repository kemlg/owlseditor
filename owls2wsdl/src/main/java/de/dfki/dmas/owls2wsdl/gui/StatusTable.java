/*
 * StatusTable.java
 *
 * Created on 11. Dezember 2006, 13:46
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

import java.util.Enumeration;
import java.util.Observable;
import javax.swing.*;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeKB;

/**
 *
 * @author Oliver Fourman
 */
public class StatusTable extends JTable implements java.util.Observer {
    
    private static String[] columnNames = {"Key", "Count", "Valid"};
    private static Object[][] data = { 
                {"Services 1.0", new Integer(0), new Integer(0) },
                {"Services 1.1", new Integer(0), new Integer(0) },
                {"Metatypes (owl:class)", new Integer(0), new Integer(0) },
                {"Metatypes (anonymous)", new Integer(0), "" } };
    
    /** Creates a new instance of StatusTable */
    public StatusTable() {
        super(data,columnNames);
        this.setTableHeader(null);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        this.getColumnModel().getColumn(0).setPreferredWidth(130);
        this.getColumnModel().getColumn(1).setPreferredWidth(25);
        this.getColumnModel().getColumn(2).setPreferredWidth(25);
        
        this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setEnabled(false);
        
        //AbstractDatatypeKB.getInstance().addObserver(this);
        RuntimeModel.getInstance().addObserver(this);
        
        /* not initialized 
        this.getModel().setValueAt(new Integer(AbstractDatatypeKB.getInstance().data.getRegisteredDatatypes().size()), 2, 2);
        this.getModel().setValueAt(new Integer(RuntimeModel.getInstance().getStatus("10SERVICES")), 0, 2);
        this.getModel().setValueAt(new Integer(RuntimeModel.getInstance().getStatus("11SERVICES")), 1, 2);
         **/
    }    
    
    public void update(Observable observable, Object object) 
    {        
        if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SERVICE_MODEL_CHANGED)) {
            System.out.println("[u] Update StatusTable");
            int[] c = RuntimeModel.getInstance().getProject().getAbstractServiceCollection().getTranslateableCount();            
            int service10Count = RuntimeModel.getInstance().getProject().getAbstractServiceCollection().getCount10();
            int service11Count = RuntimeModel.getInstance().getProject().getAbstractServiceCollection().getCount11();
            
            this.getModel().setValueAt(Integer.valueOf(service10Count), 0, 1);
            this.getModel().setValueAt(Integer.valueOf(service11Count), 1, 1);
            
            this.getModel().setValueAt(new Integer(c[0]), 0, 2);
            this.getModel().setValueAt(new Integer(c[1]), 1, 2);
        }
        else if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.DATATYPE_MODEL_CHANGED)) {
            System.out.println("[u] Update StatusTable");
            int[] c = RuntimeModel.getInstance().getProject().getAbstractServiceCollection().getTranslateableCount();
            
            this.getModel().setValueAt(new Integer(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getRegisteredDatatypes().size()), 2, 1);
            this.getModel().setValueAt(new Integer(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getErroneousDatatypes().size()), 2, 2);
            this.getModel().setValueAt(new Integer(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getMetaDatatypes().size()), 3, 1);
            
            this.getModel().setValueAt(new Integer(c[0]), 0, 2);
            this.getModel().setValueAt(new Integer(c[1]), 1, 2);
        }
    }
    
}
