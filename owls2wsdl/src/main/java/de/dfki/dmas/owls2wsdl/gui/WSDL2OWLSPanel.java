/*
 * WSDL2OWLSPanel.java
 *
 * Created on 29. Januar 2007, 00:49
 *
 * Modified source of OWL-S API.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.namespace.QName;

import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.vocabulary.OWL;
import org.mindswap.owl.vocabulary.XSD;
import org.mindswap.utils.QNameProvider;
import org.mindswap.utils.SwingUtils;
import org.mindswap.utils.URIUtils;
import org.mindswap.wsdl.WSDLConsts;
import org.mindswap.wsdl.WSDLOperation;
import org.mindswap.wsdl.WSDLParameter;
import org.mindswap.wsdl.WSDLService;
import org.mindswap.wsdl.WSDLTranslator;

import org.mindswap.owls.validator.*;
import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;
import de.dfki.dmas.owls2wsdl.core.*;
import de.dfki.dmas.owls2wsdl.grounding.*;


/**
 * A simple to GUI to create OWL-S files from WSDL descriptions.
 *
 * @author (c) 2004 Evren Sirin and modified by Oliver Fourman
 */
public class WSDL2OWLSPanel extends JPanel implements ActionListener {
    final String[] nsColumnNames  = {"Abbr", "URI"};
    final String[] columnNames  = {
        "WSDL Parameter", "WSDL Type",
        "OWL-S Name", "OWL Type", "XSLT"};
    
    final String[][] emptyRow	= new String[0][5];
    
    final String[] defaultFiles = {
        "http://cheeso.members.winisp.net/books/books.asmx?WSDL",
        "http://www.mindswap.org/axis/services/TranslatorService?wsdl",
        "http://www.mindswap.org/axis/services/DictionaryService?wsdl",
        "http://www.swanandmokashi.com/HomePage/WebServices/StockQuotes.asmx?WSDL",
        "http://www.tilisoft.com/ws/LocInfo/ZipCode.asmx?WSDL",
        "http://www.webservicex.net/uszip.asmx?WSDL",
    };
    
    private String url;
    
    public  List savedServices = new ArrayList();
    
    JComboBox  wsdlUriComboBox = new JComboBox(defaultFiles);
    
    //JList      opList		   = new JList(); // --> resulting wsdl definition has only one get-operation
    private List ops;
    JComboBox    opComboBox;
    
    JTable     inputTable      = new JTable(emptyRow, columnNames);
    JTable     outputTable     = new JTable(emptyRow, columnNames);
    JTextField nameSpaceField  = new JTextField();
    
    //JTextField wsdlUriField    = new JTextField();
    
    JTextField serviceNameField= new JTextField();
    JTextArea  textDescription = new JTextArea(5, 20);
    JTable     nsTable         = new JTable(0, 2);
    JButton    loadButton, generateButton, addNSButton, removeNSButton, debugButton, clearButton;

    JDialog advanced;
    JRadioButton prefixButton, manualButton;
    JTextField prefixText    = new JTextField();
    JTextField serviceText   = new JTextField();
    JTextField profileText   = new JTextField();
    JTextField processText   = new JTextField();
    JTextField groundingText = new JTextField();
    
    QNameProvider qnames = new QNameProvider();
    
    HashMap previousWSDLDefinitions;
    boolean LOADGENERATED_FLAG = false;
    
    public WSDL2OWLSPanel() 
    {
        this.url =  "http://www.tilisoft.com/ws/LocInfo/ZipCode.asmx?WSDL";
        
        this.ops = new ArrayList();
        opComboBox = new JComboBox();
        
        opComboBox.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                doSelect();
            }
        });
        
        JPanel contentPane = new JPanel();
        JPanel addressPanel = new JPanel();
        JPanel middlePanel = new JPanel();
        
        //JPanel operationsPanel = new JPanel();
        
        JPanel detailsPanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        setLayout(new GridLayout(1,1));
        add(contentPane);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        contentPane.add(addressPanel);
        contentPane.add(Box.createVerticalStrut(2));
        contentPane.add(middlePanel);
        contentPane.add(Box.createVerticalStrut(2));
        contentPane.add(buttonPanel);

