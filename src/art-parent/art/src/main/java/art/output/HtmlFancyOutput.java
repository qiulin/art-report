/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
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
/*
 * Fancy html output mode
 */
package art.output;

import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;

/**
 * Fancy html output mode
 *
 * @author Enrico Liboni
 */
public class HtmlFancyOutput extends TabularOutput {

	private final String CLOSE_RESULTS_TABLE_HTML = "</tr></tbody></table></div>";
	private boolean evenRow;

	@Override
	public void beginHeader() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlFancyOutput.css'>");

		//start results table
		out.println("<div style='border: 3px solid white'>");
		out.println("<table class='qe' width='80%'>");
		out.println("<tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<td class='qeattr'>" + value + "</td>");
	}

	@Override
	public void addHeaderCellLeftAligned(String value) {
		out.println("<td class='qeattrLeft'>" + value + "</td>");
	}

	@Override
	public void endHeader() {
		out.println("</tr>");
	}

	@Override
	public void addCellString(String value) {
		String cssClass;
		if (evenRow) {
			cssClass = "qeevenLeft";
		} else {
			cssClass = "qeoddLeft";
		}

		out.println("<td class='" + cssClass + "'>" + value + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;
		if (value == null) {
			formattedValue = null;
		} else {
			formattedValue = actualNumberFormatter.format(value);
		}

		String cssClass;
		if (evenRow) {
			cssClass = "qeeven";
		} else {
			cssClass = "qeodd";
		}

		out.println("<td style='text-align: right' class='" + cssClass + "'>" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue;
		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = ArtConfig.getDateDisplayString(value);
		}

		String cssClass;
		if (evenRow) {
			cssClass = "qeevenLeft";
		} else {
			cssClass = "qeoddLeft";
		}

		out.println("<td class='" + cssClass + "'>" + formattedValue + "</td>");
	}

	@Override
	public boolean newRow() {
		boolean canProceed;

		rowCount++;

		if (rowCount % 2 == 0) {
			evenRow = true;
		} else {
			evenRow = false;
		}

		if (rowCount > maxRows) {
			canProceed = false;

			//close table
			out.println(CLOSE_RESULTS_TABLE_HTML);
		} else {
			canProceed = true;

			if (rowCount > 1) {
				//close previous row
				out.println("</tr>");
			}

			//open new row
			out.println("<tr>");
		}

		return canProceed;
	}

	@Override
	public void endRows() {
		out.println(CLOSE_RESULTS_TABLE_HTML);
	}

}