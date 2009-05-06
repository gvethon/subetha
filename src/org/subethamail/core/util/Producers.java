package org.subethamail.core.util;

import javax.annotation.Resource;
import javax.context.ApplicationScoped;
import javax.inject.Produces;
import javax.mail.Session;
import javax.sql.DataSource;

import com.caucho.config.Name;

/**
 * Producers used for creating things with context. Yeah!
 * 
 * @author Scott Hernandez
 *
 */
@ApplicationScoped
public class Producers {

	@Resource(name="java:comp/env/jdbc/subetha")
	DataSource ds;

	@Resource(name="java:comp/env/mail")
	Session ses = null;
	
	@Produces //@Name("subetha")
	Session	createMailSession(){
		return this.ses;
	}
	
	@Produces @Name("subetha")
	DataSource createSubethaDS(){
		return this.ds;
	}
}