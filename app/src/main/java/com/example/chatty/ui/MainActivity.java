package com.example.chatty.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.chatty.App;
import com.example.chatty.presenter.Presenter;
import com.example.chatty.R;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements ViewContract, EasyPermissions.PermissionCallbacks {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private static final String TAG = MainActivity.class.getSimpleName();

    /*Containers for video*/
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    /*Views showed, when there is no opponent in chat*/
    private View mWaitingView;
    private View mErrorView;


    private boolean onSavedWasCalled = false;


    @Inject
    Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: savedState = null: " + (savedInstanceState == null));

        onSavedWasCalled = false;

        App.getAppComponent().inject(this);

        presenter.setViewContract(this);

        requestPermissions();

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    /**
     * clearing connection and layout on exit
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");

        if (!onSavedWasCalled) {
            //assuming this is exit case
            presenter.unsubscribe();
            presenter.disconnect();
            finish();
        }

        try {
            mSubscriberViewContainer.removeAllViews();
            mPublisherViewContainer.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        finish();
    }

    /*Permissions handling*/

    /**
     * Asks video and audio recording permissions from user
     */
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private boolean requestPermissions() {
        Log.d(TAG, "requestPermissions: ");
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initializing view objects from layout
            initViewContainers();

            // initialize and connect to the session
            Log.i(LOG_TAG, "subscribing");
            if (presenter.isInSession()) {
                setSubscriberSource(presenter.getSubscriberView());
                setPublisherSource(presenter.getPublisherView());
            } else {
                presenter.subscribe();
            }
            return true;
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_needed_explanation), RC_VIDEO_APP_PERM, perms);
        }
        return false;
    }

    private void initViewContainers() {
        mPublisherViewContainer = (FrameLayout) findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout) findViewById(R.id.subscriber_container);

        mWaitingView = View.inflate(this, R.layout.waiting_for_opponent_view, null);
        mErrorView = View.inflate(this, R.layout.error_view, null);
        mSubscriberViewContainer.addView(mWaitingView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: grant=" + Arrays.toString(grantResults));
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
     *
     * @param view, containing video stream
     */
    @Override
    public void setPublisherSource(View view) {
        if (view != null) {
            mPublisherViewContainer.removeAllViews();
            mPublisherViewContainer.addView(view);
            view.bringToFront();
//            mSubscriberViewContainer.removeAllViews();
//            mSubscriberViewContainer.addView(view);
        }
    }

    /**
     * Set the opponent's video
     *
     * @param view, containing video stream
     */
    @Override
    public void setSubscriberSource(View view) {
        if (view != null) {
//            mPublisherViewContainer.removeAllViews();
//            mPublisherViewContainer.addView(view);
            mSubscriberViewContainer.removeAllViews();
            mSubscriberViewContainer.addView(view);
        }
    }

    /**
     * Set error message in area for opponent video
     */
    @Override
    public void setSubscriberErrorView(boolean disconnected) {
        TextView errorText = (TextView) mErrorView.findViewById(R.id.error_text);
        if (disconnected) {
            errorText.setText(R.string.reconnect_waiting);
        } else {
            errorText.setText(getString(R.string.error_string));
        }
        mSubscriberViewContainer.removeAllViews();
        mSubscriberViewContainer.addView(mErrorView);
    }

    /**
     * Clears view when opponent leaves chat
     */
    @Override
    public void dropSubscriberView() {
        mSubscriberViewContainer.removeAllViews();
        mSubscriberViewContainer.addView(mWaitingView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        onSavedWasCalled = true;
        presenter.onSaveInstanceState(outState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        Log.d(TAG, "onRestoreInstanceState: bundle is null " + (savedInstanceState == null));
////        super.onRestoreInstanceState(savedInstanceState);
////        presenter.onRestoreState(savedInstanceState);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
