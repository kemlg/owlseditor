package de.dfki.dmas.owls2wsdl.parser;
/*
 * OntClassContainer.java
 *
 * Created on 8. August 2006, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import com.hp.hpl.jena.ontology.impl.OntResourceImpl;
import de.dfki.dmas.owls2wsdl.core.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.IOException;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.log4j.TTCCLayout;
//import org.exolab.castor.xml.schema.writer.SchemaWriter;
//import org.exolab.castor.xml.schema.*;

/**
 *
 * @author Oliver Fourman
 */
public class OntClassContainer {
    
    public static final boolean VERBOSE = true;    
    
    private OntClass  ontclass;                // main owl/rdf class
    
    private String    RDFS_Comment    = null;
    private String    OWL_VersionInfo = null;
    
    private Vector    typeClassList;           // rdf:type
    private HashSet   superClassSet;           // set of super classes
    
    // The following variables (HashSets) are used to collect logical owl sets
    // like intersection, union, ...
    // these could be defined as subclass relations or equivalent relations
    // The information is used to create new meta types that mirror the 
    // relationship into XML schema.
    
    private HashSet   subclassUnionSet;        // set of super classes (filled in DatatypeParser)
    private HashSet   subclassIntersectionSet; // set of intersection classes
    private HashSet   subclassComplementSet;   // set of complement classes    
    private HashSet   subclassEnumerationSet;  // set of enumeration classes (OneOf)    
    private Vector    subclassPropertyRestrictions; // list of ranges of properties
    
    private HashSet   equivalentEnumerationSet;  // collection of individuals / OntResources    
    private HashSet   equivalentUnionSet;        // eg. http://www.daml.org/services/owl-s/1.1/Process.owl#Process
    private HashSet   equivalentIntersectionSet; // eg. http://www.daml.org/services/owl-s/1.1/Process.owl#OutputBinding
                                                 // contains classes and restrictions
    private HashSet   equivalentComplementSet;
    private Vector    equivalentPropertyRestrictions;
    
    private HashSet   propertySet;          // set of datatype and object properties
    private Vector    individualList;       // list of ontclass individuals
    
    private HashMap   constantMap;

    private Vector    parseInformation;     // OWL information
    
    private static OWLHelper owlHelper = new OWLHelper();
    
    /** Creates a new instance of OntClassContainer */
    private OntClassContainer() {
        this.ontclass                     = null;
        this.OWL_VersionInfo              = null;
        this.RDFS_Comment                 = null;
        
        this.typeClassList                = new Vector();
        this.superClassSet                = new HashSet();
        
        this.subclassUnionSet             = new HashSet();
        this.subclassIntersectionSet      = new HashSet();
        this.subclassComplementSet        = new HashSet();
        this.subclassEnumerationSet       = new HashSet();
        this.subclassPropertyRestrictions = new Vector();
        
        this.equivalentEnumerationSet     = new HashSet();
        this.equivalentUnionSet           = new HashSet();
        this.equivalentIntersectionSet    = new HashSet();
        this.equivalentComplementSet      = new HashSet();
        this.equivalentPropertyRestrictions = new Vector();        
                        
        this.propertySet          = new HashSet();        
        this.individualList       = new Vector();
        this.constantMap          = new HashMap();
        this.parseInformation     = new Vector();
    }
    

    public OntClassContainer(OntClass c, String rdfsComment, String owlVersionInfo) {
        this();
        this.ontclass = c;
        this.RDFS_Comment = rdfsComment;
        this.OWL_VersionInfo = owlVersionInfo;
                
        if(this.ontclass.isUnionClass()) { 
            
            if(VERBOSE) { System.out.println("[i] UNION found."); }
            
            // Operanden können Restrictions oder Klassen sein
            for(ExtendedIterator unionIt=this.ontclass.asUnionClass().listOperands(); unionIt.hasNext(); ) {
                this.equivalentUnionSet.add( (OntClass) unionIt.next() );
            }
        }
        else if(this.ontclass.isIntersectionClass()) { 
            
            if(VERBOSE) { System.out.println("[i] INTERSECTION found."); }
            
            // Operanden können Restrictions oder Klassen sein
            for(ExtendedIterator intSectIt=this.ontclass.asIntersectionClass().listOperands(); intSectIt.hasNext(); ) {
                OntClass curoc = (OntClass) intSectIt.next();                
                if(curoc.isRestriction()) {
                    this.addEquivalentPropertyRestriction(curoc.asRestriction());
                    System.out.println("[i] Operand is Restriction");
                }
                else if(curoc.isComplementClass()) {                 
                    this.addEquivalentComplementClass(curoc.asComplementClass()); // kann class oder restriction sein                    
                }                
                // ===
                this.equivalentIntersectionSet.add( curoc ); //<- kann alles sein: Restriction (kein else!), class, complement
            }
        }
        else if(this.ontclass.isEnumeratedClass()) {
            
            if(VERBOSE) { System.out.println("[i] ENUMERATION found. (add Individuals)"); }
            
            for(ExtendedIterator enumIt=this.ontclass.asEnumeratedClass().listOneOf(); enumIt.hasNext(); ) {
                OntResource enumRes = (OntResource) enumIt.next();
                this.addEquivalentOntResource(enumRes);
            }
        }
        
        if(this.ontclass.hasProperty(OWL.equivalentClass)) {
            System.out.println("OWL.equivalent check");
            OntClass equivalent = this.ontclass.getEquivalentClass();
            if( equivalent.isUnionClass() ) {
//                for(ExtendedIterator unionIt=this.ontclass.asUnionClass().listOperands(); unionIt.hasNext(); ) {
//                    this.equivalentUnionSet.add( (OntClass) unionIt.next() );
//                }
                System.out.println("IMPLICITE UNION CLASS");
            }
        }       
    }
    
