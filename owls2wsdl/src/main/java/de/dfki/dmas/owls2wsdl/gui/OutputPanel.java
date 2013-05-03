/*
 * OutputPanel.java
 *
 * Created on 4. Dezember 2006, 13:03
 *
 * Copyright (C) 2007
 * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
 * Hochschule fuer Technik und Wirtschaft (HTW) des Saarlandes
 * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
 *
 * The code is free for non-commercial use only.
 * You can redistribute it and/or modify it under the terms
 * of the Mozilla Public License version 1.1  as
 * published by the Mozilla Foundation at
 * http://www.mozilla.org/MPL/MPL-1.1.txt
 */

package de.dfki.dmas.owls2wsdl.gui;

import java.net.URI;
import java.net.URL;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.WindowConstants;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.tigris.syntalight.editor.Editor;

import com.javatutor.insel.gui.swing.PopupMenuMouseListener;
import com.jgoodies.looks.Options;
import com.jgoodies.uif_lite.component.Factory;

import de.dfki.dmas.owls2wsdl.core.*;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

import de.dfki.dmas.owls2wsdl.grounding.*;
import javax.wsdl.WSDLException;
import org.mindswap.wsdl.WSDLTranslator;

// pimp my swing gui
// https://swingutil.dev.java.net/docs/api1.5/org/wonderly/swing/tabs/CloseableTabbedPane.html
import org.wonderly.swing.tabs.*;


/**
 *
 * @author Oliver Fourman
 */
public class OutputPanel extends JPanel {
    
    static  PrintStream STD_OUT = System.out;
    private CloseableTabbedPane tabbedPane; // JTabbedPane
    private JToolBar outputToolbar;
    
    final URL saveIconURL    = ToolBar.class.getResource( "/images/16x16etool/save_edit.gif");
    final URL saveAllIconURL = ToolBar.class.getResource( "/images/16x16etool/saveall_edit.gif");
    final Icon saveIcon      = new ImageIcon( saveIconURL );
    final Icon saveAllIcon   = new ImageIcon( saveAllIconURL );
    
