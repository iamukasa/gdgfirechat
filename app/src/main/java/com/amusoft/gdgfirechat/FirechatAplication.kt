package com.amusoft.gdgfirechat

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Created by Tobenna on 13/08/19
 */
class FirechatAplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)

            FirebaseMessaging.getInstance().subscribeToTopic("android")
        }


    }
}
