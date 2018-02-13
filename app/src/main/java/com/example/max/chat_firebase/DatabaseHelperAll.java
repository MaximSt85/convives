package com.example.max.chat_firebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Max on 29.12.2017.
 */

public class DatabaseHelperAll extends SQLiteOpenHelper {

    private static final String TAG = "mDebugger";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "databaseAll";

    // Table Names
    public static final String DB_TABLE_CHAT_MESSAGES = "tableChatMessages";
    // column names
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_TIME = "time";
    public static final String KEY_REFERENCE_PARENT = "referenceParent";
    public static final String KEY_REFERENCE_CHILD = "referenceChild";
    public static final String KEY_IS_DELIEVERED_TO_FIREBASE = "isDeliveredToFirebase";
    public static final String KEY_IS_DELIEVERED_TO_USER = "isDelieveredToUser";
    // Table create statement
    private static final String CREATE_TABLE_CHAT_MESSAGES = "CREATE TABLE " + DB_TABLE_CHAT_MESSAGES + "("+
            KEY_MESSAGE + " TEXT," +
            KEY_SENDER_NAME + " TEXT," +
            KEY_RECEIVER_ID + " TEXT," +
            KEY_SENDER_ID + " TEXT," +
            KEY_TIME + " INTEGER," +
            KEY_REFERENCE_PARENT + " TEXT," +
            KEY_REFERENCE_CHILD + " TEXT," +
            KEY_IS_DELIEVERED_TO_FIREBASE + " INTEGER," +
            KEY_IS_DELIEVERED_TO_USER + " INTEGER);";

    // Table Names
    public static final String DB_TABLE_DELETED_USERS = "tableDeletedUsers";
    // column names
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_DELETED_USER_ID = "deletedUserIds";
    // Table create statement
    private static final String CREATE_TABLE_DELETED_USERS = "CREATE TABLE " + DB_TABLE_DELETED_USERS + "("+
            KEY_USER_ID + " TEXT," +
            KEY_DELETED_USER_ID + " TEXT);";

    // Table Names
    public static final String DB_TABLE_MUTE = "tableMute";
    // column names
    public static final String KEY_IS_MUTED= "isMuted";
    public static final String KEY_IS_DELIEVERED = "isDelivered";
    // Table create statement
    private static final String CREATE_TABLE_MUTE = "CREATE TABLE " + DB_TABLE_MUTE + "("+
            KEY_USER_ID + " TEXT," +
            KEY_IS_MUTED + " INTEGER," +
            KEY_IS_DELIEVERED + " INTEGER);";

    // Table Names
    public static final String DB_TABLE_USER = "tableUser";
    // column names
    public static final String KEY_USER_PASSWORD = "userPassword";
    public static final String KEY_USER_AGE = "userAge";
    public static final String KEY_USER_SEX = "userSex";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_IS_DELIEVERED_TO_FIREBASE_AGE = "isDeliveredToFirebase";
    public static final String KEY_IS_DELIEVERED_TO_FIREBASE_SEX = "isDeliveredToFirebaseAuth";
    // Table create statement
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + DB_TABLE_USER + "("+
            KEY_USER_ID + " TEXT," +
            KEY_USER_PASSWORD + " TEXT," +
            KEY_USER_NAME + " TEXT," +
            KEY_USER_AGE + " INTEGER," +
            KEY_USER_SEX + " INTEGER," +
            KEY_IS_DELIEVERED_TO_FIREBASE_AGE + " INTEGER," +
            KEY_IS_DELIEVERED_TO_FIREBASE_SEX + " INTEGER);";

    // Table Names
    public static final String DB_TABLE_TOKENS = "tableTokens";
    // column names
    public static final String KEY_USER_TOKEN = "userToken";
    // Table create statement
    private static final String CREATE_TABLE_TOKENS = "CREATE TABLE " + DB_TABLE_TOKENS + "("+
            KEY_USER_ID + " TEXT," +
            KEY_USER_TOKEN + " TEXT," +
            KEY_IS_DELIEVERED + " INTEGER);";

