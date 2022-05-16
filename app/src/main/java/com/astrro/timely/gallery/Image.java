package com.astrro.timely.gallery;

import android.net.Uri;

import java.io.Serializable;

public class Image implements Serializable {
   private String folderName;
   private String imageUri;
   private int size;
   private String fileName;

   public Image(Uri imageUri, int size, String fileName, String folderName) {
      this.folderName = folderName;
      this.imageUri = imageUri.toString();
      this.size = size;
      this.fileName = fileName;
   }

   public Image() {
   }

   public static Image createImageFromUri(Uri uri) {
      Image image = new Image();
      image.setImageUri(uri);
      return image;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public String getFolderName() {
      return folderName;
   }

   public Uri getImageUri() {
      return Uri.parse(imageUri);
   }

   public void setImageUri(Uri imageUri) {
      this.imageUri = imageUri.toString();
   }

   public int getSize() {
      return size;
   }

   public void setSize(int size) {
      this.size = size;
   }

}
