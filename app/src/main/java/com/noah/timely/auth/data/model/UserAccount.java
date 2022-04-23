package com.noah.timely.auth.data.model;

import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.Serializable;
import java.net.URL;

public class UserAccount implements Serializable {
   private String userId;
   private String firstName;
   private String lastName;
   private String userName;
   private String phoneNumber;
   private String email;
   private String passowrd;
   private String dateOfBirth;
   private Uri profilePictureUri;
   private URL profilePictureURL;
   private String gender;
   private String country;
   private String school;

   public static UserAccount createFromGoogleSignIn(GoogleSignInAccount account) {
      UserAccount userAccount = new UserAccount();
      userAccount.setFirstName(account.getGivenName());
      userAccount.setLastName(account.getFamilyName());
//      userAccount.setEmail(account.getEmail());
//      userAccount.setProfilePictureUri(account.getPhotoUrl());

      return userAccount;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public void setFirstName(CharSequence firstName) {
      this.firstName = firstName.toString();
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(CharSequence lastName) {
      this.lastName = lastName.toString();
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getUserName() {
      return userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public void setUserName(CharSequence userName) {
      this.userName = userName.toString();
   }

   public String getPhoneNumber() {
      return phoneNumber;
   }

   public void setPhoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
   }

   public void setPhoneNumber(CharSequence phoneNumber) {
      this.phoneNumber = phoneNumber.toString();
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public void setEmail(CharSequence email) {
      this.email = email.toString();
   }

   public String getPassowrd() {
      return passowrd;
   }

   public void setPassowrd(String passowrd) {
      this.passowrd = passowrd;
   }

   public void setPassowrd(CharSequence passowrd) {
      this.passowrd = passowrd.toString();
   }

   public String getDateOfBirth() {
      return dateOfBirth;
   }

   public void setDateOfBirth(String dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
   }

   public Uri getProfilePictureUri() {
      return profilePictureUri;
   }

   public void setProfilePictureUri(Uri profilePictureUri) {
      this.profilePictureUri = profilePictureUri;
   }

   public URL getProfilePictureURL() {
      return profilePictureURL;
   }

   public void setProfilePictureURL(URL profilePictureURL) {
      this.profilePictureURL = profilePictureURL;
   }

   public String getGender() {
      return gender;
   }

   public void setGender(String gender) {
      this.gender = gender;
   }

   public String getCountry() {
      return country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   public String getSchool() {
      return school;
   }

   public void setSchool(String school) {
      this.school = school;
   }

   public String getRandomUserName() {
      // don't generate any random username if first and last name wasn't provided
      if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) return null;

      String l_firstName = firstName.toLowerCase();
      String l_lastName = lastName.toLowerCase();
      StringBuilder indexBuilder = new StringBuilder(l_firstName + l_lastName);

      int n = (int) (Math.random() * 10) % 4;  // limit number to 4

      while (n-- != 0) {
         int randomUID = (int) (Math.random() * 10);
         indexBuilder.append(randomUID);
      }

      return indexBuilder.toString();

   }

}
