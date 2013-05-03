/*
 * ResourceManager.java
 *
 * Created on 19. November 2006, 17:59
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

import java.util.*;

/**
 * Mappt Namen von Buttons, Labels usw. auf Icons, 
 * Beschriftungen etc, die in Properties-Datei festgehalten sind.
 */
public final class ResourceManager
{
    /** Das Resource-Bundle, das die Daten aus der Property-Datei ausliest */
    private static PropertyResourceBundle resourceBundle = null;
    private static PropertyResourceBundle defaultBundle = null;

    /** 
     * Konstruktor zum Laden des Bundles
     *  @param baseName unser Basename
     */
    public final static void setBundle( String baseName, 
                                        String language, 
                                        String country)
    {
        System.out.println("[.] Trying to load Property-File:" + baseName + "_" + language + "_" + country + ".properties");
        resourceBundle = (PropertyResourceBundle)ResourceBundle.getBundle(baseName, new Locale(language, country), ClassLoader.getSystemClassLoader());
        System.out.println("[.] resourceBundle: "+resourceBundle.toString());
    }
    
    public final static void setDefaultBundle(String baseName, 
                                        String language, 
                                        String country)                  
    {
        System.out.println("[.] Trying to load default Property-File:" + baseName + "_" + language + "_" + country + ".properties");
        defaultBundle = (PropertyResourceBundle)ResourceBundle.getBundle(baseName, new Locale(language, country), ClassLoader.getSystemClassLoader());    
    }
    
    /** Gibt das Bundle zurück 
     *  @return das Bundle-Objekt
     */
    public final static PropertyResourceBundle getBundle() {
        if(resourceBundle == null) {
            setBundle(  "de.dfki.dmas.owls2wsdl.gui.OWLS2WSDL",
                        Locale.getDefault().getLanguage(), 
                        Locale.getDefault().getCountry() );
        }
        return resourceBundle;
    }
    
    public final static PropertyResourceBundle getDefault() {
        if(defaultBundle == null) {
            setBundle(  "de.dfki.dmas.owls2wsdl.gui.OWLS2WSDL",
                        Locale.getDefault().getLanguage(), 
                        Locale.getDefault().getCountry() );
        }
        return defaultBundle;
    }

    public final static Enumeration getKeys(){
        return getBundle().getKeys();
    }

    public final static Object handleGetObject(String key){
        return getBundle().handleGetObject(key);
    }

    public final static Locale getLocale(){
        return getBundle().getLocale();
    }

    public final static String[] getStringArray(String key){
        return getBundle().getStringArray(key);
    }

    public final static String getString(String key){
        if(getBundle().handleGetObject(key) == null) {
            System.out.println("[i] choosing default string for "+key);
            return getDefault().getString(key);
        }
        return getBundle().getString(key);
    }
    
    public final static void printResources() {
        Enumeration resources = getKeys();
        while(resources.hasMoreElements()) {
            String key = resources.nextElement().toString();
            System.out.println( key + " = (" + getBundle().getString(key) + ")" );
        }
    }
    
    public static void main(String args[]) {
        ResourceManager.setBundle("de.dfki.dmas.owls2wsdl.gui.OWLS2WSDL", "de", "DE");
        int i=0;
        Enumeration e = ResourceManager.getKeys();
        while(e.hasMoreElements()) {
            String key = e.nextElement().toString();
            System.out.println(i+": "+key+" - "+ResourceManager.getString(key));
            i++;
        }
    }
    
}