<%-- 
    Document   : language
    Created on : 17-Jan-2014, 11:13:03
    Author     : Timothy Anyona

Display application language selection page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.language" var="pageTitle"/>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[href*="language.do"]').parent().addClass('active');
				});

				$('#lang').focus();
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<fieldset>
				<div class="form-group">
					<label class="col-md-2 control-label" for="lang">
						<spring:message code="page.label.language"/>
					</label>
					<div class="col-md-10">
						<%-- select must have name of "lang" as per configuration in dispatcher-servlet.xml --%>
						<c:set var="localeCode" value="${pageContext.response.locale}"/>
						<select name="lang" id="lang" class="form-control">
							<option value="en">English</option>
							<option data-divider="true"></option>
							<c:forEach var="language" items="${languages}">
								<option value="${language.key}" ${localeCode == language.key ? "selected" : ""}>${language.value}</option>
							</c:forEach>
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.button.save"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>