/*
 * CharacterLiteralTokenType.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class CharacterLiteralTokenType extends LiteralTokenType {
    
    static private CharacterLiteralTokenType INSTANCE = 
        new CharacterLiteralTokenType();
    
    /** Creates a new instance of CommentToken */
    private CharacterLiteralTokenType() {
    }
    
    static public CharacterLiteralTokenType getInstance() {
        return INSTANCE;
    }
    

    public String toString() {
        return "CL";
    }
}
