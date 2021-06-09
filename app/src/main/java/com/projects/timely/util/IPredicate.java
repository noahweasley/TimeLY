package com.projects.timely.util;

/**
 * A predicate that does same function similar to {@link java.util.function.Predicate}
 *
 * @param <T> the type of data in which to test
 * @see java.util.function.Predicate
 */
@FunctionalInterface
public interface IPredicate<T> {
    boolean test(T type);
}