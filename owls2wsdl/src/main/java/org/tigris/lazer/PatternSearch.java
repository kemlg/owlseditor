/*
 * PatternSearch.java
 *
 * Created on 09 August 2003, 11:07
 */

package org.tigris.lazer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.tigris.lazer.cj.java.JavaLanguageStrategy;

/**
 *
 * @author Bob Tarling
 */
public class PatternSearch {

    int addPosition;
    private ArrayList tokens;
    private ArrayList compareQueue;
    private ArrayList positionQueue;
    private ArrayList pattern = new ArrayList();
    boolean codeOnly = true;
    
    /** Creates a new instance of PatternSearch */
    public PatternSearch(LanguageStrategy languageStrategy, File file, String pattern) {
        // Create a list of all tokens in that string.
        tokens = new ArrayList();
        Lazer tokenizer = new Lazer(languageStrategy, readFile(file), 0/*Lazer.COMPOUND_WHITE_SPACE*/);
        while (tokenizer.hasMoreElements()) {
            tokens.add(tokenizer.nextElement());
        }
        init(pattern);
    }
    
    /** Creates a new instance of PatternSearch */
    public PatternSearch(LanguageStrategy languageStrategy, String str, String pattern) {
        // Create a list of all tokens in that string.
        tokens = new ArrayList();
        Lazer tokenizer = new Lazer(languageStrategy, str, 0/*Lazer.COMPOUND_WHITE_SPACE*/);
        while (tokenizer.hasMoreElements()) {
            tokens.add(tokenizer.nextElement());
        }
        init(pattern);
    }
    
    /** Creates a new instance of PatternSearch */
    public PatternSearch(ArrayList tokens, String pattern) {
        // Create a list of all tokens in that string.
        this.tokens = tokens;
        init(pattern);
    }
    
    /** Creates a new instance of PatternSearch */
    public PatternSearch(ArrayList tokens, String pattern, boolean codeOnly) {
        // Create a list of all tokens in that string.
        this.tokens = tokens;
        this.codeOnly = codeOnly;
        init(pattern);
    }
    
    private void init(String patternString) {
        
        // Build the pattern
        pattern = new ArrayList();
        Lazer tokenizer;
        tokenizer = new Lazer(JavaLanguageStrategy.getInstance(), patternString, 0/*Lazer.COMPOUND_WHITE_SPACE*/);
        while (tokenizer.hasMoreElements()) {
            pattern.add(tokenizer.nextElement());
        }
        
        compareQueue = new ArrayList(pattern.size());
        positionQueue = new ArrayList(pattern.size());
        
    }

    public void setPosition(int position) {
        addPosition = position;
        compareQueue = new ArrayList(pattern.size());
        positionQueue = new ArrayList(pattern.size());
    }

    public int getMatch() {
        while (addPosition < tokens.size()) {
            //System.out.println("X" + tokens.get(addPosition));
            Token token = (Token)tokens.get(addPosition++);
            if (token.getType() instanceof CodeTokenType) {
                if (compareQueue.size() == pattern.size()) {
                    compareQueue.remove(0);
                    positionQueue.remove(0);
                }
                compareQueue.add(token);
                positionQueue.add(new Integer(addPosition-1));
                if (compareQueue.equals(pattern)) {
                    return Integer.parseInt(positionQueue.get(0).toString());
                }
            }
        }
        return -1;
    }
    
    public Token getToken(int i) {
        return (Token)tokens.get(i);
    }
    
    private String readFile(File file) {
        String fileContents = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                fileContents += "\n" + str;
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Exception caught " + e);
        }
        return fileContents;
    }
}
