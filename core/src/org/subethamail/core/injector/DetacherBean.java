/*
 * $Id$
 * $URL$
 */

package org.subethamail.core.injector;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.lob.BlobImpl;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.subethamail.common.NotFoundException;
import org.subethamail.common.SubEthaMessage;
import org.subethamail.common.io.TrivialDataSource;
import org.subethamail.core.util.EntityManipulatorBean;
import org.subethamail.entity.Attachment;
import org.subethamail.entity.Mail;

/**
 * @author Jeff Schnitzer
 */
@Stateless(name="Detacher")
@SecurityDomain("subetha")
//@PermitAll
//@RunAs("siteAdmin")
public class DetacherBean extends EntityManipulatorBean implements Detacher
{
	/** */
	private static Log log = LogFactory.getLog(DetacherBean.class);
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.injector.Detacher#detach(javax.mail.internet.MimePart, org.subethamail.entity.Mail)
	 */
	public void detach(MimePart part, Mail ownerMail) throws MessagingException, IOException
	{
		if (log.isDebugEnabled())
			log.debug("Attempting to detach " + part + " of type " + part.getContentType());

		String contentType = part.getContentType().toLowerCase();
		
		if (contentType.startsWith("multipart/"))
		{
			if (log.isDebugEnabled())
				log.debug("Content is multipart");
			
			Multipart multi = (Multipart)part.getContent();
			
			// This is necessary because of the mysterious JavaMail bug 4404733
			part.setContent(multi);
			
			for (int i=0; i<multi.getCount(); i++)
				this.detach((MimeBodyPart)multi.getBodyPart(i), ownerMail);
		}
		else if (contentType.startsWith("text/") && !Part.ATTACHMENT.equals(part.getDisposition()))
		{
			if (log.isDebugEnabled())
				log.debug("Leaving text alone");
		}
		else
		{
			// It would be better if we could do this by knowing which
			// mime types produce Part
			Object content = part.getContent();
			
			if (content instanceof MimePart)
			{
				if (log.isDebugEnabled())
					log.debug("Content " + contentType + " is part, probably a message");
				
				this.detach((MimePart)content, ownerMail);
			}
			else
			{
				// Actually detach the sucka
				if (log.isDebugEnabled())
					log.debug("Detaching an attachment of type " + contentType);
				
				InputStream input = part.getInputStream();
				Blob blobby = new BlobImpl(input, input.available());
				
				Attachment attach = new Attachment(ownerMail, blobby, contentType);
				this.em.persist(attach);
				ownerMail.getAttachments().add(attach);
				
				part.setContent(attach.getId(), SubEthaMessage.DETACHMENT_MIME_TYPE);
				
				part.setHeader(SubEthaMessage.HDR_ORIGINAL_CONTENT_TYPE, contentType);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.core.injector.Detacher#attach(javax.mail.internet.MimePart)
	 */
	public void attach(MimePart part) throws MessagingException, IOException
	{
		if (log.isDebugEnabled())
			log.debug("Attempting reattachment for " + part + " of type " + part.getContentType());

		String contentType = part.getContentType().toLowerCase();
		
		if (contentType.startsWith("multipart/"))
		{
			if (log.isDebugEnabled())
				log.debug("Content is multipart");
			
			Multipart multi = (Multipart)part.getContent();
			
			// This is necessary because of the mysterious JavaMail bug 4404733
			part.setContent(multi);

			for (int i=0; i<multi.getCount(); i++)
				this.attach((MimePart)multi.getBodyPart(i));
		}
		else if (contentType.startsWith(SubEthaMessage.DETACHMENT_MIME_TYPE))
		{
			Long attachmentId = (Long)part.getContent();

			if (log.isDebugEnabled())
				log.debug("Reattaching attachment " + attachmentId + " for type " + contentType);

			try
			{
				Attachment att = this.em.get(Attachment.class, attachmentId);
				
				part.setDataHandler(
						new DataHandler(
								new TrivialDataSource(
										att.getContentStream(),
										att.getContentType())));
				
				part.removeHeader(SubEthaMessage.HDR_ORIGINAL_CONTENT_TYPE);
			}
			catch (NotFoundException ex)
			{
				// Log an error and otherwise leave the mime part as-is.
				if (log.isErrorEnabled())
					log.error("Missing referenced attachment " + attachmentId);
			}
		}
		else
		{
			Object content = part.getContent();
			
			if (content instanceof MimePart)
			{
				if (log.isDebugEnabled())
					log.debug("Content " + contentType + " is part, probably a message");
				
				this.attach((MimePart)content);
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug("Ignoring part of type " + contentType);
			}
		}
	}
	
}
