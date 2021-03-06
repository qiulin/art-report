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
package net.sf.jpivotart.jpivot.mondrian;

import java.util.Iterator;
import java.util.List;

import net.sf.mondrianart.mondrian.olap.*;
import net.sf.mondrianart.mondrian.olap.AxisOrdinal.StandardAxisOrdinal;
import net.sf.mondrianart.mondrian.mdx.MemberExpr;
import net.sf.mondrianart.mondrian.mdx.UnresolvedFunCall;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.olap.model.Dimension;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.query.Quax;
import net.sf.jpivotart.jpivot.olap.query.QuaxChangeListener;
import net.sf.jpivotart.jpivot.olap.query.QueryAdapter;

/**
 * Adapt the Mondrian Query Object to the JPivot System.
 */
public class MondrianQueryAdapter extends QueryAdapter implements QuaxChangeListener {

  static Logger logger = Logger.getLogger(MondrianQueryAdapter.class);

  private String originalMDX;
  private Query monQuery = null; // query object representing the current MDX
  private Query cloneQuery = null; // query object representing the original MDX
  private int nAxes; // number of axes

  private final SchemaReader scr;

  /**
   * Constructor
   */
  MondrianQueryAdapter(MondrianModel model, net.sf.mondrianart.mondrian.olap.Query monQuery) {
    super(model);
    this.monQuery = monQuery;
    scr = monQuery.getSchemaReader(true).withLocus();

    genMDXHierarchize = true; // Result hierarchize cannot be used

    // initialize the query axis state objects
    nAxes = monQuery.getAxes().length;
    quaxes = new MondrianQuax[nAxes];
    for (int i = 0; i < monQuery.getAxes().length; i++) {
      net.sf.mondrianart.mondrian.olap.Hierarchy[] monHiers = monQuery.getMdxHierarchiesOnAxis(
        StandardAxisOrdinal.forLogicalOrdinal(i));
      monHiers = MondrianUtil.removeNull(monHiers);
      quaxes[i] = new MondrianQuax(i, monQuery.getAxes()[i], model);
      Hierarchy[] hiers = new Hierarchy[monHiers.length];
      for (int j = 0; j < hiers.length; j++) {
        if (monHiers[j] != null) {
          hiers[j] = model.lookupHierarchy(monHiers[j].getUniqueName());
        }
      }
      quaxes[i].setHiers(hiers);
      quaxes[i].addChangeListener(this);
    }
  }
  public SchemaReader getSchemaReader() {
    return scr;
  }

  /**
   * implement MondrianQuaxChangeListener
   */
  public void quaxChanged(Quax quax, Object source, boolean changedByNavi) {
    useQuax = true;
  }

  /**
   * Returns the monQuery.
   * @return Query
   */
  public Query getMonQuery() {

    if (monQuery != null)
      return monQuery;

    try {
      logger.warn("NOT EXPECTED getMonQuery calling parseQuery");
      MondrianModel mmodel = (MondrianModel) model;
      monQuery = mmodel.getConnection().parseQuery(mmodel.getMdxQuery());
    } catch (Exception ex) {
      // we should never get here
      logger.fatal("getMonQuery parse error", ex);
    }
    return monQuery;
  }

  /**
   * set the monQuery, used for restore
   */
  public void setMonQuery(Query q) {
    this.monQuery = q;
  }

  /**
   * Update the Mondrian Query before Execute.
   * The current query is build from
   * - the original query
   * - adding the drilldown groups
   * - apply pending swap axes
   * - apply pending sorts.
   *
   * Called from MondrianModel.getResult before the query is executed.
   */
  protected void onExecute() {

    // if quax is to be used, generate axes from quax
    if (useQuax) {
      int iQuaxToSort = -1;
      if (sortMan != null)
        iQuaxToSort = sortMan.activeQuaxToSort();

      for (int i = 0; i < quaxes.length; i++) {
        if (quaxes[i].getPosTreeRoot() == null)
          continue;
        boolean doHierarchize = false;
        if (genMDXHierarchize && quaxes[i].isHierarchizeNeeded() && i != iQuaxToSort) {
          doHierarchize = true;
          if (logger.isDebugEnabled())
            logger.debug("MDX Generation added Hierarchize()");
        }

        monQuery.getAxes()[iASwap(i)].setSet((Exp) quaxes[i].genExp(doHierarchize));
      } // for quaxes
    }

    // generate order function if neccessary
    if (sortMan != null) {
      if (!useQuax) {
        // if Quax is used, the axis exp's are re-generated every time.
        // if not -
        //    adding a sort to the query must not be permanent.
        //    Therefore, we clone the orig state of the query object and use
        //    the clone furthermore in order to avoid duplicate "Order" functions.
        if (cloneQuery == null) {
          if (sortMan.isSortOnQuery())
            cloneQuery = monQuery.safeClone();
        } else {
          // reset to original state
          if (sortMan.isSortOnQuery())
            monQuery = cloneQuery.safeClone();
          else
            monQuery = cloneQuery;
        }
      }
      sortMan.addSortToQuery();
    }

    long t1 = System.currentTimeMillis();
    String mdx = monQuery.toString();
    long t2 = System.currentTimeMillis();
    logger.info("monQuery.toString took " + (t2 - t1) + " millisec");
    ((MondrianModel) model).setCurrentMdx(mdx);

    if (logger.isDebugEnabled())
      logger.debug(mdx);

  }

