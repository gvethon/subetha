/*
 * $Id: $
 * $URL:  $
 */


package org.subethamail.core.queue;

import java.util.concurrent.BlockingQueue;

import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.common.NotFoundException;
import org.subethamail.core.util.SubEthaEntityManager;
import org.subethamail.entity.Mail;
import org.subethamail.entity.Subscription;

import com.caucho.config.Name;

/**
 * Queue which takes the individual, stored messages and turns them into
 * a series of delivery queue messages.  This could take a while; any
 * individual message may get turned into thousands of outboud queue
 * messages.
 */
@MessageDriven
public class InjectListener implements MessageListener{
	/** */
	private final static Logger log = LoggerFactory.getLogger(InjectListener.class);

	/** */
	@SuppressWarnings("unchecked")
	@DeliveryQueue 
	BlockingQueue outboundQueue;

	/** */
	@Name("subetha")
	protected SubEthaEntityManager em;

	/** */
	public void onMessage(Message qMsg)
	{
		try
		{
			InjectedQueueItem item = (InjectedQueueItem) ((ObjectMessage)qMsg).getObject();
			this.deliver(item.getMailId());
		}
		catch (JMSException e)
		{
			log.error("Error getting object outa message (from queue)", e);
		}
	}
	
	/**
	 * Looks up who gets that message and creates new queue entries.
	 */
	@SuppressWarnings("unchecked")
	private void deliver(Long mailId)
	{
		if (log.isDebugEnabled())
			log.debug("Distributing mailId " + mailId);

		Mail mail;
		try
		{
			mail = this.em.get(Mail.class, mailId);
		}
		catch (NotFoundException ex)
		{
			// Possible if the message was deleted during the queue time.
			// Not a problem, just log an error and return, accepting the
			// queue message.

			if (log.isWarnEnabled())
				log.warn("Wanted to distribute nonexistant mailId " + mailId);

			return;
		}

		// Now make sure each subscriber gets a copy
		for (Subscription sub: mail.getList().getSubscriptions())
		{
			if (sub.getDeliverTo() != null)
			{
				try
				{
					this.outboundQueue.put(new DeliveryQueueItem(mailId, sub.getPerson().getId()));
				}
				catch (InterruptedException e)
				{
					log.error("Error queuing delivery message",e);
				}
			}
		}
	}
	
}