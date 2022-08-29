package com.astrro.timely.core.loader;

public interface Callback<T> {

   void onSuccess(T t);

   void onCancelled(Void v);

}
