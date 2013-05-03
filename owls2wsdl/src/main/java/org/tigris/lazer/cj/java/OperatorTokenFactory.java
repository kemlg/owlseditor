/*
 * OperatorTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.cj.java;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.OperatorTokenType;
import org.tigris.lazer.Token;

/**
 * A factory for creating a Token of type OperatororTokenType
 * 
 * @author Bob Tarling
 */
public class OperatorTokenFactory extends AbstractTokenFactory {
    
    private static String operators = ".,<>=!%&|*+-/=^{}[]();~";
    
    /** Creates a new instance of OperatorTokenFactory */
    public OperatorTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return operators.indexOf(text.charAt(start)) != -1;
    }
    
    public Token createToken(String text, int start) {
        int position = start;
        position++;
        return createToken(text, start, position, OperatorTokenType.getInstance());
    }
}
