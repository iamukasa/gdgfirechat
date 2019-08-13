package com.amusoft.gdgfirechat.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amusoft.gdgfirechat.R
import com.amusoft.gdgfirechat.addToPrefs
import com.amusoft.gdgfirechat.showSnackBar
import com.amusoft.gdgfirechat.startActivityExt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_log_in.*
import java.util.*
import kotlin.properties.Delegates

class LogInActivity : AppCompatActivity() {

    private val database by lazy { FirebaseDatabase.getInstance() }

    private val ref by lazy { database.getReference(DB_PATH) }

    private val auth: FirebaseAuth? by lazy { FirebaseAuth.getInstance() }

    private var authListener: FirebaseAuth.AuthStateListener by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_log_in)

        authenticationSetup()

        butonLogIn.setOnClickListener {
            signInUser(editUsername.text.toString(),
                    editemail.text.toString(), editPass.text.toString())
        }

        butonSignUp.setOnClickListener {
            doUserAdd(editUsername.text.toString(),
                    editemail.text.toString(), editPass.text.toString())
        }
    }

    private fun doUserAdd(username: String, email: String, pass: String) {

        auth?.createUserWithEmailAndPassword(email, pass)

                ?.addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {
                        addToPrefs(CHAT_PREFERENCES, CHAT_USERNAME, username)

                        //Saving userdata to firebase
                        val result = HashMap<String, Any>()
                        result["Username"] = username
                        result["Email"] = email

                        ref.push().setValue(result)

                        showSnackBar(loglogloglog, getString(R.string.auth_success))
                    } else {
                        showSnackBar(loglogloglog, getString(R.string.auth_fail))
                    }

                }


    }


    private fun signInUser(username: String, email: String, pass: String) {

        auth?.signInWithEmailAndPassword(email, pass)

                ?.addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {
                        showSnackBar(loglogloglog, getString(R.string.login_success))

                        addToPrefs(CHAT_PREFERENCES, CHAT_USERNAME, username)
                    } else {

                        showSnackBar(loglogloglog, getString(R.string.auth_fail))

                    }

                }
    }


    private fun authenticationSetup() {

        authListener = FirebaseAuth.AuthStateListener {
            it.currentUser?.let {
                startActivityExt<MainActivity>()
                finish()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        auth?.addAuthStateListener(authListener)
    }

    public override fun onStop() {
        super.onStop()
        auth?.removeAuthStateListener(authListener)
    }

}

const val CHAT_PREFERENCES: String = "ChatPrefs"
const val CHAT_USERNAME: String = "username"
const val DB_PATH: String = "Users"