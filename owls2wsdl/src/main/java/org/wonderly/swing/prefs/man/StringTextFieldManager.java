package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import org.wonderly.swing.UITools;
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
 *  Manage the state of a String Preference value with
 *  an associated JTextComponent component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class StringTextFieldManager implements PrefManager<Component> {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected String def;
	protected String val;

	public StringTextFieldManager( Preferences prNode, String prefName, String def ) {
		// Chop of classname and last package component for logger.
		String p = getClass().getName();
		int idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		log = Logger.getLogger( p+"."+prefName.replace('/','.') );

		this.def = def;
		pr = prNode;
		pref = prefName;
	}

	public boolean prepare( Component comp ) {
		if( comp instanceof JTextComponent ) {
			JTextComponent fl = (JTextComponent)comp;
			val = fl.getText();
		} else if( comp instanceof JLabel ) {
			JLabel fl = (JLabel)comp;
			val = fl.getText();
		} else {
			return false;
		}
		return true;
	}

	public boolean commit( Component comp ) {
		pr.put( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		UITools.runInSwing( new Runnable() {
			public void run() {
				if( comp instanceof JTextComponent ) {
					JTextComponent fl = (JTextComponent)comp;
					fl.setText( pr.get( pref, def ) );
				} else if( comp instanceof JLabel ) {
					JLabel fl = (JLabel)comp;
					fl.setText( pr.get( pref, def ) );
				}
			}
		});
	}	
}