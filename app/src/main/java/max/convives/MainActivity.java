package max.convives;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.max.chat_firebase.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements DatabaseIntentServiceResultReceiver.Receiver {

    private static final String TAG = "mDebugger";

    boolean FromChildActivity = false;

    private DatabaseHelper mDatabaseHelper;
    private DatabaseHelperAll mDatabaseHelperAll;
    private ProgressDialog progressDialog;

    private static final int SIGN_IN_REQUEST_CODE = 1;

    public static String FIREBASESTORAGE_EXCEPTION = "Object does not exist at location.";

    View proceed;
    TextView userName;
    ImageView userPic;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUserAuth;
    private StorageReference mStorage;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 11;

    private DatabaseIntentServiceResultReceiver mReceiver;

    SharedPreferences firstTimeStartPreferences;
    private SharedPreferences.Editor editor;
    public static final String FIRST_TIME_START_PREFERENCES = "firstTimeStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.show();

        mReceiver = new DatabaseIntentServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        firstTimeStartPreferences = getSharedPreferences(FIRST_TIME_START_PREFERENCES, Context.MODE_PRIVATE);
        editor = firstTimeStartPreferences.edit();

        if ( ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }

        //deleteDatabase(DatabaseHelper.DATABASE_NAME);
        //deleteDatabase(DatabaseHelperAll.DATABASE_NAME);

        if (getIntent().hasExtra("FromChildActivity")) {FromChildActivity = true;}
        else {FromChildActivity = false;}

        if (getIntent().hasExtra("USER_ID1") || getIntent().hasExtra("USER_ID2") ||
                getIntent().hasExtra("USER_NAME2") || getIntent().hasExtra("USER_AGE2") ||
                getIntent().hasExtra("USER_SEX2")) {
            Bundle notification_extras = getIntent().getExtras();
            String notification_userId1 = notification_extras.getString("USER_ID1");;
            String notification_userId2 = notification_extras.getString("USER_ID2");
            String notification_user_name2 = notification_extras.getString("USER_NAME2");
            int notification_user_age2 = Integer.valueOf(notification_extras.getString("USER_AGE2"));
            boolean notification_user_sex2 = Boolean.valueOf(notification_extras.getString("USER_SEX2"));

            Intent intent = new Intent(this, ChatActivity.class);
            Bundle extras = new Bundle();
            extras.putString("USER_ID1", notification_userId1);
            extras.putString("USER_ID2", notification_userId2);
            extras.putString("USER_NAME2", notification_user_name2);
            extras.putInt("USER_AGE2", notification_user_age2);
            extras.putBoolean("USER_SEX2", notification_user_sex2);
            intent.putExtras(extras);
            startActivity(intent);
        }

        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelperAll = new DatabaseHelperAll(this);

        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        if(currentUserAuth == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }
        else {
            MApplication.setOnlineListener(currentUserAuth);
            mDatabase.child("users").child(currentUserAuth.getUid()).child("isUserOnline").setValue(true);
            updateGUI();
        }

        proceed = (View) findViewById(R.id.convive);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) !=
                        PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    makeToast(getResources().getString(R.string.location_permission));
                }
                else {
                    usersActivity();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                MApplication.makeToast(getResources().getString(R.string.signed_in), this);
                currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
                MApplication.setOnlineListener(currentUserAuth);
                mDatabase.child("users").child(currentUserAuth.getUid()).child("userId").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            updateGUI();
                        }
                        else {
                            writeNewUser(currentUserAuth.getDisplayName(), currentUserAuth.getUid());
                            updateGUI();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else {
                MApplication.makeToast(getResources().getString(R.string.could_not_sign_you_in), this);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOnline();
    }

    @Override
    protected void onPause() {
        super.onPause();
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOffline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            mDatabase.child("users").child(currentUserAuth.getUid()).child("isUserOnline").setValue(false);
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            MApplication.makeToast(getResources().getString(R.string.signed_out), MainActivity.this);
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });
        }
        if (item.getItemId() == R.id.menu_settings){
            settingsActivity();
        }
        if (item.getItemId() == R.id.menu_search) {
            serachActivity();
        }
        if (item.getItemId() == R.id.menu_delete_account) {
            deleteUser();
        }
        return true;
    }

    private void deleteUser() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_login,null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(customView);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.show();

        final EditText emailEditText = (EditText) customView.findViewById(R.id.popup_email_edit_text);
        final EditText passwordEditText = (EditText) customView.findViewById(R.id.popup_password_edit_text);

        Button closeButton = (Button) customView.findViewById(R.id.popup_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String email = emailEditText.getText().toString();
                email = email.replaceAll("\\s+","");
                String password = passwordEditText.getText().toString();
                password = password.replaceAll("\\s+","");
                AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                currentUserAuth.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //deleteFromFirebase();
                            deleteAllChats();
                        }
                        else {
                            MApplication.makeToast(getResources().getString(R.string.invalid_email_password), MainActivity.this);
                            progressDialog.dismiss();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
    };

    private void deleteAllChats() {
        Intent intentService;
        intentService = new Intent(this, DatabaseIntentService.class);
        intentService.putExtra(DatabaseIntentService.RECEIVER_KEY, mReceiver);
        intentService.putExtra(DatabaseIntentService.USER_ID_INTENT_SERVICE, currentUserAuth.getUid());
        startService(intentService);
    }

    private void deleteFromFirebase() {
        final String userId = currentUserAuth.getUid();
        mStorage.child("users").child(currentUserAuth.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful() || task.getException().getMessage().equals(FIREBASESTORAGE_EXCEPTION)) {
                    mDatabase.child("mute").child(currentUserAuth.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mDatabase.child("users").child(currentUserAuth.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            currentUserAuth.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        String [] args = {userId};
                                                        mDatabaseHelperAll.deleteChatReference(DatabaseHelperAll.KEY_USER_ID + "=?", args);
                                                        mDatabaseHelperAll.deleteDeletedUsers(DatabaseHelperAll.KEY_USER_ID + "=?", args);
                                                        mDatabaseHelperAll.deleteMute(DatabaseHelperAll.KEY_USER_ID + "=?", args);
                                                        progressDialog.dismiss();
                                                        MApplication.makeToast(getResources().getString(R.string.delete_account_successfully), MainActivity.this);
                                                        Intent intent = getIntent();
                                                        finish();
                                                        startActivity(intent);
                                                    }
                                                    else {
                                                        MApplication.makeToast(getResources().getString(R.string.delete_account_not_successfully_firebase_auth), MainActivity.this);
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            MApplication.makeToast(getResources().getString(R.string.delete_account_not_successfully_firebase_database), MainActivity.this);}
                                    }
                                });
                            }
                            else {
                                MApplication.makeToast(getResources().getString(R.string.delete_account_not_successfully_firebase_database), MainActivity.this);}
                        }
                    });
                }
                else {
                    MApplication.makeToast(getResources().getString(R.string.delete_account_not_successfully_firebase_storage), MainActivity.this);}
            }
        });
    }

    private void writeNewUser(final String name, final String id) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //mDatabaseHelperAll.insertUser(id,"", name, 0, 3,0, 0);
        User user = new User(name, 0, true, id, 0, 0, true, new Date().getTime(), refreshedToken, 0);
        mDatabase.child("users").child(id).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    /*final String [] args = {id};
                    mDatabaseHelperAll.updateUser(id,"", name, 0, 1,
                            1, 1, DatabaseHelperAll.KEY_USER_ID + "=?", args);*/
                }
            }
        });
        updateGUI();
    }

    private void usersActivity() {
        Intent intent = new Intent(this, UsersActivity.class);
        Bundle extras = new Bundle();
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void updateGUI() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        mDatabase.child("users").child(currentUserAuth.getUid()).child("notificationTokens").setValue(refreshedToken);
        mDatabase.child("users").child(currentUserAuth.getUid()).child("userAge").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    writeNewUser(currentUserAuth.getDisplayName(), currentUserAuth.getUid());
                    return;
                }
                int age = Integer.valueOf(String.valueOf(dataSnapshot.getValue()));
                if (age == 0) {
                    settingsActivity();
                    return;
                }
                proceed = (View) findViewById(R.id.convive);
                proceed.setVisibility(View.VISIBLE);
                userName = (TextView) findViewById(R.id.user_name);
                userName.setText(currentUserAuth.getDisplayName());
                userName.setVisibility(View.VISIBLE);
                userPic = (ImageView) findViewById(R.id.main_activity_user_images);
                showPictureFromLocalDatabase(currentUserAuth.getUid());
                progressDialog.dismiss();
                synchronizePhotoBetweenDataBases(currentUserAuth.getUid());
                //String id = "FSLajGL8KMZ2EPY9zSfHAixXMi53";
                //mDatabaseHelperAll.showAllDataInDatabase(id);
                //mDatabaseHelperAll.showAllDataInDatabase(currentUserAuth.getUid());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void settingsActivity() {
        Intent intent = new Intent(this, settingsActivity.class);
        startActivity(intent);
    }

    private void serachActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                }
                break;
            }
        }

    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showPictureFromLocalDatabase (String userId) {

        final String [] args = {userId};
        Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
        byte [] byteArray = null;
        if (cursor.moveToFirst()) {
            byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
            cursor.close();
        }
        if (byteArray != null) {
            Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
            userPic.setImageBitmap(bitmap);
        }
        else {
            userPic.setImageResource(R.drawable.ic_no_person);
        }
    }

    void synchronizePhotoBetweenDataBases (String userId) {

        if (FromChildActivity) {
            String [] args = {userId};
            byte [] byteArray = null;
            Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
            if (cursor.moveToFirst()) {
                byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
                cursor.close();
            }
            Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
            if (bitmap != null) {userPic.setImageBitmap(bitmap);}
            else {userPic.setImageResource(R.drawable.ic_no_person);}
            progressDialog.dismiss();
            return;
        }

        //mReceiver = new DatabaseIntentServiceResultReceiver (new Handler());
        //mReceiver.setReceiver(this);
        SynchronizeDatabases mSynchronizeDatabases = new SynchronizeDatabases(userId, mReceiver, this);
        mSynchronizeDatabases.getMetadataFromUserPhoto();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultCode == 0) {
            int code = resultData.getInt(SynchronizeDatabases.RESULT_SERVICE_KEY);
            if (code == 1 || code == 2) {
                showPictureFromLocalDatabase(currentUserAuth.getUid());
            }
            if (code == -13010) {userPic.setImageResource(R.drawable.ic_no_person);}
        }
        if (resultCode == 1) {
            int code = resultData.getInt(DatabaseIntentService.RESULT_SERVICE_KEY);
            if (code == DatabaseIntentService.SUCCESSFUL_CODE) {
                deleteFromFirebase();
            }
        }
    }
}


