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
package art.parameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.report.Report;
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represent a parameter
 *
 * @author Timothy Anyona
 */
public class Parameter implements Serializable {

	private static final long serialVersionUID = 1L;
	private int parameterId;
	private String name;
	private ParameterType parameterType;
	private String label;
	private String helpText;
	private ParameterDataType dataType;
	private String defaultValue;
	private boolean hidden;
	private boolean useLov;
	private int lovReportId;
	private boolean useRulesInLov;
	private int drilldownColumnIndex;
	private boolean useDirectSubstitution;
	private Date creationDate;
	private Date updateDate;
	private String description;
	private String createdBy;
	private String updatedBy;
	private Report defaultValueReport;
	private boolean shared;
	private String options;

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the shared
	 */
	public boolean isShared() {
		return shared;
	}

	/**
	 * @param shared the shared to set
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}

	/**
	 * @return the defaultValueReport
	 */
	public Report getDefaultValueReport() {
		return defaultValueReport;
	}

	/**
	 * @param defaultValueReport the defaultValueReport to set
	 */
	public void setDefaultValueReport(Report defaultValueReport) {
		this.defaultValueReport = defaultValueReport;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * Get the value of description
	 *
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the value of description
	 *
	 * @param description new value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the parameterId
	 */
	public int getParameterId() {
		return parameterId;
	}

	/**
	 * @param parameterId the parameterId to set
	 */
	public void setParameterId(int parameterId) {
		this.parameterId = parameterId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameterType
	 */
	public ParameterType getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType the parameterType to set
	 */
	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the helpText
	 */
	public String getHelpText() {
		return helpText;
	}

	/**
	 * @param helpText the helpText to set
	 */
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	/**
	 * @return the dataType
	 */
	public ParameterDataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(ParameterDataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the useLov
	 */
	public boolean isUseLov() {
		return useLov;
	}

	/**
	 * @param useLov the useLov to set
	 */
	public void setUseLov(boolean useLov) {
		this.useLov = useLov;
	}

	/**
	 * @return the lovReportId
	 */
	public int getLovReportId() {
		return lovReportId;
	}

	/**
	 * @param lovReportId the lovReportId to set
	 */
	public void setLovReportId(int lovReportId) {
		this.lovReportId = lovReportId;
	}

	/**
	 * @return the useRulesInLov
	 */
	public boolean isUseRulesInLov() {
		return useRulesInLov;
	}

	/**
	 * @param useRulesInLov the useRulesInLov to set
	 */
	public void setUseRulesInLov(boolean useRulesInLov) {
		this.useRulesInLov = useRulesInLov;
	}

	/**
	 * @return the drilldownColumnIndex
	 */
	public int getDrilldownColumnIndex() {
		return drilldownColumnIndex;
	}

	/**
	 * @param drilldownColumnIndex the drilldownColumnIndex to set
	 */
	public void setDrilldownColumnIndex(int drilldownColumnIndex) {
		this.drilldownColumnIndex = drilldownColumnIndex;
	}

	/**
	 * @return the useDirectSubstitution
	 */
	public boolean isUseDirectSubstitution() {
		return useDirectSubstitution;
	}

	/**
	 * @param useDirectSubstitution the useDirectSubstitution to set
	 */
	public void setUseDirectSubstitution(boolean useDirectSubstitution) {
		this.useDirectSubstitution = useDirectSubstitution;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + this.parameterId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Parameter other = (Parameter) obj;
		if (this.parameterId != other.parameterId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Parameter{" + "parameterId=" + parameterId + '}';
	}

	/**
	 * Returns the html element name that should be used for this parameter
	 *
	 * @return the html element name that should be used for this parameter
	 */
	public String getHtmlElementName() {
		return ArtUtils.PARAM_PREFIX + name;
	}

	/**
	 * Returns the default value string to be used in html elements. Null is
	 * returned as an empty string.
	 *
	 * @return the default value string to be used in html elements
	 */
	public String getHtmlDefaultValue() {
		String value = defaultValue;

		if (defaultValue == null) {
			value = "";
		}

		return getHtmlValue(value);
	}

	/**
	 * Returns the string that should be used in html elements
	 *
	 * @param value the original value
	 * @return the string that should be used in html elements
	 */
	public String getHtmlValue(Object value) {
		switch (dataType) {
			case Date:
				//convert date to string that will be recognised by parameter processor class
				return ArtUtils.isoDateFormatter.format(value);
			case DateTime:
				return ArtUtils.isoDateTimeFormatter.format(value);
			default:
				return String.valueOf(value);
		}
	}

	/**
	 * Returns the label to use for this parameter, given a particular locale,
	 * taking into consideration the i18n options defined for the parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the label to use for this parameter
	 * @throws java.io.IOException
	 */
	public String getLocalizedLabel(Locale locale) throws IOException {
		if (locale == null) {
			return label;
		} else {
			return getLocalizedLabel(locale.toString());
		}

	}

	/**
	 * Returns the label to use for this parameter, given a particular locale,
	 * taking into consideration the i18n options defined for the parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @return the label to use for this parameter
	 * @throws java.io.IOException
	 */
	public String getLocalizedLabel(String localeString) throws IOException {
		String localizedLabel = label;

		if (StringUtils.isNotBlank(options) && StringUtils.isNotBlank(localeString)) {
			ObjectMapper mapper = new ObjectMapper();
			ParameterOptions parameterOptions = mapper.readValue(options, ParameterOptions.class);
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nLabelOptions = i18nOptions.getLabel();
				if (CollectionUtils.isNotEmpty(i18nLabelOptions)) {
					//https://stackoverflow.com/questions/886955/breaking-out-of-nested-loops-in-java
					//https://stackoverflow.com/questions/5097513/in-java-how-does-break-interact-with-nested-loops
					boolean labelFound = false;
					for (Map<String, String> i18nLabelOption : i18nLabelOptions) {
						//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
						// Get the first entry that the iterator returns
						Entry<String, String> entry = i18nLabelOption.entrySet().iterator().next();
						String localeSetting = entry.getKey();
						String localeLabel = entry.getValue();
						String[] locales = StringUtils.split(localeSetting, ",");
						for (String locale : locales) {
							if (StringUtils.equalsIgnoreCase(locale.trim(), localeString)) {
								localizedLabel = localeLabel;
								labelFound = true;
								break;
							}
						}

						if (labelFound) {
							break;
						}
					}
				}
			}
		}

		return localizedLabel;
	}

	/**
	 * Returns the help text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the help text to use for this parameter
	 * @throws java.io.IOException
	 */
	public String getLocalizedHelpText(Locale locale) throws IOException {
		if (locale == null) {
			return helpText;
		} else {
			return getLocalizedHelpText(locale.toString());
		}
	}

	/**
	 * Returns the help text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param localeString the string that represents the locale to use
	 * @return the help text to use for this parameter
	 * @throws java.io.IOException
	 */
	public String getLocalizedHelpText(String localeString) throws IOException {
		String localizedHelpText = helpText;

		if (StringUtils.isNotBlank(options) && StringUtils.isNotBlank(localeString)) {
			ObjectMapper mapper = new ObjectMapper();
			ParameterOptions parameterOptions = mapper.readValue(options, ParameterOptions.class);
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nHelpTextOptions = i18nOptions.getHelpText();
				if (CollectionUtils.isNotEmpty(i18nHelpTextOptions)) {
					boolean helpTextFound = false;
					for (Map<String, String> i18nHelpTextOption : i18nHelpTextOptions) {
						//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
						// Get the first entry that the iterator returns
						Entry<String, String> entry = i18nHelpTextOption.entrySet().iterator().next();
						String localeSetting = entry.getKey();
						String localeHelpText = entry.getValue();
						String[] locales = StringUtils.split(localeSetting, ",");
						for (String locale : locales) {
							if (StringUtils.equalsIgnoreCase(locale.trim(), localeString)) {
								localizedHelpText = localeHelpText;
								helpTextFound = true;
								break;
							}
						}

						if (helpTextFound) {
							break;
						}
					}
				}
			}
		}

		return localizedHelpText;
	}
}
