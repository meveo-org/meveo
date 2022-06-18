/**
 * 
 */
package org.meveo.commons.utils;

import java.util.Comparator;
import java.util.List;

public class MvCollectionUtils {

	public static <T extends Comparable> void bubbleSort(List<T> objects, Comparator<T> comparator) {

		/*
		 * In bubble sort, we basically traverse the array from first to array_length - 1 position and compare the element with the next one. Element is
		 * swapped with the next element if the next element is smaller.
		 * This sort assume that comparable.compare() returns 0 when objects are not comparable
		 * 
		 */

		int n = objects.size();

		for (int i = 0; i < n; i++) {
			boolean swapped = false;
			for (int j = 1; j < (n - i); j++) {

				int compareJMinus1AndJ = comparator.compare(objects.get(j - 1), objects.get(j));
				if (compareJMinus1AndJ>0) {
					// swap the elements!
					T temp = objects.get(j - 1);
					objects.set(j - 1, objects.get(j));
					objects.set(j, temp);
					swapped = true;
				} else if (compareJMinus1AndJ==0 & j>1) {
					for (int z = j-2; z>=0; z--) {
						int compareZAndJ = comparator.compare(objects.get(j - z), objects.get(j));
						if (compareZAndJ>0) {
							T temp = objects.get(z);
							objects.set(z, objects.get(j));
							objects.set(j, temp);
							swapped = true;
						}else if (compareZAndJ<0) {
							break;
						}
					}
				}

			}
			if (!swapped)
				break;
		}

	}
}
