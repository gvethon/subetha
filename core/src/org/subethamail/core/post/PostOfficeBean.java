/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.post;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.annotation.EJB;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.core.admin.i.Encryptor;
import org.subethamail.core.post.i.Constant;
import org.subethamail.core.post.i.MailType;
import org.subethamail.core.util.OwnerAddress;
import org.subethamail.core.util.VERPAddress;
import org.subethamail.entity.Config;
import org.subethamail.entity.EmailAddress;
import org.subethamail.entity.Mail;
import org.subethamail.entity.MailingList;
import org.subethamail.entity.Person;
import org.subethamail.entity.Subscription;
import org.subethamail.entity.SubscriptionHold;
import org.subethamail.entity.dao.DAO;

/**
 * Implementation of the PostOffice interface.
 * 
 * @author Jeff Schnitzer
 */
@Stateless(name="PostOffice")
@SecurityDomain("subetha")
@RolesAllowed("siteAdmin")
public class PostOfficeBean implements PostOffice
{
	/** */
	private static Log log = LogFactory.getLog(PostOfficeBean.class);
	
	/** */
	@Resource(mappedName="java:/Mail") Session mailSession;

	/** */
	@EJB DAO dao;
	@EJB Encryptor encryptor;
	
	/** 
	 * Builds a message from a velocity template, context, and some
	 * information about sender and recipient. 
	 */
	class MessageBuilder
	{
		SubEthaMessage message;
		InternetAddress toAddress;
		InternetAddress fromAddress;
		String senderEmail;
		
		/** */
		public MessageBuilder(MailType kind, VelocityContext vctx)
		{
			StringWriter writer = new StringWriter(4096);
			
			try
			{
				Velocity.mergeTemplate(kind.getTemplate(), "UTF-8", vctx, writer);
			}
			catch (Exception ex)
			{
				log.fatal("Error merging " + kind.getTemplate(), ex);
				throw new EJBException(ex);
			}

			String mailSubject = (String)vctx.get("subject");
			String mailBody = writer.toString();
			
			// If we're in debug mode, annotate the subject.
			if (log.isDebugEnabled())
				mailSubject = kind.toString() + " " + mailSubject;

			try
			{
				this.message = new SubEthaMessage(mailSession);
				this.message.setSubject(mailSubject);
				this.message.setText(mailBody);
			}
			catch (MessagingException ex) { throw new RuntimeException(ex); }
		}
		
		/** */
		public void setTo(EmailAddress to)
		{
			to.bounceDecay();
			try
			{
				this.toAddress = new InternetAddress(to.getId(), to.getPerson().getName());
			}
			catch (UnsupportedEncodingException ex) { throw new RuntimeException(ex); }
		}
		
		/** */
		public void setTo(String to)
		{
			try
			{
				this.toAddress = new InternetAddress(to);
			}
			catch (AddressException ex) { throw new RuntimeException(ex); }
		}
		
		/** Must call setTo first */
		public void setFrom(MailingList list)
		{
			if (this.toAddress == null)
				throw new IllegalStateException("Must call setTo() first");
			
			// Set the list owner as the pretty from field
			String ownerAddress = OwnerAddress.makeOwner(list.getEmail());
			try
			{
				this.fromAddress = new InternetAddress(ownerAddress, list.getName());
			}
			catch (UnsupportedEncodingException ex) { throw new RuntimeException(ex); }
			
			// Set up the VERP bounce address as the envelope sender
			byte[] token = encryptor.encryptString(this.toAddress.getAddress());
			this.senderEmail = VERPAddress.encodeVERP(list.getEmail(), token);
		}
		
		/** Must call setTo first */
		public void setFrom(String from)
		{
			if (this.toAddress == null)
				throw new IllegalStateException("Must call setTo() first");
			
			try
			{
				this.fromAddress = new InternetAddress(from);
			}
			catch (AddressException ex) { throw new RuntimeException(ex); }
		}
		
		/**
		 * @return the message we have built.
		 */
		public SubEthaMessage getMessage() throws MessagingException
		{
			this.message.setRecipient(Message.RecipientType.TO, this.toAddress);
			this.message.setFrom(this.fromAddress);
			this.message.setEnvelopeFrom(this.senderEmail);
			
			// This minimizes the likelyhood of getting autoreplies
			this.message.setHeader("Precedence", "junk");

			return this.message;
		}
		
		/**
		 * Sends the message through JavaMail
		 */
		public void send()
		{
			try
			{
				Transport.send(this.getMessage());
			}
			catch (MessagingException ex) { throw new RuntimeException(ex); }
		}
	}
	
	/**
	 * Modifies the token with debug information which can be picked out
	 * by the unit tester.
	 */
	protected String token(String tok)
	{
		if (log.isDebugEnabled())
			return Constant.DEBUG_TOKEN_BEGIN + tok + Constant.DEBUG_TOKEN_END;
		else
			return tok;
	}
	
