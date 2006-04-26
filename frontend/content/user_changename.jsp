<%@include file="/inc/top_standard.jspf" %>

<t:action var="userChangeName" type="org.subethamail.web.action.UserChangeName"/>

<c:choose>
	<c:when test="${empty model.errors}">
		<c:redirect url="/user_profile.jsp">
		</c:redirect>
	</c:when>
	<c:otherwise>
		<jsp:forward page="/user_profile.jsp" />
	</c:otherwise>				
</c:choose>
