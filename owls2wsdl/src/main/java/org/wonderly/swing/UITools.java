/*
 * UITools.java
 *
 * Created on August 30, 2007, 5:22 PM
 *
 * @author Mike
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wonderly.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class UITools {
	
	public static final int YES_OPTION = JOptionPane.YES_OPTION;
	public static final int NO_OPTION = JOptionPane.NO_OPTION;
	public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	
	private static Logger log = Logger.getLogger(UITools.class.getName());
	
	private static Cursor waitCursor;
	private static Cursor defCursor;
	
	private UITools() {
	}
	
	static {
		waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}
	
	public static void setWaitCursor(final Component comp) {
		
		Runnable r = null;
		
		r = new Runnable() {
			public void run() {
				setComponentCursor(comp, waitCursor);
			}
		};
		
		runInSwing(r);
		
	}
	
	public static void setDefCursor(final Component comp) {
		
		Runnable r = null;
		
		r = new Runnable() {
			public void run() {
				setComponentCursor(comp, defCursor);
			}
		};
		
		runInSwing(r);
		
	}
	
	private static void setComponentCursor(final Component comp, final Cursor cursor) {
		
		if (comp instanceof Container) {
			Component[] comps = ((Container)comp).getComponents();
			for (Component c : comps) {
				setComponentCursor(c, cursor);
			}
		}
		comp.setCursor(cursor);
		
	}
	
	public static File browseForDirectory(final Component comp) { return browseForDirectory(comp, null); }
	public static File browseForDirectory(final Component comp, final String initialDirectory) {
		
		setWaitCursor(comp);
		
		final JFileChooser chooser;
		
		if (initialDirectory == null) {
			chooser = new JFileChooser();	
		} else {
			chooser = new JFileChooser(initialDirectory);
		}
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		SyncThread<File,Object> exec = new SyncThread<File,Object>() {
			public void setup() {
				setDefCursor(comp);
				int rsp = chooser.showOpenDialog(comp);
				if (rsp == JFileChooser.APPROVE_OPTION) {
					setValue(chooser.getSelectedFile());
				}
			}
		};
		
		exec.block();
		return exec.getValue();
		
	}
			
	public static File browseForFile(final Component comp, final boolean save) { return browseForFile(comp, null, save); }
	public static File browseForFile(final Component comp) { return browseForFile(comp, null, false); }
	public static File browseForFile(final Component comp, final String initialDirectory, final boolean save) {
		
		setWaitCursor(comp);
		
		final JFileChooser chooser;
		
		if (initialDirectory == null) {
			chooser = new JFileChooser();	
		} else {
			chooser = new JFileChooser(initialDirectory);
		}
		
		SyncThread<File,Object> exec = new SyncThread<File,Object>() {
			public void setup() {
				setDefCursor(comp);
				int rsp = 0;
				if (save) {
					rsp = chooser.showSaveDialog(comp);
				} else {
					rsp = chooser.showOpenDialog(comp);
				}
				if (rsp == JFileChooser.APPROVE_OPTION) {
					setValue(chooser.getSelectedFile());
				}
			}
		};
		
		exec.block();
		return exec.getValue();
		
	}
	
	public static Window getParentWindow(Component comp) {
		
		if (comp == null) {
			return null;
		} else if (comp instanceof Window) {
			return (Window)comp;
		} else if (comp instanceof Frame) {
			return (Frame)comp;
		} else if (comp instanceof Dialog) {
			return (Dialog)comp;
		}
		
		return getParentWindow(comp.getParent());
		
	}
	
	public static JDialog getParentDialog(Component comp) {
		
		if (comp == null) {
			return null;
		} else if (comp instanceof JDialog) {
			return (JDialog)comp;
		}
		
		return getParentDialog(comp.getParent());
		
	}
	
	public static JFrame getParentFrame(Component comp) {
		
		if (comp == null) {
			return null;
		} else if (comp instanceof JFrame) {
			return (JFrame)comp;
		}
		
		return getParentFrame(comp.getParent());
		
	}
	
	public static JButton createButton(String text, final Runnable r) {
		
		JButton btn = new JButton(text);
		if (r != null) {
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					runInContext(r);
				}
			});
		}
		return btn;
		
	}
	
	public static JMenuItem createMenuItem(String text, final Runnable r) {
		
		JMenuItem item = new JMenuItem(text);
		if (r != null) {
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					runInContext(r);
				}
			});
		}
		return item;
		
	}
	
	public static Action createAction(String title, final Runnable r) { return createAction(title, r, UITools.class, true); }
	public static Action createAction(String title, final Runnable r, Class contextClass, boolean swing) {
		
		Action act;
		final Runnable runnable = new Runnable() {
			public void run() {
				runInContext(r);
			}
		};
		
		if (swing) {
			act = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					runInSwing(runnable);
				}
			};
		} else {
			act = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					runnable.run();
				}
			};
		}
		
		act.putValue(Action.NAME, title);
		return act;
		
	}
	
	public static boolean confirmAction(final Component parent, final String prompt) { return confirmAction(parent, prompt, "Are You Sure?"); }
	public static boolean confirmAction(final Component parent, final String prompt, final String title) {
		
		SyncThread<Boolean,Object> exec = new SyncThread<Boolean,Object>() {
			public void setup() {	
				int rtn = JOptionPane.showConfirmDialog(parent, prompt, title, JOptionPane.YES_NO_OPTION);
				setValue(rtn == JOptionPane.YES_OPTION);
			}
		};
		
		exec.block();
		return exec.getValue();

	}
	
	public static int promptYesNoCancel(final Component parent, final String prompt) { return promptYesNoCancel(parent, prompt, "Are You Sure?"); }
	public static int promptYesNoCancel(final Component parent, final String prompt, final String title) {

		SyncThread<Integer,Object> exec = new SyncThread<Integer,Object>() {
			public void setup() {
				int rtn = JOptionPane.showConfirmDialog(parent, prompt, title, JOptionPane.YES_NO_CANCEL_OPTION);
				setValue(rtn);
			}
		};

		exec.block();
		return exec.getValue();

	}
	
	public static void showWindow(final Window window) { showWindow(window, null); }
	public static void showWindow(final Window window, final Component centerComp) {
		
		runInSwing(new Runnable() {
			public void run() {
				window.setLocationRelativeTo(centerComp);
				window.setVisible(true);
			}
		});
		
	}
	
	public static void showInformationMessage(final Component parent, final String message) { showInformationMessage(parent, message, "Information"); }	
	public static void showInformationMessage(final Component parent, final String message, final String title) {
		showMessageBox(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void showWarningMessage(final Component parent, final String message) { showWarningMessage(parent, message, "Warning"); }
	public static void showWarningMessage(final Component parent, final String message, final String title) {
		showMessageBox(parent, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void showErrorMessage(final Component parent, final String message) { showErrorMessage(parent, message, "Error"); }
	public static void showErrorMessage(final Component parent, final String message, final String title) {
		showMessageBox(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showMessageBox(final Component parent, final String message) { showMessageBox(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE); }
	public static void showMessageBox(final Component parent, final String message, final String title, final int type) {
		
		runInSwing(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(parent, message, title, type);
			}
		});
		
	}
	
	public static void reportException(Throwable ex) { reportException(null, null, ex, false); }
	public static void reportException(String method, Throwable ex) { reportException(null, method, ex, false); }
	public static void reportException(Component parent, Throwable ex) { reportException(parent, null, ex, false); }
	public static void reportException(Component parent, String method, Throwable ex) { reportException(parent, method, ex, false); }
	public static void reportException(final Component parent, final String method, final Throwable ex, final boolean modal) {
		
		if (method == null) {
			showErrorMessage(parent, ex.getClass().getName()+System.getProperty("line.separator")+ex.getMessage());
		} else {
			showErrorMessage(parent, ex.getClass().getName()+" occurred in method "+method+System.getProperty("line.separator")+ex.getMessage());
		}
		
	}
	
	public static List<Component> getAllComponents(Container cont) {
		
		List<Component> rtn = null;
		
		if (cont != null && cont.getComponentCount() > 0) {
			rtn = new ArrayList<Component>();
			for (Component comp : cont.getComponents()) {
				if (comp instanceof Container) {
					List<Component> comps = getAllComponents((Container)comp);
					if (comps != null) {
						rtn.addAll(comps);
					}
				}
				rtn.add(comp);
			}
		}
		
		return rtn;
		
	}
	
	public static void runInContext(Runnable r) { runInContext(r, UITools.class); }
	public static void runInContext(Runnable r, Class contextClass) {
		
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(contextClass.getClassLoader());
			r.run();
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
		
	}
	
	public static void runInSwing(final Runnable r) {
		
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception e) {
				if (e.getCause() == null) {
					throw (IllegalArgumentException)new IllegalArgumentException("Runnable target threw "+e.getClass().getName()).initCause(e);
				}
				throw (IllegalArgumentException)new IllegalArgumentException("Runnable target threw "+e.getCause().getClass().getName()).initCause(e.getCause());
			}
		}
		
	}
	
}
