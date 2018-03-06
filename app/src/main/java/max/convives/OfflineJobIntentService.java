package max.convives;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;

/**
 * Created by Max on 11.12.2017.
 */

public class OfflineJobIntentService extends JobIntentService {

    private DatabaseReference mDatabase;

    public static final String USER_ID_JOBINTENT_SERVICE = "userId";

    SharedPreferences offlinePreferences;

    boolean wroteSuccessfully = false;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String userId = intent.getStringExtra(USER_ID_JOBINTENT_SERVICE);

        SystemClock.sleep(1000);

        offlinePreferences = getSharedPreferences(OnlineStatus.OFFLINE_PREFERENCES, Context.MODE_PRIVATE);
        boolean isOnline = offlinePreferences.getBoolean(OnlineStatus.ISUSERONLINE, false);
        if (!isOnline) {
            //mDatabase.child("users").child(userId).child("isUserOnline").setValue(false);
            while (!wroteSuccessfully) {
                mDatabase.child("users").child(userId).child("isUserOnline").setValue(false, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            wroteSuccessfully = true;
                        }
                    }
                });
                SystemClock.sleep(3000);
            }
        }
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");
        }
        catch (Exception e) {return false;}
    }
}
