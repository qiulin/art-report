/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2006 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc.impl;

import net.sf.mondrianart.mondrian.calc.Calc;
import net.sf.mondrianart.mondrian.calc.VoidCalc;
import net.sf.mondrianart.mondrian.olap.Evaluator;
import net.sf.mondrianart.mondrian.olap.Exp;

/**
 * Abstract implementation of the {@link net.sf.mondrianart.mondrian.calc.VoidCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateVoid(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it
 * and return <code>null</code>.
 *
 * @author jhyde
 * @since Sep 29, 2005
 */
public class AbstractVoidCalc extends GenericCalc implements VoidCalc {
    private final Calc[] calcs;

    protected AbstractVoidCalc(Exp exp, Calc[] calcs) {
        super(exp);
        this.calcs = calcs;
    }

    public Object evaluate(Evaluator evaluator) {
        evaluateVoid(evaluator);
        return null;
    }

    public Calc[] getCalcs() {
        return calcs;
    }
}

// End AbstractVoidCalc.java
