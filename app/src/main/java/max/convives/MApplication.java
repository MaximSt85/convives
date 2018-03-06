package max.convives;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Max on 28.12.2017.
 */

public class MApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        /*DatabaseReference test = mDatabase.child("online");
        test.setValue(true);
        test.onDisconnect().setValue("false");*/

        /*if (currentUser != null) {

        }*/
    }

    public static void setOnlineListener(FirebaseUser currentUser) {
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference isUserOnlineRef = mDatabaseReference.child("users").child(currentUser.getUid()).child("isUserOnline");
        final DatabaseReference lastOnlineRef = mDatabaseReference.child("users").child(currentUser.getUid()).child("lastUserOnline");
        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    isUserOnlineRef.setValue(true);
                    isUserOnlineRef.onDisconnect().setValue(false);
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled at .info/connected");
            }
        });
    }

    public static void makeToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}


/*public class Utils {
private static FirebaseDatabase mDatabase;

public static FirebaseDatabase getDatabase() {
   if (mDatabase == null) {
      mDatabase = FirebaseDatabase.getInstance();
      mDatabase.setPersistenceEnabled(true);
   }
return mDatabase;
}

}*/
