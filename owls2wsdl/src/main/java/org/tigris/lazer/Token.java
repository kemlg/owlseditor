/*
 * Token.java
 *
 * Created on 06 August 2003, 20:31
 */

package org.tigris.lazer;

import java.lang.String;

/**
 *
 * @author Bob Tarling
 */
public class Token {

    String text;
    TokenType type;
    int length;
        
    /** Creates a new instance of Token */
    Token(String text, TokenType type) {
        this.text = text;
        this.type = type;
        this.length = text.length();
    }
    
    /** Creates a new instance of Token */
    Token(int length, TokenType type) {
        this.type = type;
        this.length = length;
    }
    
    public String toString() {
        return text;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Token)) return false;
        Token other = (Token)o;
        if (this == o) return true;
        if (type != other.type) return false;
        if (this.text == null && other.text == null) {
            return ((length == other.length));
        }
        return (text.equals(other.text));
    }
    
    public boolean hasText(String text) {
        return this.text.equals(text);
    }
    
    public int getLength() {
        return length;
    }
    
    public int hashCode() {
        if (text != null) {
            return text.hashCode();
        }
        return length;
    }
    
    public Token append(Token token) {
        if (text != null) {
            return new Token(text + token.toString(), type);
        } else {
            return new Token(length + token.getLength(), type);
        }
    }
}
