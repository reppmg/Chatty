package com.example.chatty.presenter;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import com.example.chatty.App;
import com.example.chatty.service.SessionService;
import com.example.chatty.ui.ViewContract;

import javax.inject.Inject;



public class PresenterImpl implements Presenter, SessionCommunicator {
    private static final String TAG = PresenterImpl.class.getSimpleName();

    private ViewContract mViewContract;

    @Inject
    SessionService mSessionService;

    public PresenterImpl() {
        App.getAppComponent().inject(this);
        mSessionService.setPresenter(this);
    }


    @Override
    public void subscribe() {
        mSessionService.fetchSessionConnectionData();
    }

    /**
     * Tells server, that user, obtained session, is gone and session should be lost
     */
    @Override
    public void unsubscribe() {
        if (mSessionService != null) {
            mSessionService.unsubscribe();
        }
    }

    @Override
    public boolean isInSession() {
        return mSessionService.isInSession();
    }

    @Override
    public View getSubscriberView() {
        return mSessionService.getSubscriberView();
    }

    @Override
    public View getPublisherView() {
        return mSessionService.getPublisherView();
    }

    @Override
    public void disconnect() {
        if (mSessionService != null) {
            mSessionService.disconnect();
        }
    }


    @Override
    public void onResume() {
        if (mSessionService.resumeSession()){
            mViewContract.setPublisherSource(mSessionService.getPublisherView());
            mViewContract.setSubscriberSource(mSessionService.getSubscriberView());
        }
    }

    @Override
    public void onPause() {
        mSessionService.pauseSession();
    }

    @Override
    public void onError() {
        mViewContract.setSubscriberErrorView(false);
        mSessionService.unsubscribe();
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mSessionService.fetchSessionConnectionData();
            }
        }.start();
    }

    public void dropSubscriberView() {
        mViewContract.dropSubscriberView();
    }

    public void showSubscriber() {
        mViewContract.setSubscriberSource(mSessionService.getSubscriberView());
    }

    public void showPublisher() {
        mViewContract.setPublisherSource(mSessionService.getPublisherView());
    }

    @Override
    public void internetFailure() {
        mViewContract.setSubscriberErrorView(true);
    }


    public void setViewContract(ViewContract viewContract) {
        this.mViewContract = viewContract;
    }
}

