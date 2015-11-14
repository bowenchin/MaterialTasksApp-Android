package com.bowenchin.android.materialtasks.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bowenchin.android.materialtasks.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by bowenchin on 25/7/2015.
 */
public class TaskCameraFragment extends Fragment {
    private static final String TAG = "TaskCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME = "TaskCameraFragment.filename";
    public static final String EXTRA_PHOTO_ORIENTATION = "CameraPhotoOrientation";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;
    private OrientationEventListener mOrientationEventListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOrientationEventListener = new OrientationEventListener(getActivity()) {

            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d(TAG, "Orientation changed to " + orientation);

                if (orientation == ORIENTATION_UNKNOWN || mCamera == null)
                    return;

                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(0, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else { // back-facing camera
                    rotation = (info.orientation + orientation) % 360;
                }
                Camera.Parameters params = mCamera.getParameters();
                params.setRotation(rotation);
                Log.d(TAG, "Setting camera rotation to " + rotation);
                mCamera.setParameters(params);
            }
        };
    }

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        public void onShutter(){
            //DIsplay the progress indicator
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };
    private final Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Build filename
            String filename = UUID.randomUUID().toString() + "jpg";
            // Save file to disk
            FileOutputStream os = null;
            boolean success = true;
            try {
                os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                os.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + filename, e);
                success = false;
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file " + filename, e);
                    success = false;
                }
            }
            // Set photo filename on result intent
            if (success) {
                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, filename);
                TaskCameraActivity activity = (TaskCameraActivity)getActivity();
                int orientation = activity.getOrientation();

                i.putExtra(EXTRA_PHOTO_ORIENTATION, orientation);
                getActivity().setResult(Activity.RESULT_OK, i);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
            getActivity().finish();
        }
    };


    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_task_camera,parent,false);

        mProgressContainer = v.findViewById(R.id.task_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        FloatingActionButton takePictureButton = (FloatingActionButton)v.findViewById(R.id.task_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera != null){
                    mCamera.takePicture(mShutterCallback,null,mJpegCallback);
                }
            }
        });
        mSurfaceView = (SurfaceView)v.findViewById(R.id.task_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        //settype() and SURFACE_TYPE_PUSH_BUFFERS are deprecated, but required to work on pre-3.0 devices
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //Tell the camera to use this surface as its preview area
                try{
                    if(mCamera != null){
                        mCamera.setPreviewDisplay(holder);
                    }
                }catch (IOException exception){
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if(mCamera == null) return;

                //The surface has changed size; update the camera preview size
                Camera.Parameters parameters = mCamera.getParameters();
                Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(),width,height);
                parameters.setPreviewSize(s.width, s.height);
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(),width,height);
                parameters.setPictureSize(s.width,s.height);
                mCamera.setParameters(parameters);
                try{
                    mCamera.startPreview();
                }catch (Exception e){
                    Log.e(TAG,"Could not start preview",e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //We can no longer display on this surface, so stop preview
                if(mCamera != null){
                    mCamera.stopPreview();
                }
            }
        });

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();

            mCamera = Camera.open(0);
            //set camera to continually auto-focus
            Camera.Parameters params = mCamera.getParameters();
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else {
                //Choose another supported mode
            }

            mCamera.setParameters(params);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    /* A simple algorithm to get the largest size available.
     *For a more robust version, see CameraPreview.java in the ApiDemos sample app from Android
     */
    private Size getBestSupportedSize(List<Size> sizes, int width, int height){
        Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for(Size s : sizes){
            int area = s.width * s.height;
            if(area > largestArea){
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }

}