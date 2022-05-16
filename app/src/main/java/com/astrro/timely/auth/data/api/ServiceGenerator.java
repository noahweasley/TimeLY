package com.astrro.timely.auth.data.api;

import android.os.Build;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service Genrator class used for network calls
 */
public class ServiceGenerator {
   public static final long CONNECTION_TIMEOUT = 30;
   public static final long READ_TIMEOUT = 30;
   public static final long WRITE_TIMEOUT = 30;
   private static final String API_BASE_URL = "http://Timeley.herokuapp.com/";
   private static Retrofit retrofit;
   private static OkHttpClient baseHttpClient;

   /**
    * Can't use this constructor, use the factory methods provided
    */
   private ServiceGenerator() {
   }

   private static Retrofit getRetrofit() {
      if (retrofit == null)
         return retrofit = new Retrofit.Builder()
                 .baseUrl(API_BASE_URL)
                 .client(getBaseHttpClient())
                 .addConverterFactory(GsonConverterFactory.create())
                 .build();

      else return retrofit;
   }

   /**
    * @return the base Http client used in managing connection pools
    */
   public static OkHttpClient getBaseHttpClient() {
      if (baseHttpClient != null) return baseHttpClient;
      /* ConnectionSpec.MODERN_TLS is the default value */
      List<ConnectionSpec> tlsSpec = Collections.singletonList(ConnectionSpec.MODERN_TLS);

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         tlsSpec = Collections.singletonList(ConnectionSpec.COMPATIBLE_TLS);
      }

      baseHttpClient = new OkHttpClient.Builder()
              .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
              .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
              .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
              .connectionSpecs(tlsSpec)
              .build();

      return baseHttpClient;
   }

   /**
    * @return a newly created retrofit instance used for HTTP requests on TimeLY's api
    */
   public static Retrofit createInstance() {
      return getRetrofit();
   }

   /**
    * @return the existent retrofit instance used for HTTP request on TimeLY's api
    */
   public static Retrofit getInstance() {
      if (retrofit != null) {
         return retrofit;
      } else {
         return createInstance();
      }
   }

   /**
    * @return TimeLY's api base url
    */
   public static String getAPIBaseUrl() {
      return API_BASE_URL;
   }

   /**
    * @param serviceImp the service to be created
    * @return a valid Retrofit implementation of a service
    */
   public static <S> S createService(Class<S> serviceImp) {
      return getInstance().create(serviceImp);
   }

}
