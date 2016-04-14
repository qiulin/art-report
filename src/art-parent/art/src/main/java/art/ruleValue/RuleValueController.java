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
package art.ruleValue;

import art.rule.RuleService;
import art.user.UserService;
import art.usergroup.UserGroupService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
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
 * Controller for rule value configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class RuleValueController {

	private static final Logger logger = LoggerFactory.getLogger(RuleValueController.class);

	@Autowired
	private RuleValueService ruleValueService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private RuleService ruleService;

	@RequestMapping(value = "/app/ruleValues", method = RequestMethod.GET)
	public String showRuleValues(Model model) {
		logger.debug("Entering showRuleValues");

		try {
			model.addAttribute("userRuleValues", ruleValueService.getAllUserRuleValues());
			model.addAttribute("userGroupRuleValues", ruleValueService.getAllUserGroupRuleValues());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "ruleValues";
	}

	@RequestMapping(value = "/app/ruleValuesConfig", method = RequestMethod.GET)
	public String showRuleValuesConfig(Model model, HttpSession session) {
		logger.debug("Entering showRuleValuesConfig");

		try {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("rules", ruleService.getAllRules());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "ruleValuesConfig";
	}

	@RequestMapping(value = "/app/deleteRuleValue", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRuleValue(@RequestParam("id") String id) {

		logger.debug("Entering deleteRuleValue: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <value type>~<rule value key>. rule value key contains a hyphen
		String[] values = StringUtils.split(id, "~");
		String valueType = values[0];
		String valueKey = values[1];

		try {
			if (StringUtils.equalsIgnoreCase(valueType, "userRuleValue")) {
				ruleValueService.deleteUserRuleValue(valueKey);
			} else if (StringUtils.equalsIgnoreCase(valueType, "userGroupRuleValue")) {
				ruleValueService.deleteUserGroupRuleValue(valueKey);
			}
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/deleteAllRuleValues", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteAllRuleValues(
			@RequestParam(value = "users[]", required = false) String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "ruleId") Integer ruleId) {

		logger.debug("Entering deleteAllRuleValues: ruleId={}", ruleId);

		AjaxResponse response = new AjaxResponse();

		try {
			ruleValueService.deleteAllRuleValues(users, userGroups, ruleId);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/updateRuleValue", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateRuleValue(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "rule") String rule,
			@RequestParam(value = "ruleValue") String ruleValue) {

		logger.debug("Entering updateRuleValue: action='{}', rule='{}', ruleValue='{}'",
				action, rule, ruleValue);

		AjaxResponse response = new AjaxResponse();

		try {
			if (StringUtils.equalsIgnoreCase(action, "add")) {
				ruleValueService.addRuleValue(users, userGroups, rule, ruleValue);
			} else {
				Integer ruleId = Integer.valueOf(StringUtils.substringBefore(rule, "-"));
				ruleValueService.deleteAllRuleValues(users, userGroups, ruleId);
			}
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/editUserRuleValue", method = RequestMethod.GET)
	public String editUserRuleValue(@RequestParam("id") String id, Model model) {
		logger.debug("Entering editUserRuleValue: id='{}'", id);

		try {
			model.addAttribute("value", ruleValueService.getUserRuleValue(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserRuleValue();
	}

	@RequestMapping(value = "/app/saveUserRuleValue", method = RequestMethod.POST)
	public String saveUserRuleValue(@ModelAttribute("value") @Valid UserRuleValue value,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveUserRuleValue: value={},", value);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserRuleValue();
		}

		try {
			ruleValueService.updateUserRuleValue(value);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = value.getUser().getUsername() + " - "
					+ value.getRule().getName() + " - " + value.getRuleValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/app/ruleValues.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserRuleValue();
	}

	@RequestMapping(value = "/app/editUserGroupRuleValue", method = RequestMethod.GET)
	public String editUserGroupRuleValue(@RequestParam("id") String id, Model model) {
		logger.debug("Entering editUserGroupRuleValue: id='{}'", id);

		try {
			model.addAttribute("value", ruleValueService.getUserGroupRuleValue(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupRuleValue();
	}

	@RequestMapping(value = "/app/saveUserGroupRuleValue", method = RequestMethod.POST)
	public String saveUserGroupRuleValue(@ModelAttribute("value") @Valid UserGroupRuleValue value,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveUserGroupRuleValue: value={},", value);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserGroupRuleValue();
		}

		try {
			ruleValueService.updateUserGroupRuleValue(value);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = value.getUserGroup().getName() + " - "
					+ value.getRule().getName() + " - " + value.getRuleValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/app/ruleValues.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupRuleValue();
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @return
	 */
	private String showEditUserRuleValue() {
		logger.debug("Entering showEditUserRuleValue");

		return "editUserRuleValue";
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @return
	 */
	private String showEditUserGroupRuleValue() {
		logger.debug("Entering showEditUserGroupRuleValue");

		return "editUserGroupRuleValue";
	}

}