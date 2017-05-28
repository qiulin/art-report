/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package net.sf.wcfart.wcf.form;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.utils.ResourceLocator;
import net.sf.wcfart.wcf.utils.XmlUtils;

/**
 * creates a ConfirmComponent
 * 
 * @author av
 */
public class ConfirmComponentTag extends ComponentTag {
  private static final Logger logger = Logger.getLogger(ConfirmComponentTag.class);
  
  private static final long serialVersionUID = 1L;

  private String xmlUri;
  
  public ConfirmComponentTag() {
    super.id = ConfirmComponent.SESSIONKEY;
  }
  public void release() {
    super.release();
    super.id = ConfirmComponent.SESSIONKEY;
  }

  /**
   * loads a form from an xml file and registeres it with the controller.
   */
  public Component createComponent(RequestContext context) throws JspException {
    try {
      String xmlUri1 = (xmlUri != null) ? xmlUri : "/WEB-INF/wcf/confirm.xml";
      URL url = ResourceLocator.getResource(context.getServletContext(), context.getLocale(),
          xmlUri1);
      Document doc = XmlUtils.parse(url);
      
      //In replaceI18n(...) wird gepr�ft, ob "bundle"-Attribut vorhanden
      FormDocument.replaceI18n(context, doc, null);
      
      // create the component
      return new ConfirmComponent(id, null, doc);
    } catch (MalformedURLException e) {
      logger.error(null, e);
      throw new JspException(e);
    }
  }

  /**
   * Sets the xmlUri.
   * @param xmlUri The xmlUri to set
   */
  public void setXmlUri(String xmlUri) {
    this.xmlUri = xmlUri;
  }

}
