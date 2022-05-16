package com.astrro.timely.util.collections;

@FunctionalInterface
public interface LoginConsumer <R> {
   void accept(Throwable t, R data);
}