    private FileFilter directoryFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory();
        }
        public String getDescription() {
            return "directory";
        }
    };
    
    /** Creates a new instance of OutputPanel */
    public OutputPanel() {
        super(new BorderLayout());
        this.tabbedPane = new CloseableTabbedPane(JTabbedPane.TOP);
        this.tabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);        
        this.tabbedPane.addTabCloseListener( new TabCloseListener() {
            public void tabClosed(TabCloseEvent e) {
                System.out.println("CLOSE TAB: "+e.getClosedTab());
                tabbedPane.removeTabAt(e.getClosedTab());
            }
        });
        this.outputToolbar = new JToolBar();
        
        JButton saveSelectedButton = new JButton(
                ResourceManager.getString("iframe.output.save"),
                saveIcon);
        saveSelectedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //File outputdir = showSave2DirectoryChooser();
                //if(outputdir != null)
                //    saveSelectedTab(outputdir);
                
                File exportFile = showSave2Chooser();
                if(exportFile != null) {
                    saveSelectedTab(exportFile);
                }
            }
        });
        this.outputToolbar.add(saveSelectedButton);
        
        JButton saveAllButton = new JButton(
                ResourceManager.getString("iframe.output.saveall"),
                saveAllIcon);
        saveAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File outputdir = showSave2DirectoryChooser();
                if(outputdir != null)
                    saveAllTabs(outputdir);
            }
        });        
        this.outputToolbar.add(saveAllButton);
        
        this.add(this.tabbedPane, BorderLayout.CENTER);
        this.add(this.outputToolbar, BorderLayout.SOUTH);
    }
    
    public void addOutput(AbstractDatatype curtype)
    {   
        assert RuntimeModel.getInstance().getProject() != null;
        
        //JTextPane outputTextPane = new JTextPane();
        //JTextArea outputTextPane = new JTextArea();
        JEditorPane outputTextPane = new Editor();
        outputTextPane.setContentType("text/plain");
        outputTextPane.setEditable(false);        
        
        JButton b=new JButton("X");
        
        RedirectablePrintStream redirectedOutStream = new RedirectablePrintStream(STD_OUT, outputTextPane, createOutAttributeSet());
        redirectedOutStream.setRedirect(true);
        //System.setOut(redirectedOutStream); // not needed because toXSD prints a lot of status information
        
        XsdSchemaGenerator xsdgen = RuntimeModel.getInstance().getProject().buildXsdSchemaGenerator();
        try {
            xsdgen.toXSD(curtype);
            xsdgen.printXSD(redirectedOutStream);
        }
        catch(Exception exception) {
            System.err.println("Error: "+exception.toString());
            exception.printStackTrace();
        }
                
        redirectedOutStream.setRedirect(false);
        
        this.tabbedPane.addTab(curtype.getLocalName()+".xsd", Factory.createStrippedScrollPane(outputTextPane));
    }
    
    public void addOutput(AbstractService service)
    {
        assert RuntimeModel.getInstance().getProject() != null;
        
        //JTextPane outputTextPane = new JTextPane();
        //JTextArea outputTextPane = new JTextArea();
        JEditorPane outputTextPane = new Editor();
        outputTextPane.setContentType("text/plain");
        outputTextPane.setEditable(false);
        
        RedirectablePrintStream redirectedOutStream = new RedirectablePrintStream(STD_OUT, outputTextPane, createOutAttributeSet());
        redirectedOutStream.setRedirect(true);
        
        XsdSchemaGenerator xsdgen = RuntimeModel.getInstance().getProject().buildXsdSchemaGenerator();        
        System.out.println("[marshall AbstractService as WSDL] "+service.toString());
                        
        try {
            javax.wsdl.Definition wsdl = WSDLBuilder.getInstance().buildDefinition(service, xsdgen);
            WSDLBuilder.getInstance().printSchema(wsdl, redirectedOutStream);
            //this.tabbedPane.addTab(service.getID()+".wsdl", Factory.createStrippedScrollPane(outputTextPane));
            int index = service.getLocalFilename().lastIndexOf(".");
            String tabname = service.getLocalFilename().substring(0,index)+".wsdl";
            this.tabbedPane.addTab(tabname, Factory.createStrippedScrollPane(outputTextPane));
        }
        catch(WSDLException e) {
            System.out.println("[e] WSDLException: "+e.getMessage());
        }
        catch(Exception e) {
            System.err.println("[e] Exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addOWLSOutput(AbstractService aService) {
        assert RuntimeModel.getInstance().getProject() != null;
        
        //JTextPane outputTextPane = new JTextPane();
        //JTextArea outputTextPane = new JTextArea();
        JEditorPane outputTextPane = new Editor();
        outputTextPane.setContentType("text/plain");
        outputTextPane.setEditable(false); 
                
        RedirectablePrintStream redirectedOutStream = new RedirectablePrintStream(STD_OUT, outputTextPane, createOutAttributeSet());
        redirectedOutStream.setRedirect(true);
        
        XsdSchemaGenerator xsdgen = RuntimeModel.getInstance().getProject().buildXsdSchemaGenerator();
        System.out.println("[marshall AbstractService as OWL-S] "+aService.toString());
        
        try {
            javax.wsdl.Definition wsdlDef = WSDLBuilder.getInstance().buildDefinition(aService, xsdgen);
            
            WsdlGroundingBuilder groundingBuilder = new WsdlGroundingBuilder();
            WSDLTranslator t = groundingBuilder.translate(wsdlDef, aService);
            
            t.writeOWLS(redirectedOutStream);
            //this.tabbedPane.addTab(aService.getID()+".owls", Factory.createStrippedScrollPane(outputTextPane));
            this.tabbedPane.addTab(aService.getLocalFilename(), Factory.createStrippedScrollPane(outputTextPane));
            
//            System.out.println("[i] starting owl-s validator");
//            OWLSValidator validator = new OWLSValidator();
//            try {
//                System.out.println("TEMP: "+System.getenv("TEMP"));
//                URI tempURI = URI.create(System.getenv("TEMP")+"/owlsvalidator-report.txt");
//                File temp = new File(tempURI);
//                FileOutputStream fos = new FileOutputStream(temp);
//                t.writeOWLS(fos);
//                fos.close();
//                System.out.println("Saved in TEMP:" + temp.toString());                
//                OWLSValidatorReport report = validator.validate(tempURI);
//                //temp.delete();
//                
//                if(!report.getMessages().isEmpty()) {
//                    JOptionPane.showMessageDialog(
//                        this,
//                        report.getMessages(),
//                        "Validation Report",
//                        JOptionPane.ERROR_MESSAGE);
//                }
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
        }
        catch(WSDLException e) {
            System.out.println("[e] WSDLException: "+e.getMessage());
        }
        catch(Exception e) {
            System.err.println("[e] Exception: "+e.getMessage());
        }
    }
        
    private SimpleAttributeSet createOutAttributeSet() {
        SimpleAttributeSet outAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(outAttributes, Color.black);
        StyleConstants.setBold(outAttributes, false);
        return outAttributes;
    }
    
    public void saveAllTabs(File directory) {
        for(int i=0; i<this.tabbedPane.getTabCount(); i++) {
            File exportFile = new File(directory.getPath()+File.separator+this.tabbedPane.getTitleAt(i));
            this.saveSingleTab(exportFile, i);
        }
    }
    
    public void saveSelectedTab(File exportFile) {
        this.saveSingleTab(exportFile, this.tabbedPane.getSelectedIndex());
    }
    
    private void saveSingleTab(File exportFile, int pos) {
        System.out.println("[OutputPanel] saveSingleTab "+pos+": "+exportFile.getAbsolutePath());
        String buffer = ((JEditorPane)((JScrollPane)this.tabbedPane.getComponent(pos)).getViewport().getView()).getText();        
        FileWriter fw = null;
        try 
        { 
            //fw = new FileWriter( directory.getPath()+File.separator+this.tabbedPane.getTitleAt(pos) ); 
            fw = new FileWriter(exportFile); 
            fw.write( buffer ); 
        } 
        catch ( IOException e ) { 
            System.err.println( "[e] "+e.getMessage()+", konnte Datei nicht erstellen" ); 
        } 
        finally { 
            if ( fw != null ) {
                try { fw.close(); } catch ( IOException e ) { } 
                System.out.println("[i] "+exportFile.getAbsolutePath()+" saved.");
            }
        }
    }
    
    private File showSave2DirectoryChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);        
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(directoryFilter);
        
        if(OWLS2WSDLSettings.getInstance().containsKey("EXPORT_PATH")) {
            fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH")));
        }
        
        int returnVal = fc.showSaveDialog(null);
        if(returnVal != JFileChooser.APPROVE_OPTION)
            return null;
        
        File selectedDirectory = fc.getSelectedFile();
        if(!selectedDirectory.exists()) {
            JOptionPane.showMessageDialog(
                null,
                selectedDirectory.getAbsolutePath() + "does not exist!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return selectedDirectory;
    }
    
    private File showSave2Chooser() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);        
        fc.setMultiSelectionEnabled(false);
        
//        if(OWLS2WSDLSettings.getInstance().containsKey("EXPORT_PATH")) {
//            fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH")));
//        }
        System.out.println("[OutputPanel] EXPORT DIR: "+OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH"));
        System.out.println("[OutputPanel] TAB NAME  : "+this.tabbedPane.getTitleAt(this.tabbedPane.getSelectedIndex()));
        fc.setSelectedFile(new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH")+File.separator+this.tabbedPane.getTitleAt(this.tabbedPane.getSelectedIndex())));
        
        int returnVal = fc.showSaveDialog(null);
        if(returnVal != JFileChooser.APPROVE_OPTION)
            return null;

        File selFile = fc.getSelectedFile();
        System.out.println("[OutputPanel] FILE to save: "+selFile.getAbsolutePath());
        return selFile;
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (Exception e) {
            // Likely PlasticXP is not in the class path; ignore.
        }
        JFrame frame = new JFrame("Example - Abstract Datatype Output Panel");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        frame.setPreferredSize(new Dimension(800,600));
        
        OutputPanel p = new OutputPanel();
        try {
            //p.addOutput( AbstractDatatypeKB.getInstance().data.get("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhiteWine"));
            
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setVisible(true); 
        frame.validate();
        frame.pack();        
    }
    
    public static void main(String[] args) {
//        AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB("file:/D:/tmp/KB/KB_Student-MAP.xml");
//                        
//        //Schedule a job for the event-dispatching thread:
//        //creating and showing this application's GUI.
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();actionPerf
//            }
//        });
        
        JFrame f = new JFrame(); 
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 

        final JPopupMenu popmen = new JPopupMenu(); 
        final JTextArea textArea = new JTextArea(); 

        f.add( new JScrollPane(textArea) ); 

        JMenuItem entryHead = new JMenuItem( "Kompaktstaubsauger" ); 
        popmen.add( entryHead ); 
        popmen.addSeparator();
        
        String[] models = { "AEG Vampyrino SX" , "Electrolux Clario Z 1941" ,
                         "Quelle Privileg Piccolino" , "Siemens Super T120VS12A00",
                         "Hoover Micro Power Electronic" , "Rowenta dymbo"};
        
        for ( int i=0; i<models.length; i++) 
        { 
            popmen.add( new AbstractAction(models[i]) { 
                public void actionPerformed( ActionEvent e ) { 
                    textArea.append( e.getActionCommand() + "\n" ); 
                }
            }); 
        }
        textArea.addMouseListener( new PopupMenuMouseListener(popmen) ); 

        f.setSize( 300, 300 ); 
        f.setVisible( true );         
    }   
    
    /* 
     * @description: class can used standalone or to redirect System.out
     */
    static class RedirectablePrintStream extends PrintStream 
    {
        //JTextPane   textPane;
        JEditorPane textPane;
        //JTextArea   textPane;
        boolean redirect;
        SimpleAttributeSet attributeSet;
        public RedirectablePrintStream(OutputStream out, JEditorPane textPane, SimpleAttributeSet attributeSet) {
            super(out);
            this.textPane = textPane;
            this.attributeSet = attributeSet;
        }
        
        public void write(byte[] buf, int off, int len) 
        {
            if (redirect) {
                try {
                    int startOffSet = textPane.getDocument().getLength();
                    String buffer = new String(buf, off, len);
                    int count = 0;
                    while(buffer.contains("\r")) {
                        buffer = buffer.replaceFirst("\r", "");
                        count++;                            
                    }
                    buffer.trim();                    
                    textPane.getDocument().insertString(startOffSet, buffer, attributeSet);
                    textPane.setCaretPosition(startOffSet + len-count);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            } else {
                super.write(buf, off, len);
            }
        }
        public boolean isRedirect() {
            return redirect;
        }
 
        public void setRedirect(boolean redirect) {
            this.redirect = redirect;
        }
    }
}
