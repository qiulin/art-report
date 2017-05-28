/*
 * Created on 07.03.2005
 */
package net.sf.wcfart.wcf.utils;

import java.util.Comparator;
import java.util.List;

/**
 * comparator to sort a list in the order of another list. Missing elements are placed
 * at the end.
 * 
 * @author av
 * @since 07.03.2005
 */
public class IndexOfComparator <T extends Comparable<T>> implements Comparator<T> {
	
	//https://stackoverflow.com/questions/15189949/making-a-generic-comparator-class

  List<T> list;

  public IndexOfComparator(List<T> list) {
    this.list = list;
  }

  public int compare(T o1, T o2) {
    int i1 = list.indexOf(o1);
    int i2 = list.indexOf(o2);
    if (i1 < 0 && i2 < 0)
      return 0;
    if (i1 < 0)
      return 1;
    if (i2 < 0)
      return -1;
    if (i1 > i2)
      return 1;
    if (i1 < i2)
      return -1;
    return 0;
  }
}
