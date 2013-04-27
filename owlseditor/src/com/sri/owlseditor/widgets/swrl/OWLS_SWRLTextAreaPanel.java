package com.sri.owlseditor.widgets.swrl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.compactparser.OWLCompactParser;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtomList;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLParseException;
import edu.stanford.smi.protegex.owl.swrl.ui.code.SWRLTextArea;
import edu.stanford.smi.protegex.owl.swrl.ui.code.SWRLTextAreaPanel;
import edu.stanford.smi.protegex.owl.ui.code.OWLTextFormatter;

/**
 * A panel which can be used to edit an OWL expression in a multi-line dialog.
 * This is a modified version of SWRLTextAreaPanel.
 *
 * Changes:
 * 1) A setImp() method
 * 2) Uses a modified toolbar that doesn't have the Imp button.
 * 3) Added change listeners, and a constructor parameter to pass the slot widget,
 *    so that we can call valueChanged() when there is editing.
 * 4) A method to remove those listeners. 
 *
 * @author Holger Knublauch  <holger@smi.stanford.edu>
 * @author Daniel Elenius <elenius@csl.sri.com>
 */
public class OWLS_SWRLTextAreaPanel extends JPanel implements ModalDialog.CloseCallback {

    private OWLModel okb;
    private OWLS_SWRLSymbolPanel symbolPanel;
    private SWRLTextArea textArea;
    //private InstanceDisplay id;
    private SWRLWidget swrlWidget;
    
    private FocusListener focusListener = new FocusAdapter(){
    		public void focusLost(FocusEvent e){
    			swrlWidget.valueChanged();
    		}
    	};

    private MouseListener mouseListener = new MouseAdapter(){
    	public void mouseClicked(MouseEvent e){
    		swrlWidget.valueChanged();
    	}
    };

    public OWLS_SWRLTextAreaPanel(OWLModel okb, SWRLWidget swrlWidget) {
        this(okb, null, swrlWidget);
    }


    public OWLS_SWRLTextAreaPanel(OWLModel okb, SWRLAtomList atomList, SWRLWidget swrlWidget) {
        this.okb = okb;
        this.swrlWidget = swrlWidget;
        symbolPanel = new OWLS_SWRLSymbolPanel(okb, false, false);
        textArea = new SWRLTextArea(okb, symbolPanel) {
            protected void checkExpression(String text) throws Throwable {
                OWLCompactParser.checkClass(OWLS_SWRLTextAreaPanel.this.okb, text);
            }
        };
        
        // Make the font a little smaller
        textArea.setFont(getFont().deriveFont((float) 12));
        
        if (atomList != null) {
            String text = atomList.getBrowserText();
            textArea.setText(text);
            textArea.reformatText();
        }
        symbolPanel.setSymbolEditor(textArea);

        //id = new InstanceDisplay(okb.getProject(), false, false);
        //id.setInstance(imp);

        setLayout(new BorderLayout(0, 8));
        //add(BorderLayout.NORTH, id);
        add(BorderLayout.CENTER, new JScrollPane(textArea));
        add(BorderLayout.SOUTH, symbolPanel);
        setPreferredSize(new Dimension(300, 200));

        textArea.addFocusListener(focusListener);
        symbolPanel.addMouseListener(mouseListener);
    }

    public void removeListeners(){
    	textArea.removeFocusListener(focusListener);
        symbolPanel.removeMouseListener(mouseListener);
    }
    
    public void setAtomList(SWRLAtomList atomList){
    	//id.setInstance(imp);
    	String text = new String();
    	
    	
    	if (atomList != null){
    		text = atomList.getBrowserText();
    	}
    		
    	textArea.setText(text);
    	textArea.reformatText();
    }
    
    public boolean canClose(int result) {
        if (result == ModalDialog.OPTION_OK) {
            String uniCodeText = textArea.getText();
            if (uniCodeText.length() == 0) {
                return false;
            }
            else {
                try {
                    OWLS_SWRLParser parser = new OWLS_SWRLParser(okb);
                    parser.parse(uniCodeText);
                    return true;
                }
                catch (Exception ex) {
                    symbolPanel.displayError(ex);
                    return false;
                }
            }
        }
        else
            return true;

    }


    public SWRLAtomList getAtomList() {
        String uniCodeText = textArea.getText();
        OWLS_SWRLParser parser = new OWLS_SWRLParser(okb);

        // First parse without generating any instances
        try {
            parser.setParseOnly(true);
            parser.parse(uniCodeText);
        }
        catch (SWRLParseException e) {
        		//System.out.println("SWRL parse exception (I)");
            //System.out.println(e.getMessage());
            return null;
        }

        // If that worked, parse, generate instances, and return the result
        try {
            parser.setParseOnly(false);
            return parser.parse(uniCodeText);
        }
        catch (SWRLParseException e) {
        		//System.out.println("SWRL parse exception (II)");
        		//System.out.println(e.getMessage());
            return null;
        }
    }


    public String getResultAsString() {
        String uniCodeText = textArea.getText();
        return OWLTextFormatter.getParseableString(uniCodeText);
    }


    public static boolean showEditDialog(Component parent, OWLModel okb, SWRLImp imp) {
        SWRLTextAreaPanel panel = new SWRLTextAreaPanel(okb, imp);
        String title = "Edit SWRL Rule";
        if (ModalDialog.showDialog(parent, panel, title, ModalDialog.MODE_OK_CANCEL) == ModalDialog.OPTION_OK) {
            try {
                imp.setExpression(panel.getResultAsString());
                return true;
            }
            catch (Exception ex) {
                System.err.println("[SWRLTextAreaPanel]  Fatal error");
                ex.printStackTrace();
            }
        }
        return false;
    }
}