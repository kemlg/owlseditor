/*
 * RegisteredDatatypeOverviewPanel.java
 *
 * Created on 25. September 2006, 01:20
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

import de.dfki.dmas.owls2wsdl.gui.models.AbstractDatatypeListModel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.Vector;

/**
 *
 * @author Oliver Fourman
 */
public class RegisteredDatatypeOverviewPanel extends JPanel  {
    
    private AbstractDatatypeListModel model;
    
    private JList       typeList;
    
    private JScrollPane leftScrollPanel;    
    private JButton     loadButton;
    private JPanel      navControlPanel;
    private JPanel      navigationPanel;
    
    private RegisteredDatatypeDetailPanel detailViewPanel;
    private JScrollPane rightScrollPanel;
    private JButton     createWSDLButton;
    private JPanel      serviceCommandPanel;
    private JPanel      servicePanel;    
    
    private JSplitPane  jSplitPane1Main;    
    private JLabel      headLabel;
    
    private JFrame frameRef;
    
    /** Creates a new instance of RegisteredDatatypeOverviewPanel */
    public RegisteredDatatypeOverviewPanel() {
        super(new BorderLayout(), true);
    }
    
    /**
     * Creates, intializes and configures the UI components. 
     * Real applications may further bind the components to underlying models. 
     */
    private void initComponents() {
        headLabel = new JLabel("ABSTRACT (OWL2XSD) DATATYPES", JLabel.CENTER);
        
        model            = new AbstractDatatypeListModel();        
        typeList         = new JList(model);
        leftScrollPanel  = new JScrollPane();
        loadButton       = new JButton("LOAD");
        navControlPanel  = new JPanel();
        navigationPanel  = new JPanel(new BorderLayout());
        
        rightScrollPanel = new JScrollPane();
        detailViewPanel  = new RegisteredDatatypeDetailPanel();
        createWSDLButton = new JButton("Show XSD");
        serviceCommandPanel = new JPanel();
        servicePanel     = new JPanel(new BorderLayout());
        
        jSplitPane1Main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    }
    
    public void buildPanel(JFrame frame)     
    {        
        frameRef=frame;
        initComponents();
        
        //
        // Navigation Panel
        //
        loadButton.addActionListener( new ActionListener() { 
           public void actionPerformed(ActionEvent e)  {
               //model.updateModel("file:/D:/tmp/AbstractDatatypesKB2.xml");
               //model.updateModel("file:/D:/tmp/AbstractDatatypesKB_ZIPCODE.xml");
               //model.updateModel("file:/D:/tmp/AbstractDatatypeKB_Transportation.xml");
               //model.updateModel("file:/D:/tmp/KB/KB_Student.xml");
               //model.updateModel("file:/D:/tmp/KB/KB_Wine.xml");
               typeList.setModel(model);
               typeList.updateUI();
               typeList.revalidate();
           }
        });
        
        typeList.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if(!e.getValueIsAdjusting()) {
                            detailViewPanel.updatePanel( ((AbstractDatatypeListModel)typeList.getModel()).getElementAt(typeList.getSelectedIndex()) );
                        }                        
                    }
            });
        
        typeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftScrollPanel.setViewportView(typeList);
        navControlPanel.add(loadButton);
        navigationPanel.add(leftScrollPanel, BorderLayout.CENTER);
        navigationPanel.add(navControlPanel, BorderLayout.SOUTH);
        
        //
        // DATATYPE Detail Panel
        // 
        rightScrollPanel.setViewportView(detailViewPanel);
        serviceCommandPanel.add(createWSDLButton);
        servicePanel.add(rightScrollPanel, BorderLayout.CENTER);
        servicePanel.add(serviceCommandPanel, BorderLayout.SOUTH);
        
        // Main Panel
        jSplitPane1Main.setLeftComponent(navigationPanel);
        jSplitPane1Main.setRightComponent(servicePanel);
        jSplitPane1Main.updateUI();
        
        this.add(jSplitPane1Main, BorderLayout.CENTER);        
        this.add(headLabel, BorderLayout.NORTH);
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
        JFrame frame = new JFrame("Example - Abstract Datatype Overview Panel");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        frame.setSize(new Dimension(800,600));
        
        RegisteredDatatypeOverviewPanel p = new RegisteredDatatypeOverviewPanel();
        p.buildPanel(frame);
        
                
        frame.getContentPane().add(p, BorderLayout.CENTER);        
        frame.pack();
        frame.setVisible(true); 
    }
    
    /** Main-Methode zu Testzwecken */
    public static void main (String[] args) { 
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
}
