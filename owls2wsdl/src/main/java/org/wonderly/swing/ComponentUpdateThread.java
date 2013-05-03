package org.wonderly.swing;

import java.awt.*;
import java.awt.event.*;
import org.wonderly.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;
import javax.swing.*;
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
 *  This class is a convience class that provides the facilities of
 *  disabling and then enabling a component with the SwingWorkers
 *  <code>construct()</code> method called inbetween.  You can override
 *  <code>setup()</code> and <code>finished()</code> methods to do
 *  before and after work, just remember to call <code>super.setup()</code>
 *  and/or <code>super.finished()</code> from your override so that the
 *  enable/disable and cursor setup can be done.  These methods are left to
 *  you to call so that you can call the superclass method at the appropriate
 *  time.
 *
 *  There are a wide range of constructors provided here to try and make
 *  it easy to use this class with Action, Component and JComponent.  In
 *  most applications, there is not a mixture of these components when it
 *  comes time to manipulate a group of things.  However, there can be.
 *  The <code>setup()</code> override lets you handle this by adding any
 *  additional <code>setEnabled(false)</code> calls that you need.  The
 *  <code>finish()</code> override will let you reverse those actions and
 *  perform any other important configuration.  In particular, when you are
 *  reloading a <code>JList</code>, you may also have some list manipulation
 *  buttons that are disabled when there is not anything selected.  Here
 *  is some example code to use as a template for such.
 *<pre>
 	JList list = ...
 	Action add, del, edit;
 	new ComponentUpdateThread( new Action[] { add, del, edit } ) {
 		public void setup() {
 			super.setup();
 			list.setEnabled(false);
 			list.clearSelection();
 		}
 		public Object construct() {
 			try {
 				Vector v = remote.getData();
 				Collections.sort( v );
 				return v;
 			} catch( Exception ex ) {
 				reportException(ex);
 			}
 			return null;
 		}
 		public void finished() {
 			try {
 				Vector v = (Vector)getValue();
 				list.setListData(v);
 			} finally {
 				super.finished();
 				list.setEnabled(true);
 				edit.setEnabled(false);
 				del.setEnaled(false);
 			}
 		}
 	}.start();
 *</pre>
 *
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public abstract class ComponentUpdateThread<T> extends SwingWorker<T> {
	private static Cursor defWaitCrs = new Cursor( Cursor.WAIT_CURSOR );
	private static Cursor defDefCrs = new Cursor( Cursor.DEFAULT_CURSOR );
	private Component comp[];
	private Action act[];
	private Cursor waitCrs = defWaitCrs;
	private Cursor defCrs = defDefCrs;
	private String setupTip, finishTip;
	private volatile boolean enable;
	private volatile boolean setupSet, finishedSet;
	private volatile JDialog dlg;
	private volatile boolean dlgOpen;
	Logger log = Logger.getLogger( ComponentUpdateThread.class.getName()+"."+getClass().getName() );

	/**
	 *  Set the value of the wait cursor
	 */
	public void setWaitCursor( Cursor crs ) {
		waitCrs = crs;
	}

	/**
	 *  Set the value of the final/default cursor to leave active
	 */
	public void setDefaultCursor( Cursor crs ) {
		defCrs = crs;
	}

	/**
	 *  Get the current value of the wait Cursor
	 */
	public Cursor getWaitCursor() {
		return waitCrs;
	}

	/**
	 *  Set default enable state for component manipulation
	 */
	public void setEnabled( boolean how ) {
		enable = how;
	}
	
	/**
	 *  Get current default enable state for component manipulation
	 */
	public boolean getEnabled() {
		return enable;
	}

	/**
	 *  Get the current value of the final/default cursor.
	 */
	public Cursor getDefaultCursor() {
		return defCrs;
	}

	/**
	 *  Set the tooltip text to set on the components at setup time
	 */
	public void setSetupToolTipText( String str ) {
		setupSet = true;
		setupTip = str;
	}

	/**
	 *  Get the setup tool tip text
	 */
	public String getSetupToolTipText() {
		return setupTip;
	}

	/**
	 *  Set the tooltip text to set on the components at finished time
	 */
	public void setFinishedToolTipText( String str ) {
		finishedSet = true;
		finishTip = str;
	}
//
//	public static void main( String args[] ) {
//		JFrame f = new JFrame( "testFrame");
//		Packer pk = new Packer(f.getContentPane());
//		JPanel p = new JPanel();
//		Packer ppk = new Packer( p );
//		final JPanel q = new JPanel();
//		Packer qpk = new Packer( q );
//		qpk.pack( p ).gridx(0).gridy(0).gridh(3).fillboth();
//		final JList lst = new JList(new DefaultListModel());
//		ppk.pack( new JScrollPane(lst) ).filly().gridx(1).gridy(0);
//		for(int i =0;i < 20;++i )
//			((DefaultListModel)lst.getModel()).addElement("entry "+i );
//		final JDesktopPane dp = new JDesktopPane();
//		ppk.pack( dp ).fillboth().gridx(0).gridy(0);
//		dp.add( new JInternalFrame() );
//		new Thread() {
//			public void run() {
//				JOptionPane.showInternalConfirmDialog( dp, "Ready to continue");
//			}
//		}.start();
//		qpk.pack( new JButton("Yes") ).gridx(1).gridy(0).weightx(0).fillx();
//		qpk.pack( new JButton("No") ).gridx(1).gridy(1).weightx(0).fillx();
//		qpk.pack( new JButton("Cancel") ).gridx(1).gridy(2).weightx(0).fillx();
//		pk.pack( q ) .gridx(1).gridy(0).gridh(3).fillboth();
//		final JButton up = new JButton("Enable");
//		final JButton down = new JButton("Disable");
//		pk.pack( up ).gridx(0).gridy(0);
//		pk.pack( down ).gridx(0).gridy(1);
//		
//		up.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent ev) {
//				setCompEnabled( q, true, defDefCrs, "Now enabled" );
//			}
//		});
//		
//		down.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent ev) {
//				setCompEnabled( q, false, defWaitCrs, "Now disabled" );
//			}
//		});
//		f.pack();
//		f.setVisible(true);
//	}
	
	public static void main( String args[] ) {
		new ComponentUpdateThread( (JFrame)null, "Title Help", 
			new JButton("Line 1"), new JTextField("Line 2 is longer"),
				new JScrollPane( new JTextArea( "Then Line 3" ) ) ) {
			public Object construct() {
				try {
					Thread.sleep(40000);
				} catch( Exception ex ) {
				}
				return null;
			}
		}.start();
	}

	/**
	 *  Get the finished tool tip text
	 */
	public String getFinishedToolTipText() {
		return finishTip;
	}

	/**
	 *  This method sets the passed component tree to be either
	 *  disabled with wait cursors, or enabled with the default
	 *  cursor.
	 *
	 *  @param c component to manipulate (including all children)
	 *  @param how how to enable the component
	 *  @param doEnable whether to do setEnable(how)
	 *  @param cursor cursor to set on component, null to skip changing cursor
	 *  @param setTip if true set value of 'tip' as tooltip, if false, to set a tooltip.
	 *  @param tip tool tip to set on component, null to skip setting tooltip.
	 */
	public static void setCompEnabled( Component c, boolean how, boolean doEnable, Cursor cursor, boolean setTip, String tip ) {
		// If no component, just return
		if( c == null )
			return;
		if( doEnable )
			c.setEnabled( how );
		if( cursor != null )
			c.setCursor( cursor );
		if( setTip != false && c instanceof JComponent )
			((JComponent)c).setToolTipText( tip );
		c.repaint();
		if( c instanceof Container && c != null ) {
			Component arr[] = ((Container)c).getComponents();
			for( int i = 0; i < arr.length; ++i ) {
				try {
					if( arr[i] instanceof Component && arr[i] != null )
						setCompEnabled( (Component) arr[i], how, doEnable, cursor, setTip, tip );
				} catch( Exception ex ) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 *  This method sets the passed component tree to be either
	 *  disabled with wait cursors, or enabled with the default
	 *  cursor.
	 *
	 *  @param c component to manipulate (including all children)
	 *  @param how how to enable the component
	 *  @param doEnable whether to do setEnable(how)
	 */
	public static void setActionEnabled( Action c, boolean how, boolean doEnable ) {
		// If no Action, just return
		if( c == null )
			return;
		if( doEnable )
			c.setEnabled( how );
	}

	public static interface CancelHandler {
		public void cancelled( ComponentUpdateThread th );
	}

	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed frame.
	 */
	public ComponentUpdateThread( JFrame frm ) {
		showDialog( frm, "Working...", "Operation in Progress" );
	}

	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed frame.
	 */
	public ComponentUpdateThread( JFrame frm, String title, String... msg ) {
		showDialog( frm, (CancelHandler)null, title, msg );
	}

	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed frame.
	 */
	public ComponentUpdateThread( JFrame frm, String title, JComponent... msg ) {
		showDialog( frm, (CancelHandler)null, title, msg );
	}

	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed frame.
	 */
	public ComponentUpdateThread( JFrame frm, CancelHandler h, String title, JComponent... msg ) {
		showDialog( frm, h, title, msg );
	}

	private Component frame;
//	private String title = "Working...";
//	private String msg = "Operation in Progress";
	
	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed dialog.  Typically this is used when the whole
	 *  dialog needs to be inaccessible while work is performed.
	 *  @see SimpleProgress
	 */
	public ComponentUpdateThread( JDialog dlg ) {
		showDialog( dlg, "Working...", "Operation in Progress" );
	}

	public ComponentUpdateThread( JDialog dlg, String title, String... msg ) {
		showDialog( dlg, (CancelHandler)null, title, msg );
	}

	public ComponentUpdateThread( JDialog dlg, String title, JComponent... msg ) {
		showDialog( dlg, (CancelHandler)null, title, msg );
	}

	public ComponentUpdateThread( JDialog dlg, CancelHandler h, String title, JComponent... msg ) {
		showDialog( dlg, h, title, msg );
	}

	/**
	 *  Shows a dialog that disables access to the indicated frame
	 */
	public void showDialog( JFrame frm, String title, String... msg ) {
		frame = frm;
		openDialog(true, null, title, msg);
	}
	
	/**
	 *  Shows a dialog that disables access to the indicated dialog
	 */
	public void showDialog( JDialog dlg, String title, String... msg ) {
		frame = dlg;
		openDialog(true, null, title, msg);
	}

	/**
	 *  Shows a dialog that disables access to the indicated frame
	 */
	public void showDialog( JFrame frm, CancelHandler h, String title, String... msg ) {
		frame = frm;
		openDialog(true, h, title, msg);
	}
	
	/**
	 *  Shows a dialog that disables access to the indicated dialog
	 */
	public void showDialog( JDialog dlg, CancelHandler h, String title, String... msg ) {
		frame = dlg;
		openDialog(true, h, title, msg);
	}

	/**
	 *  Shows a dialog that disables access to the indicated frame
	 */
	public void showDialog( JFrame frm, String title, JComponent... msg ) {
		frame = frm;
		openDialog(true, null, title, msg);
	}
	
	/**
	 *  Shows a dialog that disables access to the indicated dialog
	 */
	public void showDialog( JDialog dlg, String title, JComponent... msg ) {
		frame = dlg;
		openDialog(true, null, title, msg);
	}

	/**
	 *  Shows a dialog that disables access to the indicated frame
	 */
	public void showDialog( JFrame frm, CancelHandler h, String title, JComponent... msg ) {
		frame = frm;
		openDialog(true, h, title, msg);
	}
	
	/**
	 *  Shows a dialog that disables access to the indicated dialog
	 */
	public void showDialog( JDialog dlg, CancelHandler h, String title, JComponent... msg ) {
		frame = dlg;
		openDialog(true, h, title, msg);
	}
	

	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JFrame frm, boolean block ) {
		frame = frm;
		openDialog(block, null, "Working...", "Operation in Progress" );
	}

	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JFrame frm, boolean block, String title, String... msg ) {
		frame = frm;
		openDialog(block, null, title, msg );
	}

	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JFrame frm, boolean block, String title, JComponent... msg ) {
		frame = frm;
		openDialog(block, null, title, msg );
	}

	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JFrame frm, boolean block, CancelHandler h, String title, JComponent... msg ) {
		frame = frm;
		openDialog(block, h, title, msg );
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JDialog dlg, boolean block ) {
		frame = dlg;
		openDialog(block, null, "Working...", "Operation in Progress" );
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JDialog dlg, boolean block, String title, String... msg ) {
		frame = dlg;
		openDialog(block, null, title, msg );
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JDialog dlg, boolean block, String title, JComponent... msg ) {
		frame = dlg;
		openDialog(block, null, title, msg );
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JDialog dlg, CancelHandler h, boolean block, String title, JComponent... msg ) {
		frame = dlg;
		openDialog(block, h, title, msg );
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	protected void openDialog( final boolean block, final CancelHandler h, String title, String... msg) {
		log.fine("showing "+(block?"blocking":"non-blocking")+" dialog ("+title+") with: "+
			(msg.length > 0 ? Arrays.toString(msg) : "<no messages>") );
		if( frame instanceof JDialog )
			dlg = new JDialog( (JDialog)frame, title, block );
		else
			dlg = new JDialog( (JFrame)frame, title, block );
		Packer pk = new Packer( dlg.getContentPane() );
		int y = -1;
		if( msg.length > 1 ) {
			JPanel mp = new JPanel();
			Packer mpk = new Packer( mp );
			JLabel il = new JLabel();
			Icon progIcon = new ProgressIcon(il);
			il.setIcon( progIcon );
			pk.pack( il ).gridx(0).gridy(0).inset(4,4,4,4);
			pk.pack( new JPanel() ).gridx(0).gridy(1).filly();
			pk.pack( mp ).gridx(1).gridy(0).gridh(2).fillboth().inset(4,4,4,4);
			for( String m: msg ) {
				il = new JLabel(m);
				mpk.pack( il ).gridx(0).gridy(++y).fillx();
			}
		} else {
			for( String m: msg ) {
				JLabel il = new JLabel();
				if( y == -1 ) {
					Icon progIcon = new ProgressIcon(il);
					il.setIcon( progIcon );
				}
				il = new JLabel(m);
				pk.pack( il ).gridx(0).gridy(++y).inset(4,4,4,4).fillx();
			}
		}
		if( block )
			dlg.setDefaultCloseOperation(dlg.DO_NOTHING_ON_CLOSE);
		if( h != null ) {
			dlg.addWindowListener( new WindowAdapter() {
				public void windowClosing( WindowEvent ev ) {
					dlg.setVisible(false);
					dlg.dispose();
					h.cancelled( ComponentUpdateThread.this );
				}
			});
		}
//		pk.pack( new JLabel( msg ) ).gridx(1).gridy(0).inset(40,40,40,40);
		dlg.pack();
		dlg.setLocationRelativeTo( frame );
		dlgOpen = true;
		new Thread() {
			public void run() {
				log.fine("displaying dialog: "+dlg );
				if( dlgOpen ) {
					dlg.setVisible(true);
					if( block && dlg != null ) {
						dlg.dispose();
					}
				}
			}
		}.start();
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	protected void openDialog( final boolean block, final CancelHandler h, String title, Component... msg) {
		if( frame instanceof JDialog )
			dlg = new JDialog( (JDialog)frame, title, block );
		else
			dlg = new JDialog( (JFrame)frame, title, block );
		Packer pk = new Packer( dlg.getContentPane() );
		int y = -1;
		if( msg.length > 1 ) {
			JPanel mp = new JPanel();
			Packer mpk = new Packer( mp );
			JLabel il = new JLabel();
			Icon progIcon = new ProgressIcon(il);
			il.setIcon( progIcon );
			pk.pack( il ).gridx(0).gridy(0).inset(4,4,4,4);
			pk.pack( new JPanel() ).gridx(0).gridy(1).filly();
			pk.pack( mp ).gridx(1).gridy(0).gridh(2).fillboth().inset(4,4,4,4);
			for( Component m: msg ) {
				if( m instanceof JScrollPane ) {
					mpk.pack( m ).gridx(0).gridy(++y).fillboth().inset(2,2,2,2);
				} else {
					mpk.pack( m ).gridx(0).gridy(++y).fillx().inset(2,2,2,2);
				}
			}
		} else {
			for( Component m: msg ) {
				JLabel il = new JLabel();
				if( y == -1 ) {
					Icon progIcon = new ProgressIcon(il);
					il.setIcon( progIcon );
				}
				pk.pack( m ).gridx(0).gridy(++y).inset(4,4,4,4).fillx();
			}
		}
		if( block )
			dlg.setDefaultCloseOperation(dlg.DO_NOTHING_ON_CLOSE);
		if( h != null ) {
			dlg.addWindowListener( new WindowAdapter() {
				public void windowClosing( WindowEvent ev ) {
					h.cancelled( ComponentUpdateThread.this );
				}
			});
		}
//		pk.pack( new JLabel( msg ) ).gridx(1).gridy(0).inset(40,40,40,40);
		dlg.pack();
		dlg.setLocationRelativeTo( frame );
		dlgOpen = true;
		new Thread() {
			public void run() {
				log.fine("displaying dialog: "+dlg );
				if( dlgOpen ) {
					dlg.setVisible(true);
					if( block && dlg != null ) {
						dlg.dispose();
					}
				}
			}
		}.start();
	}
	
	private static Timer timer;
	private static class ProgressIcon implements Icon {
		int wh = 0;
		int inc = 2;
		double rad = 0;
		TimerTask task;
		JLabel comp;
		Object del = new Object() {
			protected void finalize() {
				task.cancel();
			}
		};
		public ProgressIcon(final JLabel comp) {
			this( comp, 2 );
		}
		public ProgressIcon(final JLabel comp, int inc) {
			this.comp = comp;
			if( timer == null ) {
				timer = new Timer();
			}
			task = new TimerTask() {
				public void run() {
					try {
//						System.out.println("Timer fired" );
						SwingUtilities.invokeAndWait( new Runnable() {
							public void run() {
								comp.paintImmediately( comp.getBounds() );
							}
						});
					} catch( Exception ex ) {
						ex.printStackTrace();
					}
				}
			};
//			System.out.println("Scheduling painter...");
			timer.schedule( task, 0, 250 );
		}
		public void paintIcon( Component cmp, Graphics g, int x, int y ) {
//			System.out.println( "Painting icon ("+wh+" > "+inc+") ..."+x+","+y );
			Color c = new Color( (wh*3)+30, (wh*4)+30, (wh*7)+100 );
			int iw = getIconWidth();
			int ih = getIconHeight();
			g.setColor( comp.getBackground() );
			g.fillRect( x, y, iw, ih );
			g.setColor(c);
			((Graphics2D)g).setStroke( new BasicStroke(2) );
			for( int i = 0; i < 12; ++i ) {
				double drad = ((i*(360/12)) * 2 * Math.PI)/360.0;
				double rx = Math.cos( drad );
				double ry = Math.sin( drad );
				double in = .7*(iw/2.0);
				double out = .95*(iw/2.0);
				((Graphics2D)g).draw(new java.awt.geom.Line2D.Double(
					(double)x+(iw/2.0)+(rx*in), 
					(double)y+(ih/2.0)+(ry*in),
					(x+(iw/2.0)+(rx*out)), 
					(y+(ih/2.0)+(ry*out)) ));
			}

			rad += ((360/12) * 2 * Math.PI)/360.0;
			double rx = Math.cos(rad);
			double ry = Math.sin(rad);
			rx *= (iw/2.0)-1;
			ry *= (ih/2.0)-1;
//			g.fillOval(
//				(int)(x+(iw/2)+rx)-iw/8, 
//				(int)(y+(ih/2)+ry)-ih/8,
//				iw/2, ih/2 );
			((Graphics2D)g).draw( new java.awt.geom.Ellipse2D.Double(
				x,//+(iw/2), 
				y,//+(ih/2),
				iw-1, ih-1 ) );
			((Graphics2D)g).setStroke(new BasicStroke(1));
			((Graphics2D)g).draw(new java.awt.geom.Line2D.Double(
				x+(iw/2.0), y+(ih/2.0),
				(x+(iw/2.0)+rx), (y+(ih/2.0)+ry)
					));
			wh += inc;
			if( wh < 0 || wh > 12 ) {
				inc = -inc;
				wh += inc;
				//rad = 0;
			}			
		}
		public int getIconWidth() {
			return 32;
		}
		public int getIconHeight() {
			return 32;
		}
	}

	public ComponentUpdateThread( Action... upd ) {
		act = upd;
		setEnabled(false);
	}

	public ComponentUpdateThread( Component upd ) {
		comp = new Component[]{ upd };
		setEnabled(false);
	}

	public ComponentUpdateThread( Component... upd ) {
		comp = upd;
		setEnabled(false);
	}

	public ComponentUpdateThread( ) {
		setEnabled(false);
	}

	/**
	 *  @param upd the actions to inhibit while <code>construct()</code> is executing
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 * @deprecated Use version with varargs support at end of argument list
	 */
	public ComponentUpdateThread( Action upd, Cursor waitCursor, Cursor defaultCursor ) {
		this(new Action[] { upd });
		setWaitCursor( waitCursor );
		setDefaultCursor( defaultCursor );
	}

	/**
	 *
	 *  @param upd the actions to inhibit while <code>construct()</code> is executing
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 *  @param setupTip the tooltip to put on the component at <code>setup()</code>
	 *  @param finishTip the tooltip to put on the component after <code>finished</code>
	 * @deprecated Use version with varargs support at end of argument list
	 */
	public ComponentUpdateThread( Action upd, Cursor waitCursor, Cursor defaultCursor, String setupTip, String finishTip ) {
		this(upd, waitCursor, defaultCursor);
		setSetupToolTipText( setupTip );
		setFinishedToolTipText( finishTip );
	}

	/**
	 *  @param upd the Component to inhibit while <code>construct()</code> is executing
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 * @deprecated Use version with varargs support at end of argument list
	 */
	public ComponentUpdateThread( Component upd, Cursor waitCursor, Cursor defaultCursor ) {
		this( upd );
		setWaitCursor( waitCursor );
		setDefaultCursor( defaultCursor );
	}

	/**
	 *  @param upd the Component to inhibit while <code>construct()</code> is executing
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 *  @param setupTip the tooltip to put on the component at <code>setup()</code>
	 *  @param finishTip the tooltip to put on the component after <code>finished</code>
	 * @deprecated Use version with varargs support at end of argument list
	 */
	public ComponentUpdateThread( Component upd, Cursor waitCursor, Cursor defaultCursor, String setupTip, String finishTip ) {
		this(upd, waitCursor, defaultCursor);
		setSetupToolTipText( setupTip );
		setFinishedToolTipText( finishTip );
	}

	/**
	 *
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 *  @param upd the actions to inhibit while <code>construct()</code> is executing
	 *
	 */
	public ComponentUpdateThread( Cursor waitCursor, Cursor defaultCursor, Action... upd ) {
		this(upd);
		setWaitCursor( waitCursor );
		setDefaultCursor( defaultCursor );
	}

	/**
	 *
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 *  @param setupTip the tooltip to put on the component at <code>setup()</code>
	 *  @param finishTip the tooltip to put on the component after <code>finished</code>
	 *  @param upd the actions to inhibit while <code>construct()</code> is executing
	 *
	 */
	public ComponentUpdateThread( Cursor waitCursor, Cursor defaultCursor,
			String setupTip, String finishTip, Action... upd ) {
		this( waitCursor, defaultCursor, upd);
		setSetupToolTipText( setupTip );
		setFinishedToolTipText( finishTip );
	}

	/**
	 *
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 *  @param upd the Component to inhibit while <code>construct()</code> is executing
	 *
	 */
	public ComponentUpdateThread(  Cursor waitCursor, Cursor defaultCursor, JComponent... upd ) {
		this( upd );
		setWaitCursor( waitCursor );
		setDefaultCursor( defaultCursor );
	}

	/**
	 *
	 *  @param waitCursor the cursor to use while waiting for <code>construct()</code>
	 *  @param defaultCursor the cursor to restore when done
	 *  @param setupTip the tooltip to put on the component at <code>setup()</code>
	 *  @param finishTip the tooltip to put on the component after <code>finished</code>
	 *  @param upd the Component to inhibit while <code>construct()</code> is executing
	 *
	 */
	public ComponentUpdateThread( Cursor waitCursor, Cursor defaultCursor,
			String setupTip, String finishTip, JComponent... upd  ) {
		this(waitCursor, defaultCursor,upd);
		setSetupToolTipText( setupTip );
		setFinishedToolTipText( finishTip );
	}
	
	/** 
	 *  Default setup implementation, override this and call this one if you
	 *  have setup work to do.
	 *  e.g.
	 *  <pre>
	 *     public void setup() {
	 *         super.setup();
	 *         ...your setup work goes here...
	 *     }
	 *  </pre>
	 */
	public void setup() {
		for( int i = 0; comp != null && i < comp.length; ++i ) {
			setCompEnabled( comp[i], false, !getEnabled(), waitCrs, setupSet, setupTip  );
		}
		for( int i = 0; act != null && i < act.length; ++i ) {
			setActionEnabled( act[i], false, !getEnabled()  );
		}
	}
	
	/** 
	 *  Default finished implementation, override this and call this one if you
	 *  have finished work to do.
	 *  e.g.
	 *  <pre>
	 *     public void finished() {
	 *         ...your finish up work goes here...
	 *         super.finished();
	 *     }
	 *  </pre>
	 */
	public void finished() {
		log.fine("executing finished");
		
		log.fine("enable "+(comp != null ? comp.length : 0)+" components");
		for( int i = 0; comp != null && i < comp.length; ++i ) {
			setCompEnabled( comp[i], true, !getEnabled(), defCrs, finishedSet, finishTip );
		}
		log.fine("enable "+(act != null ? act.length : 0)+" actions");
		for( int i = 0; act != null && i < act.length; ++i ) {
			setActionEnabled( act[i], true, !getEnabled() );
		}
		
		log.fine("dialog is "+((dlg != null)?"":"not")+" opened");
		if( dlg != null ) {
			log.fine("Closing dialog: "+dlg );
			dlgOpen = false;
			dlg.setVisible(false);
			log.fine("disposing dialog and clearing reference");
			dlg.dispose();
		}
	}
}
		