/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.plugin.i.helper;

import javax.annotation.EJB;
import javax.annotation.security.RunAs;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.core.plugin.i.Blueprint;
import org.subethamail.core.plugin.i.BlueprintRegistry;

/**
 * Base implementation of a blueprint that registers itself upon deployment.
 * Extend this class to automatically have your blueprint register itself. 
 * 
 * @author Jeff Schnitzer
 */
@SecurityDomain("subetha")
@RunAs("siteAdmin")
abstract public class AbstractBlueprint implements Blueprint, Lifecycle
{
	/**
	 * These will automatically be injected by JBoss.
	 */
	protected @EJB BlueprintRegistry registry;

	/**
	 * @see Lifecycle#start()
	 */
	public void start() throws Exception
	{
		if (this.registry != null)
			throw new RuntimeException("JBoss fixed, code can be removed now");
		else
		{
			Context ctx = new InitialContext();
			this.registry = (BlueprintRegistry)ctx.lookup("subetha/ListWizard/local");
		}
		
		this.registry.register(this);
	}
	
	/**
	 * @see Lifecycle#stop()
	 */
	public void stop()
	{
		this.registry.deregister(this);
	}
}
