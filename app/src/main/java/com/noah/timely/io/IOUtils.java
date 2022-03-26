package com.noah.timely.io;

import static com.noah.timely.io.Zipper.MAX_DATA_TRANSFER_BITS;

import android.content.Context;
import android.os.Build;
import android.os.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

   public static boolean deleteTempFiles(Context context) {
      String tempFilePath = context.getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      File tempFilesDir = new File(tempFilePath);

      boolean allDeleteFlag = true;

      if (tempFilesDir.exists()) {
         for (File tempFile : tempFilesDir.listFiles()) {
            allDeleteFlag &= tempFile.delete();
         }
      }

      return allDeleteFlag;
   }
}
