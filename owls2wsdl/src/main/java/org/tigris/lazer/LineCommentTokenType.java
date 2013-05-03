/*
 * LineCommentTokenType.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class LineCommentTokenType extends CommentTokenType {
    
    static private LineCommentTokenType INSTANCE = 
        new LineCommentTokenType();
    
    /** Creates a new instance of CommentToken */
    private LineCommentTokenType() {
    }
    
    static public LineCommentTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "LC";
    }
}
