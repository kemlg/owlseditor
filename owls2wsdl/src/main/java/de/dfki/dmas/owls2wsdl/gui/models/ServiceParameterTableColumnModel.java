/*
 * ServiceParameterTableColumnModel.java
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

import javax.swing.table.*;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Oliver Fourman
 */
public class ServiceParameterTableColumnModel extends DefaultTableColumnModel
{
    public static final String[] COLHEADS = {"Direction", "Pos", "ID", "Type", "in KB", "Valid"};
    
    public static final int[] COLHEADWIDTHS = {
       50, 10, 100, 310, 40, 40 };
    
    public ServiceParameterTableColumnModel() {
        super();
        for(int i=0; i<COLHEADS.length; ++i) {
            TableColumn col = new TableColumn(i, COLHEADWIDTHS[i]);
            col.setHeaderValue(COLHEADS[i]);
            addColumn(col);
        }
        
        this.setColumnSelectionAllowed(false);
        
//        columnModel.addColumnModelListener( new TableColumnModelListener() {
//            public void columnSelectionChanged(ListSelectionEvent e) { }
//            public void columnMarginChanged(ChangeEvent e) { 
//                System.out.println("COLUMN WIDTH  : "+
//                        columnModel.getColumn(0).getWidth()+" "+
//                        columnModel.getColumn(1).getWidth()+" "+
//                        columnModel.getColumn(2).getWidth()+" "+
//                        columnModel.getColumn(3).getWidth()+" "+
//                        columnModel.getColumn(4).getWidth());
//            }
//            public void columnRemoved(TableColumnModelEvent e) { }
//            public void columnAdded(TableColumnModelEvent e) { }
//            public void columnMoved(TableColumnModelEvent e) { }
//        });
        
    }
}
