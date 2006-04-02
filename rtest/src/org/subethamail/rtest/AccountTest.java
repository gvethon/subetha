/*
 * $Id: FavoriteBlogTest.java 86 2006-02-22 03:36:01Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/rtest/src/com/blorn/rtest/acct/FavoriteBlogTest.java $
 */

package org.subethamail.rtest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.core.acct.i.Self;
import org.subethamail.rtest.util.AdminMixin;
import org.subethamail.rtest.util.PersonMixin;

/**
 * Tests for user account manipulation.
 * 
 * @author Jeff Schnitzer
 */
public class AccountTest extends TestCase
{
	/** */
	private static Log log = LogFactory.getLog(AccountTest.class);

	/** */
	AdminMixin admin;
	PersonMixin pers;
	
	/** */
	public AccountTest(String name) { super(name); }
	
	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.admin = new AdminMixin();
		this.pers = new PersonMixin(this.admin);
	}
	
	/** */
	public void testGetSelf() throws Exception
	{
		Self self = this.pers.getAccountMgr().getSelf();
		assertEquals(this.pers.getId(), self.getId());
		assertEquals(this.pers.getName(), self.getName());
		assertEquals(1, self.getEmailAddresses().length);
		assertEquals(this.pers.getEmail(), self.getEmailAddresses()[0]);
		assertFalse(self.isSiteAdmin());
	}
	
	/** */
	public void testSetAdmin() throws Exception
	{
		Self self = this.pers.getAccountMgr().getSelf();
		assertFalse(self.isSiteAdmin());
		
		this.admin.getAdmin().setSiteAdmin(this.pers.getId(), true);
		
		self = this.pers.getAccountMgr().getSelf();
		assertTrue(self.isSiteAdmin());
	}
	
	/** */
	public static Test suite()
	{
		return new TestSuite(AccountTest.class);
	}
}