  /**
   * return the corresponding mdx
   */
  protected String getCurrentMdx() {

    String mdx = monQuery.toString();
    return mdx;
  }

  /**
   * create set expression for list of members
   * @param memList
   * @return set expression
   */
  protected Object createMemberSet(List memList) {
    Exp[] exps = new Exp[memList.size()];
    int i = 0;
    for (Iterator iter = memList.iterator(); iter.hasNext();) {
      MondrianMember m = (MondrianMember) iter.next();
      exps[i++] = new MemberExpr(m.getMonMember());
    }
    UnresolvedFunCall f = new UnresolvedFunCall("{}", Syntax.Braces, exps);
    return f;
  }

  // ***************
  // Expand Collapse
  // ***************

  /**
   * find out, whether a member can be expanded.
   * this is true, if
   * - the member is on an axis  and
   * - the member is not yet expanded  and
   * - the member has children
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canExpand(Member)
   * @param member to be expanded
   * @return true if the member can be expanded
   */
  public boolean canExpand(Member member) {
    net.sf.mondrianart.mondrian.olap.Member monMember = ((MondrianMember) member).getMonMember();
    // a calculated member cannot be expanded
    if (monMember.isCalculatedInQuery())
      return false;

    if (!scr.isDrillable(monMember))
      return false;

    Dimension dim = member.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);
    return (quax == null) ? false : quax.canExpand(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canExpand(Member)
   * @param pathMembers members to be expanded
   * @return true if the members can be expanded
   */
  public boolean canExpand(Member[] pathMembers) {

    MondrianMember m = (MondrianMember) pathMembers[pathMembers.length - 1];
    net.sf.mondrianart.mondrian.olap.Member monMember = m.getMonMember();
    // a calculated member cannot be expanded
    if (monMember.isCalculatedInQuery())
      return false;

    if (!scr.isDrillable(monMember))
      return false;

    Dimension dim = m.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);
    return (quax == null) ? false : quax.canExpand(pathMembers);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canCollapse(Member)
   * @param member Member to be collapsed
   * @return true if the member can be collapsed
   */
  public boolean canCollapse(Member member) {

    // a calculated member cannot be collapsed
    if (((MondrianMember) member).getMonMember().isCalculatedInQuery())
      return false;
    Dimension dim = member.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);
    return (quax == null) ? false : quax.canCollapse(member);
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.navi.DrillExpandMember#canCollapse(Member)
   * @param pathMembers members to be expanded
   * @return true if the members can be collapsed
   */
  public boolean canCollapse(Member[] pathMembers) {

    Member member = pathMembers[pathMembers.length - 1];
    // a calculated member cannot be collapsed
    if (((MondrianMember) member).getMonMember().isCalculatedInQuery())
      return false;
    Dimension dim = member.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);
    return (quax == null) ? false : quax.canCollapse(pathMembers);
  }

  /**
   * expand a member in all positions
   *  this is done by applying ToggleDrillState to the Query
   *
   * @param member member to be expanded
   */
  public void expand(Member member) {
    Dimension dim = member.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);

    if (logger.isInfoEnabled())
      logger.info("expand Member" + poString(null, member));
    if ((quax == null) || !quax.canExpand(member)) {
      logger.fatal("Expand Member failed for " + ((MondrianMember) member).getUniqueName());
      //throw new java.lang.IllegalArgumentException("cannot expand");
      return;
    }
    quax.expand(member);
    model.fireModelChanged();
  }

  /**
   * expand members
   *
   * @param pathMembers members to be expanded
   */
  public void expand(Member[] pathMembers) {

    MondrianMember m = (MondrianMember) pathMembers[pathMembers.length - 1];
    Dimension dim = m.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);

    if (logger.isDebugEnabled())
      logger.info("expand Path" + poString(pathMembers, null));
    if ((quax == null) || !quax.canExpand(pathMembers)) {
      logger.fatal("Expand failed for" + poString(pathMembers, null));
      throw new java.lang.IllegalArgumentException("cannot expand");
    }

    quax.expand(pathMembers);
    model.fireModelChanged();
  }

  // ************
  // DrillReplace
  // ************

  /**
   * drill down is possible if <code>member</code> has children
   */
  public boolean canDrillDown(Member member) {
    net.sf.mondrianart.mondrian.olap.Member monMember = ((MondrianMember) member).getMonMember();
    if (!scr.isDrillable(monMember))
      return false;
    Dimension dim = member.getLevel().getHierarchy().getDimension();
    Quax quax = findQuax(dim);
    return (quax == null) ? false : quax.canDrillDown(member);
  }

  // *********
  // Swap Axes
  // *********

  /**
   * swap axes
   *  toggle swap state if neccessary
   */
  public void setSwapAxes(boolean swap) {
    if (monQuery.getAxes().length != 2)
      return;
    // swap axes if neccessary
    if (swap != axesSwapped) {
      monQuery.swapAxes();
      axesSwapped = swap;
      if (logger.isInfoEnabled()) {
        logger.info("swapAxes " + axesSwapped);
      }
      model.fireModelChanged();
    }
  }

} // End MondrianQueryAdapter
