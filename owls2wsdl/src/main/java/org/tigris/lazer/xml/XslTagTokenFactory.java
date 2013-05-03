/*
 * TagTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.xml;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class XslTagTokenFactory extends AbstractTokenFactory {
    
    private static String START_TAG = "<xsl:";
    private static String END_TAG = "</xsl:";
    
    /** Creates a new instance of XslTagTokenFactory */
    public XslTagTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return regionMatches(text, start, START_TAG)
            || regionMatches(text, start, END_TAG);
    }
    
    public Token createToken(String text, int start) {
        int end = indexOf(text, '>', start + START_TAG.length(), 1);
        return createToken(text, start, end, XslTagTokenType.getInstance());
    }
}
