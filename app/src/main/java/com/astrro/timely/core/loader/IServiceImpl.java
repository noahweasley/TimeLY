package com.astrro.timely.core.loader;

public interface IServiceImpl<T> {

   void enqueue(Callback<T> callback);

   void cancel();

   void onCancelled(Callback<T> callback);

}
