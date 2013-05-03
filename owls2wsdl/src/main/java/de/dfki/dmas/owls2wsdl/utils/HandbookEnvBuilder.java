/*
 * HandbookEnvBuilder.java
 *
 * Created on 5. Oktober 2007, 00:19
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

package de.dfki.dmas.owls2wsdl.utils;

import de.dfki.dmas.owls2wsdl.config.OWLS2WSDLSettings;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Vector;

/**
 * Helper class that synchronizes web pages SVN to local application directory.
 * Used as long there is no regular webpage with OWLS2WSDL handbook.
 * @author Oliver Fourman
 */
public class HandbookEnvBuilder {
    
    private URL hb_index;
    private URL hb_pics;
    private File handbookDir;
    private File handbookPicsDir;
    private File handbookIndex;
    
    /** 
     * Creates a new instance of HandbookEnvBuilder 
     * @param hb_urlString handbooks url (web address)
     * @param picsString comma seperated list of pictures for handbook
     */
    public HandbookEnvBuilder(String hb_urlString, String picsString) throws MalformedURLException, IOException {
        
        this.hb_index = new URL(hb_urlString+"index.html");
        this.hb_pics = new URL(hb_urlString+"pics/");
        String[] picNames = picsString.split(",");

        String applPath      = OWLS2WSDLSettings.getInstance().getProperty("APPL_PATH");
        this.handbookDir     = new File(applPath+File.separator+"handbook");
        this.handbookPicsDir = new File(applPath+File.separator+"handbook"+File.separator+"pics");
        this.handbookIndex   = new File(applPath+File.separator+"handbook"+File.separator+"index.html");
        
        System.out.println("[HandbookEnvBuilder] handbook directory: "+handbookDir);
        if(handbookDir.mkdir()) {
            System.out.println("[HandbookEnvBuilder] new handbook directory created");
        }
        if(handbookPicsDir.mkdir()) {
            System.out.println("[HandbookEnvBuilder] new pics directory for handbook created");
        }
        
//        BufferedReader in = new BufferedReader( new InputStreamReader(this.hb_index.openStream()));
//        FileWriter handbookFileWriter = new FileWriter(handbookIndex, false);
//
//        String inputLine;
//        //String buffer = "";
//
//        while ((inputLine = in.readLine()) != null) {                
//            //buffer += inputLine+"\n";
//            handbookFileWriter.write(inputLine+"\n");
//        }
//        in.close();
//        handbookFileWriter.close();
//        //System.out.println(buffer);
        
        download(this.hb_index.toString(), this.handbookIndex.getAbsolutePath());
        
        for(int i=0; i<picNames.length; i++) {
            //System.out.println(hb_urlString+picNames[i]);
            if(new File(this.handbookPicsDir+File.separator+picNames[i]).exists()) {
                System.out.println("[HandbookEnvBuilder] "+this.handbookPicsDir+File.separator+picNames[i]+" exits!");
            }
            else {
                download(this.hb_pics+picNames[i], this.handbookPicsDir+File.separator+picNames[i]);
            }
        }
        
    }
    
    private static void download(String address, String localFileName) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream  in = null;
        try {
                URL url = new URL(address);                
                out = new BufferedOutputStream(new FileOutputStream(localFileName));
                conn = url.openConnection();
                in = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int numRead;
                long numWritten = 0;
                while ((numRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, numRead);
                    numWritten += numRead;
                }
                System.out.println(localFileName + "\t" + numWritten);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) { in.close(); }
                if (out != null) { out.close(); }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    
    public URL getHandbookIndexURL() throws MalformedURLException {
        return this.handbookIndex.toURL();
    }
    
    public static void main(String[] args) 
    {        
        try {
            HandbookEnvBuilder heb = new HandbookEnvBuilder(
                    "http://owls2wsdl.googlecode.com/svn/trunk/handbook/",
                    "owls2wsdl.gif,owls2wsdl.jpg,owls2wsdl-logo.jpg,owl-mini.jpg");
            System.out.println("URL: "+heb.getHandbookIndexURL().toString());
        }
        catch(MalformedURLException murle) {
            System.err.println("MalformedURLException: "+murle.getMessage());
        }
        catch(IOException ioe) {
            ioe.getStackTrace();
        }
    }
}
