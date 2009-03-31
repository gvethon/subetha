/*
 * $Id: SubEthaLoginModule.java 735 2006-08-20 04:21:14Z lhoriman $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/util/SimilarityLoginModule.java,v $
 */

package org.subethamail.core.util;


/**
 * Authenticates against the database
 *
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public class SubEthaLoginModule //extends UsernamePasswordLoginModule
{
	/** Must be the same as what is defined in the DynamicLoginConfig service DD */
	public static final Long UNAUTHENTICATED_IDENTITY = -1L;
	
//	/**
//	 * We need this to lookup data objects
//	 */
//	protected static JaasLogin jaasLogin;
//	
//	protected static JaasLogin getJaasLogin()
//	{
//		if (jaasLogin == null)
//		{
//			try
//			{
//				Context ctx = new InitialContext();
//				jaasLogin = (JaasLogin)ctx.lookup(JaasLogin.JNDI_NAME);
//			}
//			catch (NamingException ex) { throw new RuntimeException(ex); }
//		}
//		
//		return jaasLogin; 
//	} 
//	
//	/** */
//	protected static final Group[] EMPTY_ROLES = { new SimpleGroup("Roles") };
//	
//	/** */
//	protected static final Group[] USER_ROLES = { new SimpleGroup("Roles") };
//	static
//	{
//		USER_ROLES[0].addMember(new SimplePrincipal("user"));
//	}
//	
//	/** */
//	protected static final Group[] SITE_ADMIN_ROLES = { new SimpleGroup("Roles") };
//	static
//	{
//		SITE_ADMIN_ROLES[0].addMember(new SimplePrincipal("user"));
//		SITE_ADMIN_ROLES[0].addMember(new SimplePrincipal("siteAdmin"));
//	}
//
//	
//	/**
//	 * Processed from Admin.getRoles()
//	 */
//	protected Group[] roles;
//	
//	/**
//	 */
//	@Override
//	protected Group[] getRoleSets() throws LoginException
//	{
//		if (this.getIdentity().equals(this.unauthenticatedIdentity))
//		{
//			log.debug("getRoleSets() returning EMPTY_ROLES");
//			return EMPTY_ROLES;
//		}
//		else
//		{
//			if (log.isDebugEnabled())
//				log.debug("getRoleSets() returning " + Arrays.toString(this.roles));
//			
//			return this.roles;
//		}
//	}
//
//	@Override
//	protected String getUsersPassword() throws LoginException
//	{
//		String idStr = this.getUsername();
//		
//		if (log.isDebugEnabled())
//			log.debug("Checking password for " + idStr);
//		
//		Long id = Long.valueOf(idStr);
//
//		try
//		{
//			Person pers = getJaasLogin().getPerson(id);
//			
//			this.roles = new Group[] { new SimpleGroup("Roles") };
//			for (String role: pers.getRoles())
//				roles[0].addMember(new SimplePrincipal(role));
//			
//			return pers.getPassword();
//		}
//		catch (NotFoundException ex)
//		{
//			throw new FailedLoginException("No user with id " + id);
//		}
//	}
}
