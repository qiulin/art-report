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
package net.sf.jpivotart.jpivot.olap.query;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sf.jpivotart.jpivot.core.Model;
import net.sf.jpivotart.jpivot.olap.model.Axis;
import net.sf.jpivotart.jpivot.olap.model.Cell;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Result;
import net.sf.jpivotart.jpivot.olap.model.Visitor;
import net.sf.jpivotart.jpivot.util.CubeIndexIterator;

/**
 * base class for both Mondrian and XMLA result
 */
public abstract class ResultBase implements Result {

  private static String[] specialProps = { "arrow"};

  protected List<Axis> axesList;

  protected List<Object> aCells;

  protected Axis slicer;

  protected Model model;

  boolean overflow = false;

  // c'tor
  public ResultBase(Model model) {
    aCells = new ArrayList<>();
    axesList = new ArrayList<>();
    this.model = model;
  }

  /**
   * After the result was gotten, handle special measures, which are
   * "invisible". Their meaning is a property for "another" cell, eg.
   * [Measures].[Unit Sales_arrow] is the "arrow" property for
   * [Measures].[Unit Sales]
   */
  void processSpecialProps() {
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Result#getAxes()
   */
  public abstract Axis[] getAxes();

  /**
   * Returns the slicer.
   * 
   * @return Slicer Axis
   * @see net.sf.jpivotart.jpivot.olap.model.Result#getSlicer()
   */
  public Axis getSlicer() {
    return slicer;
  }

  /**
   * Returns the cells.
   * 
   * @return List of cells
   * @see net.sf.jpivotart.jpivot.olap.model.Result#getCells()
   */
  public List<Object> getCells() {
    return aCells;
  }

  /**
   * perform hierarchize not resorting siblings under parent
   * 
   * this method is not fully tested we cannot use Result hierarchize, because
   * by Quax navigation the prerequisits are not given Example (Customers
   * Products): if by Quax navigation (CA, Drink) USA.children is split into
   * {OR, WA) * Drink {CA} * { Drink + Drink.Children) CA would then come
   * later in the result. Therefore : MDX hierarchize is needed
   */
  public void hierarchize(int iAxis) {
    List<Position> posList = axesList.get(iAxis).getPositions();
    int nDim = axesList.size();
    int indexForAxis = nDim - 1 - iAxis;
    int[] ni = new int[nDim];
    int[] iFull = new int[nDim];
    for (int i = 0; i < nDim; i++) {
      ni[i] = axesList.get(i).getPositions().size() - 1;
    }
    int[] iSlice = new int[nDim - 1];
    CubeIndexIterator cubit = null;
    if (nDim > 1) {
      full2slice(ni, iSlice, iAxis);
      cubit = new CubeIndexIterator(iSlice, false);
    }

    // assign the cells
    // c00 c01 c02 ... c0n
    // c10 c11 c12 ... c1n
    // ...
    // cm0 cm1 cm2 ... cmn
    // cell ordinal of cell [i,k] = c[i*(n+1) +k]
    // n+1 = position size of axis 0 (columns)
    // m+1 = position size of axis 1 (rows)
    // iAxis=0 position=0 : c00, c10, ... cm0
    // iAxis=0 position=1 : c01, c11, ... cm1
    // iAxis=1 position=0 : c00, c01, ... c0n
    // iAxis=1 position=1 : c10, c11, ... c1n

    // for each position of iAxis we will get the slice
    //  of cells for the "other" axes

    int iPos = 0;
    int nDimension = 0;
    for (Position position : posList) {
      PositionBase pos = (PositionBase) position;
      if (nDimension == 0)
        nDimension = pos.getMembers().length;
      pos.number = iPos;
      if (pos.cellList == null)
        pos.cellList = new ArrayList<>();
      else
        pos.cellList.clear();
      if (nDim > 1) {
        cubit.reset();
        while (true) {
          int[] iCurrent = cubit.next();
          if (iCurrent == null)
            break;
          slice2full(iCurrent, iFull, indexForAxis, iPos);
          int ii = lindex(iFull, ni);
          pos.cellList.add(aCells.get(ii));
        }
      } else {
        // nDim <= 1
        pos.cellList.add(aCells.get(iPos));
      }
    }

    // sort
    posList = sortPosList(posList, 0, nDim);

    // rewrite cell list
    int nc = aCells.size();
    aCells.clear();
    for (int i = 0; i < nc; i++)
      aCells.add(null);
    iPos = 0;
    for (Iterator<Position> iter = posList.iterator(); iter.hasNext(); iPos++) {
      PositionBase posBase = (PositionBase) iter.next();
      if (nDim > 1) {
        cubit.reset();
        for (Iterator iterator = posBase.cellList.iterator(); iterator.hasNext();) {
          Object cellObj = iterator.next();
          int[] iCurrent = cubit.next();
          if (iCurrent == null)
            break;
          slice2full(iCurrent, iFull, indexForAxis, iPos);
          int ii = lindex(iFull, ni);
          aCells.set(ii, cellObj);
        }
      } else {
        // nDim <= 1
        Object cellObj = posBase.cellList.get(0);
        aCells.set(iPos, cellObj);
      }
      posBase.cellList.clear();
    }

  }

  /**
   * 
   * @param posList
   * @param iDim
   * @param nDim
   * @return sort pos list
   */
  private List<Position> sortPosList(List<Position> posList, final int iDim, int nDim) {

    printPosList(posList, new PrintWriter(System.out), "Start sortPosList " + iDim);

    if (posList.size() < 2)
      return posList;

    // collect members and assign first occurrence prio
    final Map<Member, Integer> firstOcc = new HashMap<>();
    int k = 0;
    for (Iterator iter = posList.iterator(); iter.hasNext(); k++) {
      PositionBase posb = (PositionBase) iter.next();
      posb.parent = null;
      Member m = posb.getMembers()[iDim];
      if (!firstOcc.containsKey(m))
        firstOcc.put(m, k);
    }

    // first step
    // sort by level and original position to assure
    //  that any child follows its parent
    Collections.sort(posList, new Comparator<Position>() {
      public int compare(Position pos1, Position pos2) {
        // compare two positions
        Member a1 = pos1.getMembers()[iDim];
        Member a2 = pos2.getMembers()[iDim];

        // if it is on different level, the descendant is higher
        // otherwise - decide by original index
        int level1 = ((MDXLevel) a1.getLevel()).getDepth();
        int level2 = ((MDXLevel) a2.getLevel()).getDepth();
        if (level1 == level2) {
          return ((PositionBase) pos1).number - ((PositionBase) pos2).number;
        } else {
          return level1 - level2;
        }
      }
    });

    // second step
    // establish parent child dependencies
    int i = 0;
    Outerloop: for (Iterator iter = posList.iterator(); iter.hasNext(); i++) {
      PositionBase posb = (PositionBase) iter.next();
      if (!iter.hasNext())
        break;
      MDXMember m = (MDXMember) posb.getMembers()[iDim];
      int iLevel = ((MDXLevel) m.getLevel()).getDepth();
      ListIterator lit = posList.listIterator(i + 1);
      InnerLoop: while (lit.hasNext()) {
        PositionBase posb2 = (PositionBase) lit.next();
        if (posb2.parent != null)
          continue;
        MDXMember m2 = (MDXMember) posb2.getMembers()[iDim];
        int iLevel2 = ((MDXLevel) m2.getLevel()).getDepth();
        if (iLevel2 <= iLevel)
          continue InnerLoop;
        if (iLevel2 > iLevel + 1)
          break InnerLoop;
        // here iLevel2 = iLevel +1
        if (m.getUniqueName().equals(m2.getParentUniqueName()))
          posb2.parent = posb;
      }
    }

    // third step
    // sort by hierarchy and member first ocurrence
    Collections.sort(posList, new Comparator<Position>() {
      public int compare(Position o1, Position o2) {
        // compare two positions
        PositionBase pos1 = (PositionBase) o1;
        PositionBase pos2 = (PositionBase) o2;
        Member a1 = pos1.getMembers()[iDim];
        Member a2 = pos2.getMembers()[iDim];
        if (a1.equals(a2)) { return pos1.number - pos2.number; }

        // if a1 and a2 are descendant, the descendant is higher
        int level1 = ((MDXLevel) a1.getLevel()).getDepth();
        int level2 = ((MDXLevel) a2.getLevel()).getDepth();
        PositionBase par1 = null;
        PositionBase par2 = null;
        PositionBase parb = null;
        if (level1 < level2) {
          // a2 is possibly descendant of a1
          parb = pos2;
          for (int j = 0; j < level2 - level1; j++) {
            if (parb != null)
              parb = parb.parent;
          }
          if (parb != null) {
            Member ab = parb.getMembers()[iDim];
            if (ab.equals(a1))
              return -1; // a2 is descendant of a1, a2 is higher
          }
          par1 = pos1;
          par2 = parb;
        } else if (level1 > level2) {
          // a1 is possibly descendant of a2
          parb = pos1;
          for (int j = 0; j < level1 - level2; j++) {
            if (parb != null)
              parb = parb.parent;
          }
          if (parb != null) {
            Member ab = parb.getMembers()[iDim];
            if (ab.equals(a2))
              return 1; // a1 is descendant of a2, a1 is higher
          }
          par1 = parb;
          par2 = pos2;

        } else {
          // level1 = level2
          par1 = pos1;
          par2 = pos2;
        }
        // pos1 and pos2 are on equal level
        if (par1 == null || par2 == null)
          return pos1.number - pos2.number; // should not occur
        // go up until we come to a common ancestor or null
        Member apar1 = par1.getMembers()[iDim];
        Member apar2 = par2.getMembers()[iDim];
        PositionBase p1 = par1.parent;
        PositionBase p2 = par2.parent;
        while (p1 != null && p2 != null) {
          Member ap1 = p1.getMembers()[iDim];
          Member ap2 = p2.getMembers()[iDim];
          if (ap1.equals(ap2))
            break;
          par1 = p1;
          par2 = p2;
          p1 = par1.parent;
          p2 = par2.parent;
          if (p1 == null || p2 == null)
            break;
          apar1 = ap1;
          apar2 = ap2;
        }

        int retcode = firstOcc.get(apar1) - firstOcc.get(apar2);

        return retcode;
      }
    });

    printPosList(posList, new PrintWriter(System.out), "Step 3 sortPosList " + iDim);

    // last step
    // sort sublists next hierarchy
    if (iDim == nDim - 1)
      return posList;
    List<Position> newPosList = new ArrayList<>();
    List<Position> subList = new ArrayList<>();
    Member first = null;
    for (Iterator iter = posList.iterator(); iter.hasNext();) {
      PositionBase pb = (PositionBase) iter.next();
      if (first == null) {
        first = pb.getMembers()[iDim];
        subList.add(pb);
      } else {
        Member current = pb.getMembers()[iDim];
        if (current.equals(first)) {
          subList.add(pb);
        } else {
          subList = sortPosList(subList, iDim + 1, nDim);
          newPosList.addAll(subList);
          subList.clear();
          first = current;
          subList.add(pb);
        }
      }
    }
    if (subList.size() > 1) {
      subList = sortPosList(subList, iDim + 1, nDim);
    }
    newPosList.addAll(subList);

    printPosList(newPosList, new PrintWriter(System.out), "End sortPosList " + iDim);

    return newPosList;
  }

  /**
   * print position list for debugging
   */
  private void printPosList(List<Position> posList, PrintWriter wout, String label) {
    wout.println(label);
    int n = 0;
    for (Iterator iter = posList.iterator(); iter.hasNext(); n++) {
      PositionBase pb = (PositionBase) iter.next();
      Member[] members = pb.getMembers();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < members.length; i++) {
        if (i == 0) {
          sb.append(n);
          sb.append(" ");
          sb.append(pb.number);
          sb.append(" ");
        } else {
          sb.append(" * ");
        }

        sb.append(members[i].getLabel());
      }
      wout.println(sb.toString());
    }
    wout.flush();
  }

