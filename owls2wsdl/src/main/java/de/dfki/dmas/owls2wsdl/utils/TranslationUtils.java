/*
 * AbstractServiceUtils.java
 *
 * Created on 2. Februar 2007, 23:33
 */

package de.dfki.dmas.owls2wsdl.utils;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

import de.dfki.dmas.owls2wsdl.core.AbstractService;

/**
 *
 * @author Oliver Fourman
 */
public class TranslationUtils {
        
    /**
     * Find common root directory of services.
     * @param list of AbstractService objects
     * @return common directory name
     * @author Oliver Fourman
     */
    public static String findCommonRoot(Vector directoryList) throws java.lang.ArrayIndexOutOfBoundsException
    {
        Vector buffer = new Vector();
        for(Iterator it=directoryList.iterator(); it.hasNext(); ) {
            URI uri = URI.create( ((AbstractService)it.next()).getFilename() );
            String path = uri.getPath().substring(uri.getPath().indexOf(":")+1, uri.getPath().lastIndexOf("/"));
            buffer.add(path);
        }
        
        LongestCommonSubstring lcsApp = new LongestCommonSubstring();
        
        String common = buffer.get(0).toString();        
        for(int i=0; i<buffer.size(); i++) {
            common = lcsApp.lcs(common, buffer.get(i).toString());
        }
        return common;
    }
    
    public static String findCommonRootInStringList(Vector buffer) throws java.lang.ArrayIndexOutOfBoundsException
    {
        LongestCommonSubstring lcsApp = new LongestCommonSubstring();        
        String common = buffer.get(0).toString();        
        for(int i=0; i<buffer.size(); i++) {
            common = lcsApp.lcs(common, buffer.get(i).toString());
        }
        return common;
    }
    
    /**
     * Checks if Name is a valid NCName.
     * See comment for OntClassContainer.getOntClass(..)
     * @param uri URI of OntClass
     * @return valid
     */
    public static boolean isValidNCName(String uri) {
        int idx = 0;
        if(uri.contains("#")) {
            idx=uri.indexOf("#")+1;
        }
        char first = uri.charAt(idx);
        char[] checklist = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };        
        
        for(int i=0; i<checklist.length; i++) {
            if(first == checklist[i]) {
                return false;
            }
        }
        return true;
    }
    
    public static void main(String args[]) {
        
        String[] testList = {
            "/a/services/a/a",
            "/a/services/a/b",
            "/a/services/a/c",
            "/a/services/b/a",
            "/b/services/b/b",
            "/b/services/a/c",
            "/b/was/ganz/anderes/b/a",
            "/b/services/b/b"
        };
        
        List l = Arrays.asList(testList);        
        Vector buffer = new Vector(l);
        System.out.println("COMMON: "+findCommonRootInStringList(buffer));
    }
}
