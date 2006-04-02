/*
 * $Id: PersonInfoMixin.java 86 2006-02-22 03:36:01Z jeff $
 * $URL: https://svn.infohazard.org/blorn/trunk/rtest/src/com/blorn/rtest/util/PersonInfoMixin.java $
 */

package org.subethamail.rtest.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jeff Schnitzer
 */
public class PersonInfoMixin
{
	/** */
	private static Log log = LogFactory.getLog(PersonInfoMixin.class);

	String email;
	String password;
	String name;
	
	/** */
	public PersonInfoMixin() throws Exception
	{
		String objectId = this.toString();
		// looks like:  com.similarity.rtest.PersonInfoMixin@bb0d0d
		objectId = objectId.substring(objectId.lastIndexOf('@') + 1);
		
		String baseName = Long.toString(System.currentTimeMillis(), 36); 
		String name =  baseName + "-" + objectId;
		
		this.password = "asdf";
		this.email = "subetha-" + name + "@localhost";
		
		this.name = "Test User";
	}
	
	/** */
	public String getEmail() { return this.email; }
	public String getPassword() { return this.password; }
	public String getName() { return this.name; }
	
	/** Used to modify credentials */
	public void setPassword(String value) { this.password = value; }
}
