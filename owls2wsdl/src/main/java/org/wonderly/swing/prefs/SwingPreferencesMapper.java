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
 *  This class maps {@link java.util.prefs.Preferences} entries to Components
 *  utilizing instances of {@link PrefManager}.  The
 *  use of this class is simply:
 *<pre>
   Preferences pr = Preferences.getUserNodeForClass( getClass() );
   SwingPreferencesMapper pm = new SwingPreferencesMapper(pr);
   JCheckBox sel = new JCheckBox( "Select All?" );
   JRadioButton red = new JRadioButton("Red");
   JRadioButton blue = new JRadioButton("Blue");
   ButtonGroup grp = new ButtonGroup();
   grp.add(red);
   grp.add(blue);
   JTextField val = new JTextField();
   pm.map( "selall", false, sel );
   pm.map( "red", true, red );
   pm.map( "blue", true, blue );
   pm.map( "val", "", val );
   ... Show user controls
 
   if( userOkayed ) {
   	pm.commit();
 	}
 *</pre>
 *
 *  The overrides of <code>map( String, ???, Component )</code>
 *  provide the interface to mapping components to a particular
 *  representation in the preferences structure.  The mappings
 *  provided here are simply what seems reasonable.  Subclassing
 *  this class to add more mappings is suggested for special
 *  applications where this class does not provide the correct
 *  mappings, or use the <code>public void map( PrefManager<Component>, Component)</code>
 *  override to provide your own implementation of the PrefManager for the indicated
 *  component.  A multi-select JList, or some other more complex operation
 *  can be easily implemented then.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class SwingPreferencesMapper extends PreferencesMapper<Component> {
	/**
	 *  Construct an instance anchored at the
	 *  indicated preferences node
	 *  @param node the Preferences node to anchor the
	 *    tree of preferences under
	 */
	public SwingPreferencesMapper(Preferences node) {
		super(node);
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
	public void map( PrefManager<Component> man, Component comp ) {
		comps.put( comp, man );
		man.setValueIn( comp );
	}

	/**
	 *  Map an int preferences value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int value
	 *  @param comp the text component to manage the value in
	 */
	public void map( String pref, int def, JTextComponent comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new IntTextFieldManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a String preferences value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default String value
	 *  @param comp the text component to manage the value in
	 */
	public void map( String pref, String def, JTextComponent comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new StringTextFieldManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a String preferences value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default String value
	 *  @param comp the label component to manage the value in
	 */
	public void map( String pref, String def, JLabel comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new StringTextFieldManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JComboBox selected index to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int selected index
	 *  @param comp the combo box instance to map to
	 */
	public void map( String pref, int def, JComboBox comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new ComboBoxSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JSlider position to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int position
	 *  @param comp the slider instance to map to
	 */
	public void map( String pref, int def, JSlider comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new SliderSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JTabbedPanes selected tab index to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected row
	 *  @param tabs the list instance to map to
	 */
	public void map( String pref, final int def, final JTabbedPane tabs ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+tabs+" with default: "+def );
		final Preferences pn = nodeFor(pref);
		final String ipref = lastComponent(pref);
		PrefManager<Component> pm = new PrefManager<Component>() {
			int idx;
			public boolean prepare( Component comp ) {
				idx = ((JTabbedPane)comp).getSelectedIndex();
				return true;
			}
			public boolean commit( Component comp ) {
				pn.putInt( ipref, idx );
				return true;
			}
			public void setValueIn( Component comp ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						tabs.setSelectedIndex(
							pn.getInt(ipref,def) );
					}
				});
			}
		};
		pm.setValueIn( tabs );
		comps.put( tabs, pm );
	}

	/**
	 *  Map an int JSpinner value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int selected index
	 *  @param comp the spinner instance to map to
	 */
	public void map( String pref, int def, JSpinner comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new IntSpinnerManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a double JSpinner value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default double selected index
	 *  @param comp the spinner instance to map to
	 */
	public void map( String pref, double def, JSpinner comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create  with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new DoubleSpinnerManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JSpinner selected node/row to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected node/row
	 *  @param comp the tree instance to map to
	 */
	public void map( String pref, int def, JTree comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new ListSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JTable selected row to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected row
	 *  @param comp the table instance to map to
	 */
	public void map( String pref, int def, JTable comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new ListSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JList selected row to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected row
	 *  @param comp the list instance to map to
	 */
	public void map( String pref, int def, JList comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager<Component> pm = new ListSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JToggleButton selected state to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected state
	 *  @param comp the toggle button instance to map to
	 */
	public void map( String pref, boolean def, JToggleButton comp ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+pref+
				" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		if (log.isLoggable(Level.INFO))
			log.info("prefs: "+pn+", name="+pref );
		PrefManager<Component> pm = new BooleanToggleButtonManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JMenuItem selected state to the named preference.
	 *  @param spref the path to the Preferences node to use
	 *  @param def the default selected state
	 *  @param mi the checkbox menu item instance to map to
	 */
	public void map( final String spref, final boolean def, 
				final JMenuItem mi ) {
		if (log.isLoggable(Level.FINER))
			log.finer( "Create mapping for "+spref+
				" using "+mi+" with default: "+def );
		final Preferences pn = nodeFor(spref);
		final String pref = lastComponent(spref);
		PrefManager<Component> pm = new PrefManager<Component>() {
			public boolean prepare( Component comp ) {
				return true;
			}
			public boolean commit( Component comp ) {
				pn.putBoolean( pref, mi.isSelected() );
				return true;
			}
			public void setValueIn( Component comp ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						mi.setSelected( pn.getBoolean( pref, def ) );
					}
				});
			}
		};
		pm.setValueIn( mi );
		comps.put( mi, pm );
	}

	/**
	 *  Map a JSplitPane divider location to the named preference.
	 *  @param prefn the path to the Preferences node to use
	 *  @param defloc the default divider location
	 *  @param spl the JSplitPane menu item instance to map to
	 */
	public void map( final String prefn, final int defloc, 
			final JSplitPane spl ) {

		final Preferences pn = nodeFor( prefn );
		final String pref = lastComponent(prefn);

		PrefManager<Component> pm = new PrefManager<Component>() {
			int val;
			public boolean prepare( Component comp ) {
				return true;
			}
			public boolean commit( Component comp ) {
				val = spl.getDividerLocation();
				pn.putInt( pref, val );
				return true;
			}

			// Just initialize on startup
			public void setValueIn( Component comp ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						// Get current value or use default
						double nloc = pn.getInt( pref, defloc );
						if (log.isLoggable(Level.FINE))
							log.fine("setDivider("+
								prefn+", "+defloc+", "+nloc+")");
						// Set location
						spl.setDividerLocation( (int)nloc );
						spl.revalidate();
						spl.repaint();
					}
				});
			}
		};

		pm.setValueIn( spl );
		comps.put( spl, pm );
		if (log.isLoggable(Level.FINER))
			log.finer("locating split: " + defloc );
	}

	/**
	 *  Map a Window location and size to the named preference.
	 *  The visibility of the Window is also saved.
	 *  @param pref the path to the Preferences node to use
	 *  @param loc the default location
	 *  @param sz the default location
	 *  @param w the Window instance to map to
	 */
	public void map( String pref, Point loc, Dimension sz, Window w ) {
		Preferences pn = nodeFor( pref );
		pref = lastComponent(pref);
		PrefManager<Component> pm = new WindowManager( pn, pref, loc, sz );
		pm.setValueIn( w );
		comps.put( w, pm );
		if (log.isLoggable(Level.FINER))
			log.finer("locating window: "+w+" to "+loc+" @ "+sz );
	}

	/**
	 *  Map a Window location and size to the named preference.
	 *  The default location and size are the windows current
	 *  values for getLocation() and getSize().
	 *  @param pref the path to the Preferences node to use
	 *  @param w the Window instance to map to
	 *  @see #map(String,Point,Dimension,Window)
	 */
	public void map( String pref, Window w ) {
		map( pref, w.getLocation(), w.getSize(), w );
	}


	/**
	 *  Map a JComponent location and size to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param loc the default location
	 *  @param sz the default location
	 *  @param w the component instance to map to
	 *  This method is primarily used for
	 *  JInternalFrame.  This method also saves the isIcon()
	 *  and isMaximum() properties.
	 *  Also, since JFrame and JDialog are not JComponents, we
	 *  have to have separate methods to handle these and the
	 *  Window version of the map() method is for that.
	 */
	public void map( String pref, Point loc, Dimension sz, JComponent w ) {
		Preferences pn = nodeFor( pref );
		pref = lastComponent(pref);
		PrefManager<Component> pm = new WindowManager( pn, pref, loc, sz );
		pm.setValueIn( w );
		comps.put( w, pm );
		if (log.isLoggable(Level.FINER))
			log.finer("locating component: "+w+" to "+loc+" @ "+sz );
	}

	/**
	 *  Map a JComponent location and size to the named preference.
	 *  The default location and size are taken from the
	 *  component's current values.
	 *  @param pref the path to the Preferences node to use
	 *  @param w the component instance to map to
	 *  @see #map(String,Point,Dimension,JComponent)
	 */
	public void map( String pref, JComponent w ) {
		map( pref, w.getLocation(), w.getSize(), w );
	}
}