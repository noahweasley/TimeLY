package com.projects.timely.core;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DataMultiChoiceMode extends MultiChoiceMode {
    private final List<DataModel> dataModels = new ArrayList<>();

    /**
     * @return the items that was previously selected
     */
    public DataModel[] getSelectedItems() {
        return dataModels.toArray(new DataModel[0]);
    }

    /**
     * Add to list of selected items
     *
     * @param item the to be added
     */
    public void addSelectedItem(DataModel item) {
        dataModels.add(item);
    }

    /**
     * Removes an item from the list of selected items
     *
     * @param item the data model to be removed or deleted
     */
    public void removeImageUri(DataModel item) {
        dataModels.remove(item);
    }

}
