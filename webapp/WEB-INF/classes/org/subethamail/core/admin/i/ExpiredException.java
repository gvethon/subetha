/*
 * $Id: ExpiredException.java 263 2006-05-04 20:58:25Z lhoriman $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/ejb/NameAlreadyTakenException.java,v $
 */

package org.subethamail.core.admin.i;

import java.security.GeneralSecurityException;

/**
 * Thrown when decrypting a token that has expired.
 */
@SuppressWarnings("serial")
public class ExpiredException extends GeneralSecurityException
{
	/**
	 */
	public ExpiredException(String msg)
	{
		super(msg);
	}
	
	/**
	 */
	public ExpiredException(Throwable cause)
	{
		super(cause);
	}
}

