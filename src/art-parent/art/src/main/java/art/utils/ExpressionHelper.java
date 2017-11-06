/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.enums.DateFieldType;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes strings that may contain parameter, field or groovy expressions
 *
 * @author Timothy Anyona
 */
public class ExpressionHelper {

	private static final Logger logger = LoggerFactory.getLogger(ExpressionHelper.class);

	public static final String GROOVY_START_STRING = "g{";
	private final String GROOVY_END_STRING = "}g";

	/**
	 * Processes a string that may have parameter or field expressions and
	 * returns the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map containing report parameters
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processString(String string, Map<String, ReportParameter> reportParamsMap,
			String username) throws ParseException {

		String finalString = string;
		finalString = processParameters(finalString, reportParamsMap);
		finalString = processFields(finalString, username);
		finalString = processGroovy(finalString, reportParamsMap);
		return finalString;
	}

	/**
	 * Processes a string that may have parameter expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map containing report parameters
	 * @return the processed value with these items replaced
	 */
	public String processParameters(String string, Map<String, ReportParameter> reportParamsMap) {
		if (MapUtils.isEmpty(reportParamsMap)) {
			return string;
		}

		String finalString = string;

		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			String paramName = entry.getKey();
			ReportParameter reportParam = entry.getValue();

			List<Object> actualParameterValues = reportParam.getActualParameterValues();

			String replaceString;
			if (CollectionUtils.isEmpty(actualParameterValues)) {
				replaceString = "";
			} else {
				List<String> paramValues = new ArrayList<>();
				for (Object value : actualParameterValues) {
					String paramValue;
					if (value instanceof Date) {
						Date dateValue = (Date) value;
						paramValue = ArtUtils.isoDateTimeMillisecondsFormatter.format(dateValue);
					} else {
						paramValue = String.valueOf(value);
					}
					paramValues.add(paramValue);
				}

				replaceString = StringUtils.join(paramValues, ",");
			}

			String paramIdentifier = "#" + paramName + "#";
			finalString = StringUtils.replaceIgnoreCase(finalString, paramIdentifier, replaceString);
		}

