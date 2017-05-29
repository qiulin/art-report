/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.cewolfart;

/**
 * Thrown by a DatasetProducer if it has problems in creating a correct dataset.
 * @see DatasetProducer
 * @author  Guido Laures
 */
public class PostProcessingException extends CewolfException {

	static final long serialVersionUID = 188604852216929486L;

    /** Creates a new instance of <code>DatasetProduceException</code> without detail message. */
    public PostProcessingException() {
    }

    /**
     * Constructs an instance of <code>DatasetProduceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PostProcessingException(String msg) {
        super(msg);
    }
}
