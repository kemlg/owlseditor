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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.Graph;
import att.grappa.Grappa;
import att.grappa.GrappaConstants;
import att.grappa.GrappaListener;
import att.grappa.GrappaNexus;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.GrappaSupport;
import att.grappa.Node;
import att.grappa.Parser;

import com.sri.owlseditor.cmp.GraphUpdateManager;
import com.sri.owlseditor.cmp.tree.OWLSTree;
import com.sri.owlseditor.cmp.tree.OWLSTreeMapper;
import com.sri.owlseditor.cmp.tree.OWLSTreeNode;
import com.sri.owlseditor.cmp.tree.PerformNode;
import com.sri.owlseditor.matchmaker.InputProviderFinder;
import com.sri.owlseditor.matchmaker.MatchingProcessFinder;
import com.sri.owlseditor.matchmaker.OutputConsumerFinder;
import com.sri.owlseditor.options.GraphVizOptions;
import com.sri.owlseditor.util.OWLSIcons;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

public class GraphPanel extends JPanel implements GrappaConstants {

	GrappaPanel itsGrappaPanel;
	Graph itsGrappaGraph;
	Vector itsSelection;
	private Project project;
	String itsDotFontsize;
	OWLIndividual selectedProcess;
	// OWLSTree tree;
	private OWLModel okb;
	private JScrollPane scrollPane;
	private JToolBar toolbar;
	private OwlsvizGrappaAdapter grappaAdapter;
	private HashSet currentNameSet;
	private OWLObjectProperty processProperty;
	private GraphUpdateManager mgr;

	public GraphPanel(Project project, GraphUpdateManager mgr) {
		// super(VERTICAL_SCROLLBAR_ALWAYS,
		// HORIZONTAL_SCROLLBAR_ALWAYS);
		super();
		this.project = project;
		this.okb = (OWLModel) project.getKnowledgeBase();
		this.mgr = mgr;
		// this.tree = tree;
		itsSelection = new Vector();
		itsGrappaGraph = null;
		itsGrappaPanel = null;
		scrollPane = ComponentFactory.createScrollPane();
		createToolBar();

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(null);
		// Initialize Grappa constants
		Grappa.nodeLabelsScaleCutoff = 0.1;
		Grappa.edgeLabelsScaleCutoff = .1;
		itsDotFontsize = ApplicationProperties.getString("dot.fontsize", null);
		grappaAdapter = new OwlsvizGrappaAdapter(this);

		processProperty = okb.getOWLObjectProperty("process:process");
	}

	private OWLIndividual getPerformForNode(Element performNode) {
		String name = performNode.getName().replaceAll("BeginNode", "")
				.replaceAll("EndNode", "");
		String performName = UniqueName.findOriginalName(name, currentNameSet)
				.replaceAll(PerformNode.ATOMIC_PERFORM_PREFIX, "")
				.replaceAll(PerformNode.SIMPLE_PERFORM_PREFIX, "")
				.replaceAll(PerformNode.COMPOSITE_PERFORM_PREFIX, "")
				.replaceAll(PerformNode.PERFORM_PREFIX, "");
		OWLIndividual perform = okb.getOWLIndividual(performName);
		return perform;
	}

	public void findInputProviders(Element performNode) {
		InputProviderFinder.getInstance(mgr).show(
				getPerformForNode(performNode), selectedProcess);
	}

	public void findOutputConsumers(Element performNode) {
		OutputConsumerFinder.getInstance(mgr).show(
				getPerformForNode(performNode), selectedProcess);
	}

	public void findMatchingProcesses(Element performNode) {
		MatchingProcessFinder.getInstance(mgr).show(
				getPerformForNode(performNode), selectedProcess);
	}

	class ZoomInAction extends AbstractAction {
		public ZoomInAction() {
			super("", OWLSIcons.getZoomInIcon());
		}

		public void actionPerformed(ActionEvent ae) {
			GrappaPanel gp = itsGrappaPanel;
			gp.multiplyScaleFactor(1.25);
			grappaAdapter.setCurrentZoom(grappaAdapter.getCurrentZoom() * 1.25);

			itsGrappaPanel.setScaleToFit(false);
			scrollPane.setViewportView(itsGrappaPanel);

			itsGrappaGraph.paintImmediately();
			// scrollToCenterPoint(lastPt);
		}
	}

