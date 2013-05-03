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
public class NumberTokenType extends CodeTokenType {
    
    static private NumberTokenType INSTANCE = 
        new NumberTokenType();
    
    /** Creates a new instance of CommentToken */
    private NumberTokenType() {
    }
    
    static public NumberTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "NL";
    }
}
