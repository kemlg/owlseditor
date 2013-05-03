package org.tigris.syntalight.editor;

import javax.swing.text.BadLocationException;

class Highlighter extends Thread {
    private HighlightStrategy highlightStrategy;
    private SyntaxDocument doc;
    
    public Highlighter(SyntaxDocument doc, HighlightStrategy highlightStrategy) {
        System.out.println("Creating highlighter");
        this.doc = doc;
        this.highlightStrategy = highlightStrategy;
    }

    /*
     *  Parse the line to determine the appropriate highlighting
     */
    public void highlight() throws BadLocationException {
        while (true) {
            highlightStrategy.highlight();
            try {
                synchronized (this) {
                    System.out.println("Thread is waiting " + this);
                    wait();
                    doc.setRestartHighlightingRequested(false);
                    System.out.println("Thread is continuing");
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void run() {
        System.out.println("Starting highlighter");
        try {
            highlight();
        } catch (BadLocationException e) {
        }
    }
}