	class ZoomOutAction extends AbstractAction {
		public ZoomOutAction() {
			super("", OWLSIcons.getZoomOutIcon());
		}

		public void actionPerformed(ActionEvent ae) {
			GrappaPanel gp = itsGrappaPanel;
			gp.multiplyScaleFactor(0.8);
			grappaAdapter.setCurrentZoom(grappaAdapter.getCurrentZoom() * 0.8);
			itsGrappaGraph.paintImmediately();
			// scrollToCenterPoint(lastPt);
			// can use centerPanelAtPoint()
		}
	}

	class ZoomInFastAction extends AbstractAction {
		public ZoomInFastAction() {
			super("", OWLSIcons.getZoomInFastIcon());
		}

		public void actionPerformed(ActionEvent ae) {
			GrappaPanel gp = itsGrappaPanel;
			gp.multiplyScaleFactor(2.0);
			grappaAdapter.setCurrentZoom(grappaAdapter.getCurrentZoom() * 2.0);
			itsGrappaGraph.paintImmediately();
			// scrollToCenterPoint(lastPt);
		}
	}

	class ZoomOutFastAction extends AbstractAction {
		public ZoomOutFastAction() {
			super("", OWLSIcons.getZoomOutFastIcon());
		}

		public void actionPerformed(ActionEvent ae) {
			GrappaPanel gp = itsGrappaPanel;
			gp.multiplyScaleFactor(0.5);
			grappaAdapter.setCurrentZoom(grappaAdapter.getCurrentZoom() * 0.5);
			itsGrappaGraph.paintImmediately();
			// scrollToCenterPoint(lastPt);
		}
	}

	class ResetZoomAction extends AbstractAction {
		public ResetZoomAction() {
			super("", OWLSIcons.getResetZoomIcon());
		}

		public void actionPerformed(ActionEvent ae) {
			GrappaPanel gp = itsGrappaPanel;
			double currentZoom = grappaAdapter.getCurrentZoom();
			// gp.multiplyScaleFactor(1.0/currentZoom);
			gp.resetZoom();
			grappaAdapter.setCurrentZoom(1.0);
			itsGrappaGraph.paintImmediately();
			// scrollToCenterPoint(lastPt);
		}
	}

	class PrintAction extends AbstractAction {
		public PrintAction() {
			super("", OWLSIcons.getPrintIcon());
		}

