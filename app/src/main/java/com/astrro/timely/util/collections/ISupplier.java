package com.astrro.timely.util.collections;

/**
 * Same functionality as {@link java.util.function.Supplier}
 *
 * @param <R> the return type of this supplier function
 * @see java.util.function.Supplier
 */
@FunctionalInterface
public interface ISupplier <R> {
   R get();
}
