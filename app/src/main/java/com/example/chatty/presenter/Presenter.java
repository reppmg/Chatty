package com.example.chatty.presenter;

import com.example.chatty.ui.ViewContract;

/**
 * Interface for communication between Presenter and Activity
 */
public interface Presenter  {

    void subscribe();

    void setViewContract(ViewContract mainActivity);

    void unsubscribe();

}
