/*
 * XmlLanguageStrategy.java
 *
 * Created on 16 September 2003, 00:11
 */

package org.tigris.lazer.xml;

import org.tigris.lazer.LanguageStrategy;

/**
 *
 * @author Bob Tarling
 */
public class XmlLanguageStrategy extends LanguageStrategy {

    private static final XmlLanguageStrategy INSTANCE = new XmlLanguageStrategy();
    
    /** Creates a new instance of JavaLanguageStrategy */
    private XmlLanguageStrategy() {
        tokenFactories.add(new BlockCommentTokenFactory());
        tokenFactories.add(new ProcessorInstructionTokenFactory());
        tokenFactories.add(new XslTagTokenFactory());
        tokenFactories.add(new XslAliasTagTokenFactory());
        tokenFactories.add(new SchemaTagTokenFactory());
        tokenFactories.add(new SoapTagTokenFactory());
        tokenFactories.add(new WsdlTagTokenFactory());
        tokenFactories.add(new XmlTagTokenFactory());
        tokenFactories.add(new DefaultTokenFactory());
    }
    
    public static XmlLanguageStrategy getInstance() {
        return INSTANCE;
    }
}
