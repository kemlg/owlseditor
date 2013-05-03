/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wonderly.swing;

/**
 *  Provides an interface for observing progress changes externally to
 * stepwise processing happening inside of another object.
 * @see SyncThread
 * @see SimpleProgress
 * @author gregg
 */
public interface ProgressObserver {
	public void startProcessSteps( int cnt );
	public void progressToStep( int step, String name );
	public void progressStepsDone( int total );
}
