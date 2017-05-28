/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2006 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc;

import net.sf.mondrianart.mondrian.olap.Dimension;
import net.sf.mondrianart.mondrian.olap.Evaluator;

/**
 * Expression which yields a {@link net.sf.mondrianart.mondrian.olap.Dimension}.
 *
 * <p>When implementing this interface, it is convenient to extend
 * {@link net.sf.mondrianart.mondrian.calc.impl.AbstractDimensionCalc}, but it is not required.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public interface DimensionCalc extends Calc {
    /**
     * Evaluates this expression to yield a dimension.
     *
     * <p>Never returns null.
     *
     * @param evaluator Evaluation context
     * @return a dimension
     */
    Dimension evaluateDimension(Evaluator evaluator);
}

// End DimensionCalc.java
