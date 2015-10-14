package com.bowenchin.android.materialtasks.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bowenchin.android.materialtasks.R;
import com.bowenchin.android.materialtasks.model.Task;

import java.util.UUID;


/**
 * Created by bowenchin on 21/7/2015.
 */
public class TaskView extends AppCompatActivity implements TaskFragment.Callbacks {
    private Toolbar toolbar;
    public static final String EXTRA_TRANSITION = "TaskView:Transition";

    /*public TaskView() {
        // Required empty public constructor
    }*/

    public void onTaskUpdated(Task task){
    }

    protected Fragment createFragment(){
        UUID taskId = (UUID)getIntent().getSerializableExtra(TaskFragment.EXTRA_TASK_ID);
        return TaskFragment.newInstance(taskId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Preferences.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}