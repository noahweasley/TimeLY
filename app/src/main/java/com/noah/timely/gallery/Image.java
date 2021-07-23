package com.noah.timely.gallery;

import android.net.Uri;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Image implements Serializable {
    private String folderName;
    private Uri imageUri;
    private int size;
    private String fileName;

    public Image(Uri imageUri, int size, String fileName, String folderName) {
        this.folderName = folderName;
        this.imageUri = imageUri;
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

    String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    String getFolderName() {
        return folderName;
    }

    Uri getImageUri() {
        return imageUri;
    }

    void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    int getSize() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

}
