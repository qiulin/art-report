<%-- 
    Document   : header
    Created on : 17-Sep-2013, 11:45:05
    Author     : Timothy Anyona

Header that appears at the top of all pages, except the login and logs pages
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="header">

	<!-- Fixed navbar -->
	<div class="navbar navbar-default navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="${pageContext.request.contextPath}/app/reports.do">
					ART
				</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li>
						<a href="${pageContext.request.contextPath}/app/reports.do">
							<i class="fa fa-bar-chart-o"></i>
							<spring:message code="header.link.reports"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/jobs.do">
							<i class="fa fa-clock-o"></i> 
							<spring:message code="header.link.jobs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/archives.do">
							<i class="fa fa-archive"></i> 
							<spring:message code="header.link.archives"/>
						</a>
					</li>
					<c:if test="${sessionUser.accessLevel.value >= 10 || sessionUser.accessLevel.value < 0}">
						<li class="dropdown">
							<a id="configure" href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-delay="100">
								<i class="fa fa-wrench"></i> 
								<spring:message code="header.link.configure"/>
								<b class="caret"></b>
							</a>
							<ul class="dropdown-menu">
								<c:if test="${sessionUser.accessLevel.value >= 100 || sessionUser.accessLevel.value < 0}">
									<li>
										<a href="${pageContext.request.contextPath}/app/artDatabase.do">
											<spring:message code="header.link.artDatabase"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 100}">
									<li>
										<a href="${pageContext.request.contextPath}/app/settings.do">
											<spring:message code="header.link.settings"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/app/datasources.do">
											<spring:message code="header.link.datasources"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 10}">
									<li>
										<a href="${pageContext.request.contextPath}/app/reportsConfig.do">
											<spring:message code="header.link.reportsConfiguration"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/app/reportGroups.do">
											<spring:message code="header.link.reportGroups"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40 || sessionUser.accessLevel.value < 0}">
									<li>
										<a href="${pageContext.request.contextPath}/app/users.do">
											<spring:message code="header.link.users"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/app/userGroups.do">
											<spring:message code="header.link.userGroups"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/app/userGroupMembershipConfig.do">
											<spring:message code="header.link.userGroupMembership"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 30}">
									<li>
										<a href="${pageContext.request.contextPath}/app/accessRightsConfig.do">
											<spring:message code="header.link.accessRights"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/app/adminRightsConfig.do">
											<spring:message code="header.link.adminRights"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 10}">
									<li>
										<a href="${pageContext.request.contextPath}/app/parameters.do">
											<spring:message code="header.link.parameters"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/app/rules.do">
											<spring:message code="header.link.rules"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/app/ruleValuesConfig.do">
											<spring:message code="header.link.ruleValues"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/app/jobsConfig.do">
											<spring:message code="header.link.jobsConfiguration"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/app/schedules.do">
											<spring:message code="header.link.schedules"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/app/ftpServers.do">
											<spring:message code="header.link.ftpServers"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li class="divider"></li>
									<li>
										<a href="${pageContext.request.contextPath}/app/caches.do">
											<spring:message code="header.link.caches"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/app/connections.do">
											<spring:message code="header.link.connections"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/app/loggers.do">
											<spring:message code="header.link.loggers"/>
										</a>
									</li>
								</c:if>
							</ul>
						</li>
					</c:if>
					<c:if test="${sessionUser.accessLevel.value >= 80}">
						<li>
							<a href="${pageContext.request.contextPath}/app/logs.do">
								<i class="fa fa-bars"></i> 
								<spring:message code="header.link.logs"/>
							</a>
						</li>
					</c:if>
					<c:if test="${sessionUser.accessLevel.value >= 10}">
						<li>
							<a href="${pageContext.request.contextPath}/docs">
								<i class="fa fa-book"></i> 
								<spring:message code="header.link.documentation"/>
							</a>
						</li>
					</c:if>
					<c:if test="${authenticationMethod eq internalAuthentication && sessionUser.canChangePassword}">
						<li>
							<a href="${pageContext.request.contextPath}/app/password.do">
								<i class="fa fa-lock"></i> 
								<spring:message code="header.link.password"/>
							</a>
						</li>
					</c:if>
					<li>
						<a href="${pageContext.request.contextPath}/app/language.do">
							<i class="fa fa-comment"></i> 
							<spring:message code="header.link.language"/>
						</a>
					</li>
					<li>
						<form method="POST" action="${pageContext.request.contextPath}/logout.do">
							<button type="submit" class="btn btn-link navbar-btn">
								<i class="fa fa-sign-out"></i> 
								<spring:message code="header.link.logout"/>
							</button>
						</form>
					</li>
				</ul>
				<div class="nav navbar-nav navbar-right navbar-text">
					<i class="fa fa-user"></i> ${sessionUser.username} 
				</div>
			</div><!--/.nav-collapse -->
		</div>
	</div>
</div>