/**
 * StudentInformationServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.dfki.dmas.axis.services.StudentInformationService;

public class StudentInformationServiceSoapBindingSkeleton implements de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1, org.apache.axis.wsdl.Skeleton {
    private de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1 impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getInformation", _params, new javax.xml.namespace.QName("", "return"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "Student"));
        _oper.setElementQName(new javax.xml.namespace.QName("getInformation", "getInformation"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getInformation") == null) {
            _myOperations.put("getInformation", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getInformation")).add(_oper);
    }

    public StudentInformationServiceSoapBindingSkeleton() {
        this.impl = new de.dfki.dmas.axis.services.StudentInformationService.StudentInformationServiceSoapBindingImpl();
    }

    public StudentInformationServiceSoapBindingSkeleton(de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1 impl) {
        this.impl = impl;
    }
    public de.dfki.dmas.axis.services.StudentInformationService.Student getInformation(java.lang.String in0) throws java.rmi.RemoteException
    {
        de.dfki.dmas.axis.services.StudentInformationService.Student ret = impl.getInformation(in0);
        return ret;
    }

}
