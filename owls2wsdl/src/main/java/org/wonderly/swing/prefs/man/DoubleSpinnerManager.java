package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
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
 *  Manage the state of a integer Preference value with
 *  an associated JSpinner component that contains Double values.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class DoubleSpinnerManager implements PrefManager<Component> {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected double def;
	protected double val;
	
	public DoubleSpinnerManager( Preferences prNode, String prefName, double def ) {
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
		JSpinner fl = (JSpinner)comp;
		val = ((Number)fl.getValue()).doubleValue();
		return true;
	}

	public boolean commit( Component comp ) {
		pr.putDouble( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				JSpinner fl = (JSpinner)comp;
				fl.setValue( new Double( pr.getDouble( pref, def ) ) );
			}
		});
	}	
}