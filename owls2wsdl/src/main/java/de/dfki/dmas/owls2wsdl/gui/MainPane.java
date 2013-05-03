/*
 * ContentPane.java
 *
 * Created on 27. November 2006, 14:18
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

import de.dfki.dmas.owls2wsdl.core.AbstractDatatype;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeKB; // import static ab 1.5
import de.dfki.dmas.owls2wsdl.gui.models.AbstractDatatypeListModel;
import de.dfki.dmas.owls2wsdl.gui.models.ServiceListModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Color;
//import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.looks.Options;
import com.jgoodies.uif_lite.component.Factory;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

import java.util.Observer;
import java.util.Vector;

import java.net.URL;

/**
 *
 * @author Oliver Fourman
 */
final class MainPane implements ChangeListener, Observer
{
    private OWLS2WSDLGui        appl;
    
    protected JSplitPane        leftSplitPane; // to change DividerLocation
    protected JTabbedPane       tabbedPaneLeft;
    private SimpleInternalFrame sif_overview;
    private SimpleInternalFrame sif_status;    
    
    private JTabbedPane         tabbedPaneRight;
    private SimpleInternalFrame sif_details;
    private SimpleInternalFrame sif_output;
    
    protected ServiceListModel              serviceListModel;
    protected JList                         serviceList;
    
    protected AbstractDatatypeListModel     datatypeListModel;    
    protected JList                         datatypeList;    
    
    private ServiceDetailPanel              sdp;    
    private RegisteredDatatypeDetailPanel   rdtdp;
    private ProjectDetailPanel              pdp;
    
//    private JInternalFrame serviceFrame;
//    private JInternalFrame datatypeFrame;
    
    private ServiceOverviewPanel            servicePanel;
    private RegisteredDatatypeOverviewPanel datatypePanel;    
    protected OutputPanel                   outputPanel;
    
    protected StatusTable                   statusTable;
    
    private JComponent                      horizontalSplitComponent;
    private JComponent                      mainRightComponentPanel;
    
    private SimpleInternalFrame             converterFrame;
    private WSDL2OWLSPanel                  wsdl2owlsPanel;
    
    final URL owls2wsdlLogoURL = MainPane.class.getResource( "/images/owls2wsdl-logo.jpg" );   
    private ViewComponent logoComponent;
    public boolean       panelActivationFlag = false;
    
