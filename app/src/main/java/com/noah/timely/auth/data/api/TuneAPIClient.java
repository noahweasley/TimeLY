package com.noah.timely.auth.data.api;

import com.noah.timely.auth.data.model.UserAccount;

@SuppressWarnings("unused")
public class TuneAPIClient {

   /**
    * Checks if a user already has an account
    *
    * @param meAccount the account to check its existence
    * @return true, if user already has an account
    */
   public static boolean checkExistingUser(UserAccount meAccount) {
      return false;
   }

   /**
    * Register a new user to TuneMe if user haven't registered yet
    *
    * @param meAccount the user's account to send to the server
    * @return true, if operation was successful
    */
   public static boolean signUp(UserAccount meAccount) {
      // stop registration if duplicate account was found
      return !checkExistingUser(meAccount);
   }

   /**
    * @param meAccount the user's account to send to the server
    * @return true, if operation was successful
    */
   public static boolean login(UserAccount meAccount) {
      // stop login if account isn't registered
      return checkExistingUser(meAccount);
   }

   public enum Options {

   }
}
