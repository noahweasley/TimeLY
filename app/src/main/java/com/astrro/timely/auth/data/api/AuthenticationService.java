package com.astrro.timely.auth.data.api;

import com.astrro.timely.auth.data.model.LoginResponse;
import com.astrro.timely.auth.data.model.UserAccount;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

/**
 * User Authentication service
 */
public interface AuthenticationService {

   @POST("register")
   Call<RegistrationResponse> registerUser(@Body UserAccount userAccount);

   @POST("login")
   Call<LoginResponse> verifyUserLogin(@Body UserAccount userAccount);

   @POST("forgotpassword")
   Call<ResponseBody> retrieveNewLogin(@Body UserAccount userAccount);

   @PATCH("reset")
   Call<ResponseBody> resetPassword(@Body UserAccount userAccount);

}
