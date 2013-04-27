// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License.  You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
// License for the specific language governing rights and limitations under
// the License.
//
// The Original Code is OWL-S Editor for Protege.
//
// The Initial Developer of the Original Code is SRI International.
// Portions created by the Initial Developer are Copyright (C) 2004 the
// Initial Developer.  All Rights Reserved.


package com.sri.owlseditor.consistency;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.sri.owlseditor.util.Cleaner;
import com.sri.owlseditor.util.CleanerListener;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

public class ConsistencyCheckDisplay implements CleanerListener {
    private static ConsistencyCheckDisplay instance;
    private JFrame  m_frame;
    private JPanel  m_panel;
    private Vector  m_data;
    SimpleAttributeSet m_attribBlueBold;
    SimpleAttributeSet m_attribBlue;
    SimpleAttributeSet m_attribRed;
    SimpleAttributeSet m_attribGreen;
    Color m_commitButtonColor = new Color (50, 230, 150);
    Color m_ignoreButtonColor = new Color (255, 100, 50);

    private ConsistencyCheckDisplay () {
	m_frame = ComponentFactory.createFrame();
	m_frame.setTitle ("Consistency Check");
	m_frame.setSize (1000, 800);
					       
	m_frame.setDefaultCloseOperation (WindowConstants.HIDE_ON_CLOSE);

	m_panel = new JPanel (new ColumnLayout (0, ColumnLayout.LAYOUT_STRETCH));

	JScrollPane ccScroll = new JScrollPane (m_panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	m_frame.getContentPane().setLayout (new BorderLayout());
	m_frame.getContentPane().add (ccScroll, BorderLayout.CENTER);
	m_frame.setVisible (false);

	m_attribBlueBold = new SimpleAttributeSet();
	StyleConstants.setForeground (m_attribBlueBold, Color.blue);
	StyleConstants.setBold (m_attribBlueBold, true);

	m_attribBlue = new SimpleAttributeSet();
	StyleConstants.setForeground (m_attribBlue, Color.blue);

	m_attribRed = new SimpleAttributeSet();
	StyleConstants.setForeground (m_attribRed, Color.red);

	m_attribGreen = new SimpleAttributeSet();
	StyleConstants.setForeground (m_attribGreen, new Color (0, 150, 50));

	ComponentListener compList = new ComponentListener() {
		public void componentResized (ComponentEvent e) {
		    updateDisplay (m_data);
		}
		public void componentShown (ComponentEvent e) {
		}
		public void componentMoved (ComponentEvent e) {
		}
		public void componentHidden (ComponentEvent e) {
		}
	    };
	m_frame.addComponentListener (compList);
	
	Cleaner.getInstance().registerCleanerListener(this);
    }

    public void cleanup(){
    	instance = null;
    }

    public static ConsistencyCheckDisplay getInstance() {
	if (instance == null)
	    instance = new ConsistencyCheckDisplay();
	return instance;
    }


    public void updateDisplay (Vector vData) {
	m_data = vData;
	show();
	m_panel.removeAll();

	if (vData == null || vData.size() == 0) {
	    m_frame.setVisible (false);
	    return;
	}

	Graphics g = m_panel.getGraphics();

	addLabelRow ("Inconsistencies", "Solutions", 20);

	for (int i = 0; i < vData.size(); i++) {
	    Vector vEntry = (Vector) vData.elementAt(i);

	    DefaultStyledDocument probDoc = new DefaultStyledDocument();

	    try {
		String str = (String) vEntry.elementAt(0); // label
		probDoc.insertString (probDoc.getLength(), str, m_attribBlueBold);

		Vector vProb = (Vector) vEntry.elementAt(1);
		for (int j = 0; j < vProb.size(); j++) {
		    SimpleAttributeSet sas = null;
		    Object obj = vProb.elementAt (j);
		    buildDoc (probDoc, obj);
		}

		Vector vSolution = (Vector) vEntry.elementAt (2);
		ActionListener ignoreListener = (ActionListener) vEntry.elementAt (3);

		for (int j = 0; j < vSolution.size(); j++) {
		    DefaultStyledDocument solDoc = new DefaultStyledDocument();
		    Vector vSol = (Vector) vSolution.elementAt (j);
		    ActionListener lst = (ActionListener) vSol.elementAt(0);
		    // skip over inst object (element after listener)

		    for (int k = 2; k < vSol.size(); k++) {
			Object obj = vSol.elementAt (k);
			buildDoc (solDoc, obj);
		    }

		    if (j != 0)
			probDoc = new DefaultStyledDocument();

		    addRowToDisplay (probDoc, solDoc, lst, (j == 0) ? ignoreListener : null);
		}

		addLabelRow ("", "", 10);
	    }
	    catch (BadLocationException e) {
	    }
	}

	m_panel.paintAll (g);
    }


    private void buildDoc (DefaultStyledDocument doc, Object obj) {
	SimpleAttributeSet sas = null;
	String str;

	if (obj instanceof RDFSClass) {
	    sas = m_attribRed;
	    str = ((RDFResource) obj).getName();
	}
	else if (obj instanceof RDFProperty) {
	    sas = m_attribGreen;
	    str = ((RDFResource) obj).getName();
	}
	else if (obj instanceof RDFIndividual) {
	    sas = m_attribBlue;
	    str = ((RDFResource) obj).getName();
	}
	else if (obj instanceof Vector) {
	    sas = new SimpleAttributeSet();
	    Vector v = (Vector) obj;
	    StyleConstants.setForeground (sas, (Color) v.elementAt(1));
	    str = (String) v.elementAt(0);
	}
	else {
	    str = obj.toString();
	}

	try {
	    doc.insertString (doc.getLength(), str, sas);
	}
	catch (BadLocationException e) {
	}
    }


    private void addRowToDisplay (DefaultStyledDocument problemDoc, DefaultStyledDocument solutionDoc, ActionListener lst, ActionListener ignoreLst) {
	JTextPane problemBox  = new JTextPane (problemDoc);
	JTextPane solutionBox = new JTextPane (solutionDoc);

	Insets insets = m_frame.getInsets();
	int width  = m_frame.getWidth() - insets.left - insets.right;
	int height = m_frame.getHeight() - insets.top;

	JPanel jj = new JPanel (new GridBagLayout());
	jj.setSize (new Dimension (width, height));
	m_panel.add (jj);

	// calc the pane width and height
	FontMetrics fm = solutionBox.getFontMetrics (solutionBox.getFont());
	int rowHeight = fm.getHeight();

	int paneWidth = (width / 2) - 50;

	int problemRowCount  = Math.max (2, getLineCount (problemBox, paneWidth, fm));
	int solutionRowCount = Math.max (2, getLineCount (solutionBox, paneWidth, fm));
	int paneHeight = rowHeight * Math.max (problemRowCount, solutionRowCount) + 4;

	problemBox.setPreferredSize (new Dimension (paneWidth, paneHeight));
	solutionBox.setPreferredSize (new Dimension (paneWidth, paneHeight));
	problemBox.setBorder (LineBorder.createBlackLineBorder());
	solutionBox.setBorder (LineBorder.createBlackLineBorder());

	jj.add (problemBox);
	jj.add (solutionBox);

	JPanel jbp = new JPanel();  // jbutton panel
	jbp.setLayout (new BoxLayout (jbp, BoxLayout.Y_AXIS));

	if (ignoreLst != null) {
	    JButton ignore = new JButton ("Ignore");
	    ignore.addActionListener (ignoreLst);
	    ignore.setBackground (m_ignoreButtonColor);
	    ignore.setPreferredSize (new Dimension (80, 20));
	    jbp.add (ignore);
	}

	if (lst != null) {
	    JButton jb = new JButton ("Commit");
	    jb.addActionListener (lst);
	    jb.setBackground (m_commitButtonColor);
	    jb.setPreferredSize (new Dimension (80, 20));
	    jbp.add (jb);
	}

	jj.add (jbp);
    }


    private void addLabelRow (String probStr, String solStr, int height) {
	JLabel probLabel = new JLabel ();
	JLabel solLabel  = new JLabel ();

	Insets insets = m_frame.getInsets();
	int width = m_frame.getWidth() - insets.left - insets.right;
	
	JPanel jj = new JPanel (new GridBagLayout());
	jj.setSize (new Dimension (width, height));
	m_panel.add (jj);
	probLabel.setPreferredSize (new Dimension (width/2 - 50, height));
	solLabel.setPreferredSize (new Dimension (width/2 - 50, height));

	JLabel backgrndLabel = new JLabel ();
	backgrndLabel.setPreferredSize (new Dimension (80, height));

	if (probStr.length() != 0) {
	    probLabel.setText ("<html><b>" + probStr + "</b></html>");
	    probLabel.setHorizontalAlignment (JLabel.CENTER);
	    probLabel.setBorder (LineBorder.createBlackLineBorder());
	}
	if (solStr.length() != 0) {
	    solLabel.setText ("<html><b>" + solStr + "</b></html>");
	    solLabel.setHorizontalAlignment (JLabel.CENTER);
	    solLabel.setBorder (LineBorder.createBlackLineBorder());
	}

	jj.add (probLabel);
	jj.add (solLabel);
	jj.add (backgrndLabel);
    }


    private int getLineCount (JTextPane box, int paneWidth, FontMetrics fm) {
	int nlines = 0;

	if (box == null)
	    return nlines;

	String str = box.getText();

	if (str == null || str.length() == 0)
	    return nlines;

	if (fm != null && fm.stringWidth (str) > paneWidth) {
	    StringTokenizer st = new StringTokenizer (str);
	    String nextStr = st.nextToken();

	    while (st.hasMoreTokens()) {
		String currToken = st.nextToken();

		if (fm.stringWidth (nextStr + " " + currToken) <= paneWidth) {
		    nextStr += " " + currToken;
		    continue;
		}

		nlines++;
		nextStr = currToken;
	    }

	    if (nextStr.length() > 0)
		nlines++;
	}
	else {
	    nlines++;
	}

	return nlines;
    }


    public void removeInconsistencyFromDisplay (ActionListener lst_rem, int ignoreFlag) {
	int indx = -1;

	for (int i = 0; i < m_data.size() && indx == -1; i++) {
	    Vector vEntry = (Vector) m_data.elementAt (i);
	    ActionListener lst;

	    if (ignoreFlag == 1) {
		lst = (ActionListener) vEntry.elementAt (3);
		if (lst == lst_rem)
		    indx = i;
	    }
	    else {
		Vector vSolution = (Vector) vEntry.elementAt (2);
		for (int j = 0; j < vSolution.size() && indx == -1; j++) {
		    Vector vSol = (Vector) vSolution.elementAt (j);
		    lst = (ActionListener) vSol.elementAt (0);
		    if (lst == lst_rem)
			indx = i;
		}
	    }
	}

	if (indx != -1) {
	    m_data.removeElementAt (indx);
	    updateDisplay (m_data);
	}
    }


    /*  should work for JTextPane but doesn't!!!
    private int getLineCount (JTextPane jtp) {
	int nlines = 0;
	View view = jtp.getUI().getRootView (jtp).getView(0);
	for (int i = 0; i < view.getViewCount(); i++)
	    nlines += view.getView(i).getViewCount();
	
	return nlines;
    }
    */


    public void show () {
    	m_frame.setVisible (true);
    }
}
