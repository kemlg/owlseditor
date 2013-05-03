package de.dfki.dmas.owls2wsdl.parser;
/*
 * OntClassKB.java
 *
 * Created on 18. August 2006, 15:43
 */


import de.dfki.dmas.owls2wsdl.core.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;

import java.io.OutputStream;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.rdf.model.*;
import org.mindswap.pellet.jena.PelletReasoner;

/**
 *
 * @author Oliver Fourman
 */
public class OntClassKB {
    
    private Vector  ontologyURIs;        // current ontologyURI and importedOntologyURIs
    private HashMap registeredDatatypes; // OntClassContainer    
    private HashMap erroneousDatatypes;
    private HashMap disjoints;
    private Vector  collectedClassesWithIndividuals;
        
    private OntModel pelletModel;
    private static OWLHelper helper = new OWLHelper();
    
    /** Creates a new instance of DatatypeKB */
    public OntClassKB(OntModel model) {
        this.ontologyURIs = new Vector();
        this.registeredDatatypes  = new HashMap();  // uri - container objects
        this.erroneousDatatypes   = new HashMap();  // uri - exception msges
        this.disjoints            = new HashMap();
        this.collectedClassesWithIndividuals = new Vector();
        this.pelletModel = model;
    }
    
    public Vector  getOntologyURIs() { return this.ontologyURIs; }
    public HashMap getRegisteredDatatypes() { return this.registeredDatatypes; }
    public HashMap getErroneousDatatype() { return this.erroneousDatatypes; }
    public HashMap getDisjoints() { return this.disjoints; }
    public Vector  getCollectedClassesWithIndividuals() { return this.collectedClassesWithIndividuals; }
    public Vector  getIndividuals(String ontClassURI) { 
        return this.get(ontClassURI).getIndividualList();
    }
    
    public void addOntologyURI(String uri) {
        this.ontologyURIs.add(uri);
    }
        
    public boolean containsKey(String key) {
        System.out.println("[i] check registeredDatatypes key: "+key);
        return this.registeredDatatypes.containsKey(key);
    }
    
    public void addDatatype(OntClassContainer container) {
        this.registeredDatatypes.put(container.getName(), container);
    }
    
    /**
     * Parmeter is the whole exception, but only the exception msg is saved,
     * not the stacktrace. if stacktrace saved, we're pumping the persistent
     * the datatype part in the project file with the stacktrace messages.
     * 5.2.2007: changed back to Exception message (String) only
     */
    public void addError(String uri, String msg) {
        System.out.println("[i] ADD error message for "+uri);
        System.out.println("    MSG: "+msg);
        if(this.erroneousDatatypes.containsKey(uri)) {
            Vector exceptions = (Vector)this.erroneousDatatypes.get(uri);
            boolean new_msg = true;
            for(int i=0; i<exceptions.size(); i++) {
                //if( ((Exception)exceptions.get(i)).toString().equals(exception_msg) ) {
                if(exceptions.get(i).toString().equals(msg)) {
                    new_msg = false;
                    System.out.println("NEW MSG");
                }
            }
            if(new_msg) {
                ((Vector)this.erroneousDatatypes.get(uri)).add(msg);
                System.out.println("ADD NEW ERROR MESSAGE: "+msg);
            }
        }
        else {
            Vector exceptions = new Vector();
            exceptions.add(msg);
            this.erroneousDatatypes.put(uri, exceptions);
            System.out.println("ADD NEW VECTOR AND ERROR MESSAGE: "+msg);
        }        
    }
    public Vector getErrorList(String uri) {
        return (Vector)this.erroneousDatatypes.get(uri);
    }
    
    public OntClassContainer get(String key) {
        return (OntClassContainer)this.registeredDatatypes.get(key);
    }
    
    public void putDisjoint(String uri1, String uri2) {
        this.disjoints.put(uri1, uri2);
        this.disjoints.put(uri2, uri1);
        System.out.println("DISJOINT REGISTERED: "+uri1+":"+uri2);
        System.out.println("DISJOINT REGISTERED: "+uri2+":"+uri1);
    }
    
    public boolean isDisjoint(String uri1, String uri2) {
        if(this.disjoints.containsKey(uri1)) {
            return this.disjoints.get(uri1).equals(uri2);
        }
        else
            return false;
    }
    
    public void addCollectedClassWithIndividuals(String uri) {
        this.collectedClassesWithIndividuals.add(uri);
    }
    
//    public void showDatatype(String urlOWLClass) 
//    {
//        if(this.registeredDatatypes.containsKey(urlOWLClass)) {
//            System.out.println("[showData] ENTHALTEN: "+urlOWLClass);
//            OntClassContainer curOntContainer = (OntClassContainer)this.registeredDatatypes.get(urlOWLClass);
//            curOntContainer.showData(this); // refernce for dependencies
//        }
//        else {
//            System.out.println("[showData] NICHT REGISTRIERT: "+urlOWLClass);
//        }
//    }
    
    public void showAllRegisteredDatatypes() {
        ArrayList keys = new ArrayList( this.registeredDatatypes.keySet() );
        //Collections.sort(keys);
        for(int i=0; i<keys.size(); i++)
        {
            try {
                System.out.println("[registered] ["+i+"] "+keys.get(i));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION : "+e.getMessage());
                continue;
            }
        }        
//        ArrayList values = new ArrayList( this.registeredDatatypes.values() );
//        for(int i=0; i<values.size(); i++)
//        {
//            try {
//                System.out.println("[registered2] "+i+", "+values.get(i));
//            }
//            catch(Exception e) {
//                System.out.println("EXCEPTION : "+e.getMessage());
//                continue;
//            }
//        }
    }
    
    public void showRegisteredDatatypeFull(String key) {
        System.out.println();
        OntClassContainer curOntContainer = (OntClassContainer)this.registeredDatatypes.get(key);
        System.out.println("[OntClassContainer] KEY: "+key);
        //System.out.println("VALUE (OWL/RDFS Class): "+curOntContainer);
        curOntContainer.showData();
        
//        System.out.println("================================================================================");
//        System.out.println("LOOKUP INTERSECTION CLASSES:");            
//        Vector temp = this.buildIntersection(curOntContainer.getName());
//        for(int i=0; i<temp.size(); i++) {
//            System.out.println(i+".) "+temp.get(i).toString());
//            this.get(temp.get(i).toString()).showData();
//        }
//        System.out.println("================================================================================");
        
    }
    
    public void showAllRegisteredDatatypesFull() 
    {          
        ArrayList keys = new ArrayList( this.registeredDatatypes.keySet() );
        Collections.sort(keys);
        
        OntClassContainer curOntContainer = null;  // nur zur Ausgabe
        
        Iterator it = keys.iterator();
        System.out.println("=================================================");
        while(it.hasNext()) 
        {            
            System.out.println();
            String key = it.next().toString();
            if(key == null)
                break;
            
            curOntContainer = (OntClassContainer)this.registeredDatatypes.get(key);
            System.out.println("[OntClassContainer] KEY: "+key);
            //System.out.println("VALUE (OWL/RDFS Class): "+curOntContainer);
            curOntContainer.showData();
        }
    }

    public void showErroneous() {
        ArrayList keys = new ArrayList( this.erroneousDatatypes.keySet() );
        Iterator it = keys.iterator();
        System.out.println("=================================================");
        while(it.hasNext()) 
        {
            String key = it.next().toString();
            Vector temp = (Vector)this.erroneousDatatypes.get(key);
            for(int i=0; i<temp.size(); i++) {
                System.out.println("KEY: "+key+" VAL: "+temp.get(i).toString());
            }            
        }
    }
    
