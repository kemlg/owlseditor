/*
 * ProjectDetailPanel.java
 *
 * Created on 19. März 2007, 20:18
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

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Observable;

import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;

import de.dfki.dmas.owls2wsdl.core.Project;
import de.dfki.dmas.owls2wsdl.core.ProjectLoadingThread;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeKB;

import de.dfki.dmas.owls2wsdl.gui.models.*;

import java.io.File;
import java.util.Vector;
import java.util.Observer;

/**
 *
 * @author Oliver Fourman
 */
public class ProjectDetailPanel extends JPanel implements Observer {
    
    private ProjectLoadedOntListModel ontlistModel;
    private JList parsedOntologiesList;
        
    private ProjectServiceDependencyTypesTableModel serviceDependencyTypesTableModel;    
    private JTable serviceDependencyTypesTable;
    
    private JTextField datatypesTotalCountField;
    private JTextField serviceDependencyTypesCountField;
    private JTextField serviceMissingTypesCountField;
    
    private JList changesList;
    
    private JTabbedPane projectTab;
    
    private JButton removeSelectedTypeButton;
    
    /** Creates a new instance of ProjectDetailPanel */
    public ProjectDetailPanel() {
        RuntimeModel.getInstance().addObserver(this);
        
        this.setLayout(new BorderLayout());
        initComponents();
        buildPanel(false);        
    }
    
