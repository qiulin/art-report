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
package net.sf.jpivotart.jpivot.olap.model.impl;

import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Visitor;

/**
 * Created on 11.10.2002
 * 
 * @author av
 */
public class PositionImpl implements Position {
  Member members[];

  /**
   * Returns the members.
   * @return Member[]
   */
  public Member[] getMembers() {
    return members;
  }

  /**
   * Sets the members.
   * @param members The members to set
   */
  public void setMembers(Member[] members) {
    this.members = members;
  }

  public void accept(Visitor visitor) {
    visitor.visitPosition(this);
  }
  
  public Object getRootDecoree() {
    return this;
  }
}
