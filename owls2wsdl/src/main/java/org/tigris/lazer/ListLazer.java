package org.tigris.lazer;

import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author Bob Tarling
 */
public class ListLazer implements Enumeration {

    public static int COMPOUND_WHITE_SPACE = 1;
    //public static int COMPOUND_OPERATORS = 2;
    public static int IGNORE_WHITESPACE = 4;
    public static int IGNORE_COMMENTS = 8;

    private int mode = 0;
    
    private List tokens;

    private Token nextToken;
    
    private int position = 0;
    private int lastPosition = 0;
    private String script;
    private boolean compoundWhiteSpace;
    private boolean ignoreWhiteSpace;
    private boolean ignoreComments;

    private LanguageStrategy languageStrategy = null;

    /** Creates a new instance of LanguageTokenizer */
    public ListLazer(LanguageStrategy languageStrategy, String script, int mode) {
        this.languageStrategy = languageStrategy;
        this.compoundWhiteSpace = ((mode & COMPOUND_WHITE_SPACE) > 0);
        this.script = script;
    }
    
    /** Creates a new instance of LanguageTokenizer */
    public ListLazer(List tokens) {
        this(tokens, 0, 0);
    }
    
    /** Creates a new instance of LanguageTokenizer */
    public ListLazer(List tokens, int mode) {
        this(tokens, mode, 0);
    }
    
    /** Creates a new instance of LanguageTokenizer */
    public ListLazer(List tokens, int mode, int position) {
        this.position = position;
        this.compoundWhiteSpace = ((mode & COMPOUND_WHITE_SPACE) > 0);
        this.ignoreWhiteSpace = ((mode & IGNORE_WHITESPACE) > 0);
        this.ignoreComments = ((mode & IGNORE_COMMENTS) > 0);
        this.tokens = tokens;
    }
    
    public int getPosition() {
        if (tokens != null) {
            return lastPosition;
        } else {
            return position;
        }
    }
    
    /**
     * Determine the token that will be returned on the next call to
     * nextElement()
     */
    private Token getNextToken() {
        lastPosition = position;
        if (position == tokens.size()) {
            nextToken = null;
            return null;
        }
        nextToken = (Token)tokens.get(position++);

        while ((ignoreWhiteSpace && nextToken.getType() instanceof WhiteSpaceTokenType) ||
                 (ignoreComments && nextToken.getType() instanceof CommentTokenType)) {
            if (position == tokens.size()) {
                nextToken = null;
                return null;
            }
            nextToken = (Token)tokens.get(position++);
        }
        
        if (compoundWhiteSpace && nextToken.getType() instanceof WhiteSpaceTokenType) {
            while (position < tokens.size() && ((Token)(tokens.get(position))).getType() instanceof WhiteSpaceTokenType) {
                nextToken = nextToken.append((Token)tokens.get(position++));
            }
        }
        return nextToken;
    }
    
    public boolean hasMoreElements() {
        if (tokens != null) {
            return nextToken != null;
        } else {
            return position < script.length();
        }
    }
    
    public Object nextElement() {
        if (tokens != null) {
            Token returnToken = nextToken;
            nextToken = getNextToken();
            return returnToken;
        } else {
            Token returnToken = languageStrategy.createToken(script, position);
            if (returnToken != null) {
                position += returnToken.getLength();
            }
            return returnToken;
        }
    }
}