    public void showAllTypesWithIndividuals() {
        System.out.println();
        System.out.println("= Individuals for following types registered ====");
        for(Iterator it = this.collectedClassesWithIndividuals.iterator(); it.hasNext(); ) {
            System.out.println("URI: "+it.next().toString());
        }
    }
    

    private Vector buildSubClassIntersection(String containerUri) 
    {   
        Vector temp = new Vector();        
        if(!this.get(containerUri).getSubClassIntersectionClassSet().isEmpty()) {
            for(Iterator it=this.get(containerUri).getSubClassIntersectionClassSet().iterator(); it.hasNext();) {
                OntClass intSecClass = (OntClass) it.next();
                String uri = intSecClass.getURI();
                if(uri != null) {
                    //recursion
                    Vector temp2 = buildSubClassIntersection(uri);
                    for(int i=0; i<temp2.size(); i++) {
                        if(!temp.contains(temp2.get(i))) {              
    //                        System.out.println("> merge "+temp2.get(i)+" to "+containerUri+" List.");
                            temp.add(temp2.get(i));
                        }
    //                    else {
    //                        System.out.println("ALREADY IN LIST: "+temp2.get(i));
    //                    }
                    }
                    //System.out.println("> addAll I-Classes of "+uri);
                    //temp.addAll(buildIntersection(uri));
                }
            }            
        }
//        else {
//            System.out.println("EMPTY getIntersectionClassSet for "+containerUri);            
//        }
        temp.add(containerUri);
//        System.out.println("CONTENT of "+containerUri+" List: "+temp.toString());
        return temp;            
    }
       
    /**
     * obsolete seit addAbstractDatatypeElementsByIntersectionClass
     */
    private Vector buildEquivalentIntersection(String containerUri) 
    {   
        Vector temp = new Vector();        
        if(!this.get(containerUri).getEquivalentIntersectionSet().isEmpty()) {
            for(Iterator it=this.get(containerUri).getEquivalentIntersectionSet().iterator(); it.hasNext();) {
                OntClass intSecClass = (OntClass) it.next();
                String uri = intSecClass.getURI();
                if(uri != null) {  // d.h. keine Restriction!
                    //recursion
                    Vector temp2 = buildEquivalentIntersection(uri);                
                    for(int i=0; i<temp2.size(); i++) {
                        if(!temp.contains(temp2.get(i))) {              
                            temp.add(temp2.get(i));
                        }
                    }
                }
            }            
        }
        temp.add(containerUri); System.out.println("ADD: "+containerUri);
        return temp;
    }
    
    
    
    /**
     * Obsolete seit addAbstractDatatypeElementsBySuperClass
     * direktes Einpflegen der Elemente anstatt sie über einen Vector zwischenzuspeichern
     * @obsolete
     */
    private Vector buildSuperClassList(String containerUri) {
        System.out.println(">> superClass for "+containerUri);
        Vector temp = new Vector();
        if(this.containsKey(containerUri)) {
            if(!this.get(containerUri).getSuperClasses().isEmpty()) {
                //System.out.println("COUNT: "+this.get(containerUri).getSuperClasses().size());
                for(Iterator it=this.get(containerUri).getSuperClasses().iterator(); it.hasNext(); ) {
                    String uri =  it.next().toString();
                    //recursion
                    Vector temp2 = buildSuperClassList(uri);                
                    for(int i=0; i<temp2.size(); i++) {
                        if(!temp.contains(temp2.get(i))) {
                            temp.add(temp2.get(i));
                        }
                    }
                }
            }
        }
        else {
            System.out.println("[e] Not in KB: "+containerUri);
        }
        temp.add(containerUri);
        return temp;
    }
    
