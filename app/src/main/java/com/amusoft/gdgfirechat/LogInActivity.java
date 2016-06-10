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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    EditText editusername,editemail,editpassword;
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
        editusername=(EditText)findViewById(R.id.editUsername);
        editemail=(EditText)findViewById(R.id.editemail);
        editpassword=(EditText)findViewById(R.id.editPass);
        blogin=(Button) findViewById(R.id.butonLogIn);
        btnsignUp=(Button)findViewById(R.id.butonSignUp);

        mAuth = FirebaseAuth.getInstance();
        authenticationSetup();




        blogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFifebaseSign(editemail.getText().toString(),editpassword.getText().toString());

            }
        });
        btnsignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUserAdd(editusername.getText().toString(),editemail.getText().toString(),editpassword.getText().toString());

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
                    Snackbar snackbar3 = Snackbar
                            .make(coordinatorLayout, "Signed up successfully", Snackbar.LENGTH_LONG);
                    snackbar3.show();



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

                        Snackbar snackbar1 = Snackbar
                                .make(coordinatorLayout, "Log in Sucessful", Snackbar.LENGTH_LONG);
                        snackbar1.show();
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
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(i);
                    finish();


                } else {
                    // User is signed out
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