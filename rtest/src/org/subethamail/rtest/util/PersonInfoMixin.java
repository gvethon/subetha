/*
 * $Id$
 * $URL$
 */

package org.subethamail.rtest.util;

import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.common.Utils;

/**
 * @author Jeff Schnitzer
 */
public class PersonInfoMixin extends BeanMixin
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(PersonInfoMixin.class);

	String email;
	String password;
	String name;
	InternetAddress address;
	
	/** */
	public PersonInfoMixin() throws Exception
	{
		String uniqueString = Utils.uniqueString();
		
		this.password = "asdf" + uniqueString;
		this.email = "subetha-" + uniqueString + "@localhost";
		
		this.name = "Test User";
		
		this.address = new InternetAddress(this.email, this.name);
	}
	
	@Override
	public String getPassword() { return this.password; }
	
	public String getEmail() { return this.email; }
	
	public String getName() { return this.name; }
	
	public InternetAddress getAddress() { return this.address; }
	
	/** Used to modify credentials */
	public void setPassword(String value) { this.password = value; }
}
