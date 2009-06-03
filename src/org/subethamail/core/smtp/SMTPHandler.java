package org.subethamail.core.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.client.SMTPException;
import org.subethamail.client.SmartClient;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.io.DeferredFileOutputStream;

/**
 * This is the master SMTP handler for SubEtha.
 * 
 * Mail may arrive with one or more recipients.  Every recipient accepted by
 * the Injector results in a separate, split injection.
 * 
 * There may be an SMTP default host which is offered any addresses that are not
 * accepted by SubEtha.  The host is delivered only a single SMTP transaction for
 * all non-mailinglist addresses. 
 * 
 * Some example cases:
 * 
 * 1) Message arrives for single recipient, a valid list address.  It is handed to
 *    the Injector as a raw data stream on the socket.
 * 2) Message arrives for multiple recipients, all valid lists.  It is split and
 *    injected once for each list.
 * 3) Message arrives for one or more defaulted addresses.  The addresses are
 *    checked via SMTP RCPT TO on the default host and then a single DATA is
 *    written to the server.
 * 4) Message arrives for two lists and two defaulted addresses (both accepted via
 *    RCPT TO).  The data is split three times, injected twice into SubEtha and
 *    then sent once to the (already RCPT TO'd) default host.
 * 
 * @author Jeff Schnitzer
 */
public class SMTPHandler implements MessageHandlerFactory
{
	/** */
	private final static Logger log = LoggerFactory.getLogger(SMTPHandler.class);
	
	/** Beyond this, we buffer to disk instead of memory.  10MB */
	static final int DATA_DEFERRED_SIZE = 1024 * 1024 * 10;
		
	/** */
	protected SMTPService smtpService;

	/** */
	public SMTPHandler(SMTPService service)
	{
		this.smtpService = service;
	}

	/* */
	@Override
	public MessageHandler create(MessageContext ctx)
	{
		return new Handler(ctx);
	}

	/**
	 * The actual handler implementation
	 */
	public class Handler implements MessageHandler
	{
		MessageContext ctx;
		String from;
		
		List<String> ourLists = new ArrayList<String>();
		SmartClient fallbackConnection;
		
		/** */
		Handler(MessageContext ctx)
		{
			this.ctx = ctx;
		}

		/* */
		@Override
		public void from(String from) throws RejectException
		{
			this.from = from;
		}

		/* */
		@Override
		public void recipient(String recipient) throws RejectException
		{
			if (smtpService.getInjector().accept(recipient))
			{
				this.ourLists.add(recipient);
			}
			else
			{
				SmartClient client = this.getFallbackConnection();
				if (client == null)
				{
					// No defaulting config, reject it
					throw new RejectException(553, "<" + recipient + "> address unknown");
				}
				else
				{
					try
					{
						try
						{
							if (!client.sentFrom())
								client.from(this.from);
							
							client.to(recipient);
						}
						catch (SMTPException ex)
						{
							throw new RejectException(ex.getResponse().getCode(), ex.getResponse().getMessage());
						}
					}
					catch (IOException ex)
					{
						throw new RejectException(554, ex.getMessage());
					}
				}
			}
		}

		/* */
		@Override
		public void data(InputStream data)
			throws RejectException, TooMuchDataException, IOException
		{
			List<Deliverer> deliveries = this.makeDeliverers();
			if (deliveries.size() == 1)
			{
				deliveries.get(0).deliver(data);
			}
			else
			{
				DeferredFileOutputStream dfos = new DeferredFileOutputStream(DATA_DEFERRED_SIZE);
				
				try
				{
					// Buffer it
					int value;
					while ((value = data.read()) >= 0)
						dfos.write(value);
					
					// Deliver it, but track the last exception just in case
					// nobody succeeds - we'll throw it as if it was critical.
					boolean anyoneAtAll = false;
					Exception lastProblem = null;
					
					for (Deliverer deliv: deliveries)
					{
						try
						{
							deliv.deliver(dfos.getInputStream());
							anyoneAtAll = true;
						}
						catch (Exception ex)
						{
							log.error("Error delivering to " + deliv.toString(), ex);
							lastProblem = ex;
						}
					}
					
					if (!anyoneAtAll)
					{
						if (lastProblem instanceof IOException)
							throw (IOException)lastProblem;
						else if (lastProblem instanceof RejectException)
							throw (RejectException)lastProblem;
						else if (lastProblem instanceof RuntimeException)
							throw (RuntimeException)lastProblem;
						else
							throw new RuntimeException(lastProblem);
					}
				}
				finally
				{
					dfos.close();
				}
			}
		}
		
		/* */
		@Override
		public void done()
		{
			if (this.fallbackConnection != null)
				this.fallbackConnection.close();
		}

		/** 
		 * Gets the current connection to the remote host, or creates one.
		 * @throws RejectException if something goes wrong connecting to the backend 
		 */
		SmartClient getFallbackConnection() throws RejectException
		{
			if (this.fallbackConnection == null)
			{
				String hostAndPort = smtpService.getFallbackHost();
				if (hostAndPort != null)
				{
					String[] split = hostAndPort.split(":");
					int port = split.length > 1 ? Integer.parseInt(split[1]) : 25;
					
					try
					{
						this.fallbackConnection = new SmartClient(split[0], port, this.ctx.getSMTPServer().getHostName());
					}
					catch (IOException e)
					{
						throw new RejectException(554, e.toString());
					}
				}
			}
			
			return this.fallbackConnection;
		}
		
		/** Makes a nice list of deliverers based on who wants our data */
		List<Deliverer> makeDeliverers()
		{
			List<Deliverer> result = new ArrayList<Deliverer>(this.ourLists.size() + 1);
			
			for (String list: this.ourLists)
				result.add(new OurDeliverer(smtpService.getInjector(), this.from, list));
			
			if (this.fallbackConnection != null && this.fallbackConnection.sentTo())
				result.add(new FallbackDeliverer(this.fallbackConnection));
			
			return result;
		}
	}
}