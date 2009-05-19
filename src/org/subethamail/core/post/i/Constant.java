/*
 * $Id: Constant.java 263 2006-05-04 20:58:25Z lhoriman $
 * $URL: http://subetha.tigris.org/svn/subetha/branches/resin/core/src/org/subethamail/core/post/i/Constant.java $
 */

package org.subethamail.core.post.i;


/**
 * Just a few random constants.
 *
 * @author Jeff Schnitzer
 */
public class Constant
{
	/**
	 * Constant which defines the start of a token string in an email,
	 * but only when debug is enabled.  This makes the token automatically
	 * recognizable by the unit test harness.
	 */
	public static final String DEBUG_TOKEN_BEGIN = "---BEGINTOKEN---";
	public static final String DEBUG_TOKEN_END = "---ENDTOKEN---";
}
