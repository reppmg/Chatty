package com.example.chatty.presenter;

import android.view.View;

/**
 * Interface for communication between service and presenter
 */

public interface SessionCommunicator {
    void dropSubscriberView();

    void showSubscriber();

    void showPublisher();

    void onError();

    void internetFailure();

}
