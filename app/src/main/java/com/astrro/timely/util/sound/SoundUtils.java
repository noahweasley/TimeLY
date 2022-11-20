package com.astrro.timely.util.sound;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RawRes;

import com.astrro.timely.R;
import com.astrro.timely.util.PreferenceUtils;

import java.io.IOException;

/**
 * Small utility class to play sound
 */
public class SoundUtils {
   public static final String ENABLE_ALERTS_KEY = "enable alerts";
   private static MediaPlayer alertPlayer;

   /**
    * Play an alert tone when item is added to a list
    *
    * @param context the context which the tone is being played
    */
   @SuppressWarnings("deprecation")
   public static void playAlertTone(Context context, AlertType type) {
      // If alert tone are disabled, don't play any alert tone
      if (!PreferenceUtils.getBooleanValue(context, ENABLE_ALERTS_KEY, true)) return;

      if (alertPlayer == null) alertPlayer = new MediaPlayer();
      else if (alertPlayer != null) {
         try {
            // push internal audio player to it's idle state
            alertPlayer.reset();
         } catch (Exception ignored) {
         }

      }

      try {

         if (type == AlertType.DELETE)
            alertPlayer.setDataSource(context, getUri(context, R.raw.piece_of_cake1));
         else if (type == AlertType.NOTIFICATION)
            alertPlayer.setDataSource(context, getUri(context, R.raw.echoed_ding1));
         else if (type == AlertType.TODO_UPDATE)
            alertPlayer.setDataSource(context, getUri(context, R.raw.pristine));
         else
            alertPlayer.setDataSource(context, getUri(context, R.raw.accomplished1));

         alertPlayer.setOnPreparedListener(MediaPlayer::start);

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertPlayer.setAudioAttributes(new AudioAttributes.Builder()
                                                   .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                   .setUsage(AudioAttributes.USAGE_MEDIA)
                                                   .build());
         } else {
            // backward compatibility for pre LOLLIPOP devices
            alertPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
         }

         // sound effect too loud, reduce it
         final int MAX_VOLUME = 100;
         final int SOUND_VOLUME = 40;
         float volume = (float) (1 - Math.log(MAX_VOLUME - SOUND_VOLUME) / Math.log(MAX_VOLUME));
         alertPlayer.setVolume(volume, volume);

         alertPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(context, "Error playing sound", Toast.LENGTH_LONG).show();
            mp.reset();
            return false;
         });

         alertPlayer.prepareAsync();

      } catch (IOException ignored) {
      }

   }

   private static Uri getUri(Context context, @RawRes int rawRes) {
      return new Uri.Builder()
              .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
              .authority(context.getPackageName())
              .path(String.valueOf(rawRes))
              .build();
   }

   /**
    * Release memeory held by alert player
    */
   public static void doCleanUp() {
      if (alertPlayer != null) {
         alertPlayer = null;
         alertPlayer.release();
      }
   }

}
