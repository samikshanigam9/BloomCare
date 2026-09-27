package com.samiksha.bloomcare;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

public class BloomCareApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                );
    }
}
