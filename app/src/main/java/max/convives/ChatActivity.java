package max.convives;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.max.chat_firebase.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "mDebugger";

    private String reference;

    //ValueEventListener myValueEventListener;
    ChildEventListener myChildEventListener;

    //private FirebaseListAdapter<ChatMessage> adapter;

    private ChatAdapterAddMore adapter;
    private List<ChatMessage> chatsList = new ArrayList<>();

    private FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder> adapter1;
    private RecyclerView recyclerView;

    private DatabaseReference mDatabase;

    String userId1;
    static String userId2;
    String user_name2;
    int user_age2;
    boolean user_sex2;
    TextView mTitle;
    ImageView profile_image;

    DatabaseHelper mDatabaseHelper;
    DatabaseHelperAll mDatabaseHelperAll;

    static boolean isChatActivityActive = false;

    Intent intentService;

    public static int AMOUNT_OF_MESSAGES_TO_LOAD_FROM_LOCAL_DATABASE = 20;
    private boolean dataReached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        Bundle extras = getIntent().getExtras();
        userId1 = extras.getString("USER_ID1");
        userId2 = extras.getString("USER_ID2");
        user_name2 = extras.getString("USER_NAME2");
        user_age2 = extras.getInt("USER_AGE2");
        user_sex2 = extras.getBoolean("USER_SEX2");

        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelperAll = new DatabaseHelperAll(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(user_name2);
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userActivity();
            }
        });
        profile_image = (ImageView) toolbar.findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userActivity();
            }
        });

        showPicture(userId2);

        reference = getReference(userId1, userId2);

        //listOfMessages = (ListView)findViewById(R.id.list_of_messages1);
        //listOfMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        recyclerView = (RecyclerView) findViewById(R.id.list_of_messages2);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new ChatAdapterAddMore(recyclerView, chatsList, userId2);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(0);

        loadMessagesFromLocalDatabase(AMOUNT_OF_MESSAGES_TO_LOAD_FROM_LOCAL_DATABASE);
        if (dataReached) {
            recyclerView.clearOnScrollListeners();
        }

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (dataReached) {
                    recyclerView.clearOnScrollListeners();
                    MApplication.makeToast(getResources().getString(R.string.data_reached), ChatActivity.this);
                    return;
                }
                chatsList.add(null);
                //chatsList.add(0, null);
                adapter.notifyItemInserted(chatsList.size() - 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatsList.remove(chatsList.size() - 1);
                        //chatsList.remove(0);
                        adapter.notifyItemRemoved(chatsList.size());
                        //adapter.notifyItemRemoved(0);

                        //Take more data
                        loadMessagesFromLocalDatabase(AMOUNT_OF_MESSAGES_TO_LOAD_FROM_LOCAL_DATABASE);

                        adapter.notifyDataSetChanged();
                        adapter.setLoaded();
                    }
                }, 1000);
            }
        });

        String [] args = {userId1, reference};
        Cursor cursor = mDatabaseHelperAll.queryChatReference(DatabaseHelperAll.KEY_USER_ID + "=?"
                + " AND " + DatabaseHelperAll.KEY_CHAT_REFERENCE + "=?", args);
        if (!cursor.moveToFirst()) {
            cursor.close();
            mDatabaseHelperAll.insertChatReference(userId1, reference, 0);
        }

        //displayChatMessages();
        //displayChatMessages1();

        intentService = new Intent(this, WriteToFirebaseJobIntentService.class);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab1);

        final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        mDatabase.child("users").child(userId1).child("newMessage").child(userId2).keepSynced(true);
        fab.setOnClickListener(new View.OnClickListener() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input1);
                if (!input.getText().toString().equals("")) {
                    addDateItem();
                    final String message = input.getText().toString();
                    final String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    final long time = new Date().getTime();
                    final ChatMessage chatMessage = new ChatMessage(message, senderName, userId2, userId1, time, 0, 0);
                    mDatabaseHelperAll.insertChatMessagesModel(chatMessage, reference, "");
                    chatsList.add(0, chatMessage);
                    adapter.notifyDataSetChanged();

                    //recyclerView.scrollToPosition(0);
                    mDatabase.child("chats").child(reference).keepSynced(true);
                    mDatabase.child("chats").child(reference).push().setValue(chatMessage, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                String [] args = {String.valueOf(time)};
                                mDatabaseHelperAll.updateChatMessagesModel(1, 0, DatabaseHelperAll.KEY_TIME + "=?", args);
                                int index = chatsList.indexOf(chatMessage);
                                chatMessage.setIsDelieveredToFirebase(1);
                                chatsList.set(index, chatMessage);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });

                    mDatabase.child("users").child(userId1).child("newMessage").child(userId2).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Log.d(TAG, "dataSnapshot is : " + dataSnapshot);
                            int newMessages;
                            try {
                                newMessages = Integer.parseInt(String.valueOf(dataSnapshot.getValue()));
                                //Log.d(TAG, "in listener, newMessages is: " + newMessages);
                            }
                            catch (Exception e) {
                                newMessages = 0;
                            }
                            newMessages += 1;
                            mDatabase.child("users").child(userId1).child("newMessage").child(userId2).setValue(newMessages);
                            //newMessages = 0;
                            //dataSnapshot = null;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //Log.d(TAG, "newMessages" + newMessages);
                }

                input.setText("");
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    int count = adapter.getItemCount() - 1;
                    if (count > 0) {
                        //recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        //this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.chat_menu_delete_match) {
            mDatabase.child("users").child(userId1).child("deletedMatches").child(userId2).setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mDatabaseHelperAll.insertDeletedUsers(userId1, userId2);
                        NavUtils.navigateUpFromSameTask(ChatActivity.this);
                    }
                    else {
                        MApplication.makeToast(getResources().getString(R.string.not_delete_match), ChatActivity.this);
                    }
                }
            });
        }
        /*if (item.getItemId() == R.id.chat_menu_mute_notifications){

            mDatabase.child("muteNotifications").child(userId1).child(userId2).setValue(!tone);
            //MenuItem muteItem = menu.findItem(R.id.chat_menu_mute_notifications);
            //muteItem.setTitle(getResources().getText(R.string.mute_notifications));
        }*/
        if (item.getItemId() == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessagesFromFribase();
        isChatActivityActive = true;
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOnline();
        mDatabase.child("users").child(userId2).child("newMessage").child(userId1).removeValue();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mDatabase.child("chats").child(reference).removeEventListener(myValueEventListener);
        if (myChildEventListener != null) {
            mDatabase.child("chats").child(reference).removeEventListener(myChildEventListener);
        }
        isChatActivityActive = false;
        OnlineStatus myOnlineStatus = new OnlineStatus(this);
        myOnlineStatus.makeOffline();
        mDatabase.child("users").child(userId2).child("newMessage").child(userId1).removeValue();
        //mDatabase.child("users").child(userId2).child("newMessage").setValue(0);
        //mDatabase.child("users").child(userId2).child("newMessage").child(userId1).setValue(0);
        //mDatabase.child("users").child(userId2).child("newMessage").child(userId1).removeValue();

        /*if (WriteToFirebaseJobIntentService.running) {
            WriteToFirebaseJobIntentService.restart = true;
        }
        else {startService(intentService);}*/
    }

    private void addDateItem() {
        String [] args = {reference, ""};
        String orderBy = DatabaseHelperAll.KEY_TIME;
        Cursor cursor = mDatabaseHelperAll.queryChatMessages(DatabaseHelperAll.KEY_REFERENCE_PARENT + "=?" + " AND " +
                DatabaseHelperAll.KEY_SENDER_ID + "=?", args, orderBy + " DESC");

        long lastDateTime = 0;
        if (cursor.moveToFirst()) {
            lastDateTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelperAll.KEY_TIME));
        }

        if (!DateUtils.isToday(lastDateTime)) {
            long currentTime = new Date().getTime();
            ChatMessage dateItemChatMessage = new ChatMessage("", "", "", "", currentTime,1, 1);
            mDatabaseHelperAll.insertChatMessagesModel(dateItemChatMessage, reference, "");
            chatsList.add(0, dateItemChatMessage);
        }
    }

    private void loadMessagesFromLocalDatabase(int amountOfMassages) {
        String [] args = {reference};
        String orderBy = DatabaseHelperAll.KEY_TIME;
        Cursor cursor = mDatabaseHelperAll.queryChatMessages(DatabaseHelperAll.KEY_REFERENCE_PARENT + "=?",
                args, orderBy + " DESC");
        int index = chatsList.size();
        //int end = index + amountOfMassages;
        int count = 0;
        if (index == 0) {index +=1;}
        if (cursor.move(index)) {
            try {
                do {
                    String message = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_MESSAGE));
                    String senderName = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_SENDER_NAME));
                    String receiverId = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_RECEIVER_ID));
                    String senderId = cursor.getString(cursor.getColumnIndex(DatabaseHelperAll.KEY_SENDER_ID));
                    int isDelieveredToFirebase = cursor.getInt(cursor.getColumnIndex(DatabaseHelperAll.KEY_IS_DELIEVERED_TO_FIREBASE));
                    int isDelieveredToUser = cursor.getInt(cursor.getColumnIndex(DatabaseHelperAll.KEY_IS_DELIEVERED_TO_USER));
                    long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelperAll.KEY_TIME));
                    ChatMessage chatMessage = new ChatMessage(message, senderName, receiverId, senderId, time, isDelieveredToFirebase, isDelieveredToUser);
                    //chatsList.add(0, chatMessage);
                    chatsList.add(chatMessage);
                    count += 1;
                    if (count == amountOfMassages) {break;}
                }
                while (cursor.moveToNext());
                if (count < amountOfMassages) {
                    dataReached = true;
                }
            }
            finally {
                cursor.close();
            }
        }
        else {
            dataReached = true;
        }
    }

    private void loadMessagesFromFribase() {
        mDatabase.child("chats").child(reference).addChildEventListener(myChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                if (!chatMessage.getSenderId().equals(userId1) && chatMessage.getIsDelieveredToUser() == 0) {
                    String key = dataSnapshot.getKey();
                    chatMessage.setIsDelieveredToFirebase(1);
                    chatMessage.setIsDelieveredToUser(1);
                    mDatabase.child("chats").child(reference).child(key).setValue(chatMessage, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                addDateItem();
                                chatMessage.setMessageTime(new Date().getTime());
                                mDatabaseHelperAll.insertChatMessagesModel(chatMessage, reference, "");
                                chatsList.add(0, chatMessage);
                                adapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(0);
                            }
                        }
                    });
                }
                if (chatMessage.getSenderId().equals(userId1) && chatMessage.getIsDelieveredToUser() == 1) {
                    String key = dataSnapshot.getKey();
                    mDatabase.child("chats").child(reference).child(key).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                long time = chatMessage.getMessageTime();
                                String [] args = {String.valueOf(time)};
                                mDatabaseHelperAll.updateChatMessagesModel(1, 1, DatabaseHelperAll.KEY_TIME + "=?", args);
                                int index = findIndexChatsList(time);
                                chatMessage.setIsDelieveredToFirebase(1);
                                chatMessage.setIsDelieveredToUser(1);
                                chatsList.set(index, chatMessage);
                                adapter.notifyDataSetChanged();
                                //recyclerView.scrollToPosition(0);
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                if (chatMessage.getSenderId().equals(userId1)) {
                    int isDelieveredToUser = chatMessage.getIsDelieveredToUser();
                    if (isDelieveredToUser == 1) {
                        String key = dataSnapshot.getKey();
                        mDatabase.child("chats").child(reference).child(key).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    long time = chatMessage.getMessageTime();
                                    String [] args = {String.valueOf(time)};
                                    mDatabaseHelperAll.updateChatMessagesModel(1, 1, DatabaseHelperAll.KEY_TIME + "=?", args);
                                    int index = findIndexChatsList(time);
                                    chatMessage.setIsDelieveredToFirebase(1);
                                    chatMessage.setIsDelieveredToUser(1);
                                    chatsList.set(index, chatMessage);
                                    adapter.notifyDataSetChanged();
                                    //recyclerView.scrollToPosition(0);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int findIndexChatsList(long timeToCompare) {
        for (int i = 0; i < chatsList.size(); i++) {
            ChatMessage chatMessage = chatsList.get(i);
            long time = chatMessage.getMessageTime();
            if (time == timeToCompare) {
                return i;
            }
        }
        return -1;
    }

    private String getReference(String userId1, String userId2) {
        String reference = "";
        int compare = userId1.compareTo(userId2);
        if (compare < 0) {
            //userId1 is smaller
            reference = userId1 + userId2;
        }
        else {
            reference = userId2 + userId1;
        }
        return reference;
    }

    private void userActivity() {
        Intent intent = new Intent(this, UserActivity.class);
        Bundle extras = new Bundle();
        extras.putString("USER_ID",userId2);
        extras.putString("USER_NAME",user_name2);
        extras.putInt("USER_AGE", user_age2);
        extras.putBoolean("USER_SEX", user_sex2);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void showPicture(String user_id) {
        String [] args = {user_id};
        byte [] byteArray = null;
        Cursor cursor = mDatabaseHelper.query(DatabaseHelper.DB_TABLE_IMAGE, DatabaseHelper.KEY_NAME + "=?", args);
        if (cursor.moveToFirst()) {
            byteArray = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE));
        }
        if (byteArray != null) {
            Bitmap bitmap = BitmapConverter.byteArrayToBitmapConverter(byteArray);
            profile_image.setImageBitmap(bitmap);
        }
        else {profile_image.setImageResource(R.drawable.ic_no_person);}
    }
}




