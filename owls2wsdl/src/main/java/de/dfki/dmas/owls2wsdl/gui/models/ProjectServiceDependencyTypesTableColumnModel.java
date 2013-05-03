/*
 * ProjectServiceDependencyTypesTableColumnModel.java
 *
 * Created on 21. März 2007, 16:38
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

/**
 *
 * @author Oliver
 */
public class ProjectServiceDependencyTypesTableColumnModel extends DefaultTableColumnModel
{
    public static final String[] COLHEADS = {
        "#", "URI of type", "missing?" };
    
    public static final int[] COLHEADWIDTHS = {
        50, 400, 50  };    
    
    /** Creates a new instance of ProjectServiceDependencyTypesTableColumnModel */
    public ProjectServiceDependencyTypesTableColumnModel() {
                super();
        for(int i=0; i<COLHEADS.length; ++i) {
            TableColumn col = new TableColumn(i, COLHEADWIDTHS[i]);
            col.setHeaderValue(COLHEADS[i]);
            addColumn(col);
        }
    }
    
}
