/*
 * StatusBar.java
 *
 * Created on 19. November 2006, 18:15
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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Observer;
import org.mindswap.utils.SwingUtils;

/**
 *
 * @author Oliver Fourman
 */
public class StatusBar extends JPanel implements Observer {
    
    private JLabel selectedServiceLabel;
    private JLabel selectedServiceInformationLabel;
    private JLabel selectedDatatypeLabel;
    private JLabel depthStatus;
    private JLabel xsdStatus;
    private JLabel statusMsg;
       
    private final Font globalStatusBarFont = new Font("Dialog", Font.PLAIN, 11); 
    private final Font globalStatusBarFontBold = new Font("Dialog", Font.BOLD, 11); 
    
    /** Creates a new instance of StatusBar */
    public StatusBar() {
        RuntimeModel.getInstance().addObserver(this);
        
        this.setBorder(new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED), new EmptyBorder(0,2,0,2)));

        this.selectedServiceLabel = new JLabel("none");
        this.selectedServiceLabel.setFont(globalStatusBarFont);
        
        this.selectedServiceInformationLabel = new JLabel("");
        this.selectedServiceInformationLabel.setFont(globalStatusBarFont);
        
        this.selectedDatatypeLabel = new JLabel("none");
        this.selectedDatatypeLabel.setFont(globalStatusBarFont);
                
        this.depthStatus = new JLabel("0");
        this.xsdStatus = new JLabel();
        
        this.statusMsg = new JLabel("none");
        this.statusMsg.setFont(globalStatusBarFont);
        
        GridBagLayout gbl = new GridBagLayout();
        this.setLayout(gbl);
        {
            GridBagConstraints grid = new GridBagConstraints();
            
            grid.weightx = 0.5;
            grid.weighty = 0.0;
            
            grid.insets = new Insets(0, 2, 0, 2);
            grid.anchor = GridBagConstraints.WEST;
            grid.gridwidth = 1;
            grid.fill = GridBagConstraints.NONE;
            
            JLabel label1 = new JLabel(
                    ResourceManager.getString("statusbar.service.selected")
                    );
            label1.setFont(globalStatusBarFontBold);
            
            JLabel label2 = new JLabel(
                    ResourceManager.getString("statusbar.service.translatable")
                    );
            label2.setFont(globalStatusBarFontBold);
            
            JLabel label3 = new JLabel(
                    ResourceManager.getString("statusbar.type.selected")
                    );
            label3.setFont(globalStatusBarFontBold);

            JLabel label4 = new JLabel("XSD:");
            label4.setFont(globalStatusBarFontBold);
            
            JLabel label5 = new JLabel("Status:");
            label5.setFont(globalStatusBarFontBold);
            
            this.add(label1, grid);
            this.add(selectedServiceLabel, grid);
            this.add(label2, grid);
            this.add(selectedServiceInformationLabel, grid);
            this.add(label3, grid);
            this.add(selectedDatatypeLabel, grid);
            this.add(label4, grid);
            this.add(xsdStatus, grid);
            this.add(label5, grid);
            this.add(statusMsg, grid);
        }
    }
    
    public void update(java.util.Observable o, Object arg) 
    {
        System.out.println("[u] update StatusBar: "+o.toString());
        
        if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.PROPERTIES_CHANGED)) {
            int depth = RuntimeModel.getInstance().getProject().getElementDepth();
            this.depthStatus.setText(Integer.toString(depth));
            if(RuntimeModel.getInstance().getSelectedDatatype() != null) {
                this.xsdStatus.setText(RuntimeModel.getInstance().getSelectedDatatype().getResultingXsdType(depth));                
            }           
        }
        else if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SINGLE_SERVICE_SELECTED)) {
            // arg muss null sein, damit beim Umschalten Service / Datatype kein null Wert gelesen wird
            if(((RuntimeModel)o).getSelectedService() != null) {
                String servicename = ((RuntimeModel)o).getSelectedService().getName();
                this.selectedServiceLabel.setText(servicename);

                if(((RuntimeModel)o).getSelectedService().istranslatable()) {
                    this.selectedServiceInformationLabel.setForeground(Color.GREEN);
                    this.selectedServiceInformationLabel.setText("Yes");
                }
                else {
                    this.selectedServiceInformationLabel.setForeground(Color.RED);
                    this.selectedServiceInformationLabel.setText("No");
                }
            }
        }        
        else if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SINGLE_DATATYPE_SELECTED)) {
            if( ((RuntimeModel)o).getSelectedDatatype() != null ) {
                this.selectedDatatypeLabel.setText(((RuntimeModel)o).getSelectedDatatype().getLocalName());                
                int depth = RuntimeModel.getInstance().getProject().getElementDepth();                
                this.xsdStatus.setText(RuntimeModel.getInstance().getSelectedDatatype().getResultingXsdType(depth));
            }
        }
        else if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.DATATYPE_MODEL_CHANGED)) {
            if(RuntimeModel.getInstance().getSelectedDatatype()!=null) {
                this.selectedDatatypeLabel.setText(RuntimeModel.getInstance().getSelectedDatatype().getLocalName());
            }
            else {
                this.selectedDatatypeLabel.setText("none");
            }
        }
        else if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SERVICE_MODEL_CHANGED)) {
            if(RuntimeModel.getInstance().getSelectedService()!=null) {
                String servicename = RuntimeModel.getInstance().getSelectedService().getName();
                this.selectedServiceLabel.setText(servicename);
            }
            else {
                this.selectedServiceLabel.setText("none");
            }
        }
        
        if(arg!=null) {
            this.statusMsg.setText(arg.toString());
        }                
    }
    
    public static void main(String[] args) {
        
                
        JFrame test = new JFrame("TEST Statusbar");

        test.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        test.setSize(800, 600);
        SwingUtils.centerFrame(test);
        test.getContentPane().add(new StatusBar());
        test.pack();
        test.setVisible(true);
    }
    
}
