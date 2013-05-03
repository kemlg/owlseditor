/*
 * RegisteredDatatypeElementTableModel.java
 *
 * Created on 28. September 2006, 12:16
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
import de.dfki.dmas.owls2wsdl.core.*;

import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeElement;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeRestrictionElement;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;

/**
 *
 * @author Oliver Fourman
 */
public class RegisteredDatatypeElementTableModel extends AbstractTableModel {
    
    AbstractDatatypeElement _elem = null;
    Vector _collectedRestrictionValues;
    
     // COLUMNS (Element): RestrictionType | Value
    private static final int COLUMN_COUNT = 2;
    
    /** Creates a new instance of RegisteredDatatypeElementTableModel */
    public RegisteredDatatypeElementTableModel() {
        this._collectedRestrictionValues = new Vector();
    }
    
    public void removeAllElements() {
        this._collectedRestrictionValues.removeAllElements();
    }
    
    public void updateModel(AbstractDatatypeElement elem) {
        this._collectedRestrictionValues.removeAllElements();
        if(!elem.getRestrictions().isEmpty()) {
            this._collectedRestrictionValues.addAll(elem.getRestrictions());
        }
        else {
            System.out.println("[i] RegisteredDatatypeElementTableModel: update with elem.getType: "+elem.getType());
            if(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(elem.getType())) {
                if(elem.getOwlSource().equals("OBJECT") || elem.getOwlSource().equals("RESTRICTION-ON-PROJECT")) {
                    Set individualRangeSet = AbstractDatatypeKB.getInstance().data.get(elem.getType()).getIndividualRange().keySet();
                    if(individualRangeSet.isEmpty()) {
                        System.out.println("[i] individualRangeSet is empty.");
                    }
                    else {
                        for(Iterator it=individualRangeSet.iterator(); it.hasNext(); ) {
                            String rangeURI = it.next().toString();
                            String rangeType = AbstractDatatypeKB.getInstance().data.get(elem.getType()).getIndividualRange().get(rangeURI).toString();
                            this._collectedRestrictionValues.add(new AbstractDatatypeRestrictionElement(rangeType, rangeURI, 0, null));
                        }
                    }
                }
            }
            else {
                System.out.println("[e] "+elem.getType()+" not in KB. No more inforamtion about element range available.");
            }
        }
//        this.fireTableDataChanged();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {        
        if(columnIndex==0)
            return ((AbstractDatatypeRestrictionElement)this._collectedRestrictionValues.get(rowIndex)).getRestrictionType();
        else
            return ((AbstractDatatypeRestrictionElement)this._collectedRestrictionValues.get(rowIndex)).getRestrictionValue();
    }

    public int getRowCount() {
        return this._collectedRestrictionValues.size();
    }

    public int getColumnCount() {
        return this.COLUMN_COUNT;
    }
    
    public static void main(String[] args) {
        System.out.println("TEST RegisteredDatatypeElementTableModel");
        AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_Wine.xml");
        AbstractDatatype curtype = AbstractDatatypeKB.getInstance().data.get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhiteWine");
        //curtype.printDatatype();
                
//        AbstractDatatypeElement testElem = (AbstractDatatypeElement)curtype.getProperties().get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#hasColor");
//        testElem.printData();
//        
//        RegisteredDatatypeElementTableModel model = new RegisteredDatatypeElementTableModel();
//        model.updateModel(testElem);
//        System.out.println("CONTENTS:");
//        for(int i=0; i<model.getRowCount(); i++) {
//            System.out.println(model.getValueAt(i,0)+" : "+model.getValueAt(i,1));
//        }
    }
    
}
