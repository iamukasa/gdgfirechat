package com.amusoft.gdgfirechat.adapter

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.amusoft.gdgfirechat.R
import com.amusoft.gdgfirechat.inflate
import com.amusoft.gdgfirechat.model.ChatMessage
import com.amusoft.gdgfirechat.ui.MainActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import java.util.*


abstract class FirebaseListAdapter(private val ref: DatabaseReference, private val activity: Context, private val username: String) :
        ArrayAdapter<ChatMessage>(activity, android.R.layout.simple_list_item_1) {


    private val models: MutableList<ChatMessage> = ArrayList()
    private val keys: MutableList<String> = ArrayList()
    private val listener: ChildEventListener

    init {

        // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
        listener = ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                val newPost = dataSnapshot.value as Map<String, Any>?

                newPost?.let {

                    val model = ChatMessage(it["message"].toString(), it["author"].toString())

                    val key = dataSnapshot.key

                    // Insert into the correct location, based on previousChildName

                    if (previousChildName == null) {

                        models.add(0, model)

                        key?.let { it1 -> keys.add(0, it1) }

                    } else {

                        val previousIndex = keys.indexOf(previousChildName)

                        val nextIndex = previousIndex + 1

                        if (nextIndex == models.size) {
                            models.add(model)
                            key?.let { it1 -> keys.add(it1) }

                        } else {
                            models.add(nextIndex, model)
                            key?.let { it1 -> keys.add(nextIndex, it1) }
                            showNotification(model)
                        }
                    }
                }

                notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                // One of the models changed. Replace it in our list and name mapping

                val key = dataSnapshot.key

                val newPost = dataSnapshot.value as Map<String, Any>?

                newPost?.let {

                    val newModel = ChatMessage(it["message"].toString(), it["author"].toString())

                    val index = keys.indexOf(key)

                    models[index] = newModel
                }

                notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                // A model was removed from the list. Remove it from our list and the name mapping

                val key = dataSnapshot.key

                val index = keys.indexOf(key)

                keys.removeAt(index)

                models.removeAt(index)

                notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

                // A model changed position in the list. Update our list accordingly

                val key = dataSnapshot.key

                val newPost = dataSnapshot.value as Map<String, Any>?

                newPost?.let {

                    val newModel = ChatMessage(it["message"].toString(), it["author"].toString())

                    val index = keys.indexOf(key)

                    models.removeAt(index)

                    keys.removeAt(index)

                    if (previousChildName == null) {
                        models.add(0, newModel)
                        key?.let { it1 -> keys.add(0, it1) }

                    } else {

                        val previousIndex = keys.indexOf(previousChildName)

                        val nextIndex = previousIndex + 1

                        if (nextIndex == models.size) {
                            models.add(newModel)
                            key?.let { it1 -> keys.add(it1) }
                        } else {
                            models.add(nextIndex, newModel)
                            key?.let { it1 -> keys.add(nextIndex, it1) }
                        }
                    }

                }

                notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur")
            }

        })
    }

    private fun showNotification(model: ChatMessage) {

        val mNotificationManager: NotificationManager

        val notificationID = 100

        var numMessages = 0

        /* Invoking the default notification service */
        val mBuilder = NotificationCompat.Builder(activity)

        mBuilder.setContentTitle("New Message")

        mBuilder.setContentText(model.author)

        mBuilder.setSmallIcon(R.mipmap.ic_launcher)


        /* Increase notification number every time a new notification arrives */

        mBuilder.setNumber(++numMessages)

        /* Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(activity, MainActivity::class.java)


        val stackBuilder = TaskStackBuilder.create(activity)

        stackBuilder.addParentStack(MainActivity::class.java)

        /* Adds the Intent that starts the Activity to the top of the stack */

        stackBuilder.addNextIntent(resultIntent)

        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_CANCEL_CURRENT
        )

        mBuilder.setContentIntent(resultPendingIntent)

        mNotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /* notificationID allows you to update the notification later on. */

        mNotificationManager.notify(notificationID, mBuilder.build())
    }


    open fun cleanup() {
        // We're being destroyed, let go of our listener and forget about all of the models
        ref.removeEventListener(listener)
        models.clear()
        keys.clear()
    }

    override fun getCount(): Int {
        return models.size
    }

    override fun getItem(i: Int): ChatMessage? {
        return models[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView

        val comment = getItem(position)

        val type = getItemViewType(position)

        // Map a Chat object to an entry in our listView

        val author = comment!!.author

        val message = comment.message


        // If the message was sent by this user, color it differently

        if (author.contains(username)) {

            if (message.contains("firebasestorage.googleapis.com")) {

                view = parent.inflate(R.layout.chat_list_image_right, false)

                val imageView = view.findViewById<ImageView>(R.id.textimage)

                Picasso.get().load(message).into(imageView)

            } else {

                view = parent.inflate(R.layout.chat_listitem_right, false)

                (view.findViewById<TextView>(R.id.text)).text = activity.getString(R.string.comment, author, comment.message)

            }

        } else {

            if (message.contains("firebasestorage.googleapis.com")) {

                view = parent.inflate(R.layout.chat_list_image_left, false)

                val image = view.findViewById<ImageView>(R.id.textimage)

                Picasso.get().load(message).into(image)

            } else {

                view = parent.inflate(R.layout.chat_listitem_left, false)

                (view.findViewById<TextView>(R.id.text)).text = activity.getString(R.string.comment, author, comment.message)

            }

        }

        return view

    }
}