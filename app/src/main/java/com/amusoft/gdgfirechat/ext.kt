package com.amusoft.gdgfirechat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

fun EditText.getTextExt(): String = this.text.toString()

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun AppCompatActivity.hideKeyBoard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}