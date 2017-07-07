package com.example.chatty;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.chatty.presenter.Presenter;
import com.example.chatty.presenter.PresenterImpl;
import com.example.chatty.service.SessionService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by 1 on 06.07.2017.
 */
@Module
public class AppModule {
    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }


    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    SessionService provideSessionService() {
        return new SessionService(app);
    }


    @Provides
    @Singleton
    Presenter providePresenterImpl() {
        return new PresenterImpl();
    }

//    @Provides
//    @Singleton
//    SessionCommunicator provideSessionCommunicator(PresenterImpl presenter) {
//        return presenter;
//    }
//
//    @Provides
//    @Singleton
//    Presenter providePresenter(PresenterImpl presenter) {
//        return presenter;
//    }
}

