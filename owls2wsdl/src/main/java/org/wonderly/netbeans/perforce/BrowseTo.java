/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wonderly.netbeans.perforce;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class BrowseTo extends CookieAction {

	protected void performAction(Node[] activatedNodes) {
		/*EditorCookie editorCookie =*/ activatedNodes[0].getLookup().lookup(EditorCookie.class);
	// TODO use editorCookie
	}

	protected int mode() {
		return CookieAction.MODE_EXACTLY_ONE;
	}

	public String getName() {
		return NbBundle.getMessage(BrowseTo.class, "CTL_BrowseTo");
	}

	protected Class[] cookieClasses() {
		return new Class[]{EditorCookie.class};
	}

	@Override
	protected String iconResource() {
		return "org/wonderly/netbeans/perforce/admin.gif";
	}

	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}

	@Override
	protected boolean asynchronous() {
		return false;
	}
}

