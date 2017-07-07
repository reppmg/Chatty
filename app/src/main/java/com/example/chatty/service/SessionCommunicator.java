package com.example.chatty.service;

import android.view.View;

/**
 * Created by 1 on 07.07.2017.
 */

public interface SessionCommunicator {
    void dropView();

    void streamReceived(View view);

    void onNewSubscriber(View view);
}
