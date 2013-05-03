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
public class WsdlTagTokenType extends TokenType {
    
    static private WsdlTagTokenType INSTANCE = 
        new WsdlTagTokenType();
    
    /** Creates a new instance of WsdlTagTokenType */
    private WsdlTagTokenType() {
    }
    
    static public WsdlTagTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "WSDL Tag";
    }
}