    /**
     * Method takes an initial container uri and does an recursiv search 
     * for owl super classes. For each owl SuperClass the properties are copied
     * as AbstractDatatypeElements to the given AbstractDatatype.
     * The initial AbstractDatatype is referenced. (call by reference for objects)
     */
    private void addAbstractDatatypeElementsBySuperClass(AbstractDatatype atype, String containerUri, int level) 
    {
        if(this.containsKey(containerUri)) {            
            level++;
            if(!this.get(containerUri).getSuperClasses().isEmpty()) {
                System.out.println("[i] superClass count for "+containerUri+": "+this.get(containerUri).getSuperClasses().size());                
                for(Iterator it=this.get(containerUri).getSuperClasses().iterator(); it.hasNext(); ) {
                    String uri =  it.next().toString();
                    System.out.println("[i] build recursion for class "+uri+" and new type "+atype.getUrl());
                    //recursion
                    addAbstractDatatypeElementsBySuperClass(atype, uri, level);                    
                }
            }
            System.out.println("[i] recursion at SuperClass: "+containerUri+" Level "+level);            
            
            // DATATYPE
            for(Iterator it = this.get(containerUri).getDatatypeProperties().iterator(); it.hasNext(); ) {
                DatatypeProperty prop = (DatatypeProperty)it.next();
                
                boolean ADD_PROP_FLAG = true;
                for(ExtendedIterator subp_it=prop.listSubProperties(true); subp_it.hasNext(); ) {
                    System.out.println("SUB_PROP: "+subp_it.next().toString());
                    ADD_PROP_FLAG = false;
                }
                if(ADD_PROP_FLAG && !atype.containsProperty(prop.getURI())) {
                    AbstractDatatypeElement elem = null;
                    if(containerUri.equals(atype.getUrl())) {
                        elem = new AbstractDatatypeElement("DATATYPE", prop.getURI(), helper.getPropertyRange(prop));
                    }
                    else {
                        elem = new AbstractDatatypeElement("DATATYPE", prop.getURI(), helper.getPropertyRange(prop), level, containerUri);
                    }
                    
                    System.out.println("[i] Add elem (dat) "+elem.toString()+" to "+atype.getLocalName());
                    atype.addProperty(elem);
                }
                else if(ADD_PROP_FLAG && atype.containsProperty(prop.getURI())) {
                    System.out.println("[i] Don't need to add "+prop.getLocalName()+" to "+atype.getLocalName()+". Already added.");
                }
                
                //
                // set range, that ist not defined by restrictions, constants (directly defined)
                // cause elements could already set by super classes, the element has to be
                // referenced here to add a range.
                //
                if(this.get(containerUri).getConstantMap().containsKey(prop.getURI())) {
                    atype.getElement(prop.getURI()).addToRestrictedRange(this.get(containerUri).getConstantMap().get(prop.getURI()).toString());
                    System.out.println("[i] ADD CONSTANT for "+prop.getURI()+": "+this.get(containerUri).getConstantMap().get(prop.getURI()).toString()+" at level "+level);
                }                
            }
            
            // OBJECT-TYPE
            for(Iterator it = this.get(containerUri).getObjectProperties().iterator(); it.hasNext(); ) {
                ObjectProperty prop = (ObjectProperty)it.next();
                System.out.println("OBJ P.:"+prop.getURI());
                
                // Descision, if subproperties should be added
                boolean ADD_PROP_FLAG = true;
                for(ExtendedIterator subp_it=prop.listSubProperties(true); subp_it.hasNext(); ) {
                    System.out.println("SUB_PROP: "+subp_it.next().toString());
                    ADD_PROP_FLAG = false;
                }
                
                if(ADD_PROP_FLAG && !atype.containsProperty(prop.getURI())) 
                {
                    // process SuperProperties:
                    if(prop.getRange()==null) {
                        System.out.println("[i] prop.getRange() == null");
                        for(ExtendedIterator superPropIt = prop.listSuperProperties(true); superPropIt.hasNext(); ) {
                            ObjectProperty super_prop = ((OntResource)superPropIt.next()).asObjectProperty();
                            if(super_prop.getRange()!=null) {
                                String info = "Range of "+prop.getLocalName()+" inherited from SuperProperty "+super_prop.getLocalName();
                                atype.addParseMessage(info);
                            }
                        }
                    }
                    
                    if(prop.getRange() == null) {
                        continue;
                    }
                    
                    AbstractDatatypeElement elem = null;
                    if(prop.getRange().asClass().isUnionClass()) {
                        AbstractDatatype metaUnionType = new AbstractDatatype();
                        metaUnionType.setLocalName(prop.getLocalName()+"Type");
                        metaUnionType.setUrl(prop.getURI());
                        
                        UnionClass uc = prop.getRange().asClass().asUnionClass();
                        RDFList clist = uc.getOperands();
                        for(ExtendedIterator ucIt = clist.iterator(); ucIt.hasNext(); ) {
                            RDFNode node = (RDFNode)ucIt.next();
                            if(node.canAs(OntClass.class)) {
                                OntClass unionClass = (OntClass)node.as(OntClass.class);
                                String ucElementName = "elem"+Integer.toString(metaUnionType.getProperties().size()+1);
                                AbstractDatatypeElement ucElem = new AbstractDatatypeElement("UNION-CLASS", ucElementName, unionClass.getURI());
                                metaUnionType.addProperty(ucElem);
                            }
                        }                        
                        AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().addMetaDatatype(metaUnionType);
                        elem = new AbstractDatatypeElement("UNION", prop.getURI(), metaUnionType.getLocalName());
                    }
                    else {                    
                        if(containerUri.equals(atype.getUrl())) {
                            elem = new AbstractDatatypeElement("OBJECT", prop.getURI(), helper.getPropertyRange(prop));
                        }
                        else {
                            elem = new AbstractDatatypeElement("OBJECT", prop.getURI(), helper.getPropertyRange(prop), level, containerUri);
                        }
                    }
                    
                    if(prop.isFunctionalProperty()) {
                        elem.addRestrictionElement(new AbstractDatatypeRestrictionElement("isFunctional", "1", level, containerUri));
                    }
                    if(prop.isInverseFunctionalProperty()) {
                        elem.addRestrictionElement(new AbstractDatatypeRestrictionElement("isInverseFunctional", "1", level, containerUri));
                    }
                    if(prop.isTransitiveProperty()) {
                        elem.addRestrictionElement(new AbstractDatatypeRestrictionElement("isTransitive", "1", level, containerUri));
                    }
                    
                    atype.addProperty(elem);                    
                    System.out.println("[i] Add elem (obj) "+elem.toString()+" to "+atype.getUrl());
                    
//                    if(!prop.isTransitiveProperty()) {
//                        atype.addProperty(elem);
//                    }
//                    else {
//                        System.out.println("   transitive property");
//                    }
                }
                else if(ADD_PROP_FLAG && atype.containsProperty(prop.getURI())) {
                    System.out.println("[i] Don't need to add "+prop.getLocalName()+" to "+atype.getLocalName()+". Already added.");
                }
                
                //
                // set range, that ist not defined by restrictions, constants (directly defined)
                // cause elements could already set by super classes, the element has to be
                // referenced here to add a range.
                //
                if(this.get(containerUri).getConstantMap().containsKey(prop.getURI())) {
                    atype.getElement(prop.getURI()).addToRestrictedRange(this.get(containerUri).getConstantMap().get(prop.getURI()).toString());
                    System.out.println("[i] ADD CONSTANT for "+prop.getURI()+": "+this.get(containerUri).getConstantMap().get(prop.getURI()).toString()+" at level "+level);
                }
            }
            

            //System.out.println("getSubClassPropertyRestrictions: "+this.get(containerUri).getSubClassPropertyRestrictions().size());
            for(int i=0; i<this.get(containerUri).getSubClassPropertyRestrictions().size(); i++) {
                this.addElementRestrictions(atype, level, containerUri, (Restriction)this.get(containerUri).getSubClassPropertyRestrictions().get(i), null);
            }

            //System.out.println("getEquivalentPropertyRestrictions: "+this.get(containerUri).getEquivalentPropertyRestrictions().size());
            for(int i=0; i<this.get(containerUri).getEquivalentPropertyRestrictions().size(); i++) {
                this.addElementRestrictions(atype, level, containerUri, (Restriction)this.get(containerUri).getEquivalentPropertyRestrictions().get(i), null);
            }
        }
        else {
            System.out.println("[e] Not in KB: "+containerUri);
        }
    }
    