//        JButton browseButton = new JButton("Browse Local...");
//        browseButton.setActionCommand("browse");
//        browseButton.addActionListener(this);
        
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));
        
//        addressPanel.add(new JLabel("Enter URL: "));
//        addressPanel.add(Box.createHorizontalStrut(2));
//        addressPanel.add(urls); urls.setEditable(true); urls.setSelectedItem("");
        
//        addressPanel.add(Box.createHorizontalStrut(2));
//        addressPanel.add(browseButton);
        
        addressPanel.add(Box.createHorizontalStrut(2));

//        urls.setActionCommand("load");
//        urls.addActionListener(this);

        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
//        middlePanel.add(operationsPanel);
        middlePanel.add(detailsPanel);

        JPanel servicePanel = new JPanel();
        JPanel inputsPanel  = new JPanel();
        JPanel outputsPanel = new JPanel();
        JPanel nsPanel = new JPanel();
        JPanel nsButtons = new JPanel();

        JScrollPane textDescriptionPane = new JScrollPane(textDescription);
        textDescriptionPane.setPreferredSize(
            new Dimension(textDescriptionPane.getPreferredSize().width, 50));
        textDescriptionPane.setMinimumSize(
            new Dimension(textDescriptionPane.getPreferredSize().width, 50));

        JButton advancedButton = new JButton("Advanced...");
        advancedButton.setActionCommand("advanced");
        advancedButton.addActionListener(this);

        wsdlUriComboBox.setEditable(true);
        //wsdlUriComboBox.setActionCommand("load_existing");  // GUI Work necessary!!!
        //wsdlUriComboBox.addActionListener(this);           // to use this feature.
        
        // GridbagLayout used!, not TableLayout!        
        servicePanel = createTableLayout(
            new JComponent[] {
                new JLabel("Grounding (WSDL)"),
                new JLabel("Operations"),
                new JLabel("Service Name"),
                new JLabel("Text description"),
                new JLabel("xml:base URI")                
//			    advancedButton,
            },
            new JComponent[] {                
                wsdlUriComboBox,
                opComboBox,
                serviceNameField,
                textDescriptionPane,
                nameSpaceField                
//			    new JLabel("")
             }
        );
        
        servicePanel.setBorder(BorderFactory.createTitledBorder("Service information"));

        textDescription.setLineWrap(true);
        textDescription.setWrapStyleWord(true);

        nsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nsTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) return;

                    removeNSButton.setEnabled(true);
                }
            }
        );

        addNSButton = new JButton("Add");
        removeNSButton = new JButton("Remove");

        addNSButton.setActionCommand("addNS");
        addNSButton.addActionListener(this);

        removeNSButton.setActionCommand("removeNS");
        removeNSButton.addActionListener(this);

        nsButtons.setLayout(new BoxLayout(nsButtons, BoxLayout.X_AXIS));
        nsButtons.add(Box.createHorizontalGlue());
        nsButtons.add(addNSButton);
        nsButtons.add(Box.createHorizontalStrut(5));
        nsButtons.add(removeNSButton);

        nsTable.getPreferredScrollableViewportSize().setSize(400,100);
        nsPanel.setLayout(new BoxLayout(nsPanel, BoxLayout.Y_AXIS));
        nsPanel.setBorder(BorderFactory.createTitledBorder("Namespaces"));
        nsPanel.add(new JScrollPane(nsTable));
        nsPanel.add(Box.createVerticalStrut(2));
        nsPanel.add(nsButtons);

        inputTable.getPreferredScrollableViewportSize().setSize(400,50);
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));
        inputsPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));        
        inputsPanel.add(new JScrollPane(inputTable));

        outputTable.getPreferredScrollableViewportSize().setSize(400,50);
        outputsPanel.setLayout(new BoxLayout(outputsPanel, BoxLayout.Y_AXIS));
        outputsPanel.setBorder(BorderFactory.createTitledBorder("Outputs"));
        outputsPanel.add(new JScrollPane(outputTable));

        loadButton = new JButton("Load selected Service");
        loadButton.setEnabled(true);
        loadButton.setActionCommand("load");
        loadButton.addActionListener(this);
        
        debugButton = new JButton("Debug");
        debugButton.setEnabled(true);
        debugButton.setActionCommand("debug");
        debugButton.addActionListener(this);
        
        clearButton = new JButton("Clear Panel");
        clearButton.setEnabled(true);
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this);
        
        generateButton = new JButton("Generate OWL-S");
        generateButton.setEnabled(false);
        generateButton.setActionCommand("generate");
        generateButton.addActionListener(this);

        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(clearButton);
