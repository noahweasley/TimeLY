package com.noah.timely.io;

import static com.noah.timely.io.Zipper.MAX_DATA_TRANSFER_BITS;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.SystemClock;

import com.noah.timely.util.ThreadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class IOUtils {

   public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
      try {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileUtils.copy(inputStream, outputStream);
         } else {
            byte[] dataBuffer = new byte[MAX_DATA_TRANSFER_BITS];
            while (inputStream.read(dataBuffer) != -1) {
               outputStream.write(dataBuffer);
            }

         }
      } finally {
         inputStream.close();
         outputStream.close();
      }
   }

   public static String getFileExtension(File file) {
      String ssFile = file.toString();
      int i = ssFile.lastIndexOf('.');
      return i > 0 ? ssFile.substring(i) : null;
   }

   public static void deleteTempFiles(Context context) {
      String tempFilePath = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      File tempFilesDir = new File(tempFilePath);
      ThreadUtils.runBackgroundTask(() -> {
         if (tempFilesDir.exists()) {
            for (File tempFile : tempFilesDir.listFiles()) {
               //noinspection ResultOfMethodCallIgnored
               tempFile.delete();
            }
         }
      });
   }

   /*
    *  The resulting URI received from the Android device's file chooser would never be a correct URI to be used
    *  directly to get the file path because in Android, not all URIs points to a valid file. So a temp file
    *  was used to copy the data in the stream gotten from the URI, and then the temp file's path was used instead.
    */
   public static File resolveUriToTempFile(Context context, Uri uri) throws IOException {
      String parentFolder = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      String tempFilePath = String.format(Locale.US, "%stemp%d.tmp", parentFolder, SystemClock.elapsedRealtime());

      File tempFile = new File(tempFilePath);
      File tempFileDir = tempFile.getParentFile();

      boolean isCreated = true;
      if (!tempFileDir.exists()) {
         isCreated = tempFileDir.mkdirs();
      }

      if (!isCreated) return null;
      else copy(context.getContentResolver().openInputStream(uri), new FileOutputStream(tempFile));

      return tempFile;
   }

   /*
    *  The resulting URI received from the Android device's file chooser would never be a correct URI to be used
    *  directly to get the file path because in Android, not all URIs points to a valid file. So a temp file
    *  was used to copy the data in the stream gotten from the URI, and then the temp file's path was used instead.
    */
   public static File resolveDataToTempFile(Context context, Uri uri) throws IOException {
      // return immediately if the file extension is not supported
      if (!getFileExtension(new File(uri.getPath())).equals(Zipper.FILE_EXTENSION)) return null;

      String parentFolder = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      String tempFilePath = String.format(Locale.US, "%stemp%d.tmp", parentFolder, SystemClock.elapsedRealtime());

      File tempFile = new File(tempFilePath);
      File tempFileDir = tempFile.getParentFile();

      boolean isCreated = true;
      if (!tempFileDir.exists()) {
         isCreated = tempFileDir.mkdirs();
      }

      if (!isCreated) return null;
      else copy(context.getContentResolver().openInputStream(uri), new FileOutputStream(tempFile));

      return tempFile;
   }
}
