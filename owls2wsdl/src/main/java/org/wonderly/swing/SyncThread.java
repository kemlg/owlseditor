/**
 *  Mike Cox initially created this as a derivative of the the
 *  {@link org.wonderly.swing.ComponentUpdateThread} class.
 */
package org.wonderly.swing;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.swing.Action;
import javax.swing.SwingUtilities;

/**
 * This class is a SwingWorker kind of class which provides method names
 * and behaviors like SwingWorker, but extends the available capabilities
 * with several additional features.
 * <pre>
 * ThreadPoolExecutor exec = ...
 * 
 * public void searchFor( String search ) {
 * 
 *   // Create an instanc which uses our executor for the
 *   // background threads.
 *   final SyncThread th = new SyncThread() {
 *     // Override to use ThreadPoolExecutor instead of running a new thread
 *     public @Override void schedule( Runnable r ) {
 *         exec.execute( r );
 *	   }
 *   };
 *
 *   // Get all the current values from the remote instance
 *   th.add( new SyncThread<String[],String[]>() {
 *     public String[] run() {
 *         return remote.getValues();
 *     }
 *     public void done() {
 *         comp.setModel( new Model( getValue() ) );
 *     }
 *   } );
 * 
 *   // Add step to use data and select searched for
 *   // value if found.
 *   th.add( new SyncThread<String,String>() {
 *     public String run() {
 *         String[] dt = th.getLastThreadValue();
 *		   for( String s : dt ) {
 *		       if( findPattern( search, s ) ) {
 *                  return s;
 *             }
 *         }
 *         return null;
 *     }
 *     public void done() {
 *         if( getValue() == null ) {
 *             comp.clearSelection();
 *         } else {
 *             comp.setSelectedValue( getValue() );
 *         }
 *     }
 *   });
 *   
 *   th.start();
 * }
 * </pre>
 * @param <R> The returned value for doInBackground
 * @param <P> The type for published values.
 */
public class SyncThread<R, P> implements Callable<R> {
	private final Logger log = Logger.getLogger(getClass().getName());
	/**
	 * The set of actions which will be disabled/enabled.
	 */
	protected Action[] acts;
	/**
	 * The set of components which will be disabled/enabled.
	 */
	protected Component[] comps;

	// The value that this thread has calculated and returned from run().
	private volatile R value = null;

	// Used as executor for threads to pass values to subsequent threads.
	private volatile Object threadVal = null;

	// If any component has focus, set focus back after we're done.
	private volatile int focusIndex = -1;

	// The list of SyncThreads that will be ran.
	private volatile ArrayList<SyncThread> threads = null;

	private final boolean setLoader;
	private final ClassLoader ctxLoader;
	private final Subject subj;
	private final AccessControlContext ctx;
	private volatile ProgressObserver observer;

	public SyncThread( ProgressObserver obs ) {
		this();
		observer = obs;
	}
	/**
	 * Collects all the context for execution, any active Subject and context class loader will
	 * be remebered and activated.
	 */
	public SyncThread() {
		boolean loaded = false;
		ctxLoader = Thread.currentThread().getContextClassLoader();
		Subject createdSubject = null;
		final AccessControlContext rctx[] = new AccessControlContext[1];
		try {
			createdSubject = AccessController.doPrivileged( new PrivilegedExceptionAction<Subject>() {
				public Subject run() throws Exception {
					return Subject.getSubject( rctx[0] = AccessController.getContext() );
				}
			});
			// Everything worked, so set the exception
			loaded = true;
		} catch( PrivilegedActionException ex ) {
			// Construction is not complete, so don't report this exception using
			// innerReportException.
			if( log.isLoggable( Level.FINE ) )
				log.log( Level.FINE, ex.toString(), ex );
		} catch( RuntimeException ex ) {
			// Construction is not complete, so don't report this exception using
			// innerReportException.
			if( log.isLoggable( Level.FINE ) )
				log.log( Level.FINE, ex.toString(), ex );
			throw ex;
		}
		ctx = rctx[0];
		subj = createdSubject;
		setLoader = loaded;
	}

	/**
	 * Create an instance specifying the set of Components which should be
	 * disabled while doInBackground() runs.
	 * @param components
	 */
	public SyncThread( Component... components ) { 
		this();
		comps = components;
	}

