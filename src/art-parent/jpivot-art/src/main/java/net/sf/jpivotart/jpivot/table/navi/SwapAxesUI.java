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
package net.sf.jpivotart.jpivot.table.navi;

import net.sf.jpivotart.jpivot.core.ModelChangeEvent;
import net.sf.jpivotart.jpivot.core.ModelChangeListener;
import net.sf.jpivotart.jpivot.olap.navi.SwapAxes;
import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.jpivotart.jpivot.table.TableComponentExtensionSupport;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * swaps table axes. The toolbar button is connected to the boolean action property.
 * 
 * @author av
 */
public class SwapAxesUI extends TableComponentExtensionSupport implements ModelChangeListener {
  SwapAxes extension;

  public static final String ID = "swapAxes";
  public String getId() {
    return ID;
  }

  public void initialize(RequestContext context, TableComponent table) throws Exception {
    super.initialize(context, table);
    table.getOlapModel().addModelChangeListener(this);
    extension = getExtension();
  }

  public boolean isButtonPressed() {
    return extension != null && extension.isSwapAxes();
  }

  public void setButtonPressed(boolean value) {
    if (extension != null && extension.canSwapAxes()) {
      extension.setSwapAxes(value);
    }
  }

  public boolean isAvailable() {
    return getExtension() != null;
  }

  public void startBuild(RequestContext context) {
    super.startBuild(context);
  }

  private SwapAxes getExtension() {
    return (SwapAxes) table.getOlapModel().getExtension(SwapAxes.ID);
  }

  public void modelChanged(ModelChangeEvent e) {
  }

  public void structureChanged(ModelChangeEvent e) {
    extension = getExtension();
  }

}
