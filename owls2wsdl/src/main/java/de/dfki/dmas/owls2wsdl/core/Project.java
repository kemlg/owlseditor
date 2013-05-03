/*
 * Project.java
 *
 * Created on 2. Januar 2007, 18:40
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

import de.dfki.dmas.owls2wsdl.parser.DatatypeParser;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;


/**
 * Project class to associate datatypes to services.
 * @author Oliver Fourman
 */
public class Project implements java.io.Serializable {
    
    public static final String DEFAULT_PROJECT_XSDTYPE = "http://www.w3.org/2001/XMLSchema#string";
       
    private static ServiceParser  serviceParser = new ServiceParser();
    
    /**
     * filename of project
     */
    public String    _projectName;
    private Date     _modificationTime;
    
    private String   _defaultXsdType;
    private int      _elementDepth; // for xsd generation
    private String   _typeInheritanceBehaviour; // for xsd generation
            
    public ArrayList _dependenciesFromOwlsImportSection;  // OWL Files
    public String    _services_xml;
    public String    _datatypes_xml;
    
    private AbstractServiceCollection _serviceCollection;
    private AbstractDatatypeKBData    _datatypeKBData; //extra Singleton class!
    
    // private Vector  _ontologyList;  // loaded/imported ontologies
    
    private Vector  _serviceDependencyTypes; // all types for loaded services
    private Vector  _serviceMissingTypes; // all mssing types for loaded services
    
    /** Creates a new instance of Project */
    public Project() {        
        this._dependenciesFromOwlsImportSection = new ArrayList();       
        this._serviceCollection = new AbstractServiceCollection();
        this._modificationTime = new java.util.Date();
        this._elementDepth = Integer.parseInt(OWLS2WSDLSettings.getInstance().getProperty("depth"));
        this._typeInheritanceBehaviour = "None";
        this._defaultXsdType = this.DEFAULT_PROJECT_XSDTYPE;
        //this._ontologyList = new Vector();
        this._serviceDependencyTypes = new Vector();
        this._serviceMissingTypes = new Vector();
    }
    
    public Project(String name) {
        this();
        this._projectName = name;
        this.determineAllDependecyTypes();
    }
    
    public void setProjectName(String name) { this._projectName = name; }
    public void setModificationTime(Date now) {
        now = new java.util.Date();
        this._modificationTime = now;
    }
    public void setDefaultXsdType(String typeString) {
        this._defaultXsdType = typeString;
    }
    public void setElementDepth(int depth) { this._elementDepth = depth; }
    public void setTypeInheritanceBehaviour(String behaviour) { this._typeInheritanceBehaviour = behaviour; }
    public void setAbstractServiceCollection(AbstractServiceCollection serviceCollection) {
        this._serviceCollection = serviceCollection;
    }
    public void setAbstractDatatypeKBData(AbstractDatatypeKBData abstractDatatypeKBData) {        
        AbstractDatatypeKB.getInstance().setAbstractDatatypeKBData(abstractDatatypeKBData);
    }
//    public void setLoadedOntologies(Vector ontologyList) {
//        this._ontologyList = ontologyList;
//    }
    
    public String getProjectName() { return this._projectName; }
    public Date getModificationTime() { return this._modificationTime; }
    public String getDefaultXsdType() { return this._defaultXsdType; }
    public int getElementDepth() { return this._elementDepth; }
    public String getTypeInheritanceBehaviour() { return this._typeInheritanceBehaviour; }
        
    public XsdSchemaGenerator buildXsdSchemaGenerator() {
        boolean useHierarchyPattern = false;
        if(OWLS2WSDLSettings.getInstance().getProperty("xsdgen").equals("hierarchy")) {
            useHierarchyPattern = true;
        }
        XsdSchemaGenerator xsdgen = new XsdSchemaGenerator(
                "WSDL",
                useHierarchyPattern,
                this.getElementDepth(),
                this.getTypeInheritanceBehaviour(),
                this.getDefaultXsdType());
        
        if(OWLS2WSDLSettings.getInstance().getProperty("annotations").equals("yes")) {
            xsdgen.enableAnnotations();
        }
        if(OWLS2WSDLSettings.getInstance().getProperty("owlinfo").equals("yes")) {
            xsdgen.enableOwlInformation();
        }
            
        return xsdgen;
    }
    
    public AbstractServiceCollection getAbstractServiceCollection() {
        return this._serviceCollection;
    }
    public AbstractDatatypeKBData getAbstractDatatypeKBData() {
        return AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData();
    }
//    public Vector getOntologyList() {
//        return this._ontologyList;
//    }
    
//    public void addOntologyUrl(String url) {
//        this._ontologyList.add(url);
//    }
    
    public Vector getServiceDependencyTypes() {
        return this._serviceDependencyTypes;
    }
    
    public Vector getServiceMissingTypes() {
        return this._serviceMissingTypes;
    }
    
    public void importServices(String path) throws Exception {
        this._serviceCollection.addAbstractServiceList(serviceParser.parse(path));
    }
    
    public void importServices(File file) {
        try {
            this._serviceCollection.addAbstractServiceList(serviceParser.parse(file.toURI()));
        }
        catch(Exception e) {
            System.out.println("Could not parse "+file.toString()+"!");
            e.printStackTrace();
        }
    }
    
    public void importServices(File[] flist) {
        for(int i=0; i<flist.length; i++) {
            System.out.println(i+". Importing "+flist[i].getAbsolutePath());  
            this.importServices((File)flist[i]);
        }
        this._serviceCollection.sortData();
    }
    
