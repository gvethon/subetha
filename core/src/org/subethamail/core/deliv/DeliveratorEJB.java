/*
 * $Id: AccountMgr.java 127 2006-03-15 02:29:11Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/core/src/com/blorn/core/acct/i/AccountMgr.java $
 */

package org.subethamail.core.deliv;

import javax.ejb.Stateless;

import org.subethamail.core.deliv.i.Deliverator;
import org.subethamail.core.deliv.i.DeliveratorRemote;

/**
 * @author Jeff Schnitzer
 */
@Stateless
//@SecurityDomain("subetha")
//@RunAs("god")
public class DeliveratorEJB implements Deliverator, DeliveratorRemote
{
	/**
	 * @see Deliverator#deliver(Long, Long)
	 */
	public void deliver(Long mailId, Long personId)
	{
		
	}
}

