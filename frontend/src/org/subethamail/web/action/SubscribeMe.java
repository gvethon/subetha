/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.web.Backend;
import org.subethamail.web.action.auth.AuthRequired;
import org.tagonist.propertize.Property;

/**
 * Subscribes an existing user to a mailing list, or changes the
 * address to which delivery is enabled.  The resulting model
 * will be Boolean.TRUE if the user was immediately subscribed, or
 * Boolean.FALSE if the subscription is held for approval.
 * 
 * @author Jeff Schnitzer
 */
public class SubscribeMe extends AuthRequired 
{
	/** */
	private static Log log = LogFactory.getLog(SubscribeMe.class);
	
	/** */
	@Property Long listId;
	@Property String deliverTo = "";
	
	/** */
	public void authExecute() throws Exception
	{
		if (this.deliverTo.length() == 0)
			this.deliverTo = null;
		
		Backend.instance().getAccountMgr().subscribeMe(this.listId, this.deliverTo);
	}
	
}
