package com.astrro.timely.util.collections;

@FunctionalInterface
public interface IBinaryFunction <T, R, U> {

   /**
    * Applies this function to the given argument.
    *
    * @param t the first function argument
    * @param u the second function argument
    * @return the function result
    */
   R apply(T t, U u);

}
