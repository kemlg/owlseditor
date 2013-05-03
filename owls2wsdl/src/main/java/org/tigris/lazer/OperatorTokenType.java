/*
 * OperatorTokenType.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class OperatorTokenType extends CodeTokenType {
    
    static private OperatorTokenType INSTANCE = 
        new OperatorTokenType();
    
    /** Creates a new instance of CommentToken */
    private OperatorTokenType() {
    }
    
    static public OperatorTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "OP";
    }
}
