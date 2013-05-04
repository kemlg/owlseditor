package com.sri.owlseditor.widgets.swrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.XMLSchemaDatatypes;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtomList;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLBuiltin;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLNames;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLVariable;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLIncompleteRuleException;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLParseException;

/**
 * @author Martin O'Connor <moconnor@smi.stanford.edu>
 * @author Holger Knublauch <holger@smi.stanford.edu>
 */
public class OWLS_SWRLParser {

	public final static char AND_CHAR = '\u2227'; // ^

	public final static char IMP_CHAR = '\u2192'; // >

	private OWLModel owlModel;

	private SWRLFactory swrlFactory;

	private boolean parseOnly;

	private StringTokenizer tokenizer;

	private String delimiters = " ?\n\t()[],\"" + AND_CHAR + IMP_CHAR;

	private Collection xmlSchemaSymbols = XMLSchemaDatatypes.getSlotSymbols();

	private HashMap variables;

	public OWLS_SWRLParser(OWLModel owlModel) {
		this.owlModel = owlModel;
		swrlFactory = new SWRLFactory(owlModel);
		parseOnly = true;
		variables = new HashMap();
	} // SWRLParser

	public void setParseOnly(boolean parseOnly) {
		this.parseOnly = parseOnly;
	} // setParseOnly

	public SWRLAtomList parse(String rule) throws SWRLParseException {
		return parse(rule, (SWRLAtomList) null);
	} // parse

	/** New parse() method for AtomLists. Used by the SWRLWidget. */
	public SWRLAtomList parse(String rule, SWRLAtomList atomList)
			throws SWRLParseException {
		String token;
		SWRLAtomList body = null;
		SWRLAtom atom;
		boolean atLeastOneAtom = false;

		variables.clear();

		tokenizer = new StringTokenizer(rule, delimiters, true);

		if (!parseOnly) {
			body = swrlFactory.createAtomList();
		} // if

		if (!parseOnly && !tokenizer.hasMoreTokens()) {
			throw new SWRLParseException("Empty rule.");
		}

		while (tokenizer.hasMoreTokens()) {
			token = getNextNonSpaceToken("Expecting atom.");
			atom = parseAtom(token);
			atLeastOneAtom = true;
			if (!parseOnly) {
				body.append(atom);
			} // if

			if (tokenizer.hasMoreTokens()) {
				token = getNextNonSpaceToken("Expecting '" + AND_CHAR + "'.");
				if (!token.equals("" + AND_CHAR)) {
					throw new SWRLParseException("Expecting '" + AND_CHAR
							+ "' between atoms - got '" + token + "'.");
				}
			}
		}

		return body;
	}

	private SWRLAtom parseAtom(String identifier) throws SWRLParseException {
		SWRLAtom atom = null;
		List enumeratedList = null;
		boolean isEnumeratedList = false;

		if (!isValidIdentifier(identifier))
			throw new SWRLParseException("Invalid identifier: '" + identifier
					+ "'.");

		if (identifier.startsWith("[")) { // A data range with an enumerated
											// literal list
			enumeratedList = parseLiteralList();
			isEnumeratedList = true;
		} // if

		if (isEnumeratedList)
			checkAndSkipToken("(",
					"Expecting parameters enclosed in parentheses for data range atom.");
		else
			checkAndSkipToken("(",
					"Expecting parameters enclosed in parentheses for atom '"
							+ identifier + "'.");

		if (isEnumeratedList) {
			atom = parseEnumeratedListParameters(enumeratedList);
		} else if (isSameAs(identifier)) {
			atom = parseSameAsAtomParameters();
		} else if (isDifferentFrom(identifier)) {
			atom = parseDifferentFromAtomParameters();
		} else if (isOWLClassName(identifier)) {
			atom = parseClassAtomParameters(identifier);
		} else if (isOWLObjectPropertyName(identifier)) {
			atom = parseIndividualPropertyAtomParameters(identifier);
		} else if (isOWLDatatypePropertyName(identifier)) {
			atom = parseDatavaluedPropertyAtomParameters(identifier);
		} else if (isBuiltinName(identifier)) {
			atom = parseBuiltinParameters(identifier);
		}
		// else if (isXSDDatatype(identifier)) {
		// atom = parseXSDDatatypeParameters(identifier);
		// }
		else {
			throw new SWRLParseException("Invalid atom name '" + identifier
					+ "'.");
		} // if

		return atom;
	} // parseAtom

