/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action;

import java.net.MalformedURLException;
import java.net.URL;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.Length;
import org.subethamail.common.SiteUtils;
import org.subethamail.common.valid.Validator;
import org.subethamail.core.admin.i.DuplicateListDataException;
import org.subethamail.core.admin.i.InvalidListDataException;
import org.subethamail.web.Backend;
import org.subethamail.web.action.auth.AuthRequired;
import org.subethamail.web.model.ErrorMapModel;
import org.tagonist.propertize.Property;

/**
 * Creates a mailing list.  The model starts and remains a CreateList.Model.
 * 
 * @author Jeff Schnitzer
 */
public class CreateList extends AuthRequired 
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(CreateList.class);
	
	/** */
	public static class Model extends ErrorMapModel
	{
		/** Will be populated if exeuction is successful */
		@Property Long id;
		
		/** */
		@Length(min=1, max=Validator.MAX_LIST_NAME)
		@Property String name = "";
	
		/** */
		@Length(max=Validator.MAX_LIST_DESCRIPTION)
		@Property String description = "";
	
		/** */
		@Length(max=Validator.MAX_LIST_EMAIL)
		@Property String email = "";
	
		/** */
		@Length(max=Validator.MAX_LIST_URL)
		@Property String url = "";

		/** */
		@Property String owners = "";
		
		/** */
		@Property String blueprint = "";
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
		
		// Check the URL
		URL url = null;
		try
		{
			url = new URL(model.url);
			
			if(!SiteUtils.isValidListUrl(url))
				model.setError("url", "List url must contain " + SiteUtils.LIST_SERVLET_PATH);
		}
		catch (MalformedURLException ex)
		{
			model.setError("url", ex.getMessage());
		}
		
		
		// Check the address
		InternetAddress listAddress = null;
		try
		{
			listAddress = new InternetAddress(model.email);
			listAddress.validate();
			listAddress.setPersonal(model.name);
		}
		catch (AddressException ex)
		{
			model.setError("email", ex.getMessage());
		}
		
		// Check the list owners
		InternetAddress[] owners = null;
		try
		{
			owners = InternetAddress.parse(model.owners);
			for (InternetAddress owner: owners)
				owner.validate();
		}
		catch (AddressException ex)
		{
			model.setError("owners", ex.getMessage());
		}
		
		// Maybe we can proceed?
		if (model.getErrors().isEmpty())
		{
			try
			{
				model.id = Backend.instance().getListWizard().createMailingList(listAddress, url, model.description, owners, model.blueprint);
			}
			catch (InvalidListDataException ex)
			{
				if (ex.isOwnerAddress())
					model.setError("email", "Addresses cannot end with -owner");
				
				if (ex.isVerpAddress())
					model.setError("email", "Conflicts with the VERP address format");
			}
			catch (DuplicateListDataException ex)
			{
				if (ex.isAddressTaken())
					model.setError("email", "That address is already in use");
				
				if (ex.isUrlTaken())
					model.setError("url", "That url is already in use");
			}
		}
	}
	
}
