<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART</title>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.0.3/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.0.0/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>
	</head>
	<body>

		<jsp:include page="/WEB-INF/jsp/header.jsp"/>

		<table class="centerTableAuto">
			<tr>
				<td colspan="2" class="data" align="center">
					<b><span style="color:red">
							<spring:message code="analysis.message.jpivotQueryWait"/>
						</span></b>
					<img src="${pageContext.request.contextPath}/images/spinner.gif">
				</td>
			</tr>
			<tr>
				<td class=attr>
			<spring:message code="analysis.text.message"/>:
		</td>
		<td class=data>
		<spring:message code="analysis.message.jpivotResultsWait"/>
	</td>
</tr>
</table>


<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

</body>
</html>