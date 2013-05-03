/*
 * BlockCommentTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.cj.java;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.StringLiteralTokenType;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class StringLiteralTokenFactory extends AbstractTokenFactory {
    
    private static char escape = '\\';
    private static char literalQuote = '"';
    
    /** Creates a new instance of BlockCommentTokenFactory */
    public StringLiteralTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return text.charAt(start) == literalQuote;
    }
    
    public Token createToken(String text, int start) {
        int position = start;
        char quote = text.charAt(start);
        position++;
        char c;
        while (position < text.length() && (c = text.charAt(position++)) != quote) {
            if (c == escape) ++position;
        }
        return createToken(text, start, position, StringLiteralTokenType.getInstance());
    }
}
