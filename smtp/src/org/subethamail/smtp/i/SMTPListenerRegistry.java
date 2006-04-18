/*
 * $Id: StatsUpdate.java 121 2006-03-07 09:50:09Z jeff $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/mbean/BindStatisticsManagerMBean.java,v $
 */

package org.subethamail.smtp.i;

import javax.ejb.Local;


/**
 * Listeners that wish to partake of inbound SMTP traffic must
 * register themselves with this interface.
 * 
 * @author Jeff Schnitzer
 */
@Local
public interface SMTPListenerRegistry
{
	/**
	 * Register a listener.  The listener will become active immediately.
	 */
	public void register(SMTPListener listener);
	
	/**
	 * Deregister a listener.  The listener will no longer receive notifications.
	 */
	public void deregister(SMTPListener listener);
}
