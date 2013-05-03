package org.tigris.lazer.xml;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.TextTokenType;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class DefaultTokenFactory extends AbstractTokenFactory {
    
    private static final char XML_START_TAG = '<';
    
    /** Creates a new instance of DefaultTokenFactory */
    public DefaultTokenFactory() {
    }
    
    /**
     * DefaultTokenFactory is assumed to be the last token type checked by
     * the xml strategy. It is assumed to always be applicable if no other
     * factory has been found to be so.
     */
    public boolean isApplicable(String text, int start) {
        return true;
    }
    
    /**
     * This token is from the start of the input string to the character just
     * before the first start tag.
     */
    public Token createToken(String text, int start) {
        int end = indexOf(text, XML_START_TAG, start, 0);
        return createToken(text, start, end, TextTokenType.getInstance());
    }
}
