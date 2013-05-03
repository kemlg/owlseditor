/*
 * NumberLiteralTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.cj.java;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/**
 *
 * @author Bob Tarling
 */
public class NumberLiteralTokenFactory //extends AbstractTokenFactory 
{
    
//    private static Pattern numberPattern = Pattern.compile("((0[xX][0-9A-Fa-f]+)|([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?[fFdD]?)");

//    private Matcher matcher;
    
    /** Creates a new instance of BlockCommentTokenFactory */
    public NumberLiteralTokenFactory() {
    }
    
//    public boolean isApplicable(String text) {
//        matcher = numberPattern.matcher(text);
//        return matcher.lookingAt();
//    }
    
//    public Token createToken(String text, int start) {
//        int position = matcher.end();
//        return TokenFactory.createToken(text.substring(start, position), NumberTokenType.getInstance());
//    }
}
