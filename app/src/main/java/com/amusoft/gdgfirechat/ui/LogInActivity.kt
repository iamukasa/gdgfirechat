package com.amusoft.gdgfirechat.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amusoft.gdgfirechat.*
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
            signInUser(editUsername.getTextExt(),
                    editemail.getTextExt(), editPass.getTextExt())
        }

        butonSignUp.setOnClickListener {
            signUpUser(editUsername.getTextExt(),
                    editemail.getTextExt(), editPass.getTextExt())
        }
    }

    private fun signUpUser(username: String, email: String, password: String) {

        //TODO: validate email & password text
        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

            auth?.createUserWithEmailAndPassword(email, password)

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

        } else {
            showSnackBar(loglogloglog, getString(R.string.empty_error))
        }

    }


    private fun signInUser(username: String, email: String, password: String) {

        //TODO: validate email & password text
        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

            auth?.signInWithEmailAndPassword(email, password)

                    ?.addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {
                            showSnackBar(loglogloglog, getString(R.string.login_success))

                            addToPrefs(CHAT_PREFERENCES, CHAT_USERNAME, username)
                        } else {

                            showSnackBar(loglogloglog, getString(R.string.auth_fail))

                        }

                    }
        } else {
            showSnackBar(loglogloglog, getString(R.string.empty_error))
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