/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.reportgroup;

import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.List;
import javax.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for report group configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportGroupController {

	private static final Logger logger = LoggerFactory.getLogger(ReportGroupController.class);

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/app/reportGroups", method = RequestMethod.GET)
	public String showReportGroups(Model model) {
		logger.debug("Entering showReportGroups");

		try {
			model.addAttribute("groups", reportGroupService.getAllReportGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportGroups";
	}

	@RequestMapping(value = "/app/deleteReportGroup", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportGroup(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReportGroup: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			List<String> linkedReports = reportGroupService.getLinkedReports(id);
			if (linkedReports.isEmpty()) {
				//no linked reports. go ahead and delete report group
				reportGroupService.deleteReportGroup(id);
				response.setSuccess(true);
			} else {
				response.setData(linkedReports);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addReportGroup", method = RequestMethod.GET)
	public String addReportGroup(Model model) {
		logger.debug("Entering addReportGroup");

		model.addAttribute("group", new ReportGroup());
		return showReportGroup("add", model);
	}

	@RequestMapping(value = "/app/saveReportGroup", method = RequestMethod.POST)
	public String addReportGroup(@ModelAttribute("group") @Valid ReportGroup group,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveReportGroup: group={}, action='{}'", group, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReportGroup(action, model);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				reportGroupService.addReportGroup(group);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportGroupService.updateReportGroup(group);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", group.getName());
			return "redirect:/app/reportGroups.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReportGroup(action, model);
	}

	@RequestMapping(value = "/app/editReportGroup", method = RequestMethod.GET)
	public String editReportGroup(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editReportGroup: id={}", id);

		try {
			model.addAttribute("group", reportGroupService.getReportGroup(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReportGroup("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showReportGroup(String action, Model model) {
		logger.debug("Entering showReportGroup: action='{}'", action);

		model.addAttribute("action", action);
		return "editReportGroup";
	}

}
