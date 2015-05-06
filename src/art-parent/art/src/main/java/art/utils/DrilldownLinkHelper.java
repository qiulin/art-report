/*
 * Copyright (C) 2015 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.drilldown.Drilldown;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.reportparameter.ReportParameter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Timothy Anyona
 */
public class DrilldownLinkHelper {

	private final Drilldown drilldown;
	private final List<Parameter> drilldownParams;
	private final Set<String> drilldownParamNames;
	private final List<ReportParameter> reportParamsList;

	public DrilldownLinkHelper(Drilldown drilldown, List<ReportParameter> reportParamsList) throws SQLException {
		Objects.requireNonNull(drilldown, "drilldown must not be null");
		
		this.drilldown = drilldown;
		this.reportParamsList = reportParamsList;

		int drilldownReportId = drilldown.getDrilldownReport().getReportId();
		ParameterService parameterService = new ParameterService();
		drilldownParams = parameterService.getDrilldownParameters(drilldownReportId);

		//store parameter names so that parent parameters with the same name
		//as in the drilldown report are omitted
		//use hashset for fast searching using contains
		//https://stackoverflow.com/questions/3307549/fastest-way-to-check-if-a-liststring-contains-a-unique-string
		drilldownParamNames = new HashSet<>();
		for (Parameter drilldownParam : drilldownParams) {
			String paramName = drilldownParam.getName();
			drilldownParamNames.add(paramName);
		}
	}

	private void addDrilldownBaseUrl(StringBuilder sb) {
		if (drilldown != null) {
			int drilldownReportId = drilldown.getDrilldownReport().getReportId();
			String drilldownReportFormat = drilldown.getReportFormat();
			if (drilldownReportFormat == null || drilldownReportFormat.equalsIgnoreCase("all")) {
				sb.append("showReport.do?reportId=").append(drilldownReportId);
			} else {
				sb.append("runReport.do?reportId=").append(drilldownReportId)
						.append("&reportFormat=").append(drilldownReportFormat);
			}
		}
	}

	private void addUrlParameter(String paramName, String paramValue, StringBuilder sb) {
		if (paramName == null || paramValue == null || sb == null) {
			return;
		}

		try {
			String encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
			sb.append("&p-").append(paramName).append("=").append(encodedParamValue);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void addParentParameters(StringBuilder sb) {
		if (reportParamsList == null) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			String[] paramValues = reportParam.getPassedParameterValues();

			//add parameter only if one with a similar name doesn't already
			//exist in the drill down parameters
			if (drilldownParamNames == null || !drilldownParamNames.contains(paramName)) {
				if (paramValues != null) {
					for (String paramValue : paramValues) {
						addUrlParameter(paramName, paramValue, sb);
					}
				}
			}
		}

	}

	public String getDrilldownLink(Object... paramValues) {
		StringBuilder sb = new StringBuilder(200);

		//add base url
		addDrilldownBaseUrl(sb);

		//add drilldown parameters
		if (drilldownParams != null) {
			int paramCount = 0;
			for (Parameter drilldownParam : drilldownParams) {
				paramCount++;
				String paramName = drilldownParam.getName();
				Object paramValueObject = paramValues[paramCount - 1];
				String paramValueString;
				if (paramValueObject == null) {
					paramValueString = ""; //use empty string for nulls rather than the string "null"
				} else if (paramValueObject instanceof Date) {
					//convert date to string that will be recognised by parameter processor class
					paramValueString = ArtUtils.defaultDateFormatter.format(paramValueObject);
				} else {
					paramValueString = String.valueOf(paramValues[paramCount - 1]);
				}
				addUrlParameter(paramName, paramValueString, sb);
			}
		}

		//add parameters from parent report
		addParentParameters(sb);

		String drilldownUrl = sb.toString();

		return drilldownUrl;
	}

}