    public OntClass getOntClass() { return this.ontclass; }
    
    /**
     * http://jena.sourceforge.net/jena-faq.html
     * Q. Why does the localname part of my URI look wrong?
     * A: In Jena it is possible to retrieve the localname part of a Resource or Property URI.
     *    Sometimes developers create Resources with a full URI reference but find that the result
     *    of a getLocalName call is not quite what they expected. ...
     *    Thus the main requirement of the split is that the localname component must be a legal XML NCName.
     *    This means it must start with a letter or _ character and can only contain limited punctuation.
     *    In particular, they can't contain spaces, but then spaces are not legal in URI references anyway.
     *    In general, it is best to not use the localname split to encode any information, you should only
     *    be concerned with it if you are coding a parser or writer.
     * Example: 4WheeledCar ~> WheeledCar
     *    We register a wrong XML NCName to sort the types correctly in the KB!
     */
    public Vector validateLocalName() 
    {
        Vector warnings = new Vector();
        boolean VALIDATION_STATUS = true;
        if(!this.ontclass.getURI().contains("#")) {            
            System.out.println("[e] error identifying namespace and local name (no # separator)");
            warnings.add("No legal local name because no # separator found.");
        }
        
        int idx = 0;
        if(this.ontclass.getURI().contains("#")) {
            idx=this.ontclass.getURI().indexOf("#")+1;
        }
        char first = this.ontclass.getURI().charAt(idx);
        char[] checklist = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };        
        
