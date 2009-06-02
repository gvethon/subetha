/*
 * $Id: EmailAddressTest.java 1105 2009-05-10 04:16:03Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/EmailAddressTest.java $
 */

package org.subethamail.rtest;

import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.rtest.util.AdminMixin;
import org.subethamail.rtest.util.MailingListMixin;
import org.subethamail.rtest.util.PersonInfoMixin;
import org.subethamail.rtest.util.Smtp;
import org.subethamail.rtest.util.SubEthaTestCase;

/**
 * Testing the fallthrough mechanism.
 * 
 * @author Jeff Schnitzer
 */
public class FallthroughTest extends SubEthaTestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(FallthroughTest.class);
	
	/** */
	public static final int FALLTHROUGH_PORT = 2526;

	/** */
	Smtp fallthrough;
	AdminMixin admin;
	MailingListMixin ml;
	PersonInfoMixin pers;
	
	/** */
	public FallthroughTest(String name) { super(name); }
	
	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.fallthrough = new Smtp();
		this.fallthrough.setPort(FALLTHROUGH_PORT);
		this.fallthrough.start();
		
		this.admin = new AdminMixin();
		this.ml = new MailingListMixin(this.admin, null);
		this.pers = new PersonInfoMixin();
		
		this.admin.getAdmin().setFallthroughHost("localhost:" + FALLTHROUGH_PORT);
	}
	
	/** */
	protected void tearDown() throws Exception
	{
		this.fallthrough.stop();
		this.admin.getAdmin().setFallthroughHost(null);
		
		super.tearDown();
	}
	
	/** */
	public void testListOnly() throws Exception
	{
		MimeMessage msg = this.createMimeMessage(this.pers.getAddress(), this.ml.getAddress());
		Transport.send(msg);
		
		assertEquals(0, this.fallthrough.countSubject(TEST_SUBJECT));
		assertEquals(0, this.smtp.countSubject(TEST_SUBJECT));
		assertEquals(1, this.admin.getArchiver().countMailByList(this.ml.getId()));
	}
	
	/** */
	public void testTwoLists() throws Exception
	{
		MailingListMixin ml2 = new MailingListMixin(this.admin, null);
		
		MimeMessage msg = this.createMimeMessage(this.pers.getAddress(), this.ml.getAddress());
		msg.addRecipient(RecipientType.TO, ml2.getAddress());
		Transport.send(msg);
		
		assertEquals(0, this.fallthrough.countSubject(TEST_SUBJECT));
		assertEquals(0, this.smtp.countSubject(TEST_SUBJECT));
		assertEquals(1, this.admin.getArchiver().countMailByList(this.ml.getId()));
		assertEquals(1, this.admin.getArchiver().countMailByList(ml2.getId()));
	}
	
	/** */
	public void testFallthroughOnly() throws Exception
	{
		PersonInfoMixin pers2 = new PersonInfoMixin();
		
		MimeMessage msg = this.createMimeMessage(this.pers.getAddress(), pers2.getAddress());
		Transport.send(msg);
		
		assertEquals(1, this.fallthrough.countSubject(TEST_SUBJECT));
		assertEquals(0, this.smtp.countSubject(TEST_SUBJECT));
		assertEquals(0, this.admin.getArchiver().countMailByList(this.ml.getId()));
	}
	
	/** */
	public void testTwoFallthroughs() throws Exception
	{
		PersonInfoMixin pers2 = new PersonInfoMixin();
		PersonInfoMixin pers3 = new PersonInfoMixin();
		
		MimeMessage msg = this.createMimeMessage(this.pers.getAddress(), pers2.getAddress());
		msg.addRecipient(RecipientType.TO, pers3.getAddress());
		Transport.send(msg);
		
		assertEquals(2, this.fallthrough.countSubject(TEST_SUBJECT));
		assertEquals(0, this.smtp.countSubject(TEST_SUBJECT));
		assertEquals(0, this.admin.getArchiver().countMailByList(this.ml.getId()));
	}
	
	/** */
	public void testFallthroughPlusList() throws Exception
	{
		PersonInfoMixin pers2 = new PersonInfoMixin();
		
		MimeMessage msg = this.createMimeMessage(this.pers.getAddress(), this.ml.getAddress());
		msg.addRecipient(RecipientType.TO, pers2.getAddress());
		Transport.send(msg);
		
		assertEquals(1, this.fallthrough.countSubject(TEST_SUBJECT));
		assertEquals(0, this.smtp.countSubject(TEST_SUBJECT));
		assertEquals(1, this.admin.getArchiver().countMailByList(this.ml.getId()));
	}
	
	/** */
	public static Test suite()
	{
		return new TestSuite(FallthroughTest.class);
	}
}
