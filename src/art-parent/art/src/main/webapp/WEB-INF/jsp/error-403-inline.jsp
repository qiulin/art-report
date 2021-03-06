<%-- 
    Document   : error-403-inline
    Created on : 21-Mar-2018, 20:20:40
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<h1>Forbidden</h1>

<c:if test="${showErrors}">
	<table class="table table-bordered">
		<tr>
			<td><b>Page:</b></td>
			<td><c:out value="${requestUri}"/></td>
		</tr>
		<tr>
			<td><b>Status Code:</b></td>
			<td>${statusCode}</td>
		</tr>
		<tr>
			<td><b>Message:</b></td>
			<td>
				<pre>
					<c:out value="${errorMessage}"/>
				</pre>
			</td>
		</tr>
	</table>
</c:if>
