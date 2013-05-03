/*
 * ExportWSDLFrame.java
 *
 * Created on 1. Februar 2007, 17:45
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

import de.dfki.dmas.owls2wsdl.gui.models.ServiceListModel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;

import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

import java.util.Vector;
import java.util.Iterator;

import java.lang.Thread;

import de.dfki.dmas.owls2wsdl.core.AbstractService;
import de.dfki.dmas.owls2wsdl.core.*;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

/**
 * Exports service information as WSDL.
 * @author Oliver Fourman
 */
public class ExportWSDLFrame extends JDialog implements ActionListener {
    
    private WSDLOWLSExport exportThread;
    
    private JPanel contentPane;    
    private JPanel exportPanel;
    
    private JList translatableServiceList;
    private JList nonTranslatableServiceList;
    private JCheckBox buildRelativePathCheckBox;
    private JCheckBox generateWSDLCheckBox;
    private JCheckBox generateOWLSCheckBox;
    
    private File exportDirectory;
    
    private JTextField urlField;
    private JTextArea exportLog;
    
    private String translatableCount;
    private String nontranslatableCount;    
    private JLabel countInformation = new JLabel();
    
    private static final String SET_EXPORT_PATH   = "set export directory";
    private static final String CHECK_STARTBUTTON = "enable startbutton";
    private static final String START_EXPORT      = "exports services in translatable service list";
    private static final String DISPOSE_WINDOW    = "cancel and dispose window";
    
    private JButton startExportButton;
    