        for(int i=0; i<checklist.length; i++) {
            if(first == checklist[i]) {                
                warnings.add("Invalid XML NCName. It begins with: \""+first+"\" See http://jena.sourceforge.net/jena-faq.html Q. Why does the localname part of my URI look wrong?");
            }
        }
        return warnings;
    }        
    
    public String getLocalName() {
        if(!this.ontclass.getURI().contains("#")) {            
            System.out.println("[e] error identifying namespace and local name (no # separator)");
            return this.ontclass.getURI();
        }
        else {
            return this.ontclass.getURI().split("#")[1];
        }
        //return this.ontclass.getLocalNamecalName();
    } 
    public String getName() { return this.ontclass.getURI(); }  
    public String getRDFSComment() { return this.RDFS_Comment; }
    public String getOWLVersionInfo() { return this.OWL_VersionInfo; }
    
    //
    // Properties
    //
    public void addProperty(DatatypeProperty p) {
        //System.out.println("[i] add DATATYPE property "+p.getLocalName());
        this.propertySet.add(p);
        try {
            for(NodeIterator nit = this.getOntClass().listPropertyValues(p); nit.hasNext(); ) {
                this.constantMap.put(p.getURI(), nit.next().toString());
            }
        } catch(com.hp.hpl.jena.ontology.ConversionException ce) {
            System.err.println("[e] addProperty, listPropertyValues: "+ce.getMessage());
        }

    }    
    public void addProperty(ObjectProperty p) {
        //System.out.println("[i] add OBJECT property "+p.getLocalName());
        this.propertySet.add(p);
        try {
            for(NodeIterator nit = this.getOntClass().listPropertyValues(p); nit.hasNext(); ) {
                this.constantMap.put(p.getURI(), nit.next().toString());
            }
        } catch(com.hp.hpl.jena.ontology.ConversionException ce) {
            System.err.println("[e] addProperty, listPropertyValues: "+ce.getMessage());
        }
    }    
    public void addProperty(OntProperty p) {
        //System.out.println("[i] add Ont property "+p.getLocalName());
        this.propertySet.add(p);
    }
    
    // OntProperty p
    // p.addRange(OntClass)
    // file:///c:/lib/jena24/doc/javadoc/index.html:
    // Note that OntClass is a Java sub-class of Resource, so OntClass objects can be passed directly.

    public int     getPropertyCount() {
        return this.propertySet.size();
    }
    public HashSet getProperties() {
        return this.propertySet;
    }    
    public HashSet getDatatypeProperties() {
        HashSet datatypeSet = new HashSet();
        Iterator p_it = this.propertySet.iterator();
        while(p_it.hasNext()) {
            OntProperty prop = (OntProperty)p_it.next();
            if(prop.isDatatypeProperty()) {
                datatypeSet.add(prop);
            }
        }
        return datatypeSet;
    }
    public HashSet getObjectProperties() {
        HashSet objectPropertySet = new HashSet();
        Iterator p_it = this.propertySet.iterator();
        while(p_it.hasNext()) {
            OntProperty prop = (OntProperty)p_it.next();
            if(prop.isObjectProperty()) {
                objectPropertySet.add(prop);
            }
        }
        return objectPropertySet;
    }    
    
    public void addConstant(String propertyUri, String value) {
        this.constantMap.put(propertyUri, value);
    }
    
    /**
     * parsed from rdf:type 
     */
    public void addTypeClass(OntClass c) {
       System.out.println("[i] add rdf:type "+c.getLocalName()+" to container."); 
       this.typeClassList.add(c);
    }
    
    public Vector getTypeClassList() { 
        return this.typeClassList; 
    }
    
    public Vector getTypeClassUriList() {
        Vector uriList = new Vector();
        for(Iterator it = this.typeClassList.iterator(); it.hasNext(); ) {
            uriList.add(((OntClass)it.next()).getURI());
        }
        return uriList;
    }
    
    public void addSuperClass(OntClass c) {
        //System.out.println("[i] add OntSuperClass "+c.getLocalName());
        this.superClassSet.add(c.getURI()); //c.getNameSpace()+c.getLocalName());
    }
    public HashSet getSuperClasses() {
        return this.superClassSet;
    }
    
    //
    // rdfs:subClassOf _________________________________________________________
    //
    
    public void addSubClassUnionClass(OntClass c) {
        if(c.isRestriction()) {
            System.out.println("[i] add (subClassOf) UnionClass Restriction");
            owlHelper.printRestriction(c.asRestriction());
        }
        else {
            System.out.println("[i] add (subClassOf) UnionClass "+c.getLocalName());
        }
        this.subclassUnionSet.add(c); //.getURI());
    }
    public HashSet getSubClassUnionClassSet() {
        return this.subclassUnionSet;
    }
    
    public void    addSubClassIntersectionClass(OntClass c) {
        System.out.println("[i] add (subClassOf) IntersectionClass "+c.getLocalName());
        this.subclassIntersectionSet.add(c.getURI());
    }
    public HashSet getSubClassIntersectionClassSet() {
        return this.subclassIntersectionSet;
    }
    
    public void    addSubClassComplementClass(OntClass c) {
        System.out.println("[i] add (subClassOf) ComplementClass "+c.getLocalName());
        this.subclassComplementSet.add(c.getURI());
    }
    public HashSet getSubClassComplementClassSet() {
        return this.subclassComplementSet;
    }
    
    public void    addSubClassEnumerationClass(Individual i) {
        System.out.println("[i] add (subClassOf) Enumeration Individual (OneOf) "+i.getLocalName());
        this.subclassEnumerationSet.add(i.getURI());
    }
    public HashSet getSubClassEnumerationClassSet() {
        return this.subclassEnumerationSet;
    }    
    
    public void   addSubClassPropertyRestriction(Restriction curr) 
    {
        System.out.println("[i] addSubClassPropertyRestriction");
        owlHelper.printRestriction(curr);
        if(this.subclassPropertyRestrictions.contains(curr)) {
            System.out.println("[!] Restricttion already collected.");
        }
        else {
            this.subclassPropertyRestrictions.add(curr);
        }
        
// OF, 23.10.: Änderung. Properties werden ausschließlich über collect Methoden
//        im DatatypeParser hinzugefügt. Hier wird nur der Wertebereich
//        über Restrictions eingeschränkt. Bsp. locatedIn als TransitiveProperty
//
//        if(ontproperty.isDatatypeProperty()) {
//            this.addProperty(ontproperty.asDatatypeProperty());
//            // BaseDatatype bdt = new BaseDatatype(avfr.getAllValuesFrom().asNode().toString());
//            // System.out.println("BDT:"+bdt.toString());
//        }
//        else if(ontproperty.isObjectProperty()) {
//            this.addProperty(ontproperty.asObjectProperty());
//        }
//        else if(ontproperty.isFunctionalProperty()) {
//            System.out.println("FUNCTIONAL PROPERTY!");
//        }
//        //... weitere property types...
    }
    
    public Vector getSubClassPropertyRestrictions() {
        return this.subclassPropertyRestrictions;
    }
    
    //
    // owl:equivalentClass _____________________________________________________
    //
    
    public void    addEquivalentOntResource(OntResource ontRes) {
        System.out.println("[i] add (equivalent) Enumeration Resource (OneOf): "+ontRes.asIndividual().getURI()+" ("+ontRes.asIndividual().getRDFType().getURI()+")");
        this.equivalentEnumerationSet.add(ontRes);
    }
    public HashSet getEquivalentEnumerationSet() {
        return this.equivalentEnumerationSet;
    }  
    
    public void    addEquivalentUnionClass(OntClass c) {
        System.out.println("[i] add (equivalent) UnionClass "+c.getLocalName());
        this.equivalentUnionSet.add(c); //.getURI());
    }
    public HashSet getEquivalentUnionClassSet() {
        return this.equivalentUnionSet; 
    }
    
    public void    addEquivalentIntersectionClass(OntClass c) {
        if(c.isRestriction()) {
            System.out.println("[i] add (equivalent) Restriction");
            owlHelper.printRestriction(c.asRestriction());
        }
        else {
            System.out.println("[i] add (equivalent) Class "+c.getLocalName());
        }
        this.equivalentIntersectionSet.add(c); //.getURI());
    }
    public HashSet getEquivalentIntersectionSet() {
        return this.equivalentIntersectionSet; 
    }
    
    public void    addEquivalentComplementClass(OntClass c) {
        if(c.asComplementClass().getOperand().isRestriction()) {
            System.out.println("[i] add (equivalent) ComplementClass as Restriction ");
            owlHelper.printRestriction(c.asComplementClass().getOperand().asRestriction());
        }
        else {
            System.out.println("[i] add (equivalent) ComplementClass "+c.getLocalName());
        }
        this.equivalentComplementSet.add(c); //.getURI());
    }
    public HashSet getEquivalentComplementClassSet() {
        return this.equivalentComplementSet;
    }    
    
    public void   addEquivalentPropertyRestriction(Restriction curr) {
        this.equivalentPropertyRestrictions.add(curr);
    }
    public Vector getEquivalentPropertyRestrictions() {
        return this.equivalentPropertyRestrictions;
    }
    
    //
    // Individuals
    //
    public void addIndividual(Individual individual) {
        this.individualList.add(individual);
    }
    public void addIndividuals(Vector list) {
        this.individualList.addAll(list);
    }
    public void setIndividualList(Vector list) {
        this.individualList = list;
    }    
    public Vector getIndividualList() {
        return this.individualList;
    }     
    public Vector getIndividualUriList() {
        Vector uriList = new Vector();
        for(Iterator it=this.individualList.iterator(); it.hasNext(); ) {
            uriList.add( ((Individual)it.next()).getURI() );
        }
        return uriList;
    }
    
    /**
     * Additional RDF Type Information
     */
    public HashMap getIndividualUriMap() {
        HashMap uriMap = new HashMap();
        for(Iterator it=this.individualList.iterator(); it.hasNext(); ) {
            Individual i = (Individual)it.next();
            uriMap.put(i.getURI(), i.getRDFType().getURI());
        }
        return uriMap;
    }
    
    public Vector getIndividualRDFTypes() {
        Vector rangeTypes = new Vector();
        for(Iterator it=this.individualList.iterator(); it.hasNext(); ) {
            Individual i = (Individual)it.next();
            if(!rangeTypes.contains(i.getRDFType().getURI())) {
                rangeTypes.add(i.getRDFType().getURI());
            }
        }
        return rangeTypes;
    }
    
    public HashMap getConstantMap() {
        return this.constantMap;
    }
    
    //
    // Additional parse information
    //
    public Vector getParseInformation() { return this.parseInformation; }
    public void addParseInformation(String info) {
        this.parseInformation.add(info);
    }
    
    public String toString() {
        return this.getLocalName()+" ("+this.getClass().getName()+")";
    }
    
    public void showData() 
    {
        System.out.println("[showData] Container: "+this.ontclass.getURI());//getLocalName());
        
        Iterator typeIt = this.getTypeClassList().iterator();
        while(typeIt.hasNext()) {
            System.out.println("[showData] RDF Type: "+typeIt.next().toString());
        }        
        
        if(this.individualList.size() > 0) {
            System.out.println("[showData] "+this.individualList.size()+" registered individuals.");
            for(Iterator it=this.individualList.iterator(); it.hasNext(); ) {
                System.out.println("[showData] Individual: "+((Individual)it.next()).getURI());
            }
        }
        
        Iterator sc_it = this.getSuperClasses().iterator();            
        while(sc_it.hasNext()) {
            System.out.println("[showData] SuperClass: "+sc_it.next().toString());
        }
        
        Iterator sc_union_it = this.getSubClassUnionClassSet().iterator();            
        while(sc_union_it.hasNext()) {
            System.out.println("[showData] SuperClass Union: "+ sc_union_it.next().toString());
        }
        
        Iterator sc_is_it = this.getSubClassIntersectionClassSet().iterator();
        while(sc_is_it.hasNext()) {
            System.out.println("[showData] SuperClass Intersection: "+sc_is_it.next().toString());
        }
        
        Iterator sc_c_it = this.getSubClassComplementClassSet().iterator();
        while(sc_c_it.hasNext()) {
            System.out.println("[showData] SuperClass Complement: "+sc_c_it.next().toString());
        }
        

        Iterator e_enum_it = this.equivalentEnumerationSet.iterator();
        while(e_enum_it.hasNext()) {
            OntResource ontRes = (OntResource)e_enum_it.next();
            System.out.println("[showData] Equivalent Enumeration Individual: "+ ontRes.asIndividual().getURI());
        }

        
        Iterator e_is_it = this.getEquivalentIntersectionSet().iterator();
        while(e_is_it.hasNext()) {
            OntClass curclass = (OntClass)e_is_it.next();
            String restrictionFlag = "";
            if( curclass.isRestriction() ) {
                restrictionFlag = " [R]";
            }
            
            if( curclass.isComplementClass() && curclass.asComplementClass().getOperand().isRestriction()) {
                restrictionFlag = " [CR]";
            }
            
            System.out.println("[showData] Equivalent Intersection: "+curclass.toString()+restrictionFlag);
        }
        
        Iterator e_c_it = this.getEquivalentComplementClassSet().iterator();
        while(e_c_it.hasNext()) {
            OntClass curclass = (OntClass)e_c_it.next();
            String restrictionFlag = "";            
            if(curclass.asComplementClass().getOperand().isRestriction()) {
                System.out.println("[showData] Equivalent Complement Restriction");
                owlHelper.printRestriction(curclass.asComplementClass().getOperand().asRestriction());
            }
            else {
                System.out.println("[showData] Equivalent Complement: "+curclass.toString()+restrictionFlag);
            }
        } 
        
        Iterator pit = this.getProperties().iterator();            
        while(pit.hasNext()) 
        {
            OntProperty prop = (OntProperty)pit.next();
            String propName  = prop.getLocalName();
                    
            String cur_range = "n/a";           
            if(prop.getRange()!=null) {
                cur_range = prop.getRange().getURI(); // prop.getRange().getNameSpace()+prop.getRange().getLocalName();
                if(prop.getRange().asClass().isUnionClass()) {
                    cur_range = "UNION";
                }
            }
            if(prop.isDatatypeProperty()) {
                System.out.println("[showData] DT-Property: "+prop.getLocalName()+" vom Typ "+cur_range); //+", cardinality: "+this.getCardinality(prop.getLocalName())); //+", ns:"+cur_dtp.getNameSpace());               
            }
            else if(prop.isObjectProperty()) {
                System.out.println("[showData] OBJ-Property: "+propName+" vom Typ "+cur_range); //+", [Cardinality:"+this.getCardinality(propName)+", min:"+this.getMinCardinality(propName)+", max:"+this.getMaxCardinality(propName)+"]");
            }
            else {
                System.out.println("Prop: "+prop.getClass().getSimpleName());                    
            }
            
            if(this.constantMap.containsKey(prop.getURI())) {
                System.out.println("[showData] Constant value: "+this.constantMap.get(prop.getURI()));
            }
        }
        
        Vector rlist = new Vector();
        rlist.addAll(this.getSubClassPropertyRestrictions());
        rlist.addAll(this.getEquivalentPropertyRestrictions());
        
        Iterator rit = rlist.iterator();
        while(rit.hasNext()) 
        {
            Restriction r = (Restriction)rit.next();
            owlHelper.printRestriction(r);
        }

    }
   
}
