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
public class TextTokenType extends CodeTokenType {
    
    static private TextTokenType INSTANCE = new TextTokenType();
    
    /** Creates a new instance of CommentToken */
    private TextTokenType() {
    }
    
    static public TextTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "TX";
    }
}
