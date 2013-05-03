package org.wonderly.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import org.wonderly.awt.Packer;
import java.util.logging.*;
import javax.swing.text.JTextComponent;
import org.wonderly.awt.*;

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
 *  This class provides a mechanism for associating 
 *  the enabled state
 *  of many components with a JToggleButton implementation.
 *  The typical use is something like the following.
 *
 *<pre>
JPanel p = new JPanel(p);
Packer pk = new Packer(p);
<b>JCheckBox cb = new JCheckBox("Use File?");</b>
<b>ModalComponent mc = new ModalComponent(cb);</b>
pk.pack( cb ).gridx(0).gridy(0).fillx();

<b>JPanel fp = new JPanel();</b>
Packer fpk = new Packer( fp );
pk.pack( fp ).gridx(0).gridy(1).fillx();
<b>mc.add( fp );</b>
fp.setBorder( 
	BorderFactory.createTitledBorder("File Name") );
<b>JTextField fileName = new JTextField();</b>
<b>mc.add( fileName);</b>

 *</pre>
 *  The <b>bold</b> lines are the ones that must be 
 *  there the others
 *  are a specific type of layout for the components.
 *
 */
public class ModalComponent {
	protected Hashtable<ModalComponent,
		Vector<ModalComponent>> rels =
			new Hashtable<ModalComponent,
				Vector<ModalComponent>>();
	protected Vector<Component> enabs = 
		new Vector<Component>();
	protected int enables[];
	protected Component modal;
	protected Logger log = Logger.getLogger(
		"org.wonderly.swing.modal");

	public String toString() {
		if( modal instanceof JToggleButton )
			return ((JToggleButton)modal).getText();
		return modal.getClass().getName();
	}

	public boolean equals( Object obj ) {
		if( obj instanceof ModalComponent == false )
			return false;
		return modal.equals( ((ModalComponent)obj).modal );
	}

	public int hashCode() {
		return modal.hashCode();
	}

