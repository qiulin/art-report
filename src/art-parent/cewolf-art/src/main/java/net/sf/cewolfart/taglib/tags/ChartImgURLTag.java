/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.cewolfart.taglib.tags;

import net.sf.cewolfart.CewolfException;
import net.sf.cewolfart.Configuration;
import net.sf.cewolfart.Storage;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tag only renders the image URL instead of a full &lt;img&gt; tag.
 * @author glaures
 * @see net.sf.cewolfart.taglib.tags.ChartImgTag
 */
public class ChartImgURLTag extends ChartImgTag {

	private static final Logger logger = LoggerFactory.getLogger(ChartImgURLTag.class);
	
	static final long serialVersionUID = -8566975311205247052L;

	public static final String VAR_NAME = "var";

	String var = null;
    
    public int doEndTag() throws JspException {
        try {
        	if (var == null) {
	            pageContext.getOut().write(getImgURL());
        	} else {
        		pageContext.setAttribute(var, getImgURL());
        	}
        } catch(IOException ioex){
        	logger.error("Error",ioex);
            throw new JspException(ioex.getMessage());
        }

		try {
			Storage storage = Configuration.getInstance(pageContext.getServletContext()).getStorage();
			storage.removeChartImage(sessionKey, (HttpServletRequest) pageContext.getRequest());
		} catch (CewolfException cwex) {
			throw new JspException(cwex.getMessage());
		}

    	return doAfterEndTag(EVAL_PAGE);
    }
    
	/**
	 * @see net.sf.cewolfart.taglib.tags.CewolfBodyTag#reset()
	 */
	protected void reset() {
		super.reset();
		var = null;
	}

	/**
	 * Returns the var.
	 * @return String
	 */
	public String getVar() {
		return var;
	}

	/**
	 * Sets the var.
	 * @param var The var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}

}
