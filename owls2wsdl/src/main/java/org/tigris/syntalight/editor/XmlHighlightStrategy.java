package org.tigris.syntalight.editor;

import java.awt.Color;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.tigris.lazer.BlockCommentTokenType;
import org.tigris.lazer.TextTokenType;
import org.tigris.lazer.xml.ProcessorInstructionTokenType;
import org.tigris.lazer.xml.SchemaTagTokenType;
import org.tigris.lazer.xml.SoapTagTokenType;
import org.tigris.lazer.xml.WsdlTagTokenType;
import org.tigris.lazer.xml.XmlLanguageStrategy;
import org.tigris.lazer.xml.XmlTagTokenType;
import org.tigris.lazer.xml.XslAliasTagTokenType;
import org.tigris.lazer.xml.XslTagTokenType;

class XmlHighlightStrategy extends HighlightStrategy {

    public XmlHighlightStrategy(SyntaxDocument doc) {
        super(doc);
        
        setLanguageStrategy(XmlLanguageStrategy.getInstance());
        
        MutableAttributeSet normalAttributes = getAttributes(Color.black, true, false);
        MutableAttributeSet blockCommentAttributes = getAttributes(new Color(0,128,0), false, true);
        MutableAttributeSet xslTagAttributes = getAttributes(Color.blue, true, false);
        MutableAttributeSet xmlTagAttributes = getAttributes(Color.black, false, false);
        MutableAttributeSet xslAliasTagAttributes = getAttributes(Color.blue, false, false);
        MutableAttributeSet wsdlTagAttributes = getAttributes(Color.red, true, false);
        MutableAttributeSet schemaTagAttributes = getAttributes(Color.BLUE, false, false);
        MutableAttributeSet soapTagAttributes = getAttributes(Color.pink, true, false);
        MutableAttributeSet processorInstructionAttributes = getAttributes(Color.lightGray, false, false);

        setNormalAttributes(normalAttributes);
        
        addAttributeSetForToken(BlockCommentTokenType.class, blockCommentAttributes);
        addAttributeSetForToken(ProcessorInstructionTokenType.class, processorInstructionAttributes);
        addAttributeSetForToken(XmlTagTokenType.class, xmlTagAttributes);
        addAttributeSetForToken(XslTagTokenType.class, xslTagAttributes);
        addAttributeSetForToken(XslAliasTagTokenType.class, xslAliasTagAttributes);
        addAttributeSetForToken(SchemaTagTokenType.class, schemaTagAttributes);
        addAttributeSetForToken(SoapTagTokenType.class, soapTagAttributes);
        addAttributeSetForToken(WsdlTagTokenType.class, wsdlTagAttributes);
        addAttributeSetForToken(TextTokenType.class, normalAttributes);
    }
    
    private MutableAttributeSet getAttributes(Color color, boolean bold, boolean italic) {
        MutableAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        StyleConstants.setBold(attributes, bold);
        StyleConstants.setItalic(attributes, italic);
        return attributes;
    }
}
