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
package art.password;

import art.user.User;
import art.user.UserService;
import art.utils.Encrypter;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for the change password process
 *
 * @author Timothy Anyona
 */
@Controller
public class PasswordController {

	final static Logger logger = LoggerFactory.getLogger(PasswordController.class);

	@RequestMapping(value = "/app/password", method = RequestMethod.GET)
	public String showPassword() {
		return "password";
	}

	@RequestMapping(value = "/app/password", method = RequestMethod.POST)
	public String processPassword(HttpSession session,
			@RequestParam("newPassword1") String newPassword1,
			@RequestParam("newPassword2") String newPassword2,
			Model model, RedirectAttributes redirectAttributes) {

		try {
			if (!StringUtils.equals(newPassword1, newPassword2)) {
				model.addAttribute("errorMessage", "password.message.passwordsDontMatch");
			} else {
				//change password
				String passwordHash = Encrypter.HashPasswordBcrypt(newPassword1);
				UserService userService = new UserService();
				User user = (User) session.getAttribute("sessionUser");
				userService.updatePassword(user.getUserId(), passwordHash);

				redirectAttributes.addFlashAttribute("success", "true");
				return "redirect:/app/password.do";
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		
		return "password";
	}

}
