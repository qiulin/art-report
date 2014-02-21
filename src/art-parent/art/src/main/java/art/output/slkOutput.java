/**
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
// This is an attempt to create a decent streamable file
// that is loaded both by Ooo and MS Excel
// "decent" means that a string like "00123" is not
// considered as the number 123
package art.output;

import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create slk output.
 *
 * @author Enrico Liboni
 */
public class slkOutput implements ArtOutputInterface {

	private static final Logger logger = LoggerFactory.getLogger(slkOutput.class);
	FileOutputStream fout;
	byte[] buf;
	String tmpstr;
	StringBuffer exportFileStrBuf;
	String filename;
	String fullFileName;
	NumberFormat nfPlain;
	PrintWriter htmlout;
	String queryName;
	String fileUserName;
	int maxRows;
	int row_count;
	int column_count;
	int columns;
	int counter;
	String y_m_d;
	String h_m_s;
	Map<Integer, ArtQueryParam> displayParams;
	final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;) 
	String exportPath;

	/**
	 * Constructor
	 */
	public slkOutput() {
		exportFileStrBuf = new StringBuffer(8 * 1024);
		counter = 0;
		nfPlain = NumberFormat.getInstance();
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(false);
		nfPlain.setMaximumFractionDigits(99);
	}

	@Override
	public String getName() {
		return "Spreadsheet (slk)";
	}

	@Override
	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	@Override
	public void setExportPath(String s) {
		exportPath = s;
	}

	@Override
	public String getFileName() {
		return fullFileName;
	}

	@Override
	public void setWriter(PrintWriter o) {
		htmlout = o;
	}

	@Override
	public void setQueryName(String s) {
		queryName = s;
	}

	@Override
	public void setFileUserName(String s) {
		fileUserName = s;
	}

	@Override
	public void setMaxRows(int i) {
		maxRows = i;
	}

	@Override
	public void setColumnsNumber(int i) {
		columns = i;
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
		displayParams = t;
	}

	@Override
	public void beginHeader() {
		//initialize objects required for output
		initializeOutput();

		// insert slk header
		/*
		 * // not sure what all this means but it works ;)
		 * exportFileStrBuf.append( "ID;PWXL;N;E\n" + "P;PGeneral\n" + "P;P0\n"
		 * + "P;P0.00\n" + "P;P#,##0\n" + "P;P#,##0.00\n" +
		 * "P;P#,##0;;\\-#,##0\n" + "P;P#,##0;;[Red]\\-#,##0\n" +
		 * "P;P#,##0.00;;\\-#,##0.00\n" + "P;P#,##0.00;;[Red]\\-#,##0.00\n" +
		 * "P;P\"$\"\\ #,##0;;\\-\"$\"\\ #,##0\n" + "P;P\"$\"\\
		 * #,##0;;[Red]\\-\"$\"\\ #,##0\n" + "P;P\"$\"\\ #,##0.00;;\\-\"$\"\\
		 * #,##0.00\n" + "P;P\"$\"\\ #,##0.00;;[Red]\\-\"$\"\\ #,##0.00\n" +
		 * "P;P0%\n" + "P;P0.00%\n" + "P;P0.00E+00\n" + "P;P##0.0E+0\n" +
		 * "P;P#\\ ?/?\n" + "P;P#\\ ??/??\n" + "P;Pdd/mm/yy\n" +
		 * "P;Pdd\\-mmm\\-yy\n" + "P;Pdd\\-mmm\n" + "P;Pmmm\\-yy\n" + "P;Ph:mm\\
		 * AM/PM\n" + "P;Ph:mm:ss\\ AM/PM\n" + "P;Ph:mm\n" + "P;Ph:mm:ss\n" +
		 * "P;Pdd/mm/yy\\ h:mm\n" + "P;Pmm:ss\n" + "P;Pmm:ss.0\n" + "P;P@\n" +
		 * "P;P[h]:mm:ss\n" + "P;P_-\"$\"\\ * #,##0_-;;\\-\"$\"\\ *
		 * #,##0_-;;_-\"$\"\\ * \"-\"_-;;_-@_-\n" + "P;P_-* #,##0_-;;\\-*
		 * #,##0_-;;_-* \"-\"_-;;_-@_-\n" + "P;P_-\"$\"\\ *
		 * #,##0.00_-;;\\-\"$\"\\ * #,##0.00_-;;_-\"$\"\\ * \"-\"??_-;;_-@_-\n"
		 * + "P;P_-* #,##0.00_-;;\\-* #,##0.00_-;;_-* \"-\"??_-;;_-@_-\n" +
		 * "P;FArial;M200\n" );
		 */
		// This is the Ooo header:
		exportFileStrBuf.append("ID;PSCALC3\n"); // much better!!!
		row_count = 1;
		column_count = 1;

		exportFileStrBuf.append("C;Y").append(row_count++).append(";X1;K\"")
				.append(queryName).append(" executed on: ").append(y_m_d.replace('_', '-'))
				.append(" ").append(h_m_s.replace('_', ':')).append("\"\n"); // first row Y1

		// Output parameter values list
		if (displayParams != null && !displayParams.isEmpty()) {
			// rows with params names
			for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
				ArtQueryParam param = entry.getValue();
				String paramName = param.getName();
				addHeaderCell(paramName);
			}

			row_count++;

			// rows with params values
			for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
				ArtQueryParam param = entry.getValue();
				Object pValue = param.getParamValue();
				String outputString;

				if (pValue instanceof String) {
					String paramValue = (String) pValue;
					outputString = paramValue; //default to displaying parameter value

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for this value
							String paramDisplayString = lov.get(paramValue);
							if (!StringUtils.equals(paramValue, paramDisplayString)) {
								//parameter value and display string differ. show both
								outputString = paramDisplayString + " (" + paramValue + ")";
							}
						}
					}					
					addCellString(outputString);
				} else if (pValue instanceof String[]) { // multi
					String[] paramValues = (String[]) pValue;
					outputString = StringUtils.join(paramValues, ", "); //default to showing parameter values only

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for all the parameter values
							String[] paramDisplayStrings = new String[paramValues.length];
							for (int i = 0; i < paramValues.length; i++) {
								String value = paramValues[i];
								String display = lov.get(value);
								if (!StringUtils.equals(display, value)) {
									//parameter value and display string differ. show both
									paramDisplayStrings[i] = display + " (" + value + ")";
								} else {
									paramDisplayStrings[i] = value;
								}
							}
							outputString = StringUtils.join(paramDisplayStrings, ", ");
						}
					}					
					addCellString(outputString);
				}
			}

			row_count++;
		}
	}

	@Override
	public void addHeaderCell(String s) {
		exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + s + "\"\n");
	}
	
	@Override
	public void addHeaderCellLeft(String s) {
		addHeaderCell(s);
	}

	@Override
	public void endHeader() {
	}

	@Override
	public void beginLines() {
		column_count = 1;
	}

	@Override
	public void addCellString(String s) {
		if (s != null) { // s could be null!
			if (s.trim().length() > 250) {
				s = s.substring(0, 250) + "[...]";
			}
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + s.replace('\n', ' ').replace('\r', ' ').replace(';', '-').trim() + "\"\n");
		} else {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + s + "\"\n");
		}
	}

	@Override
	public void addCellDouble(Double d) {
		if (d == null) {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + d + "\"\n");
		} else {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K" + nfPlain.format(d.doubleValue()) + "\n");
		}
	}

	@Override
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
		if (i == null) {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + i + "\"\n");
		} else {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K" + i + "\n");
		}
	}

	@Override
	public void addCellDate(Date d) {
		exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + ArtConfig.getDateDisplayString(d) + "\"\n");
	}

	@Override
	public boolean newLine() {
		column_count = 1;
		row_count++;

		counter++;
		if ((counter * columns) > FLUSH_SIZE) {
			try {
				tmpstr = exportFileStrBuf.toString();
				buf = new byte[tmpstr.length()];
				buf = tmpstr.getBytes("UTF-8");
				fout.write(buf);
				fout.flush();
				exportFileStrBuf = new StringBuffer(32 * 1024);
			} catch (IOException e) {
				logger.error("Error. Data not completed. Please narrow your search", e);

				//htmlout not used for scheduled jobs
				if (htmlout != null) {
					htmlout.println("<span style=\"color:red\">Error: " + e
							+ ")! Data not completed. Please narrow your search!</span>");
				}
			}
		}

		if (counter < maxRows) {
			return true;
		} else {
			addCellString("Maximum number of rows exceeded! Query not completed.");
			endLines(); // close files
			return false;
		}
	}

	@Override
	public void endLines() {
		addCellString("Total rows retrieved:");
		addCellLong(Long.valueOf(counter));
		exportFileStrBuf.append("E");

		try {
			tmpstr = exportFileStrBuf.toString();
			buf = new byte[tmpstr.length()];
			buf = tmpstr.getBytes("UTF-8");
			exportFileStrBuf = null;
			fout.write(buf);
			fout.flush();
			fout.close();
			fout = null; // these nulls are because it seems to be a memory leak in some JVMs

			//htmlout not used for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<p><div align=\"center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=\"2\" class=\"data\" align=\"center\" >"
						+ "<a type=\"application/octet-stream\" href=\"../export/" + filename + "\"> "
						+ filename + "</a>"
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return true;
	}

	/**
	 * Build filename for output file
	 */
	private void buildOutputFileName() {
		// Build filename			
		Date today = new Date();

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd");
		y_m_d = dateFormatter.format(today);

		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH_mm_ss");
		h_m_s = timeFormatter.format(today);

		filename = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtUtils.getRandomFileNameString() + ".slk";
		filename = ArtUtils.cleanFileName(filename); //replace characters that would make an invalid filename
		fullFileName = exportPath + filename;
	}

	/**
	 * Initialise objects required to generate output
	 */
	private void initializeOutput() {
		buildOutputFileName();
		
		try {			
			fout = new FileOutputStream(fullFileName);
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}
}
