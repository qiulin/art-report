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
package art.chart;

import art.enums.ReportType;
import art.runreport.RunReportHelper;
import net.sf.cewolfart.links.PieSectionLinkGenerator;
import net.sf.cewolfart.tooltips.PieToolTipGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for working with pie charts
 *
 * @author Timothy Anyona
 */
public class PieChart extends Chart implements PieToolTipGenerator, PieSectionLinkGenerator {

	private static final Logger logger = LoggerFactory.getLogger(PieChart.class);
	private static final long serialVersionUID = 1L;

	public PieChart(ReportType reportType) {
		logger.debug("Entering PieChart: reportType={}", reportType);

		Objects.requireNonNull(reportType, "reportType must not be null");

		switch (reportType) {
			case Pie2DChart:
				type = "pie";
				break;
			case Pie3DChart:
				type = "pie3d";
				break;
			default:
				throw new IllegalArgumentException("Unsupported report type: " + reportType);
		}

		setHasTooltips(true);
	}

	@Override
	public void fillDataset(ResultSet rs) throws SQLException {
		logger.debug("Entering fillDataset");

		Objects.requireNonNull(rs, "rs must not be null");

		DefaultPieDataset dataset = new DefaultPieDataset();

		resultSetColumnNames = new ArrayList<>();
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String columnName = rsmd.getColumnLabel(i);
			resultSetColumnNames.add(columnName);
		}

		resultSetData = new ArrayList<>();

		//resultset structure
		//category, value [, link]
		while (rs.next()) {
			resultSetRecordCount++;

			Map<String, Object> row = new LinkedHashMap<>();
			Map<Integer, Object> indexRow = new LinkedHashMap<>();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnLabel(i);
				Object data = rs.getObject(i);
				row.put(columnName, data);
				indexRow.put(i, data);
			}

			if (includeDataInOutput) {
				resultSetData.add(row);
			}

			prepareRow(row, indexRow, resultSetColumnNames, dataset);
		}

		setDataset(dataset);
	}

	@Override
	public void fillDataset(List<? extends Object> data) {
		logger.debug("Entering fillDataset");

		Objects.requireNonNull(data, "data must not be null");

		DefaultPieDataset dataset = new DefaultPieDataset();

		//resultset structure
		//category, value [, link]
		for (Object row : data) {
			Map<Integer, Object> indexRow = null;
			prepareRow(row, indexRow, columnNames, dataset);
		}

		setDataset(dataset);
	}

	/**
	 * Fills the dataset with a row of data
	 *
	 * @param row the row of data. May be null if indexRow is used.
	 * @param indexRow the row of data with the column index as the key. May be
	 * null if row is used. If not null, will be used even if row is supplied.
	 * @param dataColumnNames the data column names
	 * @param dataset the dataset
	 */
	private void prepareRow(Object row, Map<Integer, Object> indexRow,
			List<String> dataColumnNames, DefaultPieDataset dataset) {

		String category = RunReportHelper.getStringRowValue(row, indexRow, 1, dataColumnNames);
		double value = RunReportHelper.getDoubleRowValue(row, indexRow, 2, dataColumnNames);

		//add dataset value
		dataset.setValue(category, value);

		String linkId = category;

		//add hyperlink if required
		addHyperLink(row, linkId);

		//add drilldown link if required
		//drill down on col 1 = data value
		//drill down on col 2 = category
		addDrilldownLink(linkId, value, category);
	}

	@Override
	public String generateToolTip(PieDataset data, @SuppressWarnings("rawtypes") Comparable key, int pieIndex) {
		//get data value to be used in tooltip
		double dataValue = data.getValue(pieIndex).doubleValue();

		//format value
		NumberFormat nf = NumberFormat.getInstance(locale);
		String formattedValue = nf.format(dataValue);

		//in case one wishes to show category names
		//String categoryName = String.valueOf(key);
		return formattedValue;
	}

	@Override
	public String generateLink(Object data, Object category) {
		String link = "";

		String key = String.valueOf(category);
		if (getHyperLinks() != null) {
			link = getHyperLinks().get(key);
		} else if (getDrilldownLinks() != null) {
			link = getDrilldownLinks().get(key);
		}

		return link;
	}
}
