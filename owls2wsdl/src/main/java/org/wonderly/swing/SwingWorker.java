package org.wonderly.swing;

import javax.swing.*;
import javax.security.auth.*;
import java.security.*;
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
 * This is a modified version of the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 *
 * This version is enhanced to copy active Subject and ContextClassLoader
 * objects out of the creating environment!
 *
 * Modified by <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>
 * to include added functionality to 
 * work in conjunction with the ComponentUpdateThread class.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */ 
public abstract class SwingWorker<T> {
    private T value;  // see getValue(), setValue()
    private Thread thread;
	private Subject subj;
	private ClassLoader ctxLoader;
	private AccessControlContext ctx;
	private Logger log = Logger.getLogger( getClass().getName() );
	
	/**
	 *  Get all of the thread, subject and access control context
	 *  currently active in the calling thread
	 */
	protected void collectContext() throws PrivilegedActionException {
		ctxLoader = Thread.currentThread().getContextClassLoader();
		subj = AccessController.doPrivileged( new PrivilegedExceptionAction<Subject>() {
			public Subject run() throws Exception {
				return Subject.getSubject( ctx = AccessController.getContext() );
			}
		});
	}

    /** 
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }

    private ThreadVar threadVar;

    /** 
     * Get the value produced by the worker thread, or null if it 
     * hasn't been constructed yet.
     */
    protected synchronized T getValue() { 
        return value; 
    }

    /** 
     * Set the value produced by worker thread 
     */
    private synchronized void setValue(T x) { 
        value = x; 
    }

    /** 
     * Compute the value to be returned by the <code>get</code> method. 
     */
    public abstract T construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.  
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public T get() {
        while (true) {  
            Thread t = threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }
    
    public boolean setupFailed() {
    	return interrupted != null || target != null;
    }
    
    public InterruptedException getInterruptedException() {
    	return interrupted;
    }
    
    public java.lang.reflect.InvocationTargetException getInvocationTargetException() {
    	return target;
    }
    
    InterruptedException interrupted;
    java.lang.reflect.InvocationTargetException target;

    /**
     *  Override this method with an implementation that sets up your
     *  environment such as setting wait cursors, disabling components etc.
     *  This is called in the context of SwingUtilities.invokeAndWait()
     */
    protected void setup() {
    }
    
    public abstract class InContextRunnable implements Runnable {
    	public abstract Object doRun();
    	public void run() {
    		Thread th = Thread.currentThread();
    		ClassLoader ld = th.getContextClassLoader();
    		try {
    			log.fine("Setting context class loader to: "+ctxLoader );
    			th.setContextClassLoader( ctxLoader );
    			if( subj == null ) {
    				log.fine("Running without a subject");
    				Object v = doRun();
    				log.finer("Generated value: "+v );
    			} else {
    				log.fine("Running with subject: "+subj );
    				Subject.doAsPrivileged( subj, new PrivilegedAction() {
    					public Object run() {
    						return doRun();
    					}
    				}, ctx);
    			}
    		} finally {
    			th.setContextClassLoader( ld );
    		}
    	}
    }

    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker() {
    	try {
    		collectContext();
    	} catch( Exception ex ) {
    		log.log( Level.SEVERE, ex.toString(), ex );
    	}
  
        final Runnable doFinished = new InContextRunnable() {
           public Object doRun() { finished(); return null; }
        };
        
        final Runnable doSetup = new InContextRunnable() {
           public Object doRun() { setup(); return null; }
        };

        Runnable doConstruct = new InContextRunnable() { 
            public Object doRun() {
                try {
	            	SwingUtilities.invokeAndWait( doSetup );
                    setValue( construct() );
                } catch( InterruptedException ex ) {
                	interrupted = ex;
                } catch( java.lang.reflect.InvocationTargetException ex ) {
                	target = ex;
                } finally {
                    threadVar.clear();
                }

                SwingUtilities.invokeLater(doFinished);
                return getValue();
            }
        };

        Thread t = new Thread(doConstruct);
        threadVar = new ThreadVar(t);
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();
        if (t != null) {
            t.start();
        }
    }
}