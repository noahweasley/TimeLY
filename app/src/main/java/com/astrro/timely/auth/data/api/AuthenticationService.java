package com.astrro.timely.auth.data.api;

import com.astrro.timely.auth.data.model.LoginResponse;
import com.astrro.timely.auth.data.model.UserAccount;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

/**
 * User Authentication service
 */
public interface AuthenticationService {

   @POST("register")
   Call<LoginResponse> registerUser(@Body UserAccount userAccount);

   @POST("login")
   Call<LoginResponse> verifyUserLogin(@Body UserAccount userAccount);

   @POST("forgotpassword")
   Call<Void> retrieveNewLogin(@Body UserAccount userAccount);

   @PATCH("reset")
   Call<Void> resetPassword(@Body UserAccount userAccount);

}
