<%@include file="/inc/top_standard.jspf" %>

<t:action var="model" type="org.subethamail.web.action.GetHeldMessages" />

<trim:list title="Held Messages" listId="${param.listId}">
	<c:choose>
		<c:when test="${empty model.holds}">
			<p>There are no held messages to this list.</p>
		</c:when>
		
		<c:otherwise>

			<table class="sort-table" id="lists-table">
			<thead>
				<tr>
					<td>Date</td>
					<td>Subject</td>
					<td>From</td>
					<td>Type</td>
					<td>Action</td>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="msg" items="${model.holds}" varStatus="loop">
					<c:choose>
						<c:when test="${loop.index % 2 == 0}">
							<c:set var="color" value="a"/>
						</c:when>
						<c:otherwise>
							<c:set var="color" value="b"/>
						</c:otherwise>
					</c:choose>
					<tr class="${color}">
						<td>
							<fmt:formatDate value="${msg.date}" type="both" dateStyle="short" timeStyle="short"/>
						</td>
						<td>
							<c:url var="msgUrl" value="/archive_msg.jsp">
								<c:param name="msgId" value="${msg.id}"/>
							</c:url>
							<a href="${msgUrl}" target="held">
								<c:if test="${empty msg.subject}">(no subject)</c:if><c:out value="${msg.subject}"/>
							</a>
						</td>
						<td>
							<c:out value="${msg.from}"/>
						</td>
						<td>
							<c:choose>
								<c:when test="${msg.hard}">
									<div class="error">Hard</div>
								</c:when>
								<c:otherwise>
									Soft
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<form action="<c:url value="/held_msg_action.jsp" />" method="post" style="display:inline">
								<input type="hidden" name="msgId" value="${msg.id}" />
								<input type="hidden" name="action" value="Discard" />
								<input type="submit" value="Discard" />
							</form>
							<form action="<c:url value="/held_msg_action.jsp" />" method="post" style="display:inline">
								<input type="hidden" name="msgId" value="${msg.id}" />
								<input type="hidden" name="action" value="Approve" />								
								<input type="submit" value="Approve" />
							</form>
							<form action="<c:url value="/held_msg_action.jsp" />" method="post" style="display:inline">
								<input type="hidden" name="msgId" value="${msg.id}" />
								<input type="hidden" name="action" value="Subscribe"/>
								<input type="submit" value="Approve and Subscribe" />
							</form>
						</td>
					</tr>
				</c:forEach>
			</tbody>
			</table>

<script type="text/javascript">
var st1 = new SortableTable(document.getElementById("lists-table"), ["String", "String", "String"]);
st1.onsort = st1.tableRowColors;
</script>

			<c:url var="pagurl" value="/held_msgs.jsp">
				<c:param name="listId" value="${param.listId}"/>
			</c:url>
			<se:searchPaginator url="${pagurl}&" model="${model}"/>

		<br /><br /><br />

		<table class="sort-table">
			<thead>
			<tr>
				<td>Hold Type</td>
				<td>Description</td>
			</tr>
			</thead>
			<tbody>
			<tr>
				<td>Soft</td>
				<td>Can be self-approved by the poster.</td>
			</tr>
			<tr>
				<td>Hard</td>
				<td>Must be manually approved by the moderator no matter what.</td>
			</tr>
			</tbody>
		</table>

		</c:otherwise>
	</c:choose>
</trim:list>