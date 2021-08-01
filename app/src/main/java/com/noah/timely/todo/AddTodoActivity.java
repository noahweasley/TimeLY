package com.noah.timely.todo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;

public class AddTodoActivity extends AppCompatActivity {
    private SchoolDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        database = new SchoolDatabase(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}