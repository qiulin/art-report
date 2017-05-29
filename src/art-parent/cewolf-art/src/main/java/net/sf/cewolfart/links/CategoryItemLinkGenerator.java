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
package net.sf.cewolfart.links;

/**
 * A link generator for category items. This interface is used by the 
 * tag library to produce image maps. Implement this if the dataset that
 * this DatasetProducer produces is a CategoryDataset.
 * @see org.jfree.data.category.CategoryDataset
 * @author  Guido Laures
 */
public interface CategoryItemLinkGenerator extends LinkGenerator {

    /**
     * Generates a link for a specific dataset/series/category triple.
     * @param dataset the dataset
     * @param series the series number
     * @param category the category
     * @see org.jfree.data.category.CategoryDataset
     */
    String generateLink(Object dataset, int series, Object category);

}
