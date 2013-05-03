/*
 * AbstractDatatypeRestrictionElement.java
 *
 * Created on 30. November 2006, 16:22
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
public class AbstractDatatypeRestrictionElement implements java.io.Serializable  {
    private String _restrictionType;
    private String _restrictionValue;
    private String _inheritedBy;
    private int    _level;
    
    /** Creates a new instance of AbstractDatatypeRestrictionElement */
    public AbstractDatatypeRestrictionElement() {
        
    }
    
    public AbstractDatatypeRestrictionElement(String restrictionType, String restrictionValue, int level, String inheritedBy) {
        //System.out.println("[C] AbstractDatatypeRestrictionElement");
        this._restrictionType    = restrictionType;
        this._restrictionValue   = restrictionValue;
        this._inheritedBy        = inheritedBy;
        this._level              = level;
    }
    
    public void setRestrictionType(String restrictionType) { this._restrictionType = restrictionType; }
    public void setRestrictionValue(String restrictionValue) { this._restrictionValue = restrictionValue; }
    public void setInheritedBy(String inheritedBy) { this._inheritedBy = inheritedBy; }
    public void setLevel(int level) { this._level = level; }
    
    public String getRestrictionType() { return this._restrictionType; }
    public String getRestrictionValue() { return this._restrictionValue; }
    public String getInheritedBy() { return this._inheritedBy; }
    public int    getLevel() { return this._level; }
    
    public String toString() {
        return this._restrictionType+": "+this._restrictionValue+ "(Level: "+this._level+", inheritedBy "+this._inheritedBy+")";
    }
}
