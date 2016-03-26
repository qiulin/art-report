/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.analysis;

import art.datasource.Datasource;
import art.enums.ReportStatus;
import art.enums.ReportType;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.user.User;
import art.utils.Encrypter;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.OlapModelDecorator;
import com.tonbeller.jpivot.olap.query.MdxOlapModel;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.jpivot.tags.OlapModelProxy;
import com.tonbeller.wcf.form.FormComponent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class AnalysisController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AnalysisController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/app/showAnalysis", method = {RequestMethod.GET, RequestMethod.POST})
	public String showDashboard(HttpServletRequest request, Model model,
			HttpSession session) {

		try {
			int reportId = 0;
			if (request.getParameter("reportId") == null) {
				//not passed when using olap navigator
				Integer i = (Integer) session.getAttribute("pivotReportId");
				if (i != null) {
					reportId = i;
				}
			} else {
				//save to session in case olap navigator is used
				reportId = Integer.parseInt(request.getParameter("reportId"));
				session.setAttribute("pivotReportId", reportId);
			}

			Report report = reportService.getReport(reportId);

			String errorPage = "reportError";

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			User sessionUser = (User) session.getAttribute("sessionUser");

			if (!sessionUser.isAdminUser()) {
				if (report.getReportStatus() == ReportStatus.Disabled) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}
			}

			String template = report.getTemplate();
			File templateFile = new File(Config.getTemplatesPath() + template);
			if (report.getReportType() == ReportType.Mondrian && !templateFile.exists()) {
				model.addAttribute("message", "reports.message.templateFileNotFound");
				return errorPage;
			}

			prepareVariables(request, session, report, model);

		} catch (SQLException | NumberFormatException | ParseException | MalformedURLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "showAnalysis";

	}

	private void prepareVariables(HttpServletRequest request, HttpSession session,
			Report report, Model model) throws NumberFormatException, SQLException, ParseException, MalformedURLException {

		int queryId = report.getReportId();
		model.addAttribute("reportId", queryId);

		String reportName = report.getName();
		model.addAttribute("reportName", reportName);

		String jpivotQueryId = "query" + queryId;
		model.addAttribute("jpivotQueryId", jpivotQueryId);

		if (request.getParameter("action") == null && request.getParameter("null") == null) {
			//first time we are displaying the pivot table.
			//parameter ?null=null&... used to display the page when settings are changed from the olap navigator toolbar button

			String template = report.getTemplate();
			String schemaFile = Config.getRelativeTemplatesPath() + template;
			model.addAttribute("schemaFile", schemaFile);

			ReportType reportType = report.getReportType();
			model.addAttribute("reportType", reportType);

			//put title in session. may be lost if olap navigator option on jpivot toolbar is used
			String title = report.getName();
			session.setAttribute("pivotTitle" + queryId, title);

			if (reportType == ReportType.Mondrian) {
				Datasource ds = report.getDatasource();

				String databaseUrl = ds.getUrl().trim();
				String databaseUser = ds.getUsername().trim();
				String databasePassword = ds.getPassword();
				String databaseDriver = ds.getDriver().trim();

				model.addAttribute("databaseUrl", databaseUrl);
				model.addAttribute("databaseUser", databaseUser);
				model.addAttribute("databasePassword", databasePassword);
				model.addAttribute("databaseDriver", databaseDriver);
			} else {
				//construct xmla url to incoporate username and password if present
				String xmlaUrl = report.getXmlaUrl();
				String xmlaUsername = report.getXmlaUsername();
				String xmlaPassword = report.getXmlaPassword();
				xmlaPassword = Encrypter.decrypt(xmlaPassword);
				URL url;

				url = new URL(xmlaUrl);
				if (StringUtils.length(xmlaUsername) > 0) {
					xmlaUrl = url.getProtocol() + "://" + xmlaUsername;
					if (StringUtils.length(xmlaPassword) > 0) {
						xmlaUrl += ":" + xmlaPassword;
					}
					int port = url.getPort();
					if (port == -1) {
						//no port specified
						xmlaUrl += "@" + url.getHost() + url.getPath();
					} else {
						xmlaUrl += "@" + url.getHost() + ":" + port + url.getPath();
					}
				}
				model.addAttribute("xmlaUrl", xmlaUrl);

				String xmlaDatasource = null;
				if (reportType == ReportType.MondrianXmla) {
					//prepend provider if only datasource name provided
					xmlaDatasource = report.getXmlaDatasource();
					if (!StringUtils.startsWithIgnoreCase(xmlaDatasource, "provider=mondrian")) {
						xmlaDatasource = "Provider=Mondrian;DataSource=" + xmlaDatasource; //datasource name in datasources.xml file must be exactly the same
					}
				} else if (reportType == ReportType.SqlServerXmla) {
					xmlaDatasource = "Provider=MSOLAP";

				}
				model.addAttribute("xmlaDatasource", xmlaDatasource);

				String xmlaCatalog = report.getXmlaCatalog();
				model.addAttribute("xmlaCatalog", xmlaCatalog);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			String username = sessionUser.getUsername();
			boolean adminSession = sessionUser.isAdminUser();

			ReportRunner reportRunner = new ReportRunner();
			reportRunner.setUsername(username);
			reportRunner.setReport(report);
			reportRunner.setAdminSession(adminSession);

			//prepare report parameters
			ParameterProcessor paramProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();

			reportRunner.setReportParamsMap(reportParamsMap);

			reportRunner.execute();

			String query = reportRunner.getQuerySql();
			model.addAttribute("query", query);
		}

		//get title from session
		String title = (String) session.getAttribute("pivotTitle" + queryId);
		model.addAttribute("title", title);

//get exclusive access status from the session
//	Boolean accessBoolean = (Boolean) session.getAttribute("pivotExclusiveAccess" + queryId);
//	exclusiveAccess = accessBoolean.booleanValue();
//set identifiers for jpivot objects
		String tableId = "table" + queryId;
		String mdxEditId = "mdxedit" + queryId;
		String printId = "print" + queryId;
		String printFormId = "printform" + queryId;
		String navigatorId = "navi" + queryId;
		String sortFormId = "sortform" + queryId;
		String chartId = "chart" + queryId;
		String chartFormId = "chartform" + queryId;
		String toolbarId = "toolbar" + queryId;

		String queryDrillThroughTable = jpivotQueryId + ".drillthroughtable";

		String modelQueryId = "#{" + jpivotQueryId + "}";
		String modelTableId = "#{" + tableId + "}";
		String modelPrintId = "#{" + printId + "}";
		String modelChartId = "#{" + chartId + "}";

		String mdxEditVisible = "#{" + mdxEditId + ".visible}";
		String navigatorVisible = "#{" + navigatorId + ".visible}";
		String sortFormVisible = "#{" + sortFormId + ".visible}";
		String tableLevelStyle = "#{" + tableId + ".extensions.axisStyle.levelStyle}";
		String tableHideSpans = "#{" + tableId + ".extensions.axisStyle.hideSpans}";
		String tableShowProperties = "#{" + tableId + ".rowAxisBuilder.axisConfig.propertyConfig.showProperties}";
		String tableNonEmptyButtonPressed = "#{" + tableId + ".extensions.nonEmpty.buttonPressed}";
		String tableSwapAxesButtonPressed = "#{" + tableId + ".extensions.swapAxes.buttonPressed}";
		String tableDrillMemberEnabled = "#{" + tableId + ".extensions.drillMember.enabled}";
		String tableDrillPositionEnabled = "#{" + tableId + ".extensions.drillPosition.enabled}";
		String tableDrillReplaceEnabled = "#{" + tableId + ".extensions.drillReplace.enabled}";
		String tableDrillThroughEnabled = "#{" + tableId + ".extensions.drillThrough.enabled}";
		String chartVisible = "#{" + chartId + ".visible}";
		String chartFormVisible = "#{" + chartFormId + ".visible}";
		String printFormVisible = "#{" + printFormId + ".visible}";

		String printExcel = request.getContextPath() + "/Print?cube=" + queryId + "&type=0";
		String printPdf = request.getContextPath() + "/Print?cube=" + queryId + "&type=1";

		model.addAttribute("tableId", tableId);
		model.addAttribute("mdxEditId", mdxEditId);
		model.addAttribute("printId", printId);
		model.addAttribute("printFormId", printFormId);
		model.addAttribute("navigatorId", navigatorId);
		model.addAttribute("sortFormId", sortFormId);
		model.addAttribute("chartId", chartId);
		model.addAttribute("chartFormId", chartFormId);
		model.addAttribute("toolbarId", toolbarId);
		model.addAttribute("queryDrillThroughTable", queryDrillThroughTable);
		model.addAttribute("modelQueryId", modelQueryId);
		model.addAttribute("modelTableId", modelTableId);
		model.addAttribute("modelPrintId", modelPrintId);
		model.addAttribute("modelChartId", modelChartId);
		model.addAttribute("mdxEditVisible", mdxEditVisible);
		model.addAttribute("navigatorVisible", navigatorVisible);
		model.addAttribute("sortFormVisible", sortFormVisible);
		model.addAttribute("tableLevelStyle", tableLevelStyle);
		model.addAttribute("tableHideSpans", tableHideSpans);
		model.addAttribute("tableShowProperties", tableShowProperties);
		model.addAttribute("tableNonEmptyButtonPressed", tableNonEmptyButtonPressed);
		model.addAttribute("tableSwapAxesButtonPressed", tableSwapAxesButtonPressed);
		model.addAttribute("tableDrillMemberEnabled", tableDrillMemberEnabled);
		model.addAttribute("tableDrillPositionEnabled", tableDrillPositionEnabled);
		model.addAttribute("tableDrillReplaceEnabled", tableDrillReplaceEnabled);
		model.addAttribute("tableDrillThroughEnabled", tableDrillThroughEnabled);
		model.addAttribute("chartVisible", chartVisible);
		model.addAttribute("chartFormVisible", chartFormVisible);
		model.addAttribute("printFormVisible", printFormVisible);
		model.addAttribute("printExcel", printExcel);
		model.addAttribute("printPdf", printPdf);

//get the current mdx
		String currentMdx = "";
		TableComponent table = (TableComponent) session.getAttribute(tableId);
		if (table != null) {
			OlapModel olapModel = table.getOlapModel();
			while (olapModel != null) {
				if (olapModel instanceof OlapModelProxy) {
					OlapModelProxy proxy = (OlapModelProxy) olapModel;
					olapModel = proxy.getDelegate();
				}
				if (olapModel instanceof OlapModelDecorator) {
					OlapModelDecorator decorator = (OlapModelDecorator) olapModel;
					olapModel = decorator.getDelegate();
				}
				if (olapModel instanceof MdxOlapModel) {
					MdxOlapModel mdxOlapModel = (MdxOlapModel) olapModel;
					currentMdx = mdxOlapModel.getCurrentMdx();
					olapModel = null;
				}
			}
		}

//save current mdx in the session
		session.setAttribute("mdx" + queryId, currentMdx);

//get object with olap query and result
		OlapModel _olapModel = (OlapModel) session.getAttribute(jpivotQueryId);

		String overflowResult = null;
		if (_olapModel != null) {
			try {
				_olapModel.getResult();
				if (_olapModel.getResult().isOverflowOccured()) {
					overflowResult = "Resultset overflow occurred";
				}
			} catch (Throwable t) {
				logger.error("Error", t);
				overflowResult = "Error Occurred While getting Resultset";
			}
		}
		model.addAttribute("overflowResult", overflowResult);

		FormComponent _mdxEdit = (FormComponent) session.getAttribute(mdxEditId);
		if (_mdxEdit != null && _mdxEdit.isVisible()) {
			model.addAttribute("mdxEditIsVisible", true);
		}
	}

	@RequestMapping(value = "/app/jpivotError", method = {RequestMethod.GET, RequestMethod.POST})
	public String jpivotError(HttpServletRequest request, Locale locale,
			Model model) {
		String msg = messageSource.getMessage("analysis.text.jpivotError", null, locale);

		Throwable e = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
		while (e != null) {
			msg = msg + e.toString() + "<br><br>";

			Throwable prev = e;
			e = e.getCause();
			if (e == prev) {
				break;
			}
		}

		model.addAttribute("msg", msg);

		return "jpivotError";
	}

	@RequestMapping(value = "/app/jpivotBusy", method = {RequestMethod.GET, RequestMethod.POST})
	public String jpivotBusy() {
		return "jpivotBusy";
	}
}