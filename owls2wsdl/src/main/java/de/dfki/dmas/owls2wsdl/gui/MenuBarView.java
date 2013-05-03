/*
 * MenuBarView.java
 *
 * Created on 19. November 2006, 19:40
 * Abgeleitet von JGoodies Karsten Lentzsch
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

import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

/**
 * Builds the menu bar and pull-down menus in the Simple Looks Demo. 
 * Demonstrates and tests different multi-platform issues.<p>
 * 
 * This class provides a couple of factory methods that create
 * menu items, check box menu items, and radio button menu items.
 * The full JGoodies Looks Demo overrides these methods to vend
 * components from the JGoodies UI framework that better handle
 * different platforms.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.3 $
 */
public class MenuBarView {
    
    private static final String HTML_TEXT = 
        "<html><b>Bold</b>, <i>Italics</i>, <tt>Typewriter</tt></html>";
    
    private GUIActionListener actionListener;
    
    private JMenuItem switch2ServiceDetailsItem;
    private JMenuItem switch2DatatypeDetailsItem;
    private JMenuItem switch2ProjectDetailsItem;
    
    final URL importMenuIconURL  = ToolBar.class.getResource( "/images/16x16etool/import_wiz.gif" );
    final URL exportMenuIconURL  = ToolBar.class.getResource( "/images/16x16etool/export_wiz.gif" );
    final URL saveMenuIconURL    = ToolBar.class.getResource( "/images/16x16etool/save_edit.gif");
    final URL saveAllMenuIconURL = ToolBar.class.getResource( "/images/16x16etool/saveall_edit.gif");        
    final URL loadAdd2KBIconURL  = ToolBar.class.getResource( "/images/16x16/addrepo_rep.gif");                
    final URL helpMenuIconURL    = ToolBar.class.getResource( "/images/16x16etool/help_contents.gif" );        
    final URL resDocPropURL      = ToolBar.class.getResource( "/images/16x16/actions/document-properties.png");
    final URL resExitURL         = ToolBar.class.getResource( "/images/16x16/actions/system-log-out.png");
    
    //getClass().getClassLoader().getResource
    final Icon importMenuIcon  = new ImageIcon( importMenuIconURL );
    final Icon exportMenuIcon  = new ImageIcon( exportMenuIconURL );
    final Icon saveMenuIcon    = new ImageIcon( saveMenuIconURL );
    final Icon saveAllMenuIcon = new ImageIcon( saveAllMenuIconURL );
    final Icon loadAdd2KBIcon  = new ImageIcon( loadAdd2KBIconURL );        
    final Icon helpMenuIcon    = new ImageIcon( helpMenuIconURL );
    final Icon iconProperties  = new ImageIcon( resDocPropURL );
    final Icon iconExit        = new ImageIcon( resExitURL );
    
    /**
     * Builds, configures, and returns the menubar. Requests HeaderStyle, 
     * look-specific BorderStyles, and Plastic 3D hint from Launcher.
     */
    JMenuBar buildMenuBar(Settings settings, GUIActionListener actionListener) 
    {
            this.actionListener = actionListener;

            JMenuBar bar = new JMenuBar();
            bar.putClientProperty(Options.HEADER_STYLE_KEY, 
                                                      settings.getMenuBarHeaderStyle());
            bar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY, 
                                                      settings.getMenuBarPlasticBorderStyle());
            bar.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY, 
                                                      settings.getMenuBarWindowsBorderStyle());
            bar.putClientProperty(PlasticLookAndFeel.IS_3D_KEY,
                                                      settings.getMenuBar3DHint());

            bar.add(buildFileMenu());
            bar.add(buildEditMenu());
            bar.add(buildViewMenu());
            bar.add(buildExtrasMenu());
            bar.add(buildHelpMenu());
            return bar;
    }
	

    /**
     * Builds and returns the file menu.
     */
    private JMenu buildFileMenu() 
    {
        JMenuItem item;

        //JMenu menu = createMenu("File", 'F');
        JMenu menu = createMenu(
                ResourceManager.getString("menubar.file"),
                ResourceManager.getString("menubar.file_mm").charAt(0));
                
        item = createMenuItem(
                ResourceManager.getString("project.new"),
                readImageIcon("images/16x16/actions/document-new.png"),
                ResourceManager.getString("project.new_mm").charAt(0),
                KeyStroke.getKeyStroke("ctrl shift N"));
        item.setActionCommand(GUIActionListener.NEW_PROJECT);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("project.open"),
                readImageIcon("images/16x16/actions/document-open.png"),
                ResourceManager.getString("project.open_mm").charAt(0),
                KeyStroke.getKeyStroke("ctrl shift O"));        
        item.setActionCommand(GUIActionListener.LOAD_PROJECT);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("project.save"),
                readImageIcon("images/16x16/actions/document-save-as.png"),
                ResourceManager.getString("project.save_mm").charAt(0),
                KeyStroke.getKeyStroke("ctrl shift S"));
        item.setActionCommand(GUIActionListener.SAVE_PROJECT);
        menu.add(item);                
        
        // ---------------------------------------------------------------------
