package com.example.chatty.ui;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.example.chatty.App;
import com.example.chatty.presenter.Presenter;
import com.example.chatty.R;

import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements ViewContract, EasyPermissions.PermissionCallbacks {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    /*Containers for video*/
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    /*Views showed, when there is no opponent in chat*/
    private View mWaitingView;
    private View mErrorView;


    @Inject
    Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getAppComponent().inject(this);

        presenter.setViewContract(this);

        requestPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    /**
     * clearing connection and layout jn exit
     */
    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
        mSubscriberViewContainer.removeAllViews();
        mPublisherViewContainer.removeAllViews();
    }

    /*Permissions handling*/

    /**
     * Asks video and audio recording permissions from user
     */
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initializing view objects from layout
            mPublisherViewContainer = (FrameLayout) findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout) findViewById(R.id.subscriber_container);

            mWaitingView = View.inflate(this, R.layout.waiting_for_opponent_view, null);
            mErrorView = View.inflate(this, R.layout.error_view, null);
            mSubscriberViewContainer.addView(mWaitingView);

            // initialize and connect to the session
            Log.i(LOG_TAG, "subscribing");
            presenter.subscribe();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_needed_explanation), RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i("permissions", "granted: " + perms);
    }

    /**
     * If user haven't gave us some permission, ask him to do it in setting
     * otherwise, application is useless
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        Log.i("permissions", "denied: " + list);

        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_needed)
                .setMessage(R.string.permission_needed_details)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.chatty"));
                        startActivity(intent);
                        requestPermissions();
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .create()
                .show();
    }

    /*ViewContract interface methods*/

    /**
     * Set the preview from camera
     * @param view, containing video stream
     */
    @Override
    public void setPublisherSource(View view) {
        mPublisherViewContainer.removeAllViews();
        mPublisherViewContainer.addView(view);
    }

    /**
     * Set error message in area for opponent video
     */
    @Override
    public void setErrorView() {
        mSubscriberViewContainer.removeAllViews();
        mSubscriberViewContainer.addView(mErrorView);
    }

    /**
     * Clears view when opponent leaves chat
     */
    @Override
    public void dropView() {
        mSubscriberViewContainer.removeAllViews();
        mSubscriberViewContainer.addView(mWaitingView);
    }

    /**
     * Set the opponent's video
     * @param view, containing video stream
     */
    @Override
    public void setSubscriberSource(View view) {
        mSubscriberViewContainer.removeAllViews();
        mSubscriberViewContainer.addView(view);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
