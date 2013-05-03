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
 * All sample code contained herein is provided to you "AS IS" without any warranties of any kind.
 */
package cgl.hpsearch.nb4ws.WSDL;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.exolab.castor.net.URIException;
import org.exolab.castor.net.URILocation;
import org.exolab.castor.net.URIResolver;
import org.exolab.castor.net.util.URILocationImpl;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.exolab.castor.xml.schema.writer.SchemaWriter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML support and utilities
 * 
 * @author Bikash Behera
 * @author Jim Winfield
 */

public class XMLUtils {

    private XMLUtils() {
    }

    /**
     * Returns a string representation of the given jdom document.
     * 
     * @param doc
     *            The jdom document to be converted into a string.
     * 
     * @return Ths string representation of the given jdom document.
     */
    public static String outputString(Document doc) {
        XMLOutputter xmlWriter = new XMLOutputter("    ", true);

        return xmlWriter.outputString(doc);
    }

    /**
     * Returns a string representation of the given jdom element.
     * 
     * @param elem
     *            The jdom element to be converted into a string.
     * 
     * @return The string representation of the given jdom element.
     */
    public static String outputString(Element elem) {
        XMLOutputter xmlWriter = new XMLOutputter("    ", true);

        return xmlWriter.outputString(elem);
    }

    /**
     * It reads the given xml returns the jdom document.
     * 
     * @param xml
     *            The xml to read.
     * 
     * @return The jdom document created from the xml.
     * 
     * @throws JDOMException
     *             If the parsing failed.
     */
    public static Document readXML(String xml) throws JDOMException, IOException {
        return readXML(new StringReader(xml));
    }

    /**
     * It reads the given xml reader and returns the jdom document.
     * 
     * @param reader
     *            The xml reader to read.
     * 
     * @return The jdom document created from the xml reader.
     * 
     * @throws JDOMException
     *             If the parsing failed.
     */
    public static Document readXML(Reader reader) throws JDOMException, IOException {
        SAXBuilder xmlBuilder = new SAXBuilder(false);

        Document doc = xmlBuilder.build(reader);

        return doc;
    }

    static class ExternalSchemaURIResolver implements URIResolver {

        public URILocation resolve(String arg0, String arg1) throws URIException {
            return resolveURN(arg0);
        }

        public URILocation resolveURN(String arg0) throws URIException {
            return new URILocationImpl(arg0);
        }
    };

    /**
     * It reads the given reader and returns the castor schema.
     * 
     * @param reader
     *            The reader to read.
     * 
     * @return The castor schema created from the reader.
     * 
     * @throws IOException
     *             If the schema could not be read from the reader.
     */
    public static Schema readSchema(Reader reader) throws IOException {
        // create the sax input source
        InputSource inputSource = new InputSource(reader);

        // create the schema reader
        SchemaReader schemaReader = new SchemaReader(inputSource);
        schemaReader.setValidation(false);
        schemaReader.setURIResolver(new ExternalSchemaURIResolver());
        schemaReader.setEntityResolver(null);

        // read the schema from the source
        Schema schema = schemaReader.read();

        return schema;
    }

    /**
     * Converts a castor schema into a jdom element.
     * 
     * @param schema
     *            The castor schema to be converted.
     * 
     * @return The jdom element representing the schema.
     * 
     * @throws SAXException
     *             If the castor schema could not be written out.
     * @throws IOException
     *             If the castor schema could not be written out.
     * @throws JDOMException
     *             If the output of the castor schema could not be converted into a jdom element.
     */
    public static Element convertSchemaToElement(Schema schema) throws SAXException, IOException,
            JDOMException {
        // get the string content of the schema
        String content = outputString(schema);

        // check for null content value
        if (content != null) {
            // create a document out of it
            Document doc = readXML(new StringReader(content));

            // return the root of the document
            return doc.getRootElement();
        }

        // return null otherwise
        return null;
    }

    /**
     * Converts the jdom element into a castor schema.
     * 
     * @param element
     *            The jdom element to be converted into a castor schema.
     * 
     * @return The castor schema corresponding to the element.
     * 
     * @throws IOException
     *             If the jdom element could not be written out.
     */
    public static Schema convertElementToSchema(Element element) throws IOException {
        // get the string content of the element
        String content = outputString(element);

        // check for null value
        if (content != null) {
            // create a schema from the string content
            return readSchema(new StringReader(content));
        }

        // otherwise return null
        return null;
    }

    /**
     * Returns a string representation of the given castor schema.
     * 
     * @param schema
     *            The castor schema to be converted into a string.
     * 
     * @return The string representation of the given castor schema.
     * 
     * @throws IOException
     *             If the schema could not be written out.
     * @throws SAXException
     *             If the schema could not be written out.
     */
    public static String outputString(Schema schema) throws IOException, SAXException {
        // create a string writer
        StringWriter writer = new StringWriter();

        // create the schema writer
        SchemaWriter schemaWriter = new SchemaWriter(writer);

        // write the schema into the source
        schemaWriter.write(schema);

        // return the content of the writer
        return writer.toString();
    }
}