package com.example.max.chat_firebase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UsersAdapter.UsersClickListener, DatabaseIntentServiceResultReceiver.Receiver,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//public class UsersActivity extends AppCompatActivity implements UsersAdapter.UsersClickListener {

    private static final String TAG = "mDebugger";

    private StorageReference mStorageRef;


    private List<User> usersList = new ArrayList<>();
    private List<Integer> indexes = new ArrayList<>();
    private RecyclerView recyclerView;
    private UsersAdapter mAdapter;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private double currentUserLatitude;
    private double currentUserLongitude;

    ValueEventListener myListener;

    ProgressDialog progressDialog;

    DatabaseHelper mDatabaseHelper;
    DatabaseHelperAll mDatabaseHelperAll;

    private int count = 0;

    private DatabaseIntentServiceResultReceiver mReceiver;

    private SharedPreferences searchPreferences;
    private int preferenceDistance;
    private boolean preferenceDistanceIn;
    private int preferenceAgeMin;
    private int preferenceAgeMax;
    private int preferenceSex;
    private List<String> deletedUsers = new ArrayList<>();

    String currentUserId;

    private Location location;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        setTitle(getString(R.string.convives));

        progressDialog = new ProgressDialog(UsersActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.show();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelperAll = new DatabaseHelperAll(this);
        //this.deleteDatabase(DatabaseHelper.DATABASE_NAME);

        searchPreferences = getSharedPreferences(SearchActivity.SEARCH_PREFERENCES, Context.MODE_PRIVATE);
        preferenceDistance = searchPreferences.getInt(SearchActivity.DISTANCE, SearchActivity.DEFAULT_SEARCH_DISTANCE);
        preferenceAgeMin = searchPreferences.getInt(SearchActivity.AGE_MIN, SearchActivity.DEFAULT_SEARCH_AGE_MIN);
        preferenceAgeMax = searchPreferences.getInt(SearchActivity.AGE_MAX, SearchActivity.DEFAULT_SEARCH_AGE_MAX);
        preferenceDistanceIn = searchPreferences.getBoolean(SearchActivity.DISTANCE_IN, true);
        preferenceSex = searchPreferences.getInt(SearchActivity.SEX, SearchActivity.DEFAULT_SEARCH_SEX);

        final String [] args = {currentUserId};
        Cursor cursor = mDatabaseHelperAll.queryDeletedUsers(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor.moveToFirst()) {
            try {
                do {
                    String deletedUserId = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_DELETED_USER_ID));
                    deletedUsers.add(deletedUserId);
                }
                while (cursor.moveToNext());
            }
            finally {
                cursor.close();
            }
        }

        /*Bundle extras = getIntent().getExtras();
        currentUserLatitude = extras.getDouble("latitude");
        currentUserLongitude = extras.getDouble("longitude");*/

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new UsersAdapter(usersList, this, preferenceDistanceIn, UsersActivity.this);

        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        //recyclerView.setLayoutManager(linearLayoutManager);
        //RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void addValueEventListener() {
        mDatabase.child("users").addValueEventListener(myListener = new ValueEventListener() {
            //database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //usersList.clear();
                indexes.clear();
                for (DataSnapshot userDataSnapshot : dataSnapshot.getChildren()) {
                    String userId = String.valueOf(userDataSnapshot.child("userId").getValue());
                    boolean userDeleted = false;
                    for (DataSnapshot deletedMatchesDataSnapshot : userDataSnapshot.child("deletedMatches").getChildren()) {
                        if (Boolean.valueOf(String.valueOf(deletedMatchesDataSnapshot.getValue()))) {
                            String deletedMatchId = String.valueOf(deletedMatchesDataSnapshot.getKey());
                            if (currentUserId.equals(deletedMatchId)) {
                                userDeleted = true;
                                break;
                            }
                        }
                    }
                    if (userDeleted) {continue;}
                    String name = String.valueOf(userDataSnapshot.child("userName").getValue());
                    int age = Integer.parseInt(String.valueOf(userDataSnapshot.child("userAge").getValue()));
                    boolean sex = Boolean.parseBoolean(String.valueOf(userDataSnapshot.child("userSex").getValue()));
                    double userLatitude = Double.parseDouble(String.valueOf(userDataSnapshot.child("userLatitude").getValue()));
                    double userLongitude = Double.parseDouble(String.valueOf(userDataSnapshot.child("userLongitude").getValue()));
                    boolean isUserOnline = Boolean.parseBoolean(String.valueOf(userDataSnapshot.child("isUserOnline").getValue()));
                    long lastUserOnline;
                    try {
                        lastUserOnline = Long.parseLong(String.valueOf(userDataSnapshot.child("lastUserOnline").getValue()));
                    }
                    catch (Exception e) {
                        lastUserOnline = 0;
                    }
                    int newMessage;
                    try {
                        //newMessage = Integer.parseInt(String.valueOf(userDataSnapshot.child("newMessage").getValue()));
                        newMessage = Integer.parseInt(String.valueOf(userDataSnapshot.child("newMessage").child(currentUserId).getValue()));
                    }
                    catch (Exception e) {
                        newMessage = 0;
                    }
                    String token = String.valueOf(userDataSnapshot.child("notificationTokens").getValue());
                    double userDistance = calculateDistance(userLatitude, userLongitude, preferenceDistanceIn);
                    boolean isUserDeleted = isUserDeleted(deletedUsers, userId);
                    if (!currentUserId.equals(userId)
                            && ((preferenceSex == 3) || (sex && preferenceSex == 2) || (!sex && preferenceSex == 1))
                            && (!(age > preferenceAgeMax) || preferenceAgeMax == SearchActivity.DEFAULT_SEARCH_AGE_MAX)
                            && !(age < preferenceAgeMin)
                            && !isUserDeleted) {
                        boolean userExists = false;
                        for (int i = 0; i < usersList.size(); i++) {
                            if (usersList.get(i).getUserId().equals(userId)) {
                                User user = usersList.get(i);
                                userExists = true;
                                if (userDistance > preferenceDistance) {
                                    usersList.remove(user);
                                } else {
                                    user.setUserName(name);
                                    user.setUserAge(age);
                                    user.setUserSex(sex);
                                    user.setUserLatitude(userLatitude);
                                    user.setUserLongitude(userLongitude);
                                    user.setIsUserOnline(isUserOnline);
                                    user.setLastUserOnline(lastUserOnline);
                                    user.setNotificationTokens(token);
                                    user.setUserDistance(userDistance);
                                    user.setNewMessage(newMessage);
                                }
                                break;
                            }
                        }
                        if (!userExists) {
                            if (userDistance < preferenceDistance || userDistance == preferenceDistance) {
                                User user = new User(name, age, sex, userId, userLatitude, userLongitude, isUserOnline, lastUserOnline, token, newMessage);
                                user.setUserDistance(userDistance);
                                usersList.add(user);
                                indexes.add(usersList.indexOf(user));
                            }
                        }
                    }
                }
                takePicturesFromLocalDatabase();
                //mAdapter.notifyDataSetChanged();
                //dowloadPictures(indexes);
                synchronizePhotoBetweenDataBases1();
                //progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getParentActivityIntent();
        intent.putExtra("FromChildActivity", true);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOnline();
        //addValueEventListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        mDatabase.child("users").removeEventListener(myListener);
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOffline();
    }

    @Override
    public void onUserItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle extras = new Bundle();
        /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId1 = user.getUid();*/

        String userId2 = usersList.get(clickedItemIndex).getUserId();
        String username = usersList.get(clickedItemIndex).getUserName();
        int userage = usersList.get(clickedItemIndex).getUserAge();
        boolean usersex = usersList.get(clickedItemIndex).getUserSex();

        //extras.putString("USER_ID1", userId1);
        extras.putString("USER_ID1", currentUserId);
        extras.putString("USER_ID2", userId2);
        extras.putString("USER_NAME2", username);
        extras.putInt("USER_AGE2", userage);
        extras.putBoolean("USER_SEX2", usersex);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public double calculateDistance(double latitude, double longitude, boolean flag) {
        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, currentUserLatitude, currentUserLongitude, results);
        if (flag) {return results[0] / 1000;}
        else {return (results[0] * SearchActivity.CONVERT_FACTOR / 1000);}
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void takePicturesFromLocalDatabase() {
        for (int i = 0; i < indexes.size(); i++) {
            User currentUser = usersList.get(i);
            final String[] args = {currentUser.getUserId()};
            Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE,DatabaseHelper.KEY_NAME + "=?", args);
            byte[] byteArray = null;
            if (cursor.moveToFirst()) {
                byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
            }
            if (byteArray != null) {
                Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
                currentUser.setUserPhoto(bitmap);
            } else {
                currentUser.setUserPhoto(null);
            }
        }
        mAdapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    private void synchronizePhotoBetweenDataBases() {
        count = 0;
        mStorageRef = FirebaseStorage.getInstance().getReference().child("users");
        for (int i = 0; i < indexes.size(); i++) {
            final String userId = usersList.get(indexes.get(i)).getUserId();
            final StorageReference users_storage = mStorageRef.child(userId);
            users_storage.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    final String lastId = storageMetadata.getCustomMetadata("lastPhotoId");
                    final String[] args = {userId};
                    Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE,DatabaseHelper.KEY_NAME + "=?", args);
                    String insertOrUptade = "";
                    if (cursor.moveToFirst()) {
                        String lastIdFromLocalDatabase = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LASTID));
                        if (lastId.equals(lastIdFromLocalDatabase)) {
                            count += 1;
                            if (count == indexes.size()) {
                                takePicturesFromLocalDatabase();
                            }
                        } else {
                            insertOrUptade = "update";
                        }
                    } else {
                        insertOrUptade = "insert";
                    }
                    if (insertOrUptade.equals("update") || insertOrUptade.equals("insert")) {
                        final long ONE_MEGABYTE = 1024 * 1024;
                        byte[] byteArrayImage = null;
                        final String finalInsertOrUptade = insertOrUptade;
                        users_storage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                String[] args = {userId};
                                if (finalInsertOrUptade.equals("update")) {
                                    mDatabaseHelper.update(DatabaseHelper.DB_TABLE_IMAGE, userId, lastId, bytes, DatabaseHelper.KEY_NAME + "=?", args);
                                    count += 1;
                                    if (count == indexes.size()) {
                                        takePicturesFromLocalDatabase();
                                    }
                                } else {
                                    mDatabaseHelper.insert(DatabaseHelper.DB_TABLE_IMAGE, userId, lastId, bytes);
                                    count += 1;
                                    if (count == indexes.size()) {
                                        takePicturesFromLocalDatabase();
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                int errorCode = ((StorageException) e).getErrorCode();
                                count += 1;
                                if (count == indexes.size()) {
                                    takePicturesFromLocalDatabase();
                                }
                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    int errorCode = ((StorageException) exception).getErrorCode();
                    if (errorCode == -13010) {
                        count += 1;
                        if (count == indexes.size()) {
                            takePicturesFromLocalDatabase();
                        }
                    } else {
                        count += 1;
                        if (count == indexes.size()) {
                            takePicturesFromLocalDatabase();
                        }
                    }
                }
            });
        }
    }

    void synchronizePhotoBetweenDataBases1() {
        count = 0;
        for (int i = 0; i < indexes.size(); i++) {
            String userId = usersList.get(indexes.get(i)).getUserId();
            mReceiver = new DatabaseIntentServiceResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            SynchronizeDatabases mSynchronizeDatabases = new SynchronizeDatabases(userId, mReceiver, this);
            mSynchronizeDatabases.getMetadataFromUserPhoto();
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        int code = resultData.getInt(DatabaseIntentService.RESULT_SERVICE_KEY);

        count += 1;
        if (count == indexes.size()) {
            takePicturesFromLocalDatabase();
            //mAdapter.notifyDataSetChanged();
        }
    }

    private boolean isUserDeleted (List<String> values, String userId) {
        for (String value : values) {
            if (value.equals(userId))
                return true;
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //myOnLocationChanged();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        makeToast(getResources().getString(R.string.current_position));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        makeToast(getResources().getString(R.string.current_position));
    }

    @Override
    public void onLocationChanged(Location location) {
        //makeToast("in onLocationChanged");
        currentUserLatitude = location.getLatitude();
        currentUserLongitude = location.getLongitude();
        mDatabase.child("users").child(currentUser.getUid()).child("userLatitude").setValue(currentUserLatitude);
        mDatabase.child("users").child(currentUser.getUid()).child("userLongitude").setValue(currentUserLongitude);
        addValueEventListener();
    }

    private void myOnLocationChanged() {
        currentUserLatitude = 0;
        currentUserLongitude = 0;
        mDatabase.child("users").child(currentUser.getUid()).child("userLatitude").setValue(currentUserLatitude);
        mDatabase.child("users").child(currentUser.getUid()).child("userLongitude").setValue(currentUserLongitude);
        addValueEventListener();
    }
}






/*mDatabase.child("deletedMatches").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot deletedMatchesDataSnapshot : dataSnapshot.getChildren()) {
                                if (Boolean.valueOf(String.valueOf(deletedMatchesDataSnapshot.getValue()))) {
                                    String deletedMatchId = String.valueOf(deletedMatchesDataSnapshot.getKey());
                                    if (!currentUserId.equals(deletedMatchId)) {

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });*/







    /*class DatabaseAsyncTask extends AsyncTask<DataForAsyncTask, Void, Void> {

        @Override
        protected Void doInBackground(DataForAsyncTask... dataForAsyncTasks) {
            //SystemClock.sleep(5000);
            int index = dataForAsyncTasks[0].getIndex();
            boolean insertOrUpdate = dataForAsyncTasks[0].getInsertOrUpdate();
            String lastId = dataForAsyncTasks[0].getLastId();

            String userId = usersList.get(index).getUserId();
            Bitmap userPicture = usersList.get(index).getUserPhoto();
            String [] args = {userId};
            byte [] image = BitmapConverter.bitmapToByteArrayConverter(userPicture);
            if (insertOrUpdate) {
                mDatabaseHelper.update(userId, lastId, image, DatabaseHelper.KEY_NAME + "=?", args);
            }
            else {
                mDatabaseHelper.insert(userId, lastId, userPicture);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //mAdapter.notifyDataSetChanged();
        }
    }*/


    /*private void synchronizePhotoBetweenDataBases(final List<Integer> indexes) {

        count = 0;

        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference users_storage = mStorageRef.child("users");

        for (int i = 0; i < indexes.size(); i++) {
            final int index = indexes.get(i);
            final String userId = usersList.get(index).getUserId();
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
                            usersList.get(index).setUserPhoto(bitmap);
                            count += 1;
                            if (count == indexes.size()) {
                                mAdapter.notifyDataSetChanged();
                                //progressDialog.dismiss();
                            }
                        }
                        else {
                            try {
                                downloadPictureFromFirebaseStorage(user_storage, index, true, lastId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        try {
                            downloadPictureFromFirebaseStorage(user_storage, index, false, lastId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    int errorCode = ((StorageException) exception).getErrorCode();
                    if (errorCode == -13010) {
                        String [] args = {userId};
                        mDatabaseHelper.delete(DatabaseHelper.KEY_NAME + "=?", args);
                    }
                    count += 1;
                    usersList.get(index).setUserPhoto(null);
                    if (count == indexes.size()) {
                        mAdapter.notifyDataSetChanged();
                        //progressDialog.dismiss();
                    }
                }
            });
        }
    }*/

    /*public void downloadPictureFromFirebaseStorage(StorageReference user_storage, final int index,
                                                   final boolean insertOrUptadeLocalDatabase,
                                                   final String lastId) throws IOException {

        final File localFile;
        final Bitmap[] picture = new Bitmap[1];
        localFile = File.createTempFile("images", "jpg");

        user_storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                String filePath = localFile.getPath();
                picture[0] = BitmapFactory.decodeFile(filePath);
                usersList.get(index).setUserPhoto(picture[0]);
                count += 1;
                if (insertOrUptadeLocalDatabase) {
                    DataForAsyncTask mDataForAsyncTask = new DataForAsyncTask(index, insertOrUptadeLocalDatabase, lastId);
                    new DatabaseAsyncTask().execute(mDataForAsyncTask);
                }
                else {
                    DataForAsyncTask mDataForAsyncTask = new DataForAsyncTask(index, insertOrUptadeLocalDatabase, lastId);
                    new DatabaseAsyncTask().execute(mDataForAsyncTask);
                }
                if (count == indexes.size()) {
                    mAdapter.notifyDataSetChanged();
                    //progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                usersList.get(index).setUserPhoto(null);
                count += 1;
                if (count == indexes.size()) {
                    mAdapter.notifyDataSetChanged();
                    //progressDialog.dismiss();
                }
            }
        });
    }*/

/*class DataForAsyncTask {
    public int index;
    public boolean insertOrUpdate;
    public String lastId;

    public DataForAsyncTask (int index, boolean insertOrUpdate, String lastId) {
        this.index = index;
        this.insertOrUpdate = insertOrUpdate;
        this.lastId = lastId;
    }

    public int getIndex() {return index;}
    public boolean getInsertOrUpdate() {return insertOrUpdate;}
    public String  getLastId() {return lastId;}
}*/



/*public void startServiceIntent(String userId, Bitmap picture) {
        byte [] imageInput = BitmapConverter.bitmapToByteArrayConverter(picture);

        //Intent dbIntentervice = new Intent(this, DatabaseIntentService.class);
        //dbIntentervice.putExtra(DatabaseIntentService.USER_ID_INTENT_SERVICE, userId);
        //dbIntentervice.putExtra(DatabaseIntentService.IMAGE_INTENT_SERVICE, imageInput);
        //startService(dbIntentervice);

        Intent intentService = new Intent(this, DatabasekakaService.class);
        intentService.putExtra(DatabaseIntentService.USER_ID_INTENT_SERVICE, userId);
        intentService.putExtra(DatabaseIntentService.IMAGE_INTENT_SERVICE, imageInput);
        startService(intentService);
    }*/


/*private UserExistsData checkUserExists(List<User> usersList, String userid) {
        boolean userExists = false;
        int index = 0;

        UserExistsData userExistsData = null;
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getUserId().equals(userid)) {
                userExists = true;
                index = i;
                break;
            }
        }
        userExistsData = new UserExistsData(index, userExists);
        return userExistsData;
    }*/


/*class UserExistsData {
    public int index = 0;
    public boolean userExists = false;

    public UserExistsData (int index, boolean userExists) {
        this.index = index;
        this.userExists = userExists;
    }
}*/


/*//if (!currentUserId.equals(userId) && isUserOnline) {
                    if (!currentUserId.equals(userId)) {
                        //if (userDistance < 15000) {
                        //    User user = new User(name, age, sex, userId, userLatitude, userLongitude, isUserOnline);
                        //    user.setUserDistance(userDistance);
                        //    usersList.add(user);
                        //}
                        User user = new User(name, age, sex, userId, userLatitude, userLongitude, isUserOnline, token);
                        user.setUserDistance(userDistance);

                        UserExistsData userExistsData = checkUserExists(usersList, userId);
                        if (userExistsData.userExists) {
                            if (usersList.get(userExistsData.index).getUserPhoto() != null) {
                                Bitmap photo = usersList.get(userExistsData.index).getUserPhoto();
                                user.setUserPhoto(photo);
                            }
                            usersList.set(userExistsData.index, user);
                        }
                        else {
                            usersList.add(user);
                            indexes.add(usersList.indexOf(user));
                        }
                    }*/