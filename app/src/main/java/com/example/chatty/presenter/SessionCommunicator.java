package com.example.chatty.presenter;

import android.view.View;

/**
 * Interface for communication between service and presenter
 */

public interface SessionCommunicator {
    void dropSubscriberView();

    void streamReceived(View view);

    void showPublisher(View view);

    void onError();
}
