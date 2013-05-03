/*
 * RegisteredDatatypeDetailPanel.java
 *
 * Created on 27. September 2006, 16:17
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
import de.dfki.dmas.owls2wsdl.core.AbstractDatatypeElement;
import de.dfki.dmas.owls2wsdl.gui.models.AbstractDatatypeElementListModel;
import de.dfki.dmas.owls2wsdl.gui.models.AbstractDatatypeMetaElementListModel;
import de.dfki.dmas.owls2wsdl.gui.models.RegisteredDatatypeElementTableModel;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.border.*;
import javax.swing.BorderFactory;
import javax.swing.table.*;
import javax.swing.JButton;
//import javax.swing.event.*;

import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.uif_lite.component.Factory;

import java.util.Vector;
import java.util.Iterator;

import de.dfki.dmas.owls2wsdl.core.*;
import de.dfki.dmas.owls2wsdl.gui.models.*;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

/**
 *
 * @author Oliver Fourman
 */
public class RegisteredDatatypeDetailPanel extends JComponent implements ChangeListener {
    
    private JTextField  datatypeUrlField;
    private JComboBox   datatypeRdfTypeField;
    private JComboBox   datatypeXsdTypeField;
    
    private JButton     lockRdfTypeButton;
    private JButton     unlockRdfTypeButton;
    
    private JButton     saveXsdTypeButton;
    private JButton     removeXsdTypeButton;
    
//    private JTextField versionField;
//    private JTextField typeField;
       
    private JTable      datatypeElementTable;
    private AbstractDatatypeTableModel tmodel_elements;
        
    private JList       datatypeParentList;
    private JList       datatypeIntersectionList;
    private JList       datatypeRangeList;
    
    private JList       datatypeMetaElementList;
    private AbstractDatatypeMetaElementListModel model_metaelements;
    
    private JTextArea   datatypeDescriptionField;
            
    private JTextArea   exceptionTextArea;
    
    private JTabbedPane dtTabs;                      // Element Information       
    private  java.net.URL imageFailed;
        
    private JTable      elementRestrictionTable;     // Element Restriction Information
    private RegisteredDatatypeElementTableModel restrictionTableModel;
    private JScrollPane restrictionRangeScrollPane;

    /** Creates a new instance of RegisteredDatatypeDetailPanel */
    public RegisteredDatatypeDetailPanel() {
        initComponents();
        buildPanel();
    }
    
    public void stateChanged(ChangeEvent event)
    {
        System.out.println("[Tab] ChangeEvent: "+dtTabs.getTitleAt(dtTabs.getSelectedIndex()));
        datatypeElementTable.clearSelection();
        restrictionTableModel.removeAllElements();
        elementRestrictionTable.revalidate();
        elementRestrictionTable.updateUI();
                
        //this.revalidate();
        //this.updateUI();
        
        
        //this.sif_overview.setTitle(tabbedPaneLeft.getTitleAt(tabbedPaneLeft.getSelectedIndex()));        
        
        
//        if(dtTabs.getTitleAt(dtTabs.getSelectedIndex()).equals("Elements") ||
//           dtTabs.getTitleAt(dtTabs.getSelectedIndex()).equals("Model") ) {            
//            //((AbstractDatatypeElementListModel)datatypeElementList.getModel()).getMetaData(false);            
//            //datatypeElementList.revalidate();
//            elementInformationPanel.setVisible(true);
//        }        
////        else if(dtTabs.getTitleAt(dtTabs.getSelectedIndex()).equals("Model")) {
////            //((AbstractDatatypeElementListModel)datatypeElementList.getModel()).getMetaData(true);            
////            //datatypeMetaElementList.revalidate();
////            elementInformationPanel.setVisible(true);
////        }
//        else {
//            elementInformationPanel.setVisible(false);
//        }
        

    }
    
