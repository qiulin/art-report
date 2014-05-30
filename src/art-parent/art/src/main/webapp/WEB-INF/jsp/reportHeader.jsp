<%-- 
    Document   : reportHeader
    Created on : 29-May-2014, 17:39:27
    Author     : Timothy Anyona

Header section of report output for html output reports
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div class="reportBanner">
	<span id="reportInfo">
		<encode:forHtmlContent value="${reportName}"/>
	</span>
	<span id="reportProgress" class="pull-right">
		<spring:message code="reports.message.preparing"/>
	</span>
</div>
