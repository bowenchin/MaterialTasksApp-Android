package com.bowenchin.android.materialtasks.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bowenchin.android.materialtasks.R;
import com.bowenchin.android.materialtasks.model.Task;
import com.bowenchin.android.materialtasks.model.TaskLab;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {
    private int SETTINGS_ACTION = 1;
    private FloatingActionButton FAB;
    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private ViewPager mViewPager;
    private ArrayList<Task> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null){
            fragment = new TaskListFragment();
            fm.beginTransaction().add(R.id.fragment_container,fragment).commit();
        }

        //Check if app is launch on first time. If yes, display dialogue help box
        if (isFirstTime()) {
            // show dialog
            new AlertDialog.Builder(this).setTitle("Welcome to Material Tasks").setMessage("Add a new to-do task by tapping on the \"+\" button to get started! \n \nLearn more by going to \"Settings\".").setNeutralButton("Got It", null).show();
        }

        //FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.add_task);
        FAB = (FloatingActionButton) findViewById(R.id.add_task);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "FAB clicked");
                Task task = new Task();
                TaskLab.get(getApplicationContext()).addTask(task);
                Intent i = new Intent(getApplicationContext(), TaskView.class);
                i.putExtra(TaskFragment.EXTRA_TASK_ID, task.getId());
                startActivityForResult(i, 0);
            }
        });
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void gotoSettings(View view) {
        Intent launchSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(launchSettingsIntent);
    }

    private void gotoHome() {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                gotoHome();
                return true;
            case R.id.action_settings:
                gotoSettings(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    @Override
    protected void onResume(){
        super.onResume();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_task);
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause(){
        super.onPause();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_task);
        fab.setVisibility(View.GONE);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        FragmentActivity fragAct = null;
        PreferenceFragment pref = null;

        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(myIntent);
                title = getString(R.string.app_name);
                break;
            case 1:
                Intent launchSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(launchSettingsIntent);
                break;
            /*case 2:
                Log.i(TAG, "Drawer: Add Task clicked");
                Task task = new Task();
                TaskLab.get(getApplicationContext()).addTask(task);
                Intent i = new Intent(getApplicationContext(), TaskView.class);
                i.putExtra(TaskFragment.EXTRA_TASK_ID, task.getId());
                startActivityForResult(i, 0);
                break;*/
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }
}