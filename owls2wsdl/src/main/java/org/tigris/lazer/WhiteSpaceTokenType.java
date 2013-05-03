/*
 * WhiteSpaceTokenType.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class WhiteSpaceTokenType extends TokenType {
    
    static private WhiteSpaceTokenType INSTANCE = new WhiteSpaceTokenType();

    /** Creates a new instance of CommentToken */
    private WhiteSpaceTokenType() {
    }
    
    static public WhiteSpaceTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "WS";
    }
}
