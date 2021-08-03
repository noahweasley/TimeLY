package com.noah.timely.todo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;

public class AddTodoActivity extends AppCompatActivity {
    private SchoolDatabase database;
    private String category;
    public static final String EXTRA_IS_EDITABLE = "com.noah.timely.todo.edit";
    public static final String EXTRA_DEFAULT_CATEGORY = "com.noah.timely.todo.category.default";
    public static final String[] CATEGORIES = {"Miscelaneous", "Work", "Music", "Creativity", "Travel", "Study"
            , "Leisure and Fun", "Home", "Shopping"};

    public static void start(Context context, boolean toEdit, String defCategory) {
        Intent starter = new Intent(context, AddTodoActivity.class);
        starter.putExtra(EXTRA_IS_EDITABLE, toEdit);
        starter.putExtra(EXTRA_DEFAULT_CATEGORY, defCategory);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        database = new SchoolDatabase(this);
        setSupportActionBar(findViewById(R.id.toolbar));

        boolean isEditable = getIntent().getBooleanExtra(EXTRA_IS_EDITABLE, false);
        getSupportActionBar().setTitle(isEditable ? "Update Task" : "New Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btn_addTask = findViewById(R.id.add_task);
        btn_addTask.setOnClickListener(v -> addTask());

        Spinner spin_category = findViewById(R.id.category);

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                                                                R.layout.simple_spinner_item,
                                                                CATEGORIES);

        courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);

        spin_category.setAdapter(courseAdapter);
        spin_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = CATEGORIES[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addTask() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}