<%@include file="../inc/taglibs.jspf" %>

<%@ attribute name="title" required="true" %>

<trim:base title="${title}">
	<!-- ###### Header ###### -->
	
	<div id="header">
		<div class="locBar">
			<c:choose>
				<c:when test="${auth.loggedIn}">
					You are <c:out value="${auth.authName}" />
					[<a href="logout.jsp">logout</a>]
				</c:when>
				
				<c:otherwise>
					<form action="login_submit.jsp" method="post">
						<table>
							<tr>
								<th>Email:</th>
								<td><input type="text" name="email" /></td>
							</tr>
							<tr>
								<th>Password:</th>
								<td><input type="password" name="password" /></td>
							</tr>
							<tr>
								<th>Remember?</th>
								<td>
									<input type="checkbox" name="remember" />
									<input type="submit" value="Login" />
									<a href="pw_forgot.jsp">forgot?</a>
								</td>
							</tr>
						</table>
					</form>
				</c:otherwise>
			</c:choose>
		</div> <!-- locBar -->
		
		<span class="headerTitle">SubEtha Mail</span>
		
		<c:if test="${auth.loggedIn}">
			<div class="menuBar">
				<a href="home.jsp" class="first">Home</a>
				
				<c:if test="${auth.siteAdmin}">
					<a href="site_status.jsp">Site Administration</a>
				</c:if>
			</div> <!-- menuBar -->
		</c:if>
			
	</div> <!-- header -->
	
	<jsp:doBody/>
	
</trim:base>
