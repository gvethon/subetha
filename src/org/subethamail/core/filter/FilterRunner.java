/*
 * $Id: FilterRunner.java 902 2007-01-15 03:00:15Z skot $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/filter/FilterRunner.java $
 */

package org.subethamail.core.filter;

import javax.ejb.Local;
import javax.mail.MessagingException;

import org.subethamail.common.SubEthaMessage;
import org.subethamail.core.plugin.i.Filter;
import org.subethamail.core.plugin.i.FilterContext;
import org.subethamail.core.plugin.i.HoldException;
import org.subethamail.core.plugin.i.IgnoreException;
import org.subethamail.entity.Mail;
import org.subethamail.entity.MailingList;

/**
 * Interface for running filters on a mime message.
 *
 * @author Jeff Schnitzer
 */
@Local
public interface FilterRunner
{
	/** */
	public static final String JNDI_NAME = "subetha/FilterRunner/local";

	/**
	 * Runs the message through all the filters associated with the list.
	 * Note that the Mail does not exist yet.
	 * 
	 * @see Filter#onInject(SubEthaMessage, FilterContext)
	 */
	public void onInject(SubEthaMessage msg, MailingList list) throws IgnoreException, HoldException, MessagingException;
	
	/**
	 * Runs the message through all the filters associated with the list.
	 * 
	 * @see Filter#onSend(SubEthaMessage, FilterContext)
	 */
	public void onSend(SubEthaMessage msg, Mail mail) throws IgnoreException, MessagingException;

	/**
	 * Runs the message through all the filters associated with the list.
	 * 
	 * @see Filter#onArchiveRender(SubEthaMessage, FilterContext)
	 */
	public void onArchiveRender(SubEthaMessage msg, Mail mail) throws MessagingException;
}

