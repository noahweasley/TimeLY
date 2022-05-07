package com.noah.timely.util.collections;

@FunctionalInterface
public interface IConsumer <T> {
   void accept(T body);
}
