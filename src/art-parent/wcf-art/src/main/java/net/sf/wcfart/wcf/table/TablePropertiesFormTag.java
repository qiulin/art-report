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
package net.sf.wcfart.wcf.table;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.form.FormDocument;
import net.sf.wcfart.wcf.utils.ResourceLocator;
import net.sf.wcfart.wcf.utils.XmlUtils;

/**
 * creates a FormComponent
 * 
 * @author av
 */
public class TablePropertiesFormTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;

  private String xmlUri;
  private String table;
  private boolean bookmarkable = false;
  private boolean closable = true;

  private static Logger logger = Logger.getLogger(TablePropertiesFormTag.class);

  /**
   * loads a form from an xml file and registeres it with the controller.
   */
  public Component createComponent(RequestContext context) throws JspException {
    try {

      URL url = ResourceLocator.getResource(context.getServletContext(), context.getLocale(), xmlUri);
      Document doc = XmlUtils.parse(url);

      //In replaceI18n(...) wird gepr�ft, ob "bundle"-Attribut vorhanden
      FormDocument.replaceI18n(context, doc, null);

      // find the bean model
      TableComponent tc = (TableComponent) context.getModelReference(table);

      // create the component
      TablePropertiesFormComponent fc= new TablePropertiesFormComponent(id, null, doc, tc);
      fc.setCloseable(closable);
      fc.setBookmarkable(bookmarkable);
      return fc;
      
    } catch (MalformedURLException e) {
      logger.error("exception caught", e);
      throw new JspException(e);
    }
  }

  public void setBookmarkable(boolean b) {
    bookmarkable = b;
  }

  public void setTable(String string) {
    table = string;
  }

  public void setXmlUri(String string) {
    xmlUri = string;
  }

  public void setClosable(boolean b) {
    closable = b;
  }

}