    // Table Names
    public static final String DB_TABLE_CHATS_REFERENCES = "tableChats";
    // column names
    public static final String KEY_CHAT_REFERENCE = "chatReference";
    // Table create statement
    private static final String CREATE_TABLE_CHATS_REFERENCES = "CREATE TABLE " + DB_TABLE_CHATS_REFERENCES + "("+
            KEY_USER_ID + " TEXT," +
            KEY_CHAT_REFERENCE + " TEXT," +
            KEY_IS_DELIEVERED + " INTEGER);";

    public DatabaseHelperAll(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating table
        db.execSQL(CREATE_TABLE_CHAT_MESSAGES);
        db.execSQL(CREATE_TABLE_DELETED_USERS);
        db.execSQL(CREATE_TABLE_MUTE);
        //db.execSQL(CREATE_TABLE_USER);
        //db.execSQL(CREATE_TABLE_TOKENS);
        db.execSQL(CREATE_TABLE_CHATS_REFERENCES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CHAT_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_DELETED_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_MUTE);
        //db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_USER);
        //db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_TOKENS);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CHATS_REFERENCES);
        // create new table
        onCreate(db);
    }

    public void insertChatMessages(String message, String senderName, String receiverId, String senderId, long time,
                       String referenceParent, String referenceChild, int isDelieveredToFirebase, int isDelieveredToUser) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_MESSAGE, message);
        cv.put(KEY_SENDER_NAME, senderName);
        cv.put(KEY_RECEIVER_ID, receiverId);
        cv.put(KEY_SENDER_ID, senderId);
        cv.put(KEY_TIME, time);
        cv.put(KEY_REFERENCE_PARENT, referenceParent);
        cv.put(KEY_REFERENCE_CHILD, referenceChild);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE, isDelieveredToFirebase);
        cv.put(KEY_IS_DELIEVERED_TO_USER, isDelieveredToUser);
        database.insert( DB_TABLE_CHAT_MESSAGES, null, cv );
    }

    public void insertChatMessagesModel(ChatMessage chatMessage, String referenceParent, String referenceChild) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        String message = chatMessage.getMessageText();
        String senderName = chatMessage.getMessageUser();
        String receiverId = chatMessage.getReceiverId();
        String senderId = chatMessage.getSenderId();
        int isDelieveredToFirebase = chatMessage.getIsDelieveredToFirebase();
        int isDelieveredToUser = chatMessage.getIsDelieveredToUser();
        long time = chatMessage.getMessageTime();
        cv.put(KEY_MESSAGE, message);
        cv.put(KEY_SENDER_NAME, senderName);
        cv.put(KEY_RECEIVER_ID, receiverId);
        cv.put(KEY_SENDER_ID, senderId);
        cv.put(KEY_TIME, time);
        cv.put(KEY_REFERENCE_PARENT, referenceParent);
        cv.put(KEY_REFERENCE_CHILD, referenceChild);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE, isDelieveredToFirebase);
        cv.put(KEY_IS_DELIEVERED_TO_USER, isDelieveredToUser);
        database.insert( DB_TABLE_CHAT_MESSAGES, null, cv );
    }

    public int updateChatMessages(String message, String senderName, String receiverId, String senderId, long time,
                      String referenceParent, String referenceChild, int isDelieveredToFirebase, int isDelieveredToUser,
                                  String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_MESSAGE, message);
        cv.put(KEY_SENDER_NAME, senderName);
        cv.put(KEY_RECEIVER_ID, receiverId);
        cv.put(KEY_SENDER_ID, senderId);
        cv.put(KEY_TIME, time);
        cv.put(KEY_REFERENCE_PARENT, referenceParent);
        cv.put(KEY_REFERENCE_CHILD, referenceChild);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE, isDelieveredToFirebase);
        cv.put(KEY_IS_DELIEVERED_TO_USER, isDelieveredToUser);
        rowsUpdated = database.update(DB_TABLE_CHAT_MESSAGES, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public int updateChatMessagesModel(int isDelieveredToFirebase, int isDelieveredToUser, String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE, isDelieveredToFirebase);
        cv.put(KEY_IS_DELIEVERED_TO_USER, isDelieveredToUser);
        rowsUpdated = database.update(DB_TABLE_CHAT_MESSAGES, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public Cursor queryChatMessages(String selection, String[] selectionArgs, String orderBy) throws SQLiteException{
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DB_TABLE_CHAT_MESSAGES, null,	selection, selectionArgs,null, null, orderBy);
        return cursor;
    }

    public void deleteChatMessages (String selection, String[] selectionArgs) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(DB_TABLE_CHAT_MESSAGES, selection, selectionArgs);
    }

    public void insertDeletedUsers(String userId, String deletedUserId) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_DELETED_USER_ID, deletedUserId);
        database.insert( DB_TABLE_DELETED_USERS, null, cv );
    }

    public Cursor queryDeletedUsers(String selection, String[] selectionArgs) throws SQLiteException{
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DB_TABLE_DELETED_USERS, null,	selection, selectionArgs,null, null, null);
        return cursor;
    }

    public void deleteDeletedUsers(String selection, String[] selectionArgs) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(DB_TABLE_DELETED_USERS, selection, selectionArgs);
    }

    public void insertMute(String userId, int isMuted, int isDelievered) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_IS_MUTED, isMuted);
        cv.put(KEY_IS_DELIEVERED, isDelievered);
        database.insert( DB_TABLE_MUTE, null, cv );
    }

    public Cursor queryMute(String selection, String[] selectionArgs) throws SQLiteException{
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DB_TABLE_MUTE, null,	selection, selectionArgs,null, null, null);
        return cursor;
    }

    public int updateMute(String userId, int isMuted, int isDelievered, String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_IS_MUTED, isMuted);
        cv.put(KEY_IS_DELIEVERED, isDelievered);
        rowsUpdated = database.update(DB_TABLE_MUTE, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public void deleteMute(String selection, String[] selectionArgs) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(DB_TABLE_MUTE, selection, selectionArgs);
    }

    public void insertUser(String userId, String userPassword, String userName, int userAge, int userSex,
                           int isDelieveredToFirebaseAge, int isDelieveredToFirebaseSex) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_USER_PASSWORD, userPassword);
        cv.put(KEY_USER_NAME, userName);
        cv.put(KEY_USER_AGE, userAge);
        cv.put(KEY_USER_SEX, userSex);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE_AGE, isDelieveredToFirebaseAge);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE_SEX, isDelieveredToFirebaseSex);
        database.insert( DB_TABLE_USER, null, cv );
    }

    public int updateUser(String userId, String userPassword, String userName, int userAge, int userSex,
                          int isDelieveredToFirebaseAge, int isDelieveredToFirebaseSex,
                          String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_USER_PASSWORD, userPassword);
        cv.put(KEY_USER_NAME, userName);
        cv.put(KEY_USER_AGE, userAge);
        cv.put(KEY_USER_SEX, userSex);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE_AGE, isDelieveredToFirebaseAge);
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE_SEX, isDelieveredToFirebaseSex);
        rowsUpdated = database.update(DB_TABLE_USER, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public Cursor queryUser(String selection, String[] selectionArgs) throws SQLiteException{
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DB_TABLE_USER, null,	selection, selectionArgs,null, null, null);
        return cursor;
    }

    public void deleteUser(String selection, String[] selectionArgs) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(DB_TABLE_USER, selection, selectionArgs);
    }

    public int updateUserDelieveredAge(int isDelieveredToFirebaseAge,
                          String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE_AGE, isDelieveredToFirebaseAge);
        rowsUpdated = database.update(DB_TABLE_USER, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public int updateUserDelieveredSex(int isDelieveredToFirebaseSex,
                                       String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_IS_DELIEVERED_TO_FIREBASE_SEX, isDelieveredToFirebaseSex);
        rowsUpdated = database.update(DB_TABLE_USER, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public void insertToken(String userId, String userToken, int isDelieveredToFirebase) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_USER_TOKEN, userToken);
        cv.put(KEY_IS_DELIEVERED, isDelieveredToFirebase);
        database.insert( DB_TABLE_TOKENS, null, cv );
    }

    public int updateUser(String userToken, int isDelieveredToFirebase,
                          String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_TOKEN, userToken);
        cv.put(KEY_IS_DELIEVERED, isDelieveredToFirebase);
        rowsUpdated = database.update(DB_TABLE_TOKENS, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public void insertChatReference(String userId, String reference, int isDelieveredToFirebase) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_USER_ID, userId);
        cv.put(KEY_CHAT_REFERENCE, reference);
        cv.put(KEY_IS_DELIEVERED, isDelieveredToFirebase);
            database.insert( DB_TABLE_CHATS_REFERENCES, null, cv );
    }

    public Cursor queryChatReference(String selection, String[] selectionArgs) throws SQLiteException{
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(DB_TABLE_CHATS_REFERENCES, null,	selection, selectionArgs,null, null, null);
        return cursor;
    }

    public int updateChatReference(int isDelieveredToFirebase,
                          String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_IS_DELIEVERED, isDelieveredToFirebase);
        rowsUpdated = database.update(DB_TABLE_CHATS_REFERENCES, cv, selection, selectionArgs);
        return rowsUpdated;
    }

    public void deleteChatReference(String selection, String[] selectionArgs) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(DB_TABLE_CHATS_REFERENCES, selection, selectionArgs);
    }

    public void showAllDataInDatabase(String userId) {
        Log.d(TAG, "userId is " + userId);
        String [] args = {userId};
        Log.d(TAG, "DB_TABLE_CHATS_REFERENCES");
        Cursor cursor = queryChatReference(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor.moveToFirst()) {
            try {
                do {
                    String reference = cursor.getString(cursor.getColumnIndex(KEY_CHAT_REFERENCE));
                    Log.d(TAG, "reference is " + reference);
                }
                while (cursor.moveToNext());
            }
            finally {
                cursor.close();
            }
        }

        /*Log.d(TAG, "DB_TABLE_TOKENS");
        Cursor cursor1 = query(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor1.moveToFirst()) {
            try {
                do {
                    String token = cursor1.getString(cursor1.getColumnIndex(KEY_USER_TOKEN));
                    Log.d(TAG, "token is " + token);
                }
                while (cursor1.moveToNext());
            }
            finally {
                cursor1.close();
            }
        }*/

        Log.d(TAG, "DB_TABLE_DELETED_USERS");
        Cursor cursor2 = queryDeletedUsers(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor2.moveToFirst()) {
            try {
                do {
                    String deletedUserId = cursor2.getString(cursor2.getColumnIndex(KEY_DELETED_USER_ID));
                    Log.d(TAG, "deleted user id is " + deletedUserId);
                }
                while (cursor2.moveToNext());
            }
            finally {
                cursor2.close();
            }
        }

        Log.d(TAG, "DB_TABLE_MUTE");
        Cursor cursor3 = queryMute(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor3.moveToFirst()) {
            try {
                do {
                    Integer isMuted = cursor3.getInt(cursor3.getColumnIndex(KEY_IS_MUTED));
                    Log.d(TAG, "is muted is " + isMuted);
                }
                while (cursor3.moveToNext());
            }
            finally {
                cursor3.close();
            }
        }

        Log.d(TAG, "DB_TABLE_CHAT_MESSAGES");
        String orderBy = DatabaseHelperAll.KEY_TIME;
        String [] args1 = {userId, userId};
        Cursor cursor4 = queryChatMessages(DatabaseHelperAll.KEY_SENDER_ID + "=?" + " OR " + DatabaseHelperAll.KEY_RECEIVER_ID + "=?",
                args1, orderBy + " DESC");
        if (cursor4.moveToFirst()) {
            try {
                do {
                    String senderName = cursor4.getString(cursor4.getColumnIndex(KEY_SENDER_NAME));
                    String message = cursor4.getString(cursor4.getColumnIndex(KEY_MESSAGE));
                    int isDelieveredToFirebase = cursor4.getInt(cursor4.getColumnIndex(KEY_IS_DELIEVERED_TO_FIREBASE));
                    int isDelieveredToUser = cursor4.getInt(cursor4.getColumnIndex(KEY_IS_DELIEVERED_TO_USER));
                    Log.d(TAG, senderName + ": "  + message + " " + isDelieveredToFirebase + " " + isDelieveredToUser);
                }
                while (cursor4.moveToNext());
            }
            finally {
                cursor4.close();
            }
        }
    }
}
