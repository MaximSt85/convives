package max.convives;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Max on 29.11.2017.
 */

public class mContentProvider extends ContentProvider {

    private static final String TAG = "mDebugger";

    static final String AUTHORITY  = "com.myPackage.max.chat_firebase";
    private static final String BASE_PATH = "users";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String AGE = "age";
    static final String SEX = "sex";
    static final String FIREBASE_USER_ID = "firebase_user_id";

    static final int USERS = 1;
    static final int USERS_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, USERS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", USERS_ID);
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "firebaseDatabase";
    static final String USERS_TABLE_NAME = "users";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + USERS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME + " TEXT NOT NULL," +
                    AGE + " INT NOT NULL," +
                    SEX + " INT NOT NULL, " +
                    FIREBASE_USER_ID + " TEXT NOT NULL);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
            //Log.d(TAG, "in onCreate databasehelper");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //Log.d(TAG, "in onUpgrade databasehelper");
            db.execSQL("DROP TABLE IF EXISTS " +  USERS_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        //Log.d(TAG, "in onCreate contentprovider");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        //Log.d(TAG, "db is " + db);
        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(USERS_TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case USERS:
                break;
            case USERS_ID:
                qb.appendWhere( _ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (sortOrder == null || sortOrder == "") {
            sortOrder = NAME;
        }

        Cursor cursor = qb.query(db, projection,	selection, selectionArgs,null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case USERS:
                return "vnd.android.cursor.dir/vnd.myPackage.students";
            case USERS_ID:
                return "vnd.android.cursor.item/vnd.myPackage.students";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        //Log.d(TAG, "in insert contentProvider, uri is " + uri);
        //Log.d(TAG, "in insert contentProvider, values are " + values);
        int uriType = uriMatcher.match(uri);
        long id = 0;
        switch (uriType) {
            case USERS:
                id = db.insert(USERS_TABLE_NAME, null, values);
                //Log.d(TAG, "in insert contentProvider, case USERS");
                break;
            default:
                //Log.d(TAG, "in insert contentProvider, case default");
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        //Log.d(TAG, "in insert contentProvider");

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int uriType = uriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (uriType) {
            case USERS:
                rowsDeleted = db.delete(USERS_TABLE_NAME, selection,
                        selectionArgs);
                break;
            case USERS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(USERS_TABLE_NAME,_ID  + "=" + id,null);
                } else {
                    rowsDeleted = db.delete(USERS_TABLE_NAME,_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {
            case USERS:
                rowsUpdated = db.update(USERS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case USERS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    //Log.d(TAG, "in update contentProvider, id is " + id);
                    rowsUpdated = db.update(USERS_TABLE_NAME, values,_ID + "=" + id,null);
                    //Log.d(TAG, "in update contentProvider, rowsUpdated is " + rowsUpdated);
                } else {
                    rowsUpdated = db.update(USERS_TABLE_NAME, values,_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
