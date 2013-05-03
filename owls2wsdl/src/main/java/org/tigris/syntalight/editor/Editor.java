package org.tigris.syntalight.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;

import java.awt.Font;

/**
 * @author Bob Tarling
 */
public class Editor extends JEditorPane {

    public Editor() {
//        this.setStyle("xml");
//        this.setFont(new Font("Dialog", Font.PLAIN, 11));
    }

    public void read(File file) throws FileNotFoundException, IOException {    
        System.out.println("Reading file");
        String extension = "";
        if (file.getName().endsWith(".java") 
                || file.getName().endsWith(".xml") 
                || file.getName().endsWith(".xsl")) {
                    
            String filename = file.getName();
            extension = filename.substring(filename.lastIndexOf('.')+1);
            System.out.println("Style = " + extension);
            setStyle(extension);
        }
        FileInputStream fis = new FileInputStream(file);
        System.out.println("Reading filestream");
        read( fis, null );
        System.out.println("Closing filestream");
        fis.close();
        System.out.println("Initiating highlighting");
        initiateHighlighting();
    }
    
    public void setStyle(final String style) {
        System.out.println("Creating EditorKit");
        EditorKit editorKit = new StyledEditorKit() {
            public Document createDefaultDocument() {
                System.out.println("New SyntaxDocument");
                return new SyntaxDocument(style);
            }
        };

        System.out.println("Setting EditorKit");
        setEditorKitForContentType("text/java", editorKit);
        System.out.println("Setting content type");
        setContentType("text/java");
    }
    
    public void initiateHighlighting() {
        if (getDocument() instanceof SyntaxDocument) {
            SyntaxDocument doc = (SyntaxDocument)getDocument();
            System.out.println("initiating highlight on SyntaxDocument " + doc);
            try {
                doc.initiateHighlighting();
            } catch (BadLocationException e) {
                System.out.println("Failed to highlight document");
            }
        } else {
            System.out.println("Failed to initiating highlight");
        }
    }
//    
//    public void abandonHighlighting() {
//        if (getDocument() instanceof SyntaxDocument) {
//            SyntaxDocument doc = (SyntaxDocument)getDocument();
//            System.out.println("abandoning highlight on SyntaxDocument " + doc);
//            doc.abortThread();
//            try {
//                doc.initiateHighlighting();
//            } catch (BadLocationException e) {
//                System.out.println("Failed to highlight document");
//            }
//        } else {
//            System.out.println("Failed to initiating highlight");
//        }
//    }
}