		public void actionPerformed(ActionEvent ae) {
			GrappaPanel gp = itsGrappaPanel;
			PrinterJob printJob = PrinterJob.getPrinterJob();
			printJob.setJobName("OWL-S Editor Print Job");
			PageFormat pf = printJob.pageDialog(printJob.defaultPage());
			printJob.setPrintable(gp, pf);
			if (printJob.printDialog()) {
				try {
					printJob.print();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	class SVGExportAction extends AbstractAction {
		JComponent parent;

		public SVGExportAction(JComponent parent) {
			super("", OWLSIcons.getSVGExportIcon());
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent ae) {
			/*
			 * try{ String dotfilename = "Z:\\MyDocuments\\testfile.dot";
			 * FileWriter writer = new FileWriter(dotfilename);
			 * itsGraphPanel.itsGrappaGraph.printGraph(writer); String command =
			 * itsGraphPanel.getGraphVizPath() +
			 * " -Tjpg -oZ:\\MyDocuments\\testfile.jpg " + dotfilename;
			 * Runtime.getRuntime().exec(command); } catch (IOException e){
			 * e.printStackTrace(); }
			 */
			// Get a DOMImplementation
			DOMImplementation domImpl = GenericDOMImplementation
					.getDOMImplementation();

			// Create an instance of org.w3c.dom.Document
			Document document = domImpl.createDocument(null, "svg", null);

			// Create an instance of the SVG Generator
			SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

			// Render the GraphPanel into the SVG Graphics2D implementation
			itsGrappaPanel.paint(svgGenerator);

			// Create and bring up the save file dialog
			class SVGFilter extends FileFilter {
				public boolean accept(File f) {
					if (f.toString().endsWith(".svg"))
						return true;
					return false;
				}

				public String getDescription() {
					return "SVG Files";
				}
			}

			JFileChooser chooser = new JFileChooser();
			SVGFilter svgFilter = new SVGFilter();
			chooser.setFileFilter(svgFilter);

			File svgFile = null;
			int returnVal = chooser.showSaveDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				svgFile = chooser.getSelectedFile();
			else
				return;

			try {
				// Finally, stream out SVG to the standard output using UTF-8
				// character to byte encoding
				boolean useCSS = true; // we want to use CSS style attribute
				FileOutputStream fileout = new FileOutputStream(svgFile);
				Writer out = new OutputStreamWriter(fileout, "UTF-8");
				svgGenerator.stream(out, useCSS);
				out.flush();
				out.close();
			} catch (SVGGraphics2DIOException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createToolBar() {
		toolbar = ComponentFactory.createToolBar();

		JButton svgexportButton = toolbar.add(new SVGExportAction(this));
		svgexportButton.setToolTipText("Export to SVG");

		JButton printButton = toolbar.add(new PrintAction());
		printButton.setToolTipText("Print");

		JButton zoomoutButton = toolbar.add(new ZoomOutAction());
		zoomoutButton.setToolTipText("Zoom out");

		JButton resetzoomButton = toolbar.add(new ResetZoomAction());
		resetzoomButton.setToolTipText("Reset zoom");

		JButton zoominButton = toolbar.add(new ZoomInAction());
		zoominButton.setToolTipText("Zoom in");

		/*
		 * JButton zoomoutfastButton = toolbar.add(new ZoomOutFastAction());
		 * zoomoutfastButton.setToolTipText("Zoom out fast");
		 * 
		 * JButton zoominfastButton = toolbar.add(new ZoomInFastAction());
		 * zoominfastButton.setToolTipText("Zoom in fast");
		 */
	}

	public void setSelectedProcess(OWLIndividual process) {
		selectedProcess = process;
	}

	/* Methods moved over from Owlsviz.java */

	public void makeInstVisible(Element node) {
		/*
		 * String instanceName = UniqueName.findOriginalName(nodeName,
		 * currentNameSet). replaceAll("PerformA","").
		 * replaceAll("PerformS",""). replaceAll("PerformC","").
		 * replaceAll("Perform","");
		 * 
		 * OWLIndividual inst = okb.getOWLIndividual(instanceName);
		 */
		OWLIndividual inst = getPerformForNode(node);
		if (inst != null) {
			project.show(inst);
		}
	}

	public boolean isCompositeProcess(String componsiteProcessname) {
		OWLIndividual inst = okb.getOWLIndividual(componsiteProcessname);
		if (inst != null)
			return inst.hasRDFType(okb
					.getOWLNamedClass("process:CompositeProcess"));
		else
			return false;
	}

	/*
	 * public boolean isEffect(String effectName) { Instance inst
	 * =project.getKnowledgeBase().getInstance(effectName); if(inst!=null)
	 * return inst.hasType(project.getKnowledgeBase().getCls(
	 * "igvbehavior:ExternalServiceRequested")); else return false; }
	 */

	public void viewSelectedCompositeProcess(OWLSTreeNode selectedConstruct) {
		class View implements Runnable {
			private OWLSTreeNode selectedConstruct;

			public View(OWLSTreeNode selectedConstruct) {
				this.selectedConstruct = selectedConstruct;
			}

			public void run() {
				try {
					OWLIndividual selectedInst = selectedProcess;
					if (selectedInst != null) {
						String file = project.getProjectName();
						if (file == null)
							file = "ontology";
						String dotFile = file + ".dot";

						// Delete the dot file if it already exists
						File tempfile = new File(dotFile);
						tempfile.delete();

						// OWLSTreeNode root =
						// (OWLSTreeNode)tree.getModel().getRoot();
						OWLSTree tree = OWLSTreeMapper.getInstance(okb)
								.getTree(selectedProcess, mgr);
						OWLSTreeNode root = (OWLSTreeNode) tree.getModel()
								.getRoot();
						OWLSTreeNode node = null;
						if (root.getChildCount() > 0)
							node = (OWLSTreeNode) root.getChildAt(0); // we only
																		// look
																		// at
																		// the
																		// main
																		// tree
																		// for
																		// now
						GraphProcessModel myGraph = new GraphProcessModel(node,
								selectedConstruct, dotFile,
								project.getKnowledgeBase());
						currentNameSet = myGraph.nameSet;
						displayGraph(dotFile, currentNameSet);
						repaint();
					}

				} catch (Exception e) {
					System.out
							.println("GraphPanel: Failed to Graph CompositeProcess");
					e.printStackTrace();
				}
			}
		}
		// System.out.println("viewSelectedCompositeProcess(): node " +
		// selectedConstruct + " selected.");
		SwingUtilities.invokeLater(new View(selectedConstruct));
	}

	public String getSelectedProcessName() {
		return selectedProcess.getName();
	}

	public String getGraphVizPath() {
		String path = (String) project
				.getClientInformation(GraphVizOptions.OWLSEDITOR_GRAPHVIZ_PATH_KEY);
		if (path == null)
			return GraphVizOptions.DEFAULT_GRAPHVIZ_PATH;
		else
			return path;
	}

	public void displayGraph(String FileName, HashSet nameSet) {

		// System.out.println("displayGraph");

		FileInputStream input;
		try {
			input = new FileInputStream(FileName);

			Parser program = new Parser(input, System.err);
			program.parse();

			Object connector = null;
			// connector =
			// Runtime.getRuntime().exec("C:\\Program Files\\ATT\\Graphviz\\bin\\dot.exe");
			try {
				connector = Runtime.getRuntime().exec(getGraphVizPath());
			} catch (Exception e) {
				System.out
						.println("ERROR! Could not execute GraphViz at the given path: "
								+ getGraphVizPath());
				return;
			}

			Graph graph = null;
			graph = program.getGraph();

			// System.err.println("The graph contains " +
			// graph.countOfElements(Grappa.NODE | Grappa.EDGE |
			// Grappa.SUBGRAPH) + " elements.");

			if (connector != null) {
				if (!GrappaSupport.filterGraph(graph, connector)) {
					System.err.println("ERROR: somewhere in filterGraph");
				}
			}

			graph.setEditable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));
			// System.err.println("bbox=" +
			// graph.getBoundingBox().getBounds().toString());
			if (itsDotFontsize != null)
				graph.setGrappaAttribute(
						GrappaConstants.GRAPPA_FONTSIZE_ADJUSTMENT_ATTR,
						itsDotFontsize);

			setGraph(graph);
			addGrappaListener(grappaAdapter);
		} catch (FileNotFoundException fnf) {
			System.err.println(fnf.toString());
		} catch (Exception ex) {
			System.err.println("Exception: " + ex.getMessage());
			ex.printStackTrace(System.err);
		}

		// Added by D.E.
		repaint();
	}

	/* End stuff from Owlsviz.java */

	public void setGraph(Graph graph) {
		// graph may be null
		// if (graphIsShowing()) {
		// }
		// System.out.println("setGraph");

		itsGrappaGraph = graph;
		itsSelection.removeAllElements();
		if (itsGrappaGraph == null) {
			itsGrappaPanel = null;
			scrollPane.setViewportView(null);
		} else {
			itsGrappaPanel = new GrappaPanel(itsGrappaGraph);

			// Set initial Scale
			Rectangle gRect = graph.getBoundingBox().getBounds();
			// System.out.println("Graph rectangle: " +gRect);

			Rectangle vRect = scrollPane.getViewport().getViewRect();
			// System.out.println("View rectangle: " +vRect);

			double hr = (double) vRect.width / (60 + (double) gRect.width);
			if (hr > 1.0)
				hr = 1.0;
			double vr = (double) vRect.height / (60 + (double) gRect.height);
			if (vr > 1.0)
				vr = 1.0;
			double scale;
			if (vr < hr)
				scale = vr;
			else
				scale = hr;
			itsGrappaPanel.multiplyScaleFactor(scale);
			itsGrappaPanel.setScaleToFit(false);
			scrollPane.setViewportView(itsGrappaPanel);

			// System.out.println("Preferred viewport size: " +
			// itsGrappaPanel.getPreferredScrollableViewportSize());

		}
	}

	public void addGrappaListener(GrappaListener listener) {
		if (graphIsShowing())
			itsGrappaPanel.addGrappaListener(listener);
	}

	public boolean graphIsShowing() {
		return itsGrappaGraph != null;
	}

	public void selectMultiple(Collection ids) {
		if (graphIsShowing()) {
			deselectAll();
			Element someElement = null;
			for (Iterator idIterator = ids.iterator(); idIterator.hasNext();) {
				String id = (String) idIterator.next();
				Element element = selectNodeOrEdge(id);
				if (someElement == null && element != null)
					someElement = element;
			}
			if (someElement != null)
				scrollToCenterPoint(someElement);
		}
	}

	Element selectNodeOrEdge(String id) {
		// find node or edge
		Node node = itsGrappaGraph.findNodeByName(id);
		if (node != null) {
			select(node);
			return node;
		} else {
			Edge edge = itsGrappaGraph.findEdgeByName(id);
			if (edge != null) {
				select(edge);
				return edge;
			}
		}
		return null;
	}

	void scrollToCenterPoint(Element element) {
		GrappaNexus nexus = element.getGrappaNexus();
		Rectangle bounds = nexus.getBounds();
		double centerX = bounds.getCenterX();
		double centerY = bounds.getCenterY();
		Point2D center = new Point2D.Double(centerX, centerY);
		AffineTransform transform = itsGrappaPanel.getTransform();
		// variables with a T suffix denote transformed coordinates
		Point2D centerT = transform.transform(center, null);
		int centerXT = (int) centerT.getX();
		int centerYT = (int) centerT.getY();
		JViewport viewport = scrollPane.getViewport();
		Rectangle viewRect = viewport.getViewRect();
		if (!viewRect.contains(centerXT, centerYT)) {
			Dimension size = viewport.getExtentSize();
			int offsetX = (int) (size.getWidth() / 2);
			int offsetY = (int) (size.getHeight() / 2);
			int x = centerXT - offsetX;
			int y = centerYT - offsetY;
			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
			viewport.setViewPosition(new Point(x, y));
			validate();
		}
		// repaint();
	}

	public void scrollToCenterPoint(GrappaPoint pt) {
		// GrappaNexus nexus = element.getGrappaNexus();
		// Rectangle bounds = nexus.getBounds();
		double centerX = pt.x; // bounds.getCenterX();
		double centerY = pt.y; // bounds.getCenterY();
		Point2D center = new Point2D.Double(centerX, centerY);
		AffineTransform transform = itsGrappaPanel.getTransform();
		Point2D centerT = transform.transform(center, null);
		int centerXT = (int) centerT.getX();
		int centerYT = (int) centerT.getY();
		JViewport viewport = scrollPane.getViewport();
		Rectangle viewRect = viewport.getViewRect();
		// Dimension yy=itsGrappaPanel.getSize();
		Dimension size = viewport.getExtentSize();
		int offsetX = (int) (size.getWidth() / 2);
		int offsetY = (int) (size.getHeight() / 2);
		int x = centerXT - offsetX;
		int y = centerYT - offsetY;
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		viewport.setViewPosition(new Point(x, y));
		validate();
		// Dimension zz=itsGrappaPanel.getSize();
	}

	// basic selections

	public void select(Element element) {
		if (graphIsShowing() && !itsSelection.contains(element)) {
			itsSelection.addElement(element);
			GrappaSupport.setHighlight(element, SELECTION_MASK, HIGHLIGHT_ON);
			itsGrappaGraph.repaint();
		}
	}

	public void deselect(Element element) {
		if (graphIsShowing() && itsSelection.contains(element)) {
			itsSelection.removeElement(element);
			GrappaSupport.setHighlight(element, SELECTION_MASK, HIGHLIGHT_OFF);
			itsGrappaGraph.repaint();
		}
	}

	public void toggleSelection(Element element) {
		if (graphIsShowing() && itsSelection.contains(element)) {
			itsSelection.removeElement(element);
			GrappaSupport.setHighlight(element, SELECTION_MASK, HIGHLIGHT_ON);
		} else {
			itsSelection.addElement(element);
			GrappaSupport.setHighlight(element, SELECTION_MASK, HIGHLIGHT_OFF);
		}
		itsGrappaGraph.repaint();
	}

	public void deselectAll() {
		if (graphIsShowing()) {
			for (Iterator elementIterator = itsSelection.iterator(); elementIterator
					.hasNext();) {
				Element element = (Element) elementIterator.next();
				GrappaSupport.setHighlight(element, SELECTION_MASK,
						HIGHLIGHT_OFF);
			}
			itsSelection.removeAllElements();
		}
	}

}
