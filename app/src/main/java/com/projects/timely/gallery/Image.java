package com.projects.timely.gallery;

import android.net.Uri;

@SuppressWarnings("unused")
class Image {
    private final String folderName;
    private Uri imageUri;
    private int size;
    private String fileName;

    Image(Uri imageUri, int size, String fileName, String folderName) {
        this.folderName = folderName;
        this.imageUri = imageUri;
        this.size = size;
        this.fileName = fileName;
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
