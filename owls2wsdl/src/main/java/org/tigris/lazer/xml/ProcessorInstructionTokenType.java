/*
 * ProcessorInstructionToken.java
 *
 * Created on 29 February 2004, 09:35
 */

package org.tigris.lazer.xml;

import org.tigris.lazer.TokenType;

/**
 * A token type to indicate a processor instruction tag.
 * @author Bob Tarling
 */
public class ProcessorInstructionTokenType extends TokenType {
    
    static private final ProcessorInstructionTokenType INSTANCE = 
        new ProcessorInstructionTokenType();
    
    /** Creates a new instance of CommentToken */
    private ProcessorInstructionTokenType() {
    }
    
    static public ProcessorInstructionTokenType getInstance() {
        return INSTANCE;
    }

    public String toString() {
        return "Processor Instruction";
    }
}
