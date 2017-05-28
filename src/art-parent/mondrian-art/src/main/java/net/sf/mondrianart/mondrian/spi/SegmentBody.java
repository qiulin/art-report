/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2011-2012 Pentaho and others
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.spi;

import net.sf.mondrianart.mondrian.rolap.CellKey;

import java.io.Serializable;
import java.util.*;

/**
 * SegmentBody is the object which contains the cached data of a
 * Segment. They are stored inside a {@link net.sf.mondrianart.mondrian.spi.SegmentCache}
 * and can be retrieved by a {@link SegmentHeader} key.
 *
 * <p>The segment body objects are immutable and fully serializable.
 *
 * @author LBoudreau
 */
public interface SegmentBody extends Serializable {
    /**
     * Converts contents of this segment into a cellkey/value map. Use only
     * for sparse segments.
     *
     * @return Map containing cell values keyed by their coordinates
     */
    Map<CellKey, Object> getValueMap();

    /**
     * Returns an array of values. Use only for dense segments.
     *
     * @return An array of values
     */
    Object getValueArray();

    /**
     * Returns a bitset indicating whether values are null.
     * Use only for dense segments.
     *
     * @return Indicators
     */
    BitSet getIndicators();

    /**
     * Returns the cached axis value sets to be used as an
     * initializer for the segment's axis.
     *
     * @return An array of SortedSets which was cached previously.
     */
    SortedSet<Comparable>[] getAxisValueSets();

    /**
     * Returns an array of boolean values which identify which
     * axis of the cached segment contained null values.
     *
     * @return An array of boolean values.
     */
    boolean[] getNullAxisFlags();
}

// End SegmentBody.java
