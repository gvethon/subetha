/*
 * $Id: PersonData.java 105 2006-02-27 10:06:27Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/core/src/com/blorn/core/blog/i/PersonData.java $
 */

package org.subethamail.core.lists.i;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * The summary information about a piece of mail, suitable for display
 * on a page of threads.  The mail body is not included, but the
 * relationships to other mail messages in the thread hierarchy are.
 *
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class MailSummary implements Serializable
{	
	Long id;
	String subject;
	String fromEmail;	// might be null if no permission to view
	String fromName;
	Date dateCreated;
	List<MailSummary> replies;

	/**
	 */
	public MailSummary(
			Long id,
			String subject,
			String fromEmail,
			String fromName,
			Date dateCreated,
			List<MailSummary> replies)
	{
		this.id = id;
		this.subject = subject;
		this.fromEmail = fromEmail;
		this.fromName = fromName;
		this.dateCreated = dateCreated;
		this.replies = replies;
	}
	
	/** */
	public Long getId()
	{
		return this.id;
	}

	/** */
	public String getSubject()
	{
		return this.subject;
	}

	/**
	 * @return null if the client does not have permission to view emails. 
	 */
	public String getFromEmail()
	{
		return this.fromEmail;
	}
	
	/** */
	public String getFromName()
	{
		return this.fromName;
	}

	/** */
	public Date getDateCreated()
	{
		return this.dateCreated;
	}

	/** */
	public List<MailSummary> getReplies()
	{
		return this.replies;
	}
}
