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

//--------------------------------------------------------------------------------------------
//DotNode Class
//--------------------------------------------------------------------------------------------

public class DotNode
{
	private String nodeName;
	private String nodeText;
	private int nodeType;
	private String rank;
	private String color;
	private boolean selected;
	private String shape;
	
	public DotNode(String aNodeName, String aNodeText)
	{
		nodeName = aNodeName;			//The node name for the dot file
		nodeText = aNodeText;			//The text displayed in the dot and graphviz files
	}
	
	public String getDotNodeName()
	{
		return nodeName;
	}
	
	public String getColor()
	{
		return color;
	}
	
	public String getShape()
	{
		return shape;
	}
	
	public String getRank()
	{
		return rank;
	}
	
	public String getNodeText()
	{
		return nodeText;
	}
	
	public void setShape(String ashape)
	{
		shape=ashape;
	}
	
	public void setColor(String aColor)
	{
		color=aColor;
	}
	
	public void setRank(String aRank)
	{
		rank = aRank;
	}
	
	public void setNodetext(String aNodeText)
	{
		nodeText = aNodeText;
	}
	
}
