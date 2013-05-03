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
public class XmlTagTokenType extends TokenType {
    
    static private XmlTagTokenType INSTANCE = 
        new XmlTagTokenType();
    
    /** Creates a new instance of CommentToken */
    private XmlTagTokenType() {
    }
    
    static public XmlTagTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "Tag";
    }
}
