package com.bowenchin.android.materialtasks.model;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by bowenchin on 21/7/2015.
 */
public class TaskLab {
    private static TaskLab sTaskLab;
    private Context mAppContext;
    private ArrayList<Task> mTasks;
    private static final String TAG = "TaskLab";
    private static final String FILENAME = "tasks.json";
    private TaskJSONSerializer mSerializer;

    private TaskLab(Context appContext){
        mAppContext = appContext;
        //mTasks = new ArrayList<Task>();
        mSerializer = new TaskJSONSerializer(mAppContext,FILENAME);

        try{
            mTasks = mSerializer.loadTasks();
        }catch (Exception e){
            mTasks = new ArrayList<Task>();
            Log.e(TAG,"ERROR loading tasks: ", e);
        }

        //FOR TESTING PURPOSES: Generate random tasks
        /*for(int i = 1; i<=5;i++){
            Task t = new Task();
            t.setTitle("Task " + i);
            t.setChecked(i%2==1);
            mTasks.add(t);
        }*/
    }

    public static TaskLab get(Context c){
        if(sTaskLab == null){
            sTaskLab = new TaskLab(c.getApplicationContext());
        }
        return sTaskLab;
    }

    public void addTask(Task t){
        mTasks.add(t);
    }

    public void deleteTask(Task t){
        //If there are photos associated to the task instance, first takes care of them in the filesystem
        if(t.getPhoto() != null) {
            String path = mAppContext.getFileStreamPath(t.getPhoto().getFilename()).getAbsolutePath();
            File f = new File(path);
            f.delete();
            t.setPhoto(null);
        }
        mTasks.remove(t);
        saveTasks();
    }

    public boolean saveTasks(){
        try{
            mSerializer.saveTasks(mTasks);
            Log.d(TAG, "Tasks saved to file");
            return true;
        }catch (Exception e){
            Log.e(TAG, "Error saving tasks: ",e);
            return false;
        }
    }

    public ArrayList<Task> getTasks(){
        return mTasks;
    }

    public Task getTask(UUID id){
        for(Task c : mTasks){
            if(c.getId().equals(id))
                return c;
        }
        return null;
    }
}
