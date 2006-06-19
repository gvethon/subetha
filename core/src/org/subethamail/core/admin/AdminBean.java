/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.admin;

import java.net.URL;
import java.util.List;
import java.util.Random;

import javax.annotation.EJB;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.common.NotFoundException;
import org.subethamail.common.Permission;
import org.subethamail.core.acct.i.AuthSubscribeResult;
import org.subethamail.core.acct.i.PersonData;
import org.subethamail.core.acct.i.SubscribeResult;
import org.subethamail.core.admin.i.Admin;
import org.subethamail.core.admin.i.AdminRemote;
import org.subethamail.core.admin.i.DuplicateListDataException;
import org.subethamail.core.admin.i.InvalidListDataException;
import org.subethamail.core.admin.i.SiteStatus;
import org.subethamail.core.lists.i.ListData;
import org.subethamail.core.post.PostOffice;
import org.subethamail.core.queue.i.Queuer;
import org.subethamail.core.util.OwnerAddress;
import org.subethamail.core.util.Transmute;
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
 * Implementation of the Admin interface.
 * 
 * @author Jeff Schnitzer
 */
@Stateless(name="Admin")
@SecurityDomain("subetha")
@RolesAllowed("siteAdmin")
public class AdminBean implements Admin, AdminRemote
{
	/** */
	private static Log log = LogFactory.getLog(AdminBean.class);
	
	/**
	 * The set of characters from which randomly generated
	 * passwords will be obtained.
	 */
	protected static final String PASSWORD_GEN_CHARS =
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789";
	
	/**
	 * The length of randomly generated passwords.
	 */
	protected static final int PASSWORD_GEN_LENGTH = 6;
	
	/** */
	@EJB DAO dao;
	@EJB PostOffice postOffice;
	@EJB Queuer queuer;

	/**
	 * For generating random passwords.
	 */
	protected Random randomizer = new Random();
	
	/**
	 * @see Admin#log(String)
	 */
	public void log(String msg)
	{
		if (log.isInfoEnabled())
			log.info("CLIENT:  " + msg);
	}
	
	/**
	 * @see Admin#createMailingList(InternetAddress, URL, String, InternetAddress[])
	 */
	public Long createMailingList(InternetAddress address, URL url, String description, InternetAddress[] initialOwners) throws DuplicateListDataException, InvalidListDataException
	{
		this.checkListAddresses(address, url);
		
		// Then create the mailing list and attach the owners.
		MailingList list = new MailingList(address.getAddress(), address.getPersonal(), url.toString(), description);
		this.dao.persist(list);
		// TODO:  remove this code when http://opensource.atlassian.com/projects/hibernate/browse/HHH-1654
		// is fixed.  This should be performed within the constructor of MailingList.
		list.setDefaultRole(list.getRoles().iterator().next());
		list.setAnonymousRole(list.getRoles().iterator().next());
		
		for (InternetAddress ownerAddress: initialOwners)
		{
			EmailAddress ea = this.establishEmailAddress(ownerAddress, null);
			Subscription sub = new Subscription(ea.getPerson(), list, ea, list.getOwnerRole());
			
			this.dao.persist(sub);
			
			list.getSubscriptions().add(sub);
			ea.getPerson().addSubscription(sub);
			
			this.postOffice.sendOwnerNewMailingList(list, ea);
		}
		
		return list.getId();
	}

	/**
	 * @see Admin#establishPerson(InternetAddress, String)
	 */
	public Long establishPerson(InternetAddress address, String password)
	{
		return this.establishEmailAddress(address, password).getPerson().getId();
	}

	/**
	 * @see AdminInternal#establishEmailAddress(InternetAddress, String)
	 */
	public EmailAddress establishEmailAddress(InternetAddress address, String password)
	{
		try
		{
			return this.dao.findEmailAddress(address.getAddress());
		}
		catch (NotFoundException ex)
		{
			// Nobody with that name, lets create
			
			if (password == null)
				password = this.generateRandomPassword();
			
			String personal = address.getPersonal();
			if (personal == null)
				personal = "";
			
			Person p = new Person(password, personal);
			EmailAddress e = new EmailAddress(p, address.getAddress());
			p.addEmailAddress(e);
			
			this.dao.persist(p);
			this.dao.persist(e);
			
			return e;
		}
	}

