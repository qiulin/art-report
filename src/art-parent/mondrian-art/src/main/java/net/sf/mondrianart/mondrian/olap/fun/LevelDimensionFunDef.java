/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2009-2009 Pentaho and others
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.olap.fun;

import net.sf.mondrianart.mondrian.calc.*;
import net.sf.mondrianart.mondrian.calc.impl.AbstractDimensionCalc;
import net.sf.mondrianart.mondrian.mdx.ResolvedFunCall;
import net.sf.mondrianart.mondrian.olap.*;

/**
 * Definition of the <code>&lt;Level&gt;.Dimension</code>
 * MDX builtin function.
 *
 * @author jhyde
 * @since Jul 20, 2009
 */
class LevelDimensionFunDef extends FunDefBase {
    public static final FunDefBase INSTANCE = new LevelDimensionFunDef();

    public LevelDimensionFunDef() {
        super(
            "Dimension",
            "Returns the dimension that contains a specified level.", "pdl");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler)
    {
        final LevelCalc levelCalc =
            compiler.compileLevel(call.getArg(0));
        return new AbstractDimensionCalc(call, new Calc[] {levelCalc}) {
            public Dimension evaluateDimension(Evaluator evaluator) {
                Level level =  levelCalc.evaluateLevel(evaluator);
                return level.getDimension();
            }
        };
    }
}

// End LevelDimensionFunDef.java
