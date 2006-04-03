<%@include file="/inc/top_standard.jspf" %>

<t:action type="org.subethamail.web.action.auth.AuthRequired" />

<trim:admin title="Create List">
	<h1>Create List</h1>
	
	<form action="<c:url value="/list_create_submit.jsp"/>" method="post">
	
		<table>
			<tr>
				<th><label for="name">Short Name</label></th>
				<td
					<c:if test="${!empty model.errors.name}">
						class="error"
					</c:if>
				>
					<input id="name" name="name" type="text" size="60" value="${model.name}" />
					
					<c:if test="${!empty model.errors.name}">
						<p class="error"><c:out value="${model.errors.name}"/></p>
					</c:if>
				</td>
			</tr>
			<tr>
				<th><label for="description">Description</label></th>
				<td
					<c:if test="${!empty model.errors.description}">
						class="error"
					</c:if>
				>
					<textarea id="description" name="description" rows="5" cols="60" style="width:95%"
					><c:out value="${model.description}"/></textarea>
					
					<c:if test="${!empty model.errors.description}">
						<p class="error"><c:out value="${model.errors.description}"/></p>
					</c:if>
				</td>
			</tr>
			<tr>
				<th><label for="address">List Address</label></th>
				<td
					<c:if test="${!empty model.errors.address}">
						class="error"
					</c:if>
				>
					<input id="address" name="address" type="text" size="60" value="${model.address}" />
					<div>Example:  announce@somedomain.com</div>
					
					<c:if test="${!empty model.errors.address}">
						<p class="error"><c:out value="${model.errors.address}"/></p>
					</c:if>
				</td>
			</tr>
			<tr>
				<th><label for="url">List URL</label></th>
				<td
					<c:if test="${!empty model.errors.url}">
						class="error"
					</c:if>
				>
					<input id="url" name="url" type="text" size="60" value="${model.url}" />
					<div>Example:  http://somedomain.com<strong>/se/list/</strong>announce</div>
					<div>The URL <strong>must</strong> contain /se/list/ after the domain</div>
					
					<c:if test="${!empty model.errors.url}">
						<p class="error"><c:out value="${model.errors.url}"/></p>
					</c:if>
				</td>
			</tr>
			<tr>
				<th><label for="owners">Initial Owner(s)</label></th>
				<td
					<c:if test="${!empty model.errors.owners}">
						class="error"
					</c:if>
				>
					<textarea id="owners" name="owners" rows="5" cols="60" style="width:95%"
					><c:out value="${model.owners}"/></textarea>
					
					<c:if test="${!empty model.errors.owners}">
						<p class="error"><c:out value="${model.errors.owners}"/></p>
					</c:if>
				</td>
			</tr>
		</table>
		
		<h3>Choose Blueprint</h3>
		<table>
			<c:forEach var="blueprint" items="${backend.listWizard.blueprints}" varStatus="loop">
				<tr>
					<th>
						<input type="radio" name="blueprint" value="${blueprint.id}" 
							<c:if test="${(empty model.blueprint && loop.first) || model.blueprint == blueprint.id}">checked="checked"</c:if>
						/>
					</th>
					<th><c:out value="${blueprint.name}" /></th>
					<td><c:out value="${blueprint.description}" /></td>
				</tr>
			</c:forEach>
		</table>
		
		<input type="submit" value="Create List" />

	</form>
</trim:admin>