	private void checkAndSkipToken(String skipToken,
			String unexpectedTokenMessage) throws SWRLParseException {
		String token = getNextNonSpaceToken(unexpectedTokenMessage);

		if (!token.equalsIgnoreCase(skipToken))
			throw new SWRLParseException("Expecting '" + skipToken + "', got '"
					+ token + "'. " + unexpectedTokenMessage);

	} // checkAndSkipToken

	private String getNextStringToken(String noTokenMessage)
			throws SWRLParseException {
		String token = "";
		String errorMessage = "Incomplete rule. " + noTokenMessage;

		if (!tokenizer.hasMoreTokens()) {
			if (parseOnly)
				throw new SWRLIncompleteRuleException(errorMessage);
			else
				throw new SWRLParseException(errorMessage);
		} // if

		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken("\"");
			return token;
		} // while

		if (parseOnly) {
			throw new SWRLIncompleteRuleException(errorMessage);
		} else {
			throw new SWRLParseException(errorMessage); // Should not get here
		}

	} // getNextNonSpaceToken

	private String getNextNonSpaceToken(String noTokenMessage)
			throws SWRLParseException {
		String token = "";
		String errorMessage = "Incomplete rule. " + noTokenMessage;

		if (!tokenizer.hasMoreTokens()) {
			if (parseOnly)
				throw new SWRLIncompleteRuleException(errorMessage);
			else
				throw new SWRLParseException(errorMessage);
		} // if

		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken(delimiters);
			if (!(token.equals(" ") || token.equals("\n") || token.equals("\t"))) {
				return token;
			}
		} // while

		if (parseOnly) {
			throw new SWRLIncompleteRuleException(errorMessage);
		} else {
			throw new SWRLParseException(errorMessage); // Should not get here
		}

	} // getNextNonSpaceToken

	private boolean hasMoreNonSpaceTokens() {

		if (!tokenizer.hasMoreTokens()) {
			return false;
		}

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken(delimiters);
			if (!(token.equals(" ") || token.equals("\n") || token.equals("\t"))) {
				return true;
			}
		} // while

		return false;

	} // hasMoreNonSpaceTokens

	private SWRLAtom parseSameAsAtomParameters() throws SWRLParseException {
		RDFResource iObject1, iObject2;
		SWRLAtom atom = null;

		iObject1 = parseIObject();
		checkAndSkipToken(",",
				"Expecting comma-separated second parameter for SameAsAtom.");
		iObject2 = parseIObject();

		if (!parseOnly) {
			atom = swrlFactory.createSameIndividualAtom(iObject1, iObject2);
		} // if

		checkAndSkipToken(")",
				"Expecting closing parenthesis after second parameters in SameAsAtom");

		return atom;
	} // parseSameAsAtomParameters

	private SWRLAtom parseDifferentFromAtomParameters()
			throws SWRLParseException {
		RDFResource iObject1, iObject2;
		SWRLAtom atom = null;

		iObject1 = parseIObject();
		checkAndSkipToken(",",
				"Expecting comma-separated second parameters for DifferentFromAtom");
		iObject2 = parseIObject();

		if (!parseOnly) {
			atom = swrlFactory.createDifferentIndividualsAtom(iObject1,
					iObject2);
		} // if

		checkAndSkipToken(")",
				"Only two parameters allowed for DifferentFromAtom");

		return atom;
	} // parseDifferentFromAtomParameters

	private SWRLAtom parseClassAtomParameters(String identifier)
			throws SWRLParseException {
		RDFResource iObject;
		SWRLAtom atom = null;
		RDFSNamedClass aClass;

		iObject = parseIObject();

		if (!parseOnly) {
			aClass = owlModel.getOWLNamedClass(identifier);
			atom = swrlFactory.createClassAtom(aClass, iObject);
		} // if

		checkAndSkipToken(")",
				"Expecting closing parenthesis for parameter for ClassAtom '"
						+ identifier + "'.");

		return atom;
	} // parseClassAtomParameters

	private SWRLAtom parseIndividualPropertyAtomParameters(String identifier)
			throws SWRLParseException {

		RDFResource iObject1, iObject2;
		SWRLAtom atom = null;
		OWLObjectProperty objectSlot;

		iObject2 = parseIObject();

		iObject1 = parseIObject();
		checkAndSkipToken(",",
				"Expecting comma-separated second parameter for IndividualPropertyAtom '"
						+ identifier + "'");

		if (!parseOnly) {
			objectSlot = owlModel.getOWLObjectProperty(identifier);
			if (objectSlot == null)
				throw new SWRLParseException(
						"no datatype slot found for IndividualPropertyAtom: "
								+ identifier);
			atom = swrlFactory.createIndividualPropertyAtom(objectSlot,
					iObject1, iObject2);
		} // if

		checkAndSkipToken(
				")",
				"Expecting closing parenthesis after second parameter of IndividualPropertyAtom '"
						+ identifier + "'.");

		return atom;
	} // parseClassAtomParameters

	// TODO: clarify parsing of second parameter - SWRLVariable,
	// rdfs:literal, or both. For the moment, we just allow
	// SWRLVariables.

	private SWRLAtom parseDatavaluedPropertyAtomParameters(String identifier)
			throws SWRLParseException {
		RDFResource iObject;
		RDFObject dObject;
		SWRLAtom atom = null;
		// Object literalValue;
		OWLDatatypeProperty datatypeSlot;

		iObject = parseIObject();
		checkAndSkipToken(",",
				"Expecting comma-separated second parameter for DatavaluedPropertyAtom '"
						+ identifier + "'.");
		// literalValue = parseLiteralValue();
		dObject = parseDObject();

		if (!parseOnly) {
			datatypeSlot = owlModel.getOWLDatatypeProperty(identifier);
			// atom = swrlFactory.createDatavaluedPropertyAtom(datatypeSlot,
			// variable1, literalValue);
			atom = swrlFactory.createDatavaluedPropertyAtom(datatypeSlot,
					iObject, dObject);
		} // if

		checkAndSkipToken(
				")",
				"Expecting closing parenthesis after second parameter of DatavaluedPropertyAtom '"
						+ identifier + "'.");

		return atom;
	} // parseDatavaluedPropertyAtomParameters

	private SWRLAtom parseBuiltinParameters(String identifier)
			throws SWRLParseException {
		// RDFResource dObject;
		SWRLBuiltin builtin;
		SWRLAtom atom = null;
		List dObjects = new ArrayList();

		dObjects = parseDObjectList(); // Swallows ')'

		if (!parseOnly) {
			builtin = swrlFactory.getBuiltin(identifier);
			atom = swrlFactory.createBuiltinAtom(builtin, dObjects.iterator());
		} // if

		return atom;
	} // parseBuiltinParameters

	// private SWRLAtom parseXSDDatatypeParameters(String identifier)
	// throws SWRLParseException {
	// RDFObject dObject;
	// SWRLAtom atom = null;
	// RDFSDatatype datatype;
	//
	// dObject = parseDObject();
	//
	// if (!parseOnly) {
	// datatype = owlModel.getRDFSDatatypeByName(identifier);
	// atom = swrlFactory.createDataRangeAtom(datatype, dObject);
	// } // if
	//
	// checkAndSkipToken(")",
	// "Expecting closing parenthesis after DataRangeAtom '" + identifier +
	// "'.");
	//
	// return atom;
	// } // parseXSDDatatypeParameters

	private SWRLAtom parseEnumeratedListParameters(List enumeratedList)
			throws SWRLParseException {
		RDFObject dObject;
		SWRLAtom atom = null;
		Object literalValue;
		Iterator iterator;

		dObject = parseDObject();

		if (!parseOnly) {

			OWLDataRange dataRange = owlModel.createOWLDataRange();
			RDFProperty oneOfProperty = owlModel.getOWLOneOfProperty();

			iterator = enumeratedList.iterator();
			while (iterator.hasNext()) {
				literalValue = (Object) iterator.next();
				dataRange.addPropertyValue(oneOfProperty, literalValue);
			} // while
			atom = swrlFactory.createDataRangeAtom(dataRange, dObject);

		} // if

		checkAndSkipToken(")",
				"Expecting closing parenthesis after parameter in DataRangeAtom.");

		return atom;
	} // parseEnumeratedListParameters

	private List parseVariableList() throws SWRLParseException {
		SWRLVariable variable;
		List variables = null;
		String token;

		if (!parseOnly)
			variables = new ArrayList();

		// TODO: We should check better that it actually is a variable and
		// not an individual
		variable = (SWRLVariable) parseVariable();

		if (!parseOnly)
			variables.add(variable);

		token = getNextNonSpaceToken("Expecting additional comma-separated variables or end of variable list.");
		while (token.equals(",")) {
			variable = (SWRLVariable) parseVariable();
			if (!parseOnly)
				variables.add(variable);

			token = getNextNonSpaceToken("Expecting ',' or ')'.");

			if (!(token.equals(",") || token.equals(")")))
				throw new SWRLParseException("Expecting ',' or ')', got '"
						+ token + "'.");
		} // if

		return variables;

	} // parseVariableList

	private List parseLiteralList() throws SWRLParseException {
		Object literalValue;
		List literals = null;
		String token;

		if (!parseOnly)
			literals = new ArrayList();

		literalValue = parseLiteralValue();
		if (!parseOnly)
			literals.add(literalValue);

		token = getNextNonSpaceToken("Expecting additional comma-separated literals or end of literal list.");

		while (token.equals(",")) {
			literalValue = parseLiteralValue();
			if (!parseOnly)
				literals.add(literalValue);
			token = getNextNonSpaceToken("Expecting additional comma-separated literals or end of literal list.");

			if (!(token.equals(",") || token.equals("]")))
				throw new SWRLParseException("Expecting ',' or ']', got '"
						+ token + "'.");
		} // if

		return literals;

	} // parseLiteralList

	private List parseDObjectList() throws SWRLParseException {
		RDFObject dObject;
		List dObjects = null;
		String token;

		if (!parseOnly)
			dObjects = new ArrayList();

		dObject = parseDObject();

		if (!parseOnly)
			dObjects.add(dObject);

		token = getNextNonSpaceToken("Expecting additional comma-separated variables or end of variable list.");
		while (token.equals(",")) {
			dObject = parseDObject();

			if (!parseOnly)
				dObjects.add(dObject);

			token = getNextNonSpaceToken("Expecting ',' or ')'.");

			if (!(token.equals(",") || token.equals(")")))
				throw new SWRLParseException("Expecting ',' or ')', got '"
						+ token + "'.");
		} // if

		return dObjects;

	} // parseDObjectList

	private SWRLVariable parseVariable() throws SWRLParseException {
		SWRLVariable variable = null;
		String variableName;

		checkAndSkipToken("?", "Expecting variable name preceded by '?'.");

		variableName = getNextNonSpaceToken("Expecting variable name.");

		if (!isValidIdentifier(variableName))
			throw new SWRLParseException("Invalid variable name: '"
					+ variableName + "'.");

		if (!parseOnly)
			variable = getSWRLVariable(variableName);

		return variable;
	} // parseVariable

	private RDFResource parseIObject() throws SWRLParseException {
		RDFResource parsedEntity = null;
		String parsedString;

		parsedString = getNextNonSpaceToken("Expecting variable or individual.");

		if (parsedString.equals("?")) {
			// The parsed entity is a variable
			String variableName = getNextNonSpaceToken("Expected variable name");

			if (!isValidIdentifier(variableName))
				throw new SWRLParseException("Invalid variable name: '"
						+ variableName + "'.");
			if (!parseOnly) {
				parsedEntity = getSWRLVariable(variableName);
			}
		} else {
			if (!parseOnly)
				parsedEntity = getIndividual(parsedString);
		}

		return parsedEntity;
	} // parseVariable

	private RDFObject parseDObject() throws SWRLParseException {
		RDFObject parsedEntity = null;
		String parsedString;

		parsedString = getNextNonSpaceToken("Expecting variable or literal.");

		if (parsedString.equals("?")) {
			// The parsed entity is a variable
			String variableName = getNextNonSpaceToken("Expected variable name");
			if (!isValidIdentifier(variableName))
				throw new SWRLParseException("Invalid variable name: '"
						+ variableName + "'.");
			if (!parseOnly)
				parsedEntity = getSWRLVariable(variableName);
		} else if (parsedString.equals("\"")) {
			// The parsed entity is a string
			String stringValue = getNextStringToken("Expected a string.");
			if (!parseOnly)
				parsedEntity = owlModel.createRDFSLiteral(stringValue,
						owlModel.getXSDstring());
			getNextNonSpaceToken("Expected \"");
		} else if (parsedString.equals("true") || parsedString.equals("false")) {
			// The parsed entity is a boolean
			if (!parseOnly)
				parsedEntity = owlModel.createRDFSLiteral(parsedString,
						owlModel.getXSDboolean());
		}
		// According to the XSD spec, xsd:boolean's have the lexical space
		// {true, false, 1, 0}. We don't allow {1, 0} since these are
		// parsed as xsd:int's.
		else if (parsedString.equals("True")) {
			if (!parseOnly)
				parsedEntity = owlModel.createRDFSLiteral("true",
						owlModel.getXSDboolean());
		} else if (parsedString.equals("False")) {
			if (!parseOnly)
				parsedEntity = owlModel.createRDFSLiteral("false",
						owlModel.getXSDboolean());
		} else {
			// Is it an integer or a float then?
			int integerValue;
			float floatValue;
			try {
				integerValue = Integer.decode(parsedString).intValue();
				if (!parseOnly)
					parsedEntity = owlModel.createRDFSLiteral(parsedString,
							owlModel.getXSDint());
			} catch (NumberFormatException e1) {
				try {
					floatValue = Float.parseFloat(parsedString);
					if (!parseOnly)
						parsedEntity = owlModel.createRDFSLiteral(parsedString,
								owlModel.getXSDfloat());
				} catch (NumberFormatException e2) {
					throw new SWRLParseException("Invalid data literal: '"
							+ parsedString + "'.");
				}
			}
		}

		return parsedEntity;
	} // parseDObject

	// TODO: Handle escaped quote characters? Literal may also be an integer.

	private Object parseLiteralValue() throws SWRLParseException {
		String literalValue = null;
		String token;

		checkAndSkipToken("\"", "Quotation enclosed literal value expected.");

		// Original delimiters will be restored in next call to
		// getNextNonSpaceToken().
		if (!tokenizer.hasMoreTokens()) {
			if (parseOnly)
				throw new SWRLParseException("");
			else
				throw new SWRLParseException(
						"Expecting literal value after quote - got nothing.");
		} // if

		token = tokenizer.nextToken("\"");

		if (token.equals("")) {
			if (parseOnly)
				throw new SWRLParseException("");
			else
				throw new SWRLParseException(
						"Expecting literal value after quote - got nothing.");
		} // if

		if (token.equals("\"")) { // Empty literal
			if (!parseOnly)
				literalValue = "";
		} else {
			if (!parseOnly)
				literalValue = new String(token);

			checkAndSkipToken("\"", "Expecting '\"' to close literal value '"
					+ literalValue + "'.");
		} // if

		return literalValue;
	} // parseLiteralValue

	private boolean isSameAs(String identifier) throws SWRLParseException {
		return identifier.equalsIgnoreCase("sameAs");
	} // isSameAs

	private boolean isDifferentFrom(String identifier)
			throws SWRLParseException {
		return identifier.equalsIgnoreCase("differentFrom");
	} // isDifferentFrom

	private boolean isOWLClassName(String identifier) throws SWRLParseException {
		return owlModel.getRDFResource(identifier) instanceof RDFSNamedClass;
	} // isOWLClassName

	private boolean isOWLObjectPropertyName(String identifier)
			throws SWRLParseException {
		return owlModel.getRDFResource(identifier) instanceof OWLObjectProperty;
	} // isOWLObjectPropertyName

	private boolean isOWLDatatypePropertyName(String identifier)
			throws SWRLParseException {
		return owlModel.getRDFResource(identifier) instanceof OWLDatatypeProperty;
	} // isOWLDatatypePropertyName

	private boolean isBuiltinName(String identifier) throws SWRLParseException {
		RDFResource resource = owlModel.getRDFResource(identifier);
		return resource != null
				&& resource.getProtegeType().getName()
						.equals(SWRLNames.Cls.BUILTIN);
	} // isBuiltinName

	private boolean isXSDDatatype(String identifier) throws SWRLParseException {

		return (identifier.startsWith("xsd:") && xmlSchemaSymbols
				.contains(identifier.substring(4)));
	} // isXSDDatatype

	private boolean isValidIdentifier(String s) {
		if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
			return false;
		}
		for (int i = 1; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!(Character.isJavaIdentifierPart(c) || c == ':' || c == '-')) {
				return false;
			}
		}
		return true;
	} // isValidIdentifier

	// We do not create a new instance for each occurence of the same variable.

	private RDFResource getIndividual(String name) throws SWRLParseException {
		RDFResource resource = owlModel.getRDFResource(name);
		if (resource != null) {
			return resource;
		} else {
			throw new SWRLParseException(name
					+ " is not a valid individual name");
		}
	} // getIndividual

	private SWRLVariable getSWRLVariable(String name) throws SWRLParseException {
		RDFResource resource = owlModel.getRDFResource(name);
		if (resource instanceof SWRLVariable) {
			System.out.println("returning existing resource "
					+ resource.getName());
			return (SWRLVariable) resource;
		} else if (resource == null) {
			SWRLVariable var = swrlFactory.createVariable(name);
			System.out.println("returning new variable " + var.getName());

			OWLNamedClass swrlVarCls = owlModel
					.getOWLNamedClass("swrl:Variable");
			Collection swrlVars = swrlVarCls.getInstances(false);
			return var;
		} else {
			throw new SWRLParseException(name
					+ " cannot be used as a variable name");
		}
	} // getVariable

} // SWRLParser