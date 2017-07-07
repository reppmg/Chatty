package com.example.chatty.presenter;

import android.view.View;

import com.example.chatty.service.SessionService;
import com.example.chatty.ui.ViewContract;


public class PresenterImpl implements Presenter, SessionCommunicator {
    private static final String LOG_TAG = PresenterImpl.class.getSimpleName();

    private SessionService mSessionService;

    private ViewContract mViewContract;


    public void setViewContract(ViewContract viewContract) {
        this.mViewContract = viewContract;
    }

    @Override
    public void unsubscribe() {
        if (mSessionService != null) {
            mSessionService.unsubscribe();

        }
        mSessionService = null;
    }

    @Override
    public void subscribe() {
        mSessionService = new SessionService(mViewContract.getContext());
        mSessionService.setPresenter(this);
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
