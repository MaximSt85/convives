package com.example.max.chat_firebase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Max on 17.11.2017.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder>{

    private static final String TAG = "mDebugger";

    public interface UsersClickListener { void onUserItemClick(int clickedItemIndex); }
    final private UsersClickListener mOnClickListener;

    private List<User> usersList;
    private boolean preferenceDistanceIn;
    Activity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            public TextView name, age, sex, distance, lastOnline, newMessage;
            public RelativeLayout relaitiveLayout;
            public ImageView pic;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            name = (TextView) view.findViewById(R.id.name);
            age = (TextView) view.findViewById(R.id.age);
            sex = (TextView) view.findViewById(R.id.sex);
            distance = (TextView) view.findViewById(R.id.distance);
            lastOnline = (TextView) view.findViewById(R.id.lastOnline);
            newMessage = (TextView) view.findViewById(R.id.new_message);
            pic = (ImageView) view.findViewById(R.id.users_images);
            relaitiveLayout = (RelativeLayout) view.findViewById(R.id.relative_layout);
            relaitiveLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onUserItemClick(clickedPosition);
        }
    }

    public UsersAdapter(List<User> usersList, UsersClickListener listener, boolean preferenceDistanceIn, Activity activity) {
        this.usersList = usersList;
        this.mOnClickListener = listener;
        this.preferenceDistanceIn = preferenceDistanceIn;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        User user = usersList.get(position);
        holder.name.setText(user.getUserName());
        holder.age.setText(activity.getResources().getString(R.string.age_users_activity) + " " + String.valueOf(user.getUserAge()));
        if (user.getUserSex()) {
            holder.sex.setText(R.string.search_male);
        }
        else {
            holder.sex.setText(R.string.search_female);
        }
        if (user.getIsUserOnline()) {
            //holder.lastOnline.setTextColor(R.color.hell_blue);
            holder.lastOnline.setTextColor(ContextCompat.getColor(holder.lastOnline.getContext(), R.color.green));
            holder.lastOnline.setText(R.string.online);
        }
        else {
            holder.lastOnline.setTextColor(Color.GRAY);
            long lastOnline = user.getLastUserOnline();
            if (lastOnline == 0) {
                holder.lastOnline.setText(R.string.last_online_no_data);
            }
            else {
                String data = String.valueOf(android.text.format.DateFormat.format("dd.MM.yy | HH:mm", user.getLastUserOnline()));
                holder.lastOnline.setText(activity.getResources().getString(R.string.last_online) + " " + data);
            }
        }
        int newMessage = user.getNewMessage();
        if (newMessage > 0) {
            holder.newMessage.setText(String.valueOf(newMessage));
            holder.newMessage.setVisibility(View.VISIBLE);
        }
        else {
            holder.newMessage.setVisibility(View.GONE);
        }

        double distance = user.getUserDistance();
        if (preferenceDistanceIn) {holder.distance.setText(String.format(activity.getResources().getString(R.string.distance_users_activity) +
                " " + "%.1f", distance) + " " + activity.getResources().getString(R.string.km_users_activity));}
        else {holder.distance.setText(String.format(activity.getResources().getString(R.string.distance_users_activity) +
                " " + "%.1f", distance) + " " + activity.getResources().getString(R.string.mi_users_activity));}

        if (user.getUserPhoto() != null) {
            holder.pic.setImageBitmap(user.getUserPhoto());
        }
        else {
            holder.pic.setImageResource(R.drawable.ic_no_person);
        }

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
}






//holder.distance.setText(String.format("%.1f", user.getUserDistance()) + " m");
        /*double distance = user.getUserDistance();
        if (distance < 1000) {
            holder.distance.setText(String.format("Distance: %.0f", distance) + " m");
        }

        if (distance > 1000 || distance == 1000) {
            distance /= 1000;
            holder.distance.setText(String.format("Distance: %.1f", distance) + " km");
        }*/

