/*
 * ProjectServiceDependencyTypesTableModel.java
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

import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;
import de.dfki.dmas.owls2wsdl.core.Project;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Oliver
 */
public class ProjectServiceDependencyTypesTableModel extends AbstractTableModel
{   
    public Object getValueAt(int rowIndex, int columnIndex) 
    {
        String uri = RuntimeModel.getInstance().getProject().getServiceDependencyTypes().get(rowIndex).toString();
        
        switch(columnIndex) {
            case 0:
                return Integer.toString(rowIndex);
            case 1:
                return uri;
            case 2:
                if(RuntimeModel.getInstance().getProject().getServiceMissingTypes().contains(uri))
                    return "yes";
                else
                    return "no";                
            default:
                break;
        }
        return "blub";
    }

    public int getRowCount() {
        if(RuntimeModel.getInstance().getProject() != null) {
            return RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size();
        }
        else {
            return 0;
        }
    }

    public int getColumnCount() {
        return 3;
    }
    
}
