package org.subethamail.core.lists;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import lombok.extern.java.Log;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.subethamail.common.ExportMessagesException;
import org.subethamail.common.ImportMessagesException;
import org.subethamail.common.MailUtils;
import org.subethamail.common.NotFoundException;
import org.subethamail.common.SearchException;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.core.deliv.i.Deliverator;
import org.subethamail.core.filter.FilterRunner;
import org.subethamail.core.injector.Detacher;
import org.subethamail.core.injector.i.Injector;
import org.subethamail.core.lists.i.Archiver;
import org.subethamail.core.lists.i.ExportFormat;
import org.subethamail.core.lists.i.ListMgr;
import org.subethamail.core.lists.i.MailData;
import org.subethamail.core.lists.i.MailSummary;
import org.subethamail.core.lists.i.SearchHit;
import org.subethamail.core.lists.i.SearchResult;
import org.subethamail.core.plugin.i.IgnoreException;
import org.subethamail.core.post.OutboundMTA;
import org.subethamail.core.util.PersonalBean;
import org.subethamail.core.util.Transmute;
import org.subethamail.entity.Attachment;
import org.subethamail.entity.Mail;
import org.subethamail.entity.MailingList;
import org.subethamail.entity.Person;
import org.subethamail.entity.i.Permission;
import org.subethamail.entity.i.PermissionException;

import com.caucho.remote.HessianService;
import com.sun.mail.util.LineInputStream;

