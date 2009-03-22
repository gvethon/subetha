<%@include file="/inc/top_standard.jspf" %>

<t:action var="model" type="org.subethamail.web.action.SaveListSettings"/>

<c:choose>
	<c:when test="${empty model.errors}">
		<c:redirect url="/list.jsp">
			<c:param name="listId" value="${model.listId}" />
		</c:redirect>
	</c:when>
	<c:otherwise>
		<jsp:forward page="/list_settings.jsp" />
	</c:otherwise>				
</c:choose>
