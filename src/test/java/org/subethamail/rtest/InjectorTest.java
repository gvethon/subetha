/*
 * $Id$
 * $URL$
 */

package org.subethamail.rtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Before;
import org.subethamail.core.post.i.MailType;
import org.subethamail.rtest.util.AdminMixin;
import org.subethamail.rtest.util.MailingListMixin;
import org.subethamail.rtest.util.PersonMixin;
import org.subethamail.rtest.util.SubEthaTestCase;
import org.subethamail.wiser.WiserMessage;

/**
 * @author Jeff Schnitzer
 */
public class InjectorTest extends SubEthaTestCase
{
	/** */
	AdminMixin admin;
	MailingListMixin ml;
	PersonMixin person1;
	
	/**
	 * Creates a mailing list with two people subscribed.
	 */
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		
		this.admin = new AdminMixin();
		this.person1 = new PersonMixin(this.admin);
		this.ml = new MailingListMixin(this.admin, null);
		
		this.person1.getAccountMgr().subscribeMe(this.ml.getId(), this.person1.getEmail());
	}

	/** */
    @org.junit.Test
	public void testTrivialInjection() throws Exception
	{
        Assume.assumeTrue(ResinTestSetup.exists());
		// Create a second subscriber to make it more interesting
		PersonMixin person2 = new PersonMixin(this.admin);
		person2.getAccountMgr().subscribeMe(this.ml.getId(), person2.getEmail());
		
		this.admin.getAdmin().log("############### Starting testTrivialInjection()");
		
		this.smtp.debugPrintSubjects();
		
		// Two "you are subscribed" msgs
		assertEquals(2, this.smtp.count(MailType.YOU_SUBSCRIBED));
		
		byte[] rawMsg = this.createMessage(this.person1.getAddress(), this.ml.getAddress());
		
		this.admin.getInjector().inject(this.person1.getAddress().getAddress(), this.ml.getEmail(), rawMsg);
		
		Thread.sleep(1000);
		
		assertEquals(2, this.smtp.countSubject(TEST_SUBJECT));
		
		this.admin.getAdmin().log("############### Ended testTrivialInjection()");
	}
	
	/** */
    @org.junit.Test
	public void testHasVERP() throws Exception
	{
        Assume.assumeTrue(ResinTestSetup.exists());
		this.admin.getAdmin().log("############### Starting testHasVERP()");
		
		// Two "you are subscribed" msg
		assertEquals(1, this.smtp.count(MailType.YOU_SUBSCRIBED));
		
		byte[] rawMsg = this.createMessage(this.person1.getAddress(), this.ml.getAddress());
		
		this.admin.getInjector().inject(this.person1.getAddress().getAddress(), this.ml.getEmail(), rawMsg);
		
		Thread.sleep(1000);
		
		assertEquals(1, this.smtp.countSubject(TEST_SUBJECT));
		WiserMessage msg = this.smtp.getSubject(TEST_SUBJECT, 0);
		
		String envelopeSender = msg.getEnvelopeSender();
		assertTrue(envelopeSender.contains("-verp-"));
		
		this.admin.getAdmin().log("############### Ended testHasVERP()");
	}

	/** */
    @org.junit.Test
	public void testVERPBounceMessage() throws Exception
	{
        Assume.assumeTrue(ResinTestSetup.exists());
		this.admin.getAdmin().log("############### Starting testVERPBounceMessage()");

		this.admin.getInjector().inject(this.smtp.get(0).getEnvelopeSender(), this.smtp.get(0).getEnvelopeSender(), this.smtp.get(0).getData());
		this.admin.getInjector().inject(this.smtp.get(0).getEnvelopeSender(), this.smtp.get(0).getEnvelopeSender(), this.smtp.get(0).getData());
		this.admin.getInjector().inject(this.smtp.get(0).getEnvelopeSender(), this.smtp.get(0).getEnvelopeSender(), this.smtp.get(0).getData());
		
		// TODO: there is no api i can find that will tell me the bouncecount for the envelopesender.
		// So, if you look in the database after this method runs, then you will see the bouncecount
		// is 2.
	}
}
