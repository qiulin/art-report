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

import art.servlets.Config;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FontSelector;
import java.util.Objects;

/**
 * Provides common methods and variables used in pdf generation
 *
 * @author Timothy Anyona
 */
public class PdfHelper {

	public final String PDF_AUTHOR_ART = "ART - http://art.sourceforge.net";

	/**
	 * Sets font selector objects to be used for body text and header text
	 *
	 * @param body the font selector for body text, not null
	 * @param header the font selector for header text, not null
	 */
	public void setFontSelectors(FontSelector body, FontSelector header) {
		//use fontselector and potentially custom fonts with specified encoding
		//to enable display of more non-ascii characters
		//first font added to selector wins
		
		Objects.requireNonNull(body, "body must not be null");
		Objects.requireNonNull(header, "header must not be null");

		//use custom font if defined			
		if (Config.isUseCustomPdfFont()) {
			String fontName = Config.getSettings().getPdfFontName();
			String encoding = Config.getSettings().getPdfFontEncoding();
			boolean embedded = Config.getSettings().isPdfFontEmbedded();

			Font bodyFont = FontFactory.getFont(fontName, encoding, embedded);
			bodyFont.setSize(8);
			bodyFont.setStyle(Font.NORMAL);
			body.addFont(bodyFont);

			Font headingFont = FontFactory.getFont(fontName, encoding, embedded);
			headingFont.setSize(10);
			headingFont.setStyle(Font.BOLD);
			header.addFont(headingFont);
		}

		//add default font after custom font			
		body.addFont(FontFactory.getFont(BaseFont.HELVETICA, 8, Font.NORMAL));
		header.addFont(FontFactory.getFont(BaseFont.HELVETICA, 10, Font.BOLD));
	}

}