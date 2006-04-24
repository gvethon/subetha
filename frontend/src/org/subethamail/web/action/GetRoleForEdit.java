/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.core.lists.i.RoleData;
import org.subethamail.web.Backend;
import org.subethamail.web.action.RoleSave.Model;
import org.subethamail.web.action.auth.AuthAction;
import org.tagonist.propertize.Property;

/**
 * Pre-populates a model for RoleSave.
 * 
 * @author Jeff Schnitzer
 */
public class GetRoleForEdit extends AuthAction 
{
	/** */
	private static Log log = LogFactory.getLog(GetRoleForEdit.class);
	
	/** */
	@Property Long roleId;
	
	/** */
	public void execute() throws Exception
	{
		Model model = new Model();
		
		RoleData data = Backend.instance().getListMgr().getRole(this.roleId);
		
		model.roleId = data.getId();
		model.listId = data.getListId();
		model.name = data.getName();
		model.realPermissions = data.getPermissions();
		
		this.getCtx().setModel(model);
	}
	
}
