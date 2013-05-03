/*
 * ConfigurationFrame.java
 *
 * Created on 7. Januar 2007, 22:44
 * http://www.java2s.com/Code/Java/Swing-Components/Createandconfigurealayoutcreateabuilderaddcomponents.htm
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
//import javax.swing.JToggleButton;
import javax.swing.AbstractButton;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import java.io.File;
import java.io.IOException;

import java.awt.Font;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.Window;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;

import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatype;
import de.dfki.dmas.owls2wsdl.core.Project;

/**
 *
 * @author Oliver Fourman
 */
public class ConfigurationFrame extends JDialog implements ActionListener {
    
    private OWLS2WSDLGui mainWindowRef;
    
    private JPanel contentPane;    
    private JTabbedPane configTabbedPane;
    
    private static final String SET_APPL_PATH = "set application working path";
    private static final String SET_EXPORT_DIRECTORY = "set export directory";    
        
    private JTextField applPathField;
    private JButton    applPathBrowseButton;
    private JTextField logPathField;
    
    private JTextField persistentProjectDirectoryField;    
    private JTextField persistentDatatypeDirectoryField;
    private JCheckBox  jconsoleCheckBox;
    private JCheckBox  persistentDatatypeCheckBox;
        
    //private JTextField   xsdExportPathField;
    private JTextField   exportDirectoryField;
    private JButton      exportDirectoryBrowseButton;
    private JCheckBox    buildRelativePathCheckBox;
    
    private JComboBox    xsdTypeInheritanceBehaviour;    
    private JRadioButton shortButton;
    private JRadioButton hierarchyButton;
    private ButtonGroup  xsdSelectionGroup;
    
    private JCheckBox    addMetaTypesCheckBox;
    private JCheckBox    addAnnotationCheckBox;
    
    private JTextField   defaultElementDepthField;
    private JComboBox    projectElementDepthField;
    private JComboBox    projectDefaultXsdTypeField;
    
    private JRadioButton deLangButton;
    private JRadioButton enLangButton;    
    private ButtonGroup  lanSelectionGroup;
    
    private JRadioButton windowsLfButton;
    private JRadioButton plasticLfButton;
    private JRadioButton motifLfButton;
    private ButtonGroup  lfSelectionGroup;
    
    // WSDL / OWL-S Building
    private JTextField   wsdlNamespaceField;
    private JRadioButton autoNamespaceBaseSelectButton;
    private JRadioButton newNamespaceBaseSelectButton;
    private ButtonGroup  nsBaseSelectionGroup;
    
    //private JTextField   axisServicePathField;
    
    private JTextField   owlsBaseField;
    private JRadioButton autoOWLSBaseSelectButton;
    private JRadioButton newOWLSBaseSelectButton;
    private ButtonGroup  owlsBaseSelectionGroup;
    private JTextField   wsdldocPathField;
    private JRadioButton autoWsdldocPathSelectButton;
    private JRadioButton setWsdlDocPathSelectButton;
    private ButtonGroup  wsdldocPathSelectionGroup;
    
    protected static final Dimension PREFERRED_SIZE =
        LookUtils.IS_LOW_RESOLUTION 
            ? new Dimension(600, 500) 
            : new Dimension(600, 800);
    
