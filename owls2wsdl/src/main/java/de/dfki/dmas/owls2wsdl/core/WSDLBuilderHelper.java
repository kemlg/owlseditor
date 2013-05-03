/*
 * WSDLBuilderHelper.java
 *
 * Created on 25. August 2006, 15:35
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

package de.dfki.dmas.owls2wsdl.core;

/**
 *
 * @author Oliver Fourman
 */
public class WSDLBuilderHelper {
    
    /** Creates a new instance of WSDLBuilderHelper */
    public WSDLBuilderHelper() {
    }
    
    public String reformatOWLSSupportedByString(String supportedByString) 
    {        
        String servicename = supportedByString.toLowerCase();
        
        int i=0;
        int index;
        while(true) {
            index = servicename.indexOf("_",i);
            if(index == -1) {
                break;   // no more underlines found
            }
            else {                
//                char[] c = new char[1];
//                c[0] = servicename.charAt(index+1);
//                String cur = new String(c);                
//                cur = cur.toUpperCase();
                String cur = servicename.substring(index+1, index+2);                
                cur = cur.toUpperCase();
                String substring1 = servicename.substring(0,index+1);
                String substring2 = servicename.substring(index+2);
                //System.out.println("Index: "+index+" SubStr1: ("+substring1+") ("+cur+") SubStr2: ("+substring2+")");
                servicename = substring1 + cur + substring2;
            }
            i = index+1;
        }
        
        servicename = servicename.replace("_","");
        String cur = servicename.substring(0,1);
        cur = cur.toUpperCase();
        servicename = cur + servicename.substring(1);
        
        int len = servicename.length();
        if(servicename.substring(len-7,len).toLowerCase().equals("service")) {
            servicename = servicename.substring(0,len-7);
        }
               
        return servicename;
    }
    
    
    
    public String extractContext(String supportedByString) 
    {
        int index = supportedByString.lastIndexOf("_");
        System.out.println("INDEX: "+index);
        if(supportedByString.substring(index+1).toLowerCase().equals("service")) {
            return this.reformatOWLSSupportedByString(supportedByString.substring(0, index));
        }
        else {
            return this.reformatOWLSSupportedByString(supportedByString);
        }
    }
    
    public static void main(String[] args) {
        WSDLBuilderHelper helper = new WSDLBuilderHelper();
        //System.out.println("ServiceName: "+helper.reformatOWLSSupportedByString("BOOK_PRICE_SERVICE"));
        System.out.println("Kontext    : "+helper.extractContext("BOOK_PRICE_SERVICE"));
        System.out.println("ServiceName: "+helper.reformatOWLSSupportedByString("ZipCodeFinderService"));
        System.out.println("ServiceName: "+helper.reformatOWLSSupportedByString("_STUDENTINFORMATION_BLAH_BLUB_SERVICE"));
    }
    
}
