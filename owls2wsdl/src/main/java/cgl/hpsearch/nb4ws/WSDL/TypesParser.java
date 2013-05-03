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

import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.XMLType;

import cgl.hpsearch.common.objects.XObject;

/**
 * Parses the specified schema to retrieve all elements defined in the schema. This program uses the
 * Castor SchemaReader to parse the schema <br>
 * Created on Feb 7, 2005
 * 
 * @author Harshawardhan Gadgil (hgadgil@grids.ucs.indiana.edu)
 */

public class TypesParser {

    public Hashtable types = new Hashtable();

    public void init(Schema schema) {
        try {
            processSchema(schema);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean topLevel;

    public void processSchema(Schema schema) {
        for (Enumeration e = schema.getElementDecls(); e.hasMoreElements();) {
            ElementDecl element = (ElementDecl) e.nextElement();
            // System.out.println(element.getName() + " = {");
            topLevel = true;
            processElement(element);
            // System.out.println("};\n");

        }
    }

    public void processElement(ElementDecl element) {
        // System.out.println("* PROCESSING: " + element.getName());
        if (element.isReference()) {
            // Process the Reference ...
            System.out.println("Reference...");
            element = element.getReference();
            processElement(element);
        }

        else if (element.isAbstract()) {
            // Abstract Element.. ??
            System.out.println("Abstract...");
        }

        else {

            XMLType type = element.getType();

            if (type.isSimpleType()) {
                // This is SimpleType...

                String elementType = type.getName();

                String out = "\t" + type.getName();

                int min = element.getMinOccurs();
                int max = element.getMaxOccurs();
                if (max < 1) {
                    // This element occurs unbounded no. of times...
                    out += "[]";
                    elementType += "[]";
                } else if (max > 1) out += "[" + max + "]";

                out += "\t" + element.getName();
                currentObj.setObjectField(element.getName(), elementType, "0");
                // System.out.println(out);
            }

            else if (type.isComplexType()) {
                // Process ComplexType...

                if (topLevel) {
                    XObject o = new XObject(element.getName());
                    types.put(element.getName(), o);
                    currentObj = o; // Mark the current Object
                    topLevel = false;
                    o.setParent(null);
                    System.out.println("* - Adding NEW: " + element.getName());
                } else {

                    System.out.println("* - Adding " + element.getName() + " UNDER "
                            + currentObj.getName());

                    XObject o = new XObject(element.getName());
                    currentObj.setObjectField(element.getName(), "", o);
                    o.setParent(currentObj);
                    currentObj = o;
                }

                processComplexType((ComplexType) type);

                currentObj = currentObj.getParent();
            }
        }
    }

    public void processComplexType(ComplexType c) {
        for (Enumeration e = c.enumerate(); e.hasMoreElements();) {
            Particle p = (Particle) e.nextElement();
            processParticle(p);
        }
    }

    public void processGroup(Group g) {
        for (Enumeration e = g.enumerate(); e.hasMoreElements();) {
            Particle p = (Particle) e.nextElement();
            processParticle(p);
        }
    }

    public void processParticle(Particle p) {
        if (p instanceof ElementDecl)
            processElement((ElementDecl) p);
        else if (p instanceof Group) processGroup((Group) p);
        //         else
        //            System.out.println(" :> " + p.toString());
        //        
    }

    XObject currentObj;

    public static void main(String[] args) {

        TypesParser r = new TypesParser();
        try {
            // create the sax input source
            Schema schema = XMLUtils.readSchema(new FileReader("D:\\development\\xsd\\SchemaEx.xsd"));
            System.out.println("SCHEMA ( CASTOR) : "+schema.toString());
            System.out.println("============================================");
            r.init(schema);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Enumeration e = r.types.keys(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            System.out.println("Showing: " + s);
            
            String xml = ((XObject) r.types.get(s)).toXML(true);
            
            try {
                XmlObject obj = XmlObject.Factory.parse(xml);
                
                System.out.println(">>>>>> "+obj.xmlText((new XmlOptions()).setSavePrettyPrint()
                        .setSaveAggressiveNamespaces()));
            } catch (XmlException e1) {

                e1.printStackTrace();
            }

        }
    }
}