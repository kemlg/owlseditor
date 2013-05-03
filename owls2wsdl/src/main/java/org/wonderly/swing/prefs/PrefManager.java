package org.wonderly.swing.prefs;

import java.awt.Component;

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
 *  This interface is implemented by Preferences access
 *  Managers used by {@link PreferencesMapper}.  Implementations
 *  of this interface are used to manage the mapping of
 *  values to UI state or other similar mappings.  The
 *  {@link PreferencesMapper} class will call {@link
 *  #setValueIn(T)} when the initial call to
 *  one of the <code>map()</code> methods is called.  When
 *  {@link PreferencesMapper}.commit() is called, the
 *  {@link #prepare(T)} and then the {@link #commit(T)}
 *  will be called to cause the storing of the preferred
 *  values of an associated Component. For preferences
 *  associated with a UI, but not with a components state,
 *  you can use invisible JPanel's or JLabel or other
 *  unalterable components in the call to {@link
 *  PreferencesMapper}.map(PrefManager,T).
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">
 *	gregg.wonderly@pobox.com</a>
 */
public interface PrefManager<T> {
	/**
	 *  This method should look at the content of the
	 *  passed component and validate that it is ready
	 *  to be stored into Preferences.
	 *  @return true if data is valid, false if not
	 */
	public boolean prepare( T comp );
	/**
	 *  This method should store the value for the passed
	 *  component into Preferences and return true
	 *  if it was successful.  If it returns false,
	 *  some applications might use this information to
	 *  take some specific action to deal with the failure
	 *  @return true if commit was successful, false if not
	 */
	public boolean commit( T comp );
	/**
	 *  This method should extract the current value from
	 *  Preferences and set the components value/state
	 *  to reflect that value.
	 */
	public void setValueIn( T comp );
}