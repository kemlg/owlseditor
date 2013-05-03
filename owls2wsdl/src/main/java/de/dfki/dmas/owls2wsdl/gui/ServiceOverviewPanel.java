/*
 * ServiceOverviewPanel.java
 * TEST class only
 *
 * Created on 21. September 2006, 16:32
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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.Vector;

/**
 *
 * @author Oliver Fourman
 */
public class ServiceOverviewPanel extends JPanel  {
    
    private ServiceListModel serviceListModel;
    private JList            serviceList;
    private JScrollPane leftScrollPanel;    
    private JButton     loadButton;
    private JPanel      navControlPanel;
    private JPanel      navigationPanel;
    
    private ServiceDetailPanel detailViewPanel;
    private JScrollPane rightScrollPanel;
    private JButton     createWSDLButton;
    private JPanel      serviceCommandPanel;
    private JPanel      servicePanel;    
    
    private JSplitPane  jSplitPane1Main;    
    private JLabel      headLabel;
    
    /** Creates a new instance of ServiceOverviewPanel */
    public ServiceOverviewPanel() {
        super(new BorderLayout(), true);        
        initComponents();
        buildPanel();
    }
    
    /**
     * Creates, intializes and configures the UI components. 
     * Real applications may further bind the components to underlying models. 
     */
    private void initComponents() {
        headLabel = new JLabel("OWLS Services", JLabel.CENTER);
        
        serviceListModel    = new ServiceListModel();        
        serviceList         = new JList(serviceListModel);
        serviceList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        leftScrollPanel  = new JScrollPane();
        loadButton       = new JButton("LOAD");
        
        loadButton.addActionListener( new ActionListener() { 
           public void actionPerformed(ActionEvent e)  {
               //serviceListModel.addToModel("file:///D:/htw_kim/thesis/OWLS-MX/owls-tc2/domains/1.1/travel/"); //_warmfront_Italyservice.owls");
               serviceList.updateUI();
               //serviceList.revalidate();
               System.out.println("SIZE OF MODEL: "+serviceListModel.getSize());
           }
        });
        
        navControlPanel  = new JPanel();
        navigationPanel  = new JPanel(new BorderLayout());        
    }   
    
    public void buildPanel()
    {               
        //
        // Navigation Panel
        //

        
        serviceList.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if(!e.getValueIsAdjusting()) {
                            if(serviceList.getSelectedValues().length>1) {
                                System.out.println("MANY SERVICES SELECTED");
                            }
                            else {
                                System.out.println(">>>> "+ ((ServiceListModel)serviceList.getModel()).getAbstractServiceAt(serviceList.getSelectedIndex()));
                                //detailViewPanel.updatePanel( ((ServiceListModel)serviceList.getModel()).getAbstractServiceAt(serviceList.getSelectedIndex()) );
                                //jSplitPane1Main.setRightComponent(detailViewPanel);
                            }
                        }
                    }
            });
        
        serviceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftScrollPanel.setViewportView(serviceList);
        navControlPanel.add(loadButton);
        
        this.add(leftScrollPanel, BorderLayout.CENTER);
        this.add(navControlPanel, BorderLayout.SOUTH);
        
        
//        //
//        // SERVICE Detail Panel
//        // 
//        rightScrollPanel.setViewportView(detailViewPanel);
//        serviceCommandPanel.add(createWSDLButton);
//        servicePanel.add(rightScrollPanel, BorderLayout.CENTER);
//        servicePanel.add(serviceCommandPanel, BorderLayout.SOUTH);
//        
//        // Main Panel
//        jSplitPane1Main.setLeftComponent(navigationPanel);
//        jSplitPane1Main.setRightComponent(servicePanel);
//        jSplitPane1Main.updateUI();
//
//        this.add(jSplitPane1Main, BorderLayout.CENTER);        
//        this.add(headLabel, BorderLayout.NORTH);
    }        
    
//    public void populateList(Vector services) {
//        DefaultListModel model = new DefaultListModel();
//        for(int i=0; i<services.size();i++){
//            model.addElement(services.get(i));
//        }
//        serviceList.setModel(model);
//        serviceList.updateUI();
//    }
    
    public void updateServiceList() {
        
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
        JFrame frame = new JFrame("Example - Services Overview Panel");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        frame.setSize(new Dimension(800,600));
        
        ServiceOverviewPanel p = new ServiceOverviewPanel();
              
          
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
