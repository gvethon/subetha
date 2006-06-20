/*
 * $Id$
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/util/Geometry.java,v $
 */

package org.subethamail.core.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * Simple wrapper of all EntityManager methods. 
 * 
 * @author Jeff Schnitzer
 */
public class EntityManagerWrapper implements EntityManager
{
	/** */
	EntityManager base;
	
	/**
	 * Wraps the base entity manager
	 */
	public EntityManagerWrapper(EntityManager base)
	{
		this.base = base;
	}
	
	public void persist(Object arg0)
	{
		this.base.persist(arg0);
	}

	public <T> T merge(T arg0)
	{
		return this.base.merge(arg0);
	}

	public void remove(Object arg0)
	{
		this.base.remove(arg0);
	}

	public <T> T find(Class<T> arg0, Object arg1)
	{
		return this.base.find(arg0, arg1);
	}

	public <T> T getReference(Class<T> arg0, Object arg1)
	{
		return this.base.getReference(arg0, arg1);
	}

	public void flush()
	{
		this.base.flush();
	}

	public void setFlushMode(FlushModeType arg0)
	{
		this.base.setFlushMode(arg0);
	}

	public FlushModeType getFlushMode()
	{
		return this.base.getFlushMode();
	}

	public void lock(Object arg0, LockModeType arg1)
	{
		this.base.lock(arg0, arg1);
	}

	public void refresh(Object arg0)
	{
		this.base.refresh(arg0);
	}

	public void clear()
	{
		this.base.clear();
	}

	public boolean contains(Object arg0)
	{
		return this.base.contains(arg0);
	}

	public Query createQuery(String arg0)
	{
		return this.base.createNamedQuery(arg0);
	}

	public Query createNamedQuery(String arg0)
	{
		return this.base.createNamedQuery(arg0);
	}

	public Query createNativeQuery(String arg0)
	{
		return this.base.createNativeQuery(arg0);
	}

	public Query createNativeQuery(String arg0, Class arg1)
	{
		return this.base.createNativeQuery(arg0, arg1);
	}

	public Query createNativeQuery(String arg0, String arg1)
	{
		return this.createNativeQuery(arg0, arg1);
	}

	public void joinTransaction()
	{
		this.base.joinTransaction();
	}

	public Object getDelegate()
	{
		return this.base.getDelegate();
	}

	public void close()
	{
		this.base.clear();
	}

	public boolean isOpen()
	{
		return this.base.isOpen();
	}

	public EntityTransaction getTransaction()
	{
		return this.base.getTransaction();
	}
}
