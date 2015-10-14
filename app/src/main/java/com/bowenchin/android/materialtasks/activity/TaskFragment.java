package com.bowenchin.android.materialtasks.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bowenchin.android.materialtasks.R;
import com.bowenchin.android.materialtasks.adapter.PictureUtils;
import com.bowenchin.android.materialtasks.model.Photo;
import com.bowenchin.android.materialtasks.model.Task;
import com.bowenchin.android.materialtasks.model.TaskLab;
import com.bowenchin.android.materialtasks.notification.NotifyService;
import com.bowenchin.android.materialtasks.notification.ScheduleClient;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


/**
 * Created by bowenchin on 21/7/2015.
 */
public class TaskFragment extends Fragment {
    private Task mTask;
    private EditText mTitleField;
    private EditText mSubjectField;
    private Button mDateButton;
    private CheckBox mDoneCheckBox;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private int mOrientation;
    private Callbacks mCallbacks;
    // This is a handle so that we can call methods on our service
    private ScheduleClient scheduleClient;
    private NotifyService notifyService;
    public static final String EXTRA_TASK_ID = "com.bowenchin.android.taskintent.task_id";
    public static final String DIALOG_DATE = "todo_date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final String DIALOG_IMAGE = "image";
    private static final String TAG = "TaskFragment";
    private Calendar calendar;

    public TaskFragment() {
        // Required empty public constructor
    }

    public static TaskFragment newInstance (UUID taskId){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TASK_ID, taskId);

        TaskFragment frag = new TaskFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mTask = new Task();

        // Create a new service client and bind our activity to this service
        scheduleClient = new ScheduleClient(getActivity());
        scheduleClient.doBindService();

        UUID taskId = (UUID)getActivity().getIntent().getSerializableExtra(EXTRA_TASK_ID);
        //UUID taskId = (UUID)getArguments().getSerializable(EXTRA_TASK_ID);
        mTask = TaskLab.get(getActivity()).getTask(taskId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_tall, container, false);

        //Set FAB of first activity to invisible
        FloatingActionButton fab2 = (FloatingActionButton)getActivity().findViewById(R.id.add_task);
        fab2.setVisibility(View.GONE);

        //Set check FAB to invisible first
        FloatingActionButton doneEdit = (FloatingActionButton) rootView.findViewById(R.id.done_edit);
        doneEdit.setVisibility(View.VISIBLE);

