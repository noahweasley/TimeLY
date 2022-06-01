package com.astrro.timely.auth.data.api;

import com.astrro.timely.auth.data.model.UserAccount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TmApi {
   private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

   public static java.util.concurrent.Future<Response<?>>
   verifyNewUserLogin(UserAccount userAccount, ResponseCallback<ResponseBody> callback){
      return executorService.submit(() -> {
         AuthenticationService apiService = ServiceGenerator.createService(AuthenticationService.class);
         Call<ResponseBody> call = apiService.registerUser(userAccount);

         Response<ResponseBody> response = call.execute();

         Retrofit retrofit = ServiceGenerator.getRetrofitInstance();
         // convert errorbody
//         Converter<ResponseBody, ApiError> converter = retrofit.responseBodyConverter(ApiError.class, new Annotation[0]);
         callback.apply(response.body(), null);
         return response;
      });
   }

   public interface ResponseCallback <T> {
      void apply(T body, ApiError error);
   }
}