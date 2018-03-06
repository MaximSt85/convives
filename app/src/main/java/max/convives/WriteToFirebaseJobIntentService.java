package max.convives;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Max on 26.12.2017.
 */

public class WriteToFirebaseJobIntentService extends JobIntentService {

    private static final String TAG = "mDebugger";

    public static final String IS_MUTE_JOBINTENT_SERVICE = "isMuteJobintentService";
    public static final String FIREBASE_REFERENCE_JOBINTENT_SERVICE = "firebaseReferenceJobintentService";
    private boolean wroteSuccessfully = false;
    private boolean writeAgain = true;
    public static boolean running = false;
    public static boolean stop = false;
    public static boolean restart = false;

    DatabaseHelperAll mDatabaseHelperAll;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseHelperAll = new DatabaseHelperAll(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        stop = false;
        running = false;
        writeAgain = true;

        restart = false;
        do {
            restart = false;
            running = false;
            writeAgain = true;

            final String [] argsMute = {String.valueOf(0)};
            Cursor cursorMute = mDatabaseHelperAll.queryMute(DatabaseHelperAll.KEY_IS_DELIEVERED + "=?", argsMute);
            if (cursorMute.moveToFirst()) {
                final String userId = cursorMute.getString(cursorMute.getColumnIndex(DatabaseHelperAll.KEY_USER_ID));
                final int isMute = cursorMute.getInt(cursorMute.getColumnIndex(DatabaseHelperAll.KEY_IS_MUTED));
                boolean preferenceMute;
                if (isMute == 1) {preferenceMute = true;}
                else {preferenceMute = false;}
                wroteSuccessfully = false;
                writeAgain = true;
                while (!wroteSuccessfully) {
                    if (restart) {
                        break;
                    }
                    if (writeAgain) {
                        writeAgain = false;
                        mDatabase.child("mute").child(userId).setValue(preferenceMute, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    final String [] args = {String.valueOf(userId)};
                                    int rowsupdated = mDatabaseHelperAll.updateMute(userId, isMute,
                                            1, DatabaseHelperAll.KEY_USER_ID + "=?", args);
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

            /*final String [] args = {String.valueOf(0)};
            final String orderBy = DatabaseHelperAll.KEY_TIME;
            Cursor cursor = mDatabaseHelperAll.queryChatMessages(DatabaseHelperAll.KEY_IS_DELIEVERED + "=?", args, orderBy + " DESC");
            if (cursor.moveToFirst()) {
                try {
                    do {
                        if (restart) {
                            break;
                        }
                        running = true;
                        wroteSuccessfully = false;
                        writeAgain = true;
                        final String message = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_MESSAGE));
                        final String senderName = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_SENDER_NAME));
                        final String receiverId = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_RECEIVER_ID));
                        final String senderId = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_SENDER_ID));
                        final long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelperAll.KEY_TIME));
                        ChatMessage chatMessage = new ChatMessage(message, senderName, receiverId, senderId, time);
                        final String referenceParent = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_REFERENCE_PARENT));
                        final String referenceChild = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_REFERENCE_CHILD));
                        while (!wroteSuccessfully) {
                            if (restart) {
                                break;
                            }
                            if (writeAgain) {
                                writeAgain = false;
                                //mDatabase.child(referenceParent).child(referenceChild).push().setValue(chatMessage, new DatabaseReference.CompletionListener() {
                                mDatabase.child("chats").child(referenceParent).child(referenceChild).setValue(chatMessage, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            final String [] args = {String.valueOf(time)};
                                            int rowsupdated = mDatabaseHelperAll.updateChatMessages(message, senderName,
                                                    receiverId, senderId, time, referenceParent, referenceChild, 1,
                                                    DatabaseHelperAll.KEY_TIME + "=?", args);
                                            wroteSuccessfully = true;
                                        }
                                        else {
                                            writeAgain = true;
                                        }
                                    }
                                });
                            }
                            //SystemClock.sleep(3000);
                        }
                    }
                    while (cursor.moveToNext());
                    running = false;
                }
                finally {
                    cursor.close();
                }
            }*/
        }
        while (restart);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }
}



/*if (intent.hasExtra(IS_MUTE_JOBINTENT_SERVICE) && intent.hasExtra(FIREBASE_REFERENCE_JOBINTENT_SERVICE)) {
            boolean isMute = intent.getBooleanExtra(IS_MUTE_JOBINTENT_SERVICE, false);
            String reference = intent.getStringExtra(FIREBASE_REFERENCE_JOBINTENT_SERVICE);
            wroteSuccessfully = false;
            writeAgain = true;
            while (!wroteSuccessfully) {
                if (stop) {
                    break;
                }
                if (writeAgain) {
                    writeAgain = false;
                    mDatabase.child(reference).setValue(isMute, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                wroteSuccessfully = true;
                            }
                            else {
                                writeAgain = true;
                            }
                        }
                    });
                }
                SystemClock.sleep(3000);
            }
        }*/




/*private boolean allTrue (List<Boolean> values) {
        for (boolean value : values) {
            if (!value)
                return false;
        }
        return true;
    }*/
