/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.login;

import art.enums.ArtAuthenticationMethod;
import art.utils.ArtHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for logging success or failure of login attempts
 *
 * @author Timothy Anyona
 */
public class LoginHelper {

	private static final Logger logger = LoggerFactory.getLogger(LoginHelper.class);

	/**
	 * Logs login attempts
	 *
	 * @param loginMethod the login method
	 * @param result the login result
	 * @param username the username used
	 * @param ip the ip address from which login was done or attempted
	 */
	public void log(ArtAuthenticationMethod loginMethod, LoginResult result,
			String username, String ip) {

		log(loginMethod, result.isAuthenticated(), username, ip, result.getDetails());
	}

	/**
	 * Logs login attempts
	 *
	 * @param loginMethod the login method
	 * @param success whether the login attempt was successful
	 * @param username the username used
	 * @param ip the ip address from which the login was done or attempted
	 * @param failureMessage the login failure message
	 */
	private void log(ArtAuthenticationMethod loginMethod, boolean success,
			String username, String ip, String failureMessage) {

		String loginStatus;
		String logMessage;
		if (success) {
			loginStatus = "login";
			logMessage = loginMethod.getValue();
		} else {
			loginStatus = "loginerr";
			logMessage = loginMethod.getValue() + ", " + failureMessage;
		}

		ArtHelper.log(username, loginStatus, ip, logMessage);

		//also log to file
		logger.info("{}. username={}, message={}", loginStatus, username, logMessage);
	}

	/**
	 * Logs a successful login attempt
	 * 
	 * @param loginMethod the login method used
	 * @param username the username used
	 * @param ip the ip address from which the login was done
	 */
	public void logSuccess(ArtAuthenticationMethod loginMethod,
			String username, String ip) {

		log(loginMethod, true, username, ip, "");
	}

	/**
	 * Logs a failed login attempt
	 * 
	 * @param loginMethod the login method used
	 * @param username the username used
	 * @param ip the ip address from which the login was done
	 * @param message the message accompanying the failed login attempt
	 */
	public void logFailure(ArtAuthenticationMethod loginMethod,
			String username, String ip, String message) {

		log(loginMethod, false, username, ip, message);
	}
}
