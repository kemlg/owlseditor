package org.wonderly.swing.prefs.man;

import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;
import java.util.logging.*;

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
 *  Manage the location and size of a Component as
 *  a set of preferences.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class WindowManager implements PrefManager<Component> {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected Point defLoc;
	protected Point valLoc;
	protected Dimension defSz;
	protected Dimension valSz;
	protected boolean ismax;
	protected boolean ismin;
	protected boolean isvis;

	public WindowManager( Preferences prNode,
			String prefName, Point loc, Dimension sz ) {
		// Chop off classname and last package component for logger.
		String p = getClass().getName();
		int idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		log = Logger.getLogger( p+"."+prefName.replace('/','.') );

		this.defLoc = loc;
		this.defSz = sz;
		pr = prNode.node(prefName);
		pref = prefName;
	}

	public boolean prepare( Component fl ) {
		valLoc = fl.getLocation();
		valSz = fl.getSize();
		isvis = fl.isVisible();
		if( fl instanceof JInternalFrame ) {
			JInternalFrame f = (JInternalFrame)fl;
			ismin = f.isIcon();
			ismax = f.isMaximum();
		}
		return true;
	}

	public boolean commit( Component comp ) {
		log.fine("position window: "+pref+"("+valLoc+") with dim="+valSz );

		Preferences pn = pr.node("location");
		pn.putInt( "x", valLoc.x );
		pn.putInt( "y", valLoc.y );

		pn = pr.node("size");
		pn.putInt( "w", valSz.width );
		pn.putInt( "h", valSz.height );

		pn = pr.node("info");
		pn.putBoolean( "vis", isvis );
		pn.putBoolean( "max", ismax );
		pn.putBoolean( "min", ismin );

		return true;
	}

	public void setValueIn( final Component fl ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				try {
					Preferences pn = pr.node("location");
					fl.setLocation(
						pn.getInt("x",fl.getLocation().x),
						pn.getInt("y",fl.getLocation().y ) );
					pn = pr.node("size");
					fl.setSize(
						pn.getInt("w", fl.getSize().width), 
						pn.getInt("h", fl.getSize().height ) );
					log.fine("Position("+pref+") @ "+
						fl.getLocation()+" with "+fl.getSize() );
					pn = pr.node("info");
					fl.setVisible( pn.getBoolean("vis", true ) );
					if( fl instanceof JInternalFrame ) {
						JInternalFrame f = (JInternalFrame)fl;
						f.setIcon( pn.getBoolean("min", false ) );
						f.setMaximum( pn.getBoolean("max", false ) );
					}
				} catch( Exception ex ) {
					log.log( Level.INFO, ex.toString(), ex );
				}
			}
		});
	}	
}