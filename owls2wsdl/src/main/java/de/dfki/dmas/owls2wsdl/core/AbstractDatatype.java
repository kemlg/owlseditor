/*
 * AbstractDatatype.java
 *
 * Created on 4. September 2006, 15:31
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

package de.dfki.dmas.owls2wsdl.core;

import de.dfki.dmas.owls2wsdl.parser.OntClassContainer;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;

// XSD creation
import org.exolab.castor.xml.schema.*;


/**
 * Raw XSD datatype information. Class without jena libs.
 * @author Oliver Fourman
 */
public class AbstractDatatype implements java.io.Serializable 
{
    private String  _localname;
    private String  _url;
    private String  _RDFSComment;
    private String  _OWLVersionInfo;
    private String  _XSDType;    // Edited with OWLS2WSDL Gui
    
    public static final String DEFAULT_XSDTYPE = "http://www.w3.org/2001/XMLSchema#anyType";   
    public static final String InheritanceByNone = "None";
    public static final String InheritanceByRDFTypeOnly = "RDF Type only";
    public static final String InheritanceBySuperClassOnly = "SuperClass only";
    public static final String InheritanceByRDFTypeFirstParentsSecond = "RDF Type first, SuperClass second";
    public static final String InheritanceByParentsFirstRDFTypeSecond = "SuperClass first, RDF Type second";
    
    // enumeration values (came from owl-instances)
    private HashMap _individualRange; // 5.2.2007: HashMap used to save the Types
    
    // parent information
    private Vector  _parentList;
    
    // rdf:type information, used for identify owl-individuals
    private Vector  _typeList;
    
    // intersection classes/types
    private Vector  _intersectionList;
       
    // sub elements
    private Vector  _properties; // type: AbstractDatatypeElement
        
    // jena exceptions and warnings
    private Vector  _errorMessages;
    
    // owl/rdfs depending deduction information
    private Vector  _owlParseMessages;
    
    // xsd creation
    private Schema schema;
    //private AbstractDatatypeKBData ref2KBDATA; obsolete, weil AbstractDatatypeKB Singleton
        
    
    /** Creates a new instance of AbstractDatatype */
    public AbstractDatatype() {
//        System.out.println("[C] AbstractDatatype_1");
        this._individualRange = new HashMap();
        this._parentList = new Vector();
        this._typeList = new Vector();
        this._intersectionList = new Vector();
        this._properties = new Vector();
        this._errorMessages = new Vector();
        this._owlParseMessages = new Vector();
        
        this.schema = new Schema("xsd",Schema.DEFAULT_SCHEMA_NS);
        this.schema.setId(this.getLocalName());
        java.util.Date now = new java.util.Date();
        this.schema.setVersion("OWLS2WSDL "+now);
        this.schema.addNamespace("tns", "http://schemas.dmas.dfki.de/venetianblind");
    }
        
    public AbstractDatatype(OntClassContainer oc_con) {
        this();
        System.out.println("[C] AbstractDatatype_2");
        this._localname      = oc_con.getLocalName();
        this._url            = oc_con.getName();
        this._RDFSComment    = oc_con.getRDFSComment();
        this._OWLVersionInfo = oc_con.getOWLVersionInfo();
        this._parentList.addAll(oc_con.getSuperClasses());
        
        if(!oc_con.getOntClass().getURI().contains("#")) {
            this.addParseMessage("OntClass ID doesn't contain # seperator");
        }
    }
    
    /*
     * Copy-Constructor for XSD generation: restricted subtypes
     */
    public AbstractDatatype(AbstractDatatype type) {        
        this();
        System.out.println("[C] Copy Constructor AbstractDatatype");
        
        this._individualRange.putAll(type.getIndividualRange());
        this._parentList.addAll(type.getParentList());
        this._typeList.addAll(type.getTypeList());
        this._intersectionList.addAll(type.getIntersectionList());
        this._properties.addAll(type.getProperties());
        this._errorMessages.addAll(type.getErrorMessages());
        this._owlParseMessages.addAll(type.getParseMessages());
        
        this._localname            = type.getLocalName();
        this._url                  = type.getUrl();
        this._RDFSComment          = type.getRdfsComment();
        this._OWLVersionInfo       = type.getOwlVersionInfo();
        this._XSDType              = type.getXsdType();
    }
    
