/*
 * RegisteredDatatypeElementTableColumnModel.java
 *
 * Created on 18. Januar 2007, 15:12
 * inserted in sub-package models
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

public class RegisteredDatatypeElementTableColumnModel extends DefaultTableColumnModel
{
    public static final String[] COLHEADS = {
        "Restriction", "Values" };
    
    public static final int[] COLHEADWIDTHS = {
        90, 398 };
    
    public RegisteredDatatypeElementTableColumnModel() {
        super();
        for(int i=0; i<COLHEADS.length; ++i) {
            TableColumn col = new TableColumn(i, COLHEADWIDTHS[i]);
            col.setHeaderValue(COLHEADS[i]);
            addColumn(col);
        }
    }
}