		return finalString;
	}

	/**
	 * Processes a string that may have field expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processFields(String string, String username) throws ParseException {
		String finalString = string;
		finalString = processUsername(finalString, username);
		finalString = processDates(finalString);
		return finalString;
	}

	/**
	 * Processes a string that may have username field expressions and returns
	 * the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 */
	public String processUsername(String string, String username) {
		String replaceString;
		if (StringUtils.isBlank(username)) {
			replaceString = "";
		} else {
			replaceString = username;
		}

		String finalString = StringUtils.replace(string, "{username}", replaceString);
		return finalString;
	}

	/**
	 * Processes a string that may have date field expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processDates(String string) throws ParseException {
		String finalString = string;
		//process datetime field before date field
		finalString = processDate(finalString, DateFieldType.DateTime);
		finalString = processDate(finalString, DateFieldType.Date);
		return finalString;
	}

	/**
	 * Processes a string that may have date field expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param dateFieldType the type of date field to process
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	private String processDate(String string, DateFieldType dateFieldType) throws ParseException {
		String finalString = string;

		String dateFieldStartString;
		String dateFieldEndString = "}";
		switch (dateFieldType) {
			case Date:
				dateFieldStartString = "{date";
				break;
			case DateTime:
				dateFieldStartString = "{datetime";
				break;
			default:
				throw new IllegalArgumentException("Unexpected date field type: " + dateFieldType);
		}

		String[] dateFields = StringUtils.substringsBetween(string, dateFieldStartString, dateFieldEndString);
		if (dateFields != null) {
			Map<String, String> dateFieldValues = new HashMap<>();
			for (String dateField : dateFields) {
				String dateValue = processDateFieldContents(dateField, dateFieldType);
				String dateSpecification = dateFieldStartString + dateField + dateFieldEndString;
				dateFieldValues.put(dateSpecification, dateValue);
			}

			for (Entry<String, String> entry : dateFieldValues.entrySet()) {
				String searchString = entry.getKey();
				String replaceString = entry.getValue();
				finalString = StringUtils.replace(finalString, searchString, replaceString);
			}
		}

		return finalString;
	}

	/**
	 * Processes the contents of a date field and returns the processed value
	 * which will be used to replace the date field definition
	 *
	 * @param dateField the contents of the date field definition
	 * @param dateFieldType the type of date field being processed
	 * @return returns the processed value
	 * @throws ParseException
	 */
	private String processDateFieldContents(String dateField, DateFieldType dateFieldType) throws ParseException {
		String result = dateField;

		if (StringUtils.isBlank(result)) {
			switch (dateFieldType) {
				case Date:
					result = ArtUtils.isoDateFormatter.format(new Date());
					break;
				case DateTime:
					result = ArtUtils.isoDateTimeMillisecondsFormatter.format(new Date());
					break;
				default:
					throw new IllegalArgumentException("Unexpected date field type: " + dateFieldType);
			}
		} else {
			String separator = result.substring(0, 1);
			String expression = result.substring(1);
			String[] components = StringUtils.split(expression, separator);
			String dateString = components[0].trim();
			String outputFormat;
			switch (dateFieldType) {
				case Date:
					outputFormat = ArtUtils.ISO_DATE_FORMAT;
					break;
				case DateTime:
					outputFormat = ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT;
					break;
				default:
					throw new IllegalArgumentException("Unexpected date field type: " + dateFieldType);
			}
			if (components.length > 1) {
				outputFormat = components[1];
			}
			String localeString = null;
			if (components.length > 2) {
				localeString = components[2].trim();
			}
			Locale outputLocale = ArtUtils.getLocaleFromString(localeString);

			Date dateValue = convertStringToDate(dateString);
			SimpleDateFormat dateFormatter = new SimpleDateFormat(outputFormat, outputLocale);
			result = dateFormatter.format(dateValue);
		}

		return result;
	}

	/**
	 * Converts a string representation of a date to a date object
	 *
	 * @param string the string
	 * @return the date object of the string representation
	 * @throws ParseException
	 */
	public Date convertStringToDate(String string) throws ParseException {
		String dateFormat = null;
		Locale locale = Locale.ENGLISH;
		return convertStringToDate(string, dateFormat, locale);
	}

	/**
	 * Converts a string representation of a date to a date object
	 *
	 * @param string the string
	 * @param dateFormat the date format that the string is in
	 * @param locale the locale to use for the date
	 * @return the date object of the string representation
	 * @throws ParseException
	 */
	public Date convertStringToDate(String string, String dateFormat,
			Locale locale) throws ParseException {

		logger.debug("Entering convertStringToDate: string='{}',"
				+ " dateFormat='{}', locale={}", string, dateFormat, locale);

		Date dateValue;
		Date now = new Date();

		if (string == null || StringUtils.equalsIgnoreCase(string, "now")
				|| StringUtils.isBlank(string)) {
			dateValue = now;
		} else if (StringUtils.startsWithIgnoreCase(string, "today")) {
			dateValue = ArtUtils.zeroTime(now);
		} else if (StringUtils.startsWithIgnoreCase(string, "add")) {
			//e.g. add days 1
			String[] tokens = StringUtils.split(string);
			if (tokens.length != 3) {
				throw new IllegalArgumentException("Invalid interval: " + string);
			}

			String period = tokens[1];
			int offset = Integer.parseInt(tokens[2]);

			if (StringUtils.startsWithIgnoreCase(period, "day")) {
				dateValue = DateUtils.addDays(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "week")) {
				dateValue = DateUtils.addWeeks(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "month")) {
				dateValue = DateUtils.addMonths(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "year")) {
				dateValue = DateUtils.addYears(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "hour")) {
				dateValue = DateUtils.addHours(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "min")) {
				dateValue = DateUtils.addMinutes(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "sec")) {
				dateValue = DateUtils.addSeconds(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "milli")) {
				dateValue = DateUtils.addMilliseconds(now, offset);
			} else {
				throw new IllegalArgumentException("Invalid period: " + period);
			}
		} else {
			//convert date string as it is to a date
			if (StringUtils.isBlank(dateFormat)) {
				if (string.length() == ArtUtils.ISO_DATE_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_FORMAT;
				} else if (string.length() == ArtUtils.ISO_DATE_TIME_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_TIME_FORMAT;
				} else if (string.length() == ArtUtils.ISO_DATE_TIME_SECONDS_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_TIME_SECONDS_FORMAT;
				} else if (string.length() == ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT;
				} else {
					throw new IllegalArgumentException("Unexpected date format: " + string);
				}
			}

			if (locale == null) {
				locale = Locale.getDefault();
			}

			//not all locales work with simpledateformat
			//with lenient set to false, parsing may throw an error if the locale is not available
			if (logger.isDebugEnabled()) {
				Locale[] locales = SimpleDateFormat.getAvailableLocales();
				if (!Arrays.asList(locales).contains(locale)) {
					logger.debug("Locale '{}' not available for date parameter parsing", locale);
				}
			}

			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat, locale);
			dateFormatter.setLenient(false); //don't allow invalid date strings to be coerced into valid dates
			dateValue = dateFormatter.parse(string);
		}

		return dateValue;
	}

	/**
	 * Processes the contents of a groovy expression specification and returns
	 * the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @return the processed value
	 */
	public String processGroovy(String string) {
		Map<String, ReportParameter> reportParamsMap = null;
		return processGroovy(string, reportParamsMap);
	}

	/**
	 * Processes the contents of a groovy expression specification and returns
	 * the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map with report parameters
	 * @return the processed value
	 */
	public String processGroovy(String string, Map<String, ReportParameter> reportParamsMap) {
		String finalString = string;

		String[] groovyExpressions = StringUtils.substringsBetween(string, GROOVY_START_STRING, GROOVY_END_STRING);
		if (groovyExpressions != null) {
			CompilerConfiguration cc = new CompilerConfiguration();
			cc.addCompilationCustomizers(new SandboxTransformer());

			Map<String, Object> variables = new HashMap<>();
			if (reportParamsMap != null) {
				variables.putAll(reportParamsMap);
			}

			Binding binding = new Binding(variables);

			GroovyShell shell = new GroovyShell(binding, cc);

			GroovySandbox sandbox = null;
			if (Config.getCustomSettings().isEnableGroovySandbox()) {
				sandbox = new GroovySandbox();
				sandbox.register();
			}

			try {
				Map<String, String> groovyExpressionValues = new HashMap<>();
				for (String groovyExpression : groovyExpressions) {
					Object resultObject = shell.evaluate(groovyExpression);
					String resultString = String.valueOf(resultObject);
					String groovySpecification = GROOVY_START_STRING + groovyExpression + GROOVY_END_STRING;
					groovyExpressionValues.put(groovySpecification, resultString);
				}

				for (Entry<String, String> entry : groovyExpressionValues.entrySet()) {
					String searchString = entry.getKey();
					String replaceString = entry.getValue();
					finalString = StringUtils.replace(finalString, searchString, replaceString);
				}
			} finally {
				if (sandbox != null) {
					sandbox.unregister();
				}
			}
		}

		return finalString;
	}

	/**
	 * Runs a groovy expression and returns the result
	 * 
	 * @param string the string containing the groovy script
	 * @return the object returned by the groovy script
	 */
	public Object runGroovyExpression(String string) {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.addCompilationCustomizers(new SandboxTransformer());

		Binding binding = new Binding();

		GroovyShell shell = new GroovyShell(binding, cc);

		GroovySandbox sandbox = null;
		if (Config.getCustomSettings().isEnableGroovySandbox()) {
			sandbox = new GroovySandbox();
			sandbox.register();
		}

		if (StringUtils.startsWith(string, GROOVY_START_STRING)) {
			string = StringUtils.substringBetween(string, GROOVY_START_STRING, GROOVY_END_STRING);
		}

		Object result = null;
		try {
			result = shell.evaluate(string);
		} finally {
			if (sandbox != null) {
				sandbox.unregister();
			}
		}

		return result;
	}

}
