<%@include file="/inc/top_standard.jspf" %>

<t:action var="msg" type="org.subethamail.web.action.GetMessage" />

<t:action var="myList" type="org.subethamail.web.action.GetMyListRelationship">
	<t:param name="listId" value="${msg.listId}"/>
</t:action>
<c:set var="perms" value="${myList.perms}"/>


<trim:list title="${msg.subject}" listId="${msg.listId}">
	<table>
		<tr>
			<td>
				<fieldset><legend>Actions</legend>
					<form action="<c:url value="/message/${msg.id}/download/${msg.listId}-${msg.id}.eml"/>" method="get" style="display:inline">
						<input type="submit" value="Download" />
					</form>
					<form action="<c:url value="/message/${msg.id}/view/${msg.listId}-${msg.id}.eml"/>" method="get" style="display:inline">
						<input type="submit" value="View" />
					</form>
					<%--
					<c:if test="${auth.loggedIn && perms.POST}">
						<form action="<c:url value="/archive_msg_reply.jsp"/>" method="get" style="display:inline">
							<input type="hidden" name="msgId" value="${msg.id}"/>
							<input type="submit" value="Reply" />
						</form>
					</c:if>
					--%>
				</fieldset>
			</td>
			<c:if test="${auth.loggedIn}">
				<td>
					<form action="<c:url value="/archive_msg_resend.jsp"/>" method="post" style="display:inline">
						<input type="hidden" name="msgId" value="${msg.id}" />
						
						<fieldset><legend>Send Mail</legend>
							<label for="email"><span style="font-size:smaller">Send this message to: </span></label>
							
							<c:set var="emailAddresses" value="${backend.accountMgr.self.emailAddresses}"/>
							<c:choose>
								<c:when test="${fn:length(emailAddresses) == 1}">
									<input type="hidden" name="email" value="${emailAddresses[0]}"/>
									<strong><c:out value="${emailAddresses[0]}"/></strong>
								</c:when>
								<c:otherwise>
									<select name="email">
										<c:forEach var="email" items="${emailAddresses}" varStatus="loop">
											<option value="<c:out value="${email}"/>"><c:out value="${email}"/></option>
										</c:forEach>
									</select>
								</c:otherwise>
							</c:choose>
							<input type="submit" value="Send" />
						</fieldset>
					</form> 
				</td>
			</c:if>
			<td>
				<c:if test="${perms.DELETE_ARCHIVES}">
					<form action="<c:url value="/archive_msg_delete.jsp"/>" method="post" style="display:inline">
						<input type="hidden" name="msgId" value="${msg.id}" />
						
						<fieldset><legend>Administration</legend>
							<input type="submit" value="Delete" />
						</fieldset>
					</form> 
				</c:if>
			</td>
		</tr>
	</table>
	
	<p>
		From
		<span class="authorName"><c:out value="${msg.fromName}"/></span>
		
		<c:if test="${!empty msg.fromEmail}">
			<span class="authorEmail">
				&lt;<a href="mailto:<c:out value="${msg.fromEmail}"/>"><c:out value="${msg.fromEmail}"/></a>&gt;
			</span>
		</c:if>
		
		<span class="messageDate"><fmt:formatDate value="${msg.sentDate}" type="both" timeStyle="short" /></span>
	</p>

	<div class="message">
		<c:forEach var="part" items="${msg.textParts}">
			<div class="messagePart">
				<pre class="message" ><c:out value="${part.contents}"/></pre>
			</div>
		</c:forEach>
	</div>

	<c:if test="${!empty msg.attachments}">
		<div class="attachments">
			<h3>Attachments</h3>
			<ul>
				<c:forEach var="attachment" items="${msg.attachments}">
					<li>
						<a href="<c:url value="/attachment/${attachment.id}/${attachment.name}"/>"><c:out value="${attachment.name}" /></a>
					</li>
				</c:forEach>	
			</ul>
		</div>
	</c:if>
	
	<c:if test="${! (msg == msg.threadRoot && empty msg.replies)}">
		<h3>Thread History</h3>
		<ul class="rootSummaries">
			<li>
				<div class="nestedSummaries">
					<se:summaryNode msg="${msg.threadRoot}" highlight="${msg}"/>
				</div>
			</li>
		</ul>
		</div>
	</c:if>
</trim:list>