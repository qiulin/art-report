<%-- 
    Document   : footer
    Created on : 15-Sep-2013, 09:15:02
    Author     : Timothy Anyona

Footer that appears on all pages
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<c:set var="administratorEmail">
	<%= art.servlets.ArtConfig.getArtSetting("administrator_email") %>
</c:set>

<div id="pageFooter">
	<div class="navbar navbar-fixed-bottom well" style="bottom: -20px;">
		<div class="container"> 
			<span class="text-muted credit">
				<a href="http://art.sourceforge.net">ART</a>
				&nbsp; A Reporting Tool 
				&nbsp; <spring:message code="footer.text.artVersion"/> ${artVersion}
			</span>
			<span class="text-muted credit pull-right">
				<a href="mailto:${administratorEmail}">
					<spring:message code="footer.link.artAdministrator"/>
				</a>
			</span>
		</div>
	</div>
</div>