    private void addAbstractDatatypeElementsByIntersectionClass(AbstractDatatype atype, String containerUri, int level) 
    {
//        boolean KEYFOUND = this.containsKey(containerUri);
//        boolean EQUALENT = atype.getUrl().equals(containerUri); // Don't need to collect to elements of type itself here, done in addAbstractDatatypeElementsBySuperClass
        
        System.out.println("AbstractType: "+atype.getUrl());
        System.out.println("ContainerUri: "+containerUri);
        
        if(this.containsKey(containerUri)) { 
            level++;
            if(!this.get(containerUri).getEquivalentIntersectionSet().isEmpty()) {
                for(Iterator it=this.get(containerUri).getEquivalentIntersectionSet().iterator(); it.hasNext();) {
                    String uri = ((OntClass)it.next()).getURI();
                    System.out.println("[i] build i-recursion for class "+uri+" and new type "+atype.getUrl());
                    // == Rekursion, wenn es keine Restriction ist, und es sich nicht um den gleichen Typ handelt.
                    if(uri != null) { // && !atype.getUrl().equals(uri)) {
                        addAbstractDatatypeElementsByIntersectionClass(atype, uri, level);
                    }
                    else {
                        System.out.println("[i] restriction, not recursion");
                    }
                }
            }
            System.out.println("[i] recursion at IntersectionClass: "+containerUri+" Level "+level);
            
            // DATATYPE
            for(Iterator it = this.get(containerUri).getDatatypeProperties().iterator(); it.hasNext(); ) {
                DatatypeProperty prop = (DatatypeProperty)it.next();
                
                boolean ADD_PROP_FLAG = true;
                for(ExtendedIterator subp_it=prop.listSubProperties(true); subp_it.hasNext(); ) {
                    System.out.println("SUB_PROP: "+subp_it.next().toString());
                    ADD_PROP_FLAG = false;
                }                
                if(ADD_PROP_FLAG && !atype.containsProperty(prop.getURI())) {
                    AbstractDatatypeElement elem = null;
                    if(containerUri.equals(atype.getUrl())) {
                        elem = new AbstractDatatypeElement("DATATYPE", prop.getURI(), helper.getPropertyRange(prop));                        
                    }
                    else {
                        elem = new AbstractDatatypeElement("DATATYPE", prop.getURI(), helper.getPropertyRange(prop), level, containerUri);
                    }
                    
                    System.out.println("[i] Add elem (dat) "+elem.toString());
                    atype.addProperty(elem);
                }
                else if(ADD_PROP_FLAG && atype.containsProperty(prop.getURI())) {
                    System.out.println("[i] Don't need to add "+prop.getLocalName()+" to "+atype.getLocalName()+". Already added.");
                }
                
                //
                // set range, that ist not defined by restrictions, constants (directly defined)
                // cause elements could already set by super classes, the element has to be
                // referenced here to add a range.
                //
                if(this.get(containerUri).getConstantMap().containsKey(prop.getURI())) {
                    atype.getElement(prop.getURI()).addToRestrictedRange(this.get(containerUri).getConstantMap().get(prop.getURI()).toString());
                    System.out.println("[i] ADD CONSTANT for "+prop.getURI()+": "+this.get(containerUri).getConstantMap().get(prop.getURI()).toString()+" at level "+level);
                } 
            }

            // OBJECT-TYPE
            for(Iterator it = this.get(containerUri).getObjectProperties().iterator(); it.hasNext(); ) {
                ObjectProperty prop = (ObjectProperty)it.next();
                System.out.println("OBJ P.:"+prop.getURI());                
                
                // Decision, if subproperties should be added
                boolean ADD_PROP_FLAG = true;
                for(ExtendedIterator subp_it=prop.listSubProperties(true); subp_it.hasNext(); ) {
                    System.out.println("SUB_PROP: "+subp_it.next().toString());
                    ADD_PROP_FLAG = false;
                }

                if(ADD_PROP_FLAG && !atype.containsProperty(prop.getURI())) 
                {
                    // process SuperProperties:
                    if(prop.getRange()==null) {
                        System.out.println("[i] prop.getRange() == null");
                        for(ExtendedIterator superPropIt = prop.listSuperProperties(true); superPropIt.hasNext(); ) {
                            ObjectProperty super_prop = ((OntResource)superPropIt.next()).asObjectProperty();
                            if(super_prop.getRange()!=null) {
                                String info = "Range of "+prop.getLocalName()+" inherited from SuperProperty "+super_prop.getLocalName();
                                atype.addParseMessage(info);
                            }
                        }
                    }
                    
                    if(prop.getRange()==null) {
                        continue;
                    }
                    
                    AbstractDatatypeElement elem = null;
                    if(prop.getRange().asClass().isUnionClass()) {
                        AbstractDatatype metaUnionType = new AbstractDatatype();
                        metaUnionType.setLocalName(prop.getLocalName()+"Type");
                        metaUnionType.setUrl(prop.getURI());
                        
                        UnionClass uc = prop.getRange().asClass().asUnionClass();
                        RDFList clist = uc.getOperands();
                        for(ExtendedIterator ucIt = clist.iterator(); ucIt.hasNext(); ) {
                            RDFNode node = (RDFNode)ucIt.next();
                            if(node.canAs(OntClass.class)) {
                                OntClass unionClass = (OntClass)node.as(OntClass.class);
                                String ucElementName = "elem"+Integer.toString(metaUnionType.getProperties().size()+1);
                                AbstractDatatypeElement ucElem = new AbstractDatatypeElement("UNION-CLASS", ucElementName, unionClass.getURI());
                                metaUnionType.addProperty(ucElem);
                            }
                        }                        
                        AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().addMetaDatatype(metaUnionType);
                        elem = new AbstractDatatypeElement("UNION", prop.getURI(), metaUnionType.getLocalName());
                    }
                    else {
                        if(containerUri.equals(atype.getUrl())) {
                            elem = new AbstractDatatypeElement("OBJECT", prop.getURI(), helper.getPropertyRange(prop));
                        }
                        else {
                            elem = new AbstractDatatypeElement("OBJECT", prop.getURI(), helper.getPropertyRange(prop), level, containerUri);
                        }
                    }
                    
                    if(prop.isFunctionalProperty()) {
                        elem.addRestrictionElement(new AbstractDatatypeRestrictionElement("isFunctional", "1", level, containerUri));
                    }
                    if(prop.isInverseFunctionalProperty()) {
                        elem.addRestrictionElement(new AbstractDatatypeRestrictionElement("isInverseFunctional", "1", level, containerUri));
                    }
                    if(prop.isTransitiveProperty()) {
                        elem.addRestrictionElement(new AbstractDatatypeRestrictionElement("isTransitive", "1", level, containerUri));
                    }
                    
                    System.out.println("[i] Add elem (obj) "+elem.toString());
                    
                    // add transitive property only over restrictions
                    if(!prop.isTransitiveProperty()) {                    
                        atype.addProperty(elem);
                    }
                    else {
                        System.out.println("   transitive");
                    }
                }
                else if(ADD_PROP_FLAG && atype.containsProperty(prop.getURI())) {
                    System.out.println("[i] Don't need to add "+prop.getLocalName()+" to "+atype.getLocalName()+". Already added.");
                }
                
                //
                // set range, that ist not defined by restrictions, constants (directly defined)
                // cause elements could already set by super classes, the element has to be
                // referenced here to add a range.
                //
                if(this.get(containerUri).getConstantMap().containsKey(prop.getURI())) {
                    atype.getElement(prop.getURI()).addToRestrictedRange(this.get(containerUri).getConstantMap().get(prop.getURI()).toString());
                    System.out.println("[i] ADD CONSTANT for "+prop.getURI()+": "+this.get(containerUri).getConstantMap().get(prop.getURI()).toString()+" at level "+level);
                }
            }            
            
            //System.out.println("getSubClassPropertyRestrictions: "+this.get(containerUri).getSubClassPropertyRestrictions().size());
            for(int i=0; i<this.get(containerUri).getSubClassPropertyRestrictions().size(); i++) {
                this.addElementRestrictions(atype, level, containerUri, (Restriction)this.get(containerUri).getSubClassPropertyRestrictions().get(i), null);
            }
            
            //System.out.println("getEquivalentPropertyRestrictions: "+this.get(containerUri).getEquivalentPropertyRestrictions().size());
            for(int i=0; i<this.get(containerUri).getEquivalentPropertyRestrictions().size(); i++) {
                this.addElementRestrictions(atype, level, containerUri, (Restriction)this.get(containerUri).getEquivalentPropertyRestrictions().get(i), null);
            }
            
            // CHECK FOR COMPLEMENT DEFINTION TO ADJUST RANGE
            if(!this.get(containerUri).getEquivalentComplementClassSet().isEmpty()) {
                System.out.println("getEquivalentComplementClassSet COMPLEMENTS!");
                int i=0;
                for(Iterator it=this.get(containerUri).getEquivalentComplementClassSet().iterator(); it.hasNext(); ) 
                {   i++;             
                    ComplementClass complement = (ComplementClass) it.next();
                    if(complement.getOperand().isRestriction())
                    {
                        Restriction complRes    = complement.getOperand().asRestriction();
                        String ontpropertyName  = complRes.getOnProperty().getURI();
                        String ontpropertyRange = complRes.getOnProperty().getRange().getURI();

                        System.out.println("(Complement) NAME : "+ontpropertyName);
                        System.out.println("(Complement) RANGE: "+ontpropertyRange);
                        
                        //this.addElementRestrictions(atype, level, containerUri, complRes); //verwirrend, weil das ja die Ausschlussmenge ist.
                        
                        if(complRes.isHasValueRestriction()) 
                        {
                            atype.getElement(ontpropertyName).setLevel(level);
                            atype.getElement(ontpropertyName).setInheritedBy(containerUri);
                            
                            OntResource hvrvalue = (OntResource)complRes.asHasValueRestriction().getHasValue().as(OntResource.class);
                            atype.getElement(ontpropertyName).addToComplementRange(hvrvalue.getURI());
                            
                            // Process restrictedRange Vector
                            if(i == this.get(containerUri).getEquivalentComplementClassSet().size()) {
                                atype.getElement(ontpropertyName).setRestrictedRange(this.get(ontpropertyRange).getIndividualUriList());
                                atype.getElement(ontpropertyName).removeComplementsFromRestrictedRange();
                            }
                            
//                            atype.getElement(ontpropertyName).emptyRestrictedRange();
//                            for(Iterator ind_it = this.get(ontpropertyRange).getIndividualList().iterator(); ind_it.hasNext(); ) 
//                            {
//                                String currange = ind_it.next().toString();
//                                if(currange.equals(hvrvalue.getURI())) {
//                                    System.out.println("(-) "+currange);                                    
//                                }
//                                else {
//                                    //System.out.println("(+) "+currange);
//                                    atype.getElement(ontpropertyName).addToRestrictedRange(currange);
//                                }                                
//                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * add restriction information to AbstractDatatypeElement
     */
    private void addElementRestrictions(AbstractDatatype atype, int level, String inheritedBy, Restriction r, String context) 
    {
        System.out.println();
        System.out.println("Process addElementRestriction");
        helper.printRestriction(r);
        
        try {
            // 8.2.: there are ontologies, that don't define the type of restriction operand
            // ConversionException: Cannot convert node http://www.mindswap.org/2003/owl/geo/geoFeatures20040307.owl#coordinates to OntProperty
            
            OntProperty ontproperty = r.getOnProperty();        
            String ontpropertyName = ontproperty.getURI();
            String owlsource = "RESTRICTION";
            
            if(ontproperty.isDatatypeProperty()){
                owlsource = "RESTRICTION-ON-DATATYPE";
            }
            else if(ontproperty.isObjectProperty()) {
                owlsource = "RESTRICTION-ON-OBJECT";
            }

            if(!atype.containsProperty(ontpropertyName)) 
            {
                String cur_range = "not yet available";                
                
                if(ontproperty.getRange()!=null) {
                    cur_range = ontproperty.getRange().getURI();                    
                    System.out.println("[OntClassKB] addElementRestrictions, found range: "+cur_range);
                }
                else {
                    //cur_range = "http://www.w3.org/2001/XMLSchema#anyType"; // The WSDL Analyzer don't understand anyType
                    //cur_range = "http://www.w3.org/2002/07/owl#Thing"; // must be translated into the default type
                    System.out.println("[OntClassKB] addElementRestrictions, getRange is null!");
                    for(Iterator it=ontproperty.listRDFTypes(true); it.hasNext(); ) {
                        Resource cls = (Resource) it.next();
                        System.out.println( "[OntClassKB] addElementRestrictions, ontproperty, Individual "+ cls.getURI() );
                    }
                    for(ExtendedIterator eit=ontproperty.listRange(); eit.hasNext(); ) {
                        System.out.println("[OntClassKB] addElementRestrictions, listRange: "+eit.next().getClass().getName() );
                    }
                    
                    cur_range = "null";
                }

                AbstractDatatypeElement elem = null;
                if(inheritedBy.equals(atype.getUrl())) {                
                    elem = new AbstractDatatypeElement(owlsource, ontpropertyName, cur_range, level, null);
                }
                else {
                    elem = new AbstractDatatypeElement(owlsource, ontpropertyName, cur_range, level, inheritedBy);
                }
                atype.addProperty(elem);
            }
    //        else {
    //            System.out.println(" Type "+atype.getLocalName()+" contains already property "+ontpropertyName);
    //        }

            if( 0 == level ) {
                inheritedBy = null;
            }

            if(r.isCardinalityRestriction()) {
                AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("Cardinality", Integer.toString(r.asCardinalityRestriction().getCardinality()), level, inheritedBy);
                atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
            }
            else if(r.isMinCardinalityRestriction()) {
                AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("MinCardinality", Integer.toString(r.asMinCardinalityRestriction().getMinCardinality()), level,inheritedBy);
                atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);                        
            }
            else if(r.isMaxCardinalityRestriction()) {
                AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("MaxCardinality", Integer.toString(r.asMaxCardinalityRestriction().getMaxCardinality()), level,inheritedBy);
                atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
            }
            else if(r.isAllValuesFromRestriction()) 
            {                
                // Reset values from parents and intersection classes
                atype.getElement(ontpropertyName).clearAllValuesFromRestrictions();
                
                try {                        
                    OntClass avfrvalue = (OntClass)r.asAllValuesFromRestriction().getAllValuesFrom().as( OntClass.class );

                    if( avfrvalue.isEnumeratedClass() ) {
                        for(ExtendedIterator oneOfIt = avfrvalue.asEnumeratedClass().listOneOf(); oneOfIt.hasNext(); ) {
                            OntResource oneOf = (OntResource) oneOfIt.next();                        
                            AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("allValuesFrom", oneOf.getURI(), level, inheritedBy);
                            atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                            atype.getElement(ontpropertyName).addToRestrictedRange(oneOf.getURI());
                        }
                    }
                    else {
                        
                        // use reaoner to get OWL class for OntResource
                        // http://nuin.blogspot.com/2007/01/jena-tip-get-owl-class-of-_116899477372532135.html                   
                        for (Iterator it = avfrvalue.listRDFTypes(true); it.hasNext(); ) {
                            Resource cls = (Resource) it.next();
                            System.out.println( "[isAllValuesFromRestriction] Individual "+avfrvalue.getLocalName()+" has rdf:type " + cls );
                            atype.getElement(ontpropertyName).setType(cls.getURI());
                        }
                        
                        atype.getElement(ontpropertyName).setType(avfrvalue.getURI());
                        AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("allValuesFrom", r.asAllValuesFromRestriction().getAllValuesFrom().getURI(), level, inheritedBy);
                        atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                    }
                }
                catch(Exception e) {
                    System.out.println("[e] process isAllValuesFromRestriction: "+e.getMessage());
                    if( helper.checkForPrimitiveSchemaType(r.asAllValuesFromRestriction().getAllValuesFrom().getURI()) ) {
                        System.out.println("--> "+r.asAllValuesFromRestriction().getAllValuesFrom().getURI());
                        System.out.println("--> Primitive Type found.");
                        atype.getElement(ontpropertyName).setType(r.asAllValuesFromRestriction().getAllValuesFrom().getURI());                        
                        AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("allValuesFrom", r.asAllValuesFromRestriction().getAllValuesFrom().getURI(), level, inheritedBy);
                        atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                    }                    
                }
            }
            else if(r.isSomeValuesFromRestriction()) 
            {                
                // Reset values from parents and intersection classes
                atype.getElement(ontpropertyName).clearSomeValuesFromRestrictions();
                
                try {
                    OntClass svfrvalue = (OntClass)r.asSomeValuesFromRestriction().getSomeValuesFrom().as( OntClass.class );

                    if( svfrvalue.isEnumeratedClass() ) {
                        for(ExtendedIterator oneOfIt = svfrvalue.asEnumeratedClass().listOneOf(); oneOfIt.hasNext(); ) {
                            OntResource oneOf = (OntResource) oneOfIt.next();
                            AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("someValuesFrom", oneOf.getURI(), level, inheritedBy);
                            atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                            atype.getElement(ontpropertyName).addToRestrictedRange(oneOf.getURI());                                                
                        }
                    }
                    else {
                        atype.getElement(ontpropertyName).setType(svfrvalue.getURI());

                        AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("someValuesFrom", r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI(), level, inheritedBy);
                        atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                    }
                }
                catch(Exception e) {
                    System.err.println("[e] process isSomeValuesFromRestriction: "+e.getMessage());
                    if( helper.checkForPrimitiveSchemaType(r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI())) {
                        atype.getElement(ontpropertyName).setType(r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI());
                        
                        AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("someValuesFrom", r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI(), level, inheritedBy);
                        atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                    }
                }
            }
            else if(r.isHasValueRestriction()) {
                System.out.println("ADD NEW RESTRICTION HASVALUE ELEMENT AND REMOVE OLD ONES.");
                atype.getElement(ontpropertyName).clearSomeValuesFromRestrictions();
                atype.getElement(ontpropertyName).clearAllValuesFromRestrictions();
                if(context==null || !context.equals("UNION")) {
                    atype.getElement(ontpropertyName).emptyRestrictedRange();
                    atype.getElement(ontpropertyName).removeHasValueElement();
                }
                
                HasValueRestriction hvr = r.asHasValueRestriction();
                if(hvr.getHasValue().isLiteral()) {
                    System.out.println("     [Res] HasValue. isLiteral "+hvr.getHasValue().asNode().getLiteralValue()+" ("+hvr.getHasValue().asNode().getLiteralDatatypeURI()+")");
                    AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("hasValue", hvr.getHasValue().asNode().getLiteralValue().toString(), level, inheritedBy);
                    atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                    atype.getElement(ontpropertyName).addToRestrictedRange(hvr.getHasValue().asNode().getLiteralValue().toString());
                }
                else {
                    OntResource hvrvalue = (OntResource)r.asHasValueRestriction().getHasValue().as(OntResource.class);
                    
                    // use reaoner to get OWL class for OntResource
                    // http://nuin.blogspot.com/2007/01/jena-tip-get-owl-class-of-_116899477372532135.html                   
                    for (Iterator it = hvrvalue.listRDFTypes(true); it.hasNext(); ) {
                        Resource cls = (Resource) it.next();
                        System.out.println( "[isHasValueRestriction] Individual "+hvrvalue.getLocalName()+" has rdf:type " + cls );
                        if(!cls.getURI().equals("http://www.w3.org/2002/07/owl#Thing")) {
                            atype.getElement(ontpropertyName).setType(cls.getURI());
                        }
                    }
                    
                    AbstractDatatypeRestrictionElement restrictionElement = new AbstractDatatypeRestrictionElement("hasValue", hvrvalue.getURI(), level, inheritedBy);
                    atype.getElement(ontpropertyName).addRestrictionElement(restrictionElement);
                    atype.getElement(ontpropertyName).addToRestrictedRange(hvrvalue.getURI());
                }
            }
            else {
                System.out.println("Can't sync restriction to AbstractDatatype element.");
            }
        }
        catch(com.hp.hpl.jena.ontology.ConversionException ce) {
            System.err.println("[e] "+ce.getMessage());
            ce.printStackTrace();
        }
    }
    
    /**
     * @obsolete
     */
    public HashSet getInheritedProperties(String uri) {        
        Vector list = this.buildSuperClassList(uri);
        HashSet properties = new HashSet();
        for(int i=0; i<list.size();i++) {
            properties.addAll(this.get(list.get(i).toString()).getProperties());
        }
        return properties;
    }
    
    /*
     * Special class, that collects the necessary informations about
     * the xml schema type. Also used for mapping to xml registry file. 
     */
     public void syncAbstractDatatypeKB() 
     {
         AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().addOntologyURIs(this.ontologyURIs);
         
         Vector keys = new Vector( this.registeredDatatypes.keySet() );
        
//        keys.removeAllElements();
//        keys.add("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine");
//        keys.add("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bordeaux");
//        keys.add("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhiteBordeaux");
//        keys.add("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhiteWine");
//        keys.add("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#NonFrenchWine");
        
//        keys.add("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#UnitOfMeasure");
//        keys.add("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#SetOrClass");
//        keys.add("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#EuroDollar");
//        keys.add("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#YearDuration");
        
//        keys.add("http://127.0.0.1/ontology/Mid-level-ontology.owl#LinguisticAttribute");
        
        OntClassContainer curOntContainer = null;
                 
        for(int i=0; i<keys.size(); i++) {
            if(keys.get(i)!=null) {
                String key = keys.get(i).toString();
                System.out.println("\nKEY (OntClassKB) "+key);
                curOntContainer = (OntClassContainer)this.registeredDatatypes.get(key);
                
                if(!AbstractDatatypeKB.getInstance().data.containsKey(curOntContainer.getName())) { // && curOntContainer.validateLocalName().size() > 0) {
                    AbstractDatatype atype = this.buildAbstractDatatype(curOntContainer);
                    
                    if(this.erroneousDatatypes.containsKey(key)) {
                        atype.setErrorMessages(this.getErrorList(key));
                    }
                    
//                    atype.addAllParseMessages(curOntContainer.getParseInformation()); // gaaanz wichtig: addAll und nicht set, da sonst alle vorher gesetzten messages aus buildAbstractDatatype verlogen gehen!
                    atype.setTypeList(curOntContainer.getTypeClassUriList());
                    atype.addRangeList(curOntContainer.getIndividualUriMap());                    
                    
                    AbstractDatatypeKB.getInstance().addDatatype(atype);                    
                    System.out.println("[i] new AbstractDatatype for "+atype.getUrl()+" created.");
                }
            }
        }
        
        
        
     }

     private AbstractDatatypeElement createMetaElementFromRestriction(OntClassContainer container, Restriction r, String owlSource, String name)
     {
        OWLHelper helper = new OWLHelper();
        helper.printRestriction(r);
        OntProperty ontproperty = r.getOnProperty();
        AbstractDatatypeElement elem = null;

        for (Iterator it = ontproperty.listRDFTypes(true); it.hasNext(); ) {
            Resource cls = (Resource) it.next();
            System.out.println( "Individual "+ontproperty.getLocalName()+" has rdf:type " + cls );
        }
        
        // create dependency type for new restriction element
        AbstractDatatype metaRestrictionType = new AbstractDatatype();        
        metaRestrictionType.setUrl(container.getName());
        if(ontproperty.getRange() != null) {
            AbstractDatatypeElement onProperty  = new AbstractDatatypeElement("ONPROPERTY", ontproperty.getURI(), ontproperty.getRange().getURI());
            metaRestrictionType.addProperty(onProperty);                
        }
        else {
            System.out.println("[OntClassKB] createMetaElementFromRestriction, can't find range for OnProperty");
            if(r.isSomeValuesFromRestriction()) {
                OntClass svfrvalue = (OntClass)r.asSomeValuesFromRestriction().getSomeValuesFrom().as( OntClass.class );
                if(!svfrvalue.isEnumeratedClass()) {
                    AbstractDatatypeElement onProperty  = new AbstractDatatypeElement("ONPROPERTY", ontproperty.getURI(), svfrvalue.getURI());
                    metaRestrictionType.addProperty(onProperty);
                }
            }
        }
                
        String restrictionTypeName = ontproperty.getLocalName()+container.getLocalName(); // + RestrictionComponent type
        
        if(r.isCardinalityRestriction()) 
        {            
            metaRestrictionType.setLocalName(restrictionTypeName+"CardinalityRestriction");                        
            AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "cardinality", "Cardinality");// "http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
            restriction.addToRestrictedRange(Integer.valueOf(r.asCardinalityRestriction().getCardinality()).toString() );            
            metaRestrictionType.addProperty(restriction);            
                        
            elem = new AbstractDatatypeElement(owlSource, name, restrictionTypeName+"CardinalityRestriction");
        }
        else if(r.isMinCardinalityRestriction()) {
            metaRestrictionType.setLocalName(restrictionTypeName+"MinCardinalityRestriction");                        
            AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "minCardinality", "MinCardinality"); //http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
            restriction.addToRestrictedRange(Integer.valueOf(r.asMinCardinalityRestriction().getMinCardinality()).toString());            
            metaRestrictionType.addProperty(restriction);
            
            elem = new AbstractDatatypeElement(owlSource, name, restrictionTypeName+"MinCardinalityRestriction");
        }
        else if(r.isMaxCardinalityRestriction()) {
            metaRestrictionType.setLocalName(restrictionTypeName+"MaxCardinalityRestriction");
            AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "maxCardinality", "MaxCardinality"); //http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
            restriction.addToRestrictedRange(Integer.valueOf(r.asMaxCardinalityRestriction().getMaxCardinality()).toString());
            metaRestrictionType.addProperty(restriction);
            
            elem = new AbstractDatatypeElement(owlSource, name, restrictionTypeName+"MaxCardinalityRestriction");            
        }
        else if(r.isAllValuesFromRestriction()) {
            metaRestrictionType.setLocalName(restrictionTypeName+"AllValuesFromRestriction");            
            
            OntClass avfrvalue = (OntClass)r.asAllValuesFromRestriction().getAllValuesFrom().as( OntClass.class );
                        
            if( avfrvalue.isEnumeratedClass() ) {
                AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "allValuesFrom", ontproperty.getRange().getURI());
                for(ExtendedIterator oneOfIt = avfrvalue.asEnumeratedClass().listOneOf(); oneOfIt.hasNext(); ) {
                    OntResource oneOf = (OntResource) oneOfIt.next();
                    //restriction.addToRestrictedRange(oneOf.getLocalName());
                    restriction.addToRestrictedRange(oneOf.getURI());
                }
                metaRestrictionType.addProperty(restriction);
            }
            else {
                AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "allValuesFrom", avfrvalue.getURI());
                metaRestrictionType.addProperty(restriction);
            }            
            
            elem = new AbstractDatatypeElement(owlSource, name, restrictionTypeName+"AllValuesFromRestriction");
        }
        else if(r.isSomeValuesFromRestriction()) {
            /**
             * http://www.semaweb.org/dokumente/w3/TR/2004/REC-owl-guide-20040210-DE.html
             * Unterschied zwischen notwendig (subClass) und hinreichend (equivalent)
             */
            //System.out.println("isSomeValuesFromRestriction, URI: "+r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI());
            metaRestrictionType.setLocalName(restrictionTypeName+"SomeValuesFromRestriction");
            
            OntClass svfrvalue = (OntClass)r.asSomeValuesFromRestriction().getSomeValuesFrom().as( OntClass.class );
            
            if( svfrvalue.isEnumeratedClass() ) {
                AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "someValuesFrom", ontproperty.getRange().getURI());
                for(ExtendedIterator oneOfIt = svfrvalue.asEnumeratedClass().listOneOf(); oneOfIt.hasNext(); ) {
                    OntResource oneOf = (OntResource) oneOfIt.next();
                    //restriction.addToRestrictedRange(oneOf.getLocalName());
                    restriction.addToRestrictedRange(oneOf.getURI());
                }
                metaRestrictionType.addProperty(restriction);
            }
            else {
                AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "someValuesFrom", svfrvalue.getURI());
                metaRestrictionType.addProperty(restriction);
            }
            elem = new AbstractDatatypeElement(owlSource, name, restrictionTypeName+"SomeValuesFromRestriction");
        }
        else if(r.isHasValueRestriction()) {
            metaRestrictionType.setLocalName(restrictionTypeName+"HasValueRestriction");
            AbstractDatatypeElement restriction = new AbstractDatatypeElement("RESTRICTION-COMPONENT", "hasValue", ontproperty.getRange().getURI());
            
            HasValueRestriction hvr = r.asHasValueRestriction();
            if(hvr.getHasValue().isLiteral()) {
                System.out.println("     [Res] HasValue. isLiteral "+hvr.getHasValue().asNode().getLiteralValue()+" ("+hvr.getHasValue().asNode().getLiteralDatatypeURI()+")");
                restriction.addToRestrictedRange(hvr.getHasValue().asNode().getLiteralValue().toString());
            }
            else {
                OntResource hvrvalue = (OntResource)r.asHasValueRestriction().getHasValue().as(OntResource.class);                            
                restriction.addToRestrictedRange(hvrvalue.getURI());                
            }
            metaRestrictionType.addProperty(restriction);
            elem = new AbstractDatatypeElement(owlSource, name, restrictionTypeName+"HasValueRestriction");            
        }
        else {
            System.out.println("[e] Can't sync restriction to AbstractDatatype element.");
        }
        
        AbstractDatatypeKB.getInstance().data.addMetaDatatype(metaRestrictionType);
        
        return elem;
    }     
     
    public AbstractDatatype buildAbstractDatatype(OntClassContainer curOntContainer)
    {
        // OWLHelper helper = new OWLHelper(); --> static class
        
        System.out.println("######### CUR-CONTAINER: "+curOntContainer.toString());        
        /* ================================================================== */
        AbstractDatatype atype = new AbstractDatatype(curOntContainer);
        System.out.println("### AbstractDatatypeURI: "+atype.getUrl());
                
        System.out.println("[i] === EquivalentUnionClassSet ================================================");
        if(!curOntContainer.getEquivalentUnionClassSet().isEmpty()) 
        {            
            AbstractDatatypeElement metaElement
                    = new AbstractDatatypeElement("UNION", "hasSufficientUnion", "Sufficient"+curOntContainer.getLocalName()+"Union");
            atype.addProperty(metaElement);
            
            AbstractDatatype metaType = new AbstractDatatype();
            metaType.setLocalName("Sufficient"+curOntContainer.getLocalName()+"Union");
            metaType.setUrl(curOntContainer.getName());
            metaType.setRdfsComment("n/a");
            metaType.setOwlVersionInfo("n/a");
            
            for(Iterator it=curOntContainer.getEquivalentUnionClassSet().iterator(); it.hasNext(); ) 
            {
                OntClass unionClass = (OntClass) it.next();
                String unionElementName = "elem"+Integer.toString(metaType.getProperties().size()+1);
                        
                if(unionClass.isRestriction()) {
                    System.out.println("[i] Equivalent Restriction found.");
                    AbstractDatatypeElement elem = this.createMetaElementFromRestriction(curOntContainer, unionClass.asRestriction(), "UNION-RESTRICTION", unionElementName);
                    metaType.addProperty(elem);
                    
//                    // REGULAR ABSTRACT DATATYPE (not placed in a recursive method, that add restrictions by superclass oder intersection class)
//                    this.addElementRestrictions(atype, 0, atype.getUrl(), unionClass.asRestriction());
                }
                else if(unionClass.isUnionClass()) {
                    System.out.println("[i] another Union in Union");
                }
                else if(unionClass.isClass()) {                    
                    AbstractDatatypeElement elem = new AbstractDatatypeElement("UNION-CLASS", unionElementName, unionClass.getURI());
                    metaType.addProperty(elem);
                }
                else {
                    System.out.println("RDF-TYPE unkown: "+unionClass.getRDFType().toString());
                }
            }
            AbstractDatatypeKB.getInstance().data.addMetaDatatype(metaType);
        }
                
        System.out.println("[i] === EquivalentIntersectionSet ==============================================");
        if(!curOntContainer.getEquivalentIntersectionSet().isEmpty()) 
        {
            AbstractDatatypeElement metaElement
                    = new AbstractDatatypeElement("INTERSECTION", "hasSufficientIntersection", "Sufficient"+curOntContainer.getLocalName()+"Intersection");
            atype.addProperty(metaElement);
            
            AbstractDatatype metaIntersectionType = new AbstractDatatype();
            System.out.println("SET LOCALNAME: "+"Sufficient"+curOntContainer.getLocalName()+"Intersection");
            System.out.println("SET       URI: "+curOntContainer.getName());
            metaIntersectionType.setLocalName("Sufficient"+curOntContainer.getLocalName()+"Intersection");
            metaIntersectionType.setUrl(curOntContainer.getName());
            AbstractDatatypeKB.getInstance().data.addMetaDatatype(metaIntersectionType);
            
            for(Iterator it=curOntContainer.getEquivalentIntersectionSet().iterator(); it.hasNext(); ) 
            {               
                OntClass intSecClass = (OntClass) it.next();
                String intersectionElementName = "elem"+Integer.toString(metaIntersectionType.getProperties().size()+1);
                        
                if(intSecClass.isRestriction()) {
                    System.out.println("[i] Equivalent restriction found.");
                    Restriction r = intSecClass.asRestriction();
                    //curOntContainer.addSubClassPropertyRestriction(intSecClass.asRestriction());                    
                    
                    this.addElementRestrictions(atype, 0, atype.getUrl(), r, null);
                    
                    AbstractDatatypeElement elem = this.createMetaElementFromRestriction(curOntContainer, r, "INTERSECTION-RESTRICTION", intersectionElementName);                    
                    if(elem!=null)
                        metaIntersectionType.addProperty(elem);
                    else
                        System.err.println("<<< PROBLEM >>>");
                }
                else if(intSecClass.isComplementClass()) {
                    System.out.println("[i] Equivalent ComplementClass found.");
                    if(intSecClass.asComplementClass().getOperand().isRestriction()) {
                        Restriction complRes = intSecClass.asComplementClass().getOperand().asRestriction();
                        System.out.println("[i] Equivalent Complement found: "+complRes.getOnProperty().getURI());
                        
                        AbstractDatatypeElement elem = this.createMetaElementFromRestriction(curOntContainer, complRes, "INTERSECTION-RESTRICTION", intersectionElementName);
                        elem.setOwlSource("INTERSECTION-COMPLEMENT-RESTRICTION");
                        String complementName = elem.getName();
                        complementName = complementName.replaceAll("Restriction", "ComplementRestriction");
//                        complementName = complementName.substring(0,1).toUpperCase()+complementName.substring(1);
//                        complementName = "not"+complementName;
                        elem.setName(complementName);
                        
                        if(complRes.isHasValueRestriction()) {
                            String range = complRes.getOnProperty().getRange().getURI();
                            System.out.println("RANGE: "+range);
                            Iterator ind_it = this.get(range).getIndividualList().iterator();
//                            while(ind_it.hasNext()) {
//                                  System.out.println(" > "+ind_it.next().toString());
//                            }
                            OntResource hvrvalue = (OntResource)complRes.asHasValueRestriction().getHasValue().as(OntResource.class);
                            System.out.println("VALUE: "+hvrvalue.getURI());                            
                        }
                        else {
                            System.out.println("[e] complement has not an hasValue Restriction");
                        }
                        metaIntersectionType.addProperty(elem);
                    }                    
                }
                else if(intSecClass.isClass()) {
                    System.out.println("[i] Equivalent Class found.");
                    if(intSecClass.getURI() == null) {
                        System.out.println("[i] another anonym inner class found.");
                    }
                    else {
                        AbstractDatatypeElement elem = new AbstractDatatypeElement("INTERSECTION-CLASS", intersectionElementName, intSecClass.getURI());
                        metaIntersectionType.addProperty(elem);
                        atype.addIntersection(intSecClass.getURI());
                    }
                }
                else {
                    System.out.println("RDF-TYPE unkown: "+intSecClass.getRDFType().toString());
                }
            }
        }        
        
        if(!curOntContainer.getEquivalentEnumerationSet().isEmpty()) {
            System.out.println("[i] === EquivalentEnumerationSet (oneOf collections of individuals ) ============");
            for(Iterator it = curOntContainer.getEquivalentEnumerationSet().iterator(); it.hasNext(); ) {
                OntResource ontRes = (OntResource)it.next();
                System.out.println("          [I,oneOf] "+ontRes.getURI()+" ("+ontRes.getRDFType().getURI()+")");
                atype.addIndividualRange(ontRes.getURI(), ontRes.getRDFType().getURI());
            }
        }
        
        System.out.println("[i] === IndividualList =========================================================");
        
        if(!curOntContainer.getIndividualList().isEmpty()) {
            for(Iterator it = curOntContainer.getIndividualList().iterator();it.hasNext();) {
                Individual i = (Individual)it.next();
                atype.addIndividualRange(i.getURI(), i.getRDFType().getURI());
            }
        }        
                
        //
        // collect all properties and restrictions from intersection classes
        //        
        System.out.println("[i] === EquivalentIntersection ==================================================");
        this.addAbstractDatatypeElementsByIntersectionClass(atype, curOntContainer.getName(), -1);
                
        //
        // Collect all properties and restrictions from super classes
        //                
        System.out.println("[i] === Super Classes ==========================================================");
        this.addAbstractDatatypeElementsBySuperClass(atype, curOntContainer.getName(), -1);
        
        //
        // Collect restrictions from UNION COLLECTION
        //       
        System.out.println("[i] === UNION COLLECTION =======================================================");        
        for(Iterator it=curOntContainer.getSubClassUnionClassSet().iterator(); it.hasNext(); ) {
            OntClass ontClass = (OntClass)it.next();
            if(ontClass.isRestriction()) {
                this.addElementRestrictions(atype, 0, atype.getUrl(), ontClass.asRestriction(), "UNION");
            }
        }
        
        //
        // process complements
        //
        Vector complements = new Vector();

//        System.out.println("Property           Count: "+this.get(containerUri).getPropertyCount());
//        System.out.println("Equiv. Compl.Class Count: "+this.get(containerUri).getEquivalentComplementClassSet().size());


    //            complements.addAll(this.get(containerUri).getSubClassComplementClassSet());
    //            complements.addAll(this.get(containerUri).getEquivalentComplementClassSet());       
        
        // delete obsolete and double restriction entries (from super and intersection classes)
        
        
        for(Iterator it = complements.iterator(); it.hasNext(); ) 
        {
            ComplementClass complement = (ComplementClass) it.next();
            if(complement.getOperand().isRestriction())
            {
                Restriction complRes    = complement.getOperand().asRestriction();                    
                String ontpropertyName  = complRes.getOnProperty().getURI();
                String ontpropertyRange = complRes.getOnProperty().getRange().getURI();

                System.out.println("(Complement) NAME : "+ontpropertyName);
                System.out.println("(Complement) RANGE: "+ontpropertyRange);

                System.out.println("Element Info      : ");
                atype.printDatatype();
                
//                atype.getElement(ontpropertyName).emptyRestrictedRange();
//
//                if(complRes.isHasValueRestriction()) 
//                {
//                    OntResource hvrvalue = (OntResource)complRes.asHasValueRestriction().getHasValue().as(OntResource.class);
//
//                    Iterator ind_it = this.get(ontpropertyRange).getIndividualList().iterator();
//                    while(ind_it.hasNext()) {
//                        String currange = ind_it.next().toString();
//                        if(currange.equals(hvrvalue.getURI())) {
//                            System.out.println("(-) "+currange);
//                        }
//                        else {
//                            //System.out.println("(+) "+currange);
//                            atype.getElement(ontpropertyName).addToRestrictedRange(currange);
//                        }
//                    }
//                }
            }
        }        

//        System.out.println("=====================================================================");
//        System.out.println("ZUM VERGLEICH CONTAINER DATA");
//        atype.printDatatype();
//        this.showRegisteredDatatypeFull(curOntContainer.getName());
        return atype;
    }

}