/**
 * Implementation of the Archiver interface.
 *
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
@Stateless(name="Archiver")
@PermitAll
@RunAs(Person.ROLE_ADMIN)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@HessianService(urlPattern="/api/Archiver")
@Log
public class ArchiverBean extends PersonalBean implements Archiver
{
	@Inject Deliverator deliverator;
	@Inject FilterRunner filterRunner;
	@Inject Detacher detacher;
	@Inject ListMgr listManager;
	@Inject Injector injector;

	/** */
	@Inject @OutboundMTA Session mailSession;

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#sendTo(java.lang.Long, String email)
	 */
	@RolesAllowed(Person.ROLE_USER)
	public void sendTo(Long mailId, String email) throws NotFoundException
	{
		this.deliverator.deliverToEmail(mailId, email);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#getThreads(java.lang.Long)
	 */
	public List<MailSummary> getThreads(Long listId, int skip, int count) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();

		// Are we allowed to view archives?
		MailingList list = this.getListFor(listId, Permission.VIEW_ARCHIVES, me);

		List<Mail> mails = this.em.findMailByList(listId, skip, count);

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
	 * @see org.subethamail.core.lists.i.Archiver#getThread(java.lang.Long)
	 */
	public MailSummary getThread(Long mailId) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();

		Mail mail = this.getMailFor(mailId, Permission.VIEW_ARCHIVES, me);

		while (mail.getParent() != null) { mail = mail.getParent(); }

		return Transmute.mailSummary(mail, mail.getList().getPermissionsFor(me).contains(Permission.VIEW_ADDRESSES), null);
	}

	public MailData[] getThreadMessages(Long mailId) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();
		Mail mail = this.getMailFor(mailId, Permission.VIEW_ARCHIVES);
		return Transmute.mailThread(mail, mail.getList().getPermissionsFor(me).contains(Permission.VIEW_ADDRESSES));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#search(java.lang.Long, java.lang.String, int, int)
	 */
	public SearchResult search(Long listId, String query, int skip, int count) throws NotFoundException, PermissionException, SearchException
	{
		Person me = this.getMe();

		// Are we allowed to view archives?
		MailingList list = this.getListFor(listId, Permission.VIEW_ARCHIVES, me);

		// We use Hibernate Search for this
		FullTextEntityManager ftem = Search.getFullTextEntityManager(this.em);
		QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, new String[]{"subject", "content"}, new StandardAnalyzer(Version.LUCENE_29));

		org.apache.lucene.search.Query luceneQuery;
		try
		{
			luceneQuery = parser.parse(query);
		}
		catch (ParseException e) { throw new SearchException(e); }
		
		FullTextQuery ftquery = ftem.createFullTextQuery(luceneQuery, Mail.class);
		ftquery.setFirstResult(skip).setMaxResults(count);
		TermsFilter filter = new TermsFilter();
		filter.addTerm(new Term("list", listId.toString()));
		ftquery.setFilter(filter);
		
		@SuppressWarnings("unchecked")
		List<Mail> rawHits = ftquery.getResultList();  		

		// Now turn these into real hits
		List<SearchHit> hits = new ArrayList<SearchHit>(rawHits.size());

		for (Mail mail: rawHits)
		{
			hits.add(new SearchHit(
					mail.getId(),
					mail.getSubject(),
					list.getPermissionsFor(me).contains(Permission.VIEW_ADDRESSES)
						? mail.getFromAddress().getAddress() : null,
					mail.getFromAddress().getPersonal(),
					mail.getSentDate()
					));
		}

		return new SearchResult(ftquery.getResultSize(), hits);
	}

	/* (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#countMailByList(java.lang.Long)
	 */
	public int countMailByList(Long listId)
	{
		return this.em.countMailByList(listId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#getMessage(java.lang.Long, OutputStream)
	 */
	public void writeMessage(Long mailId, OutputStream stream) throws NotFoundException, PermissionException
	{
		Mail mail = this.getMailFor(mailId, Permission.VIEW_ARCHIVES);

		try
		{
			this.writeMessage(mail, stream);
		}
		catch (Exception e)
		{
		    LogRecord logRecord=new LogRecord(Level.FINE,"exception getting mail#{0}");
		    logRecord.setParameters(new Object[]{mailId});
		    logRecord.setThrown(e);
            log.log(logRecord);
		}
	}
	/**
	 * Writes a message out to the stream. First current filters are applied, and then message is written.
	 * If an {@link IgnoreException} is thrown, ignore the message and return.
	 *
	 * @param mail The message to write
	 * @param stream The stream to write to
	 */
	protected void writeMessage(Mail mail, OutputStream stream) throws MessagingException, IOException
	{
		SubEthaMessage msg = new SubEthaMessage(this.mailSession, mail.getContent());
		try
		{
			this.filterRunner.onSend(msg, mail);
		}
		catch (IgnoreException e)
		{
		    LogRecord logRecord=new LogRecord(Level.FINE,"Ignoring mail {0}");
		    logRecord.setParameters(new Object[]{mail});
		    logRecord.setThrown(e);
            log.log(logRecord);
			return;
		}
		this.detacher.attach(msg);
		msg.writeTo(stream);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#writeAttachment(java.lang.Long, java.io.OutputStream)
	 */
	public void writeAttachment(Long attachmentId, OutputStream stream) throws NotFoundException, PermissionException
	{
		Attachment a = this.em.get(Attachment.class, attachmentId);
		a.getMail().getList().checkPermission(this.getMe(), Permission.VIEW_ARCHIVES);

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
		Attachment a = this.em.get(Attachment.class, attachmentId);
		a.getMail().getList().checkPermission(this.getMe(), Permission.VIEW_ARCHIVES);
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
		Mail mail = this.getMailFor(mailId, Permission.VIEW_ARCHIVES, me);

		// Figure out if we're allowed to see emails
		boolean showEmail = mail.getList().getPermissionsFor(me).contains(Permission.VIEW_ADDRESSES);

		MailData data = Transmute.mailData(mail, showEmail);

		Mail root = mail;
		while (root.getParent() != null)
			root = root.getParent();

		MailSummary mailSummary = Transmute.mailSummary(root, showEmail, data);
		
		// This trick inserts us into the thread hierarchy we create.
		data.setThreadRoot(mailSummary);

		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#importMessages(Long, InputStream)
	 */
	public int importMessages(Long listId, InputStream mboxStream) throws NotFoundException, PermissionException, ImportMessagesException
	{
		MailingList list = this.getListFor(listId, Permission.IMPORT_MESSAGES);

		try
		{
		    LineInputStream in = new LineInputStream(mboxStream);
			String line = null;
			String fromLine = null;
			String envelopeSender = null;
			ByteArrayOutputStream buf = null;
			Date fallbackDate = new Date();
			int count = 0;

			for (line = in.readLine(); line != null; line = in.readLine())
			{
				if (line.indexOf("From ") == 0)
				{
					if (buf != null)
					{
						byte[] bytes = buf.toByteArray();
						ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
						Date sent = this.injector.importMessage(list.getId(), envelopeSender, bin, true, fallbackDate);
						if (sent != null)
							fallbackDate = sent;

						count++;
					}

					fromLine = line;
					envelopeSender = MailUtils.getMboxFrom(fromLine);
					if (envelopeSender == null)
						continue;
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
				this.injector.importMessage(list.getId(), envelopeSender, bin, true, fallbackDate);
				count++;
			}

			return count;
		}
		catch (IOException ex)
		{
			throw new ImportMessagesException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#deleteMail(java.lang.Long)
	 */
	@WebMethod
	public Long deleteMail(Long mailId) throws NotFoundException, PermissionException
	{
		Mail mail = this.getMailFor(mailId, Permission.DELETE_ARCHIVES);

		// Make all the children belong to the parent
		Mail parent = mail.getParent();

		if (parent != null)
			parent.getReplies().remove(mail);

		for (Mail child: mail.getReplies())
		{
			child.setParent(parent);
			if (parent != null)
				parent.getReplies().add(child);
		}

		mail.getReplies().clear();

		this.em.remove(mail);

		// TODO:  figure out how to remove it from the search index

		return mail.getList().getId();
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#post(java.lang.String, java.lang.Long, java.lang.String, java.lang.String)
	 */
	@RolesAllowed(Person.ROLE_USER)
	public void post(String fromAddress, Long listId, String subject, String body) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();
		if (me == null)
			throw new IllegalStateException("Must be logged in");

		MailingList toList = this.getListFor(listId, Permission.POST, me);

		if (me.getEmailAddress(fromAddress) == null)
			throw new IllegalArgumentException("Not one of your addresses");

		try
		{
			// Craft a new message
			SubEthaMessage sm = this.craftMessage(toList, fromAddress, me.getName(), subject, body, false);

			ByteArrayOutputStream tmpStream = new ByteArrayOutputStream(8192);
			sm.writeTo(tmpStream);

			this.injector.inject(fromAddress, toList.getEmail(), tmpStream.toByteArray());
		}
		catch (MessagingException ex) { throw new RuntimeException(ex); }
		catch (IOException ex) { throw new RuntimeException(ex); }
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#reply(java.lang.String, java.lang.Long, java.lang.String, java.lang.String)
	 */
	@RolesAllowed(Person.ROLE_USER)
	public Long reply(String fromAddress, Long msgId, String subject, String body) throws NotFoundException, PermissionException
	{
		Person me = this.getMe();
		if (me == null)
			throw new IllegalStateException("Must be logged in");

		Mail mail = this.getMailFor(msgId, Permission.POST, me);
		MailingList toList = mail.getList();

		if (me.getEmailAddress(fromAddress) == null)
			throw new IllegalArgumentException("Not one of your addresses");

		try
		{
			// Craft a new message
			SubEthaMessage sm = this.craftMessage(toList, fromAddress, me.getName(), subject, body, true);

			String inReplyTo = mail.getMessageId();
			if (inReplyTo != null && inReplyTo.length() > 0)
				sm.setHeader("In-Reply-To", inReplyTo);

			ByteArrayOutputStream tmpStream = new ByteArrayOutputStream(8192);
			sm.writeTo(tmpStream);

			this.injector.inject(fromAddress, toList.getEmail(), tmpStream.toByteArray());
		}
		catch (MessagingException ex) { throw new RuntimeException(ex); }
		catch (IOException ex) { throw new RuntimeException(ex); }

		return toList.getId();
	}

	/** */
	protected SubEthaMessage craftMessage(MailingList toList, String fromAddress, String fromName, String subject, String body, boolean reply)
		throws MessagingException, IOException
	{
		Session session = Session.getDefaultInstance(new Properties());

		SubEthaMessage sm = new SubEthaMessage(session);
		sm.setFrom(new InternetAddress(fromAddress, fromName));
		sm.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toList.getEmail()));;
		sm.setSubject(subject);
		sm.setContent(body, "text/plain");
		return sm;
	}


	/* (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#exportMessages(java.lang.Long[], org.subethamail.core.lists.i.ExportFormat, java.io.OutputStream)
	 */
	public void exportMessages(Long[] msgIds, ExportFormat format, OutputStream outStream) throws NotFoundException, PermissionException, ExportMessagesException
	{
		if(ExportFormat.XML.equals(format))
			throw new ExportMessagesException("The XML format is not supported, for now.");

		ZipOutputStream zipOutputStream = null;

		if(ExportFormat.RFC2822DIRECTORY.equals(format))
		{
			CheckedOutputStream checksum = new CheckedOutputStream(outStream, new Adler32());
			zipOutputStream = new ZipOutputStream(new BufferedOutputStream(checksum));
		}

		try
		{
			for (Long msgId : msgIds)
			{
				Mail mail = this.getMailFor(msgId, Permission.VIEW_ARCHIVES);

				switch (format)
				{
					case RFC2822DIRECTORY:
						ZipEntry entry = new ZipEntry(msgId.toString() + ".eml");
						entry.setComment("Message from " + mail.getFrom() + " for list " + mail.getList().getEmail());
						entry.setTime(mail.getArrivalDate().getTime());
						zipOutputStream.putNextEntry(entry);
						this.writeMessage(mail, zipOutputStream);
						zipOutputStream.closeEntry();
						break;
					case MBOX:
						outStream.write(("FROM_ " + mail.getFrom()).getBytes());
						outStream.write("\r\n".getBytes());
						this.writeMessage(mail, outStream);
						outStream.write("\r\n\r\n".getBytes());
						break;

					default:
						throw new ExportMessagesException("Unsupported Format!" + format.toString());
						// break;
				}
			}
			switch (format)
			{
			case RFC2822DIRECTORY:
				zipOutputStream.close();
				break;
			}

		}
		catch (Exception e)
		{
		    log.log(Level.SEVERE,"Error Exporting! ",e);
			throw new ExportMessagesException("Error:" + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.subethamail.core.lists.i.Archiver#exportList(java.lang.Long, org.subethamail.core.lists.i.ExportFormat, java.io.OutputStream)
	 */
	public void exportList(Long listId, ExportFormat format, OutputStream outStream) throws NotFoundException, PermissionException, ExportMessagesException
	{
		List<Mail> mails = this.em.findMailByList(listId, 0, Integer.MAX_VALUE);

		Stack<Long> mailIds = new Stack<Long>();

		for (Mail mail : mails)
		{
			mailIds.add(mail.getId());
		}

		this.exportMessages(mailIds.toArray(new Long[] {}), format, outStream);
	}
}
