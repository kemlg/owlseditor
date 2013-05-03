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
package cgl.hpsearch.common.objects;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * XML - 2 - Object for in-memory use(Dynamic purposes). <br>
 * Created on Feb 8, 2005
 * 
 * @author Harshawardhan Gadgil (hgadgil@grids.ucs.indiana.edu)
 */

public class XObject {

    public static final String XSI_NAMESPACE_PREFIX = "xsi";

    public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

    // For internal use only...
    private XObject _parent;

    String _name;

    Hashtable _typeTable;

    Hashtable _valueTable;

    public XObject(String name) {
        _name = name;
        _typeTable = new Hashtable();
        _valueTable = new Hashtable();
        _parent = null;
    }

    public final void setParent(XObject parent) {
        _parent = parent;
    }

    public final XObject getParent() {
        return _parent;
    }

    public final String getName() {
        return _name;
    }

    public void setObjectField(String name, String type, Object value) {
        _typeTable.put(name, type);
        _valueTable.put(name, value);
    }

    public void show(String prefix) {
        System.out.println(prefix + _name + ":->");

        for (Enumeration e = _typeTable.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            Object o = _valueTable.get(key);

            if (o instanceof XObject) {
                ((XObject) o).show(prefix + "-");
            } else {
                System.out.println(prefix + "  " + (String) _typeTable.get(key) + "  " + key);
            }
        }
    }

    public void set(String key, Object o) {
        if (_typeTable.containsKey(key)) {
            if (_valueTable.get(key).getClass().equals(o.getClass())) {
                _valueTable.put(key, o);
            }
        }
    }

    public String toXML(boolean includeSelfName) {
        StringBuffer _sb = new StringBuffer();
        if (includeSelfName)
                _sb.append("<" + _name + " xmlns:" + XSI_NAMESPACE_PREFIX + "=\""
                        + XSI_NAMESPACE_URI + "\">");
        for (Enumeration e = _typeTable.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            Object o = _valueTable.get(key);
            if (o instanceof XObject) {
                // _sb.append("<" + key + ">\n");
                _sb.append(((XObject) o).toXML(true));
                // _sb.append("</" + key + ">");
            } else {

                _sb.append("<" + key + " xmlns:" + XSI_NAMESPACE_PREFIX + "=\"" + XSI_NAMESPACE_URI
                        + "\" " + XSI_NAMESPACE_PREFIX + ":type=\"");
                _sb.append(_typeTable.get(key));
                _sb.append("\">");
                if (o.toString().equals(""))
                    _sb.append("0");
                else
                    _sb.append(o.toString());
                _sb.append("</" + key + ">\n");
            }
        }
        if (includeSelfName) _sb.append("</" + _name + ">");

        return _sb.toString();
    }

    public static void main(String[] o) {
        XObject myObj = new XObject("myObj");

        // Define the object
        myObj.setObjectField("ipAddress", "xsd:string", new String());
        myObj.setObjectField("licenseKey", "xsd:string", new String());

        // Set values
        myObj.set("ipAddress", "156.56.104.155");
        myObj.set("licenseKey", "0");

        myObj.show("");
        System.out.println(myObj.toXML(true));

        // ------ NESTED OBJECT TEST ---------
        System.out.println("\n\n");

        XObject myObj1 = new XObject("myObj1");
        XObject myObj2 = new XObject("myObj2");

        // Define the object
        myObj2.setObjectField("name", "xsd:string", new String());
        myObj2.setObjectField("dept", "xsd:string", new String());

        myObj1.setObjectField("details", "", myObj2);
        myObj1.setObjectField("aid", "xsd:int", new Integer(0));

        myObj1.show("\t  ");

        // Set values
        myObj2.set("name", "Jim");
        myObj2.set("details", "Hr. Mgr.");
        myObj1.set("aid", new Integer(10332));

        System.out.println(myObj1.toXML(true));

    }

}