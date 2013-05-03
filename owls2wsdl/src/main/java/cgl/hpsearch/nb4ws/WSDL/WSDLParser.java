/*
 * Indiana University Community Grid Computing Lab Software License,Version 1.1
 *
 * Copyright (c) 2002-2006 Community Grid Computing Lab, Indiana University. 
 * All rights reserved.		
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *      "This product includes software developed by the Indiana University 
 *       Community Grid Computing Lab (http://www.communitygrids.iu.edu/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Indiana Univeristy","Indiana Univeristy Pervasive Techonology
 *    Labs" and  "Indiana Univeristy Community Grid Computing Lab" must not be
 *    used to endorse or promote products derived from this software without 
 *    prior written permission. For written permission, please contact 
 *    http://www.communitygrids.iu.edu/.
 *
 * 5. Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * INDIANA UNIVERSITY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created on Feb 8, 2005
 * 
 */
package cgl.hpsearch.nb4ws.WSDL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleTypesFactory;
import org.jdom.input.DOMBuilder;

import cgl.hpsearch.common.objects.XObject;

/**
 * A class that defines methods for building components to invoke a web service by analyzing a WSDL
 * document.
 * 
 * @author Jim Winfield
 */

public class WSDLParser {

    static Logger log = Logger.getLogger("WSDLParser");

    String wsdlURI;

    public List serviceList;

    public TypesParser typesParser;

    /** JWSDL Factory instance */
    WSDLFactory wsdlFactory = null;

    /** Cator simple types factory */
    SimpleTypesFactory simpleTypesFactory = null;

    /** WSDL type schema */
    private Schema wsdlTypes = null;

    public static void main(String[] a) {
        //WSDLParserModified p = new WSDLParserModified("file:///d:/tmp/EADSServiceMod2.wsdl");
        
        WSDLParser p = new WSDLParser("file:/D:/development/netbeans-projects/OWLS2WSDL/src/ModuleInformation.wsdl");
    }

    public WSDLParser(String wsdl) {
        wsdlURI = wsdl;
        typesParser = new TypesParser();

        try {
            wsdlFactory = WSDLFactory.newInstance();
            simpleTypesFactory = new SimpleTypesFactory();

            // Create the in memory model of services and operations defined in the current WSDL

            buildComponents();
        } catch (Exception e) {
            log.error("", e);
        }

    }

    /** The default SOAP encoding to use. */
    public final static String DEFAULT_SOAP_ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";

    /**
     * Builds a List of ServiceInfo components for each Service defined in a WSDL Document. <br>
     * A List of SoapComponent objects populated for each service defined in a WSDL document. The
     * List is null if the document can't be read.
     */
    public void buildComponents() throws Exception {
        // The list of components that will be returned
        serviceList = Collections.synchronizedList(new ArrayList());

        // Create the WSDL Reader object
        WSDLReader reader = wsdlFactory.newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);

        // Read the WSDL and get the top-level Definition object
        Definition def = reader.readWSDL(null, wsdlURI);

        // Create a castor schema from the types element defined in WSDL
        // This method will return null if there are types defined in the WSDL
        wsdlTypes = createSchemaFromTypes(def);
        if (wsdlTypes != null) typesParser.init(wsdlTypes);

        // Get the services defined in the document
        Map services = def.getServices();

