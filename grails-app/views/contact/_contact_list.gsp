<%@ page import="frontlinesms2.Contact" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<div id="contacts-list">
	<g:if test="${contactInstanceTotal > 0}">
		<ol id="contact-list">
			<g:each in="${contactInstanceList}" status="i" var="c">
				<li class="${c == contactInstance ? 'selected' : ''}" id="contact-${c.id}">
					<g:if test="${contactsSection instanceof frontlinesms2.Group}">
						<g:checkBox name="contact" checked="${params.checkedId == c.id + '' ? 'true': 'false'}" value="${c.id}" onclick="contactChecked(${c.id});"/>	
						<g:link class="displayName-${c.id}" controller="contact" action="show" params="[contactId:c.id, groupId:contactsSection.id]">
							${c.name?:c.primaryMobile?:c.secondaryMobile?:'[No Name]'}</g:link>
					</g:if>
					<g:else>
						<g:checkBox name="contact" checked="${params.checkedId == c.id+'' ? 'true': 'false'}" value="${c.id}" onclick="contactChecked(${c.id});"/>	
						<g:link class="displayName-${c.id}" action="show" id="${c.id}">
							${c.name?:c.primaryMobile?:c.secondaryMobile?:'[No Name]'}</g:link>
					</g:else>
				</li>
			</g:each>
		</ol>
			<g:paginate next="Forward" prev="Back"  action="list" total="${contactInstanceTotal}"/>
	</g:if>
	<g:else>
		<div id="contact-list">
			You have no contacts saved
		</div>
	</g:else>
</div>
