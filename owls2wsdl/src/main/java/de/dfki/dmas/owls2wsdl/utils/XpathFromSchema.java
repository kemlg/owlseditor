/*
 * XpathFromSchema.java
 *
 * Created on 1. März 2007, 19:31
 *
 * http://affy.blogspot.com/2004/01/using-castor-and-java-to-list-xpaths.html
 */

package de.dfki.dmas.owls2wsdl.utils;

import java.util.*;
import java.util.regex.*;
import org.exolab.castor.xml.schema.*;
import org.exolab.castor.xml.schema.reader.*;
import org.exolab.castor.xml.schema.simpletypes.*;
import org.xml.sax.*;

/**
 * This class reads an XSD file to extract XPATHs. Most of the code is simple. However, a Stack is used to track which types of elements are visited
 * so that the program won't infinitely recurse when a container can contain itself. For example, <foo><bar><foo/></bar></foo>
 */
public class XpathFromSchema {

	private static int numXpaths = 0;
	private static Stack visitedTypes = new Stack();
//        private HashMap childParentRelation = new HashMap();
//        private static Vector processedComplexTypes = new Vector();

	/** provide a simple method to start the dump. */
	private static void dump(final ElementDecl elementDecl) {
		dump("", elementDecl);
	}

	/** there may be some situations where the starting xpath is actually
	 * a prefix because the starting node can be different from the root node.
	 * In either case, allowing an xpath as a parameter provides flexibility 
	 * for unforeseen needs.
	 */
	private static void dump(final String xpath, final ElementDecl elementDecl) {
		if (elementDecl == null) {
			return;
		}
		List forcedXpaths = new ArrayList();
		XMLType typeReference = elementDecl.getType();
//                System.out.println("XPATH: "+xpath);
                int idx = xpath.indexOf(elementDecl.getName());                                
                
		if (typeReference.getName() != null && visitedTypes.contains(typeReference.getName())) {
                    // The type is already in the stack, therefore if we were to continue we would infinitely recurse.
                    System.out.println("The type ("+typeReference.getName()+") element ("+elementDecl.getName()+") is already in the stack");
		}
                else if(idx > 0) {
                    System.out.println("Element "+elementDecl.getName()+" already in xpath");
                }
                else {
			if (typeReference.getName() != null) {
				visitedTypes.push(typeReference.getName());
			}

			String newXpath = xpath + "/" + elementDecl.getName();
			numXpaths++;

			if (typeReference.isComplexType()) {
				ComplexType ct = (ComplexType)typeReference;
                                
				Enumeration attributes = ct.getAttributeDecls();
				while (attributes.hasMoreElements()) {
					AttributeDecl attributeDecl = (AttributeDecl)attributes.nextElement();
					System.out.println(numXpaths + ": " + newXpath + "/@" + attributeDecl.getName());
					numXpaths++;
				}
				Enumeration particles = ct.enumerate();
				while (particles.hasMoreElements()) {
					Object o = particles.nextElement();
					if (o instanceof Group) {
                                            System.out.println("ComplexType ("+numXpaths + "): " + newXpath+" ("+ct.getName()+")");
                                            dumpGroup(newXpath, (Group)o);
					} else {
						System.out.println(" [dump] ***** Unknown particle type: " + o.getClass().getName());
					}
				}
			}
                        else if(typeReference.isSimpleType()) {
                            SimpleType st = (SimpleType)typeReference;
                            System.out.println("SimpleType  ("+numXpaths + "): " + newXpath + " ("+st.getName()+")");
                        }
                        else {
                            System.out.println(numXpaths + ": " + newXpath);
                        }
		}

		if (typeReference.getName() != null && !visitedTypes.empty()) {
			visitedTypes.pop();
		}
	}

	/** I have no idea what a group is, but a little experimentation
	 * showed the follow method to work.
	 */
	public static void dumpGroup(String xpath, final Group group) {
                Enumeration particles = group.enumerate();
                while (particles.hasMoreElements()) {                                       
                        Object o = particles.nextElement();
                        if (o instanceof Group) {
                                System.out.println("[recursion] "+xpath);
                                dumpGroup(xpath, (Group)o);
                        } else if (o instanceof ElementDecl) {      
                            ElementDecl elemDecl = (ElementDecl)o;
                                //System.out.println("[recursion] "+elemDecl.getName()+" ("+elemDecl.getType().getName()+")");
                                dump(xpath, (ElementDecl)o);
                        } else {
                                System.out.println("[dumpGroup] ***** Unknown particle type: " + o.getClass().getName());
                        }
                }
	}

	public static void main(String[] args) {
		//String xsdFile = "file:///[PUT_YOUR_XSD_FILESPEC_HERE.";
                String xsdFile = "file:///d:/tmp/stud.xsd"; //bsp.xsd";

		try {
			SchemaReader a = new SchemaReader(new InputSource(xsdFile));
			Schema s = a.read();

			// since this is a demonstration program, I select the topmost element to
			// start with.
			//ElementDecl elementDecl = s.getElementDecl("[NAME_OF_ELEMENT]");
                        ElementDecl elementDecl = s.getElementDecl("Student");
			dump(elementDecl);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done.");
		}
	}
}