  /**
   * @param iFull
   *            full index array
   * @param iSlice
   *            slice index array
   * @param iAxis -
   *            index beeing omitted
   */
  private void full2slice(int[] iFull, int[] iSlice, int iAxis) {
    if (iSlice.length == 0)
      return; // 1-dimensional
    int j = 0;
    for (int i = 0; i < iFull.length; i++) {
      if (i != iAxis)
        iSlice[j++] = iFull[i];
    }
  }

  /**
   * @param iFull
   *            full index array
   * @param iSlice
   *            slice index array
   * @param iAxis -
   *            index beeing omitted
   */
  private void slice2full(int[] iSlice, int[] iFull, int iAxis, int iAxisVal) {
    if (iSlice.length == 0) {
      iFull[0] = iAxisVal;
      return;
    }

    int j = 0;
    for (int i = 0; i < iFull.length; i++) {
      if (i != iAxis)
        iFull[i] = iSlice[j++];
      else
        iFull[i] = iAxisVal;
    }
  }

  /**
   * linear index from index array
   * 
   * @param iar
   * @return linear index from index array
   */
  private int lindex(int[] iar, int[] ni) {

    // 3 dim sample
    // c000 c001 ... c00n ck00 ck01 ... ck01
    // c010 c011 ... c01n ---> ck10 ck11 ... ck1n
    // ...
    // c0m1 c0m2 ... c0mn ckm1 ckm2 ckmn

    /* kk*(n+1)*(m+1) + mm*(n+1) +nn */

    int k = iar[0];
    for (int j = 1; j < iar.length; j++)
      k = k * (ni[j - 1] + 1) + iar[j];

    return k;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Decorator#getRootDecoree()
   */
  public Object getRootDecoree() {
    return this;
  }

  /**
   * @see net.sf.jpivotart.jpivot.olap.model.Visitable#accept
   */

  public void accept(Visitor visitor) {
    visitor.visitResult(this);
  }

  /**
   * Render Test Output to HTML
   */
  public static void renderHtml(Result result, String mdx, String outfile) throws IOException {
    int i;

    PrintWriter wout = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));

