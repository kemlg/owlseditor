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
import java.util.Iterator;
import java.util.List;

/**
 * Service Info an in memory representation of a service defined in WSDL
 * 
 * @author Jim Winfield
 */

public class ServiceInfo {

    /** The service name */
    String name = "";

    /** The list of operations that this service defines. */
    List operations = new ArrayList();

    /**
     * Constructor
     */
    public ServiceInfo() {
    }

    /**
     * Sets the name of the service
     * 
     * @param value
     *            The name of the service
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Gets the name of the service
     * 
     * @return The name of the service is returned
     */
    public String getName() {
        return name;
    }

    /**
     * Add an ooperation info object to this service definition
     * 
     * @param operation
     *            The operation to add to this service definition
     */
    public void addOperation(OperationInfo operation) {
        operations.add(operation);
    }

    /**
     * Returs the operations defined by this service
     * 
     * @return an Iterator that can be used to iterate the operations defined by this service
     */
    public Iterator getOperations() {
        return operations.iterator();
    }

    /**
     * Override toString to return the name of the service
     * 
     * @return The name of the service is returned
     */
    public String toString() {
        return getName();
    }
}