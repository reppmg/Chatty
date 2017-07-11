package com.example.chatty.service;

import android.os.CountDownTimer;
import android.util.Log;

import com.example.chatty.presenter.SessionCommunicator;
import com.opentok.android.Connection;
import com.opentok.android.Session;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 1 on 11.07.2017.
 */

public class Pinger implements Session.SignalListener {

    private static final String TAG = Pinger.class.getSimpleName();
    private SessionCommunicator mSessionCommunicator;
    private Session mSession;
    private boolean disconnectedOccurred = false;
    private boolean initiatedByOpponent = false;
    private CountDownTimer mTimer;

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
    }

    @Override
    public void onSignalReceived(Session session, String s, String s1, Connection connection) {
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
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "sendPing: ");
                mSession.sendSignal("ping", mSession.getConnection().getConnectionId());
                mTimer.start();
            }
        }.start();
    }
}
