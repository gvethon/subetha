/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * If you instantiate an action of this type (or one derived from it) in a page,
 * the user will be automatically logged in when autologin cookies are
 * present and valid.
 * 
 * The execute2() method will be invoked even if autologin does not
 * occur.   
 * 
 * @author Jeff Schnitzer
 */
public class AutoLogin extends AuthAction 
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(AutoLogin.class);
	
	/**
	 * Derived actions should override this method to implement behavior.
	 */
	protected void execute2() throws Exception
	{
		// By default do nothing
	}
	
	/**
	 * Override the execute2() method instead.
	 */
	public final void execute() throws Exception
	{
		if (!this.isLoggedIn())
			this.tryAutoLogin();
		
		this.execute2();
	}

}