    /*
     * Main GUI class
     */
    public MainPane() 
    {
        RuntimeModel.getInstance().addObserver(this);
        
        //
        // init components
        //
        
        logoComponent = new ViewComponent();
        logoComponent.setImage(owls2wsdlLogoURL);
        logoComponent.scale(184);
        
        serviceListModel = new ServiceListModel();   
//        serviceListModel.addListDataListener( new ListDataListener() {
//            public void contentsChanged(ListDataEvent e) {
//                System.out.println("[i] servicelist, contents changed.");
//            }
//            public void intervalRemoved(ListDataEvent e) {
//                JOptionPane.showMessageDialog(
//                        appl.getContentPane(),
//                        "Selected service interval removed.",
//                        "Service Managment",
//                        JOptionPane.INFORMATION_MESSAGE);
//            }
//            public void intervalAdded(ListDataEvent e) {
//                JOptionPane.showMessageDialog(
//                        appl.getContentPane(),
//                        "Service(s) added.",
//                        "Service Managment",
//                        JOptionPane.INFORMATION_MESSAGE);
//            }
//        }); 
        
        serviceList = new JList(serviceListModel);        
        serviceList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        serviceList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    if(serviceList.getSelectedValues().length>1) {
                        System.out.println("[i] MULTIPLE SERVICES SELECTED");
                        RuntimeModel.getInstance().unsetSelectedService();
                        RuntimeModel.getInstance().setStatusAndNotify("RUNTIME", RuntimeModel.MULTIPLE_SERVICES_SELECTED, "mult. services");
//                            for(int i=0; i<serviceList.getSelectedValues().length; i++) {
//                                System.out.println("SELECTED: "+serviceList.getSelectedValues()[i].toString());
//                            }
                    }
                    else if(serviceList.getSelectedValues().length == 1) {
                        RuntimeModel.getInstance().setSelectedService(((ServiceListModel)serviceList.getModel()).getAbstractServiceAt(serviceList.getSelectedIndex()));
                        RuntimeModel.getInstance().setStatusAndNotify("RUNTIME", RuntimeModel.SINGLE_SERVICE_SELECTED, "single service");
                        // sdp.updateServiceDetailPanel();  // obsolete due to the usage of the Observable pattern.
                    }
                    else {
                        System.out.println("[i] No service selected");
                        sdp.clearPanel();
                    }
                }
            }
        });
        
        datatypeListModel = new AbstractDatatypeListModel();        
        datatypeList = new JList(datatypeListModel);
        datatypeList.revalidate();
        datatypeList.updateUI();
        datatypeList.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if(!e.getValueIsAdjusting()) {
                            if(datatypeList.isSelectionEmpty()) {
                                System.out.println("[i] MainPane.datatypeList: no selection, nothing triggerd.");
                            }
                            else {
                                RuntimeModel.getInstance().setSelectedDatatype(((AbstractDatatypeListModel)datatypeList.getModel()).getAbstractDatatypeAt(datatypeList.getSelectedIndex()));
                                RuntimeModel.getInstance().setStatusAndNotify("RUNTIME", RuntimeModel.SINGLE_DATATYPE_SELECTED, "single type sel.");
                                rdtdp.updatePanel( ((AbstractDatatypeListModel)datatypeList.getModel()).getAbstractDatatypeAt(datatypeList.getSelectedIndex()));
                            }
                        }                        
                    }
            });        
        datatypeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        statusTable = new StatusTable();
                
        converterFrame = new SimpleInternalFrame("WSDL2OWL-S Converter");
        wsdl2owlsPanel = new WSDL2OWLSPanel();
        converterFrame.add(Factory.createStrippedScrollPane(wsdl2owlsPanel));
        
        // SERVICES
        sdp = new ServiceDetailPanel();
        
        // DATATYPES
        rdtdp = new RegisteredDatatypeDetailPanel();
        
        // PROJECT
        pdp = new ProjectDetailPanel();
                        
        mainRightComponentPanel  = buildMainRightPanel();
        horizontalSplitComponent = buildHorizontalSplit();
    }
    
    
    public void stateChanged(ChangeEvent event)
    {
        this.sif_overview.setTitle(tabbedPaneLeft.getTitleAt(tabbedPaneLeft.getSelectedIndex()));
        this.setServiceInformationFrame(tabbedPaneLeft.getSelectedIndex());
    }
    
    protected void setServiceInformationFrame(int i) 
    {
        if(i == 0) {
            sif_details.remove(1);
            sif_details.add(Factory.createStrippedScrollPane(sdp));
            sif_details.setTitle("Service Details");
            sif_details.revalidate();
            //swicth details frame to service view
            //RuntimeModel.getInstance().setStatusAndNotify("VIEW", RuntimeModel.SERVICE_VIEW);
        }
        else if(i == 1) {
            sif_details.remove(1);
            sif_details.add(Factory.createStrippedScrollPane(rdtdp));
            sif_details.setTitle("Datatype Details");
            sif_details.revalidate();
            //switch details frame to datatype view
            //RuntimeModel.getInstance().setStatusAndNotify("VIEW", RuntimeModel.DATATYPE_VIEW);
        }
        else if(i == 2) {
            pdp.updatePanel();
            sif_details.remove(1);
            sif_details.add(Factory.createStrippedScrollPane(pdp));
            sif_details.setTitle("Project Details");
            sif_details.revalidate();
        }
    }         
    
    public void showWSDL2OWLSConverter(boolean flag) 
    {        
        if(flag) {
            ((JSplitPane)horizontalSplitComponent).setRightComponent(converterFrame);
        }
        else {
            ((JSplitPane)horizontalSplitComponent).setRightComponent(mainRightComponentPanel);
        }
        horizontalSplitComponent.revalidate();
    }
    
    public Vector getSelectedServicesList() {
        Vector selectedAbstractServices = new Vector();
        int selectionIndex = -1;
        for(int i=0; i<this.serviceList.getSelectedIndices().length; i++) {
            selectionIndex = this.serviceList.getSelectedIndices()[i];
            selectedAbstractServices.add( ((ServiceListModel)serviceList.getModel()).getAbstractServiceAt(selectionIndex) );
        }
        return selectedAbstractServices;
    }
    
    public void update(java.util.Observable o, Object arg) 
    {   
        if(RuntimeModel.getInstance().getRuntime().equals(RuntimeModel.PARAMETER_SELECTED)) {
            AbstractDatatype atype = RuntimeModel.getInstance().getSelectedDatatype();
            System.out.println("[u] MainPane: new datatype (parameter) selected: "+atype.getUrl());
            int idx = datatypeListModel.getIndexOfAbstractDatatype(atype);
            datatypeList.setSelectedIndex(idx);
            datatypeList.ensureIndexIsVisible(idx);
            datatypeList.updateUI();
            this.tabbedPaneLeft.setSelectedIndex(1);
            this.setServiceInformationFrame(1);
            this.showWSDL2OWLSConverter(false);
        }
        
//        if(RuntimeModel.getInstance().getStatus("VIEW").equals(RuntimeModel.DATATYPE_VIEW)) {
//            datatypeList.updateUI();
//        }
//        else if(RuntimeModel.getInstance().getStatus("VIEW").equals(RuntimeModel.SERVICE_VIEW)) {
//            serviceList.updateUI();
//        }        
        
        if(RuntimeModel.getInstance().getRuntime().equals(RuntimeModel.SERVICE_MODEL_CHANGED)) {
            this.serviceListModel.updateModel();
            this.serviceList.revalidate();
            this.serviceList.updateUI();
        }
        else if(RuntimeModel.getInstance().getRuntime().equals(RuntimeModel.DATATYPE_MODEL_CHANGED)) {
            this.datatypeListModel.updateModel();
            this.datatypeList.revalidate();
            this.datatypeList.updateUI();
        }
        else if(RuntimeModel.getInstance().getRuntime().equals(RuntimeModel.PROJECT_LOADED)) {
            System.out.println("[MainPane] update service and datatype list!");
            this.serviceListModel.updateModel();
            this.serviceList.revalidate();
            this.serviceList.updateUI();
            this.datatypeListModel.updateModel();
            this.datatypeList.revalidate();
            this.datatypeList.updateUI();
        }
    }
    
    /**
     * Builds and returns the panel.
     */
    JComponent build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(Borders.DIALOG_BORDER);
        panel.add(horizontalSplitComponent);
        return panel;
    }
    
    /**
     * Builds and returns the horizontal split using stripped split panes.<p>
     * 
     * Nesting split panes often leads to duplicate borders. 
     * However, a look&feel should not remove borders completely
     * - unless he has good knowledge about the context: the surrounding 
     * components in the component tree and the border states.
     */
    private JComponent buildHorizontalSplit() {
        return Factory.createStrippedSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildMainLeftPanel(),
            mainRightComponentPanel,
            0.2f);
    }
    
    /**
     * Builds and returns a panel that uses a tabbed pane with embedded tabs
     * enabled.
     */
    private JComponent buildMainLeftPanel() {
        tabbedPaneLeft = new JTabbedPane(SwingConstants.BOTTOM);
        tabbedPaneLeft.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        tabbedPaneLeft.addTab("Services", Factory.createStrippedScrollPane(this.serviceList));
        tabbedPaneLeft.addTab("Datatypes", Factory.createStrippedScrollPane(this.datatypeList));
        tabbedPaneLeft.addChangeListener(this);        
        
        sif_overview = new SimpleInternalFrame("Services");        
        sif_overview.setPreferredSize(new Dimension(150, 400));
        sif_overview.add(tabbedPaneLeft);
        
        sif_status = new SimpleInternalFrame(
                ResourceManager.getString("iframe.projectstatus.inactive")
                );
        sif_status.setPreferredSize(new Dimension(150,100));                
        //sif_status.add( new JScrollPane(this.statusTable) );
        
        sif_status.setBackground(Color.WHITE);        
        sif_status.add(this.logoComponent);
        
        leftSplitPane = Factory.createStrippedSplitPane(JSplitPane.VERTICAL_SPLIT, sif_overview, sif_status, 0.9f);
        leftSplitPane.setDividerLocation(390);
        return leftSplitPane;
    }
    
    private JComponent buildHelp() {
        JTextArea area = new JTextArea("\n This tabbed pane uses\n embedded tabs.");
        return area;
    }
       
    /**
     * Builds and returns a tabbed pane with the no-content-border enabled.
     */
    private JComponent buildMainRightPanel() 
    {
        
//        tabbedPaneRight = new JTabbedPane(SwingConstants.BOTTOM);
//        tabbedPaneRight.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
//        tabbedPaneRight.addTab("Service", Factory.createStrippedScrollPane(sdp));
//        tabbedPaneRight.addTab("Datatype", Factory.createStrippedScrollPane(rdtdp));
//        tabbedPaneRight.addChangeListener(this);
        
        
        // Detail Panel
        sif_details = new SimpleInternalFrame(
                ResourceManager.getString("iframe.details.service")
                );
        sif_details.setPreferredSize(new Dimension(500, 400));
        //sif_details.add(Factory.createStrippedScrollPane(tabbedPaneRight));        
        sif_details.add(Factory.createStrippedScrollPane(sdp));     
        //System.out.println("COMPONENT COUNT : "+sif_details.getComponentCount());
                
        // Output Panel
        outputPanel = new OutputPanel();
        
        sif_output = new SimpleInternalFrame(
                ResourceManager.getString("iframe.output")
                );
        sif_output.setPreferredSize(new Dimension(500,320));
        sif_output.add(outputPanel);
        
        
//        // Inner Frames
//        JDesktopPane desktopPane = new JDesktopPane();
//        
//        JInternalFrame serviceFrame  = new JInternalFrame();
//        serviceFrame.setTitle("Services");
//        serviceFrame.setBounds(10, 190, 310, 220);
//        serviceFrame.setVisible(true);
//        serviceFrame.setClosable(true);
//        serviceFrame.setResizable(true);
//        serviceFrame.getContentPane().setLayout(new BorderLayout());
//        serviceFrame.add(sif_details);
//        desktopPane.add(serviceFrame, JLayeredPane.DEFAULT_LAYER);
//        
//        JInternalFrame datatypeFrame = new JInternalFrame();
//        datatypeFrame.setTitle("Datatypes");
//        datatypeFrame.setBounds(110, 190, 310, 220);
//        datatypeFrame.setVisible(true);
//        datatypeFrame.setClosable(true);
//        datatypeFrame.setResizable(true);
//        datatypeFrame.getContentPane().setLayout(new BorderLayout());
//        datatypeFrame.add(sif_wsdl);
//        desktopPane.add(datatypeFrame, JLayeredPane.DEFAULT_LAYER);
                
        return Factory.createStrippedSplitPane(JSplitPane.VERTICAL_SPLIT, sif_details, sif_output, 0.7f);
    }              
    
    /**
     * As soon as a project is loaded, activate panel.
     */
    public void activatePanel() {
        leftSplitPane.setDividerLocation(440);
        sif_status.remove(1);
        sif_status.add( new JScrollPane(this.statusTable) );
        sif_status.setTitle(
                ResourceManager.getString("iframe.projectstatus.active")
                );
        sif_status.validate();
        sif_status.repaint();
        this.panelActivationFlag = true;
    }
    
    public void deactivatePanel() {
        leftSplitPane.setDividerLocation(390);
        sif_status.remove(1);
        sif_status.add( this.logoComponent );
        sif_status.setTitle(
                ResourceManager.getString("iframe.projectstatus.inactive")
                );
        sif_status.validate();
        sif_status.repaint();
        this.panelActivationFlag = false;
    }
}

class ViewComponent extends JComponent {
    private Image image;
    private Image imageBackup;
    protected void paintComponent( Graphics g ) {
        if ( image != null )
            g.drawImage( image, 0, 0, this );
    }

    public void setImage(URL url) {
        image = Toolkit.getDefaultToolkit().getImage(url);
        if ( image != null )
            repaint();
    }

    public void scale(int width) {
        imageBackup = image;
        image = image.getScaledInstance(width, -1, Image.SCALE_SMOOTH ); 
        repaint();
    }
}
