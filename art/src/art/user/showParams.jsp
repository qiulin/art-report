<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP,art.params.*;" %>
<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<jsp:useBean id="aq" scope="request" class="art.utils.ArtQuery" />
<!--   query_name may be from query_id... -->
<jsp:setProperty name="aq" property="*" />

<%
aq.setUsername(ue.getUsername());
aq.create();
int queryType = aq.getQueryType();
int queryId=aq.getQueryId();
String queryName=aq.getName();
String showParameters=aq.getShowParameters();

String action;
if(queryType==110){
	action="showPortlets.jsp";
} else if(queryType==112 || queryType==113 || queryType==114){
	action="showAnalysis.jsp";
} else {
	action="ExecuteQuery";
}

int adminLevel=ue.getAdminLevel();
boolean hasParams=false;

%>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/date.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/art-checkParam_js.jsp"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/dhtmlgoodies_calendar/dhtmlgoodies_calendar_js.jsp"></script>

<div id="params">

    <fieldset>
        <legend><%=messages.getString("enterParams") %></legend>

        <form name="artparameters" id="paramForm" action="<%=action%>"  method="post">
			<input type="hidden" name="queryId" VALUE="<%=queryId%>">
            <table class="art" align="center">
                <tr>
                    <td colspan="4" class="title" >
                        <br> <b><%=queryName%></b> <br><br>
                    </td>
                </tr>

                <%
                java.util.Iterator it = aq.getParamList().iterator();
                StringBuffer validateJS = new StringBuffer(128);
                String paramClass;
                String paramName;
                String paramHtmlName;
                String paramChainedId;
                String paramId;
				String paramChainedValueId;
				
                while(it.hasNext()) {
                   hasParams=true;
                   ParamInterface param = (ParamInterface) it.next();
                   paramHtmlName=param.getHtmlName();
                   paramClass=param.getParamClass();
                   paramName=param.getName();
                   paramChainedId=param.getChainedId();
                   paramId=param.getId();
				   paramChainedValueId=param.getChainedValueId();

                   if ( paramClass.equals("INTEGER") || paramClass.equals("NUMBER") || paramClass.equals("DATE") || paramClass.equals("DATETIME") ){
                      validateJS.append("ValidateValue('"+paramClass+"', '"+paramName+"', document.getElementById('"+paramId+"').value ) && ");
					}
                %>
                <tr>
                    <td class="data">
                        <%=paramName%>
                    </td>
                    <td class="data">                        
                        <%
						out.println(param.getValueBox(request.getParameter(param.getHtmlName())));
						
						if (param.isChained()) {
                              String params = "";
                              if (paramHtmlName.startsWith("M_")) { // handle ALL_ITEMS in select
                                 params = "action=lov,queryId="+paramClass+",isMulti=yes,filter={"+paramChainedValueId+"}";
                              } else {
                                 params = "action=lov,queryId="+paramClass+",filter={"+paramChainedValueId+"}";
                              }
                              String dataProviderUrl = request.getContextPath()+"/XmlDataProvider";
                        %>
                        <ajax:select
							baseUrl="<%=dataProviderUrl%>"
							source="<%=paramChainedId%>"
							target="<%=paramId%>"
							parameters="<%=params%>"
							preFunction="artAddWork"
							postFunction="artRemoveWork"
							executeOnLoad="true"
							emptyOptionName="..."
							emptyOptionValue=""
                            />

                        <!-- js code to add a "dummy item" on master select -->
                        <script type="text/javascript">
                            addElementToSelectItem("<%=paramChainedId%>");
                        </script>

                        <% } %>

                        <% if (param.isChained()) { %>
                        <img src="<%= request.getContextPath() %>/images/chain.png" alt="chain"><br>
                        <small>
                            <script type="text/javascript"> document.write(document.getElementById("<%=paramChainedId+"_NAME"%>").value) </script>
                        </small>
                        <% } else if (paramClass.equals("INTEGER")) { %>
                        <img src="<%= request.getContextPath() %>/images/123.png" alt="integer">
                        <% } else if (paramClass.equals("NUMBER")) { %>
                        <img src="<%= request.getContextPath() %>/images/1dot23.png" alt="number">
                        <% } else if (paramClass.equals("DATE") || paramClass.equals("DATETIME")) { %>
                        <!-- image included in html box -->
                        <% } else { %>
                        <img src="<%= request.getContextPath() %>/images/abc.png" alt="text">
                        <% } %>
                    </td>
                    <td class="data">
                        <%=param.getShortDescr()%>
                    </td>
                    <td class="data">
                        <input type="button" class="buttonup" onClick="alert('<%=param.getDescr()%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="...">

                    </td>
                </tr>

                <% } %>
                <tr>
                    <td colspan="2" class="attr">
                        <%
                        if (queryType == 110 || queryType==112 || queryType==113 || queryType==114) {
							//dashboards and pivot tables don't have view modes
							if(queryType==110){ //dashboard
							%>
							<%=messages.getString("portletObject")%>
							<%} else { //pivot table
							%>
								<%=messages.getString("pivotTableObject")%>
								<%
							}
							%>

                    </td>
                    <td colspan="2" class="data">
                        <%
                        } else {
                           switch ( queryType ) {
                              case 0  :   // normal query
                              case 101:  // crosstab
                        %>    <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <SELECT name="viewMode" size="1">
                            <%
                            java.util.Iterator itVm = ArtDBCP.getUserViewModes().iterator();
                            while(itVm.hasNext()) {
                               String viewMode = (String) itVm.next();
                            %>
                            <OPTION VALUE="<%=viewMode%>"> <%=messages.getString(viewMode)%> </OPTION>
                            <% } %>

                            <% if (adminLevel>=5 && ArtDBCP.isSchedulingEnabled()) { %>
                            <OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
                            <% } %>

                        </SELECT>
                        <br>
                         <% if(hasParams){
                             if("N".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams"> <%=messages.getString("showParams")%>
                            <%} else if("Y".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams" checked> <%=messages.getString("showParams")%>
                            <%} else if("A".equals(showParameters)) { %>
                                <input type="hidden" name="_showParams" value="true"> 
                            <%}
                          }                        
                        %>

                        <%
                        break;
                        case 103: // normal html
                        case 102: // xtab html
                        %> <input type="hidden" name="viewMode" VALUE="html">
                        
                          <% if(hasParams){
                             if("N".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams"> <%=messages.getString("showParams")%>
                            <%} else if("Y".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams" checked> <%=messages.getString("showParams")%>
                            <%} else if("A".equals(showParameters)) { %>
                                <input type="hidden" name="_showParams" value="true"> 
                            <%}
                          }                        
                        %>

                        <%
						break;
						case 115: //jasper report
                        case 116:
                        %>
                        <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <SELECT name="viewMode" size="1">
                            <OPTION VALUE="pdf"><%=messages.getString("pdf")%></OPTION>
                            <OPTION VALUE="xls"><%=messages.getString("xls")%></OPTION>
                            <OPTION VALUE="xlsx"><%=messages.getString("xlsx")%></OPTION>
                            <OPTION VALUE="html"><%=messages.getString("htmlJasper")%></OPTION>
                            <% if (adminLevel>=5 && ArtDBCP.isSchedulingEnabled()) { %>
                            <OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
                            <% } %>

                        </SELECT>
                        <%
                        break;
					case 117: //jxls spreadsheet
					case 118:
					%>
					<span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
					<SELECT name="viewMode" size="1">			     		
					<OPTION VALUE="xls"><%=messages.getString("xls")%></OPTION>
					 <% if (ue.getAdminLevel() >=5 && ArtDBCP.isSchedulingEnabled()) { %>
					<OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
					 <% } %>
						 </SELECT>
						 <%
						 break;
					  default:
						if (queryType >0 && queryType <100 ) {
							%>
							<input type="hidden" name="viewMode" VALUE="HTMLREPORT">
							<input type="hidden" name="SPLITCOL" VALUE="<%=queryType%>">
							<%
							}
					   }//end switch


					   if (queryType== 101 || queryType == 102) { %>
                        <input type="hidden" name="_isCrosstab" VALUE="Y">
                        <% } else if (queryType<0) { %>
                        <small><i><%=messages.getString("graphType")%></i>
                            <%=messages.getString("graph"+queryType)%>                            
                            <br><i><%=messages.getString("graphSizeWH")%></i>
                            <INPUT TYPE="text" name="_GRAPH_SIZE" VALUE="Default" size="16" maxlength="60">
                            <input type="button" class="buttonup" value="..." onclick='javascript:alert(" <%=messages.getString("graphHelp1")%> <%=messages.getString("graphHelp2")%> <%=messages.getString("graphHelp3")%> <%=messages.getString("graphHelp4")%> ");' onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
                        </small>

                        <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <SELECT name="viewMode" size="1">
                            <OPTION VALUE="GRAPH"><%=messages.getString("htmlPlain")%></OPTION>
                            <OPTION VALUE="PDFGRAPH"><%=messages.getString("pdf")%></OPTION>
                            <OPTION VALUE="PNGGRAPH"><%=messages.getString("png")%></OPTION>
                            <% if (adminLevel>=5 && ArtDBCP.isSchedulingEnabled()) { %>
                            <OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
                            <%}%>
                        </SELECT>

                        <br /> <%=messages.getString("showGraphOptions")%>
                        <input type="checkbox" name="_showLegend" <%=(aq.isShowLegend()?"checked":"")%> /><%=messages.getString("legend")%>
                        <input type="checkbox" name="_showLabels" <%=(aq.isShowLabels()?"checked":"")%> /><%=messages.getString("labels")%>
                        <input type="checkbox" name="_showDataPoints" <%=(aq.isShowPoints()?"checked":"")%> /><%=messages.getString("dataPoints")%>
						<input type="checkbox" name="_showGraphData" <%=(aq.isShowGraphData()?"checked":"")%> /><%=messages.getString("graphData")%>

                        <br><br>
                         <% if(hasParams){
                             if("N".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams"> <%=messages.getString("showParams")%>
                            <%} else if("Y".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams" checked> <%=messages.getString("showParams")%>
                            <%} else if("A".equals(showParameters)) { %>
                                <input type="hidden" name="_showParams" value="true"> 
                            <%}
                          }                        
                         }
                        %>

                    </td>
                    <td colspan="2" class="attr">
                        <input type="hidden" name="QUERY_ID" VALUE="<%=queryId%>">
						<INPUT TYPE="hidden" name="QUERY_NAME" VALUE="<%=queryName%>">
						<INPUT TYPE="hidden" name="QUERY_TYPE" VALUE="<%=queryType%>">

                        <%}%>

                        <div align="center" valign="middle">
                            <input type="submit" onClick="javascript:return(<%= validateJS.toString()%> returnTrue() )" class="buttonup"  style="width:100px;" value="<%=messages.getString("executeQueryButton")%>">
                        </div>
                    </td>
                </tr>
            </table>
        </form>
    </fieldset>

</div>

<%@ include file ="footer.jsp" %>

