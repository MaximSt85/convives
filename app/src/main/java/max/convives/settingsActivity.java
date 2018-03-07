package max.convives;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.max.chat_firebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class settingsActivity extends AppCompatActivity implements DatabaseIntentServiceResultReceiver.Receiver {
//public class settingsActivity extends AppCompatActivity {

    private static int MAX_LENGTH = 20;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_FILE = 2;
    //private Uri imageUri;

    private static final String TAG = "mDebugger";

    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUserAuth;
    User mUser;

    ImageView user_picture;
    //EditText name;
    EditText age;
    //EditText sex;
    Spinner sex;
    ProgressDialog progressDialog;

    private DatabaseHelper mDatabaseHelper;
    private DatabaseHelperAll mDatabaseHelperAll;

    private DatabaseIntentServiceResultReceiver mReceiver;

    Switch muteSwitch;
    private boolean preferenceMute;

    int user_age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(getString(R.string.settings));
        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelperAll = new DatabaseHelperAll(this);

        progressDialog = new ProgressDialog(settingsActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.show();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();

        showPicture();

        //name = (EditText) findViewById(R.id.user_name_settings);
        age = (EditText) findViewById(R.id.user_age_settings);
        sex = (Spinner) findViewById(R.id.user_sex_settings);
        muteSwitch = (Switch) findViewById(R.id.search_switch_notifications);
        //final EditText sex = (EditText) findViewById(R.id.user_sex_settings);

        mDatabase.child("users").child(currentUserAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
                //name.setText(mUser.getUserName());
                if (mUser.getUserAge() > 0) {
                    age.setText(String.valueOf(mUser.getUserAge()));
                }
                if (mUser.getUserSex()) {
                    sex.setSelection(1);
                }
                else {
                    sex.setSelection(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = currentUser.getUid();
        final String [] args = {userId};
        final int isMute;
        Cursor cursor = mDatabaseHelperAll.queryMute(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor.moveToFirst()) {
            isMute = cursor.getInt(cursor.getColumnIndex(DatabaseHelperAll.KEY_IS_MUTED));
        }
        else {
            isMute = 0;
            mDatabaseHelperAll.insertMute(userId, isMute, 0);
        }
        if (isMute == 1) {
            preferenceMute = true;
        }
        else {preferenceMute = false;}

        muteSwitch.setChecked(preferenceMute);
        mDatabase.child("mute").child(userId).keepSynced(true);
        final Intent intentService = new Intent(settingsActivity.this, WriteToFirebaseJobIntentService.class);
        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                muteSwitch.setChecked(isChecked);
                preferenceMute = isChecked;
                int isMute = 0;
                if (preferenceMute) {
                    isMute = 1;
                }
                mDatabaseHelperAll.updateMute(userId, isMute, 0, DatabaseHelperAll.KEY_USER_ID + "=?", args);
                mDatabase.child("mute").child(userId).setValue(preferenceMute);

                if (WriteToFirebaseJobIntentService.running) {
                    WriteToFirebaseJobIntentService.restart = true;
                }
                else {startService(intentService);}
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            int userAge;
            try {
                userAge = Integer.valueOf(age.getText().toString());
            }catch (Exception e){
                userAge = 0;
            }
            if (userAge < 18) {
                MApplication.makeToast(getResources().getString(R.string.minimum_age), this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int userAge;
        try {
            userAge = Integer.valueOf(age.getText().toString());
        }catch (Exception e){
            userAge = 0;
        }
        if (userAge < 18) {
            MApplication.makeToast(getResources().getString(R.string.minimum_age), this);
        }
        else {
            super.onBackPressed();
        }
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getParentActivityIntent();
        intent.putExtra("FromChildActivity", true);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOffline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOnline();
    }

    private void saveData() {
        try {
            user_age = Integer.parseInt(age.getText().toString());
        }
        catch (Exception e) {
            user_age = 18;
        }
        final boolean user_sex;
        final String [] args = {currentUserAuth.getUid()};
        if (String.valueOf(sex.getSelectedItem()).equals("Female")) {
            user_sex = false;
        }
        else {
            user_sex = true;
        }

        mDatabase.child("users").child(currentUserAuth.getUid()).child("userAge").setValue(user_age, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (databaseError == null) {
                //mDatabaseHelperAll.updateUserDelieveredAge(1, DatabaseHelperAll.KEY_USER_ID + "=?", args);
            }
            }
        });
        mDatabase.child("users").child(currentUserAuth.getUid()).child("userSex").setValue(user_sex, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (databaseError == null) {
                //mDatabaseHelperAll.updateUserDelieveredSex(1, DatabaseHelperAll.KEY_USER_ID + "=?", args);
            }
            }
        });
    }

    public void uploadPicture(View view) {
        Intent image_intent = new Intent();
        image_intent.setType("image/*");
        image_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(image_intent, "Select File"),SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        progressDialog.show();
        if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            if (selectedImage == null) {
                progressDialog.dismiss();
                return;
            }
            Uri file = selectedImage;

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
            } catch (IOException e) {
                return;
            }

            final Bitmap compressedBitmap = compressImage(bitmap);
            final byte[] byteData = BitmapConverter.bitmapToByteArrayConverter(compressedBitmap);

            mReceiver = new DatabaseIntentServiceResultReceiver (new Handler());
            mReceiver.setReceiver(this);
            SynchronizeDatabases mSynchronizeDatabases = new SynchronizeDatabases(currentUserAuth.getUid(), mReceiver, this);
            mSynchronizeDatabases.uploadPictureInFirebaseStorage(byteData);
        }
        else {progressDialog.dismiss();}
    }

    private void showPicture() {
        user_picture = (ImageView) findViewById(R.id.user_picture_settings);
        String [] args = {currentUserAuth.getUid()};
        byte [] byteArray = null;
        Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE,DatabaseHelper.KEY_NAME + "=?", args);
        if (cursor.moveToFirst()) {
            byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
        }
        if (byteArray != null) {
            Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
            user_picture.setImageBitmap(bitmap);
        }
        else {user_picture.setImageResource(R.drawable.ic_no_person);}
        progressDialog.dismiss();
    }

    private Bitmap compressImage(Bitmap bitmapImage) {
        int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
        return scaled;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        int code = resultData.getInt(DatabaseIntentService.RESULT_SERVICE_KEY);
        if (code == 3 || code == 4) {
            //progressDialog.show();
            showPicture();
        }
        /*if (code == -3 || code == -4) {
            progressDialog.show();
            showPicture();
        }*/
    }
}






