/**
 * StudentInformationService_1ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.dfki.dmas.axis.services.StudentInformationService;

public class StudentInformationService_1ServiceLocator extends org.apache.axis.client.Service implements de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1Service {

    public StudentInformationService_1ServiceLocator() {
    }


    public StudentInformationService_1ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public StudentInformationService_1ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for StudentInformationService
    private java.lang.String StudentInformationService_address = "http://localhost:8080/axis/services/StudentInformationService";

    public java.lang.String getStudentInformationServiceAddress() {
        return StudentInformationService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String StudentInformationServiceWSDDServiceName = "StudentInformationService";

    public java.lang.String getStudentInformationServiceWSDDServiceName() {
        return StudentInformationServiceWSDDServiceName;
    }

    public void setStudentInformationServiceWSDDServiceName(java.lang.String name) {
        StudentInformationServiceWSDDServiceName = name;
    }

    public de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1 getStudentInformationService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(StudentInformationService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getStudentInformationService(endpoint);
    }

    public de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1 getStudentInformationService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            de.dfki.dmas.axis.services.StudentInformationService.StudentInformationServiceSoapBindingStub _stub = new de.dfki.dmas.axis.services.StudentInformationService.StudentInformationServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getStudentInformationServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setStudentInformationServiceEndpointAddress(java.lang.String address) {
        StudentInformationService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (de.dfki.dmas.axis.services.StudentInformationService.StudentInformationService_1.class.isAssignableFrom(serviceEndpointInterface)) {
                de.dfki.dmas.axis.services.StudentInformationService.StudentInformationServiceSoapBindingStub _stub = new de.dfki.dmas.axis.services.StudentInformationService.StudentInformationServiceSoapBindingStub(new java.net.URL(StudentInformationService_address), this);
                _stub.setPortName(getStudentInformationServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("StudentInformationService".equals(inputPortName)) {
            return getStudentInformationService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "StudentInformationService_1Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "StudentInformationService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("StudentInformationService".equals(portName)) {
            setStudentInformationServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
