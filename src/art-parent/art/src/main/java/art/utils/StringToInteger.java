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
package art.utils;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * Spring converter for string to integer. To override the default converter
 * which throws an exception with an empty string. This converter converts an
 * empty string to 0.
 *
 * @author Timothy Anyona
 */
public class StringToInteger implements Converter<String, Integer> {

	//for default converter, see http://docs.spring.io/spring/docs/3.0.0.RC2/reference/html/ch05s05.html
	@Override
	public Integer convert(String s) {
		return NumberUtils.toInt(s);
	}
}