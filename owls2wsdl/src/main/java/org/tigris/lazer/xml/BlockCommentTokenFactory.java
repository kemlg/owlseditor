/*
 * BlockCommentTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.xml;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.BlockCommentTokenType;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class BlockCommentTokenFactory extends AbstractTokenFactory {
    
    private static String blockComments[] = {"<!--", "-->"};
    
    /** Creates a new instance of BlockCommentTokenFactory */
    public BlockCommentTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return regionMatches(text, start, blockComments[0]);
        //return text.startsWith(blockComments[0]);
    }
    
    public Token createToken(String text, int start) {
        int end = indexOf(text, blockComments[1], start+2, 3);
        return createToken(text, start, end, BlockCommentTokenType.getInstance());
    }
}