	/**
	 * @see Admin#subscribe(Long, InternetAddress, boolean, boolean)
	 */
	public AuthSubscribeResult subscribe(Long listId, InternetAddress address, boolean ignoreHold, boolean silent) throws NotFoundException
	{
		EmailAddress addy = this.establishEmailAddress(address, null);
		
		SubscribeResult result = this.subscribe(listId, addy.getPerson(), addy, ignoreHold, silent);
		
		return new AuthSubscribeResult(addy.getId(), addy.getPerson().getPassword(), result, listId);
	}
	
	/**
	 * @see Admin#subscribe(Long, Long, String, boolean)
	 */
	public SubscribeResult subscribe(Long listId, Long personId, String email, boolean ignoreHold) throws NotFoundException
	{
		Person who = this.dao.findPerson(personId);
		
		if (email == null)
		{
			// Subscribing with (or changing to) disabled delivery
			return this.subscribe(listId, who, null, ignoreHold, false);
		}
		else
		{
			EmailAddress addy = who.getEmailAddress(email);
			
			if (addy == null)
				throw new IllegalStateException("Must be one of person's email addresses");
			
			return this.subscribe(listId, who, addy, ignoreHold, false);
		}
	}
	
	/**
	 * Subscribes someone to a mailing list, or changes the delivery address
	 * of an existing subscriber.
	 * @param deliverTo can be null to disable delivery
	 * @param ignoreHold will subscribe even if a hold is requested
	 * @param silent if true will not send a welcome message to new subscribers
	 */
	protected SubscribeResult subscribe(Long listId, Person who, EmailAddress deliverTo, boolean ignoreHold, boolean silent) throws NotFoundException
	{
		MailingList list = this.dao.findMailingList(listId);
		
		Subscription sub = who.getSubscription(listId);
		if (sub != null)
		{
			// If we're already subscribed, maybe we want to change the
			// delivery address.
			sub.setDeliverTo(deliverTo);
			
			return SubscribeResult.OK;
		}
		else
		{
			if (!ignoreHold && list.isSubscriptionHeld())
			{
				// Maybe already held, if so, replace it; email address might be new
				SubscriptionHold hold = who.getHeldSubscriptions().remove(list.getId());
				if (hold != null)
					this.dao.remove(hold);
				
				hold = new SubscriptionHold(who, list, deliverTo);
				this.dao.persist(hold);
				
				// Send mail to anyone that can approve
				for (Subscription maybeModerator: list.getSubscriptions())
					if (maybeModerator.getRole().getPermissions().contains(Permission.APPROVE_SUBSCRIPTIONS)
							&& maybeModerator.getDeliverTo() != null)
						this.postOffice.sendModeratorSubscriptionHeldNotice(maybeModerator.getDeliverTo(), hold);
				
				return SubscribeResult.HELD;
			}
			else
			{
				sub = new Subscription(who, list, deliverTo, list.getDefaultRole());
				
				this.dao.persist(sub);
				
				who.addSubscription(sub);
				list.getSubscriptions().add(sub);
				
				if (!silent)
					this.postOffice.sendSubscribed(list, who, deliverTo);
			
				// Flush any messages that might be held prior to this subscription.
				this.selfModerate(who.getId());
				
				return SubscribeResult.OK;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#unsubscribe(java.lang.Long, Long)
	 */
	public void unsubscribe(Long listId, Long personId) throws NotFoundException
	{
		Person who = this.dao.findPerson(personId);
		this.unsubscribe(listId, who);
	}

	protected void unsubscribe(Long listId, Person who) throws NotFoundException
	{
		MailingList list = this.dao.findMailingList(listId);
		Subscription sub = who.getSubscriptions().remove(listId);
		list.getSubscriptions().remove(sub);
		this.dao.remove(sub);
	}

	/**
	 * @return a valid password.
	 */
	protected String generateRandomPassword()
	{
		StringBuffer gen = new StringBuffer(PASSWORD_GEN_LENGTH);
		
		for (int i=0; i<PASSWORD_GEN_LENGTH; i++)
		{
			int which = (int)(PASSWORD_GEN_CHARS.length() * randomizer.nextDouble());
			
			gen.append(PASSWORD_GEN_CHARS.charAt(which));
		}
		
		return gen.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#setSiteAdmin(java.lang.Long, boolean)
	 */
	public void setSiteAdmin(Long personId, boolean value) throws NotFoundException
	{
		Person p = this.dao.findPerson(personId);
		p.setSiteAdmin(value);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#setSiteAdmin(java.lang.String, boolean)
	 */
	public void setSiteAdmin(String email, boolean siteAdmin) throws NotFoundException
	{
		EmailAddress ea = this.dao.findEmailAddress(email);
		ea.getPerson().setSiteAdmin(siteAdmin);
	}

	/**
	 * @see Admin#addEmail(Long, String)
	 */
	public void addEmail(Long personId, String email) throws NotFoundException
	{
		EmailAddress addy = this.dao.getEmailAddress(email);
		
		// Three cases:  either addy is null, addy is already associated with
		// the person, or addy is already associated with someone else.
		
		// Lets quickly handle the case were we don't have to do anything
		if (addy != null && addy.getPerson().getId().equals(personId))
			return;
		
		Person who = this.dao.findPerson(personId);
			
		if (addy == null)
		{
			addy = new EmailAddress(who, email);
			this.dao.persist(addy);
			who.addEmailAddress(addy);
		}
		else
		{
			this.merge(addy.getPerson().getId(), who.getId());
		}
		
		this.selfModerate(who.getId());
	}

	/**
	 * @see Admin#merge(Long, Long)
	 */
	public void merge(Long fromPersonId, Long toPersonId) throws NotFoundException
	{
		Person from = this.dao.findPerson(fromPersonId);
		Person to = this.dao.findPerson(toPersonId);

		if (log.isDebugEnabled())
			log.debug("Merging " + from + " into " + to);

		// First of all watch out for permission upgrade
		if (from.isSiteAdmin())
			to.setSiteAdmin(true);
		
		// Move email addresses
		for (EmailAddress addy: from.getEmailAddresses().values())
		{
			if (log.isDebugEnabled())
				log.debug(" merging " + addy);
			
			addy.setPerson(to);
			to.addEmailAddress(addy);
		}
		
		from.getEmailAddresses().clear();
		
		// Move subscriptions
		for (Subscription sub: from.getSubscriptions().values())
		{
			// Keep our current subscription if there is a duplicate
			Subscription toSub = to.getSubscriptions().get(sub.getList().getId());
			if (toSub != null)
			{
				if (log.isDebugEnabled())
					log.debug(" abandoning duplicate " + sub);
				
				// Special case - if the other was an owner role, upgrade this one too
				if (sub.getRole().isOwner())
					toSub.setRole(sub.getRole());
				
				this.dao.remove(sub);
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug(" merging " + sub);
				
				sub.setPerson(to);
				to.addSubscription(sub);
			}
		}
		
		from.getSubscriptions().clear();
		
		// Move held subscriptions
		for (SubscriptionHold hold: from.getHeldSubscriptions().values())
		{
			Long listId = hold.getList().getId();
			if (to.getSubscriptions().containsKey(listId) || to.getHeldSubscriptions().containsKey(listId))
			{
				if (log.isDebugEnabled())
					log.debug(" abandoning obsolete or duplicate " + hold);
				
				this.dao.remove(hold);
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug(" merging " + hold);
				
				hold.setPerson(to);
				to.addHeldSubscription(hold);
			}
		}
		
		from.getHeldSubscriptions().clear();
		
		// Some of those holds we might not need anymore because we were already
		// subscribed or acquired a new subscription.
		for (SubscriptionHold hold: to.getHeldSubscriptions().values())
		{
			Long listId = hold.getList().getId();
			if (to.getSubscriptions().containsKey(listId))
			{
				to.getHeldSubscriptions().remove(listId);
				this.dao.remove(hold);
			}
		}
		
		// Nuke the old person object
		if (log.isDebugEnabled())
			log.debug(" deleting person " + from);
		
		this.dao.remove(from);
	}

	/**
	 * @see Admin#selfModerate(Long)
	 */
	public int selfModerate(Long personId) throws NotFoundException
	{
		Person who = this.dao.findPerson(personId);
		
		List<Mail> heldMail = this.dao.findSoftHoldsForPerson(personId);
		
		int count = 0;
		
		for (Mail held: heldMail)
		{
			if (held.getList().getPermissionsFor(who).contains(Permission.POST))
			{
				held.approve();
				this.queuer.queueForDelivery(held.getId());
				count++;
			}
		}
		
		return count;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#getSiteAdmins()
	 */
	public List<PersonData> getSiteAdmins()
	{
		List<Person> siteAdmins = this.dao.findSiteAdmins();
		return Transmute.people(siteAdmins);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#setListAddresses(java.lang.Long, javax.mail.internet.InternetAddress, java.net.URL)
	 */
	public void setListAddresses(Long listId, InternetAddress address, URL url) throws NotFoundException, DuplicateListDataException, InvalidListDataException
	{
		MailingList list = this.dao.findMailingList(listId);
		
		InternetAddress checkAddress = list.getEmail().equals(address.getAddress()) ? null : address;
		URL checkUrl = list.getUrl().equals(url.toString()) ? null : url;
		this.checkListAddresses(checkAddress, checkUrl);
		
		list.setEmail(address.getAddress());
		list.setUrl(url.toString());
	}

	/**
	 * Checks whether or not the list addresses are ok (valid and not duplicates)
	 * 
	 * @param address can be null to skip address checking
	 * @param url can be null to skip url checking
	 */
	protected void checkListAddresses(InternetAddress address, URL url) throws DuplicateListDataException, InvalidListDataException
	{
		boolean dupAddress = false;
		boolean dupUrl = false;
		
		if (address != null)
		{
			boolean ownerAddy = OwnerAddress.getList(address.getAddress()) != null;
			boolean verpAddy = VERPAddress.getVERPBounce(address.getAddress()) != null;
			
			if (ownerAddy || verpAddy)
				throw new InvalidListDataException("Address cannot be used", ownerAddy, verpAddy);
			
			try
			{
				this.dao.findMailingList(address);
				dupAddress = true;
			}
			catch (NotFoundException ex) {}
		}
		
		if (url != null)
		{
			// TODO:  consider whether or not we should enforce any formatting of
			// the url here.  Seems like that's a job for the web front end?
			
			try
			{
				this.dao.findMailingList(url);
				dupUrl = true;
			}
			catch (NotFoundException ex) {}
		}
		
		if (dupAddress || dupUrl)
			throw new DuplicateListDataException("Mailing list already exists", dupAddress, dupUrl);	
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#getLists(int, int)
	 */
	public List<ListData> getLists(int skip, int count)
	{
		return Transmute.mailingLists(this.dao.findMailingLists(skip, count));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#searchLists(java.lang.String, int, int)
	 */
	public List<ListData> searchLists(String query, int skip, int count)
	{
		return Transmute.mailingLists(this.dao.findMailingLists(query, skip, count));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#countLists()
	 */
	public int countLists()
	{
		return this.dao.countLists();
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#countLists(java.lang.String)
	 */
	public int countLists(String query)
	{
		return this.dao.countLists(query);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#getSiteStatus()
	 */
	public SiteStatus getSiteStatus()
	{
		return new SiteStatus(
				System.getProperty("file.encoding"),
				this.countLists(),
				this.dao.countPerson(),
				this.dao.countMail(),
				(URL)this.dao.getConfigValue(Config.ID_SITE_URL),
				(InternetAddress)this.dao.getConfigValue(Config.ID_SITE_POSTMASTER)				
			);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#setDefaultSiteUrl(java.net.URL)
	 */
	public void setDefaultSiteUrl(URL url)
	{
		this.dao.setConfigValue(Config.ID_SITE_URL, url);
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.admin.i.Admin#setPostmaster(javax.mail.internet.InternetAddress)
	 */
	public void setPostmaster(InternetAddress address)
	{
		this.dao.setConfigValue(Config.ID_SITE_POSTMASTER, address);
	}
}
