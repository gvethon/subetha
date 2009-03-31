/*
 * $Id: EmailAdd.java 310 2006-05-09 19:39:28Z jon $
 * $URL: http://subetha.tigris.org/svn/subetha/trunk/frontend/src/org/subethamail/web/action/EmailAdd.java $
 */

package org.subethamail.web.action;

import javax.inject.Current;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.core.acct.i.AccountMgr;
import org.subethamail.web.action.auth.AuthRequired;
import org.tagonist.propertize.Property;

/**
 * Adds an email address to an existing account.  Actually this
 * results in an email to the address which must be confirmed.
 * 
 * @author Scott Hernandez
 */
public class EmailRemove extends AuthRequired 
{
	@Current AccountMgr accMgr;
	
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(EmailRemove.class);
	
	@Property String email = "";

	/** */
	public void authExecute() throws Exception
	{
		accMgr.removeEmail(this.email);
	}
}
