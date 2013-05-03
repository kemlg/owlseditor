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
package cgl.hpsearch.nb4ws.WSDL;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Defines an in memory model to support a SOAP invocation
 * 
 * @author Jim Winfield
 */

public class OperationInfo {

    static Logger log = Logger.getLogger("OperationInfo");

    /** The SOAP operation name */
    private String operationName = "";

    /** The SOAP operation type */
    private String operationType = "";

    /** The SOAP encoding style to use. */
    private String encodingStyle = "";

    /** The URL where the target object is located. */
    private String targetURL = "";

    /** The namespace URI used for this SOAP operation. */
    private String namespaceURI = "";

    /** The URI of the target object to invoke for this SOAP operation. */
    private String targetObjectURI = "";

    /** The name used to when making an invocation. */
    private String targetMethodName = "";

    /** The input message. */
    private String inputMessageText = "";

    /** The output message. */
    private String outputMessageText = "";

    /** The name of input message. */
    private String inputMessageName = "";

    /** The name of output message. */
    private String outputMessageName = "";

    /** The action URI value to use when making a invocation. */
    private String soapActionURI = "";

    /** The encoding type "document" vs. "rpc" */
    private String style = "document";

    public void show() {
        System.out.print("SOAP operation name: ");
        System.out.println(operationName);

        System.out.print("SOAP operation type: ");
        System.out.println(operationType);

        System.out.print("SOAP encoding style: ");
        System.out.println(encodingStyle);

        System.out.print("URL target         : ");
        System.out.println(targetURL);

        System.out.print("namespace URI      : ");
        System.out.println(namespaceURI);

        System.out.print("URI target object  : ");
        System.out.println(targetObjectURI);

        System.out.print("Target Method name : ");
        System.out.println(targetMethodName);

        System.out.print("input message.     : ");
        System.out.println(inputMessageText);

        System.out.print("output message.    : ");
        System.out.println(outputMessageText);

        System.out.print("name input message : ");
        System.out.println(inputMessageName);

        System.out.print("name output message: ");
        System.out.println(outputMessageName);

        System.out.print("SOAPaction URI     : ");
        System.out.println(soapActionURI);

        System.out.print("encoding type      : ");
        System.out.println(style);
    }

    /**
     * Constructor
     */
    public OperationInfo() {
        super();
    }

    /**
     * Constructor
     * 
     * @param style
     *            Pass "document" or "rpc"
     */
    public OperationInfo(String style) {
        super();

        setStyle(style);
    }

    /* HG: Added as extra utility functions... */
    public final String getOperationName() {
        return operationName;
    }

    public final void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Serializes the OperationInfo to XML
     * 
     * @return Marshalled Object
     */
    public String toXML() {
        StringWriter s = new StringWriter();

        try {
            Marshaller.marshal(this, s);
        } catch (Exception e) {
            log.error("", e);
            s.write("");
        }

        return s.toString();
    }

    /**
     * Returns the OperationInfo object by unmarshalling the XML
     * 
     * @param xml
     * @return OperationInfo object or null if error
     */
    public static OperationInfo getFromString(String xml) {
        OperationInfo obj = null;

        StringReader sr = new StringReader(xml);

        try {
            obj = (OperationInfo) Unmarshaller.unmarshal(OperationInfo.class, sr);
        } catch (Exception e) {
            log.error("", e);
            obj = null;
        }

        return obj;
    }

    /**
     * Sets the encoding style for this operation.
     * 
     * @param value
     *            The encoding style
     */
    public void setEncodingStyle(String value) {
        encodingStyle = value;
    }

    /**
     * Gets the encoding style for this operation
     * 
     * @return A string value that signifies the encoding style to use.
     */
    public String getEncodingStyle() {
        return encodingStyle;
    }

    /**
     * Sets the Target URL used to make a SOAP invocation for this operation
     * 
     * @param value
     *            The target URL
     */
    public void setTargetURL(String value) {
        targetURL = value;
    }

    /**
     * Gets the Target URL used to make a SOAP invocation for this operation
     * 
     * @return The target URL is returned
     */
    public String getTargetURL() {
        return targetURL;
    }

    /**
     * Sets the namespace URI used for this operation
     * 
     * @param value
     *            The namespace URI to use
     */
    public void setNamespaceURI(String value) {
        namespaceURI = value;
    }

    /**
     * Gets the namespace URI used for this
     * 
     * @return The namespace URI of the target object
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Sets the Target Object's URI used to make an invocation
     * 
     * @param value
     *            The URI of the target object
     */
    public void setTargetObjectURI(String value) {
        targetObjectURI = value;
    }

    /**
     * Gets the Target Object's URI
     * 
     * @return The URI of the target object
     */
    public String getTargetObjectURI() {
        return targetObjectURI;
    }

    /**
     * Sets the name of the target method to call
     * 
     * @param value
     *            The name of the method to call
     */
    public void setTargetMethodName(String value) {
        targetMethodName = value;
    }

    /**
     * Gets the name of the target method to call
     * 
     * @return The name of the method to call
     */
    public String getTargetMethodName() {
        return targetMethodName;
    }

    /**
     * Sets the value of the target's input message name
     * 
     * @param value
     *            The name of input message
     */
    public void setInputMessageName(String value) {
        inputMessageName = value;
    }

    /**
     * Gets the value of the target's input message name
     * 
     * @return The name of the input message is returned
     */
    public String getInputMessageName() {
        return inputMessageName;
    }

    /**
     * Sets the value of the target's output message name
     * 
     * @param value
     *            The name of the output message
     */
    public void setOutputMessageName(String value) {
        outputMessageName = value;
    }

    /**
     * Gets the value of the target method's output message name
     * 
     * @return The name of the output message is returned
     */
    public String getOutputMessageName() {
        return outputMessageName;
    }

    /**
     * Sets the value of the target's input message
     * 
     * @param value
     *            The SOAP input message
     */
    public void setInputMessageText(String value) {
        inputMessageText = value;
    }

    /**
     * Gets the value of the target's input message
     * 
     * @return The input message is returned
     */
    public String getInputMessageText() {
        return inputMessageText;
    }

    /**
     * Sets the value of the target method's Output message
     * 
     * @param value
     *            The output message
     */
    public void setOutputMessageText(String value) {
        outputMessageText = value;
    }

    /**
     * Gets the value of the target method's Output message
     * 
     * @return The Output message is returned
     */
    public String getOutputMessageText() {
        return outputMessageText;
    }

    /**
     * Sets the value for the SOAP Action URI used to make a SOAP invocation
     * 
     * @param value
     *            The SOAP Action URI value for the SOAP invocation
     */
    public void setSoapActionURI(String value) {
        soapActionURI = value;
    }

    /**
     * Gets the value for the SOAP Action URI used to make a SOAP invocation
     * 
     * @return The SOAP Action URI value for the SOAP invocation is returned.
     */
    public String getSoapActionURI() {
        return soapActionURI;
    }

    /**
     * Sets the encoding document/literal vs. rpc/encoded
     * 
     * @return value A string value "document" or "rpc" should be passed.
     */
    public void setStyle(String value) {
        style = value;
    }

    /**
     * Returns the style "document" or "rpc"
     * 
     * @return The style type is returned
     */
    public String getStyle() {
        return style;
    }

    /**
     * Override toString to return a name for the operation
     * 
     * @return The name of the operation is returned
     */
    public String toString() {
        return getTargetMethodName();
    }
}