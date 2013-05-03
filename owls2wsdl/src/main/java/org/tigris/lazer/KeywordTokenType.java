/*
 * NumberTokenType.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class KeywordTokenType extends CodeTokenType {
    
    static private KeywordTokenType INSTANCE = 
        new KeywordTokenType();
    
    /** Creates a new instance of CommentToken */
    private KeywordTokenType() {
    }
    
    static public KeywordTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "KW";
    }
}
