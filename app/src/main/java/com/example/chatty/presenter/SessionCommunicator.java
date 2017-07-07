package com.example.chatty.presenter;

import android.view.View;

/**
 * Interface for communication between service and presenter
 */

public interface SessionCommunicator {
    void dropView();

    void streamReceived(View view);

    void onNewSubscriber(View view);

    void onError();
}
