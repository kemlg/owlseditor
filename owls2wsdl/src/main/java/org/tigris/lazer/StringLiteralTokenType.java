/*
 * StringLiteralTokenType.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class StringLiteralTokenType extends LiteralTokenType {
    
    static private StringLiteralTokenType INSTANCE = 
        new StringLiteralTokenType();
    
    /** Creates a new instance of CommentToken */
    private StringLiteralTokenType() {
    }
    
    static public StringLiteralTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "String Literal";
    }
}