	public static void main( String args[] ) {
		JFrame f = new JFrame( "Testing");
		Packer pk = new Packer(f.getContentPane() );
		
		ButtonGroup grp = new ButtonGroup();
		JRadioButton b1 = new JRadioButton("Button 1");
		JRadioButton b2 = new JRadioButton("Button 2");
		JRadioButton b3 = new JRadioButton("Button 3");
		b1.setName(b1.getText());
		b2.setName(b2.getText());
		b3.setName(b3.getText());

		grp.add(b1);
		grp.add(b2);
		grp.add(b3);
		
		pk.pack( b1 ).gridx(0).gridy(0).fillx().east();
		pk.pack( b2 ).gridx(0).gridy(1).fillx().east();
		pk.pack( b3 ).gridx(0).gridy(2).fillx().east();

		JTextField f1 = new JTextField("Field data1");
		f1.setName( f1.getText() );

		JTextField f2 = new JTextField("Field data1");
		f2.setName( f2.getText() );

		JButton bt1 = new JButton("Settings");
		bt1.setName( bt1.getText() );

		pk.pack( f1 ).gridx(1).gridy(0).fillx();
		pk.pack( bt1 ).gridx(2).gridy(0);
		
		pk.pack( f2 ).gridx(1).gridy(1).fillx();

		ModalComponent mc1 = new ModalComponent(b1);
		ModalComponent mc2 = new ModalComponent(b2);
		ModalComponent mc3 = new ModalComponent(b3);

		mc1.add( f1 );
		mc1.add( bt1 );
		mc2.add(f2);
		
		mc1.relate(mc2);
		mc1.relate(mc3);
		mc2.relate(mc1);
		mc2.relate(mc3);
		mc3.relate(mc1);
		mc3.relate(mc2);

		b2.setSelected(true);
		mc1.configure();
		mc2.configure();
		mc3.configure();

		f.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent ev ) {
				System.exit(1);
			}
		});
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	/**
	 *  This method adds a related Modal Component whose state depends on
	 *  the state of this component.
	 */
	public void relate( ModalComponent mc ) {
		Vector<ModalComponent> v = rels.get(this);
		if( v == null ) {
			v = new Vector<ModalComponent>();
			rels.put(this,v);
		}
		v.addElement(mc);		
	}

	/**
 	 *  Create the modal control and attach an
 	 *  action listener
	 *  to <code>box</code> to call configure each 
	 *  time the 
	 *  an ActionEvent fires
	 *  @param box the toggle button to act on.
	 */
	public ModalComponent( final JToggleButton box ) {
		modal = box;
		box.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				log.fine("toggle actionEvent: "+ev);
				configure( box.isSelected() );
			}
		});
	}

	public ModalComponent( final JComboBox box,
				final int enabled[] ) {
		modal = box;
		enables = enabled;
		box.addActionListener( new ActionListener() {
			public void actionPerformed( 
							ActionEvent ev ) {
				if( log.isLoggable( Level.FINER ) ) {
					log.fine("combo actionEvent: sel("+box.getSelectedIndex()+"): "+ev);
				}
				configure( box.getSelectedIndex(),
					enabled );
			}
		});
	}

	/**
	 *   @param box the list whose elements are monitored
	 *   @param enabled the indexes for this JList
	 *		selections where components become enabled.
	 */
	public ModalComponent( final JList box,
			final int enabled[] ) {
		modal = box;
		enables = enabled;
		box.addListSelectionListener(
				new ListSelectionListener() {
			public void valueChanged( 
					ListSelectionEvent ev ) {
				if( ev.getValueIsAdjusting() )
					return;
				log.fine("list selection Event: "+ev);
				configure( box.getSelectedIndex(),
					enabled );
			}
		});
	}

	/**
	 *   @param tbl the table whose selection status is monitored
	 *   @param enabled the indexes for this JList
	 *		selections where components become enabled.
	 */
	public ModalComponent( final JTable tbl,
			final int enabled[] ) {
		modal = tbl;
		enables = enabled;
		tbl.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
			public void valueChanged( 
					ListSelectionEvent ev ) {
				log.fine("Table selection event: "+ev );
				if( ev.getValueIsAdjusting() )
					return;
				log.fine("table selection Event: "+ev);
				configure( tableSel((JTable)tbl), enabled );
			}
		});
	}

	private void configure( int idx, int enabled[] ) {
		// Handle case of any selection causes enable
		if( enabled == null ) {
			log.fine(modal+": all allowed, idx="+idx);
			configure( idx != -1 );
			return;
		}

		boolean sel = false;
//		int idx = ((JComboBox)modal).getSelectedIndex();
		for( int i = 0; i < enabled.length; ++i ) {
			if( idx == enabled[i] ) {
				sel = modal.isEnabled();
				break;
			}
		}
		configure(sel);
	}

	/**
	 *  This method is called by the ActionListener
	 *  established
	 *  at the time this instance is constructed. 
	 *  Normally, this
	 *  method needs to be called after all components 
	 *  are added
	 *  to configure them based on the current state of the
	 *  controlling JToggleButton.
	 */
	public void configure() {
		int[]none = new int[]{};
		if( modal instanceof JComboBox ) {
			configure( 
				((JComboBox)modal).getSelectedIndex(),
					modal.isEnabled() ? enables : none );
			return;
		} else if( modal instanceof JList ) {
			configure( ((JList)modal).getSelectedIndex(),
					modal.isEnabled() ? enables : none );
			return;
		} else if( modal instanceof JTable ) {
			log.fine("Configuring table: "+modal+", cursel: "+tableSel((JTable)modal) );
			configure( tableSel((JTable)modal),	modal.isEnabled() ? enables : none );
			return;
		}
		log.fine("configuring togglebutton \""+(((JToggleButton)modal).getName())+
			"\" sel="+((JToggleButton)modal).isSelected() );
		configure( new HashMap<ModalComponent, ModalComponent>(), 
			modal.isEnabled() ? ((JToggleButton)modal).isSelected() : false );
	}

	private int tableSel( JTable tbl ) {
		int s0 = ((JTable)modal).getSelectionModel().getMinSelectionIndex();
		int s1 = ((JTable)modal).getSelectionModel().getMaxSelectionIndex();
		log.fine("Table min: "+s0+", max: "+s1 );
		return s0 >= 0 ? s0 : -1;
	}

	private void configure( boolean how ) {
		// Do the initial configure call with an empty 'covered' map.
		configure( new HashMap<ModalComponent,
			ModalComponent>(), modal.isEnabled() ? how : false );
	}

	private void configure( HashMap<ModalComponent,
				ModalComponent> covered, int idx ) {
		boolean sel = enables == null || enables.length > 0;

		for( int i = 0; enables != null && i < enables.length; ++i ) {
			if( idx == enables[i] ) {
				if( log.isLoggable( Level.FINER ) ) {
					log.fine("found enables["+i+"]==("+enables[i]+")=="+
						idx+", modal enabled: "+modal.isEnabled());
				}
				sel = modal.isEnabled();
				break;
			}
		}
		configure( covered, sel );
	}

	private void configure( HashMap<ModalComponent,
			ModalComponent> covered, boolean sel ) {

		// Don't allow a disabled component to cause enabled dependents
		if( modal.isEnabled() == false )
			sel = false;
		log.log(Level.FINER,"configure(cover="+covered+", sel="+sel+")", new Throwable("configuring") );

		Enumeration<Component> e = enabs.elements();
		while( e.hasMoreElements() ) {
			Component comp = e.nextElement();
			log.fine("configure: "+comp.getName()+" sel="+sel );
			if( comp instanceof JTextComponent ) {
				if( log.isLoggable( Level.FINER ) ) {
					log.fine("setting editable: modal("+modal.isEnabled()+
							"), sel="+sel+" on comp="+comp );
				}
				((JTextComponent)comp).setEditable(sel);
			} else {
				if( log.isLoggable( Level.FINER ) ) {
					log.fine("setting enabled: "+sel+" on "+comp );
				}
				comp.setEnabled( sel );
			}
		}

		covered.put( this, this );
		log.finer( "covered: "+covered );

		Vector<ModalComponent> v = rels.get(this);
		log.finer( "relate: "+v );
		if( v == null || v.size() == 0 )
			return;

		for( int i = 0; i < v.size(); ++i ) {
			ModalComponent mc = v.elementAt(i);
			log.finest( "mc: "+mc+
				", covered? "+covered.get(mc) );

			if( covered.get( mc ) != null )
				continue;

			log.finer("checking: "+((JComponent)mc.modal).getName() );
			if( mc.modal instanceof JToggleButton ) {
				mc.configure( covered,
					((JToggleButton)mc.modal
					).isSelected() );
			} else if( mc.modal instanceof JComboBox ) {
				mc.configure( covered, 
					((JComboBox)mc.modal
					).getSelectedIndex() );
			} else if( mc.modal instanceof JList ) {
				mc.configure( covered, 
					((JList)mc.modal).getSelectedIndex() );
			} else if( mc.modal instanceof JTable ) {
				mc.configure( covered, tableSel((JTable)modal) );
			} else {
				throw new IllegalArgumentException(
					"Unsupported component type: "+
					mc.modal.getClass().getName() );
			}
		}
	}

	public void add( Component comp ) {
		enabs.addElement( comp );
	}
}