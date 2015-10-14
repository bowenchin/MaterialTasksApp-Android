package com.bowenchin.android.materialtasks.notification;

/**
 * Created by bowenchin on 25/7/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bowenchin.android.materialtasks.R;
import com.bowenchin.android.materialtasks.activity.MainActivity;
import com.bowenchin.android.materialtasks.activity.TaskListActivity;
import com.bowenchin.android.materialtasks.model.Task;

import java.util.UUID;

/**
 * This service is started when an Alarm has been raised
 *
 * We pop a notification into the status bar for the user to click on
 * When the user clicks the notification a new activity is opened
 *
 * @author paul.blundell
 */
public class NotifyService extends Service {
        private CharSequence mTitle;
        private CharSequence mSubject;

    private String mTodoTitle;
    private String mTodoSubject;

    public static final String TODOTITLE = "com.bowenchin.mTasknotificationservicetext";
    public static final String TODOSUBJECT = "com.bowenchin.mTasknotificationserviceuuid";
    /**
     * Class for clients to access
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    protected void onHandleIntent(Intent intent) {
        mTodoTitle = intent.getStringExtra(TODOTITLE);
        mTodoSubject = intent.getStringExtra(TODOSUBJECT);
    }

    // Unique id to identify the notification.
    private static final int NOTIFICATION = 123;
    // Name of an intent extra we can use to identify if this service was started to create a notification
    public static final String INTENT_NOTIFY = "com.bowenchin.android.notificaiton.INTENT_NOTIFY";
    // The system notification manager
    private NotificationManager mNM;

    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // If this service was started by out AlarmTask intent then we want to show our notification
        if(intent.getBooleanExtra(INTENT_NOTIFY, false))
            showNotification();

        // We don't care if this service is stopped as we have already delivered our notification
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients
    private final IBinder mBinder = new ServiceBinder();

    /**
     * Creates a notification and shows it in the OS drag-down status bar
     */
    private void showNotification() {
        // This is the 'title' of the notification
        CharSequence title = getString(R.string.app_name);
        //CharSequence title = mTodoTitle;

        // This is the icon to use on the notification
        int icon = R.drawable.ic_notification_toggle_icon;
        // This is the scrolling text of the notification
        CharSequence text = getString(R.string.notification_alert);
        //CharSequence text = mTodoSubject;

        // What time to show on the notification
        long time = System.currentTimeMillis();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setLights(0xffffffff, 300, 300)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, TaskListActivity.class);

        //Notification notification = new Notification(icon, text, time);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TaskListActivity.class), 0);

        mBuilder.setAutoCancel(true);

        // Send the notification to the system.
        mBuilder.setContentIntent(contentIntent);
        mNM.notify(NOTIFICATION, mBuilder.build());

        // Stop the service when we are finished
        stopSelf();
    }


    public CharSequence getNotificationTitle(){
        return mTitle;
    }

    public void setNotificationTitle(String title)
    {
        CharSequence t = title;
        mTitle = t;
    }

    public CharSequence getNotificationText(){
        return mSubject;
    }

    public void setNotificationText(String text){
        CharSequence t = text;
        mSubject = t;
    }
}