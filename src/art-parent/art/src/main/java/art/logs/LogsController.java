/**
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
package art.logs;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class LogsController {

	final static Logger logger = LoggerFactory.getLogger(LogsController.class);

	@RequestMapping(value = "/app/logs", method = RequestMethod.GET)
	public String showLogs(Model model) {
		final String CYCLIC_BUFFER_APPENDER_NAME = "CYCLIC"; //name of cyclic appender in logback.xml
		
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) context.getLogger(
				Logger.ROOT_LOGGER_NAME).getAppender(CYCLIC_BUFFER_APPENDER_NAME);

		int count = -1;
		if (cyclicBufferAppender != null) {
			count = cyclicBufferAppender.getLength();
		}

		if (count > 0 && cyclicBufferAppender != null) {
			List<LoggingEvent> logs = new ArrayList<LoggingEvent>(100);
			for (int i = 0; i < count; i++) {
				logs.add((LoggingEvent) cyclicBufferAppender.get(i));
			}
			
			LoggingEvent le;
//			le.get

			model.addAttribute("logs", logs);
		}

		return "logs";
	}

//	private void reacquireCBA() {
//		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//		cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) context.getLogger(
//				Logger.ROOT_LOGGER_NAME).getAppender(CYCLIC_BUFFER_APPENDER_NAME);
//	}
	@PostConstruct
	public void init() {
		logger.info("test logs init");
	}

}
