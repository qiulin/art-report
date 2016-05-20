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
package art.jobparameter;

import art.enums.ParameterType;
import java.io.Serializable;

/**
 * Represents a job parameter
 * 
 * @author Timothy Anyona
 */
public class JobParameter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int jobId;
	private String name;
	private String Value;
	private ParameterType parameterType;
	private String paramTypeString;

	/**
	 * @return the paramTypeString
	 */
	public String getParamTypeString() {
		return paramTypeString;
	}

	/**
	 * @param paramTypeString the paramTypeString to set
	 */
	public void setParamTypeString(String paramTypeString) {
		this.paramTypeString = paramTypeString;
	}

	/**
	 * @return the jobId
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
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
	 * @return the Value
	 */
	public String getValue() {
		return Value;
	}

	/**
	 * @param Value the Value to set
	 */
	public void setValue(String Value) {
		this.Value = Value;
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
}
