/*
 * $Id: Detacher.java 485 2006-05-22 10:27:21Z lhoriman $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/injector/Detacher.java $
 */

package org.subethamail.core.injector;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimePart;

import org.subethamail.entity.Mail;

/**
 * The detacher processes binary attachments in JavaMail messages.
 * When it finds an attachment, it saves the binary data as a blob
 * in the database and replaces the content with a special indicator.
 * 
 * A binary attachment is any mime type that is not multipart/* or text/*.
 * 
 * TODO:  describe the structure of the indicator here.
 *
 * @author Jeff Schnitzer
 */
public interface Detacher
{
	/**
	 * Removes attachments from the mime message, stores the attachments
	 * as blobs, and substitutes in a special content type that indicates
	 * a link back to the database object.  Recursively descends the mime
	 * tree.
	 */
	public void detach(MimePart part, Mail ownerMail) throws MessagingException, IOException;
	
	/** 
	 * Looks through the mime message for any of the special indicator
	 * link attachments and replaces them with the actual binary content
	 * of the attachment.  Recursively descends the mime tree.
	 */
	public void attach(MimePart part) throws MessagingException, IOException;	
}