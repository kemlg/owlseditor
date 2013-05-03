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
public class XslTagTokenType extends TokenType {
    
    static private XslTagTokenType INSTANCE = 
        new XslTagTokenType();
    
    /** Creates a new instance of CommentToken */
    private XslTagTokenType() {
    }
    
    static public XslTagTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "XSL Tag";
    }
}
