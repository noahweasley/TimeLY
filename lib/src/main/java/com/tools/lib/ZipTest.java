package com.tools.lib;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipTest {

   public static void main(String... args) {
      String[] texts = new String[4];
      texts[0] = "Benin";
      texts[1] = "Panama";
      texts[2] = "35214";
      texts[3] = "Suite 618 1456 Lino Views, Port Lakita, OR 88933";

      try {
         ZipOutputStream zout = new ZipOutputStream(new FileOutputStream("C\\Users\\Noah\\Desktop\\Texts.zip"));
         for(String text : texts) {
            zout.putNextEntry(new ZipEntry(text));
            zout.write(text.getBytes(StandardCharsets.UTF_8));
            zout.closeEntry();
         }

         zout.finish();
         zout.close();
      } catch (IOException e) {
         e.printStackTrace();
      }


   }

}