/*        menu.addSeparator();
        
        item = createMenuItem("Load persistent Services", loadAdd2KBIcon);
        item.setActionCommand(GUIActionListener.LOAD_SERVICE_INFORMATION);
        menu.add(item);        
        
        item = createMenuItem("Save Services as XML", saveMenuIcon);
        item.setActionCommand(GUIActionListener.SAVE_SERVICE_INFORMATION); 
        menu.add(item); 
*/
        // ---------------------------------------------------------------------
        menu.addSeparator();
        
        item = createMenuItem(
                ResourceManager.getString("project.types.load"),
                loadAdd2KBIcon);
        item.setActionCommand(GUIActionListener.LOAD_TYPE_INFORMATION);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("project.types.save"),
                saveMenuIcon);
        item.setActionCommand(GUIActionListener.SAVE_TYPE_INFORMATION); 
        menu.add(item);
        
        // ---------------------------------------------------------------------
        /*
        menu.addSeparator();
        
        item = createMenuItem("Save selected output", saveMenuIcon);
        item.setActionCommand(GUIActionListener.SAVE_OUTPUT_PANEL_SEL);
        menu.add(item);
        
        item = createMenuItem("Save all outputs", saveAllMenuIcon);
        item.setActionCommand(GUIActionListener.SAVE_OUTPUT_PANEL);
        menu.add(item);
        */
        // ---------------------------------------------------------------------
        menu.addSeparator();
        
        item = createMenuItem(
                ResourceManager.getString("project.types.parse.file"),
                readImageIcon("images/16x16/elements_obj.gif"));
        item.setActionCommand(GUIActionListener.IMPORT_ONTOLOGY_OWL_FILE);
        menu.add(item);

        item = createMenuItem(
                ResourceManager.getString("project.types.parse.url"),                
                readImageIcon("images/16x16/elements_obj.gif"));
        item.setActionCommand(GUIActionListener.IMPORT_ONTOLOGY_URL);
        menu.add(item);
        
        // ---------------------------------------------------------------------
        menu.addSeparator();
        
        item = createMenuItem(
                ResourceManager.getString("project.services.parse.file"),
                readImageIcon("images/16x16/elements_obj-s.png"));
        item.setActionCommand(GUIActionListener.LOAD_OWLS_FILES);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("project.services.parse.url"),
                readImageIcon("images/16x16/elements_obj-s.png"));
        item.setActionCommand(GUIActionListener.LOAD_OWLS_URL);
        menu.add(item);
        
        // ---------------------------------------------------------------------
        menu.addSeparator();
        
        if (!isQuitInOSMenu()) {
            item = createMenuItem(
                    ResourceManager.getString("closing.cmd"),
                    iconExit,
                    ResourceManager.getString("closing.cmd_mm").charAt(0));
            item.setActionCommand(GUIActionListener.CLOSE);
            menu.add(item);
        }
	return menu;
    }
    
    /**
     * Builds and returns the edit menu.
     */
    private JMenu buildEditMenu() 
    {        
        JMenu menu = createMenu(
                ResourceManager.getString("menubar.edit"),
                ResourceManager.getString("menubar.edit_mm").charAt(0));
        
        JMenuItem item;
        /*
        item = createMenuItem("Copy", 'C', KeyStroke.getKeyStroke("ctrl C"));
        menu.add(item);
        item = createMenuItem("Paste", 'P', KeyStroke.getKeyStroke("ctrl P"));
        menu.add(item);
              
        menu.addSeparator();
        */
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.types.remove.selected")
                );
        item.setActionCommand(GUIActionListener.REMOVE_SELECTED_DATATYPE);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.types.remove.all")
                );
        item.setActionCommand(GUIActionListener.REMOVE_ALL_DATATYPES);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.types.remove.unreferenced")
                );
        item.setActionCommand(GUIActionListener.REMOVE_UNREFERENCED_DATATYPES);
        menu.add(item);
        
        menu.addSeparator();
        
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.services.remove.selected")
                );
        item.setActionCommand(GUIActionListener.REMOVE_SELECTED_SERVICES);
        menu.add(item);       
        
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.services.remove.all")
                );
        item.setActionCommand(GUIActionListener.REMOVE_ALL_SERVICES);
        menu.add(item);
        
        menu.addSeparator();
        
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.services.select.all")
                );
        item.setActionCommand(GUIActionListener.SELECT_ALL_SERVICES);
        menu.add(item);

        menu.addSeparator();
        
        item = createMenuItem(
                ResourceManager.getString("menubar.edit.project.setname")
                );
        item.setActionCommand(GUIActionListener.SET_PROJECT_NAME);
        menu.add(item);
        
        return menu;
    }
    
    /**
     * Builds and returns the edit menu.
     */
    private JMenu buildViewMenu() 
    {        
        JMenu menu = createMenu(
                ResourceManager.getString("menubar.view"),
                ResourceManager.getString("menubar.view_mm").charAt(0));
        
        switch2DatatypeDetailsItem = createMenuItem(
                ResourceManager.getString("menubar.view.details.types")
                );
        switch2DatatypeDetailsItem.setActionCommand(GUIActionListener.SHOW_DATATYPEDETAILS);
        menu.add(switch2DatatypeDetailsItem);
        
        switch2ServiceDetailsItem = createMenuItem(
                ResourceManager.getString("menubar.view.details.services")
                );
        switch2ServiceDetailsItem.setActionCommand(GUIActionListener.SHOW_SERVICEDETAILS);
        menu.add(switch2ServiceDetailsItem);
                       
        menu.addSeparator();
        
        switch2ProjectDetailsItem = createMenuItem(
                ResourceManager.getString("menubar.view.details.project")
                );
        switch2ProjectDetailsItem.setActionCommand(GUIActionListener.SHOW_PROJECTDETAILS);
        menu.add(switch2ProjectDetailsItem);
        
        menu.addSeparator();
        
        JMenuItem item = createMenuItem(
                ResourceManager.getString("menubar.view.update")
                );
        item.setActionCommand(GUIActionListener.UPDATE_OVERVIEWS);
        menu.add(item);
        
        return menu;
    }

    /**
     * Builds and returns menu Extras
     */
    private JMenu buildExtrasMenu() 
    {
        JMenu menu = createMenu(
                ResourceManager.getString("menubar.extras"),
                ResourceManager.getString("menubar.extras_mm").charAt(0));
        JMenuItem item;

        item = createMenuItem(
                ResourceManager.getString("menubar.extras.client"),
                ResourceManager.getString("menubar.extras.client_mm").charAt(0),
                KeyStroke.getKeyStroke("ctrl shift C"));
        //item.setEnabled(true);
        item.addActionListener(this.actionListener);
        item.setActionCommand(GUIActionListener.WSDL2JAVA);
        menu.add(item);
                
//        item = createMenuItem("Determine service dependencies");
//        item.setActionCommand(GUIActionListener.DETERMINE_SERVICE_DEPENDENCY_TYPES);        
//        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("menubar.extras.export"),
                ResourceManager.getString("menubar.extras.export_mm").charAt(0),
                KeyStroke.getKeyStroke("ctrl shift E"));
        item.addActionListener(this.actionListener);
        item.setActionCommand(GUIActionListener.SHOW_WSDL_EXPORTFRAME);
        menu.add(item);
        
        item = createMenuItem(
                ResourceManager.getString("menubar.extras.properties"),
                iconProperties,
                ResourceManager.getString("menubar.extras.properties_mm").charAt(0),
                KeyStroke.getKeyStroke("ctrl shift P"));
        item.setActionCommand(GUIActionListener.SHOW_CONFIGURATIONFRAME);
        menu.add(item);
        
        return menu;
    }

    /**
     * Builds and and returns the help menu.
     */
    private JMenu buildHelpMenu() 
    {       
        JMenu menu = createMenu(
                ResourceManager.getString("menubar.help"),
                ResourceManager.getString("menubar.help_mm").charAt(0));
        JMenuItem item;        
        
        item = createMenuItem(
                ResourceManager.getString("menubar.help.contents"),
                readImageIcon("images/16x16etool/help_contents.gif"),
                ResourceManager.getString("menubar.help.contents_mm").charAt(0)
                );
        //item.addActionListener(this.actionListener);        
        item.setActionCommand(GUIActionListener.SHOW_HELP_CONTENTS);
        menu.add(item);
        
        if (!isAboutInOSMenu()) {
            menu.addSeparator();
            item = createMenuItem(
                    ResourceManager.getString("menubar.help.about"),
                    ResourceManager.getString("menubar.help.about_mm").charAt(0)
                    );
            item.addActionListener(this.actionListener);
            item.setActionCommand(GUIActionListener.SHOW_ABOUT);
            menu.add(item);
        }

        return menu;
    }
        
    // Factory Methods ********************************************************

    protected JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }
        
    
    protected JMenuItem createMenuItem(String text) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(this.actionListener);
        return menuItem;
    }
    
    
    protected JMenuItem createMenuItem(String text, char mnemonic) {
        return new JMenuItem(text, mnemonic);
    }
    
    
    protected JMenuItem createMenuItem(String text, char mnemonic, KeyStroke key) {
        JMenuItem menuItem = new JMenuItem(text, mnemonic);
        menuItem.setAccelerator(key);
        return menuItem;
    }
    
    
    protected JMenuItem createMenuItem(String text, Icon icon) {
        JMenuItem menuItem = new JMenuItem(text, icon);
        menuItem.addActionListener(this.actionListener);
        return menuItem;
    }
    
	
    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic) {
        JMenuItem menuItem = new JMenuItem(text, icon);
        menuItem.setMnemonic(mnemonic);
        menuItem.addActionListener(this.actionListener);
        return menuItem;
    }
    
    
    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic, KeyStroke key) {
        JMenuItem menuItem = createMenuItem(text, icon, mnemonic);
        menuItem.setAccelerator(key);
        return menuItem;
    }
    
    
    protected JRadioButtonMenuItem createRadioButtonMenuItem(String text, boolean selected) {
        return new JRadioButtonMenuItem(text, selected);
    }
    
    
    protected JCheckBoxMenuItem createCheckBoxMenuItem(String text, boolean selected) {
        return new JCheckBoxMenuItem(text, selected);
    }
    

    // Subclass will override the following methods ***************************
    
    /**
     * Checks and answers whether the quit action has been moved to an
     * operating system specific menu, e.g. the OS X application menu.
     *  
     * @return true if the quit action is in an OS-specific menu
     */
    protected boolean isQuitInOSMenu() {
        return false;
    }
    

    /**
     * Checks and answers whether the about action has been moved to an
     * operating system specific menu, e.g. the OS X application menu.
     *  
     * @return true if the about action is in an OS-specific menu
     */
    protected boolean isAboutInOSMenu() {
        return false;
    }
    
    
    // Higher Level Factory Methods *****************************************
    
	/**
	 * Creates and returns a JRadioButtonMenuItem
	 * with the given enablement and selection state.
	 */
	private JRadioButtonMenuItem createRadioItem(boolean enabled, boolean selected) {
		JRadioButtonMenuItem item = createRadioButtonMenuItem(
			getToggleLabel(enabled, selected),
			selected);
		item.setEnabled(enabled);
		item.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JRadioButtonMenuItem source = (JRadioButtonMenuItem) e.getSource();
				source.setText(getToggleLabel(source.isEnabled(), source.isSelected()));
			}
		});
		return item;
	}
	

	/**
	 * Creates and returns a JCheckBoxMenuItem
	 * with the given enablement and selection state.
	 */
	private JCheckBoxMenuItem createCheckItem(boolean enabled, boolean selected) {
		JCheckBoxMenuItem item = createCheckBoxMenuItem(
			getToggleLabel(enabled, selected),
			selected);
		item.setEnabled(enabled);
		item.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
				source.setText(getToggleLabel(source.isEnabled(), source.isSelected()));
			}
		});
		return item;
	}
	
	
	/**
	 *  Returns an appropriate label for the given enablement and selection state.
	 */
	protected String getToggleLabel(boolean enabled, boolean selected) {
		String prefix = enabled  ? "Enabled" : "Disabled";
		String suffix = selected ? "Selected" : "Deselected";
		return prefix + " and " + suffix;
	}
	
    
    // Helper Code ************************************************************
    
    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    private ImageIcon readImageIcon(String filename) {
        URL url = getClass().getClassLoader().getResource(filename);
        return new ImageIcon(url);
    }
    
    
    /**
     * Creates and returns a submenu labeled with the given text.
     */
    private JMenu createSubmenu(String text) {
        JMenu submenu = new JMenu(text);
        submenu.add(new JMenuItem("Item 1"));
        submenu.add(new JMenuItem("Item 2"));
        submenu.add(new JMenuItem("Item 3"));
        return submenu;
    }
    

}