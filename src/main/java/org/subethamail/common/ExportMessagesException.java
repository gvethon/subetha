/*
 * $Id: NotFoundException.java 560 2006-05-29 21:30:15Z skot $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/ejb/NameAlreadyTakenException.java,v $
 */

package org.subethamail.common;

import javax.ejb.ApplicationException;

/** Indicates that something happened while exporting messages.
 **/
@ApplicationException(rollback=false)
public class ExportMessagesException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 */
	public ExportMessagesException(String msg)
	{
		super(msg);
	}

	/**
	 */
	public ExportMessagesException(Throwable cause)
	{
		super(cause);
	}
}