/*
 * ServiceDetailPanel.java
 *
 * Created on 22. September 2006, 01:17
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

import de.dfki.dmas.owls2wsdl.core.AbstractService;
import de.dfki.dmas.owls2wsdl.core.AbstractServiceParameter;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatype;
import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeKB;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.BorderFactory;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;

import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeEvent;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import java.util.Iterator;
import java.util.Observer;
import java.util.Observable;

import de.dfki.dmas.owls2wsdl.gui.models.*;

/**
 *
 * @author Oliver Fourman
 */
public class ServiceDetailPanel extends JComponent implements ListSelectionListener, Observer {
    
    private JTextField  serviceFilenameField;
    private JTextField  serviceVersionField;
    private JTextField  serviceIdField;
    private JTextField  serviceNameField;
    private JTextField  serviceBaseNameField;
    
    private ServiceParameterTableModel serviceParameterModel;
    private JTable      serviceParameterTable;      // JTP
    
    private JTextPane   serviceDescriptionField;    // JTP
    
    private JList       serviceNamespaceList;       // JTP
    private JList       serviceImportedFilesList;   // JTP    
    
    private int counter;
    
    /** Creates a new instance of ServiceDetailPanel */
    public ServiceDetailPanel() {
        counter=0;
        initComponents();
        buildPanel();
        RuntimeModel.getInstance().addObserver(this);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int idx = serviceParameterTable.getSelectedRow();
            System.out.println("SELECTION: "+idx);
        }
    }
    
    /**
     * Creates, intializes and configures the UI components. 
     * Real applications may further bind the components to underlying models. 
     */
    private void initComponents() {
        // General infos
        
        serviceIdField       = new JTextField(30);
        serviceNameField     = new JTextField(30);
        serviceVersionField  = new JTextField( 5);        
        serviceFilenameField = new JTextField(30);
        serviceBaseNameField = new JTextField(30);
        
        serviceIdField.setEditable(false);
        serviceNameField.setEditable(false);
        serviceVersionField.setEditable(false);
        serviceFilenameField.setEditable(false);
        serviceBaseNameField.setEditable(false);
        
        serviceParameterModel = new ServiceParameterTableModel();
        final ServiceParameterTableColumnModel columnModel = new ServiceParameterTableColumnModel();        
                
        serviceParameterTable = new JTable(serviceParameterModel, columnModel);        
        serviceParameterTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        serviceParameterTable.setColumnSelectionAllowed(false);                
        serviceParameterTable.setGridColor(Color.LIGHT_GRAY);
        serviceParameterTable.setPreferredScrollableViewportSize(new Dimension(600,80));
        serviceParameterTable.updateUI();
        serviceParameterTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceParameterTable.getSelectionModel().addListSelectionListener(this);
        
        serviceParameterTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
//                    Point p = e.getPoint();
//                    int row = serviceParameterTable.rowAtPoint(p);
//                    int column = serviceParameterTable.columnAtPoint(p); // This is the view column!
//                    System.out.println("DOUBLE CLICK: "+row+", "+column);
                    System.out.println("[TABLE POS   : "+((JTable)e.getSource()).getSelectedRow());
                    AbstractServiceParameter param = serviceParameterModel.getAbstractServiceParameter(((JTable)e.getSource()).getSelectedRow());
                    if(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().containsKey(param.getUri())) {
                        System.out.println("[ServiceDetailPanel] CHANGE selected AbstractDatatype");
                        AbstractDatatype atype = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(param.getUri());
                        RuntimeModel.getInstance().setSelectedDatatype(atype);
                        RuntimeModel.getInstance().setRuntimeAndNotify(RuntimeModel.PARAMETER_SELECTED);
                    }
                    else {
                        JOptionPane.showMessageDialog(
                            null,                                
                            "No type information for parameter in KB.", 
                            "Parameter Failure",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        serviceDescriptionField  = new JTextPane();        
        //serviceDescriptionField.setBorder(BorderFactory.createLineBorder(Color.GRAY));        
        
        serviceNamespaceList     = new JList(new DefaultListModel());
        serviceNamespaceList.setVisibleRowCount(5);
        serviceNamespaceList.setEnabled(false);
        
        serviceImportedFilesList = new JList(new DefaultListModel());
        serviceImportedFilesList.setVisibleRowCount(5);
        serviceImportedFilesList.setEnabled(false);
    }

    // Building ****************************************************************

    /**
     * Builds the panel. Initializes and configures components first,
     * then creates a FormLayout, configures the layout, creates a builder,
     * sets a border, and finally adds the components.
     */
    public void buildPanel() 
    {
        this.setBorder(Borders.DIALOG_BORDER);
        
        FormLayout layout 
                = new FormLayout("right:pref, 10px, left:0:grow, 10px, right:pref, 10px, fill:pref:grow",
                                 "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 6dlu, p, 3dlu, p");

        this.setLayout(layout);
        CellConstraints cc = new CellConstraints();
        
        this.add(DefaultComponentFactory.getInstance().createSeparator(ResourceManager.getString("service.sep.info")) , cc.xyw( 1, 1, 7));
        this.add(new JLabel("ID")           , cc.xy ( 1, 3));
        this.add(serviceIdField             , cc.xyw( 3, 3, 5));
        this.add(new JLabel("Name")         , cc.xy ( 1, 5));   
        this.add(serviceNameField           , cc.xy ( 3, 5));
        this.add(new JLabel("OWL-S Version"), cc.xy ( 5, 5));
        this.add(serviceVersionField        , cc.xy ( 7, 5));
        this.add(new JLabel("Filename")     , cc.xy ( 1, 7));
        this.add(serviceFilenameField       , cc.xyw( 3, 7, 5));
        this.add(new JLabel("Basename")     , cc.xy ( 1, 9));
        this.add(serviceBaseNameField       , cc.xyw( 3, 9, 5));
        
        this.add(DefaultComponentFactory.getInstance().createSeparator(ResourceManager.getString("service.sep.interface")) , cc.xyw( 1, 11, 7));
        
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.add("Parameter", new JScrollPane(serviceParameterTable));
        tabs.add("Description", new JScrollPane(serviceDescriptionField));
        tabs.add("Namespace", new JScrollPane(serviceNamespaceList));
        tabs.add("Imports", new JScrollPane(serviceImportedFilesList));
        
        this.add(tabs                       , cc.xyw(1,13,7));
    }
   
    private JPanel createNamespacePanel() {
        PanelBuilder builder = new PanelBuilder(new FormLayout("pref, 10px, pref:grow"));
        CellConstraints cc = new CellConstraints();
        
        //builder.setDefaultDialogBorder();
        for(int i=1; i<=3; i++) {
            int pos=(2*i)-1;
            String name = "NAME"+String.valueOf(i);        
            builder.appendRow("pref");
            builder.add(new JTextField(name+"key", 5)  , cc.xy( 1, pos));
            builder.add(new JTextField(name+"val",25)  , cc.xy( 3, pos));
            builder.appendRow("2dlu");
        }
               
        return builder.getPanel();
    }    
    
    // Updating ****************************************************************
    
    /**
     * Observer
     */
    public void update(Observable observable, Object object) {        
        if(object!=null) {
            String objString = object.toString();
            System.out.println("[u] update ServiceDetailPanel: "+objString);
        }
        
        System.out.println("[u] update ServiceDetailPanel, Runtime: "+RuntimeModel.getInstance().getStatus("RUNTIME"));
        
        if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SERVICE_MODEL_CHANGED) ||
           RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SINGLE_SERVICE_SELECTED) ) {            
            if(RuntimeModel.getInstance().getSelectedService()!=null) {
                this.updateServiceDetailPanel();
            }
            else {
                this.serviceParameterModel.removeAllParameter();
                this.clearPanel();
            }
            this.serviceParameterModel.fireTableDataChanged();
            this.serviceParameterTable.revalidate();
            this.serviceParameterTable.updateUI();
        }        
    }
    
    
    public void clearPanel()
    {       
        Component[] sdpComponents = this.getComponents();
        for(int i=0; i<sdpComponents.length; i++) {
            if(sdpComponents[i].getClass().getName().equals("javax.swing.JTextField") ||
               sdpComponents[i].getClass().getName().equals("javax.swing.JTextPane")) {
                ((JTextField)sdpComponents[i]).setText("");
                ((JTextField)sdpComponents[i]).setEnabled(false);
            }
        }
        
//        serviceFilenameField.setText("");
//        serviceVersionField.setText("");
//        serviceIdField.setText("");
//        serviceNameField.setText("");
//        serviceBaseNameField.setText("");
        
        ((ServiceParameterTableModel)serviceParameterTable.getModel()).removeAllParameter();
        serviceParameterTable.revalidate();
        serviceParameterTable.updateUI();
        
        serviceDescriptionField.setText("");
        serviceDescriptionField.setEnabled(false);
        
        ((DefaultListModel)serviceNamespaceList.getModel()).removeAllElements();
        serviceNamespaceList.revalidate();
        
        ((DefaultListModel)serviceImportedFilesList.getModel()).removeAllElements();
        serviceImportedFilesList.revalidate();
        
        this.updateUI();
    }
    
    public void updateServiceDetailPanel() 
    {   
        AbstractService aservice = RuntimeModel.getInstance().getSelectedService();
        
        Component[] sdpComponents = this.getComponents();
        for(int i=0; i<sdpComponents.length; i++) {
            if(sdpComponents[i].getClass().getName().equals("javax.swing.JTextField")) {
                ((JTextField)sdpComponents[i]).setEnabled(true);
            }
        }
        
        this.serviceParameterModel.reinitParameter( aservice.getInputParameter(), aservice.getOutputParameter() );
        
        serviceFilenameField.setText(aservice.getFilename());
        serviceVersionField.setText(aservice.getVersion());
        serviceIdField.setText(aservice.getID());
        serviceNameField.setText(aservice.getName());
        serviceBaseNameField.setText(aservice.getBase());
        
        serviceDescriptionField.setEnabled(true);
        serviceDescriptionField.setText(aservice.getDescription());        
        
        final DefaultListModel model_1 = new DefaultListModel();
        final DefaultListModel model_2 = new DefaultListModel();
        
        for(Iterator it=aservice.getNamespaceEntries().values().iterator(); it.hasNext(); ) {            
            model_1.addElement(it.next().toString());
        }
        serviceNamespaceList.setModel(model_1);
        if(serviceNamespaceList.isShowing()) { 
            serviceNamespaceList.revalidate(); 
        }
        
        for(Iterator it=aservice.getImportedOWLFiles().iterator(); it.hasNext(); ) {
            model_2.addElement(it.next().toString());
        }
        serviceImportedFilesList.setModel(model_2);
        if(serviceImportedFilesList.isShowing()) { 
            serviceImportedFilesList.revalidate(); 
        }
 
        //DYN SUBPANEL FÜR NS
        counter++;
        System.out.println("PANEL UPDATE "+counter);
    }
}


