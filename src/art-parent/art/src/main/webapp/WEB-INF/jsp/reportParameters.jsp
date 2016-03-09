<%-- 
    Document   : reportParameters
    Created on : 09-Mar-2016, 11:17:18
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<c:forEach var="reportParam" items="${reportParamsList}">
	<c:choose>
		<c:when test="${reportParam.parameter.useLov}">
			<jsp:include page="dropdownInput.jsp" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${reportParam.parameter.parameterType == 'MultiValue'}">
					<jsp:include page="textareaInput.jsp" />
				</c:when>
				<c:otherwise>
					<c:set var="reportParam" value="${reportParam}" scope="request"/>
					<jsp:include page="textInput.jsp"/>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</c:forEach>
