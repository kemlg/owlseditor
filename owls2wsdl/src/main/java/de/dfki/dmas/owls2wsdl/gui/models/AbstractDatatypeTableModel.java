/*
 * AbstractDatatyeTableModel.java
 *
 * Created on 18. Januar 2007, 15:45
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

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import java.util.Vector;
import java.util.Collections;
import java.util.Iterator;
import java.util.Comparator;
import de.dfki.dmas.owls2wsdl.core.*;
import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;

/**
 *
 * @author Oliver Fourman
 */
public class AbstractDatatypeTableModel extends AbstractTableModel  {
    
    private Vector data;
    
    /** Creates a new instance of AbstractDatatyeTableModel */
    public AbstractDatatypeTableModel() {
        super();
        this.data = new Vector();
    }        
    
    public Object getValueAt(int rowIndex, int columnIndex) {        
        if(rowIndex < this.getRowCount())
        {
            AbstractDatatypeElement elem = (AbstractDatatypeElement)this.data.get(rowIndex);
            
            boolean isDatatypeElem = elem.getOwlSource().equals("DATATYPE") || elem.getOwlSource().equals("RESTRICTION-ON-DATATYPE");
            boolean isObjectElem = elem.getOwlSource().equals("OBJECT") || elem.getOwlSource().equals("RESTRICTION-ON-OBJECT");                
            int curDepth = RuntimeModel.getInstance().getProject().getElementDepth();
            
            switch (columnIndex) {
                case 0: return elem.getLocalName();
                case 1: if(elem.getOwlSource().equals("OBJECT")) return "OBJ";
                        else if(elem.getOwlSource().equals("RESTRICTION-ON-OBJECT")) return "OBJ(R)";
                        else if(elem.getOwlSource().equals("DATATYPE")) return "DAT";
                        else if(elem.getOwlSource().equals("RESTRICTION-ON-DATATYPE")) return "DAT(R)";
                        else return elem.getOwlSource();
                case 2: return elem.getLocalTypeName();
                case 3: return elem.getInheritedByLocalName();
                case 4:
                    String defaultXsdType = RuntimeModel.getInstance().getProject().getDefaultXsdType();
                    String inheritanceBehaviour = RuntimeModel.getInstance().getProject().getTypeInheritanceBehaviour();
                    if(isObjectElem) 
                    {                        
                        // -- in case object property has primitive type
                        // -- when no type information was found a anyType was set
                        if(elem.isPrimitive()) {
                            return elem.getLocalTypeName();
                        }
                        else if(elem.getType().equals("null")) {
// In Eigenschaften Defaultwert für Thing (future work)
//                            String xsdtypeString = "anyType";
//                            if(defaultXsdType.contains("#")) {
//                                xsdtypeString = defaultXsdType.substring(defaultXsdType.indexOf("#")+1);
//                            }
//                            return xsdtypeString;
                            return "anyType";
                        }
                        else {
                            if(!AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(elem.getType())) {
                                return "?";
                            }
                            AbstractDatatype atype = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(elem.getType());
                            if(atype.getProperties(curDepth).isEmpty() && atype.getXsdType() != null) {
                                String xsdtype = atype.getXsdType();
                                if(xsdtype.contains("#")) {
                                    xsdtype = xsdtype.substring(xsdtype.indexOf("#")+1);
                                }
                                return xsdtype;
                            }
                            else if(atype.getProperties(curDepth).isEmpty() && atype.getXsdType() == null) {                                                                
                                String xsdtype = atype.determineXsdType(inheritanceBehaviour, defaultXsdType);
                                if(xsdtype.contains("#")) {
                                    xsdtype = xsdtype.substring(xsdtype.indexOf("#")+1);
                                }
                                
                                if(elem.getRestrictedRange().isEmpty()) {                                
                                    if(atype.getIndividualRange().isEmpty()) {
                                        return xsdtype;
                                    }
                                    else {
                                        return "S1("+xsdtype+")";
                                    }
                                }
                                else {
                                    return "S2("+xsdtype+")";
                                }
                            }
                            else {
                                return "C";
                            }
                        }
                    }
                    else if(isDatatypeElem) 
                    {
                        if(elem.isPrimitive()) {
                            return elem.getLocalTypeName();
                        }
                        else {
                            if(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(elem.getType())) {
                                AbstractDatatype atype = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(elem.getType());
                                if(atype.getProperties(curDepth).isEmpty() && atype.getXsdType() != null) {
                                    String xsdtype = atype.getXsdType();
                                    if(xsdtype.contains("#")) {
                                        xsdtype = xsdtype.substring(xsdtype.indexOf("#")+1);
                                    }
                                    return xsdtype;
                                }
                                else if(atype.getProperties(curDepth).isEmpty() && atype.getXsdType() == null) 
                                {
                                    String xsdtype = atype.determineXsdType(inheritanceBehaviour, defaultXsdType);
                                    if(xsdtype.contains("#")) {
                                        xsdtype = xsdtype.substring(xsdtype.indexOf("#")+1);
                                    }
                                    
                                    if(elem.getRestrictedRange().isEmpty()) {                                
                                        if(atype.getIndividualRange().isEmpty()) {
                                            return xsdtype;
                                        }
                                        else {
                                            return "S1("+xsdtype+")";
                                        }
                                    }
                                    else {
                                        return "S2("+xsdtype+")";
                                    }
                                }
                                else {
                                    return "C";
                                }
                            }
                            else {
                                return "!KB";
                            }
                        }
                    }
                    return "D";
                case 5: return new Integer(elem.getLevel());
                case 6: 
                    if(elem.getLevel() <= curDepth) {
                        return "Y";
                    }
                    else {
                        return "N";
                    }
            }
        }
        return "?";
    }

    public int getRowCount() {
        return this.data.size();
    }

    public int getColumnCount() {
        return 6;
    }
    
    public AbstractDatatypeElement getAbstractDatatypeElementAt(int i) {
        return (AbstractDatatypeElement)this.data.get(i);
    }
    
    public void updateModel(AbstractDatatype datatype) {
        this.data.removeAllElements();
        Iterator it = datatype.getProperties().iterator();
        while(it.hasNext()) {
            AbstractDatatypeElement elem = (AbstractDatatypeElement)it.next();            
            if(!elem.getOwlSource().equals("META")) {
                this.data.add(elem);
            }
        }        

        Collections.sort(this.data, new AbstractDatatypeElementComparer() );        
        System.out.println("[i] AbstractDatatypeTableModel, Count  Elements: "+this.data.size());
        //this.fireTableDataChanged();
    }   
}
