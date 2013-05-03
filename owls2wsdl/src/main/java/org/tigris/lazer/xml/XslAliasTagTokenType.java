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
public class XslAliasTagTokenType extends TokenType {
    
    static private XslAliasTagTokenType INSTANCE = 
        new XslAliasTagTokenType();
    
    /** Creates a new instance of CommentToken */
    private XslAliasTagTokenType() {
    }
    
    static public XslAliasTagTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "XSL Tag";
    }
}
