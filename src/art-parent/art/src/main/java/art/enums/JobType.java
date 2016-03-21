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
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum for job types
 *
 * @author Timothy Anyona
 */
public enum JobType {

	EmailAttachment("EmailAttachment"), EmailInline("EmailInline"),
	Alert("Alert"), Publish("Publish"), JustRun("JustRun"),
	CondEmailAttachment("CondEmailAttachment"), CondEmailInline("CondEmailInline"),
	CondPublish("CondPublish"), CacheAppend("CacheAppend"),
	CacheInsert("CacheInsert");

	private final String value;

	private JobType(String value) {
		this.value = value;
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}
	
	public boolean isEmailInline(){
		switch(this){
			case EmailInline:
			case CondEmailInline:
				return true;
			default:
				return false;
		}
	}

	public boolean isEmailAttachment() {
		switch (this) {
			case EmailAttachment:
			case CondEmailAttachment:
				return true;
			default:
				return false;
		}
	}

	public boolean isCache() {
		switch (this) {
			case CacheAppend:
			case CacheInsert:
				return true;
			default:
				return false;
		}
	}

	public boolean isConditional() {
		switch (this) {
			case CondEmailAttachment:
			case CondEmailInline:
			case CondPublish:
				return true;
			default:
				return false;
		}
	}

	public boolean isEmail() {
		switch (this) {
			case EmailAttachment:
			case EmailInline:
			case CondEmailAttachment:
			case CondEmailInline:
				return true;
			default:
				return false;
		}
	}

	public boolean isPublish() {
		switch (this) {
			case Publish:
			case CondPublish:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<JobType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it afterwards
		List<JobType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, null is returned
	 *
	 * @param value
	 * @return
	 */
	public static JobType toEnum(String value) {
		return toEnum(value, null);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value
	 * @param defaultEnum
	 * @return
	 */
	public static JobType toEnum(String value, JobType defaultEnum) {
		for (JobType v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Get description message string for use in the user interface.
	 *
	 * @return
	 */
	public String getLocalizedDescription() {
		return "jobType.option." + value;
	}

}
