/*
 * $Id$
 * $URL$
 */

package org.subethamail.rtest.util;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.subethamail.core.acct.i.AccountMgr;
import org.subethamail.core.acct.i.AccountMgrRemote;
import org.subethamail.core.admin.i.Admin;
import org.subethamail.core.admin.i.AdminRemote;
import org.subethamail.core.admin.i.ListWizard;
import org.subethamail.core.admin.i.ListWizardRemote;
import org.subethamail.core.admin.i.Plumber;
import org.subethamail.core.admin.i.PlumberRemote;
import org.subethamail.core.injector.i.Injector;
import org.subethamail.core.injector.i.InjectorRemote;
import org.subethamail.core.lists.i.Archiver;
import org.subethamail.core.lists.i.ArchiverRemote;
import org.subethamail.core.lists.i.ListMgr;
import org.subethamail.core.lists.i.ListMgrRemote;
import org.subethamail.core.search.i.Indexer;
import org.subethamail.core.search.i.IndexerRemote;

/**
 * This class makes it easy to obtain and use the various
 * EJBs.  Extending this class provides accessors that
 * establish the correct identity first.
 * 
 * Subclasses should override getEmail() and getPassword().
 * 
 * @author Jeff Schnitzer
 */
public class BeanMixin
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(BeanMixin.class);

	private AccountMgr accountMgr;
	private Admin admin;
	private ListMgr listMgr;
	private ListWizard listWizard;
	private Indexer indexer;
	private Injector injector;
	private Archiver archiver;
	private Plumber plumber;
	
	
	/** */
	public BeanMixin() throws Exception
	{
		Context ctx = new InitialContext();
		this.admin = (Admin)ctx.lookup(AdminRemote.JNDI_NAME);
		this.accountMgr = (AccountMgr)ctx.lookup(AccountMgrRemote.JNDI_NAME);
		this.listMgr = (ListMgr)ctx.lookup(ListMgrRemote.JNDI_NAME);
		this.listWizard = (ListWizard)ctx.lookup(ListWizardRemote.JNDI_NAME);
		this.indexer = (Indexer)ctx.lookup(IndexerRemote.JNDI_NAME);
		this.injector = (Injector)ctx.lookup(InjectorRemote.JNDI_NAME);
		this.archiver = (Archiver)ctx.lookup(ArchiverRemote.JNDI_NAME);
		this.plumber = (Plumber)ctx.lookup(PlumberRemote.JNDI_NAME);
	}
	
	/** If this is null, clears all credentials */
	public String getPrincipalName() { return null; };
	
	/** */
	public String getPassword() { return null; }
	
	/**
	 * Establish administrator credentials
	 */
	public void establish()
	{
		try
		{
			SecurityClient securityClient = SecurityClientFactory.getSecurityClient();
			
			if (this.getPrincipalName() == null)
			{
				securityClient.logout();
			}
			else
			{
				securityClient.setSimple(this.getPrincipalName(), this.getPassword());
				securityClient.login();
			}
		}
		catch (Exception ex) { throw new RuntimeException(ex); }
	}
	
	/** */
	public Admin getAdmin() throws Exception
	{
		this.establish();
		return this.admin;
	}
	
	/** */
	public AccountMgr getAccountMgr()
	{
		this.establish();
		return this.accountMgr;
	}
	
	/** */
	public ListMgr getListMgr()
	{
		this.establish();
		return this.listMgr;
	}

	/** */
	public ListWizard getListWizard()
	{
		this.establish();
		return this.listWizard;
	}
	
	/** */
	public Indexer getIndexer()
	{
		this.establish();
		return this.indexer;
	}
	
	/** */
	public Injector getInjector()
	{
		this.establish();
		return this.injector;
	}
	
	/** */
	public Archiver getArchiver()
	{
		this.establish();
		return this.archiver;
	}
	
	/** */
	public Plumber getPlumber()
	{
		this.establish();
		return this.plumber;
	}
}
