/*
 * AbstractDatatypeMetaElementListModel.java
 *
 * Created on 18. Dezember 2006, 16:01
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

import de.dfki.dmas.owls2wsdl.core.*;
import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;
import java.util.Vector;
import java.util.Collections;
import java.util.Iterator;
import java.util.Comparator;

/**
 *
 * @author Oliver Fourman
 */
public class AbstractDatatypeMetaElementListModel extends AbstractListModel {
    
    private Vector data;
    
    /** Creates a new instance of AbstractDatatypeMetaElementListModel */
    public AbstractDatatypeMetaElementListModel() {
        this.data = new Vector();
    }

    public Object getElementAt(int i) {
        return this.data.get(i);
    }

    public int getSize() {
        return this.data.size();
    }
        
    public void updateModel(AbstractDatatype datatype) {
        this.data.removeAllElements();
        
        Iterator it = datatype.getProperties().iterator();
        while(it.hasNext()) {
            AbstractDatatypeElement elem = (AbstractDatatypeElement)it.next();            
            if(elem.getOwlSource().equals("UNION") || elem.getOwlSource().equals("INTERSECTION")) {
                this.data.add(elem);
            }
        }
                 
        Collections.sort(this.data, new AbstractDatatypeElementComparer() );        
        System.out.println("Count Meta-Elements: "+this.data.size());
        this.fireContentsChanged(this, 0, this.data.size());
    }   
}