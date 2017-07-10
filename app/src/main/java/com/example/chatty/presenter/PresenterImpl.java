package com.example.chatty.presenter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.chatty.App;
import com.example.chatty.service.SessionService;
import com.example.chatty.ui.ViewContract;

import javax.inject.Inject;


public class PresenterImpl implements Presenter, SessionCommunicator {
    private static final String LOG_TAG = PresenterImpl.class.getSimpleName();
    private static final String API_KEY_TAG = "apiKey";
    private static final String TOKEN_TAG = "token";
    private static final String SESSION_TAG = "session";
    private static final String TAG = PresenterImpl.class.getSimpleName();


    private ViewContract mViewContract;

    @Inject
    SessionService mSessionService;

    public PresenterImpl() {
        App.getAppComponent().inject(this);
        mSessionService.setPresenter(this);
    }

    public void setViewContract(ViewContract viewContract) {
        this.mViewContract = viewContract;
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
        if (mSessionService != null){
            mSessionService.disconnect();
        }
    }

    @Override
    public void onRestoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null
                && savedInstanceState.containsKey(API_KEY_TAG)
                && savedInstanceState.containsKey(SESSION_TAG)
                && savedInstanceState.containsKey(TOKEN_TAG)) {
//            final String apiKey = savedInstanceState.getString(API_KEY_TAG);
//            final String session = savedInstanceState.getString(SESSION_TAG);
//            final String token = savedInstanceState.getString(TOKEN_TAG);
//
//            mSessionService.restoreSession(apiKey, session, token);
            Log.d(TAG, "onRestoreState: saved state is valid");
            mSessionService.restoreSession();
            View publisherView = mSessionService.getPublisherView();
            View subscriberView = mSessionService.getSubscriberView();
//            if (publisherView != null && subscriberView != null) {
            mViewContract.setPublisherSource(publisherView);
            mViewContract.setSubscriberSource(subscriberView);
//            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(API_KEY_TAG, mSessionService.getApiKey());
        outState.putString(TOKEN_TAG, mSessionService.getToken());
        outState.putString(SESSION_TAG, mSessionService.getSessionId());
    }

    @Override
    public void subscribe() {
        mSessionService.fetchSessionConnectionData();
    }


    public void showPublisher(View view) {
        mViewContract.setPublisherSource(view);
    }


    @Override
    public void onError() {
        mViewContract.setSubscriberErrorView();
        mSessionService.unsubscribe();
        mSessionService.fetchSessionConnectionData();
    }

    public void dropSubscriberView() {
        mViewContract.dropSubscriberView();
    }

    public void streamReceived(View view) {
        mViewContract.setSubscriberSource(view);
    }
}
