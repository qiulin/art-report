<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
UserEntity ue=new UserEntity();
Iterator it;
%>

    <table align="center">
        <tr>
			<td class="title" colspan="2">Current User/User Group Privileges</td>
        </tr>
	<tr>            
            <td colspan="2" class="data2">                
				<%				
				Map map;
				map=ue.getQueryGroupAssignment();
				if(map.size()>0){
					%>
					<b>Query Groups</b><br>
					<%
					it = map.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<%=entry.getValue()%> <br>
						<%
					}
					%>
					<br>
					<%
				}
				%>    

				<%				
				map=ue.getQueryAssignment();
				if(map.size()>0){
					%>
					<b>Queries</b><br>
					<%
					it = map.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<%=entry.getValue()%> <br>
						<%
					} 
				}
				%>    
            </td>
        </tr>
				
    </table>    
    
<%@ include file ="/user/footer.jsp" %>