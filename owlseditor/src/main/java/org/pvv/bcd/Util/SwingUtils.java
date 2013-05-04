//Title:        Polaris Marketer
//Version:
//Copyright:    Copyright (c) 1998
//Author:       Bent C Dalager
//Company:
//Description:  Analyses trade routes in Polaris

package org.pvv.bcd.Util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

public class SwingUtils {

	public SwingUtils() {
	}

	public static Enumeration getAllFilesIn(File dir) {
		File[] files;
		if (dir.isDirectory()) {
			files = dir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					if (f.isDirectory())
						return false;
					return (f.getName().endsWith(".txt"));
				}
			});
			Arrays.sort(files);
		} else {
			files = new File[] { dir };
		}
		Vector vect = new Vector(files.length);
		for (int i = 0; i < files.length; ++i)
			vect.addElement(files[i]);
		return vect.elements();
	}

	public static JInternalFrame getInternalFrameForComponent(JComponent c) {
		java.awt.Container p = c;
		do {
			if (p instanceof JInternalFrame) {
				return (JInternalFrame) p;
			}
			p = p.getParent();
		} while (p != null);
		return null;
	}

}