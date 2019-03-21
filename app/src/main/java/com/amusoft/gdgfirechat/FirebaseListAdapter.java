package com.amusoft.gdgfirechat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.firebase.client.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class FirebaseListAdapter extends ArrayAdapter<ChatMessage> {

    Activity activity;




    // Setup our Firebase mFirebaseRef
    DatabaseReference mRef;


    private ChatMessage model;
    private String mUsername;
    private LayoutInflater mInflater;
    private List<ChatMessage> mModels;
    private List<String> mKeys;
    private ChildEventListener mListener;
    private ViewGroup viewGroup;
    private LinearLayout wrapper;

    public FirebaseListAdapter(DatabaseReference mRef, Activity activity, String mUsername) {
        super(activity, android.R.layout.simple_list_item_1);
        this.activity = activity;
        this.mRef = mRef;
        this.mUsername = mUsername;

        mInflater = activity.getLayoutInflater();
        mModels = new ArrayList<ChatMessage>();
        mKeys = new ArrayList<String>();


        // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                Map<String, Object> newPost =
                        (Map<String, Object>) dataSnapshot.getValue();
                if (newPost.get("message").toString() !=null && newPost.get("author").toString()!=null){


                model = new ChatMessage(newPost.get("message").toString(), newPost.get("author").toString());
                String key = dataSnapshot.getKey();

                // Insert into the correct location, based on previousChildName
                if (previousChildName == null) {
                    mModels.add(0, model);
                    mKeys.add(0, key);

                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(model);
                        mKeys.add(key);

                    } else {
                        mModels.add(nextIndex, model);
                        mKeys.add(nextIndex, key);
                        showNotification(model);
                    }
                }

                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String key = dataSnapshot.getKey();
                Map<String, Object> newPost =
                        (Map<String, Object>) dataSnapshot.getValue();

                ChatMessage newModel = new ChatMessage(newPost.get("message").toString(), newPost.get("author").toString());
                int index = mKeys.indexOf(key);

                mModels.set(index, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // A model was removed from the list. Remove it from our list and the name mapping
                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);

                mKeys.remove(index);
                mModels.remove(index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                // A model changed position in the list. Update our list accordingly
                String key = dataSnapshot.getKey();
                Map<String, Object> newPost =
                        (Map<String, Object>) dataSnapshot.getValue();

                ChatMessage newModel = new ChatMessage(newPost.get("message").toString(), newPost.get("author").toString());
                int index = mKeys.indexOf(key);
                mModels.remove(index);
                mKeys.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                    mKeys.add(0, key);
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                        mKeys.add(key);
                    } else {
                        mModels.add(nextIndex, newModel);
                        mKeys.add(nextIndex, key);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");

            }


        });
    }

    private void showNotification(ChatMessage model) {
        NotificationManager mNotificationManager;
        int notificationID = 100;
        int numMessages = 0;

                    /* Invoking the default notification service */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.activity.getApplicationContext());

        mBuilder.setContentTitle("New Message");
        mBuilder.setContentText(model.getAuthor());

        mBuilder.setSmallIcon(R.mipmap.ic_launcher);


      /* Increase notification number every time a new notification arrives */
        mBuilder.setNumber(++numMessages);

      /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this.activity.getApplicationContext(), MainActivity.class);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.activity.getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);

      /* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager =
      (NotificationManager) this.activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

      /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(notificationID, mBuilder.build());
    }


    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mRef.removeEventListener(mListener);
        mModels.clear();
        mKeys.clear();
    }

    @Override
    public int getCount() {
        return mModels.size();
    }

    @Override
    public ChatMessage getItem(int i) {
        return mModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChatMessage comment = getItem(position);

        int type = getItemViewType(position);

        // Map a Chat object to an entry in our listview
        String author = comment.getAuthor();
        String message = comment.getMessage();


        // If the message was sent by this user, color it differently
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(this.activity.getApplicationContext().LAYOUT_INFLATER_SERVICE);

        if (author != null  && author.contains(mUsername)) {
            if ( message.contains("firebasestorage.googleapis.com")) {

                row = inflater.inflate(R.layout.chat_list_image_right, parent, false);
                ImageView image  = (ImageView) row.findViewById(R.id.textimage);
                Picasso.with(activity.getApplicationContext()).load(message).into(image);
            }
            else{
                row = inflater.inflate(R.layout.chat_listitem_right, parent, false);
                ((TextView) row.findViewById(R.id.text)).setText(mUsername + "\n" + comment.getMessage());

            }



        } else {

            if ( message.contains("firebasestorage.googleapis.com")) {

                row = inflater.inflate(R.layout.chat_list_image_left, parent, false);
                ImageView image  = (ImageView) row.findViewById(R.id.textimage);
                Picasso.with(activity.getApplicationContext()).load(message).into(image);

            }else {
                row = inflater.inflate(R.layout.chat_listitem_left, parent, false);
                ((TextView) row.findViewById(R.id.text)).setText(author+"\n"+comment.getMessage());

            }





        }
        wrapper = (LinearLayout) row.findViewById(R.id.wrapper);


        return row;

    }
}