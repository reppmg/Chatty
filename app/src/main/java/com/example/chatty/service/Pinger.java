package com.example.chatty.service;

import android.os.CountDownTimer;
import android.util.Log;

import com.example.chatty.presenter.SessionCommunicator;
import com.opentok.android.Connection;
import com.opentok.android.Session;

/**
 * Class responsible for detecting connection losses
 * ping opponent every 0.5s and marks opponent as disconnected after 6 seconds of no response
 */

class Pinger implements Session.SignalListener {

    private static final String TAG = Pinger.class.getSimpleName();
    private SessionCommunicator mSessionCommunicator;
    private Session mSession;
    private boolean disconnectedOccurred = false;
    private CountDownTimer mTimeoutTimer;
    private final CountDownTimer pingDelayTimer;

    Pinger(SessionCommunicator sessionCommunicator, Session session) {
        this.mSessionCommunicator = sessionCommunicator;
        mSession = session;
        mTimeoutTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "connection timed out");
                if (!disconnectedOccurred)
                    mSessionCommunicator.internetFailure();
                disconnectedOccurred = true;
            }
        };

        pingDelayTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "sendPing: ");
                try {
                    mSession.sendSignal("ping", mSession.getConnection().getConnectionId());
                    mTimeoutTimer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    /*Listens for signals from session, and if it's ping signal,
     resets timeout timer and sends ping signal to session*/
    @Override
    public synchronized void onSignalReceived(Session session, String s, String s1, Connection connection) {
        Log.d(TAG, "onSignalReceived: received a signal");
        if (s.equals("ping") && !s1.equals(mSession.getConnection().getConnectionId())) {
            Log.d(TAG, "onSignalReceived: ping signal received");
            mTimeoutTimer.cancel();
            sendPing();
            if (disconnectedOccurred) {
                mSessionCommunicator.showSubscriber();
                disconnectedOccurred = false;
            }
        }
    }

    void sendPing() {
        pingDelayTimer.cancel();
        pingDelayTimer.start();
    }

    void stop() {
        mTimeoutTimer.cancel();
        pingDelayTimer.cancel();
    }
}
