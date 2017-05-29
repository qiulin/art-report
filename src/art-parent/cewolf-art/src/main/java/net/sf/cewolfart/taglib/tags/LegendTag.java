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

import java.io.IOException;

import javax.servlet.jsp.JspException;

import net.sf.cewolfart.CewolfException;
import net.sf.cewolfart.ChartHolder;
import net.sf.cewolfart.ChartImage;
import net.sf.cewolfart.Configuration;
import net.sf.cewolfart.Storage;
import net.sf.cewolfart.WebConstants;
import net.sf.cewolfart.taglib.ChartImageDefinition;
import net.sf.cewolfart.taglib.TaglibConstants;
import net.sf.cewolfart.taglib.html.HTMLImgTag;
import net.sf.cewolfart.taglib.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tag &lt;legend&gt; which can be used to render a chart's legend
 * separately somewhere in the page.
 * @author  Guido Laures
 */
public class LegendTag extends HTMLImgTag implements CewolfRootTag {

    private static final Logger logger = LoggerFactory.getLogger(LegendTag.class);

	static final long serialVersionUID = 3011526353897699906L;

    private static final String DEFAULT_MIME_TYPE = WebConstants.MIME_PNG;
    private static final int DEFAULT_TIMEOUT = 300;
    private int timeout = DEFAULT_TIMEOUT;
    private transient String sessionKey;
    private transient String renderer;
    private String mimeType = DEFAULT_MIME_TYPE;

    public int doStartTag() throws JspException {
        ChartHolder cd = PageUtils.getChartHolder(getChartId(), pageContext);
        ChartImage cid = new ChartImageDefinition(cd, width, height, ChartImage.IMG_TYPE_LEGEND, mimeType, timeout);
        Storage storage = Configuration.getInstance(pageContext.getServletContext()).getStorage();
        try {
        	this.sessionKey = storage.storeChartImage(cid, pageContext);
        } catch(CewolfException cwex){
        	logger.error("Error",cwex);
        	throw new JspException(cwex.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
		super.doStartTag();
		final StringBuffer buffer = new StringBuffer(" src=\"");
		buffer.append(getImgURL());
		buffer.append("\"");
		try {
			pageContext.getOut().write(buffer.toString());
		} catch (IOException ioex) {
			reset();
        	logger.error("Error",ioex);
			throw new JspException(ioex.getMessage());
		}
		return super.doEndTag();
    }

	/**
	* To enable further server side scriptings on JSP output the session ID is always
	* encoded into the image URL even if cookies are enabled on the client side.
	*/
	protected String getImgURL() {
		return ChartImgTag.buildImgURL(renderer, pageContext, sessionKey, width, height, mimeType, forceSessionId, removeAfterRender);
	}

    protected void reset() {
        // as of a weird JSP compiler in resin
        // a reused tag's attribute is only set if 
        // it changes. So width an height may not
        // be unset to ensure correct values.
        int lHeight = this.height;
        int lWidth = this.width;
        int lTimeout = this.timeout;
        super.reset();
        this.height = lHeight;
        this.width = lWidth;
        this.timeout = lTimeout;
    }

    public String getChartId() {
        return getId();
    }

    public void setRenderer(String rend) {
        this.renderer = rend;
    }

    /**
     * Sets the mimeType.
     * @param mimeType The mimeType to set
     */
    public void setMime(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return Returns the timeout.
     */
    public int getTimeout()
    {
      return timeout;
    }
    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout( int timeout )
    {
      this.timeout = timeout;
    }
}
