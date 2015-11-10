package com.bowenchin.android.materialtasks.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bowenchin.android.materialtasks.R;
import com.bowenchin.android.materialtasks.model.Task;
import com.bowenchin.android.materialtasks.model.TaskJSONSerializer;
import com.bowenchin.android.materialtasks.model.TaskLab;

import java.util.ArrayList;

/**
 * Created by bowenchin on 21/7/2015.
 */
public class TaskListActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener, TaskListFragment.Callbacks, TaskFragment.Callbacks {
        private FloatingActionButton FAB;


        private static String TAG = MainActivity.class.getSimpleName();

        private Toolbar mToolbar;
        private FragmentDrawer drawerFragment;

        //Defining Variables
        private Toolbar toolbar;
        private NavigationView navigationView;
        private DrawerLayout drawerLayout;
        private ArrayList<Task> mTasks;
        private LinearLayout emptyView;

        public int getLayoutResId(){
            return R.layout.fragment_list;
            //return R.layout.activity_masterdetail;
        }

        public void onTaskSelected(Task task){
            if(findViewById(R.id.detailFragmentContainer)==null){
                //Start an instance of TaskView.class
                Intent i = new Intent(this, TaskView.class);
                i.putExtra(TaskFragment.EXTRA_TASK_ID,task.getId());
                startActivity(i);
            }
            else{
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
                Fragment newDetail = TaskFragment.newInstance(task.getId());

                if(oldDetail != null){
                    ft.remove(oldDetail);
                }

                ft.add(R.id.detailFragmentContainer,newDetail);
                ft.commit();
            }
        }

        public void onTaskUpdated(Task task){
            FragmentManager fm = getSupportFragmentManager();
            TaskListFragment listFragment = (TaskListFragment)fm.findFragmentById(R.id.frame);
            listFragment.updateUI();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Preferences.applyTheme(this);
            super.onCreate(savedInstanceState);
            //setContentView(R.layout.fragment_list);
            setContentView(getLayoutResId());

            // Initializing Toolbar and setting it as the actionbar
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Initializing NavigationView
            navigationView = (NavigationView) findViewById(R.id.navigation_view);

            //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

                     // This method will trigger on item Click of navigation menu
                     @Override
                     public boolean onNavigationItemSelected(MenuItem menuItem) {


                         //Checking if the item is in checked state or not, if not make it in checked state
                         if (menuItem.isChecked())
                             menuItem.setChecked(false);
                         else menuItem.setChecked(true);

                         //Closing drawer on item click
                         drawerLayout.closeDrawers();

                         //Check to see which item was being clicked and perform appropriate action
                         String title = getString(R.string.app_name);
                         switch (menuItem.getItemId()) {
                             case R.id.home:
                                 //Intent myIntent = new Intent(TaskListActivity.this, TaskListActivity.class);
                                 //startActivity(myIntent);
                                 title = getString(R.string.app_name);
                                 return true;
                             case R.id.settings:
                                 Intent launchSettingsIntent = new Intent(TaskListActivity.this, SettingsActivity.class);
                                 startActivity(launchSettingsIntent);
                                 return true;
                             default:
                                 return true;
                         }
                     }
                 });
            // Initializing Drawer Layout and ActionBarToggle
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

                @Override
                public void onDrawerClosed(View drawerView) {
                    // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                    super.onDrawerClosed(drawerView);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                    super.onDrawerOpened(drawerView);
                }
            };

            //Setting the actionbarToggle to drawer layout
            drawerLayout.setDrawerListener(actionBarDrawerToggle);

            //calling sync state is necessay or else your hamburger icon wont show up
            actionBarDrawerToggle.syncState();

            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.frame);
            if(fragment == null){
                fragment = new TaskListFragment();
                fm.beginTransaction().add(R.id.frame,fragment).commit();
            }

            //Check if app is launch on first time. If yes, display dialogue help box
            if (isFirstTime()) {
                // show dialog
                //new AlertDialog.Builder(this).setTitle("Welcome to Material Tasks").setMessage("Add a new to-do task by tapping on the \"+\" button to get started! \n\nLong tap on any task to bring up the delete menu. \n\nLearn more by going to \"Settings\".").setNeutralButton("Got It", null).show();
                Intent intent = new Intent(this, WalkThroughActivity.class); //call your ViewPager class
                startActivity(intent);
            }

            /*emptyView = (LinearLayout)findViewById(R.id.toDoEmptyView);
            emptyView.setVisibility(View.INVISIBLE);
            if(height<=0){
                emptyView.setVisibility(View.VISIBLE);
            }*/

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                Intent launchSettingsIntent = new Intent(TaskListActivity.this, SettingsActivity.class);
                startActivity(launchSettingsIntent);
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onDrawerItemSelected(View view, int position) {
            displayView(position);
        }

        private void displayView(int position) {
            Fragment fragment = null;
            FragmentActivity fragAct = null;
            PreferenceFragment pref = null;
            String title = getString(R.string.app_name);
            switch (position) {
                case 0:
                    //Intent myIntent = new Intent(TaskListActivity.this, TaskListActivity.class);
                    //startActivity(myIntent);
                    title = getString(R.string.app_name);
                    break;
                case 1:
                    Intent launchSettingsIntent = new Intent(TaskListActivity.this, SettingsActivity.class);
                    startActivity(launchSettingsIntent);
                    break;
                default:
                    break;
            }

            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment);
                fragmentTransaction.commit();

                // set the toolbar title
                getSupportActionBar().setTitle(title);
            }
        }
    }

