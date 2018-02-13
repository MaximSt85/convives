package com.example.max.chat_firebase;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Max on 11.12.2017.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {

    public TextView messageText, messageUser, messageTime;

    public ChatViewHolder (View view) {
        super(view);
        messageText = (TextView)view.findViewById(R.id.message_text);
        //messageUser = (TextView)view.findViewById(R.id.message_user);
        messageTime = (TextView)view.findViewById(R.id.message_time);
    }
}
