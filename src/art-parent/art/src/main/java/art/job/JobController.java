package art.job;

import art.enums.JobType;
import art.jobrunners.ReportJob;
import art.report.ReportService;
import art.runreport.ParameterProcessor;
import art.schedule.Schedule;
import art.schedule.ScheduleService;
import art.servlets.Config;
import art.user.User;
import art.utils.AjaxResponse;
import art.utils.ArtJob;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
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
 * Controller for jobs page and jobs configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class JobController {

	private static final Logger logger = LoggerFactory.getLogger(JobController.class);

	@Autowired
	private JobService jobService;

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ScheduleService scheduleService;

	@RequestMapping(value = "/app/jobs", method = RequestMethod.GET)
	public String showJobs(Model model, HttpSession session) {
		logger.debug("Entering showJobs");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			model.addAttribute("jobs", jobService.getJobs(sessionUser.getUserId()));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "jobs";
	}

	@RequestMapping(value = "/app/jobsConfig", method = RequestMethod.GET)
	public String showJobsConfig(Model model) {
		logger.debug("Entering showJobsConfig");

		try {
			model.addAttribute("jobs", jobService.getAllJobs());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "config");
		return "jobs";
	}

	@RequestMapping(value = "/app/deleteJob", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteJob(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteJob: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			jobService.deleteJob(id);
			response.setSuccess(true);
		} catch (SQLException | SchedulerException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addJob", method = RequestMethod.GET)
	public String addJob(Model model) {
		logger.debug("Entering addJob");

		model.addAttribute("job", new Job());
		return showJob("add", model);
	}

	@RequestMapping(value = "/app/saveJob", method = RequestMethod.POST)
	public String saveJob(@ModelAttribute("job") @Valid Job job,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveJob: job={}, action='{}'", job, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showJob(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			finalizeSchedule(job);

			if (StringUtils.equals(action, "add")) {
				jobService.addJob(job, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				jobService.updateJob(job, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", job.getName());
			return "redirect:/app/jobsConfig.do";
		} catch (SQLException | SchedulerException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showJob(action, model);
	}

	@RequestMapping(value = "/app/editJob", method = RequestMethod.GET)
	public String editJob(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editJob: id={}", id);

		try {
			model.addAttribute("job", jobService.getJob(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showJob("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showJob(String action, Model model) {
		logger.debug("Entering showJob: action='{}'", action);

		model.addAttribute("action", action);

		model.addAttribute("jobTypes", JobType.list());

		try {
			model.addAttribute("dynamicRecipientReports", reportService.getDynamicRecipientReports());
			model.addAttribute("schedules", scheduleService.getAllSchedules());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		return "editJob";
	}

	private void finalizeSchedule(Job job) throws SchedulerException, ParseException {
		//create quartz job to be running this job

		//build cron expression for the schedule
		String minute;
		String hour;
		String day;
		String weekday;
		String month;
		String second = "0"; //seconds always 0
		String actualHour; //allow hour and minute to be left blank, in which case random values are used
		String actualMinute; //allow hour and minute to be left blank, in which case random values are used

		actualMinute = job.getScheduleMinute();
		actualMinute = StringUtils.deleteWhitespace(actualMinute); // cron fields shouldn't have any spaces in them
		minute = actualMinute;

		actualHour = job.getScheduleHour();
		actualHour = StringUtils.deleteWhitespace(actualHour);
		hour = actualHour;

		//enable definition of random start time
		if (StringUtils.contains(actualHour, "|")) {
			String startPart = StringUtils.substringBefore(actualHour, "|");
			String endPart = StringUtils.substringAfter(actualHour, "|");
			String startHour = StringUtils.substringBefore(startPart, ":");
			String startMinute = StringUtils.substringAfter(startPart, ":");
			String endHour = StringUtils.substringBefore(endPart, ":");
			String endMinute = StringUtils.substringAfter(endPart, ":");

			if (StringUtils.isBlank(startMinute)) {
				startMinute = "0";
			}
			if (StringUtils.isBlank(endMinute)) {
				endMinute = "0";
			}

			Date now = new Date();

			java.util.Calendar calStart = java.util.Calendar.getInstance();
			calStart.setTime(now);
			calStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
			calStart.set(Calendar.MINUTE, Integer.parseInt(startMinute));

			Calendar calEnd = Calendar.getInstance();
			calEnd.setTime(now);
			calEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
			calEnd.set(Calendar.MINUTE, Integer.parseInt(endMinute));

			long randomDate = ArtUtils.getRandomNumber(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
			Calendar calRandom = Calendar.getInstance();
			calRandom.setTimeInMillis(randomDate);

			hour = String.valueOf(calRandom.get(Calendar.HOUR_OF_DAY));
			minute = String.valueOf(calRandom.get(Calendar.MINUTE));
		}

		if (minute.length() == 0) {
			//no minute defined. use random value
			minute = String.valueOf(ArtUtils.getRandomNumber(0, 59));
		}

		if (hour.length() == 0) {
			//no hour defined. use random value
			hour = String.valueOf(ArtUtils.getRandomNumber(3, 6));
		}

		month = StringUtils.deleteWhitespace(job.getScheduleMonth());
		if (month.length() == 0) {
			//no month defined. default to every month
			month = "*";
		}

		day = StringUtils.deleteWhitespace(job.getScheduleDay());
		weekday = StringUtils.deleteWhitespace(job.getScheduleWeekday());

		//set default day of the month if weekday is defined
		if (day.length() == 0 && weekday.length() >= 1 && !weekday.equals("?")) {
			//weekday defined but day of the month is not. default day to ?
			day = "?";
		}

		if (day.length() == 0) {
			//no day of month defined. default to *
			day = "*";
		}

		if (weekday.length() == 0) {
			//no day of week defined. default to undefined
			weekday = "?";
		}

		if (day.equals("?") && weekday.equals("?")) {
			//unsupported. only one can be ?
			day = "*";
			weekday = "?";
		}
		if (day.equals("*") && weekday.equals("*")) {
			//unsupported. only one can be defined
			day = "*";
			weekday = "?";
		}

		//build cron expression.
		//cron format is sec min hr dayofmonth month dayofweek (optionally year)
		String cronString;
		cronString = second + " " + minute + " " + hour + " " + day + " " + month + " " + weekday;

		//determine if start date and end date are valid dates
		String startDateString = job.getStartDateString();
		if (StringUtils.isBlank(startDateString)) {
			startDateString = "now";
		}
		ParameterProcessor parameterProcessor = new ParameterProcessor();
		Date startDate = parameterProcessor.convertParameterStringValueToDate(startDateString);
		job.setStartDate(startDate);

		String endDateString = job.getEndDateString();
		Date endDate;
		if (StringUtils.isBlank(endDateString)) {
			endDate = null;
		} else {
			endDate = parameterProcessor.convertParameterStringValueToDate(endDateString);
		}
		job.setEndDate(endDate);

		CronTrigger tempTrigger = newTrigger()
				.withSchedule(cronSchedule(cronString))
				.startAt(startDate)
				.endAt(endDate)
				.build();

		Date nextRunDate = tempTrigger.getFireTimeAfter(new Date());

		job.setNextRunDate(nextRunDate);

		//save job details to the art database. generates job id for new jobs
		job.setScheduleMinute(minute);
		job.setScheduleHour(hour);
		job.setScheduleDay(day);
		job.setScheduleMonth(month);
		job.setScheduleWeekday(weekday);

		job.setStartDate(startDate);
		job.setEndDate(endDate);

		//create quartz job
		//get scheduler instance
		Scheduler scheduler = SchedulerUtils.getScheduler();

		if (scheduler != null) {
			int jobId = job.getJobId();

			String jobName = "job" + jobId;
			String triggerName = "trigger" + jobId;

			JobDetail quartzJob = newJob(ReportJob.class)
					.withIdentity(jobKey(jobName, ArtUtils.JOB_GROUP))
					.usingJobData("jobId", jobId)
					.build();

			//create trigger that defines the schedule for the job
			CronTrigger trigger = newTrigger()
					.withIdentity(triggerKey(triggerName, ArtUtils.TRIGGER_GROUP))
					.withSchedule(cronSchedule(cronString))
					.startAt(startDate)
					.endAt(endDate)
					.build();

			//delete any existing jobs or triggers with the same id before adding them to the scheduler
			scheduler.deleteJob(jobKey(jobName, ArtUtils.JOB_GROUP));
			scheduler.unscheduleJob(triggerKey(triggerName, ArtUtils.TRIGGER_GROUP));

			//add job and trigger to scheduler
			scheduler.scheduleJob(quartzJob, trigger);
		}

	}

}
