/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2010-2011 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.parser;

import net.sf.mondrianart.mondrian.olap.*;
import net.sf.mondrianart.mondrian.server.Statement;

/**
 * Default implementation of {@link net.sf.mondrianart.mondrian.parser.MdxParserValidator}.
 *
 * @author jhyde
 */
public class MdxParserValidatorImpl implements MdxParserValidator {
    /**
     * Creates a MdxParserValidatorImpl.
     */
    public MdxParserValidatorImpl() {
    }

    public QueryPart parseInternal(
        Statement statement,
        String queryString,
        boolean debug,
        FunTable funTable,
        boolean strictValidation)
    {
        return new Parser().parseInternal(
            new Parser.FactoryImpl(),
            statement, queryString, debug, funTable, strictValidation);
    }

    public Exp parseExpression(
        Statement statement,
        String queryString,
        boolean debug,
        FunTable funTable)
    {
        return new Parser().parseExpression(
            new Parser.FactoryImpl(),
            statement, queryString, debug, funTable);
    }
}

// End MdxParserValidatorImpl.java
