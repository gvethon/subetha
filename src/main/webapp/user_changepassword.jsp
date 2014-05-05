<%@include file="/inc/top_standard.jspf" %>

<t:action var="model" type="org.subethamail.web.action.UserChangePassword"/>

<c:choose>
	<c:when test="${empty model.errors}">
		<c:redirect url="/home.jsp" />
	</c:when>
	<c:otherwise>
		<jsp:forward page="/user_profile.jsp" />
	</c:otherwise>				
</c:choose>
