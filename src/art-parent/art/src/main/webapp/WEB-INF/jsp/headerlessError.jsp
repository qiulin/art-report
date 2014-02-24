<%-- 
    Document   : headerlessError
    Created on : 07-Nov-2013, 09:41:59
    Author     : Timothy Anyona

Display an error without the main header
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.error" var="pageTitle" scope="page"/>

<t:headerlessPage title="${pageTitle}">
	<jsp:body>
		<div class="row spacer60">
			<c:if test="${not empty message}">
				<div class="col-md-6 col-md-offset-3 alert alert-danger text-center">
					<spring:message code="${message}"/>
				</div>
			</c:if>

			<c:if test="${error != null}">
				<div class="alert alert-danger alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
					<p><spring:message code="page.message.errorOccurred"/></p>
					<p>${error}</p>
				</div>
			</c:if>
		</div>
	</jsp:body>
</t:headerlessPage>
