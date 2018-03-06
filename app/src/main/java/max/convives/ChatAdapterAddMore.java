package max.convives;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.max.chat_firebase.R;

import java.util.List;

/**
 * Created by Max on 04.01.2018.
 */

public class ChatAdapterAddMore extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "mDebugger";

    private final int VIEW_TYPE_ITEM_SENDER = 0;
    private final int VIEW_TYPE_ITEM_SENDER_DELIEVERED_TO_USER = 1;
    private final int VIEW_TYPE_ITEM_RECIEVER = 2;
    private final int VIEW_TYPE_LOADING = 3;
    private final int VIEW_TYPE_DATE = 4;
    private final int VIEW_TYPE_ITEM_SENDER_NOT_DELIEVERED = 5;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading = false;
    private Activity activity;
    private List<ChatMessage> chatsList;
    private int visibleThreshold = 5;
    private int lastVisibleItem, firstVisibleItem, totalItemCount;
    private String userId2;

    public ChatAdapterAddMore(RecyclerView recyclerView, List<ChatMessage> chatsList, String userId2) {
        this.chatsList = chatsList;
        this.userId2 = userId2;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                //lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                //firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                //Log.d(TAG, "lastVisibleItem is " + lastVisibleItem);
                //Log.d(TAG, "totalItemCount is " + totalItemCount);
                //if (!isLoading && (firstVisibleItem == 0)) {
                if (!isLoading && ((totalItemCount - 1) <= lastVisibleItem)) {
                //if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    public void setLoaded() {isLoading = false;}

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = chatsList.get(position);
        if (chatMessage == null) {return VIEW_TYPE_LOADING;}
        else if (chatMessage.getSenderId().equals("")) {return VIEW_TYPE_DATE;}
        else if (chatMessage.getSenderId().equals(userId2)) {
            return VIEW_TYPE_ITEM_RECIEVER;
        }
        else {
            if (chatMessage.getIsDelieveredToFirebase() == 1) {
                if (chatMessage.getIsDelieveredToUser() == 1) {return VIEW_TYPE_ITEM_SENDER_DELIEVERED_TO_USER;}
                else {return VIEW_TYPE_ITEM_SENDER;}
            }
            else {return VIEW_TYPE_ITEM_SENDER_NOT_DELIEVERED;}
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM_SENDER) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message1, parent, false);
            return new ChatViewHolder(itemView);
        }
        if (viewType == VIEW_TYPE_ITEM_SENDER_DELIEVERED_TO_USER) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message1_delievered_to_user, parent, false);
            return new ChatViewHolder(itemView);
        }
        else if (viewType == VIEW_TYPE_ITEM_RECIEVER) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
            return new ChatViewHolder(itemView);
        }
        else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        else if (viewType == VIEW_TYPE_DATE) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
            return new DateViewHolder(itemView);
        }
        else if (viewType == VIEW_TYPE_ITEM_SENDER_NOT_DELIEVERED) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_not_delievered, parent, false);
            return new ChatViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatViewHolder) {
            ChatMessage chatMessage = chatsList.get(position);
            ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
            chatViewHolder.messageText.setText(chatMessage.getMessageText());
            chatViewHolder.messageTime.setText(android.text.format.DateFormat.format("HH:mm", chatMessage.getMessageTime()));
        }
        else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
        else if (holder instanceof DateViewHolder) {
            ChatMessage chatMessage = chatsList.get(position);
            DateViewHolder dateViewHolder = (DateViewHolder) holder;
            dateViewHolder.date.setText(android.text.format.DateFormat.format("d MMMM, yyyy", chatMessage.getMessageTime()));
        }
    }

    @Override
    public int getItemCount() {
        return chatsList == null ? 0 : chatsList.size();
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, messageUser, messageTime;

        public ChatViewHolder(View view) {
            super(view);
            messageText = (TextView)view.findViewById(R.id.message_text);
            messageTime = (TextView)view.findViewById(R.id.message_time);
        }
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        public TextView date;

        public DateViewHolder(View view) {
            super(view);
            date = (TextView)view.findViewById(R.id.item_date_text_view);
        }
    }
}
