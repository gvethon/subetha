/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action;

import org.hibernate.validator.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.entity.i.Validator;
import org.subethamail.web.Backend;
import org.subethamail.web.action.auth.AuthRequired;
import org.subethamail.web.model.ErrorMapModel;
import org.tagonist.propertize.Property;

/**
 * Changes a user's name.
 * 
 * @author Jon Stevens
 * @Aauthor Jeff Schnitzer
 */
public class UserChangeName extends AuthRequired 
{
	/** */
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(UserChangeName.class);
	
	/** */
	public static class Model extends ErrorMapModel
	{
		/** */
		@Length(max=Validator.MAX_PERSON_NAME)
		@Property String name = "";
	}

	/** */
	public void initialize()
	{
		this.getCtx().setModel(new Model());
	}
	
	/** */
	public void authExecute() throws Exception
	{
		Model model = (Model)this.getCtx().getModel();

		// Basic validation
		model.validate();
		
		Backend.instance().getAccountMgr().setName(model.name);
	}
}
