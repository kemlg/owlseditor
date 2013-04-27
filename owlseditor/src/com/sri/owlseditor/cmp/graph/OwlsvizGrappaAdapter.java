/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License
at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See
the License for the specific language governing rights and limitations
under the License.

The Original Code is the OWL/S Visualizer.

The Initial Developer of the Original Code is DCS Corporation.
Portions created by the Initial Developer are Copyright (C) 2004
the Initial Developer.  All Rights Reserved.

Contributor(s): SRI International

The following notice applies to the Original Code:

   Unlimited Rights assigned to the U.S. Government.
   This material may be reproduced by or for the U.S Government
   pursuant to the copyright license under the clause at DFARS
   227-7203-5(a), DFARS 227.7103-5(a), DFARS 252.227-7013(b)(1)(June
   1995), DFARS 252.227-7014 (June 1995), and FAR 52.227-14(a).
   This notice must appear in all copies of this file and its
   derivatives.
*/

package com.sri.owlseditor.cmp.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import att.grappa.Attribute;
import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.GrappaBox;
import att.grappa.GrappaConstants;
import att.grappa.GrappaListener;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

import com.sri.owlseditor.cmp.tree.PerformNode;


public class OwlsvizGrappaAdapter implements GrappaConstants, GrappaListener {

  private GraphPanel itsGraphPanel;
  private double currentZoom=1.0;
  private JPopupMenu rightClickMenu;
  private JPopupMenu compositeRightClickMenu;
  private Element currentElement;

  private class FindInputProviderListener implements ActionListener{
  	public void actionPerformed(ActionEvent ae) {
  		itsGraphPanel.findInputProviders(currentElement); 
  	}
  }

  private class FindOutputConsumerListener implements ActionListener{
  	public void actionPerformed(ActionEvent ae) {
  		itsGraphPanel.findOutputConsumers(currentElement); 
  	}
  }

  private class FindMatchingProcessListener implements ActionListener{
  	public void actionPerformed(ActionEvent ae) {
  		itsGraphPanel.findMatchingProcesses(currentElement); 
  	}
  }

  private class ShowCompositeProcessListener implements ActionListener{
  	public void actionPerformed(ActionEvent ae) {
  		System.out.println("Show this composite process");
  	}
  }
  
  public OwlsvizGrappaAdapter(GraphPanel panel) {
    itsGraphPanel=panel;
    createPopupMenus();
  }

  private void createPopupMenus(){
  		/* The normal right-click menu */
		rightClickMenu = new JPopupMenu();

		JMenuItem findInputProviderItem = new JMenuItem("Find input provider"); 
  		findInputProviderItem.addActionListener(new FindInputProviderListener());
  		rightClickMenu.add(findInputProviderItem); 
  		
  		JMenuItem findOutputConsumerItem = new JMenuItem("Find output consumer");
  		findOutputConsumerItem.addActionListener(new FindOutputConsumerListener());
  		rightClickMenu.add(findOutputConsumerItem); 

  		JMenuItem findMatchingProcessItem = new JMenuItem("Find matching process");
  		findMatchingProcessItem.addActionListener(new FindMatchingProcessListener());
  		rightClickMenu.add(findMatchingProcessItem); 

  		/* The composite process right-click menu */
  		/*
  		compositeRightClickMenu = new JPopupMenu();

  		JMenuItem cfindInputProviderItem = new JMenuItem("Find input provider"); 
  		cfindInputProviderItem.addActionListener(new FindInputProviderListener());
  		compositeRightClickMenu.add(cfindInputProviderItem); 
  		
  		JMenuItem cfindMatchingProcessItem = new JMenuItem("Find matching process");
  		cfindMatchingProcessItem.addActionListener(new FindMatchingProcessListener());
  		compositeRightClickMenu.add(cfindMatchingProcessItem); 
  		
  		JMenuItem showCompositeProcessItem = new JMenuItem("Show this composite process");
  		showCompositeProcessItem.addActionListener(new ShowCompositeProcessListener());
  		compositeRightClickMenu.add(showCompositeProcessItem);
  		*/ 
  }
  
  public void setCurrentZoom(double newZoom){
  	currentZoom = newZoom;
  }

  public double getCurrentZoom(){
  	return currentZoom;
  }

  private boolean isPerform(Element elem){
	return elem.getName().startsWith(PerformNode.PERFORM_PREFIX);
  }

