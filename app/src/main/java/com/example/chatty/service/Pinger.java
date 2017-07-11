package com.example.chatty.service;

import android.os.CountDownTimer;
import android.util.Log;

import com.example.chatty.presenter.SessionCommunicator;
import com.opentok.android.Connection;
import com.opentok.android.Session;

/**
 * Created by 1 on 11.07.2017.
 */

public class Pinger implements Session.SignalListener {

    private static final String TAG = Pinger.class.getSimpleName();
    private SessionCommunicator mSessionCommunicator;
    private Session mSession;
    private boolean disconnectedOccurred = false;
    private CountDownTimer mTimer;
    private final CountDownTimer pingDelayTimer;

    public Pinger(SessionCommunicator sessionCommunicator, Session session) {
        this.mSessionCommunicator = sessionCommunicator;
        mSession = session;
        mTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish: no response for 3 second");
                if (!disconnectedOccurred)
                    mSessionCommunicator.internetFailure();
                disconnectedOccurred = true;
            }
        };

        pingDelayTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "sendPing: ");
                try {
                    mSession.sendSignal("ping", mSession.getConnection().getConnectionId());
                    mTimer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public synchronized void onSignalReceived(Session session, String s, String s1, Connection connection) {
        Log.d(TAG, "onSignalReceived: received a signal");
        if (s.equals("ping") && !s1.equals(mSession.getConnection().getConnectionId())) {
            Log.d(TAG, "onSignalReceived: ping signal received");
            mTimer.cancel();
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

    public void stop() {
        mTimer.cancel();
        pingDelayTimer.cancel();
    }
}
