/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2004-2005 TONBELLER AG
// Copyright (C) 2006-2010 Pentaho and others
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.rolap;

import net.sf.mondrianart.mondrian.olap.Evaluator;
import net.sf.mondrianart.mondrian.rolap.aggmatcher.AggStar;
import net.sf.mondrianart.mondrian.rolap.sql.*;

/**
 * TupleConstraint which does not restrict the result.
 */
public class DefaultTupleConstraint implements TupleConstraint {

    private static final TupleConstraint instance =
        new DefaultTupleConstraint();

    protected DefaultTupleConstraint() {
    }

    public void addConstraint(
        SqlQuery sqlQuery,
        RolapCube baseCube,
        AggStar aggStar)
    {
    }

    public void addLevelConstraint(
        SqlQuery query,
        RolapCube baseCube,
        AggStar aggStar,
        RolapLevel level)
    {
    }

    public MemberChildrenConstraint getMemberChildrenConstraint(
        RolapMember parent)
    {
        return DefaultMemberChildrenConstraint.instance();
    }

    public String toString() {
        return "DefaultTupleConstraint";
    }

    public Object getCacheKey() {
        // we have no state, so all instances are equal
        return this;
    }

    public static TupleConstraint instance() {
        return instance;
    }

    public Evaluator getEvaluator() {
        return null;
    }

}

// End DefaultTupleConstraint.java
