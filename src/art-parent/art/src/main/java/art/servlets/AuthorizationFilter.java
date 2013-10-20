package art.servlets;

import art.user.User;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

/**
 * Filter to ensure user has access to the requested page
 *
 * @author Timothy Anyona
 */
public class AuthorizationFilter implements Filter {

	/**
	 *
	 */
	@Override
	public void destroy() {
	}

	/**
	 *
	 * @param filterConfig
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * Ensure user has access to the requested page
	 *
	 * @param srequest
	 * @param sresponse
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest srequest, ServletResponse sresponse,
			FilterChain chain) throws IOException, ServletException {

		if (srequest instanceof HttpServletRequest && sresponse instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest) srequest;
			HttpServletResponse response = (HttpServletResponse) sresponse;
			HttpSession session = request.getSession();

			User user = (User) session.getAttribute("sessionUser");
			if (user == null) {
				//user not authenticated or session expired
				if (srequest.getParameter("_public_user") != null) {
					//allow public user access
					String username = "public_user";

					user = new User();
					user.setUsername(username);
					user.setAccessLevel(0);

					session.setAttribute("sessionUser", user);
					session.setAttribute("username", username);
				} else {
					//forward to login page. 
					//use forward instead of redirect so that an indication of the
					//page that was being accessed remains in the browser
					//give session expired message, although it may just be unauthorized access attempt

					//remember the page the user tried to access in order to forward after the authentication
					//use relative path (without context path).
					//that's what redirect in login controller needs
					String nextPage=StringUtils.substringAfter(request.getRequestURI(),request.getContextPath());
					session.setAttribute("nextPage", nextPage);
					request.setAttribute("message", "login.message.sessionExpired");
					request.getRequestDispatcher("/login.do").forward(request, response);
					return;
				}
			}

			//if we are here, user is authenticated
			//ensure they have access to the specific page. if not show access denied page
			if (canAccessPage(request,user)) {
				chain.doFilter(srequest, sresponse);
			} else {
				//show access denied page. 
				//use forward instead of redirect so that the intended url remains in the browser
				request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
			}
		}
	}

	private boolean canAccessPage(HttpServletRequest request, User user) {
		boolean authorized = false;
		
		int accessLevel = user.getAccessLevel();
		String contextPath = request.getContextPath();
		String requestUri = request.getRequestURI();
		String path = contextPath + "/app/";

		//TODO use permissions instead of access level
		if (StringUtils.startsWith(requestUri, path + "admin.do")) {
			//only admins can access
			if (accessLevel >= 10) {
				authorized = true;
			}
		} else if (StringUtils.startsWith(requestUri, path + "reports.do")) {
			//everyone can access
			//NOTE: "everyone" excludes the special codes when accessing as
			//the initial setup user (-1) and the art repository user (-2)
			if (accessLevel >= 0) {
				authorized = true;
			}
		} else if (StringUtils.startsWith(requestUri, path + "jobs.do")) {
			//everyone
			if (accessLevel >= 0) {
				authorized = true;
			}
		}
		
		return authorized;
	}
}