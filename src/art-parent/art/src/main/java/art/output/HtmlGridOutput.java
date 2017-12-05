/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.owasp.encoder.Encode;

/**
 * Generates Grid html output mode
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlGridOutput extends StandardOutput {

	private String tableId;

	@Override
	public void init() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlGridOutput.css'>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/sorttable.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/htmlGridOutput.js'></script>");

		if (!ajax) {
			//including jquery.js while using $.load() or $.post() results in spinner not appearing on second run
			out.println("<script src='" + contextPath + "/js/jquery-1.12.4.min.js'></script>");
		}

		tableId = "Tid" + Long.toHexString(Double.doubleToLongBits(Math.random()));
		out.println("<script>");
		out.println("	$(document).ready(function() {");
		out.println("		var newTableObject = document.getElementById('" + tableId + "');");
		out.println("		sorttable.DATE_RE = /^(\\d\\d?)[\\/\\.-](\\d\\d?)[\\/\\.-]((\\d\\d)?\\d\\d)$/;");
		out.println("		sorttable.makeSortable(newTableObject);");
		out.println("	});");
		out.println("</script>");
	}

	@Override
	public void beginHeader() {
		out.println("<div style='border: 3px solid white'>");
		out.println("<table class='sortable heatmap' id='" + tableId + "'"
				+ " cellpadding='2' cellspacing='0'"
				+ " style='margin: 0 auto; width: 100%'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<th>" + value + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
		out.println("<th style='text-align: left'>" + value + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value, String sortValue) {
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);
		out.println("<th style='text-align: left' sorttable_customkey='" + escapedSortValue + "'>"
				+ value + "</th>");
	}

	@Override
	public void endHeader() {
		out.println("</tr></thead>");

	}

	@Override
	public void beginRows() {
		out.println("<tbody>");
	}

	@Override
	public void addCellString(String value) {
		out.println("<td style='text-align: left'>" + value + "</td>");
	}

	@Override
	public void addCellStringUnsafe(String value) {
		String escapedValue = Encode.forHtmlContent(value);

		out.println("<td style='text-align: left'>" + escapedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumericValue(value);
		String sortValue = getNumericSortValue(value);

		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);

		double heatmapValue = getHeatmapValue(value);

		out.println("<td style='text-align: right' sorttable_customkey='"
				+ escapedSortValue
				+ "' data-value='" + heatmapValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);

		double heatmapValue = getHeatmapValue(numericValue);

		out.println("<td style='text-align: right' sorttable_customkey='"
				+ escapedSortValue
				+ "' data-value='" + heatmapValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		long sortValue = getDateSortValue(value);

		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: left' sorttable_customkey='"
				+ sortValue + "'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: left' sorttable_customkey='"
				+ sortValue + "'>" + escapedFormattedValue + "</td>");
	}
	
	@Override
	public void addCellImage(byte[] binaryData) {
		if (binaryData == null) {
			out.println("<td></td>");
		} else {
			String stringData = Base64.encodeBase64String(binaryData);
			out.println("<td style='text-align: center'><img src='data:image/png;base64," + stringData + "'></td>");
		}
	}

	@Override
	public void newRow() {
		if (rowCount > 1) {
			//close previous row
			out.println("</tr>");
		}

		//open new row
		out.println("<tr class='rows' onclick='javascript:selectRow(this)'"
				+ " ondblclick='javascript:selectRow2(this)'"
				+ " onmouseover='javascript:highLight(this,\"hiliterows\")'"
				+ " onmouseout='javascript:highLight(this,\"rows\")'"
				+ ">");
	}

	@Override
	public void endRow() {
		out.println("</tr>");
	}

	@Override
	public void endRows() {
		out.println("</tbody>");
	}

	@Override
	public void beginTotalRow() {
		out.println("<tfoot><tr>");
	}

	@Override
	public void endTotalRow() {
		out.println("</tr><tfoot>");
	}

	@Override
	public void endOutput() {
		out.println("</table></div>");
	}

}
