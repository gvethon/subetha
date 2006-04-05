/*
 * $Id: Person.java 125 2006-03-07 13:27:43Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/entity/src/com/blorn/entity/Person.java $
 */

package org.subethamail.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.Length;
import org.subethamail.common.valid.Validator;

/**
 * Entity of a human user of the system.
 * 
 * @author Jeff Schnitzer
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SuppressWarnings("serial")
public class Person implements Serializable, Comparable
{
	/** */
	@Transient private static Log log = LogFactory.getLog(Person.class);
	
	/** */
	@Id
	@GeneratedValue
	Long id;
	
	@Column(name="passwd", nullable=false, length=Validator.MAX_PERSON_PASSWORD)
	@Length(min=3)
	String password;
	
	@Column(nullable=false, length=Validator.MAX_PERSON_NAME)
	String name;
	
	@Column(nullable=false)
	boolean siteAdmin;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="person")
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@MapKey(name="id")
	Map<String, EmailAddress> emailAddresses;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="person")
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@MapKey(name="list")
	Map<Long, Subscription> subscriptions;
	
	/**
	 */
	public Person() {}
	
	/**
	 */
	public Person(String password, String name)
	{
		if (log.isDebugEnabled())
			log.debug("Creating new person");
		
		// These are validated normally.
		this.setPassword(password);
		this.setName(name);
		
		this.emailAddresses = new HashMap<String, EmailAddress>();
		this.subscriptions = new HashMap<Long, Subscription>();
	}
	
	/** */
	public Long getId()		{ return this.id; }
	
	/**
	 * TODO:  consider minimal two-way encryption so that pws are not easily readable in db
	 */
	public String getPassword()
	{
		return this.password;
	}

	/**
	 * Note that the password is stored in cleartext so that
	 * we can email it back to its owner.
	 * 
	 * TODO:  consider minimal two-way encryption (even rot13) so that pws are not easily readable in db dumps
	 * 
	 * @param value is a plaintext password
	 */
	public void setPassword(String value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting password of " + this);
		
		this.password = value;
	}

	/**
	 * Checks to see if the password matches.
	 *
	 * @return true if the password does match.
	 */
	public boolean checkPassword(String plainText)
	{
		return this.password.equals(plainText);
	}
	
	/**
	 */
	public String getName() { return this.name; }
	
	/**
	 */
	public void setName(String value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting name of " + this + " to " + value);
		
		this.name = value;
	}

	/** */
	public boolean isSiteAdmin()
	{
		return this.siteAdmin;
	}

	/** */
	public void setSiteAdmin(boolean value)
	{
		if (log.isDebugEnabled())
			log.debug("Setting admin flag of " + this + " to " + value);
		
		this.siteAdmin = value;
	}
	
	/** */
	public Map<String, EmailAddress> getEmailAddresses() { return this.emailAddresses; }
	
	/** */
	public void addEmailAddress(EmailAddress value)
	{
		this.emailAddresses.put(value.getId(), value);
	}
	
	/** 
	 * @return all the subscriptions associated with this person
	 */
	public Map<Long, Subscription> getSubscriptions() { return this.subscriptions; }
	
	/**
	 * Adds a subscription to our internal map.
	 */
	public void addSubscription(Subscription value)
	{
		this.subscriptions.put(value.getList().getId(), value);
	}
	
	/**
	 * @return true if this person is subscribed to the list
	 */
	public boolean isSubscribed(MailingList list)
	{
		return this.subscriptions.containsKey(list.getId());
	}
	
	/**
	 * @return the subscription, or null if not subscribed to the list
	 */
	public Subscription getSubscription(Long listId)
	{
		return this.subscriptions.get(listId);
	}
	
	/** */
	public Role getRoleIn(MailingList list)
	{
		Subscription sub = this.subscriptions.get(list.getId());
		
		return (sub == null) ? list.getAnonymousRole() : sub.getRole();
	}
	
	/** */
	public String toString()
	{
		return "Person {id=" + this.id + ", emailAddresses=" + this.emailAddresses + "}";
	}

	/**
	 * Natural sort order is based on name
	 */
	public int compareTo(Object arg0)
	{
		Person other = (Person)arg0;

		return this.name.compareTo(other.getName());
	}
}

