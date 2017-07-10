package com.example.chatty.ui;

import android.content.Context;
import android.view.View;

/**
 * Interface provided by the activity, so Presenter can communicate with it
 */

public interface ViewContract {

    void setSubscriberSource(View view);

    void setPublisherSource(View view);

    void dropSubscriberView();

    void setSubscriberErrorView();


    Context getContext();

}
