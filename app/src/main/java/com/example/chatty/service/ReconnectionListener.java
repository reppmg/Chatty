package com.example.chatty.service;

import android.util.Log;

import com.example.chatty.presenter.SessionCommunicator;
import com.opentok.android.Session;

/**
 * Created by 1 on 11.07.2017.
 */

class ReconnectionListener implements Session.ReconnectionListener {

    private SessionCommunicator mSessionCommunicator;
    private Pinger mPinger;

    private static final String TAG = ReconnectionListener.class.getSimpleName();

    public ReconnectionListener(SessionCommunicator mSessionCommunicator, Pinger pinger) {
        this.mSessionCommunicator = mSessionCommunicator;
        mPinger = pinger;
    }

    @Override
    public void onReconnecting(Session session) {
        Log.d(TAG, "onReconnecting: ");
        mSessionCommunicator.internetFailure();

    }

    @Override
    public void onReconnected(Session session) {
        Log.d(TAG, "onReconnected: ");
        mSessionCommunicator.showSubscriber();
        mPinger.sendPing();
    }
}
