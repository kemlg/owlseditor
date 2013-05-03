package org.tigris.syntalight.editor;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

import org.tigris.lazer.LanguageStrategy;
import org.tigris.lazer.Lazer;
import org.tigris.lazer.Token;

/**
 * @author Bob Tarling
 */
public class HighlightStrategy {
    private SyntaxDocument doc;
    private LanguageStrategy languageStrategy;
    private MutableAttributeSet normalAttributes;
    private Map attributeMapByTokenType = new HashMap();

    public HighlightStrategy(SyntaxDocument doc) {
        this.doc = doc;
        System.out.println("Created Highlighter strategy with document " + doc);
    }
    
    /*
     *  Parse the line to determine the appropriate highlighting
     */
    public void highlight() throws BadLocationException {
        do {
            String content = doc.getText(0, doc.getLength());

            doc.setRestartHighlightingRequested(false);
            int startOffset = 0;
            Lazer tokenizer = new Lazer(languageStrategy, content, Lazer.COMPOUND | Lazer.APPEND_WHITESPACE);
            Token token = null;
            System.out.println("Highlighting with " + languageStrategy.getClass().getName());
            System.out.println("Document " + doc);
            while (tokenizer.hasMoreElements()) {
                token = (Token)tokenizer.nextElement();
                AttributeSet as = (AttributeSet)attributeMapByTokenType.get(token.getType().getClass());
                if (as == null) as = normalAttributes;
                if (doc.isRestartHighlightingRequested()) break;
                doc.setCharacterAttributes(startOffset, token.getLength(), as, false);
                startOffset += token.getLength();
            }
        } while(doc.isRestartHighlightingRequested());
    }

    /**
     * @param strategy
     */
    protected void setLanguageStrategy(LanguageStrategy strategy) {
        languageStrategy = strategy;
    }
    
    protected void addAttributeSetForToken(Class tokenType, AttributeSet attributeSet) {
        attributeMapByTokenType.put(tokenType, attributeSet);
    }
    /**
     * @param set
     */
    protected void setNormalAttributes(MutableAttributeSet set) {
        normalAttributes = set;
    }

}
