package com.noah.timely.util;

import java.util.ArrayList;
import java.util.Collection;
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
    * Maps a function to be applied to a target list
    *
    * @param target   the list to be used
    * @param function the function to used in the mapper
    * @param <T>      the type of data applied to thee mapper
    * @param <R>      the return type of the mapper
    * @return a list of specified results by the mapper
    */
   public static <T, R> List<R> map(List<T> target, IFunction<T, R> function) {
      List<R> result = new ArrayList<>();
      for (T element : target) {
         result.add(function.apply(element));
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

   /**
    * Searches for an item in an array
    *
    * @param target the array to perform the search on
    * @param key    the item to be found
    * @return the index of the the item or -1 if not found
    */
   public static <T> int linearSearch(T[] target, T key) {
      for (int i = 0; i < target.length; i++) {
         if (key instanceof String) {
            if (target[i].equals(key)) return i;
         } else {
            if (target[i] == key) return i;
         }
      }
      return -1;
   }

   /**
    * Searches for an item in a list
    *
    * @param target the array to perform the search on
    * @param key    the item to be found
    * @return the index of the the item or -1 if not found
    */
   public static <T> int linearSearch(List<T> target, T key) {
      for (int i = 0; i < target.size(); i++) {
         if (target.get(i).equals(key)) return i; // dir found, return immediately
      }
      return -1; // dir was not found
   }

   /**
    * Checks if a collection is empty
    *
    * @param collection the data to be checked
    * @return true if it is empty or null
    */
   public static <E> boolean isEmpty(Collection<E> collection) {
      return collection == null || collection.isEmpty();
   }
}
