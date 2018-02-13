package com.example.max.chat_firebase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Max on 20.12.2017.
 */

public class OnlineStatus {

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    SharedPreferences offlinePreferences;
    private SharedPreferences.Editor editor;
    Activity cc;

    public static final String OFFLINE_PREFERENCES = "OfflinePreferences" ;
    public static final String ISUSERONLINE = "isUserOnline";

    OnlineStatus (Activity cc) {
        this.cc = cc;
    }

    private void declareVariable() {
        offlinePreferences = cc.getSharedPreferences(OFFLINE_PREFERENCES, Context.MODE_PRIVATE);
        editor = offlinePreferences.edit();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void makeOffline() {
        /*declareVariable();
        if(currentUser != null && mDatabase != null) {
            editor.putBoolean(ISUSERONLINE, false);
            editor.commit();
            Intent intentService = new Intent(cc, OfflineJobIntentService.class);
            intentService.putExtra(OfflineJobIntentService.USER_ID_JOBINTENT_SERVICE, currentUser.getUid());
            cc.startService(intentService);
        }*/
    }

    public void makeOnline() {
        /*declareVariable();
        if(currentUser != null && mDatabase != null) {
            editor.putBoolean(ISUSERONLINE, true);
            editor.commit();
            mDatabase.child("users").child(currentUser.getUid()).child("isUserOnline").setValue(true);
        }*/
    }
}
