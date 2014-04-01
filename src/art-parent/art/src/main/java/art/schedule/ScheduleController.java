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
package art.schedule;

import art.utils.AjaxResponse;
import java.sql.SQLException;
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
 * Controller for schedule configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ScheduleController {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

	@Autowired
	private ScheduleService scheduleService;

	@RequestMapping(value = "/app/schedules", method = RequestMethod.GET)
	public String showSchedules(Model model) {
		logger.debug("Entering showSchedules");

		try {
			model.addAttribute("schedules", scheduleService.getAllSchedules());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "schedules";
	}

	@RequestMapping(value = "/app/deleteSchedule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSchedule(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteSchedule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			scheduleService.deleteSchedule(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addSchedule", method = RequestMethod.GET)
	public String addSchedule(Model model) {
		logger.debug("Entering addSchedule");

		model.addAttribute("schedule", new Schedule());
		return showSchedule("add", model);
	}

	@RequestMapping(value = "/app/saveSchedule", method = RequestMethod.POST)
	public String saveSchedule(@ModelAttribute("schedule") @Valid Schedule schedule,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveSchedule: schedule={}, action='{}'", schedule, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showSchedule(action, model);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				scheduleService.addSchedule(schedule);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				scheduleService.updateSchedule(schedule);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", schedule.getName());
			return "redirect:/app/schedules.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showSchedule(action, model);
	}

	@RequestMapping(value = "/app/editSchedule", method = RequestMethod.GET)
	public String editSchedule(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editSchedule: id={}", id);

		try {
			model.addAttribute("schedule", scheduleService.getSchedule(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showSchedule("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showSchedule(String action, Model model) {
		logger.debug("Entering showSchedule: action='{}'", action);

		model.addAttribute("action", action);
		return "editSchedule";
	}

}
