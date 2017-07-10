package com.example.chatty.presenter;

import android.os.Bundle;
import android.view.View;

import com.example.chatty.ui.ViewContract;

/**
 * Interface for communication between Presenter and Activity
 */
public interface Presenter  {

    void subscribe();

    void setViewContract(ViewContract mainActivity);

    void unsubscribe();

    void onSaveInstanceState(Bundle outState);

    void onRestoreState(Bundle savedInstanceState);

    void disconnect();

    View getPublisherView();

    View getSubscriberView();

    boolean isInSession();
}
