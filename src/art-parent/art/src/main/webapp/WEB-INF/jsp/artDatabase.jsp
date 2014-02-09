<%-- 
    Document   : artDatabase
    Created on : 08-Nov-2013, 09:28:05
    Author     : Timothy Anyona

Display art database configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<spring:message code="page.title.artDatabase" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="artDatabase.do"]').parent().addClass('active');
				});

				$(function() {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					iconBase: 'fa',
					tickIcon: 'fa-check-square'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});
			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="rightMainPanel">
		<c:if test="${not empty initialSetup}">
			<div class="col-md-3">
				<div class="alert alert-info">
					<p>
						Welcome to the ART Reporting Tool. You need to configure the
						<b>ART Database</b> before being able to use ART. The ART Database
						stores data used by the application e.g. users, report definitions etc.
					</p>
					<p>
						After saving the ART Database configuration, use the
						<b>Configure | Users</b> menu to create some users. Create at least
						one Super Admin user which you can use to administer all aspects
						of the application. After creating users, <b>Log Out</b> and log in 
						using one of the users, and continue using the application.
					</p>
					<p>
						A demo database is provided with a few sample reports.
						To use it, select <b>Demo</b> as the Database Type and Save,
						The demo database has 2 users with username/password of admin/admin
						and auser/auser.
					</p>
				</div>
			</div>
		</c:if>
	</jsp:attribute>

	<jsp:body>
		<form:form class="form-horizontal" method="POST" action="" modelAttribute="artDatabase">
			<fieldset>
				<c:if test="${formErrors != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="page.message.formErrors"/>
					</div>
				</c:if>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<p>${fn:escapeXml(error)}</p>
					</div>
				</c:if>

				<div class="form-group">
					<label class="control-label col-md-4" for="databaseType">
						<spring:message code="artDatabase.label.databaseType"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<select name="databaseType" id="databaseType" class="form-control selectpicker"
									onchange="setDatasourceFields(this.value, 'driver', 'url', 'connectionTestSql');">
								<option value="">
									<spring:message code="select.text.none"/>
								</option>
								<c:forEach var="dbType" items="${databaseTypes}">
									<option value="${dbType.key}">${dbType.value}</option>
								</c:forEach>
							</select>
							<spring:message code="artDatabase.help.databaseType" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="driver">
						<spring:message code="artDatabase.label.jdbcDriver"/>
					</label>
					<div class="col-md-8">
						<form:input path="driver" maxlength="100" class="form-control"/>
						<form:errors path="driver" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="url">
						<spring:message code="artDatabase.label.jdbcUrl"/>
					</label>
					<div class="col-md-8">
						<form:input path="url" maxlength="2000" class="form-control"/>
						<form:errors path="url" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="username">
						<spring:message code="page.label.username"/>
					</label>
					<div class="col-md-8">
						<form:input path="username" maxlength="30" class="form-control"/>
						<form:errors path="username" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="password">
						<spring:message code="page.label.password"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:password path="password" autocomplete="off" maxlength="50" class="form-control"/>
							<spring:message code="page.help.password" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="password" cssClass="error"/>
						<div class="checkbox">
							<label>
								<form:checkbox path="useBlankPassword"/>
								<spring:message code="page.checkbox.useBlankPassword"/>
							</label>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="connectionTestSql">
						<spring:message code="artDatabase.label.connectionTestSql"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="connectionTestSql" maxlength="100" class="form-control"/>
							<spring:message code="artDatabase.help.connectionTestSql" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="connectionTestSql" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="connectionPoolTimeout">
						<spring:message code="artDatabase.label.connectionPoolTimeout"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="connectionPoolTimeout" maxlength="5" class="form-control"/>
							<spring:message code="artDatabase.help.connectionPoolTimeout"
											var="help" />
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" data-html="true" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="connectionPoolTimeout" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="maxPoolConnections">
						<spring:message code="artDatabase.label.maxPoolConnections"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="maxPoolConnections" maxlength="3" class="form-control"/>
							<spring:message code="artDatabase.help.maxPoolConnections" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="maxPoolConnections" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.button.save"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>