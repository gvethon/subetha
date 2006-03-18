/*
 * $Id: DAO.java 90 2006-02-23 02:31:05Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/entity/src/com/blorn/entity/dao/DAO.java $
 */

package org.subethamail.entity.dao;

import javax.persistence.LockModeType;

/**
 * DAO interface to all persisted objects.  Use this EJB instead
 * of directly manipulating the EntityManager from other EJBs.
 * It's just a convenient layer of abstraction, usable from
 * multiple applications that share a data model.
 *
 * @author Jeff Schnitzer
 */
public interface DAO
{
	/** */
	public static final String JNDI_NAME = "kink/DAOEJB/local";
	
	/**
	 * Persists the object in the database
	 */
	public void persist(Object obj);
	
	/**
	 * Removes the object from the database
	 */
	public void remove(Object obj);
	
	/**
	 * Flush the current state of the session cache
	 */
	public void flush();
	
	/**
	 * Lock an entity.
	 */
	public void lock(Object obj, LockModeType lockMode);

}
