/*
 * $Id: PermissionException.java 867 2006-11-09 09:16:46Z lhoriman $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/ejb/NameAlreadyTakenException.java,v $
 */

package org.subethamail.entity.i;

import javax.ejb.ApplicationException;



/**
 * Thrown when a permission was needed but not available.
 */
@ApplicationException(rollback=true)
public class PermissionException extends Exception
{
	private static final long serialVersionUID = 1L;

	Permission needed;
	
	/**
	 */
	public PermissionException(Permission needed)
	{
		super("Requires permission " + needed);
		
		this.needed = needed;
	}

	public PermissionException(Permission needed, String extra)
	{
		super("Requires permission " + needed + ". " + extra);
		
		this.needed = needed;
	}
	
	/**
	 */
	public Permission getNeeded()
	{
		return this.needed;
	}
}