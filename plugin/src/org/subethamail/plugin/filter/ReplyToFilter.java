/*
 * $Id$
 * $URL$
 */

package org.subethamail.plugin.filter;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.Service;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.core.plugin.i.ArchiveRenderFilterContext;
import org.subethamail.core.plugin.i.Filter;
import org.subethamail.core.plugin.i.FilterParameter;
import org.subethamail.core.plugin.i.IgnoreException;
import org.subethamail.core.plugin.i.SendFilterContext;
import org.subethamail.core.plugin.i.helper.FilterParameterImpl;
import org.subethamail.core.plugin.i.helper.GenericFilter;
import org.subethamail.core.plugin.i.helper.Lifecycle;

/**
 * This filter sets the ReplyTo header on an outgoing message
 * to either the list or to an email address.
 * 
 * @author Jon Stevens
 */
@Service
@SecurityDomain("subetha")
//@RunAs("siteAdmin")
public class ReplyToFilter extends GenericFilter implements Lifecycle
//TODO:  remove the implements clause when http://jira.jboss.org/jira/browse/EJBTHREE-489 is fixed
{
	/** */
	private static Log log = LogFactory.getLog(ReplyToFilter.class);
	
	public static final String ARG_MAILINGLIST = "MailingList";
	public static final String ARG_EMAILADDRESS = "EmailAddress";
	
	/** */
	static FilterParameter[] PARAM_DEFS = new FilterParameter[] {
		new FilterParameterImpl(
				ARG_MAILINGLIST,
				"Checking this option will cause all replies to go to the mailing list.",
				Boolean.class,
				true
			),
		new FilterParameterImpl(
				ARG_EMAILADDRESS,
				"Enter an email address to be used as the Reply-To for the mailing list.",
				String.class,
				""
			)
	};

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.plugin.i.Filter#getName()
	 */
	public String getName()
	{
		return "Reply-To";
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.plugin.i.Filter#getDescription()
	 */
	public String getDescription()
	{
		return "Set the Reply-To to the mailing list or an email address.";
	}
	
	/**
	 * @see PluginFactory#getParameters()
	 */
	public FilterParameter[] getParameters()
	{
		return PARAM_DEFS;
	}

	/**
	 * @see Filter#onSend(SubEthaMessage, SendFilterContext)
	 */
	@Override
	public void onSend(SubEthaMessage msg, SendFilterContext ctx) throws IgnoreException, MessagingException
	{
		log.debug("ReplyToFilter: onSend()");

		InternetAddress addr = new InternetAddress();

		Boolean replyToList = (Boolean) ctx.getArgument(ARG_MAILINGLIST);
		String emailAddress = (String) ctx.getArgument(ARG_EMAILADDRESS);

		// if nothing is selected, then default to reply to the list.
		if (replyToList.booleanValue() || emailAddress == null || emailAddress.length() == 0)
		{
			addr.setAddress(ctx.getList().getEmail());
		}
		else
		{
			addr.setAddress(emailAddress);
		}

		Address[] addrs = {addr};
		msg.setReplyTo(addrs);
	}

	/* (non-Javadoc)
	 * @see org.subethamail.core.plugin.i.helper.GenericFilter#onArchiveRender(org.subethamail.common.SubEthaMessage, org.subethamail.core.plugin.i.ArchiveRenderFilterContext)
	 */
	@Override
	public void onArchiveRender(SubEthaMessage msg, ArchiveRenderFilterContext ctx) throws MessagingException
	{
	}	
}