    private void setDefaultTypes4XsdTypeField() {
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).removeAllElements();
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#anyType");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#string");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#decimal");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#integer");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#float");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#boolean");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#date");
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement("http://www.w3.org/2001/XMLSchema#time");
        datatypeXsdTypeField.revalidate();
        datatypeXsdTypeField.updateUI();
    }
    
    /**
     * Creates, intializes and configures the UI components. 
     */
    private void initComponents() {
        
        imageFailed = getClass().getClassLoader().getResource("images/failed.gif");
        
//        datatypeLocalNameField  = new JTextField(30);
        datatypeUrlField = new JTextField(30);
        //datatypeUrlField.setEnabled(false);
        datatypeUrlField.setEditable(false);
        datatypeUrlField.setBackground(Color.WHITE);
        
        datatypeRdfTypeField = new JComboBox( new DefaultComboBoxModel() );
        ((DefaultComboBoxModel)datatypeRdfTypeField.getModel()).addElement("http://www.w3.org/2000/01/rdf-schema#Resource");
//        datatypeRdfTypeField.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Selected RdfType: "+datatypeRdfTypeField.getSelectedItem().toString());
//                if(datatypeRdfTypeField.getSelectedItem().toString().equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
//                    datatypeXsdTypeField.setSelectedItem(AbstractDatatype.DEFAULT_XSDTYPE);
//                }
//                else {
//                    AbstractDatatype atype = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(datatypeRdfTypeField.getSelectedItem().toString());
//                    updateXsdTypeInformationSettings(atype);
//                }
//            }            
//        });
        datatypeRdfTypeField.setEnabled(false);
        
        datatypeXsdTypeField = new JComboBox( new DefaultComboBoxModel() );        
        this.setDefaultTypes4XsdTypeField();

        Dimension defaultButtonDim1 = new Dimension(45,datatypeRdfTypeField.getPreferredSize().height);
        Dimension defaultButtonDim2 = new Dimension(55,datatypeRdfTypeField.getPreferredSize().height);
        
        lockRdfTypeButton = new JButton("Lock");
        lockRdfTypeButton.setPreferredSize(defaultButtonDim1);
        lockRdfTypeButton.setEnabled(false);
        lockRdfTypeButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("NOT YET IMPLEMENTED!");
            }
        });
        
        unlockRdfTypeButton = new JButton("Unlock");
        unlockRdfTypeButton.setPreferredSize(defaultButtonDim2);
        unlockRdfTypeButton.setEnabled(false);
        
        saveXsdTypeButton = new JButton("Save");
        saveXsdTypeButton.setPreferredSize(defaultButtonDim1);
        saveXsdTypeButton.setEnabled(false);
        saveXsdTypeButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RuntimeModel.getInstance().getSelectedDatatype().setXsdType(datatypeXsdTypeField.getSelectedItem().toString());
                updateXsdTypeInformationSettings(RuntimeModel.getInstance().getSelectedDatatype());
            }            
        });
        
        removeXsdTypeButton = new JButton("Remove");
        removeXsdTypeButton.setPreferredSize(defaultButtonDim2);
        removeXsdTypeButton.setEnabled(false);
        removeXsdTypeButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RuntimeModel.getInstance().getSelectedDatatype().setXsdType(null);
                updateXsdTypeInformationSettings(RuntimeModel.getInstance().getSelectedDatatype());
            }            
        });               
                
        tmodel_elements = new AbstractDatatypeTableModel();
        datatypeElementTable = new JTable(tmodel_elements);
        datatypeElementTable.setColumnModel(new AbstractDatatypeTableColummModel());
        datatypeElementTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);        
        datatypeElementTable.setPreferredScrollableViewportSize(new Dimension(550,100));                
        datatypeElementTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    System.out.println("[i] selectedRow: "+datatypeElementTable.getSelectedRow());
                    if(datatypeElementTable.getSelectedRow()>=0) {
                        System.out.println("ValueIsAdjusting: "+((AbstractDatatypeTableModel)datatypeElementTable.getModel()).getValueAt(datatypeElementTable.getSelectedRow(), datatypeElementTable.getSelectedColumn()));
                        AbstractDatatypeElement elem = (AbstractDatatypeElement)((AbstractDatatypeTableModel)datatypeElementTable.getModel()).getAbstractDatatypeElementAt(datatypeElementTable.getSelectedRow());                    
                        ((RegisteredDatatypeElementTableModel)elementRestrictionTable.getModel()).updateModel(elem);
                        elementRestrictionTable.revalidate();
                        elementRestrictionTable.updateUI();
                    }
                    else {
                        System.out.println("[i] clearSelection event");
                    }
                }
            }
        });
        datatypeElementTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        
        final DefaultListModel parentInformation = new DefaultListModel();
        datatypeParentList = new JList(parentInformation);                
        datatypeParentList.setVisibleRowCount(4);