    Axis[] axes = result.getAxes();

    wout.println("<HTML>");
    wout.println("<HEAD>");
    wout.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\">");
    wout.println("<TITLE>Result from MDX Query</TITLE>");
    wout.println("</HEAD>");
    wout.println("<BODY>");

    wout.println("<h1>MDX Query Result</h1>");

    //String mdx = ((MdxOlapModel) model).getCurrentMdx();
    if (mdx != null ) {
      wout.println("<p>");
      wout.println(mdx);
      wout.println("</p>");
    }
    wout.println("<table border=\"3\">");
    wout.println("<thead><tr>");

    if (axes.length == 0) {
      // result is 0 dimensional
      wout.println("<th>Slicer</th><th>Result</th>");
      wout.println("</thead><tbody><tr>");

      // slicer members as row header
      renderSlicerRowHeader(result.getSlicer(), wout);

      Cell cell = (Cell) result.getCells().get(0);
      String value = cell.getFormattedValue();
      wout.println("<td>" + value + "</td>");
      wout.println("</tr>");
    } else if (axes.length == 1) {
      // result is 1 dimensional
      // print position of axis 0 as column headers
      renderColHeaders(wout, axes[0]);
      wout.println("</tr></thead>");

      wout.println("<tbody><tr>");
      // slicer members as row header
      renderSlicerRowHeader(result.getSlicer(), wout);

      int n = 0;
      for (i = 0; i < axes[0].getPositions().size(); i++) {
        Cell cell = (Cell) result.getCells().get(n++);
        String value = cell.getFormattedValue();
        wout.println("<td>" + value + "</td>");
      }
      wout.println("</tr>");
    } else if( axes.length == 2 ) {
      // assume 2 dimensional
      // print position of axis 0 as column headers
      renderColHeaders(wout, axes[0]);

      wout.println("</tr></thead>");

      // print rows, each one starting with row headers
      wout.println("<tbody>");
      Position[] positions = axes[1].getPositions().toArray(new Position[0]);
      int n = 0;
      for (i = 0; i < positions.length; i++) {
        wout.println("<tr>");
        Member[] members = positions[i].getMembers();

        String caption = "";
        for (int j = 0; j < members.length; j++) {
          if (j > 0)
            caption = caption + "<br>" + members[j].getLabel();
          else
            caption = members[j].getLabel();
        }
        wout.println("<th>" + caption + "</th>");
        for (int j = 0; j < axes[0].getPositions().size(); j++) {
          Cell cell = (Cell) result.getCells().get(n++);
          String value = cell.getFormattedValue();
          wout.println("<td>" + value + "</td>");
        }
        wout.println("</tr>");
      }
    } else {
      // cannot handle more than 2 axes
      throw new IllegalArgumentException("ResultBase.renderHtml cannot handle more than 2 axes");
    }

    wout.println("</tbody>");

    wout.println("</BODY></HTML>");
    wout.close();
  }

  /**
   * print column headers from axis
   */
  private static void renderColHeaders(PrintWriter wout, Axis axis) {
    // print position of axis as column headers
    wout.println("<th></th>");
    Position[] positions = axis.getPositions().toArray(new Position[0]);
    for (int i = 0; i < positions.length; i++) {
      Member[] members = positions[i].getMembers();

      String caption = "";
      for (int j = 0; j < members.length; j++) {
        if (j > 0)
          caption = caption + "<br>" + members[j].getLabel();
        else
          caption = members[j].getLabel();
      }
      wout.println("<th>" + caption + "</th>");
    }
  }

  /**
   * print row header from slicer axis
   */
  private static void renderSlicerRowHeader(Axis slicerax, PrintWriter wout) {
    Position[] positions = slicerax.getPositions().toArray(new Position[0]);
    String caption = "";
    for (int i = 0; i < positions.length; i++) {
      Member[] members = positions[i].getMembers();
      for (int j = 0; j < members.length; j++) {
        if (j == 0 && i == 0)
          caption = members[j].getLabel();
        else
          caption = caption + "<br>" + members[j].getLabel();
      }
    }
    wout.println("<th>" + caption + "</th>");
  }

  /**
   * print Result to print stream
   * 
   * @param ps
   *            Output Print Stream
   */
  public void printOut(java.io.PrintStream ps) {
    Axis[] axes = this.getAxes();
    for (int i = 0; i < axes.length; i++) {
      Axis a = axes[i];
      ps.println("Axis " + i);
      printAxis(ps, a);
    }

    Axis slicer = this.getSlicer();
    ps.println("Slicer Axis ");
    printAxis(ps, slicer);

    int ic = 0;
    for(Object o : aCells){
      Cell c = (Cell) o;
      String val = c.getFormattedValue();
      ps.println("Cell " + ic++ + " Value=" + val);
    }
  }

  /**
   * print axis to standard out
   */
  private void printAxis(java.io.PrintStream ps, Axis a) {
    List positions = a.getPositions();
    Iterator it = positions.iterator();

    int ip = 0;
    while (it.hasNext()) {
      Position p = (Position) it.next();
      ps.println("Position " + ip++);
      Member[] members = p.getMembers();
      for (int j = 0; j < members.length; j++) {
        Member m = members[j];
        String mcap = m.getLabel();
        int idep = m.getRootDistance();
        ps.println("Member " + mcap + " depth=" + idep);
      }
    }
  }

  /**
   * @return true, if the result was rolled back due to overflow condition
   */
  public boolean isOverflowOccured() {
    return overflow;
  }
  
  public void setOverflowOccured(boolean overflow) {
    this.overflow = overflow;
  }

} // ResultBase
