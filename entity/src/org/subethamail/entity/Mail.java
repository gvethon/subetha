/*
 * $Id: Person.java 125 2006-03-07 13:27:43Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/entity/src/com/blorn/entity/Person.java $
 */

package org.subethamail.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import javax.ejb.EJBException;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.subethamail.common.MailUtils;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.common.valid.Validator;

/**
 * Entity for a single piece of mail.
 * 
 * @author Jeff Schnitzer
 */
@NamedQueries({
	@NamedQuery(
		name="MailByMessageId", 
		query="from Mail m where m.messageId = :messageId",
		hints={
			@QueryHint(name="org.hibernate.readOnly", value="true"),
			@QueryHint(name="org.hibernate.cacheable", value="true")
		}
	),
	@NamedQuery(
		name="RepliesToMessageId", 
		query="from Mail m where m.parent is null and m.parentMessageId = :messageId",
		hints={
		}
	)
})
@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
@SuppressWarnings("serial")
public class Mail implements Serializable, Comparable
{
	/** */
	@Transient private static Log log = LogFactory.getLog(Mail.class);
	
	/**
	 * Possible moderation states 
	 */
	public enum HoldType
	{
		SELF,		// Hold for self-approval
		MODERATOR	// Hold for list moderator to approve
	}
	
	/** */
	@Id
	@GeneratedValue
	Long id;
	
	/** */
	@Column(nullable=false, length=Validator.MAX_MAIL_CONTENT)
	byte[] content;
	
	/** Message id might not exist */
	@Column(nullable=true, length=Validator.MAX_MAIL_MESSAGE_ID)
	@Index(name="mailMessageIdIndex")
	String messageId;
	
	/** Parent message id might not exist */
	@Column(nullable=true, length=Validator.MAX_MAIL_MESSAGE_ID)
	@Index(name="mailParentMessageIdIndex")
	String parentMessageId;
	
	/** */
	@Column(nullable=false, length=Validator.MAX_MAIL_SUBJECT)
	String subject;
	
	/** rfc222-style comma-separated address list */
	@Column(name="fromField", nullable=true, length=Validator.MAX_MAIL_FROM)
	String from;
	
	/** normalized box@domain.tld */
	@Column(nullable=true, length=Validator.MAX_MAIL_FROM)
	@Index(name="mailFromNormalIndex")
	String fromNormal;
	
	/** Date the entity was created, not from header fields */
	@Column(nullable=false)
	Date dateCreated;
	
	/** */
	@ManyToOne
	@JoinColumn(name="listId", nullable=false)
	MailingList list;
	
	/** */
	@ManyToOne
	@JoinColumn(name="parentId", nullable=true)
	Mail parent;
	
	/** */
	@OneToMany(cascade=CascadeType.ALL, mappedBy="parent")
	@Sort(type=SortType.NATURAL)
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	SortedSet<Mail> replies;
	
	/** 
	 * Is it held for moderation?  If null, no need for moderation.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable=true)
	@Index(name="holdIndex")
	HoldType hold;
	
	/**
	 */
	public Mail() {}
	
	/**
	 * @param holdFor can be null which means none required
	 */
	public Mail(SubEthaMessage msg, MailingList list, Mail parent, String parentMessageId, HoldType holdFor) throws MessagingException
	{
		if (log.isDebugEnabled())
			log.debug("Creating new mail");
		
		this.dateCreated = new Date();
		this.list = list;
		this.parent = parent;
		this.parentMessageId = parentMessageId;
		this.hold = holdFor;
		
		byte[] raw;
		try
		{
			ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
			msg.writeTo(tmpStream);
			raw = tmpStream.toByteArray();
		}
		catch (IOException ex) { throw new EJBException(ex); }
		
		// These are validated normally.
		this.setContent(raw);
		this.setSubject(msg.getSubject());
		this.setMessageId(msg.getMessageID());
		this.setFrom(MailUtils.getFrom(msg));

		// We also track the first from entry so we can do lookups
		Address[] froms = msg.getFrom();
		if (froms != null && froms.length > 0)
			this.setFromNormal(((InternetAddress)froms[0]).getAddress());
	}
	
	/** */
	public Long getId()		{ return this.id; }

	/**
	 */
	public byte[] getContent() { return this.content; }
	
	/**
	 */
	public void setContent(byte[] value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting content of " + this);
		
		this.content = value;
	}

	/**
	 */
	public String getMessageId() { return this.messageId; }
	
	/**
	 */
	public void setMessageId(String value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting message id of " + this + " to " + value);
		
		this.messageId = value;
	}
	
	/** */
	public String getParentMessageId() { return this.parentMessageId; }

	/**
	 */
	public String getSubject() { return this.subject; }
	
	/**
	 */
	public void setSubject(String value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting subject of " + this + " to " + value);
		
		this.subject = value;
	}

	/**
	 * @return an rfc222-compilant address list, comma separated.  It will
	 * be parseable with InternetAddress.parse().
	 */
	public String getFrom() { return this.from; }
	
	/**
	 * @param value can be null.
	 */
	public void setFrom(String value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting from of " + this + " to " + value);
		
		this.from = value;
	}
	
	/**
	 * @return the normalized box@domain.tld for the first From entry.
	 *
	 * @see Validator#normalizeEmail(String)
	 */
	public String getFromNormal() { return this.fromNormal; }
	
	/**
	 * @param value does not need to be normalized already; it
	 *  will be normalized in this method.  Null is ok.
	 */
	public void setFromNormal(String value)
	{
		value = Validator.normalizeEmail(value);

		if (log.isDebugEnabled())
			log.debug("Setting fromNormal of " + this + " to " + value);
		
		this.fromNormal = value;
	}
	
	/** Note no setter; this is the date the mail came into the system */
	public Date getDateCreated() { return this.dateCreated; }
	
	/**
	 */
	public HoldType getHold() { return this.hold; }
	
	/**
	 * Releases a moderation hold
	 */
	public void release()
	{
		this.hold = null;
	}
	
	/** */
	public Mail getParent() { return this.parent; }
	
	public void setParent(Mail value)
	{
		this.parent = value;
	}
	
	/** */
	public Set<Mail> getReplies() { return this.replies; }

	/** */
	public String toString()
	{
		return this.getClass() + " {id=" + this.id + ", subject=" + this.subject + "}";
	}
	
	/** */
	public MailingList getList() { return this.list; }

	/**
	 * Natural sort order is based on creation date
	 */
	public int compareTo(Object arg0)
	{
		Mail other = (Mail)arg0;

		return this.dateCreated.compareTo(other.getDateCreated());
	}
}

