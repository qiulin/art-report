<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");

QueryGroup group=new QueryGroup();

group.setGroupId(Integer.parseInt(request.getParameter("GROUP_ID")));
group.setName(request.getParameter("GROUP_NAME").trim());
group.setDescription(request.getParameter("GROUP_DESCRIPTION").trim());

if (action.equals("ADD")){	
	group.insert();
} else if (action.equals("MODIFY")){
	group.update();
}

response.sendRedirect("manageQueryGroups.jsp");
%>


