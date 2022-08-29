package com.astrro.timely.main.library;

public class MimeTypes {
   public static final String PDF = "PDF";
   public static final String DOCX = "DOCX";
   public static final String DOC = "DOC";
   public static final String BIN = "BIN";
   public static final String TXT = "TXT";
   public static final String ZIP = "ZIP";

   public static int getMimetypeOrder(String mimetype) {
      int mimetypeOrdinal = -1;
      switch (mimetype) {
         case PDF:
            mimetypeOrdinal = Types.PDF.ordinal();
            break;
         case DOCX:
            mimetypeOrdinal = Types.DOCX.ordinal();
            break;
         case DOC:
            mimetypeOrdinal = Types.DOC.ordinal();
            break;
         case BIN:
            mimetypeOrdinal = Types.BIN.ordinal();
            break;
         case TXT:
            mimetypeOrdinal = Types.TXT.ordinal();
            break;
         case ZIP:
            mimetypeOrdinal = Types.ZIP.ordinal();
            break;
         default:
            throw new IllegalStateException("Unexpected value: " + mimetype);
      }
      return mimetypeOrdinal;
   }

   public enum Types {
      PDF, DOCX, DOC, BIN, TXT, ZIP;
   }

}
