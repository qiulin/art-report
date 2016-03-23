/*
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report;

import art.datasource.DatasourceService;
import art.enums.AccessLevel;
import art.enums.ReportStatus;
import art.enums.ReportType;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.reportgroup.ReportGroupService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOptions;
import art.servlets.Config;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import art.utils.Encrypter;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for reports pages
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportController {

	private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportGroupService reportGroupService;

	@Autowired
	private DatasourceService datasourceService;

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session,
			@RequestParam(value = "reportId", required = false) Integer reportGroupId,
			HttpServletRequest request, Model model) {

		logger.debug("Entering showReports: reportGroupId={}", reportGroupId);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			List<Report> reports = reportService.getAvailableReports(sessionUser.getUserId());

			//allow to focus public_user in one report only. is this feature used? it's not documented
			if (reportGroupId != null) {
				List<Report> filteredReports = new ArrayList<>();
				for (Report report : reports) {
					if (report.getReportGroup().getReportGroupId() == reportGroupId) {
						filteredReports.add(report);
					}
				}
				model.addAttribute("reports", filteredReports);
			} else {
				model.addAttribute("reports", reports);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}

	@RequestMapping(value = "/app/selectReportParameters", method = RequestMethod.GET)
	public String selectReportParameters(HttpSession session,
			@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model) {

		logger.debug("Entering selectReportParameters: reportId={}", reportId);

		try {
			Report report = reportService.getReport(reportId);
			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return "reportError";
			} else {
				model.addAttribute("report", report);

				//prepare report parameters
				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
				List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
				ReportOptions reportOptions = paramProcessorResult.getReportOptions();
				ChartOptions chartOptions = paramProcessorResult.getChartOptions();

				model.addAttribute("reportParamsList", reportParamsList);
				model.addAttribute("reportOptions", reportOptions);
				model.addAttribute("chartOptions", chartOptions);

//				ParameterService parameterService = new ParameterService();
//				List<Parameter> paramsList = parameterService.getReportParameters(reportId);
//				model.addAttribute("paramsList", paramsList);
				ReportType reportType = report.getReportType();

				boolean enableReportFormats = false;
				switch (reportType) {
					case Dashboard:
					case Mondrian:
					case MondrianXmla:
					case SqlServerXmla:
						break;
					default:
						enableReportFormats = true;
						List<String> reportFormats = getAvailableReportFormats(report.getReportType());
						model.addAttribute("reportFormats", reportFormats);
				}
				model.addAttribute("enableReportFormats", enableReportFormats);

				User sessionUser = (User) session.getAttribute("sessionUser");
				int accessLevel = sessionUser.getAccessLevel().getValue();

				boolean enableSchedule;
				if (accessLevel >= AccessLevel.ScheduleUser.getValue()
						&& Config.getSettings().isSchedulingEnabled()) {
					if (reportType == ReportType.Dashboard || reportType.isOlap()) {
						enableSchedule = false;
					} else {
						enableSchedule = true;
					}
				} else {
					enableSchedule = false;
				}
				model.addAttribute("enableSchedule", enableSchedule);

				boolean enableShowSql = false;
				boolean enableShowSelectedParameters = false;

				switch (reportType) {
					case Dashboard:
					case Mondrian:
					case MondrianXmla:
					case SqlServerXmla:
					case JasperReportsTemplate:
					case JxlsTemplate:
						break;
					default:
						if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
							enableShowSql = true;
						}
						if (!reportParamsList.isEmpty()) {
							enableShowSelectedParameters = true;
						}
				}
				model.addAttribute("enableShowSql", enableShowSql);
				model.addAttribute("enableShowSelectedParameters", enableShowSelectedParameters);

				model.addAttribute("isChart", reportType.isChart());

				boolean enableRunInline;
				if (reportType == ReportType.Dashboard || reportType.isOlap()) {
					enableRunInline = false;
				} else {
					enableRunInline = true;
				}
				model.addAttribute("enableRunInline", enableRunInline);
			}
		} catch (SQLException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		return "selectReportParameters";
	}

	private List<String> getAvailableReportFormats(ReportType reportType) {
		List<String> formats = new ArrayList<>();

		if (reportType.isChart()) {
			formats.add("html");
			formats.add("pdf");
			formats.add("png");
		} else {
			switch (reportType) {
				case Tabular:
				case Crosstab:
					String formatsString = Config.getSettings().getReportFormats();
					String[] formatsArray = StringUtils.split(formatsString, ",");
					formats = Arrays.asList(formatsArray);
					break;
				case JasperReportsArt:
				case JasperReportsTemplate:
					formats.add("pdf");
					formats.add("xls");
					formats.add("xlsx");
					formats.add("html");
					break;
				case JxlsArt:
				case JxlsTemplate:
					formats.add("xls");
					break;
				default:
					throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}
		}

		return formats;
	}

	/**
	 * Return available reports using ajax
	 *
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/getReports", method = RequestMethod.GET)
	public @ResponseBody
	List<Report> getReports(HttpSession session, HttpServletRequest request) {
		//object will be automatically converted to json because of @ResponseBody and presence of jackson libraries
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/
		User sessionUser = (User) session.getAttribute("sessionUser");

		List<Report> reports = null;
		try {
			reports = reportService.getAvailableReports(sessionUser.getUserId());
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return reports;
	}

	@RequestMapping(value = "/app/reportsConfig", method = RequestMethod.GET)
	public String showReportsConfig(Model model) {
		logger.debug("Entering showReportsConfig");

		model.addAttribute("activeStatus", ReportStatus.Active.getValue());
		model.addAttribute("disabledStatus", ReportStatus.Disabled.getValue());

		try {
			model.addAttribute("reports", reportService.getAllReports());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportsConfig";
	}

	@RequestMapping(value = "/app/deleteReport", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReport(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReport: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = reportService.deleteReport(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//report not deleted because of linked jobs
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.GET)
	public String addReport(Model model, HttpSession session) {
		logger.debug("Entering addReport");

		model.addAttribute("report", new Report());
		return showReport("add", model, session);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.GET)
	public String editReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("edit", model, session);
	}

	@RequestMapping(value = "/app/saveReport", method = RequestMethod.POST)
	public String saveReport(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, @RequestParam("action") String action,
			@RequestParam("templateFile") MultipartFile templateFile,
			@RequestParam("subreportFile") MultipartFile subreportFile) {

		logger.debug("Entering saveReport: report={}, action='{}'", report, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport(action, model, session);
		}

		try {
			//finalise report properties
			String prepareReportMessage = prepareReport(report, templateFile, subreportFile, action);
			logger.debug("prepareReportMessage='{}'", prepareReportMessage);
			if (prepareReportMessage != null) {
				model.addAttribute("message", prepareReportMessage);
				return showReport(action, model, session);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (StringUtils.equals(action, "add")) {
				reportService.addReport(report, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "copy")) {
				reportService.copyReport(report, report.getReportId(), sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportService.updateReport(report, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", report.getName());
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport(action, model, session);
	}

	@RequestMapping(value = "/app/copyReport", method = RequestMethod.GET)
	public String copyReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("copy", model, session);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showReport(String action, Model model, HttpSession session) {
		logger.debug("Entering showReport: action='{}'", action);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			model.addAttribute("reportGroups", reportGroupService.getAdminReportGroups(sessionUser));
			model.addAttribute("reportStatuses", ReportStatus.list());
			model.addAttribute("reportTypes", ReportType.list());

			model.addAttribute("datasources", datasourceService.getAdminDatasources(sessionUser));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editReport";
	}

	/**
	 * Save file
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file) throws IOException {
		return saveFile(file, null);
	}

	/**
	 * Save file and update report template property with the file name
	 *
	 * @param file
	 * @param report
	 * @return
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file, Report report) throws IOException {
		logger.debug("Entering saveFile: report={}", report);

		logger.debug("file.isEmpty()={}", file.isEmpty());
		if (file.isEmpty()) {
			return null;
		}

		//check upload file type
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("xml");
		validExtensions.add("jrxml");
		validExtensions.add("xls");
		validExtensions.add("xlsx");

		long maxUploadSize = Config.getSettings().getMaxFileUploadSize(); //size in MB
		maxUploadSize = maxUploadSize * 1000L * 1000L; //size in bytes

		//save template file
		long uploadSize = file.getSize();
		String filename = file.getOriginalFilename();
		logger.debug("filename='{}'", filename);
		String extension = FilenameUtils.getExtension(filename).toLowerCase();

		logger.debug("maxUploadSize={}, uploadSize={}", maxUploadSize, uploadSize);
		if (maxUploadSize >= 0 && uploadSize > maxUploadSize) { //0 effectively means no uploads allowed
			return "reports.message.fileBiggerThanMax";
		}

		if (!validExtensions.contains(extension)) {
			return "reports.message.invalidFileType";
		}

		//save file
		String destinationFilename = Config.getTemplatesPath() + filename;
		File destinationFile = new File(destinationFilename);
		file.transferTo(destinationFile);

		if (report != null) {
			report.setTemplate(filename);
		}

		return null;
	}

	/**
	 * Set xmla password and chart options setting properties
	 *
	 * @param report
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setProperties(Report report, String action) throws SQLException {
		logger.debug("Entering setProperties: report={}, action='{}'", report, action);

		String setXmlaPasswordMessage = setXmlaPassword(report, action);
		logger.debug("setXmlaPasswordMessage='{}'", setXmlaPasswordMessage);
		if (setXmlaPasswordMessage != null) {
			return setXmlaPasswordMessage;
		}

		//set report source for text reports
		logger.debug("report.getReportType()={}", report.getReportTypeId());
		ReportType reportType = ReportType.toEnum(report.getReportTypeId());
		if (reportType == ReportType.Text) {
			report.setReportSource(report.getReportSourceHtml());
		}

		//build chart options setting string
		logger.debug("(report.getChartOptions() != null) = {}", report.getChartOptions() != null);
		if (report.getReportTypeId() < 0 && report.getChartOptions() != null) {
			String size = report.getChartOptions().getWidth() + "x" + report.getChartOptions().getHeight();
			String yRange = report.getChartOptions().getyAxisMin() + ":" + report.getChartOptions().getyAxisMax();

			logger.debug("size='{}'", size);
			logger.debug("yRange='{}'", yRange);

			String showLegend = "";
			String showLabels = "";
			String showPoints = "";
			String showData = "";

			logger.debug("report.getChartOptions().isShowLegend() = {}", report.getChartOptions().isShowLegend());
			if (report.getChartOptions().isShowLegend()) {
				showLegend = "showLegend";
			}
			logger.debug("report.getChartOptions().isShowLabels() = {}", report.getChartOptions().isShowLabels());
			if (report.getChartOptions().isShowLabels()) {
				showLabels = "showLabels";
			}
			logger.debug("report.getChartOptions().isShowPoints() = {}", report.getChartOptions().isShowPoints());
			if (report.getChartOptions().isShowPoints()) {
				showPoints = "showPoints";
			}
			logger.debug("report.getChartOptions().isShowData() = {}", report.getChartOptions().isShowData());
			if (report.getChartOptions().isShowData()) {
				showData = "showData";
			}

			String rotateAt = "rotateAt:" + report.getChartOptions().getRotateAt();
			String removeAt = "removeAt:" + report.getChartOptions().getRemoveAt();

			logger.debug("rotateAt='{}'", rotateAt);
			logger.debug("removeAt='{}'", removeAt);

			Object[] options = {
				size,
				yRange,
				report.getChartOptions().getBackgroundColor(),
				showLegend,
				showLabels,
				showPoints,
				showData,
				rotateAt,
				removeAt
			};

			logger.debug("options='{}'", StringUtils.join(options, " "));
			report.setChartOptionsSetting(StringUtils.join(options, " "));
		}

		return null;
	}

	/**
	 * Set xmla password
	 *
	 * @param report
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setXmlaPassword(Report report, String action) throws SQLException {
		logger.debug("Entering setXmlapassword: report={}, action='{}'", report, action);

		boolean useCurrentXmlaPassword = false;
		String newXmlaPassword = report.getXmlaPassword();

		logger.debug("report.isUseBlankXmlaPassword()={}", report.isUseBlankXmlaPassword());
		if (report.isUseBlankXmlaPassword()) {
			newXmlaPassword = "";
		} else {
			logger.debug("StringUtils.isEmpty(newXmlaPassword)={}", StringUtils.isEmpty(newXmlaPassword));
			if (StringUtils.isEmpty(newXmlaPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentXmlaPassword = true;
			}
		}

		logger.debug("useCurrentXmlaPassword={}", useCurrentXmlaPassword);
		if (useCurrentXmlaPassword) {
			//password field blank. use current password
			Report currentReport = reportService.getReport(report.getReportId());
			logger.debug("currentReport={}", currentReport);
			if (currentReport == null) {
				return "page.message.cannotUseCurrentXmlaPassword";
			} else {
				report.setXmlaPassword(currentReport.getXmlaPassword());
			}
		} else {
			logger.debug("StringUtils.isNotEmpty(newXmlaPassword)={}", StringUtils.isNotEmpty(newXmlaPassword));
			if (StringUtils.isNotEmpty(newXmlaPassword)) {
				newXmlaPassword = "o:" + Encrypter.encrypt(newXmlaPassword);
			}
			report.setXmlaPassword(newXmlaPassword);
		}

		return null;
	}

	/**
	 * Finalise report properties
	 *
	 * @param report
	 * @param templateFile
	 * @param subreportFile
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws IOException
	 * @throws SQLException
	 */
	private String prepareReport(Report report, MultipartFile templateFile,
			MultipartFile subreportFile, String action) throws IOException, SQLException {

		logger.debug("Entering prepareReport: report={}, action='{}", report, action);

		String message;

		message = saveFile(templateFile, report); //update report template property
		if (message != null) {
			return message;
		}

		message = saveFile(subreportFile);
		if (message != null) {
			return message;
		}

		message = setProperties(report, action);
		if (message != null) {
			return message;
		}

		return null;
	}

}
