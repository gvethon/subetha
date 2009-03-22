/*
 * $Id: AuthSubscribeResult.java 963 2007-07-04 01:05:05Z jon $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/acct/i/AuthSubscribeResult.java $
 */

package org.subethamail.core.acct.i;

import java.util.Set;


/**
 * When a user anonymously subscribes to a list, they get back
 * a set of auth credentials as well as a result.  This allows
 * them to be automatically logged in.
 * 
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class AuthSubscribeResult extends AuthCredentials
{
	/** */
	SubscribeResult result;
	Long listId;
	
	protected AuthSubscribeResult()
	{
		// http://forums.java.net/jive/thread.jspa?threadID=26539&tstart=0
	}

	/** */
	public AuthSubscribeResult(Long id, String prettyName, String password, Set<String> roles, SubscribeResult result, Long listId)
	{
		super(id, prettyName, password, roles);
		
		this.result = result;
		this.listId = listId;
	}

	/** */
	public Long getListId()
	{
		return this.listId;
	}

	/** */
	public SubscribeResult getResult()
	{
		return this.result;
	}
}
