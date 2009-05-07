/*
 * $Id$
 * $URL$
 */

package org.subethamail.web.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.common.MailUtils;
import org.subethamail.core.lists.i.MailData;
import org.subethamail.web.Backend;
import org.tagonist.propertize.Property;

/**
 * This action is used primarily on msg_send.jsp to initialize the data for that page.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class PrepareReply extends PostMessage
{
	/** */
	private final static Logger log = LoggerFactory.getLogger(PrepareReply.class);

	public class Model extends PostMessage.Model
	{
		@Property MailData mailData;
	}

	/** */
	@Override
	public void initialize()
	{
		this.getCtx().setModel(new Model());
	}

	/** */
	@Override
	public void authExecute() throws Exception
	{
		Model model = (Model)this.getCtx().getModel();

		log.debug("msgId: " + model.msgId);

		if (model.msgId != null && model.msgId.longValue() > 0)
		{
			model.mailData = Backend.instance().getArchiver().getMail(model.msgId);
			model.subject = MailUtils.cleanRe(model.mailData.getSubject(), null, true);
			model.listId = model.mailData.getListId();
		}
	}
}
