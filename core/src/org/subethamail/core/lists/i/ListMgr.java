/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.lists.i;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.mail.internet.InternetAddress;

import org.subethamail.common.NotFoundException;
import org.subethamail.common.Permission;

/**
 * Tools for querying and modifying list configurations.  Most methods
 * require that the caller principal have certain permissions on the list,
 * either defined by their subscription role or the anonymous role (if
 * there is no caller principal).
 *
 * @author Jeff Schnitzer
 */
@Local
public interface ListMgr
{
	/** */
	public static final String JNDI_NAME = "subetha/ListMgr/local";

	/**
	 * Finds the id for a particular list URL.
	 * 
	 * No access control.
	 */
	public Long lookup(URL url) throws NotFoundException;
	
	/**
	 * Retrieves all the subscribers for a MailingList
	 * Requires Permission.VIEW_SUBSCRIBERS.
	 * 
	 * @throws NotFoundException if the list id is not valid.
	 */
	public List<SubscriberData> getSubscribers(Long listId) throws NotFoundException, PermissionException;

	/**
	 * Sets list name, url, email and description and whether or not subscriptions
	 * are held for approval.
	 * Requires Permission.EDIT_SETTINGS
	 */
	public void setList(Long listId, String name, String description, String url, String email, boolean holdSubs) throws NotFoundException, PermissionException;
	
	/**
	 * Gets some basic information about a mailing list. 
	 * No permissions necessary.
	 */
	public ListData getList(Long listId) throws NotFoundException;

	/**
	 * Gets the basic info about a role. 
	 * Requires Permission.EDIT_ROLES
	 */
	public RoleData getRole(Long roleId) throws NotFoundException, PermissionException;

	/**
	 * Gets information about the roles associated with a list.
	 * Requires Permission.EDIT_ROLES
	 */
	public ListRoles getRoles(Long listId) throws NotFoundException, PermissionException;

	/**
	 * Adds a new role to the list.
	 * 
	 * @return the id of the new role
	 * 
	 * Requires Permission.EDIT_ROLES
	 */
	public Long addRole(Long listId, String name, Set<Permission> perms) throws NotFoundException, PermissionException;

	/**
	 * Changes the properties of an existing role.
	 * 
	 * @return the id of the list that owns the role, very useful to have.
	 * 
	 * Requires Permission.EDIT_ROLES
	 */
	public Long setRole(Long roleId, String name, Set<Permission> perms) throws NotFoundException, PermissionException;

	/**
	 * Sets the default role for a list.
	 * Requires Permission.EDIT_ROLES
	 * 
	 * @param roleId must be a role belonging to the list.
	 */
	public void setDefaultRole(Long listId, Long roleId) throws NotFoundException, PermissionException;
	
	/**
	 * Sets the anonymous role for a list.
	 * Requires Permission.EDIT_ROLES
	 * 
	 * @param roleId must be a role belonging to the list.
	 */
	public void setAnonymousRole(Long listId, Long roleId) throws NotFoundException, PermissionException;

	/**
	 * Deletes a role, converting all participants in that role to the
	 * alternate.  The roles must both belong to the same list.
	 * 
	 * You cannot delete the Owner role.
	 * Requires Permission.EDIT_ROLES
	 * 
	 * @return the id of the list which owns the roles.
	 */
	public Long deleteRole(Long deleteRoleId, Long convertToRoleId) throws NotFoundException, PermissionException;
	
	/**
	 * Gets information about all the filters that might or might
	 * not be enabled on a list.
	 * 
	 * Requires Permission.EDIT_FILTERS
	 */
	public Filters getFilters(Long listId) throws NotFoundException, PermissionException;
	
	/**
	 * Gets data for a filter on a list.  If the filter has not already
	 * been enabled, the EnabledFilterData is populated with default values.
	 * 
	 * Requires Permission.EDIT_FILTERS
	 */
	public EnabledFilterData getFilter(Long listId, String className) throws NotFoundException, PermissionException;
	
	/**
	 * Enables a filter on a list, or changes the data associated with that filter.
	 * If a FilterParameter is missing from args, it will be assigned its default
	 * value.  If an unrecognized FilterParameter is found in args, it is silently
	 * ignored.
	 * 
	 * Requires Permission.EDIT_FILTERS
	 */
	public void setFilter(Long listId, String className, Map<String, Object> args) throws NotFoundException, PermissionException;
	
	/**
	 * Disables a filter on a list.  Fails silently if filter is not enabled on the list.
	 * All argument data is deleted.
	 * 
	 * Requires Permission.EDIT_FILTERS
	 */
	public void disableFilter(Long listId, String className) throws NotFoundException, PermissionException;
	
	/**
	 * Subscribes a mass of users to the list
	 * 
	 * @param invite will send invites rather than just subscribing
	 * @param addresses are the addresses to subscribe
	 */
	public void massSubscribe(Long listId, boolean invite, InternetAddress[] addresses) throws NotFoundException, PermissionException;

	/**
	 * @return all the held subscriptions on the list.  Note the roleName is always null.
	 * 
	 * Requires Permission.APPROVE_SUBSCRIPTIONS
	 */
	public List<SubscriberData> getHeldSubscriptions(Long listId) throws NotFoundException, PermissionException;

	/**
	 * Approves a subscription hold.  User is notified.
	 * 
	 * Requires Permission.APPROVE_SUBSCRIPTIONS
	 */
	public void approveHeldSubscription(Long listId, Long personId) throws NotFoundException, PermissionException;

	/**
	 * Discards a subscription hold.  User is not notified.
	 * 
	 * Requires Permission.APPROVE_SUBSCRIPTIONS
	 */
	public void discardHeldSubscription(Long listId, Long personId) throws NotFoundException, PermissionException;

	/**
	 * Gets all the held messages for a mailing list.
	 * 
	 * Requires Permission.APPROVE_MESSAGES
	 */
	public Collection<MailHold> getHeldMessages(Long listId) throws NotFoundException, PermissionException;
	
	/**
	 * UnSubscribes a person from a list.
	 * 
	 * @param listId the mailing list id
	 * @param personId the person id
	 *  
	 * @throws NotFoundException if the list id or email is not valid.
	 * @throws PermissionException needs Permission.UNSUBSCRIBE_OTHERS
	 */
	public void unsubscribe(Long listId, Long personId) throws NotFoundException, PermissionException;

	
	/**
	 * Sets the role for a person for a list.
	 * 
	 * 
	 * @param listId the mailist list id
	 * @param personId the person id	
	 * @param roleId the role that person fulfills
	 * 
	 * @throws NotFoundException If the list, person or role is not found.
	 * @throws PermissionException Requires Permission.EDIT_ROLES
	 */
	public void setSubscriberRole(Long listId, Long personId, Long roleId) throws NotFoundException, PermissionException;

}
