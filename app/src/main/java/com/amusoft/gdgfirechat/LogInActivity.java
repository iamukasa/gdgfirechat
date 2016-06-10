package com.amusoft.gdgfirechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    EditText username,email,password;
    Button blogin ,btnsignUp;
    LinearLayout coordinatorLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        coordinatorLayout = (LinearLayout) findViewById(R.id
                .loglogloglog);
        mAuth = FirebaseAuth.getInstance();
        authenticationSetup();


        username=(EditText)findViewById(R.id.editUsername);
        email=(EditText)findViewById(R.id.editemail);
        password=(EditText)findViewById(R.id.editPass);
        blogin=(Button) findViewById(R.id.butonLogIn);
        btnsignUp=(Button)findViewById(R.id.butonSignUp);

        blogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFifebaseSign(email.getText().toString(),password.getText().toString());

            }
        });
        btnsignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUserAdd(username.getText().toString(),email.getText().toString(),password.getText().toString());

            }
        });
    }

private void doUserAdd(final String username, final String email, String pass) {
    mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Authentication failed", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    //adding username to sharedprefs
                    SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
                 prefs.edit().putString("username", username).commit();

                    //Saving userdata to firebase
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("Username", username);
                    result.put("Email", email);
                   myRef.push().setValue(result);



                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Snackbar snackbar2 = Snackbar
                                .make(coordinatorLayout, "Authentication failed", Snackbar.LENGTH_LONG);
                        snackbar2.show();

                    }

                    // ...
                }
            });


}







    private void doFifebaseSign(String s, String s1) {
        mAuth.signInWithEmailAndPassword(s, s1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "Authentication failed", Snackbar.LENGTH_LONG);
                            snackbar.show();



                        }

                        // ...
                    }
                });
    }



    private void authenticationSetup() {

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "You signed in", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(i);
                    finish();

                } else {
                    Intent i = new Intent(getApplicationContext(),LogInActivity.class);
                    startActivity(i);
                    finish();

                    // User is signed out
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "You Signed Out", Snackbar.LENGTH_LONG);
                    snackbar.show();

                }
                // ...
            }
        };
        // ...
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
