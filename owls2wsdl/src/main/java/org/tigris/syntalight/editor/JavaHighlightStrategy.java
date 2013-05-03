package org.tigris.syntalight.editor;

import java.awt.Color;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.tigris.lazer.BlockCommentTokenType;
import org.tigris.lazer.CharacterLiteralTokenType;
import org.tigris.lazer.KeywordTokenType;
import org.tigris.lazer.LineCommentTokenType;
import org.tigris.lazer.OperatorTokenType;
import org.tigris.lazer.StringLiteralTokenType;
import org.tigris.lazer.TextTokenType;
import org.tigris.lazer.cj.java.JavaLanguageStrategy;

class JavaHighlightStrategy extends HighlightStrategy {

    public JavaHighlightStrategy(SyntaxDocument doc) {
        super(doc);
        System.out.println("Creating JavaHighlightStrategy");
        
        setLanguageStrategy(JavaLanguageStrategy.getInstance());
        
        MutableAttributeSet normalAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(normalAttributes, Color.black);
        StyleConstants.setItalic(normalAttributes, false);
        StyleConstants.setBold(normalAttributes, false);
        setNormalAttributes(normalAttributes);

        MutableAttributeSet blockCommentAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(blockCommentAttributes, new Color(0,128,0));
        StyleConstants.setItalic(blockCommentAttributes, true);
        StyleConstants.setBold(blockCommentAttributes, false);

        MutableAttributeSet lineCommentAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(lineCommentAttributes, new Color(0,192,0));
        StyleConstants.setItalic(lineCommentAttributes, true);
        StyleConstants.setBold(lineCommentAttributes, false);

        MutableAttributeSet keywordAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordAttributes, Color.blue);
        StyleConstants.setBold(keywordAttributes, true);
        StyleConstants.setItalic(keywordAttributes, false);

        MutableAttributeSet operatorAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(operatorAttributes, Color.red);
        StyleConstants.setBold(operatorAttributes, false);
        StyleConstants.setItalic(operatorAttributes, false);

        MutableAttributeSet stringLiteralAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(stringLiteralAttributes, Color.blue);
        StyleConstants.setBold(stringLiteralAttributes, false);
        StyleConstants.setItalic(stringLiteralAttributes, false);

        MutableAttributeSet characterLiteralAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(characterLiteralAttributes, Color.lightGray);
        StyleConstants.setBold(characterLiteralAttributes, false);
        StyleConstants.setItalic(characterLiteralAttributes, false);

        addAttributeSetForToken(BlockCommentTokenType.class, blockCommentAttributes);
        addAttributeSetForToken(LineCommentTokenType.class, lineCommentAttributes);
        addAttributeSetForToken(KeywordTokenType.class, keywordAttributes);
        addAttributeSetForToken(OperatorTokenType.class, operatorAttributes);
        addAttributeSetForToken(StringLiteralTokenType.class, stringLiteralAttributes);
        addAttributeSetForToken(CharacterLiteralTokenType.class, characterLiteralAttributes);
        addAttributeSetForToken(TextTokenType.class, normalAttributes);
    }
}
