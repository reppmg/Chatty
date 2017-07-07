package com.example.chatty;

import com.example.chatty.presenter.PresenterImpl;
import com.example.chatty.service.SessionService;
import com.example.chatty.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by 1 on 06.07.2017.
 */

@Singleton
@Component(
        modules = {
                AppModule.class
        }
)
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(PresenterImpl presenter);

    void inject(SessionService sessionService);
}
