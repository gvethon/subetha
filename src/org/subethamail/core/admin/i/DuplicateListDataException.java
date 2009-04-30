/*
 * $Id: DuplicateListDataException.java 778 2006-10-01 19:41:43Z lhoriman $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/ejb/NameAlreadyTakenException.java,v $
 */

package org.subethamail.core.admin.i;

import javax.ejb.ApplicationException;

/**
 * Thrown when a mailing list could not be created.
 */
@SuppressWarnings("serial")
@ApplicationException(rollback=true)
public class DuplicateListDataException extends Exception
{
	/** */
	boolean addressTaken;
	boolean urlTaken;
	
	/** */
	public DuplicateListDataException() {}
	
	/**
	 */
	public DuplicateListDataException(String msg, boolean duplicateAddress, boolean duplicateUrl)
	{
		super(msg);
		
		this.addressTaken = duplicateAddress;
		this.urlTaken = duplicateUrl;
	}
	
	/**
	 */
	public DuplicateListDataException(Throwable cause, boolean duplicateAddress, boolean duplicateUrl)
	{
		super(cause);
		
		this.addressTaken = duplicateAddress;
		this.urlTaken = duplicateUrl;
	}

	/**
	 * @return true if the address is already occupied by another list. 
	 */
	public boolean isAddressTaken()
	{
		return this.addressTaken;
	}

	/**
	 * @return true if the url is already occupied by another list.
	 */
	public boolean isUrlTaken()
	{
		return this.urlTaken;
	}

}

