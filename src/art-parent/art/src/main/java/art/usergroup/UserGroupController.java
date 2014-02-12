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
package art.usergroup;

import art.reportgroup.ReportGroupService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.validation.Valid;
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
 * Controller for user group configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class UserGroupController {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupController.class);

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/app/userGroups", method = RequestMethod.GET)
	public String showUserGroups(Model model) {
		try {
			model.addAttribute("groups", userGroupService.getAllUserGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "userGroups";
	}

	@RequestMapping(value = "/app/deleteUserGroup", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserGroup(@RequestParam("id") Integer id) {
		AjaxResponse response = new AjaxResponse();

		try {
			userGroupService.deleteUserGroup(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addUserGroup", method = RequestMethod.GET)
	public String showAddUserGroup(Model model) {
		model.addAttribute("group", new UserGroup());
		return displayUserGroup("add", model);
	}

	@RequestMapping(value = "/app/addUserGroup", method = RequestMethod.POST)
	public String processAddUserGroup(@ModelAttribute("group") @Valid UserGroup group,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return displayUserGroup("add", model);
		}

		try {
			userGroupService.addUserGroup(group);
			redirectAttributes.addFlashAttribute("message", "userGroups.message.userGroupAdded");
			return "redirect:/app/userGroups.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return displayUserGroup("add", model);
	}

	@RequestMapping(value = "/app/editUserGroup", method = RequestMethod.GET)
	public String showEditUserGroup(@RequestParam("id") Integer id, Model model) {

		try {
			model.addAttribute("group", userGroupService.getUserGroup(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return displayUserGroup("edit", model);
	}

	@RequestMapping(value = "/app/editUserGroup", method = RequestMethod.POST)
	public String processEditUserGroup(@ModelAttribute("group") @Valid UserGroup group,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return displayUserGroup("edit", model);
		}

		try {
			userGroupService.updateUserGroup(group);
			redirectAttributes.addFlashAttribute("message", "userGroups.message.userGroupUpdated");
			return "redirect:/app/userGroups.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return displayUserGroup("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String displayUserGroup(String action, Model model) {
		try {
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editUserGroup";
	}

}