/*mDatabase.child("chats").child(reference).child(key).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mDatabaseHelperAll.insertChatMessagesModel(chatMessage, reference, "",1, 1);
                                chatsList.add(0, chatMessage);
                                adapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(0);
                            }
                        }
                    });*/




/*if (chatMessage.getSenderId().equals(userId1)) {
                    String key = dataSnapshot.getKey();
                    int isDelieveredToFirebase = chatMessage.getIsDelieveredToFirebase();
                    if (isDelieveredToFirebase == 0) {
                        final int index = chatsList.indexOf(chatMessage);
                        chatMessage.setIsDelieveredToFirebase(1);
                        mDatabase.child("chats").child(reference).child(key).setValue(chatMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    int isDelieveredToUser = chatMessage.getIsDelieveredToUser();
                                    long time = chatMessage.getMessageTime();
                                    String [] args = {String.valueOf(time)};
                                    mDatabaseHelperAll.updateChatMessagesModel(1, isDelieveredToUser, DatabaseHelperAll.KEY_TIME + "=?", args);
                                    chatsList.set(index, chatMessage);
                                    adapter.notifyDataSetChanged();
                                    recyclerView.scrollToPosition(0);
                                }
                            }
                        });
                    }
                }*/



/*private void updateMessagesFromLocalDatabase() {
        mDatabase.child("chats").child(reference).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = myDataSnapshot.getValue(ChatMessage.class);
                    if (chatMessage.getSenderId().equals(userId1)) {
                        int isDelieveredToUser = chatMessage.getIsDelieveredToUser();
                        long time = chatMessage.getMessageTime();
                        String [] args = {String.valueOf(time)};
                        mDatabaseHelperAll.updateChatMessagesModel(1, isDelieveredToUser, DatabaseHelperAll.KEY_TIME + "=?", args);
                    }
                }
                loadMessagesFromLocalDatabase(AMOUNT_OF_MESSAGES_TO_LOAD_FROM_LOCAL_DATABASE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/



/*private void displayChatMessages1() {
        adapter1 = new FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder>(ChatMessage.class,
                R.layout.message, ChatViewHolder.class, mDatabase.child("chats").child(reference)) {

            @Override
            public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType == 1) {
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.message, parent, false);
                    return new ChatViewHolder(itemView);
                }
                else {
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.message1, parent, false);
                    return new ChatViewHolder(itemView);
                }
            }

            @Override
            public int getItemViewType(int position) {
                ChatMessage chatMessage = getItem(position);
                if (chatMessage.getMessageUser().equals(user_name2)) {
                    return 1;
                }
                else {
                    return 0;
                }
                //return super.getItemViewType(position);
            }

            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, ChatMessage model, int position) {
                viewHolder.messageText.setText(model.getMessageText());
                //viewHolder.messageUser.setText(model.getMessageUser());
                //viewHolder.messageTime.setText(android.text.format.DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                viewHolder.messageTime.setText(android.text.format.DateFormat.format("HH:mm",
                        model.getMessageTime()));
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                recyclerView.scrollToPosition(adapter1.getItemCount() - 1);
                //adapter1.notifyDataSetChanged();
            }
        };
        recyclerView.setAdapter(adapter1);
    }*/