        mTitleField = (EditText)rootView.findViewById(R.id.task_title);
        mTitleField.setText(mTask.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Left blank intentionally
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String ed_text = mTitleField.getText().toString().trim();

                if(ed_text.isEmpty() || ed_text.length() == 0 || ed_text.equals(""))
                {
                    //EditText is empty
                    mTask.setTitle(" ");
                }
                else
                {
                    //EditText is not empty
                    mTask.setTitle(s.toString());
                }

                //mTask.setTitle(s.toString());
                mCallbacks.onTaskUpdated(mTask);
                getActivity().setTitle(mTask.getTitle());

                FloatingActionButton doneEdit = (FloatingActionButton) getView().findViewById(R.id.done_edit);
                doneEdit.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Left blank intentionally
            }
        });

        //Condition for FAB click to save current tas
            doneEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), TaskListActivity.class);
                    getActivity().startActivity(i);
                }
            });


        mSubjectField = (EditText)rootView.findViewById(R.id.task_subject);
        mSubjectField.setText(mTask.getSubject());
        mSubjectField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Left blank intentionally
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTask.setSubject(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Left blank intentionally
            }
        });

        mDateButton = (Button)rootView.findViewById(R.id.task_date);
        mDateButton.setText(DateFormat.format("EEEE, MMM dd, yyyy", mTask.getDate()).toString());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                //DatePickerFragment dialog = new DatePickerFragment();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mTask.getDate());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                dialog.show(fm,DIALOG_DATE);
            }
        });

        //mDoneCheckBox = (CheckBox)rootView.findViewById(R.id.task_checked);
        //mDoneCheckBox.setChecked(mTask.isChecked());
        /*mDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the task's completed property
                mTask.setChecked(isChecked);
                mCallbacks.onTaskUpdated(mTask);
            }
        });*/

        mPhotoButton = (ImageButton) rootView.findViewById(R.id.task_imageButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),TaskCameraActivity.class);
                startActivityForResult(i,REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView)rootView.findViewById(R.id.header);
        registerForContextMenu(mPhotoView);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo p = mTask.getPhoto();
                if (p == null)
                    return;

                FragmentManager fm = getActivity().getSupportFragmentManager();
                String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
                int orientation = p.getOrientation();
                ImageFragment.newInstance(path, orientation).show(fm, DIALOG_IMAGE);
            }
        });

        //If camera is not available, disable camera func.
        PackageManager pm = getActivity().getPackageManager();
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)&& !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            mPhotoButton.setEnabled(false);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    private void showPhoto() {
        // (Re)set the image button's image based on our photo
        Photo p = mTask.getPhoto();
        BitmapDrawable b = null;
        if (p != null) {
            String path = getActivity().getFileStreamPath(p.getFilename())
                    .getAbsolutePath();
            b = PictureUtils.getScaledDrawable(getActivity(), path);

            int orientation = p.getOrientation();
            if (orientation == TaskCameraActivity.ORIENTATION_PORTRAIT_INVERTED ||
                    orientation == TaskCameraActivity.ORIENTATION_PORTRAIT_NORMAL) {
                b = PictureUtils.getPortraitDrawable(mPhotoView, b);
            }
        }
        mPhotoView.setImageDrawable(b);
    }

    public void updateDate(){
        mDateButton.setText(DateFormat.format("EEEE, MMM dd, yyyy", mTask.getDate()).toString());;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(resultCode != Activity.RESULT_OK) return;
        if(requestCode == REQUEST_DATE){
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mTask.setDate(date);
            mCallbacks.onTaskUpdated(mTask);
            //Create a Calendar to get year, month, and day
            calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            Intent i = new Intent(getActivity(), NotifyService.class);
            i.putExtra(NotifyService.TODOTITLE, mTask.getTitle());
            i.putExtra(NotifyService.TODOSUBJECT, mTask.getSubject());

            //scheduleClient.setAlarmForNotification(calendar);


            // Notify the user what they just did
            Toast.makeText(getActivity().getApplicationContext(), "Reminder set for: " + day + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
            updateDate();
        }
        else if(requestCode == REQUEST_PHOTO){
            //Create a new Photo object and attach it to the task
            deletePhoto();
            String filename = data.getStringExtra(TaskCameraFragment.EXTRA_PHOTO_FILENAME);
            int orientation = data.getIntExtra(TaskCameraFragment.EXTRA_PHOTO_ORIENTATION, 0);
            if (filename != null) {
                Photo p = new Photo(filename,orientation);
                mTask.setPhoto(p);
                mCallbacks.onTaskUpdated(mTask);
                showPhoto();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        getActivity().getMenuInflater().inflate(R.menu.task_photo_delete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_delete_photo:
                if (mTask.getPhoto() != null){
                    String path = getActivity().getFileStreamPath(mTask.getPhoto().getFilename()).getAbsolutePath();
                    File f = new File(path);
                    f.delete();
                    mTask.setPhoto(null);
                    mPhotoView.setImageDrawable(null);
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deletePhoto() {
        if (mTask.getPhoto() != null){
            String path = getActivity().getFileStreamPath(mTask.getPhoto().getFilename()).getAbsolutePath();
            File f = new File(path);
            f.delete();
            mTask.setPhoto(null);
        }
    }

    public interface Callbacks{
        void onTaskUpdated(Task task);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onStart(){
        super.onStart();
        showPhoto();
    }

    @Override
    public void onPause(){
        super.onPause();
        scheduleClient.setAlarmForNotification(calendar);
        TaskLab.get(getActivity()).saveTasks();
    }

    @Override
    public void onStop(){
        super.onStop();
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if(scheduleClient != null) {
            scheduleClient.doUnbindService();
        }

        PictureUtils.cleanImageView(mPhotoView);
    }
}



