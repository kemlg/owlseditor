package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;

/**
<pre>
Copyright (c) 1997-2006, Gregg Wonderly
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * The name of the author may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>
 *  Manage the state of an integer valued Preference
 *  that is the index of the current selected item in
 *  an associated JList component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">
 *		gregg.wonderly@pobox.com</a>
 */
public class ListSelectionManager implements PrefManager<Component> {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected int def;
	protected int val;

	public static void main( String args[] ) {
		ListSelectionManager lm = 
			new ListSelectionManager( Preferences.userNodeForPackage( 
			ListSelectionManager.class), "pref1", 2 );
		lm.log.info("Check logger");
	}

	public ListSelectionManager( Preferences prNode,
			String prefName, int def ) {
		// Chop of classname and last package component for logger.
		String p = getClass().getName();
		int idx = p.lastIndexOf(".");
		p = p.substring( 0, idx);
		idx = p.lastIndexOf(".");
		p = p.substring( 0, idx)+"."+prefName.replace('/','.');
		log = Logger.getLogger( p );

		this.def = def;
		pr = prNode;
		pref = prefName;
	}

	public boolean prepare( Component comp ) {
		if( comp instanceof JList ) {
			JList fl = (JList)comp;
			val = fl.getSelectedIndex();
		} else if( comp instanceof JTable ) {
			JTable fl = (JTable)comp;
			val = fl.getSelectedRow();
		} else if( comp instanceof JTree ) {
			JTree fl = (JTree)comp;
			int r[] = fl.getSelectionRows();
			if( r != null && r.length > 0 )
				val = r[0];
		} else {
			return false;
		}
		return true;
	}

	public boolean commit( Component comp ) {
		log.info("Commit with val: "+val);
		pr.putInt( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				if( comp instanceof JList ) {
					JList fl = (JList)comp;
					fl.setSelectedIndex( pr.getInt( pref, def ) );
				} else if( comp instanceof JTable ) {
					JTable fl = (JTable)comp;
					int idx = pr.getInt( pref, def );
					log.info("set value in("+def+"): "+idx );
					if( fl.getRowCount() > idx && idx >= 0 )
						fl.addRowSelectionInterval( idx, idx );
				} else if( comp instanceof JTree ) {
					JTree fl = (JTree)comp;
					int idx = pr.getInt( pref, def );
					fl.setSelectionRow( idx );
				}
			}
		});
	}	
}