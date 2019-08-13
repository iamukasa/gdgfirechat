package com.amusoft.gdgfirechat

import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar


fun showSnackBar(v: View, msg: String) {
    Snackbar.make(v, msg, Snackbar.LENGTH_SHORT)
            .show()
}

fun AppCompatActivity.addToPrefs(prefName: String, key: String, value: String) {
    val prefs = getSharedPreferences(prefName, 0)
    prefs.edit().putString(key, value).apply()
}

inline fun <reified T : AppCompatActivity> AppCompatActivity.startActivityExt() {
    Intent(this, T::class.java).run {
        startActivity(this)
    }
}

fun EditText.getTextExt() = this.text.toString()