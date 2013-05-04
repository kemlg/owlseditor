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

package com.sri.owlseditor.graph;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Rukman
 * 
 */
// --------------------------------------------------------------------------------------------
// GraphObject Class
// --------------------------------------------------------------------------------------------

public class GraphObject {
	private String childName;
	private String arrowText;
	private String childColor;
	private String parentColor;
	private String type; // Used to determine color scheme
	private int level;
	private int xCenter; // x coord of center of the node, calculated when node
							// is drawn
	private int yCenter; // y coord of center of the node, calculated when node
							// is drawn
	private double nodeWidth; // width of child node based on text and font
								// settings and border gap
	private ArrayList children;
	private String parentName;
	private int childNodeNum; // The posn in dotNodeList for this nodes child
								// instance
	private int parentNodeNum; // The posn in dotNodeList for this nodes parent
								// instance

	public GraphObject(String aParentName, String aChildName, String aArrowText) {

		parentName = aParentName;
		childName = aChildName;
		arrowText = aArrowText;
		children = new ArrayList();

	}

	public String getChildName() {
		return childName;
	}

	public String getArrowText() {
		return arrowText;
	}

	public String getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getXChildCenter() {
		return xCenter;
	}

	public int getYChildCenter() {
		return yCenter;
	}

	public String getChildColor() {
		return childColor;
	}

	public String getParentColor() {
		return parentColor;
	}

	public double getChildWidth() {
		return nodeWidth;
	}

	public void addChild(GraphObject gObject) {
		this.children.add(gObject);
	}

	public int getChildCount() {
		return children.size();
	}

	public String toString() {
		String output = "\n";
		output = output + "Parent  = " + parentName + ", ";
		output = output + "ArrowText = " + getArrowText() + ", ";
		output = output + "child = " + childName + "\n";
		return output;
	}

	public GraphObject getChild(int i) {
		return (GraphObject) children.get(i);
	}

	public String getParentName() {
		return parentName;
	}

	public int getChildNodeNum() {
		return childNodeNum;
	}

	public int getParentNodeNum() {
		return parentNodeNum;
	}

	public void setChildNodeNum(int position) {
		childNodeNum = position;
	}

	public void setParentNodeNum(int position) {
		parentNodeNum = position;
	}

	public void setXCenter(int x) {
		xCenter = x;
	}

	public void setYCenter(int y) {
		yCenter = y;
	}

	public void setNodeWidth(int width) {
		nodeWidth = width;
	}

	public void setChildColor(String aColor) {
		childColor = aColor;
	}

	public void setParentColor(String aColor) {
		parentColor = aColor;
	}

}