    private static final Dimension PREFERRED_SIZE =
        LookUtils.IS_LOW_RESOLUTION 
            ? new Dimension(500, 640) 
            : new Dimension(500, 980);
    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(this.SET_EXPORT_PATH)) {
            try {
                doBrowse();
                checkStartExportButton();
            }
            catch(java.io.IOException ioExc) {
                System.err.println("IOException: "+ioExc.getMessage());
            }
            catch(java.lang.Exception exc) {
                System.err.println("Exception: "+exc.getMessage());
                exc.printStackTrace();
            }
        }
        else if(e.getActionCommand().equals(this.START_EXPORT)) {
            if(this.exportDirectory.exists()) {
                try {
                    doExport();
                }
                catch(java.io.FileNotFoundException fnfe) {
                    System.err.println("[e] FileNotFoundException: "+fnfe.getMessage());
                }
                catch(java.io.IOException ioe) {
                    System.err.println("[e] IOException: "+ioe.getMessage());
                }                
            }
            else {
                JOptionPane.showMessageDialog(
                        this,
                        "Export path " + this.exportDirectory.getAbsolutePath() + " does not exists.\nNo WSDL files created.",
                        "Export WSDL Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        else if(e.getActionCommand().equals(this.CHECK_STARTBUTTON)) {
            this.checkStartExportButton();
        }
        else if(e.getActionCommand().equals(this.DISPOSE_WINDOW)) {
            this.dispose();
        }
        else {
            System.out.println("???");
        }
    }
    
    /** Creates a new instance of ExportWSDLFrame */
    public ExportWSDLFrame() {
        System.out.println("[C] ExportWSDLFrame");
        
        urlField = new JTextField(10);
        urlField.setEditable(false);
                
        if(OWLS2WSDLSettings.getInstance().containsKey("EXPORT_PATH")) {
            this.exportDirectory = new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH"));
            this.urlField.setText(this.exportDirectory.getAbsolutePath()); //getCanonicalPath());
        }       
        
        this.setTitle("OWLS2WSDL :: Export WSDL and OWL-S");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setPreferredSize(PREFERRED_SIZE);
        
        this.contentPane = new JPanel( new BorderLayout() );
        this.setContentPane(this.contentPane);
        this.contentPane.setBorder(Borders.createEmptyBorder(
                Sizes.pixel(10),
                Sizes.pixel(10),
                Sizes.pixel(10),
                Sizes.pixel(10)));
        
        translatableServiceList = new JList(new ServiceListModel());
        translatableServiceList.setVisibleRowCount(7);
        translatableServiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nonTranslatableServiceList = new JList(new ServiceListModel());
        nonTranslatableServiceList.setVisibleRowCount(7);
        nonTranslatableServiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildRelativePathCheckBox = new JCheckBox("use relative paths");
        generateWSDLCheckBox = new JCheckBox("WSDL");
        generateWSDLCheckBox.setActionCommand(this.CHECK_STARTBUTTON);
        generateWSDLCheckBox.addActionListener(this);
        generateOWLSCheckBox = new JCheckBox("reconstruct OWL-S from internal metamodel");
        generateOWLSCheckBox.setActionCommand(this.CHECK_STARTBUTTON);
        generateOWLSCheckBox.addActionListener(this);
        
        translatableCount = "0";
        nontranslatableCount = "0";        
        
        exportLog = new JTextArea("", 6, 20);        
        
        exportPanel = buildExportSubPanel(false);        
        JPanel commandPanel = buildCommandSubPanel();
        
        this.add(exportPanel, BorderLayout.CENTER);
        this.add(commandPanel, BorderLayout.SOUTH);               
        this.pack();
        this.locateOnScreen(this);
        
        this.exportThread = new WSDLOWLSExport(this);
    }
    
    private JPanel buildExportSubPanel(boolean debugmode) {
        FormLayout layout = new FormLayout("left:10dlu, fill:pref:grow, 10px, fill:pref");
        
        DefaultFormBuilder builder = null;
        if(debugmode)
            builder = new DefaultFormBuilder(layout, new FormDebugPanel());
        else
            builder = new DefaultFormBuilder(layout);
        
        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);
                        
        builder.appendSeparator("Translatable Services");
        JScrollPane scrollPane_1 = new JScrollPane(translatableServiceList);
        scrollPane_1.setPreferredSize(translatableServiceList.getPreferredScrollableViewportSize());
        builder.append(scrollPane_1, 3);        
        
        builder.appendSeparator("Non-Translatable Services");
        JScrollPane scrollPane_2 = new JScrollPane(nonTranslatableServiceList);
        scrollPane_2.setPreferredSize(nonTranslatableServiceList.getPreferredScrollableViewportSize());
        builder.append(scrollPane_2, 3);
        
        builder.appendSeparator("Export Settings");                
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //checkPanel.setBackground(Color.blue);
        checkPanel.add(generateWSDLCheckBox);
        checkPanel.add(generateOWLSCheckBox);
        //checkPanel.add(buildRelativePathCheckBox);        
        builder.append(checkPanel, 3);        
        builder.nextLine();
        
        JButton browseButton = new JButton("Path...");
        browseButton.setActionCommand(this.SET_EXPORT_PATH);
        browseButton.addActionListener(this);
                
        builder.append(urlField, 1);
        builder.append(browseButton, 1);
        
        //builder.appendGlueColumn();
        
        builder.appendSeparator("Export Log");
        
        JScrollPane scrollPane_3 = new JScrollPane(exportLog);
        //scrollPane_3.setPreferredSize(exportLog.getPreferredScrollableViewportSize());
        builder.append(scrollPane_3, 3);
        
        layout.setRowSpec(17, new com.jgoodies.forms.layout.RowSpec("fill:pref:grow"));
        
        //builder.append("Export Status #: ", new JLabel("n"), true);
        
        if(debugmode)
            FormDebugUtils.dumpAll(builder.getPanel());
        
        return builder.getPanel();
    }
       
    private JPanel buildCommandSubPanel() {
        JPanel cmdPanel = new JPanel();
        
        startExportButton = new JButton("Start Export...");        
        startExportButton.setActionCommand(this.START_EXPORT);
        startExportButton.addActionListener(this);
        startExportButton.setEnabled(false);
        
        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand(this.DISPOSE_WINDOW);
        closeButton.addActionListener(this);
        
        cmdPanel.add(startExportButton);
        cmdPanel.add(closeButton);
        return cmdPanel;
    }
    
    public void update(Vector selectedServicesList) 
    {                
        this.exportThread.setSelectedServices(selectedServicesList);        
        
        this.translatableServiceList.removeAll();
        this.nonTranslatableServiceList.removeAll();
        
        for(Iterator it=selectedServicesList.iterator(); it.hasNext(); ) {
            AbstractService aService = (AbstractService)it.next();
            if(aService.istranslatable()) {
                ((ServiceListModel)this.translatableServiceList.getModel()).addAbstractService(aService);
            }
            else {                
                ((ServiceListModel)this.nonTranslatableServiceList.getModel()).addAbstractService(aService);
            }
        }
        
        this.translatableServiceList.revalidate();
        this.translatableServiceList.updateUI();
        this.nonTranslatableServiceList.revalidate();
        this.nonTranslatableServiceList.updateUI();
        
        this.translatableCount = Integer.toString(((ServiceListModel)this.translatableServiceList.getModel()).getSize());
        this.nontranslatableCount = Integer.toString(((ServiceListModel)this.nonTranslatableServiceList.getModel()).getSize());
        
        this.countInformation.setText("Translatable: "+this.translatableCount+" Non-Translatable: "+this.nontranslatableCount);
        
//        for(int i=0; i<exportPanel.getComponentCount(); i++) {
//            System.out.println("COMP: "+exportPanel.getComponent(i).toString());
//        }
    }
    
    private void checkStartExportButton() 
    {
        System.out.println("Translatable services: "+this.translatableServiceList.getModel().getSize());
        if(this.translatableServiceList.getModel().getSize() > 0 && 
           this.exportDirectory.isDirectory() && ( this.generateWSDLCheckBox.isSelected() || this.generateOWLSCheckBox.isSelected() ) ) {
            this.startExportButton.setEnabled(true);
        }
        else {
            this.startExportButton.setEnabled(false);
        }
    }
    
    public static void main(String[] args) {
        ExportWSDLFrame ef = new ExportWSDLFrame();
        ef.setVisible(true);
        ef.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        try {
            AbstractDatatypeMapper.getInstance().loadAbstractDatatypeKB(new File("d:\\tmp\\KB\\KB_Student-MAP.xml"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        File f = new File("d:\\tmp\\serviceCollection1.xml");
        AbstractServiceCollection collection = AbstractServiceMapper.getInstance().loadAbstractServiceCollection(f);
        
        ef.update(collection.getServiceCollection()); // dummy to load at least OWLS2WSDL settings
    }
    
    /**
     * Locates the given component on the screen's center.
     */
    protected void locateOnScreen(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation(
            (screenSize.width  - paneSize.width)  / 2,
            (screenSize.height - paneSize.height) / 2);
    }
    
    
    private void doExport() throws java.io.IOException, java.io.FileNotFoundException
    {
        assert RuntimeModel.getInstance().getProject() != null;
        Project p = RuntimeModel.getInstance().getProject();
        this.exportThread.setProject(p);
        if(this.generateWSDLCheckBox.isSelected()) {
            this.exportThread.setWSDLExport(true);
        }
        if(this.generateOWLSCheckBox.isSelected()) {
            this.exportThread.setOWLSExport(true);
        }
        new Thread(this.exportThread).start();
    }
    
    public void appendLogMsg(String msg) {
        this.exportLog.append(msg);
        this.exportLog.revalidate();
        this.exportLog.updateUI();
    }
    
    private void doBrowse() throws java.io.IOException {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory();
                }
                public String getDescription() {
                    return "directory";
                }
            });
            
        if(OWLS2WSDLSettings.getInstance().containsKey("EXPORT_PATH")) {
            fc.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH")));
        }
        
        //In response to a button click:
        int returnVal = fc.showOpenDialog(null);

        if(returnVal != JFileChooser.APPROVE_OPTION)
            return;
        
        File selectedDirectory = fc.getSelectedFile();
        
        if(!selectedDirectory.exists()) {
            JOptionPane.showMessageDialog(
                null,
                selectedDirectory.getAbsolutePath() + "does not exist!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        else {
            try {                
                //this.urlField.setText(file.toURL().toExternalForm());
                this.urlField.setText(selectedDirectory.getCanonicalPath());
                System.out.println("EXPORT_PATH: "+selectedDirectory.getAbsolutePath());
                OWLS2WSDLSettings.getInstance().setProperty("EXPORT_PATH", selectedDirectory.getAbsolutePath());
                //OWLS2WSDLSettings.getInstance().savePropertyFile();                
            } catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Not a valid file path " + this.exportDirectory.getAbsolutePath() + "\n" + e,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }            
        }
    }
}
