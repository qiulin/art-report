<%-- 
    Document   : error-400
    Created on : 20-May-2014, 17:57:37
    Author     : Timothy Anyona

Display 400 error (bad request)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
        <title>ART - Bad Request</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/js/bootstrap-3.3.6/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/public/images/favicon.ico">
    </head>
    <body>
		<jsp:include page="/WEB-INF/jsp/error-400-inline.jsp"/>
    </body>
</html>