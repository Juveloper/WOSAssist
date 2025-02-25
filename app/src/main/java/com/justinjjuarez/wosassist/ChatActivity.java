package com.justinjjuarez.wosassist;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText messageEditText;
    private Button sendButton;
    private FirebaseFirestore db;
    private String chatId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerView);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht angemeldet!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = user.getUid();

        // Auftrag-ID und Ersteller-ID aus Intent erhalten
        String itemId = getIntent().getStringExtra("ITEM_ID");
        String ownerId = getIntent().getStringExtra("OWNER_ID");

        // Chat-ID berechnen (immer gleiche ID für gleiche Nutzer)
        chatId = generateChatId(ownerId, userId);
        loadMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private String generateChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void loadMessages() {
        db.collection("Chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    messageList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Message message = doc.toObject(Message.class);
                        messageList.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", userId);
        message.put("message", messageText);
        message.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(docRef -> messageEditText.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler beim Senden!", Toast.LENGTH_SHORT).show());
    }
}
