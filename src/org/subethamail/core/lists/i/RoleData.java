/*
 * $Id: RoleData.java 963 2007-07-04 01:05:05Z jon $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/lists/i/RoleData.java $
 */

package org.subethamail.core.lists.i;

import java.io.Serializable;
import java.util.Set;

import org.subethamail.entity.i.Permission;

/**
 * Information about a role.
 *
 * @author Jeff Schnitzer
 */
public class RoleData implements Serializable
{
	private static final long serialVersionUID = 1L;

	Long id;
	String name;
	boolean owner;
	Set<Permission> permissions;
	Long listId;
	
	protected RoleData()
	{
		// http://forums.java.net/jive/thread.jspa?threadID=26539&tstart=0
	}

	/**
	 */
	public RoleData(Long id, String name, boolean owner, Set<Permission> permissions, Long listId)
	{
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.permissions = permissions;
		this.listId = listId;
	}

	/** */
	public Long getId()
	{
		return this.id;
	}

	/** */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Is this the special owner role for the list? 
	 */
	public boolean isOwner()
	{
		return this.owner;
	}

	/** */
	public Set<Permission> getPermissions()
	{
		return this.permissions;
	}

	/** */
	public Long getListId()
	{
		return this.listId;
	}
	
}
