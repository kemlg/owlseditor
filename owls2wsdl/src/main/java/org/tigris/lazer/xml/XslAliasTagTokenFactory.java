/*
 * TagTokenFactory.java
 *
 * Created on 01 March 2004, 23:45
 */

package org.tigris.lazer.xml;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class XslAliasTagTokenFactory extends AbstractTokenFactory {
    
    private static String START_TAG = "<xslout:";
    private static String END_TAG = "</xslout:";
    
    /** Creates a new instance of XslTagTokenFactory */
    public XslAliasTagTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return regionMatches(text, start, START_TAG)
            || regionMatches(text, start, END_TAG);
//        return text.startsWith(START_TAG)
//            || text.startsWith(END_TAG);
    }
    
    public Token createToken(String text, int start) {
        int end = indexOf(text, '>', start+8, 1);
        return createToken(text, start, end, XslAliasTagTokenType.getInstance());
    }
}
