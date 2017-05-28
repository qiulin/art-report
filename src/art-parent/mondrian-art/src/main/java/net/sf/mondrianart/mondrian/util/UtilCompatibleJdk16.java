/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2011-2011 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.util;

import net.sf.mondrianart.mondrian.olap.Util;
import net.sf.mondrianart.mondrian.resource.MondrianResource;
import net.sf.mondrianart.mondrian.rolap.RolapUtil;
import net.sf.mondrianart.mondrian.rolap.RolapUtil.RolapUtilComparable;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.script.*;

// Only in Java6 and above

/**
 * Implementation of {@link net.sf.mondrianart.mondrian.util.UtilCompatible} that runs in
 * JDK 1.6.
 *
 * <p>Prior to JDK 1.6, this class should never be loaded. Applications should
 * instantiate this class via {@link Class#forName(String)} or better, use
 * methods in {@link net.sf.mondrianart.mondrian.olap.Util}, and not instantiate it at all.
 *
 * @author jhyde
 */
public class UtilCompatibleJdk16 extends UtilCompatibleJdk15 {
    private static final Logger LOGGER =
        Logger.getLogger(Util.class);

    public <T> T compileScript(
        Class<T> iface,
        String script,
        String engineName)
    {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName(engineName);
        try {
            engine.eval(script);
            Invocable inv = (Invocable) engine;
            return inv.getInterface(iface);
        } catch (ScriptException e) {
            throw Util.newError(
                e,
                "Error while compiling script to implement " + iface + " SPI");
        }
    }

    @Override
    public void cancelAndCloseStatement(Statement stmt) {
        try {
            if (!stmt.isClosed()) {
                stmt.cancel();
            }
        } catch (SQLException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                    MondrianResource.instance()
                        .ExecutionStatementCleanupException
                            .ex(e.getMessage(), e),
                    e);
            }
        }
        try {
            if (!stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                    MondrianResource.instance()
                        .ExecutionStatementCleanupException
                            .ex(e.getMessage(), e),
                    e);
            }
        }
    }

    @Override
    public <T> Set<T> newIdentityHashSet() {
        return Collections.newSetFromMap(
            new IdentityHashMap<T, Boolean>());
    }

    public <T extends Comparable<T>> int binarySearch(
        T[] ts, int start, int end, T t)
    {
        return Arrays.binarySearch(
            ts, start, end, t,
            RolapUtil.ROLAP_COMPARATOR);
    }
}

// End UtilCompatibleJdk16.java
