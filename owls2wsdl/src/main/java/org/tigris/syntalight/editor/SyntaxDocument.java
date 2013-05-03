package org.tigris.syntalight.editor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;

class SyntaxDocument extends DefaultStyledDocument {
    private boolean doHighlight = false;

    private Thread highlighterThread;

    private boolean restartHighlightingRequested;
    
    private HighlightStrategy highlightStrategy;
        
    public SyntaxDocument(String type) {
        System.out.println("Creating SyntaxDocument");
        putProperty( DefaultEditorKit.EndOfLineStringProperty, "\n" );
        if (type.equals("java")) {
            highlightStrategy = new JavaHighlightStrategy(this);
        } else {
            highlightStrategy = new XmlHighlightStrategy(this);
        }
        //highlighterThread = new Thread(new Highlighter(this, highlightStrategy));
        highlighterThread = new Highlighter(this, highlightStrategy);
        highlighterThread.setDaemon(true);
        highlighterThread.start();
    }

    /*
     *  Override to apply syntax highlighting after the document has been updated
     */
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        restartHighlightThread();
     }

    /*
     *  Override to apply syntax highlighting after the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        restartHighlightThread();
    }

    public void initiateHighlighting() throws BadLocationException {
        doHighlight = true;
        restartHighlightThread();
    }

    private void restartHighlightThread() {
        if (!doHighlight) return;
        setRestartHighlightingRequested(true);
        synchronized(highlighterThread) {
            System.out.println("Restarting highlighter thread " + highlighterThread);
            highlighterThread.notify();
            System.out.println("Notified highlighter thread " + highlighterThread);
        }
    }

    public synchronized void setRestartHighlightingRequested(boolean restartHighlighting) {
        this.restartHighlightingRequested = restartHighlighting;
    }

    public synchronized boolean isRestartHighlightingRequested() {
        return restartHighlightingRequested;
    }
}
