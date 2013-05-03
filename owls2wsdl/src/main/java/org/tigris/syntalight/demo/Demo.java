package org.tigris.syntalight.demo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.tigris.syntalight.editor.Editor;

/**
 * A small demo app to demonstrate Syntalight in action
 * @author Bob Tarling
 * @since 13-Mar-04
 */
public class Demo extends JFrame {

    Editor editor;
    
    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.setVisible(true);
    }
    
    public Demo() {
        editor = new Editor();
        editor.setStyle("java");
        editor.initiateHighlighting();
        this.getContentPane().add(new JButton(new OpenAction("Open Document")), BorderLayout.NORTH);
        this.getContentPane().add(new JScrollPane(editor));
    }
    
    private class OpenAction extends AbstractAction {

        public OpenAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent arg0) {
            JFileChooser jfc = new JFileChooser();
            jfc.showOpenDialog(editor);
            File file = jfc.getSelectedFile();
            try {
                editor.read(file);
            } catch(Exception e) {
            }
        }
    }
}

