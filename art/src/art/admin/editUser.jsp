<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String username=request.getParameter("USERNAME");
int sessionAdminLevel = ((Integer)session.getAttribute("AdminLevel")).intValue();

UserEntity ue=new UserEntity();

if (action.equals("DELETE")){
	//check if lookup rules exist that reference this user
	Map rules=ue.getLinkedLookupRules(username);
	if(rules.size()>0){
		out.println("<pre>Error: There are lookup rules that reference the user you want to delete.");
		out.println("       Remove the following lookup rule references");
		out.println("       in order to be able to delete this user: ");
		out.println();
		out.println("</pre>");

		Iterator it=rules.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry)it.next();
			Rule rule=(Rule)entry.getValue();
			out.println("The user <b>" + rule.getUsername() + "</b> references user <i>" + username + "</i> for the rule <b>" + rule.getRuleName() + "</b>");
		}
		%>
		<%@ include file="footer.html" %>
		<%
		return;
	} else {
		//no linked lookup rules. delete user
		ue.delete(username);
		response.sendRedirect("manageUsers.jsp");
		return;
	}
}

boolean modify=false;
if (action.equals("MODIFY")){
	modify=true;
	ue.load(username);
}

UserGroup ug=new UserGroup();
Iterator it;
int adminLevel=ue.getAdminLevel();
username=ue.getUsername();
%>


<form name="editUser" method="post" action="execEditUser.jsp">
	<input type="hidden" name="ACTION" value="<%=action%>">

	<table align="center">
		<tr>
			<td class="title" colspan="2">Manage Users</td>
		</tr>
		<tr>
			<td class="data" colspan="2"><b>User Definition</b></td>
		</tr>

	    <tr>
			<td class="data"> Username </td>
			<%
			String edit="";
			if(modify){
				edit="readonly";
			}
			%>
			<td class="data"> <input type="text" name="USERNAME" value="<%=username%>" size="25" maxlength="30" <%=edit%> </td>
		</tr>

		<tr><td class="data"> Password </td>
			<td class="data"> <input type="password" name="PASSWORD" value="" size="25" maxlength="40"> </td>
		</tr>

		<tr><td class="data"> Status</td>
			<td class="data">
				<select name="STATUS">
					<%
					String status=ue.getActiveStatus();
					%>
					<option value="A" <%=("A".equals(status)?"selected":"")%>>Active</option>
					<option value="D" <%=("D".equals(status)?"selected":"")%>>Disabled</option>
				</select>
			</td>
		</tr>

		<tr><td class="data"> Full Name </td>
			<td class="data"> <input type="text" name="FULL_NAME" value="<%=ue.getFullName()%>" size="40" maxlength="40"> </td>
		</tr>

		<tr><td class="data"> Email </td>
			<td class="data"> <input type="text" name="EMAIL" value="<%=ue.getEmail()%>" size="40" maxlength="40"> </td>
		</tr>

		<tr><td class="data"> Admin Level</td>
			<td class="data">
				<select name="ADMIN_LEVEL" size="7">
					<option value="0" <%=(adminLevel==0?"selected":"")%>>Normal User</option>
					<option value="5" <%=(adminLevel==5?"selected":"")%>>Normal User allowed to schedule jobs</option>
					<%if(sessionAdminLevel>=30){%><option value="10" <%=(adminLevel==10?"selected":"")%>>Junior Admin (Query only)</option><%}%>
					<%if(sessionAdminLevel>=30){%><option value="30" <%=(adminLevel==30?"selected":"")%>>Mid Admin (+ User privileges)</option><%}%>
					<%if(sessionAdminLevel>=40){%><option value="40" <%=(adminLevel==40?"selected":"")%>>Admin (+ User setup)</option><%}%>
					<%if(sessionAdminLevel>=80){%><option value="80" <%=(adminLevel==80?"selected":"")%>>Senior Admin (+ Databases, Groups, Rules, Cache etc.)</option><%}%>
					<%if(sessionAdminLevel>=100){%><option value="100" <%=(adminLevel==100?"selected":"")%>>Super Admin  (manage everything)</option><%}%>
				</select>
			</td>
		</tr>

		<tr>
            <td class="data"> Default Object Group </td>
            <td class="data">
                <select name="DEFAULT_OBJECT_GROUP">
					<option value="-1">No Default</option>
                    <%
					int defaultObjectGroup=ue.getDefaultObjectGroup();

					Integer objectGroupId;
					String selected;

					ObjectGroup og=new ObjectGroup();
					Map objectGroups=og.getAllObjectGroupNames();
					it = objectGroups.entrySet().iterator();

					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						objectGroupId=(Integer)entry.getValue();
						if(objectGroupId==defaultObjectGroup){
							selected="selected";
						} else {
							selected="";
						}

						if(objectGroupId!=0){
							%>
							<option value="<%=entry.getValue()%>" <%=selected%> ><%=entry.getKey()%></option>
							<%
						}
					}
					%>
                </select>
            </td>
        </tr>

		<tr><td class="data"> Can Change Password</td>
			<td class="data">
				<select name="CAN_CHANGE_PASSWORD">
					<%
					String canChange=ue.getCanChangePasswordString();
					%>
					<option value="Y" <%=("Y".equals(canChange)?"selected":"")%>>Yes</option>
					<option value="N" <%=("N".equals(canChange)?"selected":"")%>>No</option>
				</select>
			</td>
		</tr>

		<tr><td class="data" colspan="2">  <br>User Group Membership </td></tr>
		<tr>
            <td class="data"> Select groups to add<br> or remove membership </td>
            <td class="data">
                <select name="USER_GROUPS" size="5" multiple>
                    <%
					Map userGroups=ug.getAllUserGroupNames();
					it = userGroups.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<option value="<%=entry.getValue()%>" ><%=entry.getKey()%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		<tr><td colspan="2" class="data"> Action:
			<select name="GROUPS_ACTION">
				<option value="ADD">ADD TO GROUPS AND SAVE</option>
				<option value="REMOVE">REMOVE FROM GROUPS AND SAVE</option>
			</select>
			</td>
		</tr>

		<tr><td colspan="2" class="data"> Current Memberships </td></tr>
		<tr>
            <td colspan="2" class="data2">
				<%
				Map map=ug.getUserGroupMemberships(username);
				it = map.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry entry = (Map.Entry)it.next();
					%>
					<%=entry.getValue()%> <br>
					<%
				}
				%>
            </td>
        </tr>

		<tr><td colspan="2">&nbsp;</td></tr>

		<%
		if(sessionAdminLevel>adminLevel || sessionAdminLevel==100){
			%>
			<tr>
				<td class="data" colspan="2"> <input type="submit" value="Submit"> </td>
			</tr>
		<%
		} else {
			%>
			<tr><td class="data" colspan="2"> You do not have the right to update this administrator </td></tr>
		<%
		}
		%>
    </table>

	<%
	if(modify){
		%>
		<p align="center">Leave the password field blank to maintain the previous password</p>
	<%
	}
	%>

</form>
    
    <p>&nbsp;</p>
    <div class="notes">
        <b>Notes:</b>
<table class="notes">
    <tr>
        <td>
            <ul>
                <li>If you use external authentication, you can leave the password field blank. </li>
                <li> If you want an object (query, dashboard etc) to be executable by anyone without authentication, you can define
					a user with the special username <b>public_user</b> and give this user access to the object.
					You can then use a "direct" URL to execute the object.
                </li>
                <li>An administrator can create/update only administrators as powerful as himself. </li>
            </ul>
        </td>
    </tr>
</table>
    </div>


<%@ include file="footer.html" %>