        if (services != null) {
            // Create a component for each service defined
            Iterator svcIter = services.values().iterator();

            for (int i = 0; svcIter.hasNext(); i++) {
                // Create a new ServiceInfo component for each service found
                ServiceInfo serviceInfo = new ServiceInfo();

                // Populate the new component from the WSDL Definition read
                populateComponent(serviceInfo, (Service) svcIter.next());

                // Add the new component to the List to be returned
                serviceList.add(serviceInfo);
            }
        }
    }

    /**
     * Creates a castor schema based on the types defined by a WSDL document
     * 
     * @param wsdlDefinition
     *            The WSDL4J instance of a WSDL definition.
     * 
     * @return A castor schema is returned if the WSDL definition contains a types element. null is
     *         returned otherwise.
     */
    protected Schema createSchemaFromTypes(Definition wsdlDefinition) {
        // Get the schema element from the WSDL definition
        org.w3c.dom.Element schemaElement = null;

        if (wsdlDefinition.getTypes() != null) {
            ExtensibilityElement schemaExtElem = findExtensibilityElement(wsdlDefinition.getTypes()
                    .getExtensibilityElements(), "schema");

            System.out.println(">>>> "+schemaExtElem.getElementType().toString());
            
            if (schemaExtElem != null && schemaExtElem instanceof UnknownExtensibilityElement) {
                schemaElement = ((UnknownExtensibilityElement) schemaExtElem).getElement();
            }
        }
        
        

        if (schemaElement == null) {
            // No schema to read
            System.err.println("Unable to find schema extensibility element in WSDL");
            return null;
        }

        // Convert from DOM to JDOM
        DOMBuilder domBuilder = new DOMBuilder();
        org.jdom.Element jdomSchemaElement = domBuilder.build(schemaElement);
        
        if (jdomSchemaElement == null) {
            System.err.println("Unable to read schema defined in WSDL");
            return null;
        }

        // Add namespaces from the WSDL
        Map namespaces = wsdlDefinition.getNamespaces();

        if (namespaces != null && !namespaces.isEmpty()) {
            Iterator nsIter = namespaces.keySet().iterator();

            while (nsIter.hasNext()) {
                String nsPrefix = (String) nsIter.next();
                String nsURI = (String) namespaces.get(nsPrefix);

                if (nsPrefix != null && nsPrefix.length() > 0) {
                    org.jdom.Namespace nsDecl = org.jdom.Namespace.getNamespace(nsPrefix, nsURI);
                    jdomSchemaElement.addNamespaceDeclaration(nsDecl);
                }
            }
        }

        // Make sure that the types element is not processed
        jdomSchemaElement.detach();

        // Convert it into a Castor schema instance
        Schema schema = null;

        try {
            schema = XMLUtils.convertElementToSchema(jdomSchemaElement);
        }

        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Return it
        return schema;
    }

    /**
     * Populates a ServiceInfo instance from the specified Service definiition
     * 
     * @param component
     *            The component to populate
     * @param service
     *            The Service to populate from
     * 
     * @return The populated component is returned representing the Service parameter
     */
    private ServiceInfo populateComponent(ServiceInfo component, Service service) {
        // Get the qualified service name information
        QName qName = service.getQName();

        // Get the service's namespace URI
        String namespace = qName.getNamespaceURI();

        // Use the local part of the qualified name for the component's name
        String name = qName.getLocalPart();

        // Set the name
        component.setName(name);

        // Get the defined ports for this service
        Map ports = service.getPorts();

        // Use the Ports to create OperationInfos for all request/response messages defined
        Iterator portIter = ports.values().iterator();

        while (portIter.hasNext()) {
            // Get the next defined port
            Port port = (Port) portIter.next();

            // Get the Port's Binding
            Binding binding = port.getBinding();

            // Now we will create operations from the Binding information
            List operations = buildOperations(binding);

            // Process objects built from the binding information
            Iterator operIter = operations.iterator();

            while (operIter.hasNext()) {
                OperationInfo operation = (OperationInfo) operIter.next();

                // Set the namespace URI for the operation.
                operation.setNamespaceURI(namespace);

                // Find the SOAP target URL
                ExtensibilityElement addrElem = findExtensibilityElement(port
                        .getExtensibilityElements(), "address");

                if (addrElem != null && addrElem instanceof SOAPAddress) {
                    // Set the SOAP target URL
                    SOAPAddress soapAddr = (SOAPAddress) addrElem;
                    operation.setTargetURL(soapAddr.getLocationURI());
                }

                // Add the operation info to the component
                component.addOperation(operation);
            }
        }

        return component;
    }

    /**
     * Creates Info objects for each Binding Operation defined in a Port Binding
     * 
     * @param binding
     *            The Binding that defines Binding Operations used to build info objects from
     * 
     * @return A List of built and populated OperationInfos is returned for each Binding Operation
     */
    private List buildOperations(Binding binding) {
        // Create the array of info objects to be returned
        List operationInfos = new ArrayList();

        // Get the list of Binding Operations from the passed binding
        List operations = binding.getBindingOperations();

        if (operations != null && !operations.isEmpty()) {
            // Determine encoding
            ExtensibilityElement soapBindingElem = findExtensibilityElement(binding
                    .getExtensibilityElements(), "binding");
            String style = "document"; // default

            if (soapBindingElem != null && soapBindingElem instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
                style = soapBinding.getStyle();
            }

            // For each binding operation, create a new OperationInfo
            Iterator opIter = operations.iterator();
            int i = 0;

            while (opIter.hasNext()) {
                BindingOperation oper = (BindingOperation) opIter.next();

                // We currently only support soap:operation bindings
                // filter out http:operations for now until we can dispatch them properly
                ExtensibilityElement operElem = findExtensibilityElement(oper
                        .getExtensibilityElements(), "operation");

                if (operElem != null && operElem instanceof SOAPOperation) {
                    // Create a new operation info
                    OperationInfo operationInfo = new OperationInfo(style);

                    // Populate it from the Binding Operation
                    buildOperation(operationInfo, oper);

                    // Add to the return list
                    operationInfos.add(operationInfo);
                }
            }
        }

        return operationInfos;
    }

    /**
     * Populates an OperationInfo from the specified Binding Operation
     * 
     * @param operationInfo
     *            The component to populate
     * @param bindingOper
     *            A Binding Operation to define the OperationInfo from
     * 
     * @return The populated OperationInfo object is returned.
     */
    private OperationInfo buildOperation(OperationInfo operationInfo, BindingOperation bindingOper) {
        // Get the operation
        Operation oper = bindingOper.getOperation();

        // HG: Set the operation Name
        operationInfo.setOperationName(bindingOper.getName());

        // Set the name using the operation name
        operationInfo.setTargetMethodName(oper.getName());

        // Set the action URI
        ExtensibilityElement operElem = findExtensibilityElement(bindingOper
                .getExtensibilityElements(), "operation");

        if (operElem != null && operElem instanceof SOAPOperation) {
            SOAPOperation soapOperation = (SOAPOperation) operElem;
            operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
        }

        // Get the Binding Input
        BindingInput bindingInput = bindingOper.getBindingInput();

        // Get the Binding Output
        BindingOutput bindingOutput = bindingOper.getBindingOutput();

        // Get the SOAP Body
        ExtensibilityElement bodyElem = findExtensibilityElement(bindingInput
                .getExtensibilityElements(), "body");

        if (bodyElem != null && bodyElem instanceof SOAPBody) {
            SOAPBody soapBody = (SOAPBody) bodyElem;

            // The SOAP Body contains the encoding styles
            List styles = soapBody.getEncodingStyles();
            String encodingStyle = null;

            if (styles != null) {
                // Use the first in the list
                encodingStyle = styles.get(0).toString();
            }

            if (encodingStyle == null) {
                // An ecoding style was not found, give it a default
                encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
            }

            // Assign the encoding style value
            operationInfo.setEncodingStyle(encodingStyle.toString());

            // The SOAP Body contains the target object's namespace URI.
            operationInfo.setTargetObjectURI(soapBody.getNamespaceURI());
        }

        // Get the Operation's Input definition
        Input inDef = oper.getInput();

        if (inDef != null) {
            // Build input parameters
            Message inMsg = inDef.getMessage();

            if (inMsg != null) {
                // Set the name of the operation's input message
                operationInfo.setInputMessageName(inMsg.getQName().getLocalPart());

                // Set the body of the operation's input message
                operationInfo.setInputMessageText(getMessageTemplate(operationInfo, inMsg));
            }
        }

        // Get the Operation's Output definition
        Output outDef = oper.getOutput();

        if (outDef != null) {
            // Build output parameters
            Message outMsg = outDef.getMessage();

            if (outMsg != null) {
                // Set the name of the output message
                operationInfo.setOutputMessageName(outMsg.getQName().getLocalPart());

                // Set the body of the operation's output message
                operationInfo.setOutputMessageText(getMessageTemplate(operationInfo, outMsg));
            }
        }

        // Finished, return the populated object
        return operationInfo;
    }

    /**
     * Builds and adds parameters to the supplied info object given a SOAP Message definition (from
     * WSDL)
     * 
     * @param operationInfo
     *            The component to build message text for
     * @param msg
     *            The SOAP Message definition that has parts to defined parameters for
     */

    private String getMessageTemplate(OperationInfo operationInfo, Message msg) {
        //      Create the root message element
        StringBuffer buf = new StringBuffer();
        buf.append("<" + operationInfo.getTargetMethodName() + ">");

        // Get the message parts
        List msgParts = msg.getOrderedParts(null);

        // Process each part
        Iterator iter = msgParts.iterator();

        while (iter.hasNext()) {
            // Get each part
            Part part = (Part) iter.next();

            // Add content for each message part
            String partName = part.getName();

            if (partName != null) {
                String type;

                if (part.getTypeName() == null) {

                    // Then this is complex Type
                    type = part.getElementName().getLocalPart();

                    log.info("PartName: " + partName + "\tTYPE: " + type);

                    XObject o = (XObject) typesParser.types.get(type);
                    buf.append(o.toXML(false));

                    // System.out.println("XML: " + o.toXML(false));
                }

                else {
                    buf.append("\t<" + partName);

                    type = part.getTypeName().getLocalPart();

                    if (operationInfo.getStyle().equalsIgnoreCase("rpc")) {
                        // If this is an RPC style operation, we need to include some type
                        // information
                        buf.append(" type=\"" + part.getTypeName().getLocalPart() + "\"");
                    }

                    buf.append(">0</" + partName + ">");
                }
            }
        }

        buf.append("</" + operationInfo.getTargetMethodName() + ">");
        // System.out.println("XML::\n" + buf.toString());
        try {
            XmlObject obj = XmlObject.Factory.parse(buf.toString());
            return obj.xmlText((new XmlOptions()).setSavePrettyPrint().setSaveAggressiveNamespaces()
                    .setSavePrettyPrintIndent(4));
        } catch (XmlException e1) {

            e1.printStackTrace();
        }

        return null;

    }

    /**
     * Returns the desired ExtensibilityElement if found in the List
     * 
     * @param extensibilityElements
     *            The list of extensibility elements to search
     * @param elementType
     *            The element type to find
     * 
     * @return Returns the first matching element of type found in the list
     */
    private static ExtensibilityElement findExtensibilityElement(List extensibilityElements,
            String elementType) {
        if (extensibilityElements != null) {
            Iterator iter = extensibilityElements.iterator();

            while (iter.hasNext()) {
                ExtensibilityElement element = (ExtensibilityElement) iter.next();

                if (element.getElementType().getLocalPart().equalsIgnoreCase(elementType)) {
                    // Found it
                    return element;
                }
            }
        }

        return null;
    }
}