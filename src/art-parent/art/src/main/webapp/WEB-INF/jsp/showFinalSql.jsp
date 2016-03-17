<%-- 
    Document   : showFinalSql
    Created on : 08-Jan-2015, 12:46:13
    Author     : Timothy Anyona

Display the final sql used to generate a report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div align="center">
	<table border="0" width="90%">
		<tr>
			<td>
				<div align="center" width="90%" class="greyBack">
					<encode:forHtmlContent value="${finalSql}"/>
				</div>
			</td>
		</tr>
	</table>
</div>

