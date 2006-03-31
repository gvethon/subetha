/*
 * $Id: PersonData.java 105 2006-02-27 10:06:27Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/core/src/com/blorn/core/blog/i/PersonData.java $
 */

package org.subethamail.plugin.blueprint;

import javax.annotation.security.RunAs;

import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.core.plugin.i.helper.AbstractBlueprint;
import org.subethamail.core.plugin.i.helper.Lifecycle;

/**
 * Creates an announce-only list. 
 * 
 * @author Jeff Schnitzer
 */
@Service
@SecurityDomain("subetha")
@RunAs("siteAdmin")
public class AnnounceOnlyBlueprint extends AbstractBlueprint implements Lifecycle
//TODO:  remove the implements clause when http://jira.jboss.org/jira/browse/EJBTHREE-489 is fixed
{
	/** */
	public String getName()
	{
		return "Announce-Only List";
	}

	/** */
	public String getDescription()
	{
		return 
			"Create a list which allows only moderators to post.  Normal" +
			" users are not allowd to view the subscriber list.";
	}
	
	/** */
	public void configureMailingList(Long listId)
	{
		// TODO
	}
}