//        datatypeParentList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        final DefaultListModel intersectionInformation = new DefaultListModel();
        datatypeIntersectionList = new JList(intersectionInformation);
        datatypeIntersectionList.setVisibleRowCount(4);
//        datatypeIntersectionList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        model_metaelements = new AbstractDatatypeMetaElementListModel();        
        datatypeMetaElementList = new JList(model_metaelements);
        datatypeMetaElementList.setVisibleRowCount(4);
        datatypeMetaElementList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    System.out.println(">>>> "+ ((AbstractDatatypeMetaElementListModel)datatypeMetaElementList.getModel()).getElementAt(datatypeMetaElementList.getSelectedIndex()));
                    //updateElementInformationPanel(((AbstractDatatypeElementListModel)datatypeElementList.getModel()).getAbstractDatatypeElementAt(datatypeElementList.getSelectedIndex()));
                }
            }
        });
        
        final DefaultListModel rangeListModel = new DefaultListModel();
        datatypeRangeList = new JList(rangeListModel);
        datatypeRangeList.setVisibleRowCount(4);
//        datatypeRangeList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        datatypeDescriptionField = new JTextArea();
        datatypeDescriptionField.setLineWrap(true);
        
        exceptionTextArea = new JTextArea();
        exceptionTextArea.setLineWrap(true);
        
        // === ElementRestrictionTable
        restrictionTableModel = new RegisteredDatatypeElementTableModel();
        elementRestrictionTable = new JTable(restrictionTableModel, new RegisteredDatatypeElementTableColumnModel());
        elementRestrictionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        elementRestrictionTable.setPreferredScrollableViewportSize(new Dimension(500,60));                
        elementRestrictionTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementRestrictionTable.setEnabled(false);
        elementRestrictionTable.getColumnModel().getColumn(0).setWidth(100);
        elementRestrictionTable.getColumnModel().getColumn(1).setPreferredWidth(400);
