package com.example.chatty.ui;

import android.content.Context;
import android.view.View;

/**
 * Created by 1 on 06.07.2017.
 */

public interface ViewContract {

    void updateSubscriberSource(View view);

    void setPublisherSource(View view);

    void dropView();

    Context getContext();

    void permissionsGranted();
}
