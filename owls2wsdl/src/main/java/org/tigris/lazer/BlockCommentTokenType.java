/*
 * BlockCommentTypeToken.java
 *
 * Created on 09 August 2003, 09:35
 */

package org.tigris.lazer;

/**
 *
 * @author Bob Tarling
 */
public class BlockCommentTokenType extends CommentTokenType {
    
    static private BlockCommentTokenType INSTANCE = 
        new BlockCommentTokenType();
    
    /** Creates a new instance of CommentToken */
    private BlockCommentTokenType() {
    }
    
    static public BlockCommentTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "BC";
    }
}
