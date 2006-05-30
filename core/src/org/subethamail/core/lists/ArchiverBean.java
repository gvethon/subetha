/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.lists;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.EJB;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.common.ImportMessagesException;
import org.subethamail.common.MailUtils;
import org.subethamail.common.NotFoundException;
import org.subethamail.common.Permission;
import org.subethamail.common.PermissionException;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.core.deliv.i.Deliverator;
import org.subethamail.core.filter.FilterRunner;
import org.subethamail.core.injector.Detacher;
import org.subethamail.core.injector.i.Injector;
import org.subethamail.core.lists.i.Archiver;
import org.subethamail.core.lists.i.ArchiverRemote;
import org.subethamail.core.lists.i.AttachmentPartData;
import org.subethamail.core.lists.i.InlinePartData;
import org.subethamail.core.lists.i.ListData;
import org.subethamail.core.lists.i.ListMgr;
import org.subethamail.core.lists.i.MailData;
import org.subethamail.core.lists.i.MailSummary;
import org.subethamail.core.lists.i.TextPartData;
import org.subethamail.core.util.PersonalBean;
import org.subethamail.core.util.Transmute;
import org.subethamail.entity.Attachment;
import org.subethamail.entity.Mail;
import org.subethamail.entity.MailingList;
import org.subethamail.entity.Person;
import org.subethamail.entity.dao.DAO;

import com.sun.mail.util.LineInputStream;