    /**
     * Creates, intializes and configures the UI components. 
     */
    private void initComponents() {
        ontlistModel = new ProjectLoadedOntListModel();        
        parsedOntologiesList = new JList(ontlistModel);        
        parsedOntologiesList.setVisibleRowCount(7);
        
        changesList = new JList();
        changesList.setVisibleRowCount(9);
                
        serviceDependencyTypesTableModel = new ProjectServiceDependencyTypesTableModel();
        ProjectServiceDependencyTypesTableColumnModel cm = new ProjectServiceDependencyTypesTableColumnModel();        
        serviceDependencyTypesTable = new JTable(serviceDependencyTypesTableModel, cm);
        serviceDependencyTypesTable.setPreferredScrollableViewportSize(new Dimension(500,200));
        
        datatypesTotalCountField = new JTextField(5);
        serviceDependencyTypesCountField = new JTextField(5);
        serviceMissingTypesCountField = new JTextField(5);
        
        projectTab = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        
        removeSelectedTypeButton = new JButton("DEL SEL");
        removeSelectedTypeButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                int idx = serviceDependencyTypesTable.getSelectedRow();
                String key = serviceDependencyTypesTable.getModel().getValueAt(idx, 1).toString();
                
                System.out.println("SEL: "+key);
                AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().removeRegisteredDatatype(key);
                
                RuntimeModel.getInstance().getProject().determineAllDependecyTypes();                
                RuntimeModel.getInstance().setRuntimeAndNotify(RuntimeModel.DATATYPE_MODEL_CHANGED);
            }
        });
    }
    
    /**
     * Build UI.
     */
    private void buildPanel(boolean debugmode) 
    {           
        this.setBorder(Borders.DLU4_BORDER);        
        projectTab.add("Service Dependencies", buildServiceDependenciesInfoTab(debugmode));
        projectTab.add("Ont-KB Information", buildOntInfoTab(debugmode));
        this.add(projectTab, BorderLayout.CENTER);
//        this.add(removeSelectedTypeButton, BorderLayout.SOUTH);
    }
    
    private JScrollPane buildOntInfoTab(boolean debugmode)
    {   
        JPanel ontInfoPanel = new JPanel();
        JPanel debugPanel   = new FormDebugPanel();
        
        FormLayout layout = new FormLayout("fill:pref:grow",
                "pref, 3dlu, pref, 5dlu, " +
                "pref, 3dlu, pref");
        
        PanelBuilder builder = null;
                
        if(debugmode)
            builder = new PanelBuilder(layout, debugPanel);
        else
            builder = new PanelBuilder(layout, ontInfoPanel);
                
        builder.setDefaultDialogBorder();
        
        CellConstraints cc = new CellConstraints();
        builder.addSeparator("Parsed ontologies",                  cc.xy(1,1));
        builder.add(new JScrollPane(parsedOntologiesList),         cc.xy(1,3));
        
        builder.addSeparator("Knowledgebase changes (LOG)",        cc.xy(1,5));
        builder.add(new JScrollPane(changesList),                  cc.xy(1,7));
        
        if(debugmode) {
            FormDebugUtils.dumpAll(this);
            return new JScrollPane(debugPanel);
        }
        else {
            return new JScrollPane(ontInfoPanel);
        }
    }
    
    private JScrollPane buildServiceDependenciesInfoTab(boolean debugmode)
    {   
        JPanel ontInfoPanel = new JPanel();
        JPanel debugPanel   = new FormDebugPanel();
        
        FormLayout layout = new FormLayout("fill:40dlu, right:pref, 4dlu, pref, 10dlu:grow, right:pref, 4dlu, pref, 10dlu",
                "pref, 3dlu, pref, 5dlu," +
                "pref, 3dlu, pref, 3dlu,  pref, 3dlu,  pref");
        
        PanelBuilder builder = null;
                
        if(debugmode)
            builder = new PanelBuilder(layout, debugPanel);
        else
            builder = new PanelBuilder(layout, ontInfoPanel);
                
        builder.setDefaultDialogBorder(); 
        
        CellConstraints cc = new CellConstraints();
        builder.addSeparator("Service dependencies and missing types", cc.xyw(1,1,9));       
        builder.add(new JScrollPane(serviceDependencyTypesTable), cc.xyw(1,3,9));
        
        builder.addSeparator("Project, Datatype summary", cc.xyw(1,5,9));
        builder.add(new JLabel("Total count"),         cc.xy(2,7));
        builder.add(datatypesTotalCountField,          cc.xy(4,7));
        builder.add(new JLabel("Service dependencies count"),  cc.xy(2,9));
        builder.add(serviceDependencyTypesCountField,  cc.xy(4,9));
        builder.add(new JLabel("Missing types count"), cc.xy(6,9));
        builder.add(serviceMissingTypesCountField,     cc.xy(8,9));
        
        if(debugmode) {
            FormDebugUtils.dumpAll(this);
            return new JScrollPane(debugPanel);
        }
        else {
            return new JScrollPane(ontInfoPanel);
        }
    }
    
    public void updatePanel() 
    {
        serviceDependencyTypesTable.revalidate();
        serviceDependencyTypesTable.updateUI();
        
        parsedOntologiesList.revalidate();
        parsedOntologiesList.updateUI();        

        datatypesTotalCountField.setText(String.valueOf(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getRegisteredDatatypes().size()));
        serviceDependencyTypesCountField.setText(String.valueOf(RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size()));
        serviceMissingTypesCountField.setText(String.valueOf(RuntimeModel.getInstance().getProject().getServiceMissingTypes().size()));
        
        changesList.revalidate();
        changesList.updateUI();
    }
    
    public void update(Observable o, Object arg) {
        System.out.println("[u] ProjectDetailPanel");
        if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.DATATYPE_MODEL_CHANGED) ||
           RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SERVICE_MODEL_CHANGED)) {
            System.out.println("[i] Ont Count: "+AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getOntologyURIs().size());
            updatePanel();
            this.updateUI();
        }
    }
    
   /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (Exception e) {
            // Likely PlasticXP is not in the class path; ignore.
        }
        
        ProjectDetailPanel projDetailPanel = new ProjectDetailPanel();
        
        JFrame frame = new JFrame("Example - Project Detail Panel");
        frame.setVisible(true); 
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600,600));
        frame.getContentPane().add(projDetailPanel, BorderLayout.CENTER);        
        frame.pack();
        projDetailPanel.updatePanel();
    }
    
    public static void main (String[] args) 
    {       
        File f = new File("D:\\tmp\\OWLS2WSDL-project-car_price_service\\car_price_service.xml");
        ProjectLoadingThread t = new ProjectLoadingThread(f);
        t.setFile(f);
        t.run();
        
        //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
        
//        AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().removeAllDatatypes();
//        p.determineAllDependecyTypes();
//        RuntimeModel.getInstance().setRuntimeAndNotify(RuntimeModel.DATATYPE_MODEL_CHANGED);
//        
//        System.out.println("  Ont   Count: "+AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getOntologyURIs().size());
//        System.out.println("  Types Count: "+AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().getRegisteredDatatypes().size());
//        System.out.println("D.Types Count: "+RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size());
    }
}
