package com.astrro.timely.auth.data.api;

import com.astrro.timely.auth.data.model.LoginResponse;
import com.astrro.timely.auth.data.model.UserAccount;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Encapsulation of all network calls that happens within the whole app
 */
public class TimelyApi {
   private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

   /**
    * Logs in a new user account
    *
    * @param userAccount the user account to be logged in
    * @return a response with essential details if the user exists or not
    * @throws IOException when network error occurs
    */
   public static LoginResponse loginUser(UserAccount userAccount) throws IOException {
      AuthenticationService apiService = ServiceGenerator.createService(AuthenticationService.class);
      Call<LoginResponse> responseCall = apiService.verifyUserLogin(userAccount);

      Response<LoginResponse> loginResponse = responseCall.execute();

      if (loginResponse.isSuccessful()) {
         LoginResponse response = loginResponse.body();
         response.setStatusCode(loginResponse.code());
         return response;
      } else {
         return LoginResponse.fromStatusCode(loginResponse.code());
      }

   }

   /**
    * Registers a new user account
    *
    * @param userAccount the account to be registered
    * @return the repsonse descibing if the user has been registered or not
    * @throws IOException if a network error occurs
    */
   public static RegistrationResponse registerNewUser(UserAccount userAccount) throws IOException {
      AuthenticationService apiService = ServiceGenerator.createService(AuthenticationService.class);
      Call<RegistrationResponse> responseCall = apiService.registerUser(userAccount);

      Response<RegistrationResponse> loginResponse = responseCall.execute();

      if (loginResponse.isSuccessful()) {
         RegistrationResponse response = loginResponse.body();
         response.setStatusCode(loginResponse.code());
         return response;
      } else {
         return null;
      }
   }

   /**
    * Call back interface, to serve the network calls
    *
    * @param <T> the response data type returned
    */
   public interface ResponseCallback <T> {

      /**
       * apply the responsebody and error body
       *
       * @param responseBody the response body
       * @param error        the error body
       */
      void apply(T responseBody, ApiError error);
   }
}