//        buttonPanel.add(Box.createHorizontalStrut(10));
//        buttonPanel.add(debugButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(generateButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
//        buttonPanel.add(closeButton);

        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.add(servicePanel);
        detailsPanel.add(nsPanel);
        detailsPanel.add(inputsPanel);
        detailsPanel.add(outputsPanel);

        qnames.setMapping("soapEnc", WSDLConsts.soapEnc + "#");
        updateNS();
        
        previousWSDLDefinitions = new HashMap();
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("browse"))
            doBrowse();
        else if(e.getActionCommand().equals("load"))
            doLoad();
        else if(e.getActionCommand().equals("load_existing"))
            doLoadExistingWSDL();
        else if(e.getActionCommand().equals("clear"))
            doClear();
        else if(e.getActionCommand().equals("debug"))
            doDebug();
        else if(e.getActionCommand().equals("advanced"))
            showAdvanced();
        else if(e.getActionCommand().equals("addNS"))
            addNS();
        else if(e.getActionCommand().equals("removeNS"))
            removeNS();
        else if(e.getActionCommand().equals("generate"))
            doGenerate();
        else if(e.getActionCommand().equals("close")) {
            Window window = SwingUtilities.getWindowAncestor( this );
            if( window != null )
                window.dispose();
            else
                System.exit( 0 );
        }
        else if(e.getActionCommand().equals("load"))
            doLoad();
    }

    JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setVerticalAlignment(SwingConstants.TOP);

        int labelWidth = 100;
        label.setPreferredSize(new Dimension(labelWidth, label.getPreferredSize().height));
        label.setMaximumSize(new Dimension(labelWidth, label.getMaximumSize().height));
        label.setMinimumSize(new Dimension(labelWidth, label.getMinimumSize().height));

        return label;
    }

    public void addNS() {
        JDialog info = new JDialog((JFrame) null, "Add namespace", true);

        JTextField t1 = new JTextField(50);
        JTextField t2 = new JTextField(5);
        JLabel[] labels = {new JLabel("Enter URL: "), new JLabel("Abbreviation:")};
        JComponent[] textFields = {t1, t2};
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComponent c = (JComponent) e.getSource();
                Window w = (Window) c.getTopLevelAncestor();
                w.dispose();
            }
        });
        info.getContentPane().setLayout(new BoxLayout(info.getContentPane(), BoxLayout.Y_AXIS));
        info.getContentPane().add(createTableLayout(labels, textFields));
        info.getContentPane().add(ok);
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.pack();
        info.setResizable(false);
        SwingUtils.centerFrame(info);
        info.setVisible(true);

        System.out.println(t1.getText() + " " + t2.getText());

        String uri = t1.getText();
        String prefix = t2.getText();

        qnames.setMapping(prefix, uri);

        updateNS();
    }

    public void removeNS() {
        int row = nsTable.getSelectedRow();

        if(row == -1) {
            JOptionPane.showMessageDialog(
                null,
                "Please first select an entry, then click remove!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String prefix = (String) nsTable.getModel().getValueAt(row, 0);

        qnames.removePrefix(prefix);

        updateNS();
    }

    private JPanel createTableLayout(JComponent[] labels, JComponent[] textFields) {
        JPanel textControlsPane = new JPanel();
        GridBagLayout gridbag 	= new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        textControlsPane.setLayout(gridbag);

        c.anchor = GridBagConstraints.WEST;
        int numLabels = labels.length;

        for (int i = 0; i < numLabels; i++) {
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;                       //reset to default
            c.insets = new Insets(2,2,2,2);
            gridbag.setConstraints(labels[i], c);
            textControlsPane.add(labels[i]);

            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            if(i == numLabels - 1)
                c.gridheight = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.insets = new Insets(2,2,2,2);
            gridbag.setConstraints(textFields[i], c);
            textControlsPane.add(textFields[i]);
        }

        return textControlsPane;
    }

    /**
     * Load existing or previous built service description into WSDL2OWL-S Converter
     * -- broken! GUI work necessary --
     */
    public void doLoadExistingWSDL() {                
        final String url = wsdlUriComboBox.getSelectedItem().toString().replaceAll(" ","%20");
        
        //EX. final String url = "http://www.tilisoft.com/ws/LocInfo/ZipCode.asmx?WSDL";
        try {
            WSDLService s = null;
            if(this.previousWSDLDefinitions.containsKey(url)) {
                s = new WSDLService((javax.wsdl.Definition)this.previousWSDLDefinitions.get(url), URI.create(url));
                this.LOADGENERATED_FLAG = true;
                
                int idx1 = url.lastIndexOf("/");
                int idx2 = url.lastIndexOf(".");                
                serviceNameField.setText(url.substring(idx1+1,idx2));
            }
            else {
                s = WSDLService.createService(url);
                this.LOADGENERATED_FLAG = false;
            }
            
            //List ops = s.getOperations();
            //opList.setListData(ops.toArray());
            
            this.doClear();
            
            this.ops.addAll( s.getOperations() );
            for(ListIterator it=this.ops.listIterator(); it.hasNext(); ) {
                ((DefaultComboBoxModel)this.opComboBox.getModel()).addElement(it.next());
            }
        }
        catch(final Exception e) {
            //opList.setListData(new Vector());
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(
                            null,
                            "Cannot load service description from file\n " + url + "\n" + e,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    /**
     * Build WSDL and load service description into WSDL2OWL-S Converter
     */
    public void doLoad() 
    {   
        this.LOADGENERATED_FLAG = true;
        
        if(this.generateButton.isEnabled()) {
            this.doClear();
        }
        
        XsdSchemaGenerator xsdgen = RuntimeModel.getInstance().getProject().buildXsdSchemaGenerator();
        
        try {
            WSDLService s = null;
            if( RuntimeModel.getInstance().getSelectedService() != null )
            {
                AbstractService aService = RuntimeModel.getInstance().getSelectedService();
                this.url = aService.getID();
                
                System.out.println("Service loaded, build wsdl defintion");
                javax.wsdl.Definition def = WSDLBuilder.getInstance().buildDefinition(aService, xsdgen);
                
                if(def==null) {                    
                    throw(new Exception("WSDL definition null"));
                }
                                                
                int index = aService.getBase().lastIndexOf(".");
                String wsdldoc = aService.getBase().substring(0,index)+".wsdl";
                if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_TNS").equals("yes")) {
                    String tns_basepath = OWLS2WSDLSettings.getInstance().getProperty("TNS_BASEPATH");
                    index = aService.getLocalFilename().lastIndexOf(".");
                    String localFilename = aService.getLocalFilename().substring(0, index)+".wsdl";
                    wsdldoc = tns_basepath+localFilename;
                }
                
                if(OWLS2WSDLSettings.getInstance().getProperty("CHANGE_WSDLPATH").equals("yes")) {
                    String wsdl_basepath = OWLS2WSDLSettings.getInstance().getProperty("WSDLPATH");
                    index = aService.getLocalFilename().lastIndexOf(".");
                    String localFilename = aService.getLocalFilename().substring(0, index)+".wsdl";
                    wsdldoc = wsdl_basepath+localFilename;
                }                                
                
                serviceNameField.setText(aService.getName());
                                
                s = new WSDLService(def, URI.create(def.getTargetNamespace()));
                                                
                boolean INSERT_NEW_ENTRY = true;
                for(int i=0; i<wsdlUriComboBox.getItemCount(); i++) {
                    if( wsdlUriComboBox.getItemAt(i).equals(wsdldoc) ) {
                        INSERT_NEW_ENTRY = false;
                    }
                }
                if(INSERT_NEW_ENTRY) {
                    wsdlUriComboBox.addItem(wsdldoc);
                    wsdlUriComboBox.setSelectedIndex(wsdlUriComboBox.getItemCount()-1);
                    this.previousWSDLDefinitions.put(wsdldoc, def);                    
                }
            }
            else {
                s = WSDLService.createService(this.url);
            }
            
            this.ops.addAll( s.getOperations() );
            for(ListIterator it=this.ops.listIterator(); it.hasNext(); ) {
                ((DefaultComboBoxModel)this.opComboBox.getModel()).addElement(it.next());
            }
                    
            System.out.println("LOAD and CREATE WSDLService");
            //opList.setListData(ops.toArray());
        }
        catch(final Exception e) {
            //opList.setListData(new Vector());
            e.printStackTrace();
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(
                            null,
                            "Cannot load service description from file\n " + url + "\n" + e,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    /**
     * To load next service into WSDL2OWL-S Converter we have to clear 
     * all previous service entries.
     */
    public void doClear() {
        this.ops.clear();
        ((DefaultComboBoxModel)this.opComboBox.getModel()).removeAllElements();
        this.opComboBox.revalidate();
        nameSpaceField.setText("");
        serviceNameField.setText("");
        textDescription.setText("");
        
        if(inputTable.getModel()!=null) {
            int rowCount = ((DefaultTableModel)inputTable.getModel()).getRowCount();
            for(int i=0; i<rowCount; i++) {
                ((DefaultTableModel)inputTable.getModel()).removeRow(i);
            }
            inputTable.revalidate();
        }
        
        if(outputTable.getModel()!=null) {
            int rowCount = ((DefaultTableModel)outputTable.getModel()).getRowCount();
            for(int i=0; i<rowCount; i++) {
                ((DefaultTableModel)outputTable.getModel()).removeRow(i);
            }
            outputTable.revalidate();
        }
        qnames.getPrefixSet().clear();
        qnames = new QNameProvider();
        qnames.setMapping("soapEnc", WSDLConsts.soapEnc + "#");
        updateNS();
        
        generateButton.setEnabled(false);
    }
    
    void doDebug() {
        XsdSchemaGenerator xsdgen = RuntimeModel.getInstance().getProject().buildXsdSchemaGenerator();
        try {
            if( RuntimeModel.getInstance().getSelectedService() != null ) {                
                AbstractService aService = RuntimeModel.getInstance().getSelectedService();
                this.url = aService.getID();                
                //System.out.println("Service loaded, build wsdl defintion");
                javax.wsdl.Definition wsdlDef = WSDLBuilder.getInstance().buildDefinition(aService, xsdgen);
                if(wsdlDef==null) {                    
                    throw(new Exception("WSDL definition null"));
                }                
                
                WsdlGroundingBuilder groundingBuilder = new WsdlGroundingBuilder();
                WSDLTranslator t = groundingBuilder.translate(wsdlDef, aService);
                t.writeOWLS(System.out);                
                                
                String applPath = OWLS2WSDLSettings.getInstance().getProperty("APPL_PATH");                
                File file = new File(applPath+File.separator+aService.getLocalFilename());
                System.out.println("[WSDL2OWL-S] export to "+file.getAbsolutePath());
                
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                t.writeOWLS(fos);
                fos.close();
                System.out.println("Saved to " + file.toURI());
                
                System.out.println("[i] starting owl-s validator");
                MyOWLSValidator validator = new MyOWLSValidator();
                try {
                    OWLSValidatorReport report = validator.validate(file.toURI());
                    report.print(System.out);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }     
    }
    
    void doBrowse() {
        JFileChooser fc = new JFileChooser();

        //In response to a button click:
        int returnVal = fc.showOpenDialog(null);

        if(returnVal != JFileChooser.APPROVE_OPTION)
            return;

        File file =	fc.getSelectedFile();
        if(!file.exists()) {
            JOptionPane.showMessageDialog(
                null,
                file.getAbsolutePath() + "does not exist!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        else {
//            try {
//                urls.setSelectedItem(file.toURL().toExternalForm());
//            } catch (MalformedURLException e) {
//                JOptionPane.showMessageDialog(
//                        null,
//                        "Not a valid file path " + file.getAbsolutePath() + "\n" + e,
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//            }
        }
    }

    void doSelect() {
        //WSDLOperation op = (WSDLOperation) opList.getSelectedValue();        
        
        WSDLOperation op = (WSDLOperation) this.opComboBox.getSelectedItem();

        if(op == null) {
//			fileNameField.setText("");
            nameSpaceField.setText("");
            serviceNameField.setText("");
            textDescription.setText("");

            addParams(new Vector(), inputTable);
            addParams(new Vector(), outputTable);

            generateButton.setEnabled(false);
        }
        else {
            nameSpaceField.setText("http://www.example.org/service.owl");
            
            if(!this.LOADGENERATED_FLAG) {
                serviceNameField.setText(op.getName());
            }
            else {
                // serviceNameField already set during load
            }
            
            //wsdlUriField.setText(op.getService().getFileURI().toString());            
            
            if(op.getDocumentation() == null) {
                if(RuntimeModel.getInstance().getSelectedService() != null) {                
                    textDescription.setText("Auto generated from "+RuntimeModel.getInstance().getSelectedService().toString());
                }
                else {
                    textDescription.setText("Auto generated from " + op.getService().getFileURI());
                }
            }
            else {
                textDescription.setText(op.getDocumentation());
            }
            textDescription.setCaretPosition(0);

            //
            // OWL WSDL MAPPING
            //
            if(RuntimeModel.getInstance().getSelectedService() != null) 
            {
                //String basepath =  OWLS2WSDLSettings.getInstance().getProperty("EXPORT_BASE");                
                //nameSpaceField.setText(basepath+RuntimeModel.getInstance().getSelectedService().getLocalFilename());
                //obsolete
                
                nameSpaceField.setText(RuntimeModel.getInstance().getSelectedService().getBase());
                Vector importedOntList = RuntimeModel.getInstance().getSelectedService().getImportedOWLFiles(true);

                int i=0;
                for(Iterator it=importedOntList.iterator(); it.hasNext(); ) {
                    String path = it.next().toString();
                    System.out.println("NameSpaces: "+path);                
                    qnames.setMapping("ns"+Integer.toString(i), path+"#");
                    i++;
                }
            }            
            
            addParams(op.getInputs(), inputTable);
            addParams(op.getOutputs(), outputTable);

            generateButton.setEnabled(true);
        }
        
        updateNS();
    }
    

    void prepareAdvanced() {
        advanced = new JDialog((JFrame)null, "Advanced Settings", true);

        JPanel localNamePanel = new JPanel();

        prefixButton = new JRadioButton("Use a prefix to genearate local names");
        prefixButton.setSelected(true);

        manualButton = new JRadioButton("Manually enter each local name");

        ButtonGroup group = new ButtonGroup();
        group.add(prefixButton);
        group.add(manualButton);

        localNamePanel.setBorder(BorderFactory.createTitledBorder("Local name settings for URI's"));
        localNamePanel.setLayout(new BoxLayout(localNamePanel, BoxLayout.Y_AXIS));
        localNamePanel.add(prefixButton);
        localNamePanel.add(prefixText);
        localNamePanel.add(createTableLayout(
            new JComponent[] {new JLabel("Prefix")},
            new JComponent[] {prefixText}));
        localNamePanel.add(manualButton);
        localNamePanel.add(createTableLayout(
            new JComponent[] {new JLabel("Service"),
                              new JLabel("Profile"),
                              new JLabel("Process"),
                              new JLabel("Grounding"),
                             },
            new JComponent[] {serviceText,profileText,processText,groundingText}));


        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(5));

        advanced.getContentPane().add(localNamePanel, "Center");
        advanced.getContentPane().add(buttonPanel, "South");

        advanced.pack();
        advanced.setResizable(false);
    }

    void showAdvanced() {
        SwingUtils.centerFrame(advanced);
        advanced.setVisible(true);
    }


    private void addParams(Vector params, JTable table) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for(Iterator i = params.iterator(); i.hasNext(); ) {
            String[] row = new String[columnNames.length];
            WSDLParameter p = (WSDLParameter) i.next();
            QName paramType = (p.getType() == null)
                ? new QName(WSDLConsts.xsdURI, "any")
                : p.getType();
            String wsdlType = paramType.getNamespaceURI() + "#" + paramType.getLocalPart();

            // By default use owl:Thing as param type
            String type = OWL.Thing.toString();
            
            if(paramType.getNamespaceURI().equals(WSDLConsts.soapEnc) ||
              (paramType.getNamespaceURI().equals(WSDLConsts.xsdURI) &&
               !paramType.getLocalPart().equals("any"))) {                
                type = XSD.ns + paramType.getLocalPart();
                row[3] = qnames.shortForm(type);
            }
            else {            
                //
                // WSDL OWL MAPPING
                //
                String owlTypeString = null;
                System.out.println("WSDLParameter Name: "+p.getName());
                System.out.println("WSDLParameter Type: Ns:"+p.getType().getNamespaceURI()+" Prefix:"+p.getType().getPrefix()+" Local:"+p.getType().getLocalPart());

                if(RuntimeModel.getInstance().getSelectedService() != null) {
                    AbstractService aService = RuntimeModel.getInstance().getSelectedService();
                    type = aService.getParameterType( URIUtils.getLocalName(p.getName()) );
                } 
            }
            
            System.out.println("URISET: "+qnames.getURISet().size());
            for(Iterator it=qnames.getURISet().iterator(); it.hasNext(); ) {
                System.out.println("URISET: "+it.next().toString());
            }
            System.out.println("PREFIXSET: "+qnames.getPrefixSet().size());
            
            row[0] = URIUtils.getLocalName(p.getName());
            row[1] = qnames.shortForm(wsdlType);
            row[2] = row[0];
            row[3] = qnames.shortForm(type);
            //row[4] = "None";
            model.addRow(row);
        }

        table.setModel(model);
    }

    void doGenerate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(OWLS2WSDLSettings.getInstance().getProperty("EXPORT_PATH")));
        
//        String serviceName = serviceNameField.getText().trim();
//        String name = serviceName.replaceAll(" ", " _");                
//        String fileName =
//            fileChooser.getCurrentDirectory().getAbsolutePath() +
//            File.separator +
//            name +
//            ".owl";
        
        String fileName = RuntimeModel.getInstance().getSelectedService().getLocalFilename();
        
        fileChooser.setSelectedFile(new File(fileName));
        
        int retVal = fileChooser.showSaveDialog(this);
        if(retVal != JFileChooser.APPROVE_OPTION)
            return;

        File file = fileChooser.getSelectedFile();

        if(file.exists()) {
            int option =
                JOptionPane.showConfirmDialog(
                    null,
                    file.getAbsolutePath() + " already exists.\n" +
                    "Do you want to replace it?",
                    "Save File",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(option == JOptionPane.NO_OPTION)
                return;
        }

        System.out.println("DEBUG ==================================== ");
        RuntimeModel.getInstance().getSelectedService().printInfo();
        System.out.println("DEBUG ==================================== ");
        //System.out.println("WSDLOperation  : "+opList.getSelectedValue());
        System.out.println("WSDLOperation  : "+opComboBox.getSelectedItem());
        System.out.println("NameSpaceField : "+nameSpaceField.getText());
        System.out.println("ServiceName    : "+serviceNameField.getText());
        System.out.println("ServiceDescr   : "+textDescription.getText());
        System.out.println("DEBUG END ================================ ");
                
        //WSDLOperation op = (WSDLOperation) opList.getSelectedValue();
        WSDLOperation op = (WSDLOperation) opComboBox.getSelectedItem();
        
        WSDLTranslator t = new WSDLTranslator(
                op,
                URI.create(nameSpaceField.getText()),
                RuntimeModel.getInstance().getSelectedService().getReformatedServiceId4Translator(),
                URI.create(wsdlUriComboBox.getSelectedItem().toString()));
        
        t.setServiceName(serviceNameField.getText());
        t.setTextDescription(textDescription.getText());
        
        //
        // import own concepts
        //
        try {
            if(RuntimeModel.getInstance().getSelectedService() != null) {
                Vector importedOntList = RuntimeModel.getInstance().getSelectedService().getImportedOWLFiles(true);
                for(Iterator it=importedOntList.iterator(); it.hasNext(); ) {
                    String path = it.next().toString();
                    System.out.println("ADD IMPORTED.ONT: "+path);
                    //t.addImportEntry(OWLFactory.createOntology(URI.create(path)));
                    t.addImportEntry(path);
                }
            }
        }
        catch(java.net.URISyntaxException uriSE) {
            System.out.println("URISyntaxException: "+uriSE);
        }
        catch(java.io.FileNotFoundException fnfE) {
            System.out.println("FileNotFoundException: "+fnfE);
        }
        
        Set usedNames = new HashSet();
        
        TableModel inputs = inputTable.getModel();
        for(int i = 0; i < inputs.getRowCount(); i++) {
            WSDLParameter param = op.getInput(i);
            String paramName = (String) inputs.getValueAt(i, 2);
            String paramType = (String) inputs.getValueAt(i, 3);
            String xsltTransformation = (String) inputs.getValueAt(i, 4);
            
            System.out.println("paramName: "+paramName);
            System.out.println("paramType: "+paramType);
            System.out.println("XSLT     : "+xsltTransformation);
            
            String prefix = paramName;
            for( int count = 1; usedNames.contains( paramName ); count++ ) {
                paramName = prefix + count;
                System.out.println("FOR: paramName: "+paramName);
            }
            usedNames.add( paramName );
            
            URI paramTypeURI;
            try {
                paramType = qnames.longForm(paramType);
                paramTypeURI = new URI(paramType);
            } catch(IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            } catch(URISyntaxException e) {
                JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            t.addInput(param, paramName, paramTypeURI, xsltTransformation);
        }

        TableModel outputs = outputTable.getModel();
        for(int i = 0; i < outputs.getRowCount(); i++) {
            WSDLParameter param = op.getOutput(i);
            String paramName = (String) outputs.getValueAt(i, 2);
            String paramType = (String) outputs.getValueAt(i, 3);
            String xsltTransformation = (String) outputs.getValueAt(i, 4);

            String prefix = paramName;
            for( int count = 1; usedNames.contains( paramName ); count++ )
                paramName = prefix + count;
            
            usedNames.add( paramName );

            URI paramTypeURI;
            try {
                paramType = qnames.longForm(paramType);
                paramTypeURI = new URI(paramType);
            } catch(IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            } catch(URISyntaxException e) {
                JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            t.addOutput(param, paramName, paramTypeURI, xsltTransformation);
        }        
        
        try {            
            FileOutputStream fos = new FileOutputStream(file);
            t.writeOWLS(fos);
            fos.close();
            System.out.println("Saved to " + file.toURI());
        } catch(Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                null,
                "Cannot create OWL-S file!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

//        OWLSValidator validator = new OWLSValidator();
//        boolean valid = false;
//        try {
//            valid = validator.validate(file.toURI().toString());
//            if( valid ) {
//                OWLKnowledgeBase kb = OWLFactory.createKB();
//                kb.setReasoner("Pellet");
//                kb.getReader().getCache().setLocalCacheDirectory( "cache" );
//                kb.read(file.toURI());
//                valid = kb.isConsistent();
//            }
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//
//        if(!valid)
//            JOptionPane.showMessageDialog(
//                null,
//                "Saved file " + file.getAbsolutePath() + " is not valid",
//                "Error",
//                JOptionPane.ERROR_MESSAGE);
//        else {
//            JOptionPane.showMessageDialog(
//                null,
//                "Service " + file.getAbsolutePath() + " was saved succesfully" ,
//                "Success",
//                JOptionPane.INFORMATION_MESSAGE);
//            
//            savedServices.add( file );
//        }
    }

    void updateNS() {
        DefaultTableModel model = new DefaultTableModel(nsColumnNames, 0);

        Iterator i = qnames.getPrefixSet().iterator();
        while(i.hasNext()) {
            String prefix = (String) i.next();
            String uri = qnames.getURI(prefix);

            model.addRow(new String[] {prefix, uri});
        }

        nsTable.setModel(model);
        nsTable.getColumnModel().getColumn(0).setMaxWidth(150);
        nsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    public static void main(String[] args) throws Exception {
        JFrame test = new JFrame("WSDL2OWL-S Converter");

        test.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        test.setSize(800, 600);
        SwingUtils.centerFrame(test);
        test.getContentPane().add(new WSDL2OWLSPanel());
        test.setVisible(true);
    }
}