//        elementRestrictionTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
//            public void columnSelectionChanged(ListSelectionEvent e) { }
//            public void columnMarginChanged(ChangeEvent e) { 
//                System.out.println("COLUMN WIDTH  : "+elementRestrictionTable.getColumnModel().getColumn(0).getWidth()+" "+elementRestrictionTable.getColumnModel().getColumn(1).getWidth());
//            }
//            public void columnRemoved(TableColumnModelEvent e) { }
//            public void columnAdded(TableColumnModelEvent e) { }
//            public void columnMoved(TableColumnModelEvent e) { }
//        });
        
        restrictionRangeScrollPane = new JScrollPane(elementRestrictionTable);
    }
    
    // Building *************************************************************

    /**
     * Builds the panel. Initializes and configures components first,
     * then creates a FormLayout, configures the layout, creates a builder,
     * sets a border, and finally adds the components.
     * 
     * @return the built panel
     */
    private void buildPanel() {
        this.setBorder(Borders.DIALOG_BORDER);
        this.setMinimumSize(new Dimension(400,400));
        //fill:pref:grow
        FormLayout layout
                = new FormLayout("right:pref, 10px, fill:pref:grow, 2dlu, right:pref",
                                 "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 6dlu, p, 6dlu, p");
                                 
        this.setLayout(layout);
        CellConstraints cc = new CellConstraints();
        
        this.add(DefaultComponentFactory.getInstance().createSeparator(ResourceManager.getString("type.sep.info")) , cc.xyw( 1, 1, 5));
//        this.add(new JLabel("Name")         , cc.xy ( 1, 3));
//        this.add(datatypeLocalNameField     , cc.xy ( 3, 3));      
        this.add(new JLabel("OWL URI"),    cc.xy ( 1, 3));   
        this.add(datatypeUrlField,         cc.xyw( 3, 3, 3));
        
        this.add(new JLabel("RDF Type", SwingConstants.RIGHT),   cc.xy ( 1, 5));
        this.add(datatypeRdfTypeField,                           cc.xy ( 3, 5));
        
        JPanel buttonPanel4RdfType = new JPanel();
        buttonPanel4RdfType.add(lockRdfTypeButton);
        buttonPanel4RdfType.add(unlockRdfTypeButton);
        this.add(buttonPanel4RdfType, cc.xy ( 5, 5));
        
        this.add(new JLabel("XSD Type", SwingConstants.RIGHT),   cc.xy ( 1, 7));
        this.add(datatypeXsdTypeField,                           cc.xy ( 3, 7));
        
        JPanel buttonPanel4XsdType = new JPanel();
        //buttonPanel4XsdType.setBackground(Color.BLUE);
        buttonPanel4XsdType.add(saveXsdTypeButton);
        buttonPanel4XsdType.add(removeXsdTypeButton);
        this.add(buttonPanel4XsdType, cc.xy ( 5, 7));
                
        dtTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        dtTabs.add("Elements", new JScrollPane(datatypeElementTable));  //datatypeElementList
        dtTabs.add("Description", new JScrollPane(datatypeDescriptionField));
        dtTabs.add("Super Classes", new JScrollPane(datatypeParentList));
        dtTabs.add("Intersection Classes", new JScrollPane(datatypeIntersectionList));        
        dtTabs.add("OWL Model", new JScrollPane(datatypeMetaElementList));
        dtTabs.add("Individuals", new JScrollPane(datatypeRangeList));
        //dtTabs.add("Exceptions", new JScrollPane(exceptionTextArea)); // dynamic
        //dtTabs.setEnabledAt(6, false);
        //dtTabs.setIconAt(6, new javax.swing.ImageIcon(this.imageFailed));
        dtTabs.addChangeListener(this);        
        
        this.add(dtTabs                        , cc.xyw( 1, 9, 5));                
        //this.add(buildElementInformationPanel(), cc.xyw( 1,11, 5));
                
        this.add(DefaultComponentFactory.getInstance().createSeparator(ResourceManager.getString("type.sep.range")), cc.xyw( 1, 11, 5));
        this.add(restrictionRangeScrollPane, cc.xyw(1, 13, 5));
    }   
       
    private JTextArea createArea(
            String text,
            boolean lineWrap, 
            int columns,
            Dimension minimumSize) {
        JTextArea area  = new JTextArea(text);
        area.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1)));
        area.setLineWrap(lineWrap);
        area.setWrapStyleWord(true);
        area.setColumns(columns);
        if (minimumSize != null) {
            area.setMinimumSize(new Dimension(100, 32));
        }
        return area;
    }        
    
    private void deactivateXsdTypeInformationSettings() {
        datatypeXsdTypeField.setEnabled(false);
        saveXsdTypeButton.setEnabled(false);
        removeXsdTypeButton.setEnabled(false);        
    }
    
    /**
      * Check, if we can find any rdf:type and use the xsd type from there.
      * @param abstract type reference
      * @return found flag, shows if something other than the default xsd type found.
      */
    private boolean updateXsdTypeFromRdfType(AbstractDatatype atype, String DEFAULT_XSDTYPE) {
        boolean found = false;
        System.out.println("TYPE INFO");
        atype.printDatatype();
        
        for(int i=0; i<datatypeRdfTypeField.getItemCount(); i++)
        {
            System.out.println("[i] process rdf:type "+datatypeRdfTypeField.getItemAt(i));
            String xsdTypeString = DEFAULT_XSDTYPE; // from project!
            
            if(atype.getTypeList().size() == 1) {
                xsdTypeString = atype.searchInRdfTypesForXsdType(DEFAULT_XSDTYPE);
                ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).setSelectedItem(xsdTypeString);
            }
            else if(atype.getTypeList().size() > 1) {                                
                String rdfTypeString = datatypeRdfTypeField.getSelectedItem().toString();
                if(AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(rdfTypeString).hasXsdType()) {
                    xsdTypeString = AbstractDatatypeKB.getInstance().getAbstractDatatypeKBData().get(rdfTypeString).getXsdType();
                }
                ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).setSelectedItem(xsdTypeString);
            }
            else {
                ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).setSelectedItem(DEFAULT_XSDTYPE);
            }

            if(!xsdTypeString.equals(DEFAULT_XSDTYPE)) {
                System.out.println("FOUND: "+xsdTypeString);
                found = true;
            }
            
