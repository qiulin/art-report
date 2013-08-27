<%@ page import="art.servlets.ArtConfig" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%
	ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());

	if (!ArtConfig.isArtSettingsLoaded()) {
		// settings not defined: 1st Logon -> go to adminConsole.jsp (passing through the AuthFilterAdmin)
		response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/admin/adminConsole.jsp"));
		return;
	} else {
		String toPage = ArtConfig.getArtSetting("index_page_default");
		if (!StringUtils.equals(toPage,"default")) {
			toPage = toPage + ".jsp";
%>
<jsp:forward page="<%=toPage%>"/>
<%
			return;
		}
	}

%>

<!DOCTYPE html>
<html>
    <head>
        <title>ART - Login</title>
		<link rel="stylesheet" href="css/art.css">
        <script type="text/javascript">
            function Start(page) {
                OpenWin = this.open(page, "CtrlWindow", "toolbar=yes,menubar=no,location=no,statusbar=no,scrollbars=yes,resizable=yes,width=800,height=600");
            }
        </script>
    </head>
    <body>
        <hr style="width:100%; height:2px">

        <div>
            <br>
            <table class="centerTable" style="width:400px"  class="art">
                <tr>
                    <td class="supertitle" align="center">
                        <br>
						<img src="<%= request.getContextPath()%>/images/art-64px.jpg"
							 alt="ART" height="70">
                        <br><br>
						<span style="font-size:180%">
							<b>ART</b>
						</span>
						<br><br>
                    </td>
                </tr>
                <tr>
                    <td class="link">
						<a href="login.jsp"> <%=messages.getString("login")%></a>
					</td>
                </tr>
                <tr>
                    <td class="link">
						<a href="mobile/index.jsp"> <%=messages.getString("login")%>
							<small><i>(mobile devices, micro-browsers)</i></small>
						</a>
					</td>
                </tr>
                <tr>
                    <td class="link">
						<a href="http://art.sourceforge.net">Web site</a>
					</td>
                </tr>
                <tr>
                    <td class="action">  &nbsp;	</td>
                </tr>
            </table>
        </div>

        <div>
            <br>
            <hr style="width:100%;height:2px">
        </div>
    </body>
</html>