/**
 * Implementation of the Archiver interface.
 * 
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
@Stateless(name="Archiver")
@SecurityDomain("subetha")
@PermitAll
@RunAs("siteAdmin")
public class ArchiverBean extends PersonalBean implements Archiver, ArchiverRemote
{

	@EJB Deliverator deliverator;
	@EJB FilterRunner filterRunner;
	@EJB Detacher detacher;
	@EJB DAO dao;
	@EJB ListMgr listManager;
	@EJB Injector injector;

	
	/** */
	private static Log log = LogFactory.getLog(ArchiverBean.class);

	/** */
	@Resource(mappedName="java:/Mail") private Session mailSession;

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#sendTo(java.lang.Long, String email)
	 */
	public void sendTo(Long mailId, String email) throws NotFoundException
	{
		this.deliverator.deliver(mailId, email);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#getThreads(java.lang.Long)
	 */
	public List<MailSummary> getThreads(Long listId, int skip, int count) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();
		
		// Are we allowed to view archives?
		MailingList list = this.getListFor(listId, Permission.READ_ARCHIVES, me);
		
		List<Mail> mails = this.dao.findMailByList(listId, skip, count);
		
		// This is fun.  Assemble the thread relationships.
		SortedSet<Mail> roots = new TreeSet<Mail>();
		for (Mail mail: mails)
		{
			Mail parent = mail;
			while (parent.getParent() != null)
				parent = parent.getParent();

			roots.add(parent);
		}
		
		// Figure out if we're allowed to see emails
		boolean showEmail = list.getPermissionsFor(me).contains(Permission.VIEW_ADDRESSES);
		
		// Now generate the entire summary
		return Transmute.mailSummaries(roots, showEmail, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#countMailByList(java.lang.Long)
	 */
	public int countMailByList(Long listId)
	{
		return this.dao.countMailByList(listId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#getMessage(java.lang.Long, OutputStream)
	 */
	public void writeMessage(Long mailId, OutputStream stream) throws NotFoundException, PermissionException
	{
		Mail mail = this.getMailFor(mailId, Permission.READ_ARCHIVES);

		try 
		{
			SubEthaMessage msg = new SubEthaMessage(this.mailSession, mail.getContent());

			this.filterRunner.onSend(msg, mail);
			
			this.detacher.attach(msg);
			
			msg.writeTo(stream);
		} 
		catch (Exception e)
		{
			if (log.isDebugEnabled()) log.debug("error getting exception getting mail#" + mailId + "\n" + e.toString());
		}	
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#writeAttachment(java.lang.Long, java.io.OutputStream)
	 */
	public void writeAttachment(Long attachmentId, OutputStream stream) throws NotFoundException, PermissionException
	{
		Attachment a = this.dao.findAttachment(attachmentId);
		a.getMail().getList().checkPermission(getMe(), Permission.READ_ARCHIVES);

		Blob data = a.getContent();
		try
		{
			BufferedInputStream bis = new BufferedInputStream(data.getBinaryStream());	

			int stuff;
			while ((stuff = bis.read()) >= 0)
				stream.write(stuff);
		}
		catch (SQLException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (IOException ex) 
		{
			throw new RuntimeException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#getAttachmentContentType(java.lang.Long)
	 */
	public String getAttachmentContentType(Long attachmentId) throws NotFoundException, PermissionException
	{
		Attachment a = this.dao.findAttachment(attachmentId);
		a.getMail().getList().checkPermission(getMe(), Permission.READ_ARCHIVES);
		return a.getContentType();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#getMail(java.lang.Long)
	 */
	public MailData getMail(Long mailId) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();
		
		// Are we allowed to view archives?
		Mail mail = this.getMailFor(mailId, Permission.READ_ARCHIVES, me);
		
		// Figure out if we're allowed to see emails
		boolean showEmail = mail.getList().getPermissionsFor(me).contains(Permission.VIEW_ADDRESSES);
		
		MailData data = this.makeMailData(mail, showEmail);
		
		Mail root = mail;
		while (root.getParent() != null)
			root = root.getParent();
		
		// This trick inserts us into the thread hierarchy we create.
		data.setThreadRoot(Transmute.mailSummary(root, showEmail, data));
		
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#importMessages(Long, InputStream)
	 */
	public void importMessages(Long listId, InputStream mboxStream) throws NotFoundException, PermissionException, ImportMessagesException
	{

		MailingList list = dao.findMailingList(listId);

		list.checkPermission(getMe(), Permission.IMPORT_MESSAGES);
		
		try 
		{
		    LineInputStream in = new LineInputStream(mboxStream);
			String line = null;
			String fromLine = null;
			String envelopeSender = null;
			ByteArrayOutputStream buf = null;
	
			for (line = in.readLine(); line != null; line = in.readLine())
			{
				if (line.indexOf("From ") == 0)
				{
					if (buf != null)
					{
						byte[] bytes = buf.toByteArray();
						ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
						injector.inject(envelopeSender, list.getEmail(), bin, true, true, false, false);
					}
					fromLine = line;
					envelopeSender = MailUtils.getMboxFrom(fromLine);
					buf = new ByteArrayOutputStream();
				}
				else if (buf != null)
				{
					byte[] bytes = MailUtils.decodeMboxFrom(line).getBytes();
					buf.write(bytes, 0, bytes.length);
					buf.write(10); // LF
				}
			}
			if (buf != null)
			{
				byte[] bytes = buf.toByteArray();
				ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
				injector.inject(envelopeSender, list.getEmail(), bin, true, true, false, false);
			}
		}
		catch(IOException ex)
		{
			throw new ImportMessagesException(ex);
		}
		catch(MessagingException ex)
		{
			throw new ImportMessagesException(ex);
		}
	}
	
	/**
	 * Makes the base mail data.  Doesn't set the threadRoot.
	 */
	protected MailData makeMailData(Mail raw, boolean showEmail) throws NotFoundException
	{
		try
		{
			InternetAddress addy = raw.getFromAddress();
		
			SubEthaMessage msg = new SubEthaMessage(this.mailSession, raw.getContent());
			
			List<InlinePartData> inlineParts = new ArrayList<InlinePartData>();
			List<AttachmentPartData> attachmentParts = new ArrayList<AttachmentPartData>();

			for (Part part: msg.getParts()) 
			{
				String contentType = part.getContentType();
				if (contentType.startsWith(SubEthaMessage.DETACHMENT_MIME_TYPE))
				{
					//we need the orig Content-Type before the message was munged
					contentType = part.getHeader(SubEthaMessage.HDR_ORIGINAL_CONTENT_TYPE)[0];
					//put back the orig Content-Type
					part.setHeader(SubEthaMessage.HDR_CONTENT_TYPE, contentType);

					String name = part.getFileName();
					
					// just in case we are working with something that isn't
					// C-D: attachment; filename=""
					if (name == null || name.length() == 0)
						name = MailUtils.getNameFromContentType(contentType);
					
					Long id = (Long) part.getContent();
					
					//TODO: Set the correct size. This should be the size of the Attachment.content (Blob)
					AttachmentPartData apd = new AttachmentPartData(id, contentType, name, 0);
					attachmentParts.add(apd);
				}
				else
				{
					// not an attachment cause it isn't stored as a detached part.
					Object content = part.getContent();

					String name = part.getFileName();
					
					// just in case we are working with something that isn't
					// C-D: attachment; filename=""
					if (name == null || name.length() == 0)
						name = MailUtils.getNameFromContentType(contentType);
					
					InlinePartData ipd;
					if (content instanceof String)
					{
						ipd = new TextPartData((String)content, part.getContentType(), name, part.getSize());
					}
					else 
					{
						ipd = new InlinePartData(content, part.getContentType(), name, part.getSize());
					}

					inlineParts.add(ipd);
				}
			}
			
			return new MailData(
					raw.getId(),
					raw.getSubject(),
					showEmail ? addy.getAddress() : null,
					addy.getPersonal(),
					raw.getDateCreated(),
					Transmute.mailSummaries(raw.getReplies(), showEmail, null),
					raw.getList().getId(),
					inlineParts,
					attachmentParts);
		}
		catch (MessagingException ex)
		{
			// Should be impossible since everything was already run through
			// JavaMail when the data was imported.
			throw new RuntimeException(ex);
		}
		catch (IOException ex)
		{
			// Ditto
			throw new RuntimeException(ex);
		}
	}
}