    /** Creates a new instance of ConfigurationFrame */
    public ConfigurationFrame(OWLS2WSDLGui frame) {
        this.mainWindowRef = frame;
        this.setTitle("OWLS2WSDL :: Configuration");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setPreferredSize(ConfigurationFrame.PREFERRED_SIZE);
        
        this.contentPane = new JPanel( new BorderLayout() );
        //this.contentPane.setBackground(Color.WHITE);
        this.setContentPane(this.contentPane);
        this.contentPane.setBorder(Borders.createEmptyBorder(
                Sizes.pixel(10),
                Sizes.pixel(10),
                Sizes.pixel(10),
                Sizes.pixel(10)));
        
        int fieldLen = 28;
        
        this.applPathField = new JTextField(32);
        this.applPathField.setEditable(false);
        this.applPathField.setBackground(Color.WHITE);
        this.applPathBrowseButton = new JButton("Path...");
        this.applPathBrowseButton.setActionCommand(this.SET_APPL_PATH);
        this.applPathBrowseButton.addActionListener(this);
        
        this.logPathField = new JTextField(fieldLen);
        this.logPathField.setEditable(false);        
        
        this.persistentProjectDirectoryField  = new JTextField(fieldLen);        
        this.persistentDatatypeDirectoryField = new JTextField(fieldLen);
        this.persistentProjectDirectoryField.setEditable(false);
        this.persistentDatatypeDirectoryField.setEditable(false);
        this.persistentDatatypeCheckBox = new JCheckBox("load referenced datatypes only (experimental)");
        this.jconsoleCheckBox = new JCheckBox("activate JConsole at start time");
        
        //this.xsdExportPathField  = new JTextField(fieldLen);
        this.exportDirectoryField = new JTextField(32);
        this.exportDirectoryField.setEditable(false);
        this.exportDirectoryField.setBackground(Color.WHITE);
        this.exportDirectoryBrowseButton = new JButton("Path...");
        this.exportDirectoryBrowseButton.setActionCommand(this.SET_EXPORT_DIRECTORY);
        this.exportDirectoryBrowseButton.addActionListener(this);
        
        this.buildRelativePathCheckBox = new JCheckBox("extract in relative paths");
        this.buildRelativePathCheckBox.setSelected(false);
        this.buildRelativePathCheckBox.setEnabled(false);
        
        //this.applPathField.setFont(new Font("Dialog", Font.PLAIN, 10));
        //System.out.println("FONT_INFO: "+this.applPathField.getFont().toString());
        
        xsdTypeInheritanceBehaviour = new JComboBox();
        xsdTypeInheritanceBehaviour.addItem(AbstractDatatype.InheritanceByNone);
        xsdTypeInheritanceBehaviour.addItem(AbstractDatatype.InheritanceBySuperClassOnly);
        xsdTypeInheritanceBehaviour.addItem(AbstractDatatype.InheritanceByRDFTypeOnly);
        xsdTypeInheritanceBehaviour.addItem(AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond);
        xsdTypeInheritanceBehaviour.addItem(AbstractDatatype.InheritanceByParentsFirstRDFTypeSecond);
        
        Object[] primitiveSchemaTypes = {
                "http://www.w3.org/2001/XMLSchema#anyType",
                "http://www.w3.org/2001/XMLSchema#anyURI",
                "http://www.w3.org/2001/XMLSchema#string",
                "http://www.w3.org/2001/XMLSchema#decimal",
                "http://www.w3.org/2001/XMLSchema#integer",
                "http://www.w3.org/2001/XMLSchema#float",
                "http://www.w3.org/2001/XMLSchema#boolean",
                "http://www.w3.org/2001/XMLSchema#date",
                "http://www.w3.org/2001/XMLSchema#time" };
        projectDefaultXsdTypeField = new JComboBox(primitiveSchemaTypes);        
        
        shortButton = new JRadioButton("use ext. venetian blind (short)");
        shortButton.setBorderPainted(false);
        shortButton.setContentAreaFilled(false);
        shortButton.setSelected(true);       
        
        hierarchyButton = new JRadioButton("use hierarchy pattern (subclassing)");
        hierarchyButton.setBorderPainted(false);
        hierarchyButton.setContentAreaFilled(false);
        hierarchyButton.setSelected(false);
                
        xsdSelectionGroup = new ButtonGroup();
        xsdSelectionGroup.add(shortButton);
        xsdSelectionGroup.add(hierarchyButton);       
        
        addMetaTypesCheckBox  = new JCheckBox("add meta types to express OWL anonymous language terms");
        addAnnotationCheckBox = new JCheckBox("add XML Schema annotations (general and OWL information)");
        
        defaultElementDepthField = new JTextField(2);
        defaultElementDepthField.setText("0");
        
        projectElementDepthField = new JComboBox();
        for(int i=0; i<=10; i++) { projectElementDepthField.addItem(Integer.toString(i)); }        
        
        deLangButton = new JRadioButton("de");
        enLangButton = new JRadioButton("en");
        lanSelectionGroup = new ButtonGroup();
        lanSelectionGroup.add(deLangButton);
        lanSelectionGroup.add(enLangButton);        
        
        plasticLfButton = new JRadioButton("PlasticXP");
        plasticLfButton.setActionCommand("CHANGE_LF_TO_PLASTIC");
        plasticLfButton.addActionListener(this);
        plasticLfButton.setSelected(true);
        windowsLfButton = new JRadioButton("Windows");
        windowsLfButton.setActionCommand("CHANGE_LF_TO_WINDOWS");
        windowsLfButton.addActionListener(this);
        motifLfButton = new JRadioButton("Motif");
        motifLfButton.setActionCommand("CHANGE_LF_TO_MOTIF");
        motifLfButton.addActionListener(this);
        
        lfSelectionGroup = new ButtonGroup();        
        lfSelectionGroup.add(plasticLfButton);
        lfSelectionGroup.add(windowsLfButton);
        lfSelectionGroup.add(motifLfButton);
        
        // EXPORT / WSDL /OWL-S Config-Panel
        this.wsdlNamespaceField = new JTextField(fieldLen);
        this.autoNamespaceBaseSelectButton = new JRadioButton("auto (using parsed service data)");
        this.autoNamespaceBaseSelectButton.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e )
            {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("autoNamespaceBaseSelectButton selected");
                    wsdlNamespaceField.setEditable(false);
                    wsdlNamespaceField.setEnabled(false);
                }
            }
        });
        this.newNamespaceBaseSelectButton  = new JRadioButton("set new tns: <base_path><service>");
        this.newNamespaceBaseSelectButton. addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e )
            {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("newNamespaceBaseSelectButton selected");
                    wsdlNamespaceField.setEditable(true);
                    wsdlNamespaceField.setEnabled(true);
                }
            }            
        });
        nsBaseSelectionGroup = new ButtonGroup();
        nsBaseSelectionGroup.add(autoNamespaceBaseSelectButton);
        nsBaseSelectionGroup.add(newNamespaceBaseSelectButton);
        // ---
        //this.axisServicePathField = new JTextField(fieldLen);
        // ---
        this.owlsBaseField = new JTextField(fieldLen);        
        this.autoOWLSBaseSelectButton = new JRadioButton("auto (using parsed service data)");
        this.autoOWLSBaseSelectButton.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e )
            {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    owlsBaseField.setEditable(false);
                    owlsBaseField.setEnabled(false);
                }             
            }
        });
        this.newOWLSBaseSelectButton = new JRadioButton("set new owl-s base: <base_path><OWLS_DOC>");                       
        this.newOWLSBaseSelectButton.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e )
            {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    owlsBaseField.setEditable(true);
                    owlsBaseField.setEnabled(true);
                }
            }
        });
        owlsBaseSelectionGroup = new ButtonGroup();
        owlsBaseSelectionGroup.add(autoOWLSBaseSelectButton);
        owlsBaseSelectionGroup.add(newOWLSBaseSelectButton);
        // ---
        this.wsdldocPathField = new JTextField(fieldLen);
        this.autoWsdldocPathSelectButton = new JRadioButton("auto (use WSDL Builder setting above)");
        this.autoWsdldocPathSelectButton.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e )
            {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    wsdldocPathField.setEditable(false);
                    wsdldocPathField.setEnabled(false);
                }             
            }
        });
        this.setWsdlDocPathSelectButton = new JRadioButton("set new address path: <base_path><WSDL_DOC>");
        this.setWsdlDocPathSelectButton.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e )
            {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    wsdldocPathField.setEditable(true);
                    wsdldocPathField.setEnabled(true);
                }
            }
        });
        this.wsdldocPathSelectionGroup = new ButtonGroup();
        this.wsdldocPathSelectionGroup.add(autoWsdldocPathSelectButton);
        this.wsdldocPathSelectionGroup.add(setWsdlDocPathSelectButton);
        //======================================================================
        JComponent configPanel = buildConfigSubPanel(false);        
        JPanel commandPanel = buildCommandSubPanel();
        
        this.add(configPanel, BorderLayout.CENTER);
        this.add(commandPanel, BorderLayout.SOUTH);
        this.pack();
        this.locateOnScreen(this);
        this.initContentsFromConfigurationFile();
        
        // updateFromProjectData...
        this.projectElementDepthField.setEnabled(false);
        this.xsdTypeInheritanceBehaviour.setEnabled(false);                
    }
    
    private JComponent buildConfigSubPanel(boolean debugmode) {
        configTabbedPane = new JTabbedPane();
        configTabbedPane.add("XSD Generation", new JScrollPane(this.buildSubPanel4XsdGenerationConfigs(debugmode)));
        configTabbedPane.add("WSDL- / OWL-S Builder", new JScrollPane(this.buildExportSettingsTab(debugmode)));
        configTabbedPane.add("General Settings", new JScrollPane(this.buildSubPanel4ApplicationConfigs(debugmode)));
        return configTabbedPane;
    }
    
    private JPanel buildSubPanel4ApplicationConfigs(boolean debugmode) 
    {
        FormLayout layout = new FormLayout("10dlu, right:pref, 10px, fill:pref:grow, 5px, pref");
        DefaultFormBuilder builder = null;
        if(debugmode)
            builder = new DefaultFormBuilder(layout, new FormDebugPanel());
        else
            builder = new DefaultFormBuilder(layout);
        
        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);
        
        builder.appendSeparator("Application Properties");        
        builder.append("Application path", this.applPathField, this.applPathBrowseButton);
        builder.append("Export directory", this.exportDirectoryField , this.exportDirectoryBrowseButton);        
        builder.append("Log file", this.logPathField, 3);
        builder.append("Persistent datatype directory", this.persistentDatatypeDirectoryField, 3);
        builder.append("Project directory", this.persistentProjectDirectoryField, 3);        
        builder.append("Persistence", this.persistentDatatypeCheckBox, 3);
        builder.append("JConsole", this.jconsoleCheckBox, 3);
        
        //builder.append("", this.buildRelativePathCheckBox, 1);
        
        builder.appendSeparator("Language");
        JPanel langPanel = new JPanel();
        langPanel.add(this.deLangButton);
        langPanel.add(this.enLangButton);               
        builder.setHAlignment(CellConstraints.LEFT);
        builder.append("", langPanel, 3);
        
        builder.appendSeparator("Look and Feel");
        JPanel lfPanel = new JPanel();
        lfPanel.add(this.plasticLfButton);
        lfPanel.add(this.windowsLfButton);
        lfPanel.add(this.motifLfButton);
        builder.setHAlignment(CellConstraints.LEFT);
        builder.append("", lfPanel, 3);
        
        if(debugmode)
            FormDebugUtils.dumpAll(builder.getPanel());
        
        return builder.getPanel();
    }
    
    private JPanel buildSubPanel4XsdGenerationConfigs(boolean debugmode) {
        FormLayout layout = new FormLayout("10dlu, right:pref, 10px, fill:pref:grow");
        DefaultFormBuilder builder = null;
        if(debugmode)
            builder = new DefaultFormBuilder(layout, new FormDebugPanel());
        else
            builder = new DefaultFormBuilder(layout);
        
        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);
        
        builder.appendSeparator("General Properties");
        builder.append("Default inheritance depth", this.defaultElementDepthField, 1);        
        builder.append("Pattern Usage", this.shortButton, 1);
        builder.append("", this.hierarchyButton, 1);
        builder.append("Enhance WSDL Matchmaking", this.addMetaTypesCheckBox, 1);
        builder.append("Enhance WSDL Matchmaking", this.addAnnotationCheckBox, 1);
        
        builder.appendSeparator("Project Properties (saved in project)");       
        builder.append("Use this depth to add elements", this.projectElementDepthField, 1);
        builder.append("Inherit XSD Type from", this.xsdTypeInheritanceBehaviour, 1);
        builder.append("Default XSD Type", this.projectDefaultXsdTypeField, 1);
        
        if(debugmode)
            FormDebugUtils.dumpAll(builder.getPanel());
        
        return builder.getPanel();
    }
    
    private JPanel buildExportSettingsTab(boolean debugmode) {
        FormLayout layout = new FormLayout("10dlu, right:pref, 10px, fill:pref:grow");
        DefaultFormBuilder builder = null;
        if(debugmode)
            builder = new DefaultFormBuilder(layout, new FormDebugPanel());
        else
            builder = new DefaultFormBuilder(layout);
        
        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);        
        
        builder.appendSeparator("Namespace setting for WSDL Builder");
        builder.append("Change Namespace (tns)", this.autoNamespaceBaseSelectButton, 1);
        builder.append("", this.newNamespaceBaseSelectButton, 1);
        builder.append("New Namespace base_path", this.wsdlNamespaceField, 1);
        
        //builder.append("Axis service path", this.axisServicePathField, 1);
        
        builder.appendSeparator("XML Base setting for OWL-S Builder (Re-Engineering)");
        builder.append("Change XML Base", this.autoOWLSBaseSelectButton, 1);
        builder.append("", this.newOWLSBaseSelectButton, 1);
        builder.append("New XML base_path", this.owlsBaseField, 1);
        
        builder.appendSeparator("Grounding setting for OWL-S Builder (Re-Engineering)");
        builder.append("Set grounding path", this.autoWsdldocPathSelectButton, 1);
        builder.append("", this.setWsdlDocPathSelectButton, 1);
        builder.append("New grounding path", this.wsdldocPathField, 1);
        
        if(debugmode)
            FormDebugUtils.dumpAll(builder.getPanel());
        
        return builder.getPanel();
    }
    
    private JPanel buildCommandSubPanel() {
        JPanel p = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //setVisible(false);
                dispose();
            }
        });
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveContentsInConfigurationFile();
                updateProjectData();
                
                if(RuntimeModel.getInstance().getProject()!=null) {               
                    RuntimeModel.getInstance().setStatusAndNotify("RUNTIME", RuntimeModel.PROPERTIES_CHANGED, "saved properties");
                }
                //setVisible(false);
                dispose();
            }
        });
        
        p.add(cancelButton);
        p.add(saveButton);
        return p;
    }
    
    public void initContentsFromConfigurationFile() {
        this.applPathField.setText(OWLS2WSDLSettings.getInstance().getProperty("APPL_PATH"));
        this.logPathField.setText(OWLS2WSDLSettings.getInstance().getProperty("logfile"));
        
        this.persistentProjectDirectoryField.setText(OWLS2WSDLSettings.getInstance().getProperty("PROJECT_DIR"));
        this.persistentDatatypeDirectoryField.setText(OWLS2WSDLSettings.getInstance().getProperty("PERSISTENT_DATATYPE_DIR"));
        
        if(OWLS2WSDLSettings.getInstance().getProperty("JCONSOLE").equals("yes")) {
            this.jconsoleCheckBox.setSelected(true);
        }
        else {
            this.jconsoleCheckBox.setSelected(false);
        }
        
        if(OWLS2WSDLSettings.getInstance().getProperty("RELEVANT_DATATYPES_ONLY").equals("yes")) {
            this.persistentDatatypeCheckBox.setSelected(true);
        }
        else {
            this.persistentDatatypeCheckBox.setSelected(false);
        }
        
        this.defaultElementDepthField.setText(OWLS2WSDLSettings.getInstance().getProperty("depth"));
        
        if(OWLS2WSDLSettings.getInstance().getProperty("xsdgen").equals("short")) {
            this.shortButton.setSelected(true);            
        }
        else if(OWLS2WSDLSettings.getInstance().getProperty("xsdgen").equals("hierarchy")) {
            this.hierarchyButton.setSelected(true);
        }
        
        if(OWLS2WSDLSettings.getInstance().getProperty("owlinfo").equals("yes")) {
            this.addMetaTypesCheckBox.setSelected(true);
        }
        else {
            this.addMetaTypesCheckBox.setSelected(false);
        }
        
        if(OWLS2WSDLSettings.getInstance().getProperty("annotations").equals("yes")) {
            this.addAnnotationCheckBox.setSelected(true);
        }
        else {
            this.addAnnotationCheckBox.setSelected(false);
        }
        
        this.exportDirectoryField.setText(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH"));                
        
        if(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_MODE").equals("relative")) {
            this.buildRelativePathCheckBox.setSelected(true);
        }
        else {
            this.buildRelativePathCheckBox.setSelected(false);
        }
        
        if(OWLS2WSDLSettings.getInstance().getProperty("lang").equals("de")) {
            this.deLangButton.setSelected(true);
        }
        else if(OWLS2WSDLSettings.getInstance().getProperty("lang").equals("en")) {
            this.enLangButton.setSelected(true);
        }
        
        //======================================================================
        
        if(OWLS2WSDLSettings.getInstance().getProperty("laf").equals("windows")) {
            this.windowsLfButton.setSelected(true);
        }
        else if(OWLS2WSDLSettings.getInstance().getProperty("laf").equals("motif")) {
            this.motifLfButton.setSelected(true);
        }
        else {
            this.plasticLfButton.setSelected(true);
        }
        
        //======================================================================
        
        this.projectElementDepthField.setSelectedItem(OWLS2WSDLSettings.getInstance().getProperty("default_element_depth"));
        
        this.wsdlNamespaceField.setText(OWLS2WSDLSettings.getInstance().getProperty("TNS_BASEPATH"));
        if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_TNS").equals("yes")) {
            this.newNamespaceBaseSelectButton.setSelected(true);
            this.wsdlNamespaceField.setEditable(true);
            this.wsdlNamespaceField.setEnabled(true);
        }
        else {
            this.autoNamespaceBaseSelectButton.setSelected(true);
            this.wsdlNamespaceField.setEditable(false);
            this.wsdlNamespaceField.setEnabled(false);
        }        
        
        this.owlsBaseField.setText(OWLS2WSDLSettings.getInstance().getProperty("BASE"));        
        if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_BASE").equals("yes")) {
            this.newOWLSBaseSelectButton.setSelected(true);
            this.owlsBaseField.setEditable(true);
            this.owlsBaseField.setEnabled(true);
        }
        else {
            this.autoOWLSBaseSelectButton.setSelected(true);
            this.owlsBaseField.setEditable(false);
            this.owlsBaseField.setEnabled(false);
        }
        
        this.wsdldocPathField.setText(OWLS2WSDLSettings.getInstance().getProperty("WSDLPATH"));
        if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_WSDLPATH").equals("yes")) {
            this.setWsdlDocPathSelectButton.setSelected(true);
            this.wsdldocPathField.setEditable(true);
            this.wsdldocPathField.setEnabled(true);
        }
        else {
            this.autoWsdldocPathSelectButton.setSelected(true);
            this.wsdldocPathField.setEditable(false);
            this.wsdldocPathField.setEnabled(false);            
        }
    }
    
    public void updateContentsFromProjectData() {
        // Project specific (Castor mapping)
        if(RuntimeModel.getInstance().getProject() == null) {
            this.xsdTypeInheritanceBehaviour.setEnabled(false);
            this.projectElementDepthField.setEnabled(false);            
            this.projectElementDepthField.setSelectedIndex(-1);            
            this.projectDefaultXsdTypeField.setEnabled(false);
            this.projectDefaultXsdTypeField.setSelectedIndex(-1);
        }
        else {
            this.xsdTypeInheritanceBehaviour.setSelectedItem(RuntimeModel.getInstance().getProject().getTypeInheritanceBehaviour());            
            this.xsdTypeInheritanceBehaviour.setEnabled(true);
            this.projectElementDepthField.setSelectedItem(Integer.toString(RuntimeModel.getInstance().getProject().getElementDepth()));
            this.projectElementDepthField.setEnabled(true);
            this.projectDefaultXsdTypeField.setSelectedItem(RuntimeModel.getInstance().getProject().getDefaultXsdType());            
            this.projectDefaultXsdTypeField.setEnabled(true);
        }
    }
    
    public void saveContentsInConfigurationFile() {
        OWLS2WSDLSettings.getInstance().setProperty("APPL_PATH", this.applPathField.getText());
        OWLS2WSDLSettings.getInstance().setProperty("logfile", this.logPathField.getText());
        OWLS2WSDLSettings.getInstance().setProperty("PROJECT_DIR", this.persistentProjectDirectoryField.getText());
        OWLS2WSDLSettings.getInstance().setProperty("PERSISTENT_DATATYPE_DIR", this.persistentDatatypeDirectoryField.getText());
        
        if(this.jconsoleCheckBox.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("JCONSOLE", "yes");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("JCONSOLE", "no");
        }
        
        if(this.persistentDatatypeCheckBox.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("RELEVANT_DATATYPES_ONLY", "yes");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("RELEVANT_DATATYPES_ONLY", "no");
        }
        
        OWLS2WSDLSettings.getInstance().setProperty("depth", this.defaultElementDepthField.getText());
        if(this.shortButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("xsdgen", "short");
        }
        else if(this.hierarchyButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("xsdgen", "hierarchy");
        }
        
        if(this.addMetaTypesCheckBox.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("owlinfo", "yes");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("owlinfo", "no");
        }
        
        if(this.addAnnotationCheckBox.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("annotations", "yes");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("annotations", "no");
        }
        
        //======================================================================
        
        OWLS2WSDLSettings.getInstance().setProperty("EXPORT_PATH", this.exportDirectoryField.getText());                
        
        if(this.buildRelativePathCheckBox.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("EXPORT_MODE", "relative");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("EXPORT_MODE", "absolute");
        }
        
        OWLS2WSDLSettings.getInstance().setProperty("TNS_BASEPATH", this.wsdlNamespaceField.getText());
        if(this.autoNamespaceBaseSelectButton.isSelected()) {            
            OWLS2WSDLSettings.getInstance().setProperty("CHANGE_TNS", "no");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("CHANGE_TNS", "yes");
        }
        
        OWLS2WSDLSettings.getInstance().setProperty("BASE", this.owlsBaseField.getText());
        if(this.autoOWLSBaseSelectButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("CHANGE_BASE", "no");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("CHANGE_BASE", "yes");
        }
        
        OWLS2WSDLSettings.getInstance().setProperty("WSDLPATH", this.wsdldocPathField.getText());
        if(this.autoWsdldocPathSelectButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("CHANGE_WSDLPATH", "no");
        }
        else {
            OWLS2WSDLSettings.getInstance().setProperty("CHANGE_WSDLPATH", "yes");
        }
        
        //======================================================================
        
        if(this.deLangButton.isSelected()) {
            if(!OWLS2WSDLSettings.getInstance().getProperty("lang").equals("de")) {
                JOptionPane.showMessageDialog(
                    null,
                    ResourceManager.getString("frame.config.lang.changemsg"),
                    "Localization Info",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            OWLS2WSDLSettings.getInstance().setProperty("lang", "de");
        }
        else if(this.enLangButton.isSelected()) {
            if(!OWLS2WSDLSettings.getInstance().getProperty("lang").equals("en")) {
                JOptionPane.showMessageDialog(
                    null,
                    ResourceManager.getString("frame.config.lang.changemsg"),
                    "Localization Info",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            OWLS2WSDLSettings.getInstance().setProperty("lang", "en");
        }
        
        //======================================================================
        
        if(this.windowsLfButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("laf", "windows");            
        }
        else if(this.plasticLfButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("laf", "plastic");
        }
        else if(this.motifLfButton.isSelected()) {
            OWLS2WSDLSettings.getInstance().setProperty("laf", "motif");
        }
        
        //OWLS2WSDLSettings.getInstance().printProperties(System.out);
        
        OWLS2WSDLSettings.getInstance().savePropertyFile();
    }
    
    public void updateProjectData() {
        if(RuntimeModel.getInstance().getProject()!=null) {
            RuntimeModel.getInstance().getProject().setElementDepth(Integer.parseInt(this.projectElementDepthField.getSelectedItem().toString()));
            RuntimeModel.getInstance().getProject().setTypeInheritanceBehaviour(this.xsdTypeInheritanceBehaviour.getSelectedItem().toString());
            RuntimeModel.getInstance().getProject().setDefaultXsdType(this.projectDefaultXsdTypeField.getSelectedItem().toString());
        }
    }
    
    public void deactivateProjectProperties() {
        this.configTabbedPane.setEnabledAt(0, false);
        this.configTabbedPane.setSelectedIndex(1);        
    }

    public void activateProjectProperties() {
        this.configTabbedPane.setEnabledAt(0, true);
        this.configTabbedPane.setSelectedIndex(0);        
    }
    
    private String doBrowse(String curpath) throws java.io.IOException {
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
        fc.setCurrentDirectory(new File(curpath));
        
        //In response to a button click:
        int returnVal = fc.showOpenDialog(null);

        if(returnVal != JFileChooser.APPROVE_OPTION) {
            System.err.println("returnVal != JFileChooser.APPROVE_OPTION");
            return curpath;
        }
        
        File selectedDirectory = fc.getSelectedFile();
        
        if(!selectedDirectory.exists()) {
            JOptionPane.showMessageDialog(
                null,
                selectedDirectory.getAbsolutePath() + "does not exist!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return curpath;
        }
        
        System.out.println("getCanonicalPath: "+selectedDirectory.getCanonicalPath());
        System.out.println("getAbsolutePath: "+selectedDirectory.getAbsolutePath());                
        return selectedDirectory.getCanonicalPath();
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getActionCommand().equals(this.SET_APPL_PATH)) {
                String applPath = System.getProperty("user.home")+File.separator+"owls2wsdl";
                if(OWLS2WSDLSettings.getInstance().containsKey("APPL_PATH")) {
                    applPath = OWLS2WSDLSettings.getInstance().getProperty("APPL_PATH");
                }
                applPath = this.doBrowse(applPath);
                this.applPathField.setText(applPath);
                this.logPathField.setText(applPath+File.separator+"owls2wsdl.log");
            }
            else if(e.getActionCommand().equals(this.SET_EXPORT_DIRECTORY)) {
                String exportPath = System.getProperty("user.home")+File.separator+"owls2wsdl";
                if(OWLS2WSDLSettings.getInstance().containsKey("EXPORT_PATH")) {
                    exportPath = OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH");
                }
                this.exportDirectoryField.setText(this.doBrowse(exportPath));
            }
            else if(e.getActionCommand().equals("CHANGE_LF_TO_WINDOWS")) {
                this.switchLAF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
            else if(e.getActionCommand().equals("CHANGE_LF_TO_PLASTIC")) {
                this.switchLAF("com.jgoodies.looks.plastic.PlasticLookAndFeel");
            }
            else if(e.getActionCommand().equals("CHANGE_LF_TO_MOTIF")) {
                this.switchLAF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            }
        }
        catch(java.io.IOException ioExc) {
            System.err.println("IOException: "+ioExc.getMessage());
        }
        catch(java.lang.Exception exc) {
            System.err.println("Exception: "+exc.getMessage());
            exc.printStackTrace();
        }
    }
    
    private void switchLAF(String lafName) {
        try {
            UIManager.setLookAndFeel(lafName);
            SwingUtilities.updateComponentTreeUI(this);
            this.pack();
            if(this.mainWindowRef != null) {
                SwingUtilities.updateComponentTreeUI(mainWindowRef);
                mainWindowRef.pack();
            }
        }catch(IllegalAccessException iae) {
            System.out.println("[e] InstantiationException: "+iae.getMessage());
        }catch(InstantiationException ie) {
            System.out.println("[e] InstantiationException: "+ie.getMessage());
        }catch(ClassNotFoundException cnfe) {
            System.out.println("[e] ClassNotFoundException: "+cnfe.getMessage());
        }catch(UnsupportedLookAndFeelException uslaf) {
            System.out.println("[e] UnsupportedLookAndFeelException: "+uslaf.getMessage());
        }
    }
    
    public static void main(String[] args) {
        ConfigurationFrame cf = new ConfigurationFrame(null);
        cf.setVisible(true);
        //cf.show();
        cf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
}
