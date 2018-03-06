package max.convives;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.max.chat_firebase.R;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "mDebugger";

    DatabaseHelper mDatabaseHelper;

    ImageView pictureView;
    TextView nameView;
    TextView ageView;
    TextView sexView;
    String userId;
    String user_name;
    int user_age;
    boolean user_sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        setTitle(getString(R.string.convivesdata));

        mDatabaseHelper = new DatabaseHelper(this);

        Bundle extras = getIntent().getExtras();
        userId = extras.getString("USER_ID");
        user_name = extras.getString("USER_NAME");
        user_age = extras.getInt("USER_AGE");
        user_sex = extras.getBoolean("USER_SEX");

        Bitmap bitmap = takeUserPicture();
        updateGUIuserData(bitmap);
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

    private Bitmap takeUserPicture() {
        String [] args = {userId};
        byte [] byteArray = null;
        Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
        if (cursor.moveToFirst()) {
            byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
        }
        Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
        return bitmap;
    }

    void updateGUIuserData (Bitmap user_picture) {
        nameView = (TextView) findViewById(R.id.user_name_activity_user);
        ageView = (TextView) findViewById(R.id.user_age_activity_user);
        sexView = (TextView) findViewById(R.id.user_sex_activity_user);
        nameView.setText(user_name);
        ageView.setText(String.valueOf(user_age));
        if (user_sex) {
            sexView.setText(R.string.search_male);
        }
        else {
            sexView.setText(R.string.search_female);
        }

        pictureView = (ImageView) findViewById(R.id.user_picture_activity_user);
        if (user_picture != null) {
            pictureView.setImageBitmap(user_picture);
        }
        else {
            pictureView.setImageResource(R.drawable.ic_no_person);
        }
    }
}
