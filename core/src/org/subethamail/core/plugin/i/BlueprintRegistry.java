/*
 * $Id: StatsUpdate.java 121 2006-03-07 09:50:09Z jeff $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/mbean/BindStatisticsManagerMBean.java,v $
 */

package org.subethamail.core.plugin.i;

import javax.ejb.Local;


/**
 * This local interface allows blueprints to register themsleves with the
 * application.  Plugins should register themselves when the application deploys
 * and de-register themselves when the app undeploys.  The JBoss service
 * lifecycle methods start() and stop() can be used.
 *
 * @author Jeff Schnitzer
 */
@Local
public interface BlueprintRegistry
{
	/**
	 * Register a blueprint.  The blueprint will become available
	 * immediately.
	 */
	public void register(Blueprint print);
	
	/**
	 * Deregister a blueprint.
	 */
	public void deregister(Blueprint print);
}
