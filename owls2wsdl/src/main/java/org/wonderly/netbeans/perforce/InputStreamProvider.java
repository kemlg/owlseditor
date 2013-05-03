/*
 * InputStreamProvider.java
 *
 * Created on April 11, 2006, 2:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wonderly.netbeans.perforce;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author gregg
 */
public interface InputStreamProvider {
	public void writeTo( OutputStream os ) throws IOException;
}
