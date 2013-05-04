/*****************************************************************************************
"The contents of this file are subject to the Mozilla Public License  Version 1.1 
(the "License"); you may not use this file except in compliance with the License.  
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License for the specific 
language governing rights and limitations under the License.

The Original Code is OWL-S Editor for Protege.

The Initial Developer of the Original Code is SRI International. 
Portions created by the Initial Developer are Copyright (C) 2004 the Initial Developer.  
All Rights Reserved.
 ******************************************************************************************/
package com.sri.owlseditor.cmp.graph;

/**
 * This class is used for the return value of the graph() method in
 * OWLSTreeNode. When creating a sub-graph, we must return the incoming and
 * outgoing connection points of the sub-graph, as well as attributes that we
 * want on the in- and outgoing edges, so that the caller can connect the new
 * subgraph correctly.
 * 
 * The first- and lastNode can be the same (this happens for Split, for
 * example).
 * 
 * @author Daniel Elenius
 */
public class GraphNodeInfo {
	public String firstNode;
	public String lastNode;
	public String inEdgeAttr;
	public String outEdgeAttr;
	public int clusterNumber;

	public GraphNodeInfo(String firstNode, String lastNode, String inEdgeAttr,
			String outEdgeAttr, int clusterNumber) {
		this.firstNode = firstNode;
		this.lastNode = lastNode;
		this.inEdgeAttr = inEdgeAttr;
		this.outEdgeAttr = outEdgeAttr;
		this.clusterNumber = clusterNumber;
	}

	/**
	 * Returns a string surrounded by "[]", with the incoming edge attributes of
	 * this instance, combined with the attributes in the parameter string.
	 * 
	 * If there are no resulting attributes, an empty string is returned.
	 * 
	 * @param attr
	 * @return
	 */
	public String createInEdgeAttr(String attr) {
		String attributes = null;
		if (attr.equals(""))
			if (inEdgeAttr == null || inEdgeAttr.equals(""))
				return "";
			else
				attributes = inEdgeAttr;
		else if (!(inEdgeAttr == null || inEdgeAttr.equals("")))
			attributes = new String(attr + ", " + inEdgeAttr);
		else
			attributes = attr;
		return " [" + attributes + "]";
	}

	/**
	 * Returns a string surrounded by "[]", with the outgoing edge attributes of
	 * this instance, combined with the attributes in the parameter string.
	 * 
	 * If there are no resulting attributes, an empty string is returned.
	 * 
	 * @param attr
	 *            comma-separated list of edge attributes from the DOT language.
	 * @return
	 */
	public String createOutEdgeAttr(String attr) {
		String attributes = null;
		if (attr.equals(""))
			if (outEdgeAttr == null || outEdgeAttr.equals(""))
				return "";
			else
				attributes = outEdgeAttr;
		else if (!(outEdgeAttr == null || outEdgeAttr.equals("")))
			attributes = new String(attr + ", " + outEdgeAttr);
		else
			attributes = attr;
		return " [" + attributes + "]";
	}

	public String toString() {
		return "First node: " + firstNode + "\nLast node: " + lastNode
				+ "Incoming edge attributes: " + inEdgeAttr
				+ "\nOutgoing edge attributes: " + outEdgeAttr;
	}
}