    public String  getLocalName() { return this._localname; }
    public String  getUrl() { return this._url; }
    public String  getRdfsComment() { return this._RDFSComment; }
    public String  getOwlVersionInfo() { return this._OWLVersionInfo; }
    public String  getXsdType() { return this._XSDType; }
    public HashMap getIndividualRange() { return this._individualRange; }
    public Vector  getParentList() { return this._parentList; }
    /**
     * @return Vector of owl-individuals
     */
    public Vector  getTypeList() { return this._typeList; }
    public Vector  getIntersectionList() { return this._intersectionList; }
    
    /**
     * Get all inherited properties.
     */
    public Vector  getProperties() { return this._properties; }
    public Vector  getErrorMessages() { return this._errorMessages; }
    public Vector  getParseMessages() { return this._owlParseMessages; }
    
    /**
     * Restrict the properties by given inheritance level.
     * @param maxDepth Inheritance level
     */
    public Vector  getProperties(int maxDepth) {
        if(maxDepth == -1) {
            return this._properties;
        }
        else {
            Vector filteredProperties = new Vector();
            for(Iterator it=this._properties.iterator(); it.hasNext(); ) {
                AbstractDatatypeElement elem = (AbstractDatatypeElement)it.next();
                if(elem.computeLevel()<=maxDepth) {
                    filteredProperties.add(elem);
                }
            }
            return filteredProperties;
        }
    }
    
    /**
     * Checks if Name is a valid NCName.
     * See comment for OntClassContainer.getOntClass(..)
     */
    public boolean hasValidNCName() {
        int idx = 0;
        if(this._localname.contains("#")) {
            idx=this._localname.indexOf("#")+1;
        }
        char first = this._localname.charAt(idx);
        char[] checklist = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };        
        
