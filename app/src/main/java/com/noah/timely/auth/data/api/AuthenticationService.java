package com.noah.timely.auth.data.api;

import com.noah.timely.auth.data.model.UserAccount;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

/**
 * User Authentication service
 */
public interface AuthenticationService {

   @POST("/register")
   Call<Void> registerUser(@Body UserAccount userAccount);

   @POST("/login")
   Call<Void> verifyUserLogin(@Body UserAccount userAccount);

   @POST("/forgotpassword")
   Call<Void> retrieveNewLogin(@Body UserAccount userAccount);

   @PATCH("/reset")
   Call<Void> resetPassword(@Body UserAccount userAccount);


}
