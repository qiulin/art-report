<%-- 
    Document   : error-400-inline
    Created on : 22-Jan-2017, 16:59:32
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<h1>Bad Request</h1>

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
