/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wonderly.netbeans.perforce;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author gregg
 */
public class DialogSupport {
	public static Dialog createDialog( String title, JPanel pan, boolean modal, JButton[] buts, boolean how, int bx, int by, ActionListener lis ) {
		Dialog d = null;
		DialogDescriptor dd = new DialogDescriptor( pan, title, modal, buts, buts[0], 0, null, lis );
		return DialogDisplayer.getDefault().createDialog( dd );
	}
}