/*private void writeToFirebase() {
        String userId = currentUser.getUid();
        final String [] args = {userId};
        Cursor cursor = mDatabaseHelper.queryInTableMute(DatabaseHelper.KEY_USERID + "=?", args);
        if (cursor.moveToFirst()) {
            boolean preferenceMute = false;
            int isMute = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_IS_MUTE));
            if (isMute == 1) {preferenceMute = true;}
            mDatabase.child("mute").child(userId).setValue(preferenceMute, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mDatabaseHelper.deleteInTableMute(DatabaseHelper.KEY_USERID + "=?", args);
                    }
                }
            });
        }
    }*/




/*private void downloadUserPic(final String userId) throws IOException {

        if (FromChildActivity) {
            String [] args = {userId};
            byte [] byteArray = null;
            Cursor cursor = mDatabaseHelper.query(DatabaseHelper.KEY_NAME + "=?", args);
            if (cursor.moveToFirst()) {
                byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
            }
            Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
            if (bitmap != null) {userPic.setImageBitmap(bitmap);}
            else {userPic.setImageResource(R.drawable.no_person);}
            progressDialog.dismiss();
            return;
        }

        showPicturesFromLocalDatabase(userId);
        progressDialog.dismiss();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference users_storage = mStorageRef.child("users");
        final StorageReference user_storage = users_storage.child(userId);

        user_storage.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                final String lastId = storageMetadata.getCustomMetadata("lastPhotoId");
                byte [] byteArray = null;
                final String [] args = {userId};
                Cursor cursor = mDatabaseHelper.query(DatabaseHelper.KEY_NAME + "=?", args);
                if (cursor.moveToFirst()) {
                    String lastIdFromLocalDatabase = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LASTID));
                    if (lastId.equals(lastIdFromLocalDatabase)) {
                        byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
                        Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
                        //Log.d(TAG, "in mainActivity, DataBases synchronized");
                        userPic.setImageBitmap(bitmap);
                        //progressDialog.dismiss();
                    }
                    else {
                        downloadPictureFromFirebaseStorage(user_storage,userId,lastId,true);
                    }
                }
                else {
                    downloadPictureFromFirebaseStorage(user_storage,userId,lastId,false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                if (errorCode == -13010) {
                    makeToast("You don't have Foto");
                    String [] args = {userId};
                    mDatabaseHelper.delete(DatabaseHelper.KEY_NAME + "=?", args);
                    //Log.d(TAG, "deleted from Database");
                }
                else {makeToast("Couldn't download image from Server, failer is " + errorCode);}
                userPic.setImageResource(R.drawable.no_person);
                //progressDialog.dismiss();
            }
        });
    }*/

    /*public void downloadPictureFromFirebaseStorage(StorageReference user_storage, final String userId, final String lastId,
                                                   final boolean insertOrUptadeLocalDatabase) {
        File localFile = null;
        final Bitmap[] picture = new Bitmap[1];
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File finalLocalFile = localFile;
        user_storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                String filePath = finalLocalFile.getPath();
                picture[0] = BitmapFactory.decodeFile(filePath);
                byte [] image = BitmapConverter.bitmapToByteArrayConverter(picture[0]);
                String [] args = {userId};
                if (insertOrUptadeLocalDatabase) {
                    mDatabaseHelper.update(userId, lastId, image, DatabaseHelper.KEY_NAME + "=?", args);
                    //Log.d(TAG, "in mainActivity, updated in DataBase");
                }
                else {
                    mDatabaseHelper.insert(userId, lastId, picture[0]);
                    //Log.d(TAG, "in mainActivity, insert in DataBase");
                }
                userPic.setImageBitmap(picture[0]);
                //progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                picture[0] = null;
                int errorCode = ((StorageException) exception).getErrorCode();
                if (errorCode == -13010) {
                    makeToast("You don't have Foto");
                    String [] args = {userId};
                    mDatabaseHelper.delete(DatabaseHelper.KEY_NAME + "=?", args);
                    //Log.d(TAG, "deleted from Database");
                }
                else {makeToast("Couldn't download image from Server, failer is " + errorCode);}
                userPic.setImageResource(R.drawable.no_person);
                //progressDialog.dismiss();
            }
        });
    }*/