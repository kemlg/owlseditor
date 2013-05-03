package org.tigris.lazer;

import java.util.HashMap;
import java.util.Map;

/**
 * A flyweight factory for creating tokens but reusing any exisiting tokens of
 * the same value.
 *
 * @author Bob Tarling
 * @stereotype factory
 */
public abstract class AbstractTokenFactory {

    /**
     * Previously created text tokens for flyweight pattern
     */
    protected static Map previousTokens = new HashMap();
    /**
     * If true then the tokens returned will contain the text
     * of the token. Some clients do not need to know the contents
     * of the token, just the length. Setting this to false gives
     * some small performance gain by lower object instantiation.
     */
    private boolean storeText = true;
    /**
     * If the reuse flag is set then the flyweight pattern is
     * abandoned and the factory simply returns the same token
     * populated with different data each time. This can only be
     * used for performance or memory resource situations where
     * the client code does not require the Token to be immutable.
     */
    private Token singleToken = null;
    //private Token singleToken = new Token(0, null);
        
    /** Creates a new instance of TokenFactory */
    public AbstractTokenFactory() {
    }

    /**
     * Create a Token of the given type form the string provided
     * @param text The string from which to extract the token
     * @param start The start position of the token in the text
     * @param end The end position of the token in the text
     * @param tokenType The type to assign to the token
     * @return the token
     */    
    public Token createToken(String text, int start, int end, TokenType tokenType) {
        if (storeText) {
            String tokenText = text.substring(start,end);
            if (singleToken != null) {
                singleToken.text = tokenText;
                singleToken.length = end-start;
                singleToken.type = tokenType;
                return singleToken;
            } else {
                Token token = (Token)previousTokens.get(tokenText);
                if (token == null) {
                    token = new Token(tokenText, tokenType);
                    previousTokens.put(tokenText, token);
                }
                return token;
            }
        } else {
            if (singleToken != null) {
                singleToken.length = end-start;
                singleToken.type = tokenType;
                return singleToken;
            } else {
//                return new Token(end-start, tokenType);
                Token token = new Token(end-start, tokenType);
                Token token2 = (Token)previousTokens.get(token);
                if (token2 != null) {
                    return token2;
                } else {
                    previousTokens.put(token, token);
                    return token;
                }
            }
        }
    }
    
    protected int indexOf(String text, char searchChar, int start, int offset) {
        int position = text.indexOf(searchChar, start);
        if (position == -1) {
            return text.length();
        } else {
            return position+offset;
        }
    }

    protected int indexOf(String text, String searchString, int start, int offset) {
        int position = text.indexOf(searchString, start);
        if (position == -1) {
            return text.length();
        } else {
            return position+offset;
        }
    }

    protected boolean regionMatches(String text, int start, String match) {
        return text.regionMatches(start, match, 0, match.length());
    }


    public abstract Token createToken(String text, int position);
    public abstract boolean isApplicable(String text, int start);
}
