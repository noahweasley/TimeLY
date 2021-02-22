package com.projects.timely.gallery;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link MultiChoiceMode} subclass that can get access to the uris of the images selected
 */
public class ImageMultiChoiceMode extends MultiChoiceMode {
    private final List<String> uriList = new ArrayList<>();

    /**
     * @return the uris of the items that was previously selected
     */
    public String[] getUriList() {
        return uriList.toArray(new String[0]);
    }

    /**
     * Add to list of selected items
     *
     * @param uri the uri of the item to be added
     */
    public void addImageUri(Uri uri) {
        uriList.add(uri.toString());
    }

    /**
     * Removes an item from the list of selected items
     *
     * @param uri the uri to be removed or deleted
     */
    public void removeImageUri(Uri uri) {
        uriList.remove(uri.toString());
    }
}