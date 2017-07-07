package com.example.chatty.presenter;

import android.app.Application;
import android.util.Log;
import android.view.View;

import com.example.chatty.App;
import com.example.chatty.service.SessionCommunicator;
import com.example.chatty.service.SessionService;
import com.example.chatty.ui.ViewContract;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Subscriber;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;

/**
 * Created by 1 on 06.07.2017.
 */

public class PresenterImpl implements Presenter, SessionCommunicator {

    private static final String LOG_TAG = PresenterImpl.class.getSimpleName();
    private static String API_KEY ;
    private static String SESSION_ID;
    private static String TOKEN;

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

//    @Inject
//    ViewContract mViewContract;

    @Inject
    SessionService sessionService;

    @Inject
    Application application;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ViewContract mViewContract;

    public PresenterImpl() {
        App.getAppComponent().inject(this);
        sessionService.setPresenter(this);
    }

    @Override
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    public void requestPermissions() {

        Log.i("info","wqeqw0");

//        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
//        if (EasyPermissions.hasPermissions(mViewContract.getContext(), perms)) {
//            // initialize view objects from your layout
//            mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
//            mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
//            mViewContract.permissionsGranted();


            // initialize and connect to the session
//            presenter.fetchData();
//        } else {
//            EasyPermissions.requestPermissions(mViewContract.getContext(), "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
//        }

    }

    public void setViewContract(ViewContract viewContract) {
        this.mViewContract = viewContract;
    }

    @Override
    public void fetchData() {
        sessionService.fetchSessionConnectionData();
    }

    @Override
    public void inject(ViewContract viewContract) {
        mViewContract = viewContract;
    }


    public void onNewSubscriber(View view){
        mViewContract.setPublisherSource(view);
    }

    public void dropView() {
        mViewContract.dropView();
    }

    public void streamReceived(View view) {
        mViewContract.updateSubscriberSource(view);
    }
}
