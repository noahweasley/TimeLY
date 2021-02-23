package com.projects.timely.gallery;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy class that implements {@link ChoiceMode}. Multi-choice mode signifies that the list
 * can be multi-selected, to perform a bulk action on the individual list items
 */
public class MultiChoiceMode implements ChoiceMode {
    public static final String ARG_STATES = "Choice states";
    private final List<Integer> indices = new ArrayList<>();
    private ParcelableSparseBooleanArray sbarr = new ParcelableSparseBooleanArray();

    @Override
    public boolean isChecked(int position) {
        return sbarr.get(position);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable(ARG_STATES, sbarr);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        sbarr = bundle.getParcelable(ARG_STATES);
    }

    @Override
    public int getCheckedChoiceCount() {
        return sbarr.size();
    }

    @Override
    public Integer[] getCheckedChoicesIndices() {
        return indices.toArray(new Integer[0]);
    }

    @Override
    public void clearChoices() {
        sbarr.clear();
        indices.clear();
    }

    @Override
    public void setChecked(int position, boolean checked) {
        if (!checked) {
            sbarr.delete(position);
            indices.remove(Integer.valueOf(position));
        } else {
            sbarr.put(position, true);
            indices.add(position);
        }
    }
}