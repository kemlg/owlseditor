/*
 * BlockCommentTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.cj.java;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.Token;
import org.tigris.lazer.WhiteSpaceTokenType;

/**
 *
 * @author Bob Tarling
 */
public class WhiteSpaceTokenFactory extends AbstractTokenFactory {
    
    boolean compoundWhiteSpace = true;
    private static String whiteSpace = " \n\r\t";
    
    /** Creates a new instance of WhiteSpaceTokenFactory */
    public WhiteSpaceTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return whiteSpace.indexOf(text.charAt(start)) != -1;
    }
    
    public Token createToken(String text, int start) {
        int position = start;
        if (compoundWhiteSpace) {
            while (position < text.length() && whiteSpace.indexOf(text.charAt(position)) != -1) {
                position++;
            }
        } else {
            position++;
        }
        return createToken(text, start, position, WhiteSpaceTokenType.getInstance());
    }
}
