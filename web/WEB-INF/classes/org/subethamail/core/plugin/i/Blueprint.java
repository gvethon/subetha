/*
 * $Id: Blueprint.java 263 2006-05-04 20:58:25Z lhoriman $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/plugin/i/Blueprint.java $
 */

package org.subethamail.core.plugin.i;

/**
 * A blueprint generates the starting characteristics for
 * a mailing list.  When it executes, it uses the normal EJB
 * interfaces to configure a list with predefined roles, plugins,
 * and configuration.
 *
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public interface Blueprint
{
	/**
	 * @return a nice short name for this blueprint, like "Announce-Only List".
	 */
	public String getName();

	/**
	 * @return a lengthy description of what this blueprint does. 
	 */
	public String getDescription();
	
	/**
	 * Configure a freshly-created mailing list to the specification.
	 */
	public void configureMailingList(Long listId);
}
