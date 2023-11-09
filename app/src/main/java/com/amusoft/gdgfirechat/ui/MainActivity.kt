package com.amusoft.gdgfirechat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.amusoft.gdgfirechat.R
import com.amusoft.gdgfirechat.adapter.FirebaseListAdapter
import com.amusoft.gdgfirechat.model.ChatMessage
import com.amusoft.gdgfirechat.showSnackBar
import com.amusoft.gdgfirechat.stripUsername
import com.amusoft.gdgfirechat.toUrlefy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Setup our Firebase firebaseRef
    private val firebaseRef: DatabaseReference = database.getReference("chat")

    private val storageReference: StorageReference = storage.reference


    private var username: String? = null

    private var connectedListener: ValueEventListener? = null
    private var chatListAdapter: FirebaseListAdapter? = null

    private val manager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUsername()
        setToolBar()
        getOverflowMenu()

        chat_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Perform action on key press
                val question = chat_editText.text.toString()
                if(question.contains("@SophieBot")){
                    getAnswer("SophieBot", stripUsername(question))
                }

                sendMessage(setupUsername(), question)

                chat_editText.setText("")
                return@OnKeyListener true
            }

            false
        })

        sendphoto.setOnClickListener { sendPhoto() }

    }

    private fun sendPhoto() {
        // Pick an image from storage
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RC_TAKE_PICTURE)
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val mFileUri = data.data
                print(mFileUri)
                uploadFromUri(mFileUri!!)
            } else {
                Log.d("Image selection", "Image not retrieved")
            }
        } else {
            Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadFromUri(fileUri: Uri) {

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        val photoRef = storageReference.child("photos")
                .child(fileUri.lastPathSegment!!)
        // [END get_child_ref]

        // Upload file to Firebase Storage
        photoRef.putFile(fileUri).addOnSuccessListener { taskSnapshot ->
            // [START_EXCLUDE
            val imageUri = taskSnapshot.metadata?.contentDisposition
            sendMessage(setupUsername(), imageUri!!)
            dismissProgressNotification()

            // [END_EXCLUDE]
        }.addOnProgressListener { taskSnapshot ->
            showProgressNotification(getString(R.string.progress_uploading),
                    taskSnapshot.bytesTransferred,
                    taskSnapshot.totalByteCount)
        }


    }

    public override fun onStart() {
        super.onStart()

        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes

        // Tell our list adapter that we only want 50 messages at a time
        chatListAdapter = object : FirebaseListAdapter(firebaseRef, this, username!!) {
            override fun cleanup() {
                super.cleanup()
            }
        }

        chat_listView.adapter = chatListAdapter

        chatListAdapter!!.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                chat_listView.setSelection(chatListAdapter!!.count - 1)
            }
        })
        // Finally, a little indication of connection status
        connectedListener = firebaseRef.root.child(".info/connected")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val connected = (dataSnapshot.value as Boolean)

                        if (connected) {
                            Toast.makeText(applicationContext, "Connected to Firebase", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "Disconnected from Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }

                })
    }

    public override fun onStop() {
        super.onStop()
        firebaseRef.root.child(".info/connected").removeEventListener(connectedListener!!)
        chatListAdapter!!.cleanup()
    }

    private fun setupUsername(): String {

        val prefs = application.getSharedPreferences("ChatPrefs", 0)

        username = prefs.getString("username", null)

        return username ?: ""

    }

    private fun sendMessage(userName: String, question: String) {

        if (question != "") {
            // Create our 'model', a Chat object
            val chat = ChatMessage(question, userName)
            // Create a new, auto-generated child of that chat location, and save our chat data there
            firebaseRef.push().setValue(chat)
            chat_editText.text.clear()
        }
    }
    private fun getAnswer(username: String, question: String) {
        var apilink ="https://publicapi.sophiebot.ai/answer/"
        var apireuest=apilink+ toUrlefy(question)
        //TODO send request to api and add it to firebase

        // Instantiate the cache
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())
        // Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start() }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apireuest, null,
            { response ->
                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                jsonObj.get("answer")?.let {
                    val chat = ChatMessage(username,it.toString())
                    // Create a new, auto-generated child of that chat location, and save our chat data there
                    sendMessage(chat.author,chat.message)
                }
            },
            { error ->
                // TODO: Handle error
                showSnackBar(chat_listView,"Disconnected from the internet, this version of SophieBot cannot work offline")
            }
        )
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            500000000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

// Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)


    }




    private fun setToolBar() {

        setSupportActionBar(toolbarAdminDetails)
        supportActionBar?.run {
            title = resources.getString(R.string.app_name)
            setHomeButtonEnabled(false)
            setDisplayShowHomeEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
        }

        getOverflowMenu()

        toolbarAdminDetails.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))

        toolbarAdminDetails.setTitleTextColor(ResourcesCompat.getColor(resources, R.color.white_pure, null))
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun getOverflowMenu() {

        try {
            val config = ViewConfiguration.get(this)

            val menuKeyField = ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")

            menuKeyField.run {
                isAccessible = true
                setBoolean(config, false)
                isSynthetic
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activty, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_log_out) {

            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this, LogInActivity::class.java))
            finish()

            return true
        }

        return super.onOptionsItemSelected(item)

    }


    /**
     * Show notification with a progress bar.
     */
    protected fun showProgressNotification(caption: String, completedUnits: Long, totalUnits: Long) {

        var percentComplete = 0

        if (totalUnits > 0) {
            percentComplete = (100 * completedUnits / totalUnits).toInt()
        }

        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(caption)
                .setProgress(100, percentComplete, false)
                .setOngoing(true)
                .setAutoCancel(false) as NotificationCompat.Builder



        manager.notify(PROGRESS_NOTIFICATION_ID, builder.build())
    }

    /**
     * Dismiss the progress notification.
     */
    private fun dismissProgressNotification() {
        manager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    companion object {
        private val RC_TAKE_PICTURE = 101
        private val PROGRESS_NOTIFICATION_ID = 2
    }


}
