package com.noah.timely.auth.data.api;

import android.os.Build;

import java.util.Collections;
import java.util.List;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("unused")
public class RetrofitFactory {
   private static final String API_BASE_URL = "http://Timeley.herokuapp.com/";
   private static Retrofit retrofit;

   private RetrofitFactory() {
   }

   private static Retrofit getRetrofit() {
      /* ConnectionSpec.MODERN_TLS is the default value */
      List<ConnectionSpec> tlsSpec = Collections.singletonList(ConnectionSpec.MODERN_TLS);

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         tlsSpec = Collections.singletonList(ConnectionSpec.COMPATIBLE_TLS);
      }

      OkHttpClient httpClient = new OkHttpClient.Builder()
              .connectionSpecs(tlsSpec)
              .build();

      if (retrofit == null)
         return retrofit = new Retrofit.Builder()
                 .baseUrl(API_BASE_URL)
                 .client(httpClient)
                 .addConverterFactory(GsonConverterFactory.create())
                 .build();

      else return retrofit;
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
}
