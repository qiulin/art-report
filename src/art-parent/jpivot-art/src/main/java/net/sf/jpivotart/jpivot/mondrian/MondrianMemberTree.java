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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.mondrianart.mondrian.olap.ResultLimitExceededException;
import net.sf.mondrianart.mondrian.olap.SchemaReader;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.model.Axis;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Result;
import net.sf.jpivotart.jpivot.olap.navi.MemberTree;
import net.sf.jpivotart.jpivot.olap.query.Quax;

/**
 * Implementation of the DrillExpand Extension for Mondrian Data Source.
 */
public class MondrianMemberTree extends ExtensionSupport implements MemberTree {

  static Logger logger = Logger.getLogger(MondrianMemberTree.class);

  /**
   * Constructor sets ID
   */
  public MondrianMemberTree() {
    super.setId(MemberTree.ID);
  }

  /**
   * @return the root members of a hierarchy. This is for example the "All"
   *         member or the list of measures.
   */
  public Member[] getRootMembers(Hierarchy hier) {
    try {
      return internalGetRootMembers(hier);
    } catch (ResultLimitExceededException e) {
      logger.error(null, e);
      throw new TooManyMembersException(e);
    }
  }

  private Member[] internalGetRootMembers(Hierarchy hier) {
    MondrianModel model = (MondrianModel) getModel();
    net.sf.mondrianart.mondrian.olap.Hierarchy monHier = ((MondrianHierarchy) hier).getMonHierarchy();
    net.sf.mondrianart.mondrian.olap.Query q = ((MondrianQueryAdapter) model.getQueryAdapter()).getMonQuery();
    // Use the schema reader from the query, because it contains calculated
    // members defined in both the cube and the query.
    SchemaReader scr = model.getSchemaReader();
    List<net.sf.mondrianart.mondrian.olap.Member> monMembers = scr.getHierarchyRootMembers(monHier);
    ArrayList<Member> aMem = new ArrayList<>();
    final List<Member> visibleRootMembers = new ArrayList<>();
    int k = monMembers.size();
    for (int i = 0; i < k; i++) {
      net.sf.mondrianart.mondrian.olap.Member monMember = (net.sf.mondrianart.mondrian.olap.Member) monMembers.get(i);
      if (isVisible(monMember)) {
        aMem.add(model.addMember(monMember));
      }
    }


    // find the calculated members for this hierarchy
    //  show them together with root level members
     net.sf.mondrianart.mondrian.olap.Formula[] formulas = q.getFormulas();
    for (int i = 0; i < formulas.length; i++) {
      net.sf.mondrianart.mondrian.olap.Formula f = formulas[i];
      net.sf.mondrianart.mondrian.olap.Member monMem = f.getMdxMember();
      if (monMem != null) {
        // is the member for this hierarchy,
        // and is it visible?
        // if yes add it
        if (monMem.getHierarchy().equals(monHier)) {
          if (!isVisible(monMem))
            continue;
          // ADVR MOD 2008.12.15
          // Changed to only add root calculated members (parent==null) to the root.
          // Other members will be added at the correct place within the
          // respective hierarchy
          
          // find the parent for this member
          if (monMem.getParentMember()== null){
              Member m = model.addMember(monMem);
              if (!aMem.contains(m))
                aMem.add(m);
          }
        }
      }
    }

    // order members according to occurrence in query result
    //  if there is no result available, do not sort
    Result res = model.currentResult();
    if (res != null) {
        // locate the appropriate result axis
        // find the Quax for this hier
        MondrianQueryAdapter adapter = (MondrianQueryAdapter) model.getQueryAdapter();
        Quax quax = adapter.findQuax(hier.getDimension());
        if (quax != null) {
            int iDim = quax.dimIdx(hier.getDimension());
            int iAx = quax.getOrdinal();
            if (adapter.isSwapAxes())
              iAx = (iAx + 1) % 2;
            Axis axis = res.getAxes()[iAx];
            List positions = axis.getPositions();

            for (Iterator iter = positions.iterator(); iter.hasNext();) {
              Position pos = (Position) iter.next();
              Member[] posMembers = pos.getMembers();
              MondrianMember mem = (MondrianMember) posMembers[iDim];
              // only add hierarchy items from the query results
              // if they are actually in in the currently expanding hierarchy!!
              if (mem.getMonMember().getHierarchy().equals(monHier)) {             
                if (!(mem.getMonMember().getParentMember() == null))
                  continue; // ignore, not root
                if (!visibleRootMembers.contains(mem))
                  visibleRootMembers.add(mem);
  
                // Check if the result axis contains invisible members
                if (!aMem.contains(mem)) {
                    aMem.add(mem);
                }
              }
            }
        }
    }

    Member[] members = aMem.toArray(new Member[0]);

    // If there is no query result, do not sort
    if (!visibleRootMembers.isEmpty()) {
        Arrays.sort(members, new Comparator<Member>() {
          public int compare(Member m1, Member m2) {
            int index1 = visibleRootMembers.indexOf(m1);
            int index2 = visibleRootMembers.indexOf(m2);
            if (index2 == -1)
              return -1; // m2 is higher, unvisible to the end
            if (index1 == -1)
              return 1; // m1 is higher, unvisible to the end
            return index1 - index2;
          }
        });
    }

    return members;
  }

