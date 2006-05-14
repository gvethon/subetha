/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstracts the handling of -owner list addresses.
 * 
 * An owner address looks like this:
 * 
 * announce-owner@domain.com
 * 
 * The extracted email will be announce@domain.com
 * 
 * @author Jeff Schnitzer
 */
public class OwnerAddress
{
	/** */
	private static Log log = LogFactory.getLog(OwnerAddress.class);
	
	/** This is the suffix of all VERP'd addresses, plus the '@' */
	public static final String SUFFIX = "-owner@";
	
	/**
	 * @return null if the address was not an owner address  
	 */
	public static String getList(String maybeOwner)
	{
		// Note the suffix includes the @
		int suffixIndex = maybeOwner.indexOf(SUFFIX);
		if (suffixIndex < 0)
			return null;
		
		String email = maybeOwner.substring(0, suffixIndex) + '@' + maybeOwner.substring(suffixIndex+SUFFIX.length());
		
		if (log.isDebugEnabled())
			log.debug(maybeOwner + " becomes " + email);
		
		return email;
	}
	
	/**
	 * @return an email address encoded with the token.
	 */
	public static String makeOwner(String listEmail)
	{
		int atIndex = listEmail.indexOf('@');
		
		return listEmail.substring(0, atIndex)
			+ SUFFIX 
			+ listEmail.substring(atIndex+1);
	}
}


	