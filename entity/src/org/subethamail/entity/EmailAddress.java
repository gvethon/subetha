/*
 * $Id: Person.java 125 2006-03-07 13:27:43Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/entity/src/com/blorn/entity/Person.java $
 */

package org.subethamail.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.subethamail.common.valid.Validator;

/**
 * Entity of an email address.  People can have many email
 * addresses, any of which can be used to send mail.  Note that
 * all addresses are stored in their normalized form.
 * 
 * @see Validator#normalizeEmail(String)
 * 
 * @author Jeff Schnitzer
 */
@NamedQueries({
	@NamedQuery(
		name="EmailByAddress", 
		query="from EmailAddress ea where ea.address = :address",
		hints={
			@QueryHint(name="org.hibernate.readOnly", value="true"),
			@QueryHint(name="org.hibernate.cacheable", value="true")
		}
	)
})
@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EmailAddress implements Serializable, Comparable
{
	/** */
	@Transient private static Log log = LogFactory.getLog(EmailAddress.class);
	
	/** */
	@Id
	@GeneratedValue
	Long id;
	
	@Column(nullable=false, length=Validator.MAX_EMAIL_ADDRESS)
	String address;
	
	@ManyToOne
	@JoinColumn(name="ownerId", nullable=false)
	Person owner;
	
	/**
	 */
	public EmailAddress() {}
	
	/**
	 */
	public EmailAddress(Person owner, String address)
	{
		if (log.isDebugEnabled())
			log.debug("Creating new EmailAddress");
		
		// These are validated normally.
		this.setOwner(owner);
		this.setAddress(address);
	}
	
	/** */
	public Long getId()		{ return this.id; }

	/** */
	public String getAddress() { return this.address; }
	
	/**
	 * Always normalizes the email address.
	 */
	public void setAddress(String value)
	{
		if (!Validator.validEmail(value))
			throw new IllegalArgumentException("Invalid address");
		
		value = Validator.normalizeEmail(value);

		if (log.isDebugEnabled())
			log.debug("Setting address to " + value);
		
		this.address = value;
	}
	
	/** */
	public Person getOwner() { return this.owner; }
	
	/** */
	public void setOwner(Person value)
	{
		if (value == null)
			throw new IllegalArgumentException("Owner cannot be null");
		
		this.owner = value;
	}

	/** */
	public String toString()
	{
		return this.getClass() + "{id=" + this.id + "}";
	}

	/**
	 * Natural sort order is based on email text
	 */
	public int compareTo(Object arg0)
	{
		EmailAddress other = (EmailAddress)arg0;

		return this.address.compareTo(other.getAddress());
	}
}

