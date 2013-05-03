/*
 * TagTypeToken.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer.xml;

import org.tigris.lazer.TokenType;

/**
 *
 * @author Bob Tarling
 */
public class SoapTagTokenType extends TokenType {
    
    static private SoapTagTokenType INSTANCE = 
        new SoapTagTokenType();
    
    /** Creates a new instance of SoapTagTokenType */
    private SoapTagTokenType() {
    }
    
    static public SoapTagTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "SOAP Tag";
    }
}