  private boolean isVisible(net.sf.mondrianart.mondrian.olap.Member monMember) {
    // Name convention: if member starts with "." its hidden
    if (monMember.getName().startsWith("."))
      return false;

    MondrianModel model = (MondrianModel) getModel();
    // Use the schema reader from the query, because it contains calculated
    // members defined in both the cube and the query.
    SchemaReader scr = model.getSchemaReader();

    return MondrianUtil.isVisible(scr, monMember);
  }

  /**
   * @return true if the member has children
   */
  public boolean hasChildren(Member member) {
    net.sf.mondrianart.mondrian.olap.Member monMember = ((MondrianMember) member).getMonMember();
    if (monMember.isCalculatedInQuery())
      return false;

    if (monMember.getLevel().getChildLevel() != null){
       if (!monMember.isCalculated()) return true;
       // ADVR 2010.07.27
       // for calculated we need to actually check if there are any children.
       return this.getChildren(member).length!=0;

    }

    // here for a leaf-level, but also for a level in a parent-child hierarchy:
    MondrianModel model = (MondrianModel) getModel();

    SchemaReader scr = model.getSchemaReader();
    return scr.isDrillable(monMember);
  }

  /**
   * @return the children of the member
   */
  public Member[] getChildren(Member member) {
    try {
      return internalGetChildren(member);
    } catch (ResultLimitExceededException e) {
      logger.error(null, e);
      throw new TooManyMembersException(e);
    }
  }