/*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input1);
                //showDataInDatabase();
                if (!input.getText().toString().equals("")) {
                    final String message = input.getText().toString();
                    final String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    final long time = new Date().getTime();
                    chatMessage = new ChatMessage(message, senderName, userId2, userId1, time);
                    DatabaseReference databaseReference = mDatabase.child("chats").child(reference).push();
                    final String referenceToWriteToDatabase = databaseReference.getKey();
                    mDatabaseHelperAll.insertChatMessages(message, senderName, userId2, userId1, time, reference, referenceToWriteToDatabase, 0);
                    databaseReference.setValue(chatMessage, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                final String [] args = {String.valueOf(time)};
                                mDatabaseHelperAll.updateChatMessages(message, senderName, userId2, userId1, time, reference,
                                        referenceToWriteToDatabase, 1, DatabaseHelperAll.KEY_TIME + "=?", args);
                            }
                        }
                    });
                }

                input.setText("");
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });*/

        /*recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    int count = adapter1.getItemCount() - 1;
                    if (count > 0) {
                        recyclerView.smoothScrollToPosition(adapter1.getItemCount() - 1);
                    }
                }
            }
        });*/


/*private void displayChatMessages() {
        //final ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages1);

        //listOfMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, mDatabase.child("chats").child(reference)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(android.text.format.DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                listOfMessages.setSelection(adapter.getCount() - 1);
            }
        };
        listOfMessages.setAdapter(adapter);
    }*/





/*@Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mDatabase.child("muteNotifications").child(userId1).child(userId2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tone = Boolean.valueOf(String.valueOf(dataSnapshot.getValue()));
                if (tone) {
                    menu.findItem(R.id.chat_menu_mute_notifications).setTitle(getResources().getText(R.string.no_longe_mute_notifications));
                }
                else {
                    menu.findItem(R.id.chat_menu_mute_notifications).setTitle(getResources().getText(R.string.mute_notifications));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return super.onPrepareOptionsMenu(menu);
    }*/
