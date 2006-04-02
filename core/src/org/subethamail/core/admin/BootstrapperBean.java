/*
 * $Id: ReceptionistEJB.java 110 2006-02-28 06:59:40Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/core/src/com/blorn/core/recep/ReceptionistEJB.java $
 */

package org.subethamail.core.admin;

import javax.annotation.EJB;
import javax.annotation.security.RunAs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.security.SecurityDomain;
import org.subethamail.common.NotFoundException;
import org.subethamail.core.admin.i.Admin;
import org.subethamail.entity.Config;
import org.subethamail.entity.dao.DAO;

/**
 * This bean really is only used when run for the first time
 * after a fresh install.  It makes sure there is one account
 * with siteAdmin permissions.
 * 
 * For the moment, the email address and password are hard
 * coded to "root@localhost" and "password".
 * 
 * TODO:  These default values should probably be dynamically
 * obtained, perhaps from a system property?
 * 
 * Note that this bean has neither remote nor local interfaces.
 * 
 * @author Jeff Schnitzer
 */
@Service(name="Bootstrapper", objectName="subetha:service=Bootstrapper")
// This depends annotation can be removed when JBoss fixes dependency bug.
@Depends("jboss.j2ee:ear=subetha.ear,jar=entity.jar,name=DAO,service=EJB3")
@SecurityDomain("subetha")
@RunAs("siteAdmin")
public class BootstrapperBean implements BootstrapperManagement
{
	/** */
	private static Log log = LogFactory.getLog(BootstrapperBean.class);
	
	/**
	 */
	private static final String DEFAULT_EMAIL = "root@localhost";
	private static final String DEFAULT_NAME = "Administrator";
	private static final String DEFAULT_PASSWORD = "password";
	
	/**
	 * The config id of a Boolean that lets us know if we've run or not.
	 */
	public static final String BOOTSTRAPPED_CONFIG_ID = "bootstrapped";
	
	/** */
	@EJB DAO dao;
	@EJB Admin admin;

	/**
	 * @see BootstrapperManagement#start()
	 */
	public void start() throws Exception
	{
		// If we haven't been bootstrapped, we need to run.
		try
		{
			Config cfg = this.dao.findConfig(BOOTSTRAPPED_CONFIG_ID);
			
			// Might as well sanity check it
			Boolean value = (Boolean)cfg.getValue();
			
			if (value == null || value == false)
			{
				this.bootstrap();
				cfg.setValue(Boolean.TRUE);
			}
		}
		catch (NotFoundException ex)
		{
			this.bootstrap();
			
			Config cfg = new Config(BOOTSTRAPPED_CONFIG_ID, Boolean.TRUE);
			this.dao.persist(cfg);
		}
	}
	
	/**
	 * Creates the appropriate username and password
	 */
	public void bootstrap()
	{
		log.debug("Bootstrapping - establishing default site administrator");
		
		Long id = this.admin.establishPerson(DEFAULT_EMAIL, DEFAULT_NAME, DEFAULT_PASSWORD);
		
		try
		{
			this.admin.setSiteAdmin(id, true);
		}
		catch (NotFoundException ex)
		{
			log.error("Impossible to establish person and then not find!", ex);
			throw new RuntimeException(ex);
		}
	}
}
