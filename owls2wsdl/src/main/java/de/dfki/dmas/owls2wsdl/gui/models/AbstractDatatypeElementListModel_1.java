/*
 * AbstractDatatypeElementListModel.java
 *
 * Created on 28. November 2006, 15:45
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
public class AbstractDatatypeElementListModel_1 extends AbstractListModel {
    
    private Vector registeredDatatypeElements;
    private Vector registeredDatatypeMetaElements;
    private boolean RETURNMETADATA = false;
    
    /** Creates a new instance of AbstractDatatypeElementListModel */
    public AbstractDatatypeElementListModel_1() {
        this.registeredDatatypeElements = new Vector();
        this.registeredDatatypeMetaElements = new Vector();
    }    
    
    public void getMetaData(boolean val) {
        this.RETURNMETADATA = val;
    }    
    
    public Object getElementAt(int i) {
        if(RETURNMETADATA) {
            return ((AbstractDatatypeElement)this.registeredDatatypeMetaElements.get(i)).getName();
        }
        else {
            return ((AbstractDatatypeElement)this.registeredDatatypeElements.get(i)).getName();
        }
    }
    
    public AbstractDatatypeElement getAbstractDatatypeElementAt(int i) {
//        if(RETURNMETADATA) {
//            return (AbstractDatatypeElement)this.registeredDatatypeMetaElements.get(i);
//        }
//        else {
            return (AbstractDatatypeElement)this.registeredDatatypeElements.get(i);
//        }
    }

    public int getSize() {
        if(RETURNMETADATA) {
            return this.registeredDatatypeMetaElements.size();
        }
        else {
            return this.registeredDatatypeElements.size();
        }
    }
    
    
    public void updateModel(AbstractDatatype datatype) {
        this.registeredDatatypeElements.removeAllElements();
        this.registeredDatatypeMetaElements.removeAllElements();
        
        Iterator it = datatype.getProperties().iterator();
        while(it.hasNext()) {
            AbstractDatatypeElement elem = (AbstractDatatypeElement)it.next();            
            if(elem.getOwlSource().equals("META")) {
                this.registeredDatatypeMetaElements.add(elem);
            }
            else {
                this.registeredDatatypeElements.add(elem);
            }            
        }
                
        Collections.sort(this.registeredDatatypeElements, new AbstractDatatypeElementComparer_1() );
        Collections.sort(this.registeredDatatypeMetaElements, new AbstractDatatypeElementComparer_1() );
        
        System.out.println("Count      Elements: "+this.registeredDatatypeElements.size());
        System.out.println("Count Meta-Elements: "+this.registeredDatatypeMetaElements.size());
        System.out.println("       CURRENT MODE: "+this.RETURNMETADATA);
        this.fireContentsChanged(this, 0, this.registeredDatatypeElements.size());
    }
}


class AbstractDatatypeElementComparer_1 implements Comparator {
        public int compare(Object obj1, Object obj2)
        {
                String s1 = ((AbstractDatatypeElement)obj1).getName();
                String s2 = ((AbstractDatatypeElement)obj2).getName();
                return s1.compareTo(s2);
        }
}
