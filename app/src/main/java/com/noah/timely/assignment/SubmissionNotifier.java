package com.noah.timely.assignment;

import android.content.Context;
import android.content.Intent;

import com.noah.timely.core.PositionMessageEvent;
import com.noah.timely.core.SchoolDatabase;

import org.greenrobot.eventbus.EventBus;

import static com.noah.timely.assignment.AddAssignmentActivity.POSITION;

/**
 * A {@link AssignmentNotifier} that sends notification reminding user that assignment's are
 * to be submitted the that day
 */
public class SubmissionNotifier extends AssignmentNotifier {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        SchoolDatabase database = new SchoolDatabase(context);
        int updatePos = intent.getIntExtra(POSITION, -1);
        boolean isUpdated = database.updateAssignmentStatus(updatePos, true);
        if (isUpdated)
            EventBus.getDefault().post(new PositionMessageEvent(updatePos));
    }

    @Override
    public String getDay() {
        return "today";
    }
}
