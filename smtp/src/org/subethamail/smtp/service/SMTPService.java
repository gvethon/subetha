/*
 * $Id: SMTPService.java 273 2006-05-07 04:00:41Z jon $
 * $URL$
 */
package org.subethamail.smtp.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.smtp.i.MessageListener;
import org.subethamail.smtp.i.MessageListenerRegistry;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Ian McFarland
 * @author Jeff Schnitzer
 * @author Jon Stevens
 */
@Service(name="SMTPService", objectName="subetha:service=SMTP")
@SecurityDomain("subetha")
@RolesAllowed("siteAdmin")
public class SMTPService implements SMTPManagement, MessageListenerRegistry
{
	/** */
	private static Log log = LogFactory.getLog(SMTPService.class);
	
	/** */
	public static final int DEFAULT_PORT = 2500;

	/**
	 * There is no ConcurrentHashSet, so we make up our own by mapping the
	 * object to itself.
	 */
	private Map<MessageListener, MessageListener> listeners = new ConcurrentHashMap<MessageListener, MessageListener>();
	
	private int port = DEFAULT_PORT;
	private String hostName;
	
	private SMTPServer smtpServer;

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.i.MessageListenerRegistry#register(org.subethamail.smtp.i.MessageListener)
	 */
	public void register(MessageListener listener)
	{
		if (log.isInfoEnabled())
			log.info("Registering " + listener);
		
		this.listeners.put(listener, listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.i.MessageListenerRegistry#deregister(org.subethamail.smtp.i.MessageListener)
	 */
	public void deregister(MessageListener listener)
	{
		if (log.isInfoEnabled())
			log.info("De-registering " + listener);
		
		this.listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.service.SMTPManagement#start()
	 */
	@PermitAll
	public void start() throws IOException
	{
		if (this.smtpServer != null)
			throw new IllegalStateException("SMTPServer already running");
		
		if (this.hostName == null)
		{
			try
			{
				this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
			}
			catch (UnknownHostException e)
			{
				this.hostName = "localhost";
			}
		}

		InetAddress binding = null;
		
		String bindAddress = System.getProperty("jboss.bind.address");
		if (bindAddress != null && !"0.0.0.0".equals(bindAddress))
			binding = InetAddress.getByName(bindAddress);

		log.info("Starting SMTP service: " + (binding==null ? "*" : binding) + ":" + port);
		
		smtpServer = new SMTPServer(hostName, binding, port, listeners);
		smtpServer.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.service.SMTPManagement#stop()
	 */
	@PermitAll
	public void stop()
	{
		log.info("Stopping SMTP service");
		this.smtpServer.stop();
		this.smtpServer = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.service.SMTPManagement#setPort(int)
	 */
	@PermitAll
	public void setPort(int port)
	{
		if (this.smtpServer != null)
			throw new IllegalStateException("SMTPServer already running");
		
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.service.SMTPManagement#getPort()
	 */
	@PermitAll
	public int getPort()
	{
		return this.port;
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.service.SMTPManagement#setHostName(java.lang.String)
	 */
	@PermitAll
	public void setHostName(String hostname)
	{
		if (this.smtpServer != null)
			throw new IllegalStateException("SMTPServer already running");
		
		this.hostName = hostname;
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.service.SMTPManagement#getHostName()
	 */
	@PermitAll
	public String getHostName()
	{
		return hostName;
	}
}
