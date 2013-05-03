/*
 * LanguageStrategy.java
 *
 * Created on 16 September 2003, 00:07
 */

package org.tigris.lazer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The base language strategy. This is to be extended with a concrete strategy
 * for each language implemented.
 * 
 * Each individual lanaguage strategy is responsible for defining a list of
 * token factories in their constructor. Each of these factories is scanned in order
 * to determine if it is applicable for the current text and called if so.
 * 
 * @author Bob Tarling
 */
public abstract class LanguageStrategy {
    
    protected Collection tokenFactories = new ArrayList();
    private AbstractTokenFactory lastFactory = null;
    
    /** Creates a new instance of LanguageStrategy */
    protected LanguageStrategy() {
    }
    
    /**
     * Extract a token from the start of the given String
     * @param text the text from which the token is to be extracted
     */
    public Token createToken(String text, int start) {
        // Loop round all the token factories to find the first one that
        // recognises the start of the text.
        Iterator it = tokenFactories.iterator();
        while (it.hasNext()) {
            AbstractTokenFactory factory = (AbstractTokenFactory)it.next();
            if (factory.isApplicable(text, start)) {
                // When the applicable factory is found, extract that
                // token and return it.
                lastFactory = factory;
                return factory.createToken(text, start);
            }
        }
        return null;
    }
}
