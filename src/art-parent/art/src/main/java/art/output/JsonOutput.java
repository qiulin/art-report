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
package art.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates json output
 *
 * @author Timothy Anyona
 */
public class JsonOutput {

	private static final Logger logger = LoggerFactory.getLogger(JsonOutput.class);

	private boolean prettyPrint;

	/**
	 * @return the prettyPrint
	 */
	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * @param prettyPrint the prettyPrint to set
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	/**
	 * Returns resultset data in json representation. Result in the jsonString
	 * property is like: [{"ID":"1","NAME":"Tom","AGE":"24"},
	 * {"ID":"2","NAME":"Bob","AGE":"26"}]
	 *
	 * @param rs the resultset containing the data
	 * @return an object containing the json string representation of the data
	 * and the number of rows in the resultset
	 * @throws SQLException
	 * @throws JsonProcessingException
	 */
	public JsonOutputResult generateOutput(ResultSet rs) throws SQLException, JsonProcessingException {
		logger.debug("Entering generateOutput");

		Objects.requireNonNull(rs, "rs must not be null");

		//https://stackoverflow.com/questions/18960446/how-to-convert-a-java-resultset-into-json
		List<Map<String, Object>> rows = new ArrayList<>();

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		int rowCount = 0;
		while (rs.next()) {
			rowCount++;
			Map<String, Object> row = new HashMap<>();
			for (int i = 1; i <= columnCount; ++i) {
				String columnName = rsmd.getColumnLabel(i);
				Object columnData = rs.getObject(i);
				row.put(columnName, columnData);
			}
			rows.add(row);
		}

		ObjectMapper mapper = new ObjectMapper();
		String jsonString;
		if (prettyPrint) {
			jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
		} else {
			jsonString = mapper.writeValueAsString(rows);
		}

		JsonOutputResult result = new JsonOutputResult();

		result.setJsonString(jsonString);
		result.setRowCount(rowCount);

		return result;

	}

}