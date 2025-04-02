package com.example.researchproject.mekoaipro;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.R;
import com.example.researchproject.mekoaipro.ChatAdapterPro;
import com.example.researchproject.mekoaipro.ChatMessagePro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.*;
public class MekoAIPro extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapterPro chatAdapter;
    private List<ChatMessagePro> chatMessages;
    private EditText inputMessage;
    private ImageButton sendButton;
    private final OkHttpClient client = new OkHttpClient();
    private static final String OPENAI_API_KEY = "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";  // Thay bằng API Key mới
    // Firebase
    private DatabaseReference chatHistoryRef;
    private String uid;
    //  private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meko_aipro);
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapterPro(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        // Lấy UID của người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            chatHistoryRef = FirebaseDatabase.getInstance().getReference("chat_history").child(uid);
            loadChatHistory();
        } else {
            uid = null;
        }
        sendButton.setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                detectUserIntent(messageText);  // Gọi detectUserIntent tại đây
                sendMessage(messageText);
                inputMessage.setText("");
            }
        });
    }
    //    // Phương thức phân loại ý định và xử lý tin nhắn
//    private void detectAndProcessUserIntent(String userMessage) {
//        FirebaseModelInterpreter interpreter = ...; // Khởi tạo trình biên dịch mô hình Firebase ML
//        FirebaseModelInputOutputOptions inputOutputOptions = ...; // Cấu hình đầu vào và đầu ra của mô hình
//
//        // Chuẩn bị và chạy mô hình phân loại ý định
//        interpreter.run(inputData, inputOutputOptions)
//                .addOnSuccessListener(result -> {
//                    String intentType = getIntentTypeFromResult(result); // Xác định loại ý định từ kết quả mô hình
//
//                    if (intentType.equals("simple_question")) {
//                        // Xử lý câu hỏi đơn giản từ Firebase
//                        String simpleAnswer = getSavedAnswer(userMessage);
//                        displayMessage(simpleAnswer);
//                    } else if (intentType.equals("complex_question")) {
//                        // Xử lý câu hỏi phức tạp với dữ liệu bổ sung từ Firebase
//                        String firebaseData = getAdditionalDataFromFirebase();
//                        sendToOpenAI(userMessage, firebaseData);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    displayMessage("Lỗi phân loại ý định: " + e.getLocalizedMessage());
//                });
//    }
//
//    // Gửi câu hỏi phức tạp đến OpenAI API với dữ liệu từ Firebase
//    private void sendToOpenAI(String userMessage, String firebaseData) {
//        String combinedMessage = userMessage + "\nDữ liệu bổ sung từ Firebase:\n" + firebaseData;
//        getResponseFromOpenAI(combinedMessage);
//    }
//
//    // Lấy câu trả lời đã lưu từ Firebase cho câu hỏi đơn giản
//    private String getSavedAnswer(String userMessage) {
//        // Lấy câu trả lời từ cơ sở dữ liệu Firebase
//        return "Đây là câu trả lời đơn giản cho: " + userMessage;
//    }
//
//    // Lấy dữ liệu bổ sung từ Firebase (ví dụ: thông tin cấu trúc hoặc ngữ cảnh)
//    private String getAdditionalDataFromFirebase() {
//        return "Dữ liệu bổ sung từ Firebase để cải thiện phản hồi.";
//    }
//
//    // Phương thức hiển thị tin nhắn trên giao diện
//    private void displayMessage(String messageText) {
//        ChatMessagePro message = new ChatMessagePro(messageText, false); // False nghĩa là tin nhắn từ bot
//        chatMessages.add(message);
//        chatAdapter.notifyDataSetChanged();
//        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
//    }
    private void sendMessage(String messageText) {
        ChatMessagePro userMessage = new ChatMessagePro(messageText, true);
        chatMessages.add(userMessage);
        chatAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        // Lưu tin nhắn của user vào Firebase
        saveMessageToFirebase(messageText, "user");
        // Gửi tin nhắn đến OpenAI
        getResponseFromOpenAI(messageText);
    }
    private void getResponseFromOpenAI(String question) {
        JSONObject jsonObject = new JSONObject();
        try {
            // Xây dựng danh sách các tin nhắn
            JSONArray messages = new JSONArray();
            // Thêm tất cả các tin nhắn trước đó
            for (ChatMessagePro chatMessage : chatMessages) {
                JSONObject message = new JSONObject();
                message.put("role", chatMessage.isUser() ? "user" : "assistant");
                message.put("content", chatMessage.getMessage());
                messages.put(message);
            }
            // Thêm tin nhắn hiện tại của người dùng
            JSONObject currentMessage = new JSONObject();
            currentMessage.put("role", "user");
            currentMessage.put("content", question);
            messages.put(currentMessage);
            // Gửi toàn bộ đoạn hội thoại tới API
            jsonObject.put("model", "ft:gpt-3.5-turbo-0125:mekonggo::BD33VglX");
            jsonObject.put("messages", messages);
            jsonObject.put("max_tokens", 150); // Tăng token nếu cần phản hồi dài hơn
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Gửi request API OpenAI (phần này giữ nguyên)
        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        // Xử lý phản hồi từ API OpenAI (giữ nguyên logic hiện tại)
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessagePro("Lỗi mạng: " + e.getMessage(), false));
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        final String answer = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        runOnUiThread(() -> {
                            ChatMessagePro botMessage = new ChatMessagePro(answer.trim(), false);
                            chatMessages.add(botMessage);
                            chatAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                            // Lưu phản hồi bot vào Firebase
                            saveMessageToFirebase(answer.trim(), "bot");
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessagePro("Lỗi xử lý phản hồi: " + e.getMessage(), false));
                            chatAdapter.notifyDataSetChanged();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessagePro("Lỗi API: " + response.message(), false));
                        chatAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
    private void saveMessageToFirebase(String message, String sender) {
        if (uid == null) return;
        DatabaseReference newMessageRef = chatHistoryRef.push();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        messageData.put("sender", sender);
        messageData.put("timestamp", System.currentTimeMillis());
        newMessageRef.setValue(messageData);
    }
    private void detectUserIntent(String userMessage) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(userMessage)
                .addOnSuccessListener(languageCode -> {
                    if (languageCode.equals("und")) {
                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessagePro("Không thể nhận diện ngôn ngữ.", false));
                            chatAdapter.notifyDataSetChanged();
                        });
                        return;
                    }
                    if (!languageCode.equals("vi") && !languageCode.equals("en")) {
                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessagePro("Chỉ hỗ trợ tiếng Việt và tiếng Anh.", false));
                            chatAdapter.notifyDataSetChanged();
                        });
                        return;
                    }
                    // Nếu ngôn ngữ hợp lệ, gọi OpenAI API
                    getResponseFromOpenAI(userMessage);
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessagePro("Lỗi nhận diện: " + e.getLocalizedMessage(), false));
                        chatAdapter.notifyDataSetChanged();
                    });
                });
    }
    private void loadChatHistory() {
        chatHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String message = data.child("message").getValue(String.class);
                    String sender = data.child("sender").getValue(String.class);
                    boolean isUser = sender != null && sender.equals("user");
                    chatMessages.add(new ChatMessagePro(message, isUser));
                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MekoAIPro.this, "Lỗi tải lịch sử: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}