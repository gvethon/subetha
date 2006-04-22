<%@include file="/inc/top_standard.jspf" %>

<t:action var="filters" type="org.subethamail.web.action.GetFilters" />

<trim:list title="Filters" listId="${param.listId}">
	<p>
		Many menu items are probably missing on this page.  This is due
		to a JBoss <a href="http://jira.jboss.org/jira/browse/EJBTHREE-526">bug</a>.
		It can be worked around by disabling the @SecurityDomain and @RolesAllowed
		annotations on FilterRunnerBean.
	</p>
	
	<h3>Available Filters</h3>
	
	<table>
		<c:forEach var="filter" items="${filters.available}">
			<tr>
				<td>
					<form action="filter_edit.jsp" method="get">
						<input type="hidden" name="listId" value="${param.listId}" />
						<input type="hidden" name="className" value="${filter.className}" />
						<input type="submit" value="Enable" />
					</form>
				</td>
				<td><strong><c:out value="${filter.name}"/></strong></td>
				<td><c:out value="${filter.description}"/></td>
			</tr>
		</c:forEach>
	</table>
	
	
	<h3>Enabled Filters</h3>

	<c:if test="${empty filters.enabled}">
		<p>
			None
		</p>
	</c:if>
	
	<table>
		<c:forEach var="filter" items="${filters.enabled}">
			<tr>
				<td>
					<form action="filter_edit.jsp" method="get">
						<input type="hidden" name="listId" value="${param.listId}" />
						<input type="hidden" name="className" value="${filter.className}" />
						<input type="submit" value="Edit" width="100" />
					</form>
					<form action="filter_delete.jsp" method="get">
						<input type="hidden" name="listId" value="${param.listId}" />
						<input type="hidden" name="className" value="${filter.className}" />
						<input type="submit" value="Disable" width="100" />
					</form>
				</td>
				<td>
					<div><strong><c:out value="${filter.name}"/></strong></div>
					<div><c:out value="${filter.description}"/></div>
				</td>
				<td>
					<table>
						<c:forEach var="argEntry" items="${filter.arguments}">
							<tr>
								<th><c:out value="${argEntry.key}"/></th>
								<td><c:out value="${argEntry.value}"/></td>
							</tr>
						</c:forEach>
					</table>
				</td>
			</tr>
		</c:forEach>
	</table>
</trim:list>