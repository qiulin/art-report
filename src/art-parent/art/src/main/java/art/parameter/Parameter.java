/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
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
package art.parameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Class to represent a parameter
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
	private boolean useFiltersInLov;
	private int chainedPosition;
	private int chainedValuePosition;
	private int drilldownColumnIndex;
	private boolean useDirectSubstitution;
	private Date creationDate;
	private Date updateDate;
	private String description;
	private String createdBy;
	private String updatedBy;
	
	/**
	 * Utility method to determine, if the parameter is chained (it's value
	 * depends on another one), the position of the parameter which determines
	 * this parameter's value
	 *
	 * @return the position of the parameter which determines this parameter's
	 * value
	 */
	public int getEffectiveChainedValuePosition() {
		if (chainedPosition > 0 && chainedValuePosition > 0) {
			//parameter is chained, but value doesn't come from the master parameter
			return chainedValuePosition;
		} else {
			return chainedPosition;
		}
	}

	/**
	 * Utility method to determine if the parameter is chained (it's value
	 * depends on another parameter)
	 *
	 * @return <code>true</code> if parameter is chained
	 */
	public boolean isChained() {
		if (chainedPosition > 0) {
			return true;
		} else {
			return false;
		}
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
	 * @return the useFiltersInLov
	 */
	public boolean isUseFiltersInLov() {
		return useFiltersInLov;
	}

	/**
	 * @param useFiltersInLov the useFiltersInLov to set
	 */
	public void setUseFiltersInLov(boolean useFiltersInLov) {
		this.useFiltersInLov = useFiltersInLov;
	}

	/**
	 * @return the chainedPosition
	 */
	public int getChainedPosition() {
		return chainedPosition;
	}

	/**
	 * @param chainedPosition the chainedPosition to set
	 */
	public void setChainedPosition(int chainedPosition) {
		this.chainedPosition = chainedPosition;
	}

	/**
	 * @return the chainedValuePosition
	 */
	public int getChainedValuePosition() {
		return chainedValuePosition;
	}

	/**
	 * @param chainedValuePosition the chainedValuePosition to set
	 */
	public void setChainedValuePosition(int chainedValuePosition) {
		this.chainedValuePosition = chainedValuePosition;
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
}