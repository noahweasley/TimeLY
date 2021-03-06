package com.noah.timely.core;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy class that implements {@link ChoiceMode}. Multi-choice mode signifies that the list
 * can be multi-selected, to perform a bulk action on the individual list items
 */
public class MultiChoiceMode implements ChoiceMode {
    public static final String ARG_STATES = "Choice states";
    public static final String ARG_POS_INDICES = "Position indices";
    public static final String ARG_ID_INDICES = "Id Indices";

    // States to be saved
    private List<Integer> indices = new ArrayList<>();
    private List<Integer> indices2 = new ArrayList<>();
    private ParcelableSparseBooleanArray sbarr = new ParcelableSparseBooleanArray();

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

    @Override
    public boolean isChecked(int position) {
        return sbarr.get(position);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable(ARG_STATES, sbarr);
        bundle.putIntegerArrayList(ARG_ID_INDICES, (ArrayList<Integer>) indices);
        bundle.putIntegerArrayList(ARG_POS_INDICES, (ArrayList<Integer>) indices2);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        sbarr = bundle.getParcelable(ARG_STATES);
        indices = bundle.getIntegerArrayList(ARG_ID_INDICES);
        indices2 = bundle.getIntegerArrayList(ARG_POS_INDICES);
    }

    @Override
    public int getCheckedChoiceCount() {
        return sbarr.size();
    }

    @Override
    public void clearChoices() {
        sbarr.clear();
        indices.clear();
        indices2.clear();
    }

    @Override
    public Integer[] getCheckedChoicesIndices() {
        return indices.toArray(new Integer[0]);
    }

    @Override
    public Integer[] getCheckedChoicePositions() {
        return indices2.toArray(new Integer[0]);
    }

    /**
     * Use this instead of {@link MultiChoiceMode#setChecked(int, boolean)} for Data Models.
     *
     * @param position the position in which the its checked value is to be inserted
     * @param checked  the status of the checked item
     * @param dataPos  the original data position in database
     */
    public void setChecked(int position, boolean checked, int dataPos) {
        if (!checked) {
            sbarr.delete(position);
            indices.remove(Integer.valueOf(position));
            indices2.remove(Integer.valueOf(dataPos));
        } else {
            sbarr.put(position, true);
            indices.add(position);
            indices2.add(dataPos);
        }
    }
}