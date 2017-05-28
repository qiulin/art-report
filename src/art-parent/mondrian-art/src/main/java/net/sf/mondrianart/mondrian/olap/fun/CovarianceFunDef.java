/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2011 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.olap.fun;

import net.sf.mondrianart.mondrian.calc.*;
import net.sf.mondrianart.mondrian.calc.impl.AbstractDoubleCalc;
import net.sf.mondrianart.mondrian.calc.impl.ValueCalc;
import net.sf.mondrianart.mondrian.mdx.ResolvedFunCall;
import net.sf.mondrianart.mondrian.olap.*;

/**
 * Definition of the <code>Covariance</code> and
 * <code>CovarianceN</code> MDX functions.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
class CovarianceFunDef extends FunDefBase {
    static final ReflectiveMultiResolver CovarianceResolver =
        new ReflectiveMultiResolver(
            "Covariance",
            "Covariance(<Set>, <Numeric Expression>[, <Numeric Expression>])",
            "Returns the covariance of two series evaluated over a set (biased).",
            new String[]{"fnxn", "fnxnn"},
            CovarianceFunDef.class);

    static final MultiResolver CovarianceNResolver =
        new ReflectiveMultiResolver(
            "CovarianceN",
            "CovarianceN(<Set>, <Numeric Expression>[, <Numeric Expression>])",
            "Returns the covariance of two series evaluated over a set (unbiased).",
            new String[]{"fnxn", "fnxnn"},
            CovarianceFunDef.class);

    private final boolean biased;

    public CovarianceFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
        this.biased = dummyFunDef.getName().equals("Covariance");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final ListCalc listCalc =
            compiler.compileList(call.getArg(0));
        final Calc calc1 =
            compiler.compileScalar(call.getArg(1), true);
        final Calc calc2 =
            call.getArgCount() > 2
            ? compiler.compileScalar(call.getArg(2), true)
            : new ValueCalc(call);
        return new AbstractDoubleCalc(call, new Calc[] {listCalc, calc1, calc2})
        {
            public double evaluateDouble(Evaluator evaluator) {
                TupleList memberList = listCalc.evaluateList(evaluator);
                final int savepoint = evaluator.savepoint();
                evaluator.setNonEmpty(false);
                final double covariance =
                    (Double) covariance(
                        evaluator,
                        memberList,
                        calc1,
                        calc2,
                        biased);
                evaluator.restore(savepoint);
                return covariance;
            }

            public boolean dependsOn(Hierarchy hierarchy) {
                return anyDependsButFirst(getCalcs(), hierarchy);
            }
        };
    }
}

// End CovarianceFunDef.java