	/**
	 * @see PostOffice#sendPassword(EmailAddress)
	 */
	public void sendPassword(EmailAddress addy)
	{
		if (log.isDebugEnabled())
			log.debug("Sending password for " + addy.getId());
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("addy", addy);

		if (addy.getPerson().getSubscriptions().isEmpty())
		{
			String url = (String)this.dao.getConfigValue(Config.ID_SITE_URL);
			vctx.put("url", url);
			
			MessageBuilder builder = new MessageBuilder(MailType.FORGOT_PASSWORD, vctx);
			builder.setTo(addy);
			
			String postmaster = (String)this.dao.getConfigValue(Config.ID_SITE_POSTMASTER);
			builder.setFrom(postmaster);
			
			builder.send();
		}
		else
		{
			// Try a random list that the user is subscribed to.
			Subscription sub = addy.getPerson().getSubscriptions().values().iterator().next();
			vctx.put("url", sub.getList().getUrlBase());
			
			MessageBuilder builder = new MessageBuilder(MailType.FORGOT_PASSWORD, vctx);
			builder.setTo(addy);
			builder.setFrom(sub.getList());
			builder.send();
		}
	}

	/**
	 * @see PostOffice#sendConfirmSubscribeToken(MailingList, String, String)
	 */
	public void sendConfirmSubscribeToken(MailingList list, String email, String token)
	{
		if (log.isDebugEnabled())
			log.debug("Sending subscribe token to " + email);
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("token", this.token(token));
		vctx.put("email", email);
		vctx.put("list", list);

		MessageBuilder builder = new MessageBuilder(MailType.CONFIRM_SUBSCRIBE, vctx);
		builder.setTo(email);
		builder.setFrom(list);
		builder.send();
	}

	/**
	 * @see PostOffice#sendSubscribed(MailingList, Person, EmailAddress)
	 */
	public void sendSubscribed(MailingList relevantList, Person who, EmailAddress deliverTo)
	{
		if (log.isDebugEnabled())
			log.debug("Sending welcome to list msg to " + who);

		String email;
		if (deliverTo != null)
			email = deliverTo.getId();
		else
			email = who.getEmailAddresses().values().iterator().next().getId();
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("list", relevantList);
		vctx.put("person", who);
		vctx.put("email", email);
		
		MessageBuilder builder = new MessageBuilder(MailType.SUBSCRIBED, vctx);
		builder.setTo(email);
		builder.setFrom(relevantList);
		builder.send();
	}

	/**
	 * @see PostOffice#sendOwnerNewMailingList(MailingList, EmailAddress)
	 */
	public void sendOwnerNewMailingList(MailingList relevantList, EmailAddress address)
	{
		if (log.isDebugEnabled())
			log.debug("Sending notification of new mailing list " + relevantList + " to owner " + address);
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("addy", address);
		vctx.put("list", relevantList);
		
		MessageBuilder builder = new MessageBuilder(MailType.NEW_MAILING_LIST, vctx);
		builder.setTo(address);
		builder.setFrom(relevantList);
		builder.send();
	}

	/**
	 * @see PostOffice#sendAddEmailToken(Person, String, String)
	 */
	public void sendAddEmailToken(Person me, String email, String token)
	{
		if (log.isDebugEnabled())
			log.debug("Sending add email token to " + email);
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("token", this.token(token));
		vctx.put("email", email);
		vctx.put("person", me);
		
		if (me.getSubscriptions().isEmpty())
		{
			URL url = (URL)this.dao.getConfigValue(Config.ID_SITE_URL);
			vctx.put("url", url.toString());
			
			MessageBuilder builder = new MessageBuilder(MailType.CONFIRM_EMAIL, vctx);
			builder.setTo(email);
			
			InternetAddress postmaster = (InternetAddress)this.dao.getConfigValue(Config.ID_SITE_POSTMASTER);
			builder.setFrom(postmaster.toString());
			
			builder.send();
		}
		else
		{
			// Try a random list that the user is subscribed to.
			Subscription sub = me.getSubscriptions().values().iterator().next();
			vctx.put("url", sub.getList().getUrlBase());
			
			MessageBuilder builder = new MessageBuilder(MailType.CONFIRM_EMAIL, vctx);
			builder.setTo(email);
			builder.setFrom(sub.getList());
			builder.send();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.post.PostOffice#sendModeratorSubscriptionHeldNotice(org.subethamail.entity.EmailAddress, org.subethamail.entity.SubscriptionHold)
	 */
	public void sendModeratorSubscriptionHeldNotice(EmailAddress moderator, SubscriptionHold hold)
	{
		if (log.isDebugEnabled())
			log.debug("Sending sub held notice for " + hold + " to " + moderator);
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("hold", hold);
		vctx.put("moderator", moderator);
		
		MessageBuilder builder = new MessageBuilder(MailType.SUBSCRIPTION_HELD, vctx);
		
		builder.setTo(moderator);
		builder.setFrom(hold.getList());
		builder.send();
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.post.PostOffice#sendPosterMailHoldNotice(org.subethamail.entity.MailingList, java.lang.String, org.subethamail.entity.Mail, java.lang.String)
	 */
	public void sendPosterMailHoldNotice(MailingList relevantList, String posterEmail, Mail mail, String holdMsg)
	{
		if (log.isDebugEnabled())
			log.debug("Sending mail held notice to " + posterEmail);
		
		VelocityContext vctx = new VelocityContext();
		vctx.put("list", relevantList);
		vctx.put("mail", mail);
		vctx.put("holdMsg", holdMsg);
		vctx.put("email", posterEmail);
		
		MessageBuilder builder = new MessageBuilder(MailType.MAIL_HELD, vctx);
		builder.setTo(posterEmail);
		builder.setFrom(relevantList);
		builder.send();
	}
}