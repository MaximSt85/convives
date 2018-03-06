package max.convives;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by Max on 06.12.2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "mDebugger";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "database_usersPhoto";

    // Table Names
    public static final String DB_TABLE_IMAGE = "table_image";
    // column names
    public static final String KEY_NAME = "image_name";
    public static final String KEY_LASTID = "image_lastId";
    public static final String KEY_IMAGE = "image_data";
    // Table create statement
    private static final String CREATE_TABLE_IMAGE = "CREATE TABLE " + DB_TABLE_IMAGE + "("+
            KEY_NAME + " TEXT," +
            KEY_LASTID + " TEXT," +
            KEY_IMAGE + " BLOB);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating table
        db.execSQL(CREATE_TABLE_IMAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_IMAGE);
        // create new table
        onCreate(db);
    }

    public void insert(String databaseTableName, String name, String lastId, byte[] bytes) throws SQLiteException{
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_NAME, name);
        cv.put(KEY_LASTID, lastId);
        cv.put(KEY_IMAGE, bytes);
        //database.insert( DB_TABLE_IMAGE, null, cv );
        database.insert( databaseTableName, null, cv );
    }

    public void update(String databaseTableName, String name, String lastId, byte[] image, String selection, String[] selectionArgs) throws SQLiteException{
        int rowsUpdated = 0;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(KEY_NAME, name);
        cv.put(KEY_LASTID, lastId);
        cv.put(KEY_IMAGE, image);
        //rowsUpdated = database.update(DB_TABLE_IMAGE, cv, selection, selectionArgs);
        rowsUpdated = database.update(databaseTableName, cv, selection, selectionArgs);
    }

    public Cursor query(String databaseTableName, String selection, String[] selectionArgs) throws SQLiteException{
        byte[] data;
        SQLiteDatabase database = this.getReadableDatabase();
        //Cursor cursor = database.query(DB_TABLE_IMAGE, null,	selection, selectionArgs,null, null, null);
        Cursor cursor = database.query(databaseTableName, null,	selection, selectionArgs,null, null, null);
        return cursor;
    }

    public void delete(String databaseTableName, String selection, String[] selectionArgs) {
        SQLiteDatabase database = this.getWritableDatabase();
        //database.delete(DB_TABLE_IMAGE, selection, selectionArgs);
        database.delete(databaseTableName, selection, selectionArgs);
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

}
