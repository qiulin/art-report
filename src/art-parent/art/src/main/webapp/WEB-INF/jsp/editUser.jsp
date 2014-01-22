<%-- 
    Document   : editUser
    Created on : 19-Jan-2014, 11:14:57
    Author     : Timothy Anyona

Display edit user page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addUser" var="pageTitle"/>
	</c:when>
	<c:otherwise>
		<spring:message code="page.title.editUser" var="pageTitle"/>
	</c:otherwise>
</c:choose>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="users.do"]').parent().addClass('active');
				});

				$(function() {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form:form class="form-horizontal" method="POST" action="" modelAttribute="user">
			<fieldset>
				<c:if test="${formErrors != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="page.message.formErrors"/>
					</div>
				</c:if>
				<c:if test="${not empty error}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<p>${error}</p>
					</div>
				</c:if>

				<input type="hidden" value="${action}">
				<div class="form-group">
					<label class="col-md-4 control-label">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<form:input path="userId" readonly="true" class="form-control"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="username">
						<spring:message code="page.label.username"/>
					</label>
					<div class="col-md-8">
						<form:input path="username" maxlength="30" class="form-control"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label" for="password">
						<spring:message code="page.label.password"/>
					</label>
					<div class="col-md-8">
						<form:password path="password" maxlength="40" autocomplete="off" class="form-control"/>
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
		</form:form>

	</jsp:body>

</t:mainPageWithPanel>
