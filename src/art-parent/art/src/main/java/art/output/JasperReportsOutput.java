/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.dbutils.DatabaseUtils;
import art.enums.ParameterType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.parameter.Parameter;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.ArtConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate reports using the JasperReports library
 *
 * @author Timothy Anyona
 */
public class JasperReportsOutput {

	private static final Logger logger = LoggerFactory.getLogger(JasperReportsOutput.class);
	private final List<String> completedSubReports = new ArrayList<>(); //used with recursive compileReport call
	private ResultSet resultSet;

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * Generate report output
	 *
	 * @param report
	 * @param reportParams
	 * @param reportType
	 * @param reportFormat
	 * @param outputFileName
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 * @throws net.sf.jasperreports.engine.JRException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			ReportType reportType, ReportFormat reportFormat, String outputFileName)
			throws IOException, SQLException, JRException {

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportParams, "reportParams must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");

		//use JRAbstractLRUVirtualizer instead of JRVirtualizer to have access to setReadOnly() method
		JRAbstractLRUVirtualizer jrVirtualizer = null;

		try {
			String templateFileName = report.getTemplate();
			String baseTemplateFileName = FilenameUtils.getBaseName(templateFileName);
			String templatesPath = ArtConfig.getTemplatesPath();
			String jasperFilePath = templatesPath + baseTemplateFileName + ".jasper";
			String jrxmlFilePath = templatesPath + baseTemplateFileName + ".jrxml";

			File jasperFile = new File(jasperFilePath);
			File jrxmlFile = new File(jrxmlFilePath);

			//check if template file exists
			if (!jasperFile.exists() && !jrxmlFile.exists()) {
				throw new IllegalStateException("Template file not found: " + baseTemplateFileName);
			}

			//compile report and subreports if necessary
			compileReport(baseTemplateFileName);

			//create object for storing all jasper reports parameters - query parameters, virtualizers, etc
			Map<String, Object> jasperReportsParams = new HashMap<>();

			//pass query parameters
			for (ReportParameter reportParam : reportParams) {
				jasperReportsParams.put(reportParam.getParameter().getName(), reportParam.getEffectiveActualParameterValue());
			}

			//pass virtualizer if it's to be used
			jrVirtualizer = createVirtualizer();
			if (jrVirtualizer != null) {
				jasperReportsParams.put(JRParameter.REPORT_VIRTUALIZER, jrVirtualizer);
			}

			//fill report with data
			JasperPrint jasperPrint;
			if (reportType == ReportType.JasperReportsTemplate) {
				Connection conn = null;
				try {
					RunReportHelper runReportHelper = new RunReportHelper();
					conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
					jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, conn);
				} finally {
					DatabaseUtils.close(conn);
				}
			} else {
				//use recordset from art query
				jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, new JRResultSetDataSource(resultSet));
			}

			//set virtualizer as read only to optimize performance
			//must be set after print object has been generated
			if (jrVirtualizer != null) {
				jrVirtualizer.setReadOnly(true);
			}

			//export report
			switch (reportFormat) {
				case pdf:
					JasperExportManager.exportReportToPdfFile(jasperPrint, outputFileName);
					break;
				case html:
					JRXhtmlExporter htmlExporter = new JRXhtmlExporter();

					htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					htmlExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName);

					htmlExporter.exportReport();
					break;
				case xls:
					JRXlsExporter xlsExporter = new JRXlsExporter();

					xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					xlsExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName);
					xlsExporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					xlsExporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

					xlsExporter.exportReport();
					break;
				case xlsx:
					JRXlsxExporter xlsxExporter = new JRXlsxExporter();

					xlsxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					xlsxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFileName);
					xlsxExporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					xlsxExporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

					xlsxExporter.exportReport();
					break;
				default:
					throw new IllegalArgumentException("Invalid report format: " + reportFormat);
			}
		} finally {
			if (jrVirtualizer != null) {
				jrVirtualizer.cleanup();
			}
		}
	}

	/**
	 * Create a jasper reports virtualizer using settings in the
	 * jasperreports.properties file. swap virtualizer will be used if none is
	 * configured.
	 *
	 * @return created virtualizer or null if virtualizer property in the file
	 * is set to "none"
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private JRAbstractLRUVirtualizer createVirtualizer() throws IOException {
		logger.debug("Entering createVirtualizer");

		//use JRAbstractLRUVirtualizer instead of JRVirtualizer in order to
		//provide access to setReadOnly() method
		JRAbstractLRUVirtualizer jrVirtualizer;

		//set virtualizer properties, if virtualizer is to be used
		Properties properties = new Properties();
		String propertiesFilePath = ArtConfig.getClassesPath() + "jasperreports.properties";
		File propertiesFile = new File(propertiesFilePath);
		if (propertiesFile.exists()) {
			try (FileInputStream o = new FileInputStream(propertiesFilePath)) {
				properties.load(o);
			}
		}

		//finalize properties object
		//use values from the properties file if they exist, otherwise use defaults
		final String VIRTUALIZER = "virtualizer";
		final String SWAP_MAX_SIZE = "swap.maxSize";
		final String SWAP_BLOCK_SIZE = "swap.blockSize";
		final String SWAP_MIN_GROW_COUNT = "swap.minGrowCount";
		final String SWAP_DIRECTORY = "swap.directory";
		final String FILE_MAX_SIZE = "file.maxSize";
		final String FILE_DIRECTORY = "file.directory";
		final String GZIP_MAX_SIZE = "gzip.maxSize";

		if (properties.getProperty(VIRTUALIZER) == null) {
			properties.setProperty(VIRTUALIZER, "swap");
		}
		if (properties.getProperty(SWAP_MAX_SIZE) == null) {
			properties.setProperty(SWAP_MAX_SIZE, "300");
		}
		if (properties.getProperty(SWAP_BLOCK_SIZE) == null) {
			properties.setProperty(SWAP_BLOCK_SIZE, "4096");
		}
		if (properties.getProperty(SWAP_MIN_GROW_COUNT) == null) {
			properties.setProperty(SWAP_MIN_GROW_COUNT, "1024");
		}
		if (properties.getProperty(SWAP_DIRECTORY) == null) {
			properties.setProperty(SWAP_DIRECTORY, System.getProperty("java.io.tmpdir"));
		}
		if (properties.getProperty(FILE_MAX_SIZE) == null) {
			properties.setProperty(FILE_MAX_SIZE, "300");
		}
		if (properties.getProperty(FILE_DIRECTORY) == null) {
			properties.setProperty(FILE_DIRECTORY, System.getProperty("java.io.tmpdir"));
		}
		if (properties.getProperty(GZIP_MAX_SIZE) == null) {
			properties.setProperty(GZIP_MAX_SIZE, "300");
		}

		//use virtualizer if required
		String virtualizer = properties.getProperty(VIRTUALIZER);
		logger.debug("virtualizer='{}'", virtualizer);

		if (StringUtils.equalsIgnoreCase(virtualizer, "none")) {
			jrVirtualizer = null;
		} else if (StringUtils.equalsIgnoreCase(virtualizer, "file")) {
			int maxSize = NumberUtils.toInt(properties.getProperty(FILE_MAX_SIZE));
			jrVirtualizer = new JRFileVirtualizer(maxSize, properties.getProperty(FILE_DIRECTORY));
		} else if (StringUtils.equalsIgnoreCase(virtualizer, "gzip")) {
			int maxSize = NumberUtils.toInt(properties.getProperty(GZIP_MAX_SIZE));
			jrVirtualizer = new JRGzipVirtualizer(maxSize);
		} else {
			//use swap virtualizer by default
			int maxSize = NumberUtils.toInt(properties.getProperty(SWAP_MAX_SIZE));
			int blockSize = NumberUtils.toInt(properties.getProperty(SWAP_BLOCK_SIZE));
			int minGrowCount = NumberUtils.toInt(properties.getProperty(SWAP_MIN_GROW_COUNT));
			JRSwapFile swapFile = new JRSwapFile(properties.getProperty(SWAP_DIRECTORY), blockSize, minGrowCount);
			jrVirtualizer = new JRSwapFileVirtualizer(maxSize, swapFile);
		}

		return jrVirtualizer;
	}

	/**
	 * Compile a report and all it's subreports
	 *
	 * @param baseFileName report file name without the extension
	 */
	private void compileReport(String baseFileName) throws JRException {
		logger.debug("Entering compileReport: baseFileName='{}'", baseFileName);

		String templatesPath = ArtConfig.getTemplatesPath();
		String jasperFilePath = templatesPath + baseFileName + ".jasper";
		String jrxmlFilePath = templatesPath + baseFileName + ".jrxml";

		File jasperFile = new File(jasperFilePath);
		File jrxmlFile = new File(jrxmlFilePath);

		//compile report if .jasper doesn't exist or is outdated
		if (!jasperFile.exists() || (jasperFile.lastModified() < jrxmlFile.lastModified())) {
			JasperCompileManager.compileReportToFile(jrxmlFilePath, jasperFilePath);
		}

		//load report object
		JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFilePath);

		//Compile sub reports
		JRElementsVisitor.visitReport(jasperReport, new JRVisitorSupport() {
			@Override
			public void visitSubreport(JRSubreport subreport) {
				String subreportName = subreport.getExpression().getText().replace("\"", ""); //file name is quoted
				subreportName = StringUtils.substringBeforeLast(subreportName, ".");
				//Sometimes the same subreport can be used multiple times, but
				//there is no need to compile multiple times
				if (completedSubReports.contains(subreportName)) {
					return;
				}
				completedSubReports.add(subreportName);

				//recursively compile any reports within the subreport
				//see https://stackoverflow.com/questions/10265576/java-retain-information-in-recursive-function
				try {
					compileReport(subreportName);
				} catch (JRException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}
}