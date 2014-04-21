package org.subethamail.core.lists.i;

import java.io.Serializable;

import org.subethamail.core.plugin.i.FilterParameter;

/**
 * Information about an available filter.  Note that these
 * are not entities, but modules loaded into the application.
 *
 * @author Jeff Schnitzer
 */
public class FilterData implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Class name identifies a filter type */
	String className;
	String name;
	String description;
	FilterParameter[] parameters;

	protected FilterData()
	{
		// http://forums.java.net/jive/thread.jspa?threadID=26539&tstart=0
	}

	/**
	 */
	public FilterData(String className, String name, String description, FilterParameter[] parameters)
	{
		this.className = className;
		this.name = name;
		this.description = description;
		this.parameters = parameters;
	}

	/** */
	public String getClassName()
	{
		return this.className;
	}

	/** */
	public String getDescription()
	{
		return this.description;
	}

	/** */
	public String getName()
	{
		return this.name;
	}

	/** */
	public FilterParameter[] getParameters()
	{
		return this.parameters;
	}
}
