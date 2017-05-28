/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2009 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.olap.fun;

import net.sf.mondrianart.mondrian.calc.*;
import net.sf.mondrianart.mondrian.calc.impl.AbstractDimensionCalc;
import net.sf.mondrianart.mondrian.mdx.ResolvedFunCall;
import net.sf.mondrianart.mondrian.olap.*;

/**
 * Definition of the <code>&lt;Hierarchy&gt;.Dimension</code> MDX
 * builtin function.
 *
 * @author jhyde
 * @since Mar 23, 2006
 */
public class HierarchyDimensionFunDef extends FunDefBase {
    static final HierarchyDimensionFunDef instance =
        new HierarchyDimensionFunDef();

    private HierarchyDimensionFunDef() {
        super(
            "Dimension",
            "Returns the dimension that contains a specified hierarchy.",
            "pdh");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final HierarchyCalc hierarchyCalc =
                compiler.compileHierarchy(call.getArg(0));
        return new CalcImpl(call, hierarchyCalc);
    }

    public static class CalcImpl extends AbstractDimensionCalc {
        private final HierarchyCalc hierarchyCalc;

        public CalcImpl(Exp exp, HierarchyCalc hierarchyCalc) {
            super(exp, new Calc[] {hierarchyCalc});
            this.hierarchyCalc = hierarchyCalc;
        }

        public Dimension evaluateDimension(Evaluator evaluator) {
            Hierarchy hierarchy =
                    hierarchyCalc.evaluateHierarchy(evaluator);
            return hierarchy.getDimension();
        }
    }
}

// End HierarchyDimensionFunDef.java
