/*
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.reportparameter.ReportParameter;
import art.utils.ArtUtils;
import java.util.Date;
import java.util.List;
import org.owasp.encoder.Encode;

/**
 * Generates plain html output. Can be used for jobs because the output does not
 * depend on other files (css etc) and it is a standalone page
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlPlainOutput extends StandardOutput {

	private final boolean fileOutput;

	public HtmlPlainOutput(boolean fileOutput) {
		this.fileOutput = fileOutput;
	}

	@Override
	public void init() {
		if (fileOutput) {
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<meta charset='utf-8'>");
			out.println("</head>");
			out.println("<body>");
		}

		//https://www.campaignmonitor.com/css/
		out.println("<style>"
				+ "table {border-collapse: collapse;}"
				+ "\n td {background-color: #FFFFFF; border: 1px solid #000000; font-size: 10pt;}"
				+ "\n body {font-family: Verdana, Helvetica , Arial, SansSerif; color: #000000;}"
				+ "\n</style>");

	}

	@Override
	public void addTitle() {
		if (!fileOutput) {
			return;
		}

		out.println("<div align='center'>");
		out.println("<table border='0' width='100%' cellspacing='1'"
				+ " cellpadding='1'>");
		out.println("<tr><td>");

		String escapedReportName = Encode.forHtmlContent(reportName);
		String formattedRunDate = ArtUtils.isoDateTimeSecondsFormatter.format(new Date());
		String escapedFormattedRunDate = Encode.forHtmlContent(formattedRunDate);

		out.println("<b>" + escapedReportName + "</b> :: " + escapedFormattedRunDate);

		out.println("</td></tr></table></div>");
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		if (!fileOutput) {
			return;
		}

		out.println("<div align='center'>");
		out.println("<table border='0' width='100%' cellspacing='1'"
				+ " cellpadding='1'>");
		out.println("<tr><td>");

		for (ReportParameter reportParam : reportParamsList) {
			String nameAndDisplayValues = reportParam.getNameAndDisplayValues();
			String escapedNameAndDisplayValues = Encode.forHtmlContent(nameAndDisplayValues);
			out.println(escapedNameAndDisplayValues);
			out.println("<br>");
		}

		out.println("</td></tr></table></div>");
	}

	@Override
	public void beginHeader() {
		out.println("<div align='center'>");
		out.println("<table border='0' width='100%' cellspacing='1'"
				+ " cellpadding='1'>");
		out.println("<tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		String escapedValue = Encode.forHtmlContent(value);
		out.println("<td><b>" + escapedValue + "</b></td>");
	}

	@Override
	public void endHeader() {
		out.println("</tr>");
	}

	@Override
	public void addCellString(String value) {
		String escapedValue = Encode.forHtmlContent(value);
		out.println("<td style='text-align: left'>" + escapedValue + "</td>");
	}
	
	@Override
	public void addCellStringAsIs(String value) {
		out.println("<td style='text-align: left'>" + value + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumericValue(value);
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: right'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: right'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: left'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: left'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void newRow() {
		if (rowCount > 1) {
			//close previous row
			out.println("</tr>");
		}

		//open new row
		out.println("<tr>");
	}

	@Override
	public void endRow() {
		out.println("</tr>");
	}

	@Override
	public void beginTotalRow() {
		out.println("<tfoot><tr>");
	}

	@Override
	public void addCellTotal(Double value) {
		String formattedValue = formatNumericValue(value);
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: right'><b>" + escapedFormattedValue + "</b></td>");
	}

	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: right'><b>" + escapedFormattedValue + "</b></td>");
	}

	@Override
	public void endTotalRow() {
		out.println("</tr><tfoot>");
	}

	@Override
	public void endOutput() {
		out.println("</table></div>");

		if (fileOutput) {
			out.println("</body></html>");
		}
	}
}