//            if(!datatypeRdfTypeField.getSelectedItem().equals("http://www.w3.org/2000/01/rdf-schema#Resource")) 
//            {
//            }
//            else {
//                ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).setSelectedItem(DEFAULT_XSDTYPE);
//                System.out.println("SET Default for Resource");
//            }
        }
        return found;
    }
    
    private boolean updateXsdTypeFromParent(AbstractDatatype atype, String DEFAULT_XSDTYPE) {
        boolean found = false;
        String xsdTypeString = atype.searchInParentsForXsdType(DEFAULT_XSDTYPE);
        ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).setSelectedItem(xsdTypeString);
        saveXsdTypeButton.setEnabled(true);
        removeXsdTypeButton.setEnabled(false);
        if(!xsdTypeString.equals(DEFAULT_XSDTYPE)) {
            System.out.println("FOUND: "+xsdTypeString);
            found = true;
        }
        return found;
    }
    
    private void updateXsdTypeInformationSettings(AbstractDatatype atype) {
        datatypeXsdTypeField.setEnabled(true);
        final String DEFAULT_XSDTYPE = RuntimeModel.getInstance().getProject().getDefaultXsdType();
                
        if(atype.hasXsdType()) {
            ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).removeAllElements();
            ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).addElement(atype.getXsdType());
            saveXsdTypeButton.setEnabled(false);
            removeXsdTypeButton.setEnabled(true);
        }
        else {
            this.setDefaultTypes4XsdTypeField();
            saveXsdTypeButton.setEnabled(true); 
            removeXsdTypeButton.setEnabled(false);
            // type doesn't have any user edited primitive type information.
            String inheritanceBehaviour = RuntimeModel.getInstance().getProject().getTypeInheritanceBehaviour();
            System.out.println("TypeInheritanceBehaviour: "+inheritanceBehaviour);
            if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceByRDFTypeOnly)) {
                this.updateXsdTypeFromRdfType(atype, DEFAULT_XSDTYPE);
            }
            else if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceBySuperClassOnly)) {
                this.updateXsdTypeFromParent(atype, DEFAULT_XSDTYPE);
            }
            else if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceByRDFTypeFirstParentsSecond)) {                		
                if(!this.updateXsdTypeFromRdfType(atype, DEFAULT_XSDTYPE)) {
                    this.updateXsdTypeFromParent(atype, DEFAULT_XSDTYPE);
		}
            }
            else if(inheritanceBehaviour.equals(AbstractDatatype.InheritanceByParentsFirstRDFTypeSecond)) {
		if(!this.updateXsdTypeFromParent(atype, DEFAULT_XSDTYPE)) {
                    this.updateXsdTypeFromRdfType(atype, DEFAULT_XSDTYPE);
		}
            }
            else {
		System.out.println("[i] no inheritance behaviour set.");
                ((DefaultComboBoxModel)datatypeXsdTypeField.getModel()).setSelectedItem(DEFAULT_XSDTYPE);
            }
        }
        this.updateUI();
    }
    
    /**
     * Makes a tab visible.
     * @param aFlag true to show the error message tab
     */
    private void setVisibleErrorMessageTab(boolean aFlag) {
        if(aFlag) {
            if(this.dtTabs.getTabCount()<7) {
                System.out.println("ADD TAB");
                this.dtTabs.add(new JScrollPane(exceptionTextArea), "Exceptions", 6);
                this.dtTabs.setIconAt(6, new javax.swing.ImageIcon(this.imageFailed));
                this.dtTabs.setSelectedIndex(6);
            }
        }
        else {
            if(this.dtTabs.getTabCount()==7) {
                System.out.println("REMOVE TAB");
                this.dtTabs.remove(6);
                this.dtTabs.setSelectedIndex(0);
            }
        }
    }
    
    public void updatePanel(Object obj) {                
        AbstractDatatype atype = (AbstractDatatype)obj;
        //atype.printDatatype();
        
        if(atype.getErrorMessages().isEmpty()) {
            if(dtTabs.getTabCount()==7) {
                this.exceptionTextArea.setText("");
                dtTabs.setSelectedIndex(0);
//                dtTabs.setEnabledAt(6, false);
                dtTabs.remove(6);
            }
        }        
        else {
            //this.setVisibleErrorMessageTab(true);           
            String info = "";
            for(int i=0; i<atype.getErrorMessages().size(); i++) {                
                info = info+"\n"+atype.getErrorMessages().get(i).toString();
            }
            this.exceptionTextArea.setText(info);
            
            if(dtTabs.getTabCount()==6) {                
                dtTabs.add("Exceptions", new JScrollPane(exceptionTextArea));
                dtTabs.setIconAt(6, new javax.swing.ImageIcon(this.imageFailed));
//                dtTabs.setEnabledAt(6, true);
            }
            //dtTabs.setSelectedIndex(6);
        }
        dtTabs.updateUI();
        
        datatypeUrlField.setText(atype.getUrl());
        datatypeDescriptionField.setText(atype.getRdfsComment());
        
        //
        // lookup rdf:type setting
        //
        if(atype.getTypeList().isEmpty()) {
            ((DefaultComboBoxModel)datatypeRdfTypeField.getModel()).removeAllElements();
            ((DefaultComboBoxModel)datatypeRdfTypeField.getModel()).addElement("http://www.w3.org/2000/01/rdf-schema#Resource");            
            datatypeRdfTypeField.setEnabled(false);            
        }
        else {
            ((DefaultComboBoxModel)datatypeRdfTypeField.getModel()).removeAllElements();
            for(Iterator it=atype.getTypeList().iterator(); it.hasNext(); ) {                
                ((DefaultComboBoxModel)datatypeRdfTypeField.getModel()).addElement(it.next().toString());
            }            
            datatypeRdfTypeField.setEnabled(true);            
        }
        datatypeRdfTypeField.revalidate();
        
        int depth = RuntimeModel.getInstance().getProject().getElementDepth();        
        System.out.println("depth: "+depth);
        
        this.updateXsdTypeInformationSettings(atype);   
        
        // -- Parent list
        datatypeParentList.clearSelection();
        DefaultListModel parentInformation = (DefaultListModel)datatypeParentList.getModel();        
        parentInformation.removeAllElements();        
        for(Iterator it=atype.getParentList().iterator(); it.hasNext(); ) {
            String superclassUri = it.next().toString();
            parentInformation.addElement(superclassUri);
        }
        datatypeParentList.revalidate();
        datatypeParentList.updateUI();
        
        // -- Intersection list
        datatypeIntersectionList.clearSelection();
        DefaultListModel intersectionInformation = (DefaultListModel)datatypeIntersectionList.getModel();        
        intersectionInformation.removeAllElements();
        for(Iterator it=atype.getIntersectionList().iterator(); it.hasNext(); ) {
            String intersecUri = it.next().toString();
            intersectionInformation.addElement(intersecUri);
        }
        datatypeIntersectionList.revalidate();
        datatypeIntersectionList.updateUI();
        
//        if( atype.getXSDType().equals("SIMPLE") ) {
//            this.elementTable.setEnabled(false);
//        }
//        else {
//            this.elementTable.setEnabled(true);
//        }
        
//        this.tableModel.updateModel(atype);
//        datatypeElementTable.revalidate();
//        datatypeElementTable.updateUI();
                
        datatypeElementTable.clearSelection();
        tmodel_elements.updateModel(atype);
        datatypeElementTable.revalidate();
        datatypeElementTable.updateUI();
        datatypeElementTable.addNotify();
        dtTabs.setTitleAt(0, "Elements ("+tmodel_elements.getRowCount()+")");
        
        datatypeMetaElementList.clearSelection();
        model_metaelements.updateModel(atype);        
        datatypeMetaElementList.revalidate();
        datatypeMetaElementList.updateUI();
        
        datatypeRangeList.clearSelection();
        DefaultListModel rangeListModel = (DefaultListModel)datatypeRangeList.getModel();
        rangeListModel.removeAllElements();
        for(Iterator it=atype.getIndividualRange().keySet().iterator(); it.hasNext(); ) {
            rangeListModel.addElement(it.next().toString());            
        }        
        datatypeRangeList.revalidate();
        datatypeRangeList.updateUI();
                
        restrictionTableModel.removeAllElements();
        elementRestrictionTable.revalidate();
        elementRestrictionTable.updateUI();
    }

}


