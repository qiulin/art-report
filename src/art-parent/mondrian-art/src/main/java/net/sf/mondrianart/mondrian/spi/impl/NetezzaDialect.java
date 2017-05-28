/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2008-2009 Jaspersoft
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of {@link net.sf.mondrianart.mondrian.spi.Dialect} for the Netezza database.
 *
 * @author swood
 * @since April 17, 2009
 */
public class NetezzaDialect extends PostgreSqlDialect {

    public static final JdbcDialectFactory FACTORY =
        new JdbcDialectFactory(
            NetezzaDialect.class,
            DatabaseProduct.NETEZZA);

    /**
     * Creates a NetezzaDialect.
     *
     * @param connection Connection
     */
    public NetezzaDialect(Connection connection) throws SQLException {
        super(connection);
    }

}
// End NetezzaDialect.java