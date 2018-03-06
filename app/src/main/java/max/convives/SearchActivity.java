package max.convives;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.max.chat_firebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "mDebugger";

    TextView simpleTextView_distance;
    TextView simpleTextView_age;
    NumberPicker numberPicker_min;
    NumberPicker numberPicker_max;
    TextView simpleTextView_sex;
    TextView simpleTextView_distance_in;
    Switch muteSwitch;
    TextView distanceTextView;
    SeekBar seekBarDistance;
    //RangeSeekBar<Integer> seekBarAge;
    CheckedTextView femaleCheckedText;
    CheckedTextView maleCheckedText;
    TextView kmTextView;
    TextView miTextView;

    final static double CONVERT_FACTOR = 0.621371;;
    final static int MAX_SEARCH_DISTANCE = 20000;
    //final static int MAX_SEARCH_DISTANCE_IN_MI = (int)(MAX_SEARCH_DISTANCE_IN_KM * CONVERT_FACTOR);
    final static int MAX_SEARCH_AGE = 50;
    final static int DEFAULT_SEARCH_DISTANCE = MAX_SEARCH_DISTANCE / 2;
    //final static int DEFAULT_SEARCH_DISTANCE_IN_MI = (int) (DEFAULT_SEARCH_DISTANCE_IN_KM * CONVERT_FACTOR);
    final static int DEFAULT_SEARCH_AGE_MIN = 18;
    final static int DEFAULT_SEARCH_AGE_MAX = 50;
    final static int DEFAULT_SEARCH_SEX = 3;

    private int preferenceDistance;
    private int preferenceAge_min;
    private int preferenceAge_max;
    private int preferenceSex;
    private boolean preferenceDistanceIn;
    private boolean preferenceMute;

    private SharedPreferences searchPreferences;
    private SharedPreferences.Editor editor;
    public static final String SEARCH_PREFERENCES = "searchPreferences" ;
    public static final String DISTANCE = "distance";
    public static final String AGE_MIN = "ageMin";
    public static final String AGE_MAX = "ageMax";
    public static final String SEX = "sex";
    public static final String DISTANCE_IN = "distanceIn";
    //public static final String MUTE_NOTIFICATIONS = "muteNotifications";

    DatabaseHelperAll mDatabaseHelperAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle(getString(R.string.search));

        mDatabaseHelperAll = new DatabaseHelperAll(this);

        simpleTextView_distance = (TextView) findViewById(R.id.simple_textview_distance);
        //simpleTextView_distance.setText(R.string.search_distance);
        simpleTextView_age = (TextView) findViewById(R.id.simple_textview_age);
        //simpleTextView_age.setText(R.string.search_age);
        simpleTextView_sex = (TextView) findViewById(R.id.simple_textview_sex);
        //simpleTextView_sex.setText(R.string.search_sex);
        simpleTextView_distance_in = (TextView) findViewById(R.id.simple_textview_distance_in);
        //simpleTextView_distance_in.setText(R.string.search_distance_in);
        femaleCheckedText = (CheckedTextView) findViewById(R.id.search_female);
        maleCheckedText = (CheckedTextView) findViewById(R.id.search_male);
        kmTextView = (TextView) findViewById(R.id.search_km);
        miTextView = (TextView) findViewById(R.id.search_mi);
        muteSwitch = (Switch) findViewById(R.id.search_switch_notifications);
        //muteSwitch.setText(R.string.mute_notifications);
        numberPicker_max = (NumberPicker) findViewById(R.id.search_number_max);
        numberPicker_min = (NumberPicker) findViewById(R.id.search_number_min);

        searchPreferences = getSharedPreferences(SEARCH_PREFERENCES, Context.MODE_PRIVATE);
        editor = searchPreferences.edit();

        preferenceAge_min = searchPreferences.getInt(AGE_MIN, 18);
        preferenceAge_max = searchPreferences.getInt(AGE_MAX, 50);
        numberPicker_min.setMinValue(DEFAULT_SEARCH_AGE_MIN);
        numberPicker_min.setMaxValue(DEFAULT_SEARCH_AGE_MAX);
        numberPicker_max.setMinValue(DEFAULT_SEARCH_AGE_MIN);
        numberPicker_max.setMaxValue(DEFAULT_SEARCH_AGE_MAX);
        numberPicker_min.setValue(preferenceAge_min);
        numberPicker_max.setValue(preferenceAge_max);
        //numberPicker_min.setWrapSelectorWheel(true);
        numberPicker_min.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                numberPicker_max.setMinValue(newVal);
                preferenceAge_min = newVal;
            }
        });
        numberPicker_max.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                numberPicker_min.setMaxValue(newVal);
                preferenceAge_max = newVal;
            }
        });

        preferenceDistanceIn = searchPreferences.getBoolean(DISTANCE_IN, true); //true: km, false: mi
        if (preferenceDistanceIn) {
            kmTextView.setBackgroundResource(R.drawable.shape_for_km_mi);
            miTextView.setBackgroundResource(android.R.color.transparent);
        }
        else {
            kmTextView.setBackgroundResource(android.R.color.transparent);
            miTextView.setBackgroundResource(R.drawable.shape_for_km_mi);
        }

        kmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((kmTextView.getBackground() instanceof ColorDrawable)) {
                    kmTextView.setBackgroundResource(R.drawable.shape_for_km_mi);
                    miTextView.setBackgroundResource(android.R.color.transparent);
                    preferenceDistanceIn = true;
                    prepareDistanceSeekBar();
                }
            }
        });
        miTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((miTextView.getBackground() instanceof ColorDrawable)) {
                    kmTextView.setBackgroundResource(android.R.color.transparent);
                    miTextView.setBackgroundResource(R.drawable.shape_for_km_mi);
                    preferenceDistanceIn = false;
                    prepareDistanceSeekBar();
                }
            }
        });

        seekBarDistance = (SeekBar) findViewById(R.id.search_distance_seek_bar);
        distanceTextView = (TextView) findViewById(R.id.search_distance_text_view);
        preferenceDistance = searchPreferences.getInt(DISTANCE, DEFAULT_SEARCH_DISTANCE);
        if (preferenceDistanceIn) {
            seekBarDistance.setMax(MAX_SEARCH_DISTANCE);
            seekBarDistance.setProgress(preferenceDistance);
            distanceTextView.setText(String.valueOf(seekBarDistance.getProgress() + " km"));
        }
        else {
            seekBarDistance.setMax(convertDistance(MAX_SEARCH_DISTANCE, true));
            seekBarDistance.setProgress(preferenceDistance);
            distanceTextView.setText(String.valueOf(seekBarDistance.getProgress() + " mi"));
        }
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (preferenceDistanceIn) {distanceTextView.setText(String.valueOf(progress) + " km");}
                else {distanceTextView.setText(String.valueOf(progress) + " mi");}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferenceDistance = seekBar.getProgress();
            }
        });

        preferenceSex = searchPreferences.getInt(SEX, DEFAULT_SEARCH_SEX);
        switch (preferenceSex) {
            case 1:
                femaleCheckedText.setChecked(true);
                maleCheckedText.setChecked(false);
                break;
            case 2:
                femaleCheckedText.setChecked(false);
                maleCheckedText.setChecked(true);
                break;
            case 3:
                femaleCheckedText.setChecked(true);
                maleCheckedText.setChecked(true);
                break;
        }
        femaleCheckedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (femaleCheckedText.isChecked() && !maleCheckedText.isChecked()) {
                    femaleCheckedText.setChecked(false);
                    maleCheckedText.setChecked(true);
                }
                else {femaleCheckedText.setChecked(!femaleCheckedText.isChecked());}
            }
        });
        maleCheckedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maleCheckedText.isChecked() && !femaleCheckedText.isChecked()) {
                    maleCheckedText.setChecked(false);
                    femaleCheckedText.setChecked(true);
                }
                else {maleCheckedText.setChecked(!maleCheckedText.isChecked());}
            }
        });

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = currentUser.getUid();
        final String [] args = {userId};
        int isMute;
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
        //preferenceMute = searchPreferences.getBoolean(MUTE_NOTIFICATIONS, true); //true: mute, false: not
        muteSwitch.setChecked(preferenceMute);
        final Intent intentService = new Intent(SearchActivity.this, WriteToFirebaseJobIntentService.class);
        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                muteSwitch.setChecked(isChecked);
                preferenceMute = isChecked;
                if (preferenceMute) {
                    mDatabaseHelperAll.updateMute(userId, 1, 0, DatabaseHelperAll.KEY_USER_ID + "=?", args);
                }
                else {
                    mDatabaseHelperAll.updateMute(userId, 0, 0, DatabaseHelperAll.KEY_USER_ID + "=?", args);
                }
                //editor.putBoolean(MUTE_NOTIFICATIONS, preferenceMute);
                //editor.commit();
                //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //String userId = currentUser.getUid();
                //String reference = "mute/" + userId;
                //intentService.putExtra(WriteToFirebaseJobIntentService.FIREBASE_REFERENCE_JOBINTENT_SERVICE, reference);
                //intentService.putExtra(WriteToFirebaseJobIntentService.IS_MUTE_JOBINTENT_SERVICE, preferenceMute);

                if (WriteToFirebaseJobIntentService.running) {
                    WriteToFirebaseJobIntentService.restart = true;
                }
                else {startService(intentService);}
            }
        });
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

        //if (!preferenceDistanceIn) {preferenceDistance = convertDistance(preferenceDistance, false);}
        editor.putInt(DISTANCE, preferenceDistance);
        //editor.commit();

        if (femaleCheckedText.isChecked() && maleCheckedText.isChecked()) {preferenceSex = 3;}
        if (femaleCheckedText.isChecked() && !maleCheckedText.isChecked()) {preferenceSex = 1;}
        if (!femaleCheckedText.isChecked() && maleCheckedText.isChecked()) {preferenceSex = 2;}
        editor.putInt(SEX, preferenceSex);
        //editor.commit();

        editor.putBoolean(DISTANCE_IN, preferenceDistanceIn);
        //editor.commit();

        editor.putInt(AGE_MIN, preferenceAge_min);
        editor.putInt(AGE_MAX, preferenceAge_max);

        editor.commit();
    }

    public int convertDistance(int distance, boolean flag) {
        int convertedDistance;
        if (flag) {convertedDistance = (int)(distance * CONVERT_FACTOR);}
        else  {convertedDistance = (int)(distance / CONVERT_FACTOR);}
        return convertedDistance;
    }

    private void prepareDistanceSeekBar() {
        if (preferenceDistanceIn) {
            seekBarDistance.setMax(MAX_SEARCH_DISTANCE);
            preferenceDistance = convertDistance(preferenceDistance, false);
            seekBarDistance.setProgress(preferenceDistance);
            distanceTextView.setText(String.valueOf(seekBarDistance.getProgress() + " km"));
        }
        else {
            seekBarDistance.setMax(convertDistance(MAX_SEARCH_DISTANCE, true));
            preferenceDistance = convertDistance(preferenceDistance, true);
            seekBarDistance.setProgress(preferenceDistance);
            distanceTextView.setText(String.valueOf(seekBarDistance.getProgress() + " mi"));
        }
    }
}



/*//seekBarAge = new RangeSeekBar<Integer>(this);
        seekBarAge = (RangeSeekBar) findViewById(R.id.rangeSeekbar);
        seekBarAge.setRangeValues(0, 100);
        seekBarAge.setSelectedMinValue(20);
        seekBarAge.setSelectedMaxValue(80);
        //seekBarAge.setNotifyWhileDragging(true);
        seekBarAge.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {

            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                //Now you have the minValue and maxValue of your RangeSeekbar
                Toast.makeText(getApplicationContext(), minValue + "-" + maxValue, Toast.LENGTH_LONG).show();
                //Log.d(TAG, "value is " + minValue);
            }
        });*/
