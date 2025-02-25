package com.justinjjuarez.wosassist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Message> messageList;
    private String currentUserId;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(viewType == 1 ? R.layout.item_message_sent : R.layout.item_message_received, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text);
        }
    }
}
