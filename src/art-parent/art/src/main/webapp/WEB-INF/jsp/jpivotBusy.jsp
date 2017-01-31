<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART - JPivot Busy</title>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/public/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/js/bootstrap-3.3.6/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/css/font-awesome-4.5.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/css/art.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/art.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/bootstrap-3.3.6/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>
	</head>
	<body>
		<div id="wrap">
			<jsp:include page="/WEB-INF/jsp/header.jsp"/>

			<div id="pageContent">
				<div class="container-fluid">
					<div class="row">
						<div class="col-md-6 col-md-offset-3 alert alert-warning text-center">
							<spring:message code="analysis.message.jpivotQueryWait"/>
							<img src="${pageContext.request.contextPath}/public/images/spinner.gif">
						</div>
					</div>
				</div>
			</div>
			<div id="push"></div>
		</div>

		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</body>
</html>