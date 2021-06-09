package com.projects.timely.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A collection of utilities for working with the Collections Framework but still maintains
 * backward compatibility for older versions of android
 */
public class CollectionUtils {

    /**
     * Filters a list according to a given predicate
     *
     * @param target    the list to be filtered
     * @param predicate the test to be used in the filter
     * @param <T>       the data in which the predicate would act on
     * @return a filtered list
     */
    public static <T> List<T> filterList(List<T> target, IPredicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T element : target) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Searches for an item in a list
     *
     * @param target the list to perform the search on
     * @param key    the item to be found
     * @param c      the comparator to be used in the search
     * @return the index of the the item or -1 if not found
     */
    public static <T> int linearSearch(List<? extends T> target, T key, Comparator<? super T> c) {
        for (int i = 0; i < target.size(); i++)
            if (c.compare(target.get(i), key) == 0) return i;
        return -1;
    }
}