  private Member[] internalGetChildren(Member member) {
    net.sf.mondrianart.mondrian.olap.Member monMember = ((MondrianMember) member).getMonMember();
    //  unreliable: always null in a parent-child hierarch
    // if (monMember.getLevel().getChildLevel() == null)
    //   return null;

    MondrianModel model = (MondrianModel) getModel();

    SchemaReader scr = model.getSchemaReader();    
    List<net.sf.mondrianart.mondrian.olap.Member> monChildren = scr.getMemberChildren(monMember);

    List<Member> list = new ArrayList<>(monChildren.size());
    for (int i = 0; i < monChildren.size(); i++) {
        net.sf.mondrianart.mondrian.olap.Member m = (net.sf.mondrianart.mondrian.olap.Member)monChildren.get(i);
        if (MondrianUtil.isVisible(scr, m)) {
            list.add(model.addMember(m));
        }
    }

    // ADVR MOD 2008.12.15
    // check for calculated members that belong to this level

    net.sf.mondrianart.mondrian.olap.Query q = ((MondrianQueryAdapter) model.getQueryAdapter()).getMonQuery();

    // find the calculated members for this hierarchy
    //  show them together with level members
    net.sf.mondrianart.mondrian.olap.Formula[] formulas = q.getFormulas();
    for (int i = 0; i < formulas.length; i++) {
      net.sf.mondrianart.mondrian.olap.Formula f = formulas[i];
      net.sf.mondrianart.mondrian.olap.Member monMem = f.getMdxMember();
      if (monMem != null) {
        // is the member for this hierarchy,
        // and is it visible?
        // if yes add it
        if (!isVisible(monMem))
            continue;
        if (monMem.getDimension().equals(monMember.getDimension()) &&
            monMem.getHierarchy().equals(monMember.getHierarchy())) {

          // If this calculated member is a child of this current member,
          //  add it to the child list
          if ( monMem.getParentMember().equals(monMember)){
              Member m = model.addMember(monMem);
              if (!list.contains(m))
                list.add(m);
          }
        }
      }
    }
    // ADVR 2010.07.20
    // order the children by order of appearance in Query result
    //  if there is no result available, do not sort
    Result res = model.currentResult();
    final List<Member> visibleChildMembers = new ArrayList<>();
    if (res != null) {
        // locate the appropriate result axis
        // find the Quax for this hier
        MondrianQueryAdapter adapter = (MondrianQueryAdapter) model.getQueryAdapter();
        net.sf.mondrianart.mondrian.olap.Hierarchy monHier = monMember.getHierarchy();
        Hierarchy hier = member.getLevel().getHierarchy();

        Quax quax = adapter.findQuax(hier.getDimension());
        if (quax != null) {
            int iDim = quax.dimIdx(hier.getDimension());
            int iAx = quax.getOrdinal();
            if (adapter.isSwapAxes())
              iAx = (iAx + 1) % 2;
            Axis axis = res.getAxes()[iAx];
            List positions = axis.getPositions();

            for (Iterator iter = positions.iterator(); iter.hasNext();) {
              Position pos = (Position) iter.next();
              Member[] posMembers = pos.getMembers();
              MondrianMember mem = (MondrianMember) posMembers[iDim];
              // only add hierarchy items from the query results
              // if they are actually in in the currently expanding hierarchy!!
              if (mem.getMonMember().getParentMember()==null)
                  continue; // skip root members - can't be children
              if (mem.getMonMember().getHierarchy().equals(monHier)) {
                if (mem.getMonMember().getParentMember().equals(monMember)){                     
                    visibleChildMembers.add(mem);

                    // Check if the result axis contains invisible members
                    if (!list.contains(mem)) {
                        list.add(mem);
                    }
                 }
              }
            }
        }
    }
    Member[] children = list.toArray(new Member[list.size()]);

    if (res!=null){  //turned off
        Arrays.sort(children, new Comparator<Member>() {
          public int compare(Member m1, Member m2) {
            int index1 = visibleChildMembers.indexOf(m1);
            int index2 = visibleChildMembers.indexOf(m2);
            if (index2 == -1)
              return -1; // m2 is higher, unvisible to the end
            if (index1 == -1)
              return 1; // m1 is higher, unvisible to the end
            return index1 - index2;
          }
        });
    }
    
    return children;
  }

  /**
   * @return the parent of member or null, if this is a root member
   */
  public Member getParent(Member member) {
    net.sf.mondrianart.mondrian.olap.Member monMember = ((MondrianMember) member).getMonMember();

    MondrianModel model = (MondrianModel) getModel();

    SchemaReader scr = model.getSchemaReader();
    net.sf.mondrianart.mondrian.olap.Member monParent = scr.getMemberParent(monMember);
    if (monParent == null)
      return null; // already top level
    Member parent = model.addMember(monParent);

    return parent;
  }

} // End MondrianMemberTree
