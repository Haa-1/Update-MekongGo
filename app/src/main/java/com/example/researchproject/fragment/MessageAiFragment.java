package com.example.researchproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.researchproject.R;

import com.example.researchproject.Chat.ChatAdapter;
import com.example.researchproject.Chat.ChatMessage;
import com.example.researchproject.Post.Post;
import com.example.researchproject.Post.PostAdapterGrid;
import com.example.researchproject.Post.PostDetailActivity;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class MessageAiFragment extends Fragment {
    private RecyclerView recyclerViewChat;
    private TextView txtSuggestion;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ImageButton btnSearchAI;
    private EditText edtUserQuery;
    private DatabaseReference databaseReference;
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    private List<Post> filteredFirebaseData = new ArrayList<>();
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-8b-001:generateContent?key=YOUR_API_KEY";

    public MessageAiFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_ai, container, false);

        // 🎯 Ánh xạ View
        btnSearchAI = view.findViewById(R.id.btnSearchAI);
        edtUserQuery = view.findViewById(R.id.edtUserQuery);
        gridView = view.findViewById(R.id.gridView);
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        txtSuggestion = view.findViewById(R.id.txtSuggestion);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(requireContext(), chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewChat.setAdapter(chatAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(requireContext(), filteredFirebaseData);
        gridView.setAdapter(postAdapter);


        // 🎯 Xử lý nhấn vào từng item trong GridView
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Post selectedPost = filteredFirebaseData.get(position);
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra("postId", selectedPost.getPostId());
            intent.putExtra("title", selectedPost.getTitle());
            intent.putExtra("serviceInfo", selectedPost.getServiceInfo());
            intent.putExtra("price", selectedPost.getPrice());
            intent.putExtra("rentalTime", selectedPost.getRentalTime());
            intent.putExtra("address", selectedPost.getAddress());
            intent.putExtra("contact", selectedPost.getContact());
            intent.putExtra("imageUrl", selectedPost.getImageUrl());
            startActivity(intent);
        });

        // 🎯 Xử lý nút tìm kiếm
        btnSearchAI.setOnClickListener(v -> sendUserMessage());

        return view;
    }

    // ✅ Hàm lấy từ khóa trong danh sách gợi ý
    private List<String> extractKeywords(String input, List<String> suggestions) {
        List<String> keywords = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (input.contains(suggestion)) {
                keywords.add(suggestion);
            }
        }
        return keywords;
    }

    // 🔥 Firebase Query (Tìm kiếm theo từ khóa)
    private void fetchFilteredFirebase(String keyword) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filteredFirebaseData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null && post.getTitle().contains(keyword)) {
                        filteredFirebaseData.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                requireActivity().runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("❌ Lỗi hệ thống: " + error.getMessage(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                });
            }
        });
    }

    // 🤖 Gửi yêu cầu đến Gemini API
    private void sendRequestToGemini(String userMessage) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject requestBody = new JSONObject();
        try {
            JSONArray partsArray = new JSONArray();
            JSONObject textObject = new JSONObject();
            textObject.put("text", userMessage);
            partsArray.put(textObject);
            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            userContent.put("parts", partsArray);
            JSONArray contentsArray = new JSONArray();
            contentsArray.put(userContent);
            requestBody.put("contents", contentsArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Gemini API", "Lỗi kết nối: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("❌ Lỗi Meko AI.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.isSuccessful() && response.body() != null
                        ? response.body().string() : "Lỗi hệ thống.";
                requireActivity().runOnUiThread(() -> chatMessages.add(new ChatMessage(responseText, false)));
            }
        });
    }

    private void sendUserMessage() {
        String userMessage = edtUserQuery.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            chatMessages.add(new ChatMessage(userMessage, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            edtUserQuery.setText("");
            sendRequestToGemini(userMessage);
        }
    }
}
