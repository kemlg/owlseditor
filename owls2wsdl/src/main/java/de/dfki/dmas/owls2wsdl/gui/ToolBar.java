/*
 * ToolBar.java
 *
 * Created on 19. November 2006, 18:21
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
import java.util.Observer;
import java.net.URL;

/**
 *
 * @author Oliver Fourman
 */
public class ToolBar extends JToolBar implements Observer {
    
    final URL resDocumentNew  = ToolBar.class.getResource( "/images/22x22/actions/document-new.png" );
    final URL resDocumentOpen = ToolBar.class.getResource( "/images/22x22/actions/document-open.png" );
    final URL resDocumentSave = ToolBar.class.getResource( "/images/22x22/actions/document-save.png" );
    final URL resTranslate    = ToolBar.class.getResource( "/images/22x22/actions/format-indent-more.png" );
    final URL resMindswapIcon = ToolBar.class.getResource( "/images/22x22/mindswap22x22.png" );                

    final Icon iconNew        = new ImageIcon( resDocumentNew );
    final Icon iconOpen       = new ImageIcon( resDocumentOpen );
    final Icon iconSave       = new ImageIcon( resDocumentSave );
    final Icon iconTranslate  = new ImageIcon( resTranslate );
    final Icon iconMindswap   = new ImageIcon( resMindswapIcon );
    
    /** Buttons for the main functions of this Application */
    private JButton 
                newProjectButton,
                loadProjectButton,
                saveProjectButton,
                generateXSDButton,
                generateWSDLButton,
                showWSDL2OWLSConverterButton,
                generateOWLSButton;
    
    private GUIActionListener actionListener;
    
    /** Creates a new instance of ToolBar */
    public ToolBar(GUIActionListener actionListener) {        
        super("OWLS2WSDL ToolBar", JToolBar.HORIZONTAL);
        
        this.actionListener = actionListener;
        
        RuntimeModel.getInstance().addObserver(this);        
        
        this.newProjectButton = new JButton(
                ResourceManager.getString("project.new"),
                iconNew);
        newProjectButton.addActionListener(this.actionListener);
        newProjectButton.setActionCommand(GUIActionListener.NEW_PROJECT);
        this.add(newProjectButton);
                        
        this.loadProjectButton = new JButton(
                ResourceManager.getString("project.open"),
                iconOpen);
        this.loadProjectButton.addActionListener(this.actionListener);
        this.loadProjectButton.setActionCommand(GUIActionListener.LOAD_PROJECT);
        this.add(this.loadProjectButton);
        
        this.saveProjectButton = new JButton(
                ResourceManager.getString("project.save"),
                iconSave);
        this.saveProjectButton.addActionListener(this.actionListener);
        this.saveProjectButton.setActionCommand(GUIActionListener.SAVE_PROJECT);
        this.add(this.saveProjectButton);
        
        this.addSeparator();
                
        generateWSDLButton = new JButton("OWLS2WSDL", iconTranslate);
        generateWSDLButton.setToolTipText(
                ResourceManager.getString("toolbar.owls2wsdl.tooltip"));
        generateWSDLButton.setEnabled(false);
        generateWSDLButton.addActionListener(this.actionListener);
        generateWSDLButton.setActionCommand(GUIActionListener.GENERATE_WSDL);
        this.add(generateWSDLButton);
        
        generateOWLSButton = new JButton("OWL-S", iconTranslate);
        generateOWLSButton.setToolTipText(
                ResourceManager.getString("toolbar.owls.tooltip"));
        generateOWLSButton.setEnabled(true);
        generateOWLSButton.addActionListener(this.actionListener);
        generateOWLSButton.setActionCommand(GUIActionListener.GENERATE_OWLS);
        this.add(generateOWLSButton);
        
        generateXSDButton = new JButton("OWL2XSD", iconTranslate);
        generateXSDButton.setToolTipText(
                ResourceManager.getString("toolbar.owl2xsd.tooltip"));
        generateXSDButton.setEnabled(false);
        generateXSDButton.addActionListener(this.actionListener);
        generateXSDButton.setActionCommand(GUIActionListener.GENERATE_XSD);
        this.add(generateXSDButton);          
        
        showWSDL2OWLSConverterButton = new JButton("WSDL2OWL-S Converter", iconMindswap);
        showWSDL2OWLSConverterButton.setToolTipText("Generate OWL-S by reengineering WSDL (OWL-S GUI)");
        showWSDL2OWLSConverterButton.setEnabled(true);
        showWSDL2OWLSConverterButton.addActionListener(this.actionListener);
        showWSDL2OWLSConverterButton.setActionCommand(GUIActionListener.SHOW_WSDL2OWLS_CONVERTER);
        this.add(showWSDL2OWLSConverterButton);
        
        this.initialeState();
    }
    
    private void deactivateAllButtons() {
        Component[] componentList = this.getComponents();
        for(int i=0; i<componentList.length; i++) {
            if(componentList[i].getClass().getName().equals("javax.swing.JButton")) {
                ((JButton)componentList[i]).setEnabled(false);
            }
        }
    }
    
    private void initialeState() {
        this.deactivateAllButtons();
        this.newProjectButton.setEnabled(true);
        this.loadProjectButton.setEnabled(true);
    }
    
    public void update(java.util.Observable o, Object arg) 
    {
        System.out.println("[u] ToolBar - GuiStatusModel");            
        RuntimeModel rtm = (RuntimeModel)o;
        String view_status = rtm.getStatus("VIEW");
        
        
        if(RuntimeModel.getInstance().getProject() == null) {
            this.initialeState();
        }
        else {
            
            this.saveProjectButton.setEnabled(true);
            
            if(rtm.getSelectedDatatype()!=null) {
                this.generateXSDButton.setEnabled(true);
            }
            else {
                this.generateXSDButton.setEnabled(false);
            }
            
            if(RuntimeModel.getInstance().getStatus("RUNTIME").equals(RuntimeModel.SINGLE_SERVICE_SELECTED)) {
                if(RuntimeModel.getInstance().getSelectedService().istranslatable()) {
                    this.generateWSDLButton.setEnabled(true);
                    this.showWSDL2OWLSConverterButton.setEnabled(true);  // WSDL2OWL-S Converter
                    this.generateOWLSButton.setEnabled(true);
                }
                else {
                    this.generateWSDLButton.setEnabled(false);
                    this.showWSDL2OWLSConverterButton.setEnabled(false);  // WSDL2OWL-S Converter
                    this.generateOWLSButton.setEnabled(false);
                }
            }
        }
    }    
}
