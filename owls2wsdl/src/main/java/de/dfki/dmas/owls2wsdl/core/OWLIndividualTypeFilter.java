/*
 * OWLIndividualTypeFilter.java
 *
 * Created on 23. Oktober 2006, 14:35
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

import com.hp.hpl.jena.ontology.Individual;

/**
 * Filter for OntModel.listIndividuals() method
 * @author Oliver Fourman
 */
public class OWLIndividualTypeFilter extends com.hp.hpl.jena.util.iterator.Filter {
    
    private String _ontClassURI;
    
    /** Creates a new instance of OWLIndividualTypeFilter */
    public OWLIndividualTypeFilter(String ontClassURI) {
        this._ontClassURI = ontClassURI;
    }
    
    /** accept for OWL individuals with given OntClass URI */
    public boolean accept(Object value) {
        return value instanceof Individual && ((Individual)value).getRDFType().toString().equals(this._ontClassURI);
    }
    
}
