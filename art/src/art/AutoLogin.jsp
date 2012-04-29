<%@ page import="art.servlets.ArtDBCP;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>


<%  
  java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
  
  //let application server authenticate user using login credentials      
  String username = request.getRemoteUser();
  
  String message  = (String) request.getAttribute("message");
  if (username !=null && username.length() > 0 && message == null) {     
     //user authenticated     
     session.setAttribute("username", username);
     
     String nextPage = (String)  session.getAttribute("nextPage");
     session.removeAttribute("nextPage");
         // redirect and art will verify if the user is setup as an art user,
     response.sendRedirect((nextPage!=null?nextPage:request.getContextPath()+"/user/showGroups.jsp"));
         // if not this AutoLogin page is invoked with a message and the code below is shown
}  else {
%>

<html>
    <head>
        <title>ART - Single Sign On</title>
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">        
    </head>
	<body>
User not authenticated or not an ART user. 
<br/>Username: [<%=username%>]
<br><br/><%=message%>

<p align="right">
            <span style="font-size:75%"><a href="${pageContext.request.contextPath}/login.jsp">Internal Login</a></span>
		</p>

    <%@ include file ="user/footer.jsp" %>
<%
}
%>
