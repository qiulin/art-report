/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dashboard, whose details are displayed in a dashboard report
 *
 * @author Timothy Anyona
 */
public class Dashboard extends AbstractDashboard {

	private static final long serialVersionUID = 1L;
	private List<List<Portlet>> columns;

	/**
	 * @return the columns
	 */
	public List<List<Portlet>> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<List<Portlet>> columns) {
		this.columns = columns;
	}

	public List<Portlet> getAllPortlets() {
		List<Portlet> allPortlets = new ArrayList<>();

		for (List<Portlet> column : columns) {
			allPortlets.addAll(column);
		}

		return allPortlets;
	}
}