        for(int i=0; i<checklist.length; i++) {
            if(first == checklist[i]) {
                return false;
            }
        }
        return true;
    }
    
    
    /*
    public Vector  getPropertiesMod() {
        System.out.println("getPropertiesMod");
        if(!this._propertiesMod.isEmpty()) {
            System.out.println("+");
            return this._propertiesMod;
        }
        Vector templist = new Vector();
        templist.addAll(this._properties.values());
        return templist;
    }*/
    
    public void setLocalName(String localname) { this._localname = localname; }
    public void setUrl(String url) { this._url = url; }
    public void setRdfsComment(String comment) { this._RDFSComment = comment; }
    public void setOwlVersionInfo(String versioninfo) { this._OWLVersionInfo = versioninfo; }
    public void setXsdType(String xsdtype) { this._XSDType = xsdtype; }
    public void setIndividualRange(HashMap individualRange) { this._individualRange = individualRange; }
    public void setParentList(Vector parentList) { this._parentList = parentList; }
    public void setTypeList(Vector typeList) { this._typeList = typeList;}
    public void setIntersectionList(Vector intersectionList) { this._intersectionList = intersectionList; }
    public void setProperties(Vector properties) { this._properties = properties; }
    public void setErrorMessages(Vector errorMessages) { this._errorMessages = errorMessages; }
    public void setParseMessages(Vector parseMessages) { this._owlParseMessages = parseMessages; }
    
    /*
    public void setPropertiesMod(Vector propertyList) {
        System.out.println("setPropertiesMod");
        this._properties.clear();
        for(Iterator it=propertyList.iterator(); it.hasNext(); ) {
            this._properties.put(((AbstractDatatypeElement)it.next()).getName(),it.next());
        }        
    }*/
    
    public void addErrorMessage(String msg) { this._errorMessages.add(msg); }
    public void addParseMessage(String msg) { 
        this._owlParseMessages.add(msg); 
    }
    public void addAllParseMessages(Vector parseMessages) {
        this._owlParseMessages.addAll(parseMessages);
    }
    
    /**
     * Add individual.
     * @param uriID id or about attribute of individual (oneOf)
     * @param uriType RDF Type of the collection element (oneOf)
     */
    public void addIndividualRange(String uriID, String uriType) { 
        this._individualRange.put(uriID, uriType); 
    }
    
    public void addRangeList(HashMap rangeList) { 
        //this._individualRange.addAll(rangeList); 
        for(Iterator it=rangeList.keySet().iterator(); it.hasNext(); ) {
            String key = it.next().toString();
            this._individualRange.put(key, rangeList.get(key));
        }
    }
    
    public void removeAllRanges() {
        //this._individualRange.removeAllElements();
        this._individualRange.clear();
    }
    
    /**
     * Cause of the fact, that individuals could be subtypes of given type
     * we collect all types of individuals. e.g. Language, SupportedLanguage
     * @return vector with types names
     */
    public Vector getIndividualRangeTypes() {
        Vector rangeTypes = new Vector();
        for(Iterator it=this._individualRange.keySet().iterator(); it.hasNext(); ) {
            String key = it.next().toString();
            if(!rangeTypes.contains(this._individualRange.get(key))) {
                rangeTypes.add(this._individualRange.get(key));
            }
        }
        return rangeTypes;
    }
    
    
    public void addParent(String uri)               { this._parentList.add(uri); }    
    public void addType(String uri)                 { this._typeList.add(uri); }
    
    public void addIntersection(String uri)         { this._intersectionList.add(uri); }
    
    public void addProperty(AbstractDatatypeElement elem) {
        //this._properties.put(elem.getName(), elem);
        this._properties.addElement(elem);
    }
    
    public void removeAllProperties() {
        //this._properties.clear();
        this._properties.removeAllElements();
    }    
    
    public boolean containsProperty(String key) {
        for(Iterator it=this._properties.iterator(); it.hasNext(); ) {
            if( ((AbstractDatatypeElement)it.next()).getName().equals(key) ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * After the re-definition of an AbstractDatatype using the CopyConstructor,
     * removing range values due to a restriction operation and setting a
     * specialiced name for the type (Region ~> BordeauxRegion) use this method
     * to get the original ("base") name.
     * @return localname of url
     **/
    public String getBaseName() {
        String basename = this._url.split("#")[1];
        if(basename.equals("")) {
            basename = this._url;
        }
        return basename;
    }
    
    /**
     * helper method to restrict depth of inheritage
     */
    public int getMinDepthOfProperties() {
        int result = -1;
        for(Iterator it=this._properties.iterator(); it.hasNext(); ) {
            int level = ((AbstractDatatypeElement)it.next()).getLevel();
            if(result == -1) {
                result = level;
            }            
            if(level<result) {
                result=level;
            }
        }
        //System.out.println("getMinDepthOfProperties: "+result);
        return result;
    }
    
//    /**
//     * Check AbstractType if it contains elements of given depth.
//     * @param depth of elements
//     * @return boolean value
//     */
//    public boolean hasElements(int depth) {
//        return (this.getMinDepthOfProperties()<=depth && this.getMinDepthOfProperties() >=0);
//    }
    
    public String getResultingXsdType(int depth) {
        if(this.getProperties().isEmpty()) {
            return "SimpleType";
        }
        else {
            if(this.getMinDepthOfProperties()<=depth) {
                return "ComplexType";
            }
            else {
                return "SimpleType";
            }
        }
    }
    
    public boolean isSimpleType(int depth) {
        if(this.getProperties().isEmpty()) {
            return true;
        }
        else {
            if(this.getMinDepthOfProperties()<=depth) {
                return false;
            }
            else {
                return true;
            }
        }
    }
    
    public boolean isComplexType(int depth) {
        return !isSimpleType(depth);
    }

    // =========================================================================
    // XSD Type determination
    //
    public boolean hasXsdType() {
        return this._XSDType != null;
    }
    
    public String searchInParentsForXsdType(String defaultXsdType) {
        String typeString = defaultXsdType;
        for(Iterator it=this._parentList.iterator(); it.hasNext(); ) {            
            String url = it.next().toString();
            //System.out.println("CHECK "+url);
            if( AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(url) )
            {
                if( AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).hasXsdType() ) {
                    typeString = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).getXsdType();
                    break;
                }
                else if(!AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).searchInRdfTypesForXsdType(defaultXsdType).equals(defaultXsdType)) {
                    typeString = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).searchInRdfTypesForXsdType(defaultXsdType);
                    break;
                }
                else {
                    typeString = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).searchInParentsForXsdType(defaultXsdType);
                }
            }
        }
        return typeString;
    }
    
    public String searchInParentsForXsdType() {
        return this.searchInParentsForXsdType(DEFAULT_XSDTYPE);
    }
    
    public String searchInRdfTypesForXsdType(String defaultXsdType) {
        String typeString = defaultXsdType;
        
        if(this._typeList.size() == 1) {
            if(this._typeList.get(0).toString().equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
                return defaultXsdType;
            }
        }
        
        for(Iterator it=this._typeList.iterator(); it.hasNext(); ) {
            String url = it.next().toString();
            //System.out.println("CHECK: "+url);
            if( AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(url) )
            {
                if( AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).hasXsdType() ) {
                    typeString = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(url).getXsdType();
                }
            }
        }
        return typeString;
    }
    
    public String searchInRdfTypesForXsdType() {
        return this.searchInRdfTypesForXsdType(DEFAULT_XSDTYPE);
    }    
    
    /**
     * Determine xsd primitive type for given registered AbstractDatatype.
     * @param inheritanceBehaviour defined in AbstractDatatype. when declared as null, None inheritance is set.
     * @param DEFAULT_XSDTYPE defined in AbstractDatatype. when declared as null, XSD:anyUri is set.
     * @return uri of primitive xsd datatype
     */
    public String determineXsdType(String inheritanceBehaviour, String DEFAULT_XSDTYPE) 
    {           
        if(this.hasXsdType()) {
            return this.getXsdType();
        }
        else {
            // type doesn't have any user edited primitive type information
            if(inheritanceBehaviour == null) {
                inheritanceBehaviour = AbstractDatatype.InheritanceByNone;
            }
            if(DEFAULT_XSDTYPE == null) {
                DEFAULT_XSDTYPE = AbstractDatatype.DEFAULT_XSDTYPE;
            }

            System.out.println("[i] determineXsdType TypeInheritanceBehaviour: "+inheritanceBehaviour+", DEFAULT_XSDTYPE: "+DEFAULT_XSDTYPE);
            
            if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceByRDFTypeOnly)) {
                return this.searchInRdfTypesForXsdType(DEFAULT_XSDTYPE);
            }
            else if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceBySuperClassOnly)) {
                return this.searchInParentsForXsdType(DEFAULT_XSDTYPE);
            }
            else if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond)) {
                String xsdtype = this.searchInRdfTypesForXsdType(DEFAULT_XSDTYPE);
                if(xsdtype.equals(DEFAULT_XSDTYPE)) {
                    xsdtype = this.searchInParentsForXsdType(DEFAULT_XSDTYPE);
                }
                return xsdtype;
            }
            else if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceByParentsFirstRDFTypeSecond)) {
                String xsdtype = this.searchInParentsForXsdType(DEFAULT_XSDTYPE);
		if(xsdtype.equals(DEFAULT_XSDTYPE)) {
                    xsdtype = this.searchInRdfTypesForXsdType(DEFAULT_XSDTYPE);
                }
                return xsdtype;
            }
            else {
                return DEFAULT_XSDTYPE;
            }
        }
    }
    
    public String determineXsdType(String inheritanceBehaviour) {
        return this.determineXsdType(inheritanceBehaviour, this.DEFAULT_XSDTYPE);
    }
       
    public AbstractDatatypeElement getElement(String key) {
        for(Iterator it=this._properties.iterator(); it.hasNext(); ) {
            AbstractDatatypeElement cur = (AbstractDatatypeElement)it.next();
            if( cur.getName().equals(key) ) {
                return cur;
            }
        }
        return null;
    }
    
  
    
    // === XSD =================================================================
    
