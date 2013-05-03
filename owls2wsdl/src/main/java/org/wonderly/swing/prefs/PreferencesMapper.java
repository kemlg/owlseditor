package org.wonderly.swing.prefs;

import java.util.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import org.wonderly.swing.prefs.man.*;
import org.wonderly.swing.*;

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
 *  This class maps {@link java.util.prefs.Preferences} entries to
 *  values controlled by the generic type <code>T</code>,
 *  utilizing instances of {@link PrefManager}.  The
 *  use of this class is simply:
 *<pre>
   Preferences pr = Preferences.getUserNodeForClass( getClass() );
   PreferencesMapper<MyType> pm = new PreferencesMapper<MyType>(pr);
   MyType myobj = ...
   pm.map( new PrefsManager<MyType>() {
	public boolean prepare( MyType comp ) {
		// If object is ready to commit...
		return true;
	}
	public boolean commit( MyType comp ) {
		// If object commit occured
		pr.put("myobj.value", comp.getValue() );
	}
	public void setValueIn( MyType comp ) {
		comp.setValue( pr.get("myobj.value", "default") );
	}
   }, myobj );
   if( userOkayed ) {
   	pm.commit();
 	}
 *</pre>
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class PreferencesMapper<T> {
	/**  The component to PrefManager map */
	protected Hashtable<T,PrefManager<T>> comps =
		new Hashtable<T,PrefManager<T>>();
	/** The preferences node we are anchored at */
	protected Preferences pr;
	/** The logging instance */
	protected Logger log;
	/** The flag for causing fail on commit() to raise an IllegalStateException */
	protected boolean failOnCommitFail = false;
	
//	private Vector<ChangeListener>listen = new Vector<ChangeListener>();
//	protected boolean dirty;
//	
//	public void addChangeListener( ChangeListener lis ) {
//		if( listen.contains(lis) == false )
//			listen.addElement(lis);
//	}
//	
//	public void removeChangeListener( ChangeListener lis ) {
//		listen.removeElement(lis);
//	}
//
//	public boolean isDirty() {
//		return dirty;
//	}
//
//	protected void setDirty( T comp ) {
//		ChangeEvent ev = new ChangeEvent(comp);
//		for( int i = 0; i < listen.size(); ++i ) {
//			listen.stateChanged(ev);
//		}
//	}

	/**
	 *  Set this to true to cause a commit failure to
	 *  raise an IllegalStateException.
	 */
	public void setFailOnCommitFail( boolean how ) {
		failOnCommitFail = how;
	}

	/**
	 *  Construct an instance anchored at the
	 *  indicated preferences node
	 *  @param node the Preferences node to anchor the
	 *    tree of preferences under
	 */
	public PreferencesMapper(Preferences node) {
		pr = node;
		String str = getClass().getName();
		int idx = str.lastIndexOf('.');
		String pkg = str.substring( 0, idx-1 );
		log = Logger.getLogger( pkg );
	}
	
	/**
	 *  Commit all PrefManager's contents.
	 *  This method will call PrefManager.prepare() on
	 *  all components and return false if any prepare
	 *  fails.  If none fail, it will then call 
	 *  PrefManager.commit() or all PrefManager instances
	 *  registered.  If any fails, and failOnCommit has
	 *  been set to true, an exception will be raised.
	 *  Otherwise, any commit failure will be ignored and
	 *  true will be returned.
	 *  @return false if any prepare() fails, otherwise true.
	 *  @exception IllegalStateException if setFailOnCommit(true)
	 *	has been called and a commit() call fails.
	 */
	public boolean commit() throws IllegalStateException {
		Enumeration<T> e = comps.keys();
		log.fine("Committing values to Preferences");
		while( e.hasMoreElements() ) {
			T cm = e.nextElement();
			PrefManager<T> pm = comps.get(cm);
			if( pm.prepare(cm) == false ) {
				log.finer(cm+" failed prepare through "+pm );
				return false;
			}
		}
		log.fine("Prepare of preferences succeeded, committing values");
		e = comps.keys();
		while( e.hasMoreElements() ) {
			T cm = e.nextElement();
			PrefManager<T> pm = comps.get(cm);
			if( pm.commit(cm) == false ) {
				log.finer(cm+" failed commit through "+pm );
				if( failOnCommitFail )
					throw new IllegalStateException("commit failed, after prepare okay for: "+pm );
			}
		}
		return true;
	}

	/**
	 *  Provide the complete PrefManager to associate with
	 *  the passed component.  The implementation of PrefManager
	 *  passed is responsible for managing all state.  It needs to
	 *  know how to store the data etc.  Thus, it might not use
	 *  the Preferences mechanism at all, and instead if might store
	 *  into a database, a disk file etc.
	 *
	 *  @param man the manager to use for the component.
	 *  @param comp the checkbox menu item instance to map to
	 */
	public void map( PrefManager<T> man, T comp ) {
		comps.put( comp, man );
		man.setValueIn( comp );
	}

	/**
	 *  Get the preferences node that this instance is
	 *  mapped to.
	 */
	public Preferences getPreferencesNode() {
		return pr;
	}

	/**
	 *  Walk any remaining nodes in the preferences path
	 *  to get to the base node where the final component
	 *  of the path is rooted.
	 */
	protected Preferences nodeFor( String pref ) {
		if( pref.indexOf("/") == -1 )
			return pr;
		Preferences pn = pr;
		String arr[] = pref.split("/");
		for( int i = 0; i < arr.length -1; ++i ) {
//			System.out.println("step down to: \""+arr[i]+"\"" );
			pn = pn.node(arr[i].replace(' ','_'));
		}
		return pn;
	}

	/**
	 *  Get the last component value of the preferences
	 *  tree.  This value is used as the place to
	 *  anchor/store the preference(s) of an entry
	 *  stored in the map herein.
	 *  @param pref the Perferences path to use
	 */
	protected String lastComponent( String pref ) {
		if( pref.indexOf("/") == -1 )
			return pref;
		String arr[] = pref.split("/");
		return arr[arr.length-1];
	}
}