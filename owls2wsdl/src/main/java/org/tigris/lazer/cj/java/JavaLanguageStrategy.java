/*
 * JavaLanguageStrategy.java
 *
 * Created on 16 September 2003, 00:11
 */

package org.tigris.lazer.cj.java;

import org.tigris.lazer.LanguageStrategy;

/**
 *
 * @author Bob Tarling
 */
public class JavaLanguageStrategy extends LanguageStrategy {

    private static final JavaLanguageStrategy INSTANCE = new JavaLanguageStrategy();
    
    /** Creates a new instance of JavaLanguageStrategy */
    private JavaLanguageStrategy() {
        tokenFactories.add(new BlockCommentTokenFactory());
        tokenFactories.add(new LineCommentTokenFactory());
        tokenFactories.add(new WhiteSpaceTokenFactory());
        tokenFactories.add(new StringLiteralTokenFactory());
        tokenFactories.add(new CharacterLiteralTokenFactory());
//        tokenFactories.add(new NumberLiteralTokenFactory());
        tokenFactories.add(new OperatorTokenFactory());
        tokenFactories.add(new DefaultTokenFactory());
    }
    
    public static JavaLanguageStrategy getInstance() {
        return INSTANCE;
    }
}
