package com.astrro.timely.main.library;

import androidx.annotation.NonNull;

import com.astrro.timely.core.DataModel;
import com.astrro.timely.gallery.Image;

import java.util.Locale;

public class IDocument extends DataModel {
   private Image documentThumbnail;
   private String mimetype;
   private String documentTitle;
   private String author;
   private int fileSize;
   private String uploadDate;
   private boolean isLiked;
   private boolean isFlagged;

   public IDocument() {
   }

   public IDocument(Image documentThumbnail, String mimetype, String documentTitle, String author,
                    int fileSize, String uploadDate, boolean isFlagged, boolean isLiked) {
      this.documentThumbnail = documentThumbnail;
      this.mimetype = mimetype;
      this.documentTitle = documentTitle;
      this.fileSize = fileSize;
      this.author = author;
      this.uploadDate = uploadDate;
      this.isFlagged = isFlagged;
      this.isLiked = isLiked;
   }

   public int getFileSize() {
      return fileSize;
   }

   public void setFileSize(int fileSize) {
      this.fileSize = fileSize;
   }

   public boolean isLiked() {
      return isLiked;
   }

   public void setLiked(boolean liked) {
      isLiked = liked;
   }

   public boolean isFlagged() {
      return isFlagged;
   }

   public void setFlagged(boolean flagged) {
      isFlagged = flagged;
   }

   public Image getDocumentThumbnail() {
      return documentThumbnail;
   }

   public void setDocumentThumbnail(Image documentThumbnail) {
      this.documentThumbnail = documentThumbnail;
   }

   public String getMimeType() {
      return mimetype;
   }

   public void setMimetype(String mimetype) {
      this.mimetype = mimetype;
   }

   public String getDocumentTitle() {
      return documentTitle;
   }

   public void setDocumentTitle(String documentTitle) {
      this.documentTitle = documentTitle;
   }

   public String getAuthor() {
      return author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public int getDocumentSize() {
      return fileSize;
   }

   /**
    * @return the file size from bytes to human readable format
    */
   public String getConvertedDocumentSize() {
      final int M_SIZE = 1024;
      final long mega = M_SIZE * M_SIZE, giga = mega * M_SIZE, tera = giga * M_SIZE;
      String ss = "0 B";
      try {
         double kb = (double) fileSize / M_SIZE;
         double mb = kb / M_SIZE;
         double gb = mb / M_SIZE;
         double tb = gb / M_SIZE;

         if (fileSize < M_SIZE) {
            ss = fileSize + "BYTES";
         } else if (fileSize >= M_SIZE && fileSize < mega) {
            ss = String.format(Locale.US, "%.2f KB", kb);
         } else if (fileSize >= mega && fileSize < giga) {
            ss = String.format(Locale.US, "%.2f MB", mb);
         } else if (fileSize >= giga && fileSize < tera) {
            ss = String.format(Locale.US, "%.2f GB", gb);
         } else if (fileSize >= tera) {
            ss = String.format(Locale.US, "%.2f TB", tb);
         }

      } catch (NumberFormatException exc) {
         return ss;
      }
      return ss;
   }

   public void setDocumentSize(int fileSize) {
      this.fileSize = fileSize;
   }

   public String getUploadDate() {
      return uploadDate;
   }

   public void setUploadDate(String uploadDate) {
      this.uploadDate = uploadDate;
   }

   @NonNull
   @Override
   public String toString() {
      return "IDocument { " +
              "documentThumbnail=" + documentThumbnail +
              ", mimetype='" + mimetype + '\'' +
              ", documentTitle='" + documentTitle + '\'' +
              ", author='" + author + '\'' +
              ", fileSize=" + fileSize +
              ", uploadDate='" + uploadDate + '\'' +
              " }";
   }
}
