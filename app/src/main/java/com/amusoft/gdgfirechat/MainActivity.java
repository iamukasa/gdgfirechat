package com.amusoft.gdgfirechat;

import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.Random;
public class MainActivity extends AppCompatActivity{

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Setup our Firebase mFirebaseRef
    DatabaseReference mFirebaseRef =database.getReference("chat");

    private String mUsername;

    private ValueEventListener mConnectedListener;
    private FirebaseListAdapter mChatListAdapter;

    ListView listView ;
    EditText inputText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make sure we have a mUsername
        setupUsername();
        setToolBar();
        getOverflowMenu();
//
//        setTitle("Chatting as " + mUsername);


        // Setup our input methods. Enter key on the keyboard or pushing the send button
        inputText = (EditText) findViewById(R.id.chat_editText);
        inputText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    final String question =   inputText.getText().toString();
                    sendMessage();
                    inputText.setText("");


                    return true;
                }

                return false;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes

        // Tell our list adapter that we only want 50 messages at a time
        mChatListAdapter = new FirebaseListAdapter(mFirebaseRef, this, mUsername) {
            @Override
            public void cleanup() {
                super.cleanup();
            }
        };
       listView=(ListView)findViewById(R.id.chat_listView);
        listView.setAdapter(mChatListAdapter);
        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });
        // Finally, a little indication of connection status
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(getApplicationContext(), "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mChatListAdapter.cleanup();
    }

//    private void setupUsername() {
//        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
//        mUsername = prefs.getString("username", null);
//
//    }
private void setupUsername() {
    SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
    mUsername = prefs.getString("username", null);
    if (mUsername == null) {
        Random r = new Random();
        // Assign a random user name if we don't have one saved.
        mUsername = "Client" + r.nextInt(100000);
        prefs.edit().putString("username", mUsername).commit();
    }
}
    private void sendMessage() {
        EditText inputText = (EditText) findViewById(R.id.chat_editText);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            // Create our 'model', a Chat object
            ChatMessage chat = new ChatMessage(input, mUsername);
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mFirebaseRef.push().setValue(chat);
            inputText.setText("");
        }
    }
    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAdminDetails);
        if (toolbar != null) {
            if(getSupportActionBar()!=null) {
                setSupportActionBar(toolbar);
                setUpActionbar();
                getOverflowMenu();
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                toolbar.setTitleTextColor(getResources().getColor(R.color.white_pure));
            }
        }

    }
    private void setUpActionbar() {
        if(getSupportActionBar()!=null){
            ActionBar bar = getSupportActionBar();
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setHomeButtonEnabled(false);
            bar.setDisplayShowHomeEnabled(false);
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayShowTitleEnabled(true);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }


    }
    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);

            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if(menuKeyField != null) {

                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
                menuKeyField.isSynthetic();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activty, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_log_out) {
            FirebaseAuth.getInstance().signOut();

        }
        return super.onOptionsItemSelected(item);

    }

}
