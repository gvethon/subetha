/*
 * $Id: FilterContext.java 465 2006-05-22 01:30:41Z lhoriman $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/plugin/i/FilterContext.java $
 */

package org.subethamail.core.plugin.i;

import java.util.Map;

import org.subethamail.core.lists.i.ListData;


/**
 * Context for filter execution, providing information from the container
 * such as what list is being process and what the filter arguments are.
 * 
 * @author Jeff Schnitzer
 */
public interface FilterContext
{
	/**
	 * Get the data about a mailing list.
	 * @return the data about a mailing list
	 */
	public ListData getList();	

	/**
	 * This method will use Velocity to process data using the passed in objects
	 * for the context. By default, two objects (SubEthaMessage and ListData) 
	 * are made available as $mail and $list. If you try to pass in a context
	 * with those names, they will be ignored.
	 *
	 * @return the expanded string.
	 */
	public String expand(String data);

	/**
	 * This method will use Velocity to process data using the passed in objects
	 * for the context. By default, two objects (SubEthaMessage and ListData) 
	 * are made available as $mail and $list. If you try to pass in a context
	 * with those names, they will be ignored.
	 *
	 * @return the expanded string.
	 */
	public String expand(String data, Map<String, Object> context);
	
	/**
	 * @return the correctly-typed value of the named parameter. 
	 */
	public Object getArgument(String name);
}