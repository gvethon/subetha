/*
 * $Id$
 * $URL$
 */

package org.subethamail.load;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.i.MessageListener;
import org.subethamail.smtp.i.TooMuchDataException;

/**
 * Listener which counts the number of messages that it receives. 
 * 
 * @author Jeff Schnitzer
 */
public class CountingListener implements MessageListener
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(CountingListener.class);
	
	/** Number of messages received total */
	int totalCount;
	
	/** */
	public CountingListener()
	{
	}

	/** Always accept everything */
	public boolean accept(String from, String recipient)
	{
		return true;
	}

	/** Indicate we have one more */
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
	{
		// Read the whole stream
		while (data.read() >= 0)
			;	// do nothing
		
		this.totalCount++;
	}
	
	/** */
	public int getTotalCount()
	{
		return this.totalCount;
	}
}
