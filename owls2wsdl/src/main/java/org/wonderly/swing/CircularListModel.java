package org.wonderly.swing;

import org.wonderly.swing.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

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
 *  This class provides a highspeed list model that is fixed
 *  size, and circular in nature.  It subclasses the
 *  VectorListModel to use it for storage and dynamic
 *  resizeability.  The Other side of this model is the
 *  notion of maintaining a list of Objects and values
 *  that are not continuously reallocated.  The interface,
 *  {@link ElementLifeCycleManager} can be used to make
 *  this data item caching possible.
 *
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public class CircularListModel<T> extends VectorListModel<T> {
	private int h = 0; // head index
	private int t = 0; // tail index
	private int maxSize;
	private boolean wrapped;
	private ElementLifeCycleManager mgr;

	public String toString() {
		String str = size()+"[";
		for( int i = 0; i < size(); ++i ) {
			Object obj = getElementAt(i);
			if( i != 0 )
				str += ", ";
			str += obj;
		}
		return str + "]";
	}

	public void removeAllElements() {
		h=0;
		t=0;
		wrapped=false;
		super.removeAllElements();
	}

	public CircularListModel( ElementLifeCycleManager mgr, int max ) {
		super();
		this.mgr = mgr;
		setMaxSize( max );
	}

	public CircularListModel( int max ) {
		this(null, max);
	}

	public  int size() {
		if( wrapped )
			return maxSize;
		return super.size();
	}

	public T elementAt( int i ) {
		return getElementAt(i);
	}

	public void setElementAt( T obj, int i ) {
		if( i >= maxSize )
			throw new ArrayIndexOutOfBoundsException( i+" > "+maxSize );
		i += h;
		i %= maxSize;
		if(wrapped) {
			if( mgr != null ) {
				mgr.freeElement( (LifeCycleElement)
					delegate.elementAt(t) );
			}
			delegate.setElementAt( obj, i );
			fireIntervalRemoved(this, 0, 0);
			fireIntervalAdded(this, maxSize-1, maxSize-1);
		} else {
			super.setElementAt( obj, i );
		}
	}

	public  void setMaxSize( int sz ) {
		if( h == 0 && t == 0 && !wrapped ) {
//			System.out.println("set size initially");
			maxSize = sz;
			ensureCapacity( maxSize );
		} else {
			Vector<T> v = new Vector<T>( sz );
			int cnt = 0;
			int mx = Math.min( sz, maxSize );
			int csz = wrapped ? maxSize : size();
			for( int i = (sz < csz) ? (csz-sz) : 0;
						i < csz && cnt++ < mx; i++ ) {
				v.addElement( getElementAt(i) );
			}
//				System.out.println( "new v: ("+maxSize+"->"+sz+"): "+v );
			setContents( v );
			wrapped = false;
			h = 0;
			t = Math.min( csz, sz );
			int oldsz = csz;
			int newsz = sz;
			maxSize = sz;
//				System.out.println( "h="+h+", t="+t+", maxSize="+maxSize );
			if ( oldsz > newsz) {
			    fireIntervalRemoved(this, newsz, oldsz-1);
			}
		}
	}

	public  int getSize() {
		return wrapped ? maxSize : t;
	}

	public T getElementAt( int idx ) {
		if( !wrapped ) {
			if( idx < t-h )
				return super.getElementAt(h+idx);
		} else if (idx < maxSize) {
			return super.getElementAt((h+idx)%maxSize);
		}
		throw new IllegalArgumentException( idx+" >= "+getSize() );
	}

	public void addElement( T obj ) {
		if( !wrapped ) {
			if( t < maxSize ) {
//				System.out.println( Thread.currentThread()+
//					": unwrapped: t="+t+", h="+h+", max: "+maxSize );
				// Must adjust count before adding so that events find the
				// right size.
				++t;
				super.addElement( obj );
				return;
			}
			--t;
			wrapped = true;
		}
		if( wrapped ) {
			h = (h+1) % maxSize;
			t = (t+1) % maxSize;
			if( mgr != null ) {
				mgr.freeElement( (LifeCycleElement)
					delegate.elementAt(t) );
			}
			delegate.setElementAt( obj, t );
//				System.out.println( "wrapped: t="+t+", h="+h );
			fireIntervalRemoved(this, 0, 0);
			fireIntervalAdded(this, maxSize-1, maxSize-1);
//				fireContentsChanged(this, 0, maxSize-1);
		}
	}
}