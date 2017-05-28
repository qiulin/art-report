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
package net.sf.jpivotart.jpivot.xmla;

import java.io.Serializable;
import java.util.Map;

import net.sf.jpivotart.jpivot.olap.query.Memento;

/**
 * Java Bean object to hold the state of an XMLA MDX session.
 * Contains parts of XMLA_Model and subordinate objects.
 */
public class XMLA_Memento extends Memento implements Serializable {

  static final int CURRENT_VERSION = 1;
  int version;
  private String uri = null;
  private String user = null;
  private String password = null;
  private String catalog = null;
  private String dataSource = null;
  
  private Map calcMeasurePropMap = null;

  /**
   * @return datasource
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * @return password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * @return user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param string
   */
  public void setDataSource(String string) {
    dataSource = string;
  }

  /**
   * @param string
   */
  public void setPassword(String string) {
    password = string;
  }

  /**
   * @param string
   */
  public void setUri(String string) {
    uri = string;
  }

  /**
   * @param string
   */
  public void setUser(String string) {
    user = string;
  }

  /**
   * @return version
   */
  public int getVersion() {
    return version;
  }

  /**
   * @param i
   */
  public void setVersion(int i) {
    version = i;
  }

  /**
   * @return catalog
   */
  public String getCatalog() {
    return catalog;
  }

  /**
   * @param string
   */
  public void setCatalog(String string) {
    catalog = string;
  }

  /**
   * @return calc measure prop map
   */
  public Map getCalcMeasurePropMap() {
    return calcMeasurePropMap;
  }

  /**
   * @param map
   */
  public void setCalcMeasurePropMap(Map map) {
    calcMeasurePropMap = map;
  }

} // XMLA_Memento