/*mReceiver = new DatabaseIntentServiceResultReceiver (new Handler());
            mReceiver.setReceiver(this);
            //Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DatabaseIntentService.class);
            Intent intent = new Intent(this, DatabaseIntentService.class);
            intent.putExtra(DatabaseIntentService.RECEIVER_KEY, mReceiver);
            Bundle extras = new Bundle();
            extras.putString(DatabaseIntentService.USER_ID_INTENT_SERVICE, user.getUid());
            extras.putByteArray(DatabaseIntentService.IMAGE_BYTE_ARRAY_INTENT_SERVICE, byteData);
            intent.putExtras(extras);
            startService(intent);*/

            /*final String lastPhotoId = random();
            final StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg")
                    .setCustomMetadata("lastPhotoId", lastPhotoId).build();

            UploadTask uploadTask = mStorageRef.putBytes(byteData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    int errorCode = ((StorageException) exception).getErrorCode();
                    Toast.makeText(settingsActivity.this,
                            "Couldn't upload image on Server, failer is " + errorCode,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageRef.updateMetadata(metadata).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            String [] args = {user.getUid()};
                            byte [] byteArray = null;
                            Cursor cursor = mDatabaseHelper.query(DatabaseHelper.KEY_NAME + "=?", args);
                            if (cursor.moveToFirst()) {
                                byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
                            }
                            if (byteArray != null) {
                                mDatabaseHelper.update(user.getUid(), lastPhotoId, byteData, DatabaseHelper.KEY_NAME + "=?", args);
                                Log.d(TAG, "In settings Activity, updated Database");
                            }
                            else {
                                Log.d(TAG, "In settings Activity, inserted in Database");
                                byteArray = BitmapConverter.bitmapToByteArrayConverter(compressedBitmap);
                                mDatabaseHelper.insert1(user.getUid(), lastPhotoId, byteArray);
                            }
                            showPicture();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                int errorCode = ((StorageException) exception).getErrorCode();
                                Toast.makeText(settingsActivity.this,
                                        "Couldn't upload image on Server, failer is " + errorCode,
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                    });
                }
            });*/