	/**
	 * Create an instance specifying the set of Actions which should be
	 * disabled while doInBackground() runs.
	 * @param actions
	 */
	public SyncThread( Action... actions ) {
		this();
		acts = actions;
	}

	/**
	 * Create an instance specifying the set of Components and Actions which should be
	 * disabled while doInBackground() runs.
	 * 
	 * @param components
	 * @param actions
	 */
	public SyncThread( Component[] components, Action[] actions ) {
		this();
		comps = components;
		acts = actions;
	}
	
	/**
	 * Returns the value that this thread has calculated and returned from run(). 
	 */
	public R getValue() { 
		return value;
	}

	/**
	 * Set's the return value that will be visible from {@link #getValue}.
	 * @param val
	 */
	public void setValue(R val) {
		value = val;
	}

	/**
	 * If running as an executor of multiple steps, this returns the value
	 * which the the last thread returned from run().  Threads can
	 * pass values to subsequent threads.
	 * 
	 * Each threads doInBackground(), can call this method to get the
	 * value which the last doInBackground() returned;
	 */
	public Object getLastThreadValue() { return threadVal; }

	/**
	 * Override these to customize when components are disabled.
	 * This runs in the EDT.
	 */
	protected void disableComponents() { setThingsEnabled(false); }
	/**
	 * Override these to customize when components are enabled .
	 * This runs in the EDT.
	 */
	protected void enableComponents() { setThingsEnabled(true); }	

	/**
	 * Override this to do preliminary work before components and actions are disabled.
	 * This runs in the EDT.
	 */
	protected void setup() {}

	/**
	 *  Use run() which calls this method.
	 * @deprecated - use run() so that you don't mistype the case of this method
	 *  and get nothing to run.  This method is not abstract because of the
	 *  history of things where we wanted a new method name and didn't want
	 *  to break existing code so there was construct() then doInBackground() to
	 *  be compatible with JDK6 SwingWorker, and then the realization that we
	 *  were typoing <code>doInbackground</code> too much (and not using @Override
	 *  to see that we weren't overriding anything) and not invoking our code, but the
	 *  dummy method in this class, and so we decided to just use run() and be done.
	 * @return value
	 */
	protected R doInBackground() { return value; }

	/**
	 * This method will be called in a dedicated thread after setup() has returned.
	 * This runs outside the EDT in the newly created thread.
	 */
	protected R run() {
		return doInBackground();
	}

	/**
	 * This method can be used by a Future to invoke this instance
	 * as callable.  This method executes <code> return value = {@link #run()}; </code>.
	 */
	public R call() {
		return value = run();
	}

	/** 
	 * This method will be called in the EDT after {@link #run()} has returned.
	 */
	protected void done() {}

	/** 
	 * Call publish from {@link #run()} to send preliminary data
	 * to be processed in the EDT.
	 */
	protected void publish(final P value) {
		try {
			runInSwing(new InContextRunnable() {
				public Object doRun() {
					process(value);
					return null;
				}
			}, false);
		} catch (InterruptedException ex) { 
			innerReportException(ex);
		}
	}

	/**
	 * Override this to process data that was sent from {@link #publish()} in the EDT.
	 */
	protected void process(P value) {}

	/**
	 * Adds an additional SyncThread instance into the queue of things
	 * to execute together so that a set of EDT - non-EDT - EDT operations
	 * can be performed in sequence without having {@link #done} do the
	 * initiation of the next step.
	 * @param thread
	 */
	public void add(SyncThread thread) {
		if (threads == null) {
			threads = new ArrayList<SyncThread>();
		}
		threads.add(thread);
	}

	/**
	 * This is the place where the {@link #run()} thread is allocated.
	 * This implementation just uses <code>new Thread(r).start()</code>
	 * to initiate the operation.  Subclasses can be created which use
	 * thread pools or other mechanisms.
	 * <p>
	 * The Runnable passed should be queued to execute/running when this
	 * method returns.
	 * 
	 * @param r the Runnable to execute.
	 */
	public void schedule( Runnable r ) {
		new Thread( r ).start();
	}

	/**
	 * Start this process or executor sequence in a dedicated thread.
	 * The actions passed here override any passed to the constructor
	 */
	public void start( Action...acts ) {
		this.acts = acts;
		start();
	}

	/**
	 * Start this process or executor sequence in a dedicated thread.
	 * The components passed here override any passed to the constructor
	 */
	public void start( Component...comps ) {
		this.comps = comps;
		start();
	}

