package com.example.chatty.presenter;

import com.example.chatty.ui.ViewContract;

/**
 * Created by 1 on 06.07.2017.
 */

public interface Presenter {
    void inject(ViewContract viewContract);
    void fetchData();

    void requestPermissions();

    void setViewContract(ViewContract mainActivity);
}
