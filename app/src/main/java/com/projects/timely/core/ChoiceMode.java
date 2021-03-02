package com.projects.timely.core;

import android.os.Bundle;

import com.projects.timely.gallery.ImageMultiChoiceMode;

/**
 * The selection choice mode of the list
 */
public interface ChoiceMode {

    /* Choice mode constants */

    /**
     * Multi-choice mode to be used with image list
     */
    ChoiceMode IMAGE_MULTI_SELECT = new ImageMultiChoiceMode();
    /**
     * Multi-choice mode to be used with data list
     */
    ChoiceMode DATA_MULTI_SELECT = new DataMultiChoiceMode();

    /**
     * used this to persist the state of the checked items. If <code>checked</code> is false, then
     * any previous entry (when <code>checked</code> was true) in the choice mode, would be erased,
     * as it is not useful.
     *
     * @param position the position in which the its checked value is to be inserted
     * @param check    the status of the checked item
     */
    void setChecked(int position, boolean check);

    /**
     * @param position the position of the item
     * @return the checked status of an item in the list
     */
    boolean isChecked(int position);

    /**
     * Call this when saving apps state passing a valid bundle as the parameter, or else
     * checked items state won't be saved at all.
     * Use with onSaveInstanceState.
     *
     * @param bundle the bundle in which state would be saved
     */
    void onSaveInstanceState(Bundle bundle);

    /**
     * Call this when restoring apps state passing a valid bundle as the parameter, or else
     * checked items state won't be retrieved at all.
     * Use with onRestoreInstanceState.
     *
     * @param bundle the bundle in which state would be saved
     */
    void onRestoreInstanceState(Bundle bundle);

    /**
     * @return the number items that are current checked
     */
    int getCheckedChoiceCount();

    /**
     * Clear all of the previous selection
     */
    void clearChoices();

    /**
     * @return the position or index of all the checked items in the list
     */
    Integer[] getCheckedChoicesIndices();

    /**
     * @return the position or index of all the checked items in the list (according to database)
     */
    Integer[] getCheckedChoicePositions();

}