	/**
	 * Start this process or executor sequence in a dedicated thread.
	 * The components and actions passed here override any passed to the constructor
	 */
	public void start( Component[]comps, Action...acts ) {
		this.comps = comps;
		this.acts = acts;
		start();
	}
	/**
	 * Start this process or executor sequence in a dedicated thread.
	 */
	public void start() {
		// If thread list is null, run as a single thread sequence.
		if (threads == null) {
			schedule( new InContextRunnable() {
				public Object doRun() {
					runSingle(false);
					return null;
				}
			});
		// If thread list is not null, run as an executor.	
		} else {
			schedule( new InContextRunnable() {
				public Object doRun() {
					runAllSteps();
					return threadVal;
				}
			});
		}
	}

	/**
	 *  Run this process or execution sequence in the calling thread.
	 */
	public void block() {
		if( SwingUtilities.isEventDispatchThread() ) {
			if (log.isLoggable(Level.FINE))
				log.fine("Called block(), already in event thread");
		}
		// If thread list is null, run as a single sequence.
		if (threads == null) {
			runSingle(false);
		// If thread list is not null, run as an executor.	
		} else {
			runAllSteps();
		}		
	}

	/**
	 * Run this instance in another thread, asynchronously.  In general,
	 * this should only be used for 'EDT' only processing because the EDT
	 * {@link #setup()} work is done with <code>SwingUtilities.invokeLater()</code>
	 * which can make that happen after {@link #run()} work has already
	 * started.  However, the {@link #done()} work will happen after the
	 * {@link #setup()} and after {@link #run()} work, so that sequence will
	 * happen correctly, but still with <code>SwingUtilities.invokeLater()</code>.
	 * <p>
	 * Use this with caution as it's order of execution can create unexpected
	 * behaviors.
	 */
	public void later() {
		runSingle(true);
	}

	/**
	 * Run the steps in this instance
	 * 
	 * @param later controls whether {@link #setup} is run now
	 * or later in the EDT queue.
	 */
	private void runSingle(boolean later) {
		if( observer != null )
			observer.startProcessSteps( 2 );
		try {
			if( observer != null )
				observer.progressToStep( 1, getName() );
			doStep(later);
			if( observer != null )
				observer.progressStepsDone( 2 );
		} catch (InterruptedException ex) {
			innerReportException(ex);
		}
	}
	/**
	 * Provide a name for this instance to describe its work.
	 * @return by default returns toString()
	 */
	public String getName() {
		return toString();
	}
	/**
	 *  This method is used to run the internal list of steps that where
	 *  added to this instance.
	 */
	private void runAllSteps() {
		try {
			if( observer != null )
				observer.startProcessSteps( threads.size() + 1 );
			doSetup(false);
			int cnt = 0;
			for (SyncThread thread : threads) {
				++cnt;
				if( observer != null )
					observer.progressToStep( cnt, thread.getName() );
				thread.doStep(false);
				threadVal = thread.getValue();
			}
			if( observer != null )
				observer.progressStepsDone( ++cnt );
		} catch (RuntimeException ex) {
			innerReportException(ex);
			throw ex;
		} catch (InterruptedException ex) { 
			innerReportException(ex);
		} finally {
			try {
				doDone(false);
			} catch (InterruptedException ex) {
				innerReportException(ex);
			} catch (RuntimeException ex) {
				innerReportException(ex);
				throw ex;
			}
		}		
	}

	/**
	 * Performs the three components of the execution model.  The 
	 * {@link #setup()}, {@link #run()}, {@link #done()} steps are done in order.
	 * @param later  This controls whether the EDT work is done
	 *   synchronously if false, or queued if true.
	 * @throws java.lang.InterruptedException
	 */
	private void doStep(boolean later) throws InterruptedException {
		try {
			try {
				doSetup(later);
				doRun();
			} catch (RuntimeException ex) {
				innerReportException(ex);
				throw ex;
			}
		} finally {
			try {
				doDone( later );
			} catch (RuntimeException ex) {
				innerReportException(ex);
				throw ex;
			}
		}
	}

	/**
	 * Creates an InContextRunnable instance to call {@link #setup()} in, and
	 * to then disable components enumerated in the constructor().
	 * @param later
	 * @throws java.lang.InterruptedException
	 */
	private void doSetup(boolean later) throws InterruptedException {
		runInSwing(new InContextRunnable() {
			public Object doRun() {
				setup();
				disableComponents();
				return null;
			}
		}, later);
	}