    public void importDatatypes(File f) {
        DatatypeParser ontParser = new DatatypeParser();
        try {
            ontParser.parse(f);
            ontParser.getAbstractDatatypeKBData();
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }

    public void importDatatypes(String url) {
        DatatypeParser ontParser = new DatatypeParser();
        try {
            ontParser.parse(url);
            ontParser.getAbstractDatatypeKBData();
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Check and search KB for undefined datatypes.
     * That means, we collect parent, intersection and rdfs types, that are
     * not already in KB to load them form persistent storage.
     */
    public Vector collectUndefinedDatatypes() {
        Vector dtypes = new Vector();
        for(Iterator it=this._serviceCollection.getParameterTypes().iterator(); it.hasNext(); ) {
            String uri = it.next().toString();
            if(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(uri)) {
                System.out.println("[i] Already found URI, check dependencies: "+uri);
            }
            else {
                System.out.println("[i] Undefined Datatype: "+uri);
            }
        }
        return dtypes;
    }
    
    /**
     */
    public void determineAllDependecyTypes() 
    {   
        this._serviceDependencyTypes.removeAllElements();
        this._serviceMissingTypes.removeAllElements();
        
        for(Iterator paramIt=this._serviceCollection.getParameterTypes().iterator(); paramIt.hasNext(); ) 
        {
            String uri = paramIt.next().toString();
            System.out.println("\nCURRENT PARAM: "+uri);
            if(this._serviceDependencyTypes.contains(uri)) {
                System.out.println("_> aleady processed.");
            }
            else {
                HashMap tempTypes = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().collectAllDependencyTypes(uri);
                for(Iterator it=tempTypes.keySet().iterator(); it.hasNext(); ) {
                    String typeKey = it.next().toString();                    
                    if(!this._serviceDependencyTypes.contains(typeKey)) {
                        this._serviceDependencyTypes.add(typeKey);
                    }
                    
                    if(tempTypes.get(typeKey).equals("0")) {
                        if(!this._serviceMissingTypes.contains(typeKey)) {
                            this._serviceMissingTypes.add(typeKey);
                        }
                    }
                }
            }
        }        
        java.util.Collections.sort(_serviceDependencyTypes);
        java.util.Collections.sort(_serviceMissingTypes);
    }
    
    /**
     * Removes all datatypes that don't belong to a service definition.
     */
    public void removeUnreferencedDatatypes() {
        Vector redlist = new Vector();
        for(Iterator it=AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getRegisteredDatatypes().keySet().iterator(); it.hasNext(); ) {
            String key = it.next().toString();
            if(this.getServiceDependencyTypes().contains(key)) {
                System.out.println("REMAIN TYPE: "+key);
            }
            else {
                redlist.add(key);
                System.out.println("REMOVE TYPE: "+key);
            }
        }
        for(Iterator it=redlist.iterator(); it.hasNext(); ) {
            String key = it.next().toString();
            AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().removeRegisteredDatatype(key);
        }
    }
    
//    /**
//     * Sets _valid attribute of each ServiceParameter (input/output), because
//     * this is not saved in project file.
//     * @param   AbstractService object
//     * @return  void
//     */
//    public void processServiceParameterValidation(AbstractService service) 
//    {        
//        for(Iterator it=service.getInputParameter().iterator(); it.hasNext(); ) {
//            AbstractServiceParameter param = (AbstractServiceParameter)it.next();
//            if(AbstractDatatypeKB.getInstance().data.containsKey(param.getUri())) {
//                param._valid = true;
//            }
//        }
//        for(Iterator it=service.getOutputParameter().iterator(); it.hasNext(); ) {
//            AbstractServiceParameter param = (AbstractServiceParameter)it.next();
//            if(AbstractDatatypeKB.getInstance().data.containsKey(param.getUri())) {
//                param._valid = true;
//            }
//        }        
//    }
//    
//    /**
//     * Process validation of service parameter for all project services.
//     */
//    public void processServiceParameterValidation() {
//        for(Iterator it=this._serviceCollection.getServiceCollection().iterator(); it.hasNext(); ) {
//            AbstractService service = (AbstractService)it.next();
//            System.out.println("CHECK SERVICE "+service.toString());
//            this.processServiceParameterValidation(service);
//        }
//    }
    
    public void save(File f) throws java.io.FileNotFoundException {
        this._datatypeKBData = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData();
        FileOutputStream ausgabeStream = new FileOutputStream(f);
        ProjectManager.getInstance().mapProject(this, ausgabeStream, true);
    }
    
    public static void main(String args[]) {
        for(int i=0; i<args.length; i++) {
            System.out.println("ARG "+i+": "+args[i]);
        }
        
        String arg = "save";
        
        //if(args.length >0 && args[0].equals("save")) {
        if(arg.equals("save")) {
            Project project = new Project("Testprojekt Student");
            AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_Student-MAP.xml");
            try {
                project.save(new File("D:\\tmp\\Project_1.xml"));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        
        
//        //project.importServices("abc");
//        String service_1 = "D:\\htw_kim\\thesis\\OWLS-MX\\owls-tc2\\domains\\1.1\\Weapon\\ballisticmissilegovernment_fundingrange_service.owls";
//        String service_2 = "file:///D:/htw_kim/thesis/OWLS-MX/owls-tc2/queries/1.1/car_price_service.owls";
//        try {
//            project.importServices(service_1);
//            project.importServices(service_2);
//        }
//        catch(Exception e) {
//            System.err.println("FEHLER !");
//            e.printStackTrace();
//        }
//        project.getAbstractServiceCollection().printFullData();
//        
//        //AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_Wine-neu2.xml");
//        
//        project.importDatatypes("http://localhost/ontology/wine.owl");
//        
//        project.setAbstractDatatypeKBData( AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData() );
//        
//        //ProjectManager.getInstance().mapProject(project, System.out, true);
    }
}


