package max.convives;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

/**
 * Created by Max on 22.12.2017.
 */

public class SynchronizeDatabases {
    private static final String TAG = "mDebugger";

    public static final String RESULT_SERVICE_KEY = "Status_code";

    private DatabaseHelper mDatabaseHelper;
    private StorageReference mStorageRef;

    private String userId;

    private Activity cc;

    private int status_code;

    private ResultReceiver receiver;

    SynchronizeDatabases(String userId, ResultReceiver receiver, Activity cc) {
        this.userId = userId;
        this.receiver = receiver;
        this.cc = cc;
        mStorageRef = FirebaseStorage.getInstance().getReference().child("users").child(userId);
        mDatabaseHelper = new DatabaseHelper(cc);
    }

    public void getMetadataFromUserPhoto() {
        mStorageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                final String lastId = storageMetadata.getCustomMetadata("lastPhotoId");
                final String [] args = {userId};
                Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
                if (cursor.moveToFirst()) {
                    String lastIdFromLocalDatabase = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LASTID));
                    cursor.close();
                    if (lastId.equals(lastIdFromLocalDatabase)) {
                        status_code = 0;
                        Bundle bundle = new Bundle();
                        bundle.putInt(RESULT_SERVICE_KEY, status_code);
                        receiver.send(0, bundle);
                    }
                    else {
                        downloadPictureFromFirebaseStorage(lastId, true);
                    }
                }
                else {
                    downloadPictureFromFirebaseStorage(lastId, false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                if (errorCode == -13010) {
                    String [] args = {userId};
                    mDatabaseHelper.delete(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
                    status_code = errorCode;
                    Bundle bundle = new Bundle();
                    bundle.putInt(RESULT_SERVICE_KEY, status_code);
                    receiver.send(0, bundle);
                }
                else {
                    status_code = -1;
                    Bundle bundle = new Bundle();
                    bundle.putInt(RESULT_SERVICE_KEY, status_code);
                    receiver.send(0, bundle);
                }

            }
        });
    }

    public void downloadPictureFromFirebaseStorage(final String lastId, final boolean insertOrUptadeLocalDatabase) {
        final long ONE_MEGABYTE = 1024 * 1024;
        byte [] byteArrayImage = null;
        mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String [] args = {userId};
                if (insertOrUptadeLocalDatabase) {
                    mDatabaseHelper.update(DatabaseHelper.DB_TABLE_IMAGE, userId, lastId, bytes, DatabaseHelper.KEY_NAME + "=?", args);
                    status_code = 1;
                    Bundle bundle = new Bundle();
                    bundle.putInt(RESULT_SERVICE_KEY, status_code);
                    receiver.send(0, bundle);
                }
                else {
                    mDatabaseHelper.insert(DatabaseHelper.DB_TABLE_IMAGE, userId, lastId, bytes);
                    status_code = 2;
                    Bundle bundle = new Bundle();
                    bundle.putInt(RESULT_SERVICE_KEY, status_code);
                    receiver.send(0, bundle);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int errorCode = ((StorageException) e).getErrorCode();
                status_code = -2;
                Bundle bundle = new Bundle();
                bundle.putInt(RESULT_SERVICE_KEY, status_code);
                receiver.send(0, bundle);
            }
        });
    }

    public void uploadPictureInFirebaseStorage(final byte [] imageByteArray) {
        UploadTask uploadTask = mStorageRef.putBytes(imageByteArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                status_code = - 3;
                Bundle bundle = new Bundle();
                bundle.putInt(RESULT_SERVICE_KEY, status_code);
                receiver.send(0, bundle);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final String lastPhotoId = random();
                StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg")
                        .setCustomMetadata("lastPhotoId", lastPhotoId).build();

                mStorageRef.updateMetadata(metadata).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        String [] args = {userId};
                        byte [] byteArray = null;
                        Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
                        if (cursor.moveToFirst()) {
                            byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
                            cursor.close();
                        }
                        if (byteArray != null) {
                            mDatabaseHelper.update(DatabaseHelper.DB_TABLE_IMAGE, userId, lastPhotoId, imageByteArray, DatabaseHelper.KEY_NAME + "=?", args);
                            status_code = 3;
                            Bundle bundle = new Bundle();
                            bundle.putInt(RESULT_SERVICE_KEY, status_code);
                            receiver.send(0, bundle);
                        }
                        else {
                            mDatabaseHelper.insert(DatabaseHelper.DB_TABLE_IMAGE, userId, lastPhotoId, imageByteArray);
                            status_code = 4;
                            Bundle bundle = new Bundle();
                            bundle.putInt(RESULT_SERVICE_KEY, status_code);
                            receiver.send(0, bundle);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        int errorCode = ((StorageException) exception).getErrorCode();
                        status_code = -4;
                        Bundle bundle = new Bundle();
                        bundle.putInt(RESULT_SERVICE_KEY, status_code);
                        receiver.send(0, bundle);
                    }
                });
            }
        });

    }

    private static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        //int randomLength = generator.nextInt(MAX_LENGTH);
        int randomLength = 20;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
