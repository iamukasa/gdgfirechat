package com.amusoft.gdgfirechat;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by dice on 6/10/16.
 */
public class FirechatAplication extends Application {
    FirebaseDatabase myFirebaseRef;

    @Override
    public void onCreate(){
        super.onCreate();

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            FirebaseMessaging.getInstance().subscribeToTopic("android");
        }



    }
}
