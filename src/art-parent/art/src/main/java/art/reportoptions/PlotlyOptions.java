/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents options for plotly.js reports
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlotlyOptions {
	
	private String template;
	private String barmode;
	private String xColumn;
	private List<String> yColumns;
	private String type;
	private String mode;

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @return the barmode
	 */
	public String getBarmode() {
		return barmode;
	}

	/**
	 * @param barmode the barmode to set
	 */
	public void setBarmode(String barmode) {
		this.barmode = barmode;
	}

	/**
	 * @return the xColumn
	 */
	public String getxColumn() {
		return xColumn;
	}

	/**
	 * @param xColumn the xColumn to set
	 */
	public void setxColumn(String xColumn) {
		this.xColumn = xColumn;
	}

	/**
	 * @return the yColumns
	 */
	public List<String> getyColumns() {
		return yColumns;
	}

	/**
	 * @param yColumns the yColumns to set
	 */
	public void setyColumns(List<String> yColumns) {
		this.yColumns = yColumns;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
}
