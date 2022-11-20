package com.astrro.timely.io;

import static com.astrro.timely.io.Zipper.MAX_DATA_TRANSFER_BITS;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.Process;
import android.os.SystemClock;

import com.astrro.timely.util.ThreadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * This contains all utility functions that relates to I/O processing
 */
public class IOUtils {

   /**
    * Copies data from <code>inputStream</code> to <code>outputStream</code>
    *
    * @param inputStream  the input stream in which data would be copied from
    * @param outputStream the output stream in which data would be written to
    * @throws IOException if a read or write operation occurrs
    */
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

   /**
    * Retrieves the file extension from a valid File object
    *
    * @param file the file in which it's extension would be checked
    * @return the file extension of the file, that is if the File object references a valid file, returns null if
    * the File object is not a valid file
    */
   public static String getFileExtension(File file) {
      String ssFile = file.toString();
      int i = ssFile.lastIndexOf('.');
      return i > 0 ? ssFile.substring(i) : null;
   }

   /**
    * Deletes all temp files. Don't worry, it runs in a seperate thread, you can call it anywhere
    *
    * @param context the context to access system resources
    */
   public static void deleteTempFiles(Context context) {
      String tempFilePath = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      File tempFilesDir = new File(tempFilePath);
      ThreadUtils.runBackgroundTask(() -> {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
         if (tempFilesDir.exists()) {
            for (File tempFile : tempFilesDir.listFiles()) {
               //noinspection ResultOfMethodCallIgnored
               tempFile.delete();
            }
         }
      });
   }

   /**
    * The resulting URI received from the Android device's file chooser would never be a correct URI to be used
    * directly to get the file path because in Android, not all URIs points to a valid file. So a temp file
    * was used to copy the data in the stream gotten from the URI, and then the temp file's path was used instead.
    *
    * @param context  the context in which storage access would be used
    * @param uri      the {@link Uri} to be resolved to a temporary file
    * @param callBack the callback that returns a {@link File} object or null, if an error occurred
    */
   public static void resolveUriToTempFile(Context context, Uri uri, CallBack<File> callBack) {
      ThreadUtils.runBackgroundTask(() -> {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
         String parentFolder = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
         String tempFilePath = String.format(Locale.US, "%stemp%d.tmp", parentFolder, SystemClock.elapsedRealtime());

         File tempFile = new File(tempFilePath);
         File tempFileDir = tempFile.getParentFile();

         boolean isCreated = true;
         if (!tempFileDir.exists()) {
            isCreated = tempFileDir.mkdirs();
         }

         if (isCreated) {
            try {
               copy(context.getContentResolver().openInputStream(uri), new FileOutputStream(tempFile));
               callBack.onExecuted(tempFile);
            } catch (IOException e) {
               callBack.onExecuted(null);
            }
         } else {
            callBack.onExecuted(null);
         }

      });

   }

   /**
    * The resulting URI received from the Android device's file chooser would never be a correct URI to be used
    * directly to get the file path because in Android, not all URIs points to a valid file. So a temp file
    * was used to copy the data in the stream gotten from the URI, and then the temp file's path was used instead.
    *
    * @param context  the context in which storage access would be used
    * @param uri      the {@link Uri} to be resolved to a temporary file
    * @param callBack the callback that returns a {@link File} object or null, if an error occurred
    */
   public static void resolveUriDataToTempFile(Context context, Uri uri, CallBack<File> callBack) {
      // return immediately if the file extension is not supported
      if (!getFileExtension(new File(uri.getPath())).equals(Zipper.FILE_EXTENSION)) {
         callBack.onExecuted(null);
         return;
      }

      ThreadUtils.runBackgroundTask(() -> {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
         String parentFolder = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
         String tempFilePath = String.format(Locale.US, "%stemp%d.tmp", parentFolder, SystemClock.elapsedRealtime());

         File tempFile = new File(tempFilePath);
         File tempFileDir = tempFile.getParentFile();

         boolean isCreated = true;
         if (!tempFileDir.exists()) {
            isCreated = tempFileDir.mkdirs();
         }

         if (isCreated) {
            try {
               copy(context.getContentResolver().openInputStream(uri), new FileOutputStream(tempFile));
               callBack.onExecuted(tempFile);
            } catch (IOException e) {
               callBack.onExecuted(null);
            }
         } else {
            callBack.onExecuted(null);
         }
      });

   }

   /**
    * Creates a temporary file asynchronously
    *
    * @param context  the context in which external files storage location would be checked
    * @param fileName the file name of the temporary file to be created
    * @param callBack the callback which returns a {@link File} object or null, if an error occurred
    */
   public static void createTempFile(Context context, String fileName, CallBack<File> callBack) {
      ThreadUtils.runBackgroundTask(() -> {
         String parentFolder = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
         String tempFilePath = String.format(Locale.US, "%s%s.tmp", parentFolder, fileName);
         File file = new File(tempFilePath);
         File tempFileDir = file.getParentFile();

         boolean isCreated = true;
         if (!tempFileDir.exists()) {
            isCreated = tempFileDir.mkdirs();
         }

         if (isCreated) {
            try {
               //noinspection ResultOfMethodCallIgnored
               if (file.createNewFile()) {
                  callBack.onExecuted(file);
               } else {
                  callBack.onExecuted(null);
               }
            } catch (IOException e) {
               callBack.onExecuted(null);
            }

         } else {
            callBack.onExecuted(null);
         }
      });
   }

   /**
    * Creates a temporary image asynchronously
    *
    * @param context  the context in which external files storage location would be checked
    * @param fileName the file name of the temporary image to be created
    * @param callBack the callback which returns a {@link File} object or null, if an error occurred
    */
   public static void createTempImage(Context context, String fileName, CallBack<File> callBack) {
      String imageFileName = String.format(Locale.US, "images%s%s", File.separator, fileName);
      createTempFile(context, imageFileName, callBack);
   }

   /**
    * Callback to be invoked when a background task has finished running
    *
    * @param <T> the data that was produced, after running background task
    */
   public interface CallBack <T> {
      void onExecuted(T data);
   }

}