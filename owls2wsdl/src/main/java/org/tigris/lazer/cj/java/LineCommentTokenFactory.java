/*
 * BlockCommentTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.cj.java;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.LineCommentTokenType;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class LineCommentTokenFactory extends AbstractTokenFactory {
    
    private final String lineSeparator = System.getProperty("line.separator","\r\n");
    private final static String lineComments[] = {"//"};
    
    /** Creates a new instance of BlockCommentTokenFactory */
    public LineCommentTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return regionMatches(text, start, lineComments[0]);
        //return text.startsWith(lineComments[0]);
    }
    
    public Token createToken(String text, int start) {
        int position = indexOf(text, '\n', start+2, 0);
        return createToken(text, start, position, LineCommentTokenType.getInstance());
    }
}