	/**
	 * Controls the execution of the run() method.  The default
	 * implementatioin just does <code>value = run();<code>.
	 * @throws java.lang.RuntimeException
	 */
	protected void doRun() throws RuntimeException {
		value = run();
	}

	/**
	 * Creates an InContextRunnable instance to call done() in, which
	 * enables the components enumerated in the constructor() and then
	 * calls {@link #done()}.
	 * @param later
	 * @throws java.lang.InterruptedException
	 */
	private void doDone(boolean later) throws InterruptedException {
		runInSwing(new InContextRunnable() {
			public Object doRun() {
				enableComponents();
				done();
				return null;
			}
		}, later);
	}

	/**
	 *  Change component/action enabled status
	 * @param how true to enable, false to disable.
	 */
	private void setThingsEnabled(boolean how) {
		for (int i = 0; acts != null && i < acts.length; ++i) {
			acts[i].setEnabled(how);
		}

		for (int i = 0; comps != null && i < comps.length; ++i) {
			// If we're disabling and this component has the focus, remember its index.
			if (how == false && comps[i].hasFocus() == true) {
				focusIndex = i;
			}
			
			comps[i].setEnabled(how);
			
			// If we're enabling and this component had the focus before, set it back.
			if (how == true && i == focusIndex) {
				comps[i].requestFocus();
				focusIndex = -1; // Reset for next time.
			}
		}
	}

	/**
	 * This method can be used to invoke the passed Runnable in the EventDispatchThread.
	 * 
	 * @param r The Runnable to dispatch
	 * @param later if true, SwingUtilities.invokeLater() is used, otherwise 
	 *	SwingUtilities.invokeAndWait() is used.
	 * @throws java.lang.InterruptedException if invokeAndWait() is interupted.
	 */
	public static void runInSwing(final Runnable r, boolean later) throws InterruptedException {
		try {
			if (later) {
				SwingUtilities.invokeLater(r);
			} else {
				if (SwingUtilities.isEventDispatchThread()) {
					r.run();
				} else {
					SwingUtilities.invokeAndWait(r);
				}
			}
		// Throw the cause to make sure we get the actual exception instead of one wrapped
		// inside an InvocationTargetException.
		} catch (InvocationTargetException e) {
			Throwable th = e.getTargetException();
			if (th instanceof RuntimeException) {
				throw (RuntimeException)th;
			} else if (th instanceof Error) {
				throw (Error)th;
			} else {
				throw new RuntimeException("Invocation target threw "+th.getClass().getName(), th);
			}
		}
		
	}

	/**
	 * Many types of errors inside of this class are passed into this method and
	 * simply logged at a SEVERE level.  A sub class can override this method to
	 * display dialogs or otherwise interact with the user when such problems
	 * are reported.
	 * @param t the problem to report.
	 */
	protected void innerReportException(Throwable t) {
		log.log(Level.SEVERE, t.toString(), t);
	}

	/**
	 * This class provides a Runnable implementation which will use the
	 * context ClassLoader active at construction of this instance, as
	 * well as any active Subject.
	 * @param <T> The type of value created/returned by the action.
	 */
    protected abstract class InContextRunnable<T> implements Runnable {
		/**
		 * Implement this method to do your work.
		 * @return The value created
		 */
    	public abstract T doRun();
		/**
		 * A copy of the value created.
		 */
		private volatile T val;

		/**
		 * Get the value that was calculated in {@link #run()}/{@link #doRun()}.
		 * @return
		 */
		public T get() {
			return val;
		}

    	public final void run() {
    		Thread th = Thread.currentThread();
    		ClassLoader ld = null;

			if( setLoader ) {
				ld = th.getContextClassLoader();
			}
    		try {
				try {
					if( setLoader )
						th.setContextClassLoader( ctxLoader );
				} catch( RuntimeException ex ) {
					innerReportException(ex);
					throw ex;
				}
    			if( !setLoader || subj == null ) {
    				val = doRun();
    			} else {
    				Subject.doAsPrivileged( subj, new PrivilegedAction<T>() {
    					public T run() {
    						return doRun();
    					}
    				}, ctx);
    			}
    		} finally {
				try {
					if( setLoader )
						th.setContextClassLoader( ld );
				} catch( Exception ex ) {
					innerReportException(ex);
				}
			}
    	}
    }
}