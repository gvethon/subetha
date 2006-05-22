/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.admin.i;

import java.io.Serializable;
import java.net.URL;

import javax.mail.internet.InternetAddress;

/**
 * Some random information about the site.
 *
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class SiteStatus implements Serializable
{
	/** */
	String defaultCharset;
	int listCount;
	int personCount;
	int mailCount;
	
	/** Some site config params */
	URL defaultSiteUrl;
	InternetAddress postmasterEmail;
	
	/** */
	public SiteStatus(String encoding, int listCount, int personCount, int mailCount,
						URL defaultSiteUrl, InternetAddress postmasterEmail)
	{
		this.defaultCharset = encoding;
		this.listCount = listCount;
		this.personCount = personCount;
		this.mailCount = mailCount;
		
		this.defaultSiteUrl = defaultSiteUrl;
		this.postmasterEmail = postmasterEmail;
	}

	/** */
	public String getDefaultCharset()
	{
		return this.defaultCharset;
	}

	/** */
	public int getListCount()
	{
		return this.listCount;
	}

	/** */
	public int getPersonCount()
	{
		return this.personCount;
	}

	/** */
	public int getMailCount()
	{
		return this.mailCount;
	}

	/** */
	public URL getDefaultSiteUrl()
	{
		return this.defaultSiteUrl;
	}

	/** */
	public InternetAddress getPostmasterEmail()
	{
		return this.postmasterEmail;
	}
}
