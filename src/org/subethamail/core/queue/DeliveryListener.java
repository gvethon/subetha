/*
 * $Id: $
 * $URL:  $
 */


package org.subethamail.core.queue;

import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Current;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.common.NotFoundException;
import org.subethamail.core.deliv.i.Deliverator;
import org.subethamail.core.util.SubEthaEntityManager;

import com.caucho.config.Name;

/**
 * Processes delivery queue messages by creating an actual STMP message
 * using JavaMail, relaying it through the deliverator.
 */
@MessageDriven
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DeliveryListener implements MessageListener
{
	/** */
	private final static Logger log = LoggerFactory.getLogger(DeliveryListener.class);

	/** */
	@Current Deliverator deliverator;

	/** */
	@Name("subetha")
	protected SubEthaEntityManager em;

	/**
	 */
	public void onMessage(Message qMsg)
	{
		try
		{
			DeliveryQueueItem umdd = (DeliveryQueueItem)((ObjectMessage) qMsg).getObject();

			Long mailId = umdd.getMailId();
			Long personId = umdd.getPersonId();
			if (log.isDebugEnabled())
				log.debug("Delivering mailId " + mailId + " to personId " + personId);
	
			try
			{
				this.deliverator.deliver(mailId, personId);
			}
			catch (NotFoundException ex)
			{
				// Just log a warning and accept the JMS message
				if (log.isWarnEnabled())
					log.warn("Unknown mailId(" + mailId + ") or personId(" + personId + ")", ex);
			}	
		}
		catch (JMSException ex)
		{
			log.error("Error getting data off queue",ex);
		}
	}
}
