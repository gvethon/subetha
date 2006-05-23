package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.i.MessageListener;

/**
 * Main SMTPServer class
 *
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
@SuppressWarnings("serial")
public class SMTPServer implements Runnable
{
	private static Log log = LogFactory.getLog(SMTPServer.class);

	private String hostName;
	private InetAddress bindAddress;
	private int port;
	private Map<MessageListener, MessageListener> listeners;

	private CommandHandler commandHandler;
	
	private ServerSocket serverSocket;
	private boolean go = false;
	
	private Thread serverThread;
	private Thread watchdogThread;

	private ThreadGroup connectionHanderGroup;
	
	/** 
	 * set a hard limit on the maximum number of connections this server will accept 
	 * once we reach this limit, the server will gracefully reject new connections.
	 */
	private static final int MAX_CONNECTIONS = 1000;

	public SMTPServer(String hostname, InetAddress bindAddress, int port, Map<MessageListener, MessageListener> listeners) 
		throws UnknownHostException
	{
		this.hostName = hostname;
		this.bindAddress = bindAddress;
		this.port = port;
		this.listeners = listeners;

		this.commandHandler = new CommandHandler();
		
	}

	public void start()
	{
		if (this.serverThread != null)
			throw new IllegalStateException("SMTPServer already started");

		this.serverThread = new Thread(this, SMTPServer.class.getName());
		this.serverThread.start();
		
		this.watchdogThread = new Watchdog(this);
		this.watchdogThread.start();
	}

	public void stop()
	{
		this.go = false;
		this.serverThread = null;
		this.watchdogThread = null;

		// force a socket close for good measure
		try
		{
			if (this.serverSocket != null && this.serverSocket.isBound() && !this.serverSocket.isClosed())
				this.serverSocket.close();
		}
		catch (IOException e)
		{
		}
	}
	
	public void run()
	{
		try
		{
			if (this.bindAddress == null)
				this.serverSocket = new ServerSocket(this.port, 50);
			else
				this.serverSocket = new ServerSocket(this.port, 50, this.bindAddress);
			
			this.connectionHanderGroup = new ThreadGroup(SMTPServer.class.getName() + " ConnectionHandler Group");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		this.go = true;
		while (this.go)
		{
			try
			{
				ConnectionHandler connectionHandler = new ConnectionHandler(this, serverSocket.accept());
				connectionHandler.start();
			}
			catch (IOException ioe)
			{
				
//				Avoid this exception when shutting down.
//				20:34:50,624 ERROR [STDERR]     at java.net.PlainSocketImpl.socketAccept(Native Method)
//				20:34:50,624 ERROR [STDERR]     at java.net.PlainSocketImpl.accept(PlainSocketImpl.java:384)
//				20:34:50,624 ERROR [STDERR]     at java.net.ServerSocket.implAccept(ServerSocket.java:450)
//				20:34:50,624 ERROR [STDERR]     at java.net.ServerSocket.accept(ServerSocket.java:421)
//				20:34:50,624 ERROR [STDERR]     at org.subethamail.smtp2.SMTPServer.run(SMTPServer.java:92)
//				20:34:50,624 ERROR [STDERR]     at java.lang.Thread.run(Thread.java:613)
				if (go)
				{
					log.error(ioe.toString());
				}
			}
		}

		try
		{
			if (this.serverSocket != null && this.serverSocket.isBound() && !this.serverSocket.isClosed())
				this.serverSocket.close();
			log.info("SMTP Server socket shut down.");
		}
		catch (IOException e)
		{
			log.error("Failed to close server socket.", e);
		}
	}

	public String getHostName()
	{
		return this.hostName;
	}

	public String getVersion()
	{
		return "1.0";
	}
	
	public String getName()
	{
		return "SubEthaMail Server";
	}

	/**
	 * The Listeners are what the SMTPServer delivers to.
	 * 
	 * @return A Map of MessageListener objects.
	 */
	public Map<MessageListener, MessageListener> getListeners()
	{
		return this.listeners;
	}

	/**
	 * The CommandHandler manages handling the SMTP commands
	 * such as QUIT, MAIL, RCPT, DATA, etc.
	 * 
	 * @return An instance of CommandHandler
	 */
	public CommandHandler getCommandHandler()
	{
		return this.commandHandler;
	}

	protected ThreadGroup getConnectionGroup()
	{
		return this.connectionHanderGroup;
	}

	public int getNumberOfConnections()
	{
		return this.connectionHanderGroup.activeCount();
	}
	
	public boolean hasTooManyConnections()
	{
		return (getNumberOfConnections() >= MAX_CONNECTIONS);
	}
	
	/**
	 * A watchdog thread that makes sure that
	 * connections don't go stale. It prevents
	 * someone from opening up MAX_CONNECTIONS to 
	 * the server and holding onto them for more than
	 * 1 minute.
	 */
	private class Watchdog extends Thread
	{
		private SMTPServer server;
		private Thread[] groupThreads = new Thread[MAX_CONNECTIONS];
		private boolean go = true;

		public Watchdog(SMTPServer server)
		{
			super(Watchdog.class.getName());
			this.server = server;
			setPriority(Thread.MAX_PRIORITY / 3);
		}

		public void quit()
		{
			go = false;
		}

		public void run()
		{
			while (go)
			{
				ThreadGroup connectionGroup = this.server.getConnectionGroup();
				connectionGroup.enumerate(this.groupThreads);

				for (int i=0; i<connectionGroup.activeCount();i++)
				{
					ConnectionHandler aThread = ((ConnectionHandler)this.groupThreads[i]);
					if (aThread != null)
					{
						// one minute timeout
						long lastActiveTime = aThread.getLastActiveTime() + (1000 * 60 * 1);
						if (lastActiveTime < System.currentTimeMillis())
						{
							try
							{
								aThread.timeout();
							}
							catch (IOException ioe)
							{
								log.debug("Lost connection to client during timeout");
							}
						}
					}
				}
				try
				{
					// go to sleep for 10 seconds.
					sleep(1000 * 10);
				}
				catch (InterruptedException e)
				{
					// ignore
				}
			}
		}
	}
}
