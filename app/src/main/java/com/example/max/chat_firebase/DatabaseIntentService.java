package com.example.max.chat_firebase;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Max on 10.12.2017.
 */

public class DatabaseIntentService extends IntentService {

    private static final String TAG = "mDebugger";

    public static final String USER_ID_INTENT_SERVICE = "userId";
    public static final String RESULT_SERVICE_KEY = "Status_code";
    public static final String RECEIVER_KEY = "receiver";
    public static int SUCCESSFUL_CODE = 35;

    DatabaseHelperAll mDatabaseHelperAll;
    String userId;
    //private int status_code;
    ResultReceiver receiver;

    private boolean wroteSuccessfully = false;
    private boolean writeAgain = true;

    public DatabaseIntentService() {
        super("DatabaseIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        receiver = intent.getParcelableExtra(RECEIVER_KEY);
        userId = intent.getStringExtra(USER_ID_INTENT_SERVICE);
        mDatabaseHelperAll = new DatabaseHelperAll(this);
        String [] args = {userId, String.valueOf(0)};
        Cursor cursor = mDatabaseHelperAll.queryChatReference(DatabaseHelperAll.KEY_USER_ID + "=?"
                + " AND " + DatabaseHelperAll.KEY_IS_DELIEVERED + "=?", args);
        if (cursor.moveToFirst()) {
            try {
                do {
                    wroteSuccessfully = false;
                    writeAgain = true;
                    while (!wroteSuccessfully) {
                        if (writeAgain) {
                            writeAgain = false;
                            final String reference = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_CHAT_REFERENCE));
                            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("chats").child(reference).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String [] argsReference = {reference};
                                        mDatabaseHelperAll.updateChatReference(1, DatabaseHelperAll.KEY_CHAT_REFERENCE + "=?", argsReference);
                                        mDatabaseHelperAll.deleteChatMessages(DatabaseHelperAll.KEY_REFERENCE_PARENT + "=?", argsReference);
                                        wroteSuccessfully = true;
                                    }
                                    else {
                                        writeAgain = true;
                                    }
                                }
                            });
                        }
                    }

                }
                while (cursor.moveToNext());
            }
            finally {
                cursor.close();
                Bundle bundle = new Bundle();
                bundle.putInt(RESULT_SERVICE_KEY, SUCCESSFUL_CODE);
                receiver.send(1, bundle);
            }
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putInt(RESULT_SERVICE_KEY, SUCCESSFUL_CODE);
            receiver.send(1, bundle);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
