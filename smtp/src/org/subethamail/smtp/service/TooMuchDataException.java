/*
 * $Id: TooMuchDataException.java 273 2006-05-07 04:00:41Z jon $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/mbean/BindStatisticsManagerMBean.java,v $
 */
package org.subethamail.smtp.service;

import java.io.IOException;

/**
 * Thrown by message listeners if an input stream provides more data than the
 * listener can handle.
 * 
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class TooMuchDataException extends IOException
{
	/** */
	public TooMuchDataException()
	{
		super();
	}

	/** */
	public TooMuchDataException(String message)
	{
		super(message);
	}
}
