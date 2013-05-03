/*
 * DefaultTokenFactory.java
 *
 * Created on 15 September 2003, 23:45
 */

package org.tigris.lazer.cj.java;

import java.util.TreeSet;

import org.tigris.lazer.AbstractTokenFactory;
import org.tigris.lazer.KeywordTokenType;
import org.tigris.lazer.TextTokenType;
import org.tigris.lazer.Token;

/**
 *
 * @author Bob Tarling
 */
public class DefaultTokenFactory extends AbstractTokenFactory {
    
    private static String whiteSpace = " \n\r\t";
    
    private static String operators = ".,<>=!%&|*+-/=^{}[]();~";

    private static String keywords[] = {
        "boolean",
        "byte",
        "catch",
        "char",
        "class",
        "continue",
        "default",
        "double",
        "else",
        "extends",
        "false",
        "final",
        "float",
        "for",
        "if",
        "import",
        "implements",
        "instanceof",
        "int",
        "interface",
        "new",
        "package",
        "private",
        "public",
        "return",
        "static",
        "synchronized",
        "this",
        "throw",
        "throws",
        "true",
        "try",
        "void",
        "volatile",
        "while"
    };

    private static TreeSet keywordSet;
 
    static {
        keywordSet = new TreeSet();
        for (int i=0; i < keywords.length; ++i) {
            keywordSet.add(keywords[i]);
        }
    }
    
    /** Creates a new instance of BlockCommentTokenFactory */
    public DefaultTokenFactory() {
    }
    
    public boolean isApplicable(String text, int start) {
        return true;
    }
    
    public Token createToken(String text, int start) {
        int position = start;
        // Read characters until whitespace or operator is found
        // If this is followed by a bracket then return a method token
        // else return a simple text token
        position++;
        while (position < text.length()
                && whiteSpace.indexOf(text.charAt(position)) == -1
                && operators.indexOf(text.charAt(position)) == -1
                && text.charAt(position) != '"'
                && text.charAt(position) != '\'' ) {
            ++position;
        }
        if (keywordSet.contains(text.substring(start, position))) {
            return createToken(text, start, position, KeywordTokenType.getInstance());
        }
        return createToken(text, start, position, TextTokenType.getInstance());
    }
}
