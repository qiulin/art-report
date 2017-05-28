/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2010-2012 Pentaho and others
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.rolap.agg;


import net.sf.mondrianart.mondrian.rolap.CellKey;
import net.sf.mondrianart.mondrian.rolap.SqlStatement;
import net.sf.mondrianart.mondrian.spi.SegmentBody;
import net.sf.mondrianart.mondrian.util.Pair;

import java.util.*;

/**
 * Implementation of {@link net.sf.mondrianart.mondrian.rolap.agg.DenseSegmentDataset} that stores
 * values of type {@code double}.
 *
 * @author jhyde
 */
class DenseDoubleSegmentDataset extends DenseNativeSegmentDataset {
    final double[] values; // length == m[0] * ... * m[axes.length-1]

    /**
     * Creates a DenseDoubleSegmentDataset.
     *
     * @param axes Segment axes, containing actual column values
     * @param size Number of coordinates
     */
    DenseDoubleSegmentDataset(SegmentAxis[] axes, int size) {
        this(axes, new double[size], new BitSet(size));
    }

    /**
     * Creates a populated DenseDoubleSegmentDataset.
     *
     * @param axes Segment axes, containing actual column values
     * @param values Cell values; not copied
     * @param nullIndicators Null indicators
     */
    DenseDoubleSegmentDataset(
        SegmentAxis[] axes, double[] values, BitSet nullIndicators)
    {
        super(axes, nullIndicators);
        this.values = values;
    }

    public double getDouble(CellKey key) {
        int offset = key.getOffset(axisMultipliers);
        return values[offset];
    }

    public Object getObject(CellKey pos) {
        int offset = pos.getOffset(axisMultipliers);
        return getObject(offset);
    }

    public Double getObject(int offset) {
        final double value = values[offset];
        if (value == 0 && isNull(offset)) {
            return null;
        }
        return value;
    }

    public boolean exists(CellKey pos) {
        return true;
    }

    public void populateFrom(int[] pos, SegmentDataset data, CellKey key) {
        final int offset = getOffset(pos);
        double value = values[offset] = data.getDouble(key);
        if (value == 0) {
            nullIndicators.set(offset, !data.isNull(key));
        }
    }

    public void populateFrom(
        int[] pos, SegmentLoader.RowList rowList, int column)
    {
        int offset = getOffset(pos);
        double d = values[offset] = rowList.getDouble(column);
        if (d == 0) {
            nullIndicators.set(offset, !rowList.isNull(column));
        }
    }

    public SqlStatement.Type getType() {
        return SqlStatement.Type.DOUBLE;
    }

    void set(int k, double d) {
        values[k] = d;
    }

    protected int getSize() {
        return values.length;
    }

    public SegmentBody createSegmentBody(
        List<Pair<SortedSet<Comparable>, Boolean>> axes)
    {
        return new DenseDoubleSegmentBody(
            nullIndicators,
            values,
            axes);
    }
}

// End DenseDoubleSegmentDataset.java
