/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.admin.i;

import javax.ejb.Local;


/**
 * Miscellaneous administrative tools requiring god role.
 * 
 * @author Jeff Schnitzer
 */
@Local
public interface Plumber
{
	/** */
	public static final String JNDI_NAME = "subetha/Plumber/local";
	
	/**
	 * Puts a log message in the adminstrator log at level INFO.
	 */
	public void log(String msg);
	
	/**
	 * Temporarily override the smtp server used.
	 * 
	 * @param host can be hostname:port
	 */
	public void overrideSmtpServer(String host);
	
	/**
	 * Restore default stmp server.
	 */
	public void restoreStmpServer();
}
