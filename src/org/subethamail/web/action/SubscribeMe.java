/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.core.acct.i.SubscribeResult;
import org.subethamail.web.Backend;
import org.subethamail.web.action.auth.AuthRequired;
import org.tagonist.propertize.Property;

/**
 * Subscribes an existing user to a mailing list, or changes the
 * address to which delivery is enabled.  This object remains the
 * model.  Check for the held property.
 * 
 * @author Jeff Schnitzer
 */
public class SubscribeMe extends AuthRequired 
{
	/** */
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(SubscribeMe.class);
	
	/** */
	@Property Long listId;
	@Property String deliverTo = "";
	@Property boolean held;
	
	/** */
	public void authExecute() throws Exception
	{
		if (this.deliverTo.length() == 0)
			this.deliverTo = null;
		
		SubscribeResult result = Backend.instance().getAccountMgr().subscribeMe(this.listId, this.deliverTo);
		
		if (result == SubscribeResult.HELD)
			this.held = true;
	}
	
}