  private boolean isCompositePerform(Element elem){
	return elem.getName().startsWith(PerformNode.COMPOSITE_PERFORM_PREFIX);
  }

  /**
   * The method called when a single mouse click occurs on a displayed subgraph.
   *
   * @param subg displayed subgraph where action occurred
   * @param elem subgraph element in which action occurred
   * @param pt the point where the action occurred (graph coordinates)
   * @param modifiers mouse modifiers in effect
   * @param panel specific panel where the action occurred
   */
    public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, int clickCount, GrappaPanel panel) {
      if (modifiers==16 && elem != null && elem.isNode()) {
      	//String name=elem.getName().replaceAll("BeginNode","").
		//						   replaceAll("EndNode","");
        itsGraphPanel.makeInstVisible(elem);
      }

    }

  /**
   * The method called when a mouse press occurs on a displayed subgraph.
   *
   * @param subg displayed subgraph where action occurred
   * @param elem subgraph element in which action occurred
   * @param pt the point where the action occurred (graph coordinates)
   * @param modifiers mouse modifiers in effect
   * @param panel specific panel where the action occurred
   */
  public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt,
      int modifiers, GrappaPanel panel) {

  	// This shows the right-click menu on Performs
  	
  	if ((modifiers&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK)) != 0
  	      && (modifiers&(InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK)) == modifiers
		  && elem != null && isPerform(elem)) {

  		currentElement = elem;
	    java.awt.geom.Point2D mpt = panel.getTransform().transform(pt,null);
	    
  		/*
	    if (isCompositePerform(elem))
  			compositeRightClickMenu.show(panel, (int)mpt.getX(), (int)mpt.getY());
  		else	
  			rightClickMenu.show(panel, (int)mpt.getX(), (int)mpt.getY());
  		*/
	    rightClickMenu.show(panel, (int)mpt.getX(), (int)mpt.getY());
  	}
  }


  /**
   * The method called when a mouse release occurs on a displayed subgraph.
   *
   * @param subg displayed subgraph where action occurred
   * @param elem subgraph element in which action occurred
   * @param pt the point where the action occurred (graph coordinates)
   * @param modifiers mouse modifiers in effect
   * @param pressedElem subgraph element in which the most recent mouse press occurred
   * @param pressedPt the point where the most recent mouse press occurred (graph coordinates)
   * @param pressedModifiers mouse modifiers in effect when the most recent mouse press occurred
   * @param outline enclosing box specification from the previous drag position (for XOR reset purposes)
   * @param panel specific panel where the action occurred
   */
    public void grappaReleased(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, Element pressedElem, GrappaPoint pressedPt, int pressedModifiers, GrappaBox outline, GrappaPanel panel) {
    }


  /**
   * The method called when a mouse drag occurs on a displayed subgraph.
   *
   * @param subg displayed subgraph where action occurred
   * @param currentPt the current drag point
   * @param currentModifiers the current drag mouse modifiers
   * @param pressedElem subgraph element in which the most recent mouse press occurred
   * @param pressedPt the point where the most recent mouse press occurred (graph coordinates)
   * @param pressedModifiers mouse modifiers in effect when the most recent mouse press occurred
   * @param outline enclosing box specification from the previous drag position (for XOR reset purposes)
   * @param panel specific panel where the action occurred
   */
    public void grappaDragged(Subgraph subg, GrappaPoint currentPt, int currentModifiers, Element pressedElem, GrappaPoint pressedPt, int pressedModifiers, GrappaBox outline, GrappaPanel panel) {
    }


  /**
   * The method called when an element tooltip is needed.
   *
   * @param subg displayed subgraph where action occurred
   * @param elem subgraph element in which action occurred
   * @param pt the point where the action occurred (graph coordinates)
   * @param modifiers mouse modifiers in effect
   * @param panel specific panel where the action occurred
   *
   * @return the tip to be displayed or null; in this implementation,
   * if the mouse is in a graph element that
   * has its <I>tip</I> attribute defined, then that text is returned.
   * If that attribute is not set, the element name is returned.
   * If the mouse is outside the graph bounds, then the text supplied
   * to the graph setToolTipText method is supplied.
   */
    public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, GrappaPanel panel) {
    	if (elem instanceof Edge){
    		Attribute comment = elem.getLocalAttribute("comment");
    		if (comment != null)
    			return comment.getStringValue();
    	}
	return null;
    }


}