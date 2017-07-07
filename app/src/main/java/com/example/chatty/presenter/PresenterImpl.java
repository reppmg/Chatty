package com.example.chatty.presenter;

import android.view.View;

import com.example.chatty.App;
import com.example.chatty.service.SessionService;
import com.example.chatty.ui.ViewContract;

import javax.inject.Inject;


public class PresenterImpl implements Presenter, SessionCommunicator {
    private static final String LOG_TAG = PresenterImpl.class.getSimpleName();


    private ViewContract mViewContract;

    @Inject
    SessionService mSessionService;

    public PresenterImpl(){
        App.getAppComponent().inject(this);
        mSessionService.setPresenter(this);
    }

    public void setViewContract(ViewContract viewContract) {
        this.mViewContract = viewContract;
    }

    @Override
    public void onStart() {
        mSessionService.fetchSessionConnectionData();

    }

    @Override
    public void unsubscribe() {
        if (mSessionService != null) {
            mSessionService.unsubscribe();
            mSessionService.disconnect();
        }
    }

    @Override
    public void subscribe() {
        mSessionService.fetchSessionConnectionData();
    }


    public void onNewSubscriber(View view) {
        mViewContract.setPublisherSource(view);
    }


    @Override
    public void onError() {
        mViewContract.setErrorView();
        mSessionService.unsubscribe();
        mSessionService.fetchSessionConnectionData();
    }

    public void dropView() {
        mViewContract.dropView();
    }

    public void streamReceived(View view) {
        mViewContract.setSubscriberSource(view);
    }
}
