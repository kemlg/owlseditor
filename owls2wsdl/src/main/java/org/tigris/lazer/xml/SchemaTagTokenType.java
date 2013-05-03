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
public class SchemaTagTokenType extends TokenType {
    
    static private SchemaTagTokenType INSTANCE = 
        new SchemaTagTokenType();
    
    /** Creates a new instance of SchemaTagTokenType */
    private SchemaTagTokenType() {
    }
    
    static public SchemaTagTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "XML Schema Tag";
    }
}
