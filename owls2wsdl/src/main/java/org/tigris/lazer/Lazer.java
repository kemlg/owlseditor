package org.tigris.lazer;

import java.util.Enumeration;

/**
 *
 * @author Bob Tarling
 */
public class Lazer implements Enumeration {

    public static int COMPOUND = 1;
    public static int APPEND_WHITESPACE = 2;
    public static int IGNORE_WHITESPACE = 4;
    public static int IGNORE_COMMENTS = 8;
    public static int COMPOUND_WHITESPACE = 16; // TODO - need a better solution for this.

    private int position = 0;
    private String script;
    private int mode;

    private LanguageStrategy languageStrategy = null;
    private Token nextToken;

    /** Creates a new instance of LanguageTokenizer 
     * @deprecated A seperate project should do handle this
     */
    public Lazer(TokenList tokenList, int mode, int position) {
    }
    
    /** Creates a new instance of LanguageTokenizer */
    public Lazer(LanguageStrategy languageStrategy, String script, int mode) {
        this.languageStrategy = languageStrategy;
        this.script = script;
        this.mode = mode;
        setupNextElementToReturn();
    }
    
    public boolean hasMoreElements() {
        return nextToken != null;
    }
    
    public Object nextElement() {
        Token returnToken = nextToken;
        setupNextElementToReturn();
        while (shouldCompound(mode, nextToken, returnToken)
                || shouldAppendWhitespace(mode, nextToken, returnToken)) {
            returnToken = returnToken.append(nextToken);
            setupNextElementToReturn();
        }
        return returnToken;
    }

    private boolean shouldCompound(int mode, Token nextToken, Token returnToken) {    
        return ((mode & COMPOUND) == COMPOUND 
                && nextToken != null
                && returnToken.getType() == nextToken.getType());
    }
    
    private boolean shouldAppendWhitespace(int mode, Token nextToken, Token returnToken) {    
        return ((mode & APPEND_WHITESPACE) == APPEND_WHITESPACE 
                && nextToken != null
                && nextToken.getType() == WhiteSpaceTokenType.getInstance());
    }
    
    private Token setupNextElementToReturn() {
        if (position < script.length()) {
            do {
                nextToken = languageStrategy.createToken(script, position);
                if (nextToken != null) {
                    position += nextToken.getLength();
                }
            } while (nextToken != null && shouldIgnore(nextToken));
        } else {
            nextToken = null;
        }
        return nextToken;
    }
    
    private boolean shouldIgnore(Token token) {
        if (token.getType() instanceof WhiteSpaceTokenType && (mode & IGNORE_WHITESPACE) == IGNORE_WHITESPACE) return true;
        if (token.getType() instanceof CommentTokenType && (mode & IGNORE_COMMENTS) == IGNORE_COMMENTS) return true;
        return false;
    }
}
