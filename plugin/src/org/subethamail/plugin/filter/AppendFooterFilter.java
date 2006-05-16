/*
 * $Id$
 * $URL$
 */

package org.subethamail.plugin.filter;

import java.io.IOException;

import javax.annotation.EJB;
import javax.annotation.security.RunAs;
import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.core.lists.i.ListMgr;
import org.subethamail.core.plugin.i.Filter;
import org.subethamail.core.plugin.i.FilterParameter;
import org.subethamail.core.plugin.i.IgnoreException;
import org.subethamail.core.plugin.i.SendFilterContext;
import org.subethamail.core.plugin.i.helper.FilterParameterImpl;
import org.subethamail.core.plugin.i.helper.GenericFilter;
import org.subethamail.core.plugin.i.helper.Lifecycle;

/**
 * This filter appends a customizable footer to the bottom
 * of outgoing emails.
 * 
 * @author Jon Stevens
 */
@Service
@SecurityDomain("subetha")
@RunAs("siteAdmin")
public class AppendFooterFilter extends GenericFilter implements Lifecycle
//TODO:  remove the implements clause when http://jira.jboss.org/jira/browse/EJBTHREE-489 is fixed
{
	@EJB ListMgr listMgr;
	
	/** */
	private static Log log = LogFactory.getLog(AppendFooterFilter.class);
	
	/** */
	static FilterParameter[] PARAM_DEFS = new FilterParameter[] {
		new FilterParameterImpl(
				"Footer",
				"The footer text which is appended to the bottom of the email body.",
				"",
				20,
				true,
				null
			)
	};

	/**
	 * @see Lifecycle#start()
	 */
	public void start() throws Exception
	{
		super.start();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.plugin.i.Filter#getName()
	 */
	public String getName()
	{
		return "Append Footer";
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.plugin.i.Filter#getDescription()
	 */
	public String getDescription()
	{
		return "Text which is appended to the bottom of each message to the list.";
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
		log.debug("AppendFooterFilter: onSendAfterAttaching()");
		
		Object obj;
		try
		{
			obj = msg.getContent();
			if (obj instanceof String)
			{
				ctx.expand((String)obj);
			}
			msg.setContent(obj, "text/ascii");
		}
		catch (IOException e)
		{
		}
	}
}
