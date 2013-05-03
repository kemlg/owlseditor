package org.wonderly.swing;

import javax.swing.*;
import org.wonderly.awt.*;
import java.awt.event.*;
import java.awt.*;

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
 *  This is a simple progress dialog.
 *  <pre>
 *  Vector stuff = ...
 *  SimpleProgress up = new SimpleProgress( parent, "Deleting suff...", stuff.size() );
 *  try {
 *		for( int i = 0; i < stuff.size() && up.isCancelled() == false; ++i ) {
 *			up.setValue( i+1 );
 *			Object sp = stuff.elementAt(i);
 *			up.setCurrentEntity( strValue(sp) );
 *			... do the work...
 *  	}
 *  } finally {
 *  	up.setVisible(false);
 *	}
 *  </pre>
 *
 * <b>NOTE:</b><br>
 *  This class is implemented with Swing safe threading.  All methods that interact
 *  with Swing check to make sure that the calls into swing happen with an event
 *  dispatch thread.  So, you can call these methods without concern for thread
 *  context in which the calls are made.
 *  <p>
 *  This dialog is set as DO_NOTHING_ON_CLOSE so that the cancel action can be invoked.
 *  Thus, <code>setVisible(false)</code> must always be called as shown above in the
 *  <code>try { } finally { }</code> above.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class SimpleProgress extends JDialog {
	protected JLabel msg;
	protected JLabel un ;
	protected JProgressBar br;
	protected int total;
	protected JButton quit;
	protected boolean wasCancelled;

	/**
	 *  Add an ActionListener to the quit/cancel button.  Typically, you can
	 *  just call <code>isCancelled</code> in any loops that are processing
	 *  while this dialog is up.  But, if you are listening on a socket, or
	 *  have some other object that can be closed and/or shutdown in some way
	 *  to make the users cancel request proceed more quickly, install a listener
	 *  and act on its action.
	 */
	public void addActionListener( ActionListener lis ) {
		quit.addActionListener( lis );
	}

	/**
	 *  Check if the cancel button has been pressed.  This is typically
	 *  checked at the top of a loop.  If you want direct notification
	 *  then use <code>addActionListener(ActionListener)</code>.
	 */
	public boolean isCancelled() {
		return wasCancelled;
	}

	/**
	 *  Construct a progress dialog with the indicated parenting, title and
	 *  total number of steps to perform.
	 */
	public SimpleProgress( JFrame parent, String title, int total ) {
		super( parent, title, false );
		build( parent, title, total );
	}

	/**
	 *  Construct a progress dialog with the indicated parenting, title and
	 *  total number of steps to perform.
	 */
	public SimpleProgress( JDialog parent, String title, int total ) {
		super( parent, title, false );
		build( parent, title, total );
	}
	
	protected void build( Window parent, String title, int total ) {
		this.total = total;
		Packer pk = new Packer( getContentPane() );
		msg = new JLabel( "Creating Spot Data for all selected units...", JLabel.CENTER );
		msg.setBorder( BorderFactory.createEtchedBorder() );
		un = new JLabel( "                      ", JLabel.CENTER );
		un.setBorder( BorderFactory.createLoweredBevelBorder() );
		br = new JProgressBar( 0, 0, total );
		pk.pack( msg ).gridx(0).gridy(0).inset(10,10,10,10).fillx();
		pk.pack( br ).gridx(0).gridy(1).inset(5,5,5,5).fillx();
		pk.pack( un ).gridx(0).gridy(2).inset(10,10,10,10).fillx();
		pk.pack( new JSeparator() ).gridx(0).gridy(3).fillx();
		quit = new JButton( "Cancel" );
		quit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				quit.setEnabled(false);
				cancelled();
			}
		});
		pk.pack( quit ).gridx(0).gridy(4).inset( 3,3,3,3 );
		pack();
		setLocationRelativeTo( parent );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent ev ) {
				quit.doClick();
			}
		});
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		setVisible(true);
	}
	
	/**
	 *  Should be called when user cancels the in progress action.
	 */
	protected void cancelled() {
		wasCancelled = true;
	}
	
	/**
	 *  Overridden to provide Swing safe execution.
	 *  This method also calls dispose() to release all
	 *  resources.
	 */
	public void setVisible( final boolean how ) {
		runInSwing( new Runnable() {
			public void run() {
				SimpleProgress.super.setVisible(how);
				if( how == false ) dispose();
			}
		});
	}

	/**
	 *  Used to make sure we update the components in an event dispatch thread.
	 */
	protected void runInSwing( Runnable r ) {
		if( SwingUtilities.isEventDispatchThread() ) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait( r );
			} catch( Exception ex ) {
			}
		}
	}

	/**
	 *  Sets the string value that indicates what work is being performed.
	 *  This method is Swing safe.
	 */
	public void setCurrentEntity( final String name ) {
		runInSwing( new Runnable() {
			public void run() {
				un.setText( name );
				un.revalidate();
				un.repaint();
			}
		});
	}

	/**
	 *  Sets the current progress value.  This method is Swing safe.
	 */
	public void setValue( final int val ) {
		runInSwing( new Runnable() {
			public void run() {
				br.setValue( val );
				msg.setText( val+" of "+total+" - "+((val*100)/total)+"%" );
				msg.revalidate();
				msg.repaint();
				br.repaint();
			}
		});
	}
}