//    public String getXSDType() {
//        if(this._properties.isEmpty()) {
//            return "SIMPLE";
//        }
//        else {
//            return "COMPLEX";
//        }
//    }
    
    public String toString() {
        return this._url;
    }
    
    public void printDatatype() {
        System.out.println();
        System.out.println("MEM: "+System.identityHashCode(this));
        System.out.println("Name     : "+this.getLocalName());
        System.out.println("URL      : "+this.getUrl());

        if(!this._errorMessages.isEmpty()) {            
            for(int i=0; i<this._errorMessages.size(); i++) {
                //System.err.println("        Exception: "+((Exception)this._errorMessages.get(i)).getMessage() );
                System.out.println("Error Msg: "+this._errorMessages.get(i).toString());
            }
//            System.out.println("Exception Count: "+this._errorMessages.size());
        }
        if(!this._owlParseMessages.isEmpty()) {
            for(Iterator it=this._owlParseMessages.iterator(); it.hasNext(); ) {
                System.out.println("ParseInformation: "+it.next().toString());
            }
        }
        if(!this._parentList.isEmpty()) {
            for(Iterator it=this._parentList.iterator(); it.hasNext(); ) {
                System.out.println("Parent   : "+ it.next().toString());
            }
        }
        if(!this._typeList.isEmpty()) {
            for(Iterator it=this._typeList.iterator(); it.hasNext(); ) {
                System.out.println("Type(s)  : "+ it.next().toString());
            }
        }        
        if(!this._intersectionList.isEmpty()) {
            for(Iterator it=this._intersectionList.iterator(); it.hasNext(); ) {
                System.out.println("     Intersection: "+ it.next().toString());
            }
        }
        if(!this._individualRange.isEmpty()) {
            for(Iterator it=this._individualRange.keySet().iterator(); it.hasNext(); ) {
                String key = it.next().toString();
                System.out.println("        Range Val: "+key+" ("+this._individualRange.get(key)+")");
            }
        }
        if(!this._properties.isEmpty()) {
            System.out.println("PROPERTIES (ELEMENTS)________________________");
            System.out.println(" * min depth: "+this.getMinDepthOfProperties());
            for(Iterator it=this._properties.iterator();it.hasNext(); ) {
                ((AbstractDatatypeElement)it.next()).printData();
                System.out.println();
            }
        }
        
        if(this.hasXsdType()) {
            System.out.println("XSD-Type : "+this.getXsdType());
        }
        
        if(this.getRdfsComment() != null) {
            System.out.println("Comment: "+this.getRdfsComment());
        }
        if(this.getOwlVersionInfo() != null) {
            System.out.println("Version: "+this.getOwlVersionInfo());
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // XML SCHEMA (XSD) ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public void setSchema(Schema schema) {
        this.schema = schema;
    }        
    
    public Schema getSchema() {        
        return this.schema;
    }
    
    
//    private boolean checkSchema4Type(String typeId) {
//        System.out.println("CHECK checkSchema4Type: "+typeId);
//        
//        Enumeration g_enum = this.schema.getModelGroups();
//        while(g_enum.hasMoreElements()) {
//            System.out.println("checkSchema4Type getModelGroup: "+((Group)g_enum.nextElement()).getParticleCount());
//        }
//        
//        Enumeration e_enum = schema.getElementDecls();
//        while(e_enum.hasMoreElements()) {
//            if( ((ElementDecl)e_enum.nextElement()).getName().equals(typeId) ) {
//                System.out.println("checkSchema4Type getElementDecls: "+typeId);
//                return true;
//            }
//        }
//        
//        Enumeration ct_enum = this.schema.getComplexTypes();
//        while(ct_enum.hasMoreElements()) {
//            if( ((ComplexType)ct_enum.nextElement()).getName().equals(typeId) ){
//                System.out.println("checkSchema4Type getComplexTypes: "+typeId);
//                return true;
//            }
//        }
//        Enumeration st_enum = this.schema.getSimpleTypes();
//        while(st_enum.hasMoreElements()) {
//            if( ((SimpleType)st_enum.nextElement()).getName().equals(typeId) ) {
//                System.out.println("checkSchema4Type getSimpleTypes: "+typeId);
//                return true;
//            }
//        }
//        
//        return false;
//    }
        

    public static void main(String args[]) {
        //AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_SUMO-Map.xml");
        AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_Student2.xml");
        
        //AbstractDatatype atype = AbstractDatatypeKB.getInstance().data.get("file:/D:/htw_kim/thesis/OWLS-MX/owls-tc2/ontology/SUMO.owl#Vitamin");
        //AbstractDatatype atype = AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/Student.owl#Number");
        //AbstractDatatype atype = AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/Student.owl#MyUnitOfMeasure");
        //AbstractDatatype atype = AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/Student.owl#NonnegativeInteger");
        AbstractDatatype atype = AbstractDatatypeKB.getInstance().data.get("http://127.0.0.1/ontology/Student.owl#HTWMasterStudent");
        //atype.printDatatype();
        
        System.out.println("Determinate XSD type-1: "+atype.determineXsdType(AbstractDatatype.InheritanceByNone));
        System.out.println("Determinate XSD type-2: "+atype.determineXsdType(AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond));
        System.out.println("Determinate XSD type-3: "+atype.determineXsdType(AbstractDatatype.InheritanceByParentsFirstRDFTypeSecond));
        
        if(!atype.getProperties(1).isEmpty()) {
            System.out.println("YEP");
        }
        else {
            System.out.println("NOPE, empty");
        }
        try {
            XsdSchemaGenerator gen = new XsdSchemaGenerator(
                    "WSDLTYPE",
                    false,
                    1,
                    AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond,
                    "http://www.w3.org/2001/XMLSchema#string");
            gen.toXSD(atype);
            gen.printXSD(System.out);
        }
        catch(Exception e) {
            System.out.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
    
}
