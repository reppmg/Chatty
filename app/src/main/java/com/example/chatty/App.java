package com.example.chatty;

import android.app.Application;
import android.util.Log;


public class App extends Application {

    private static AppComponent appComponent;
    private static final String TAG = App.class.getSimpleName();

    public static AppComponent getAppComponent(){
        return appComponent;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate: ");
    }
}
