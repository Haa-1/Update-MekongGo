package com.example.researchproject.admin;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.example.researchproject.Post.Post;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;
    public PostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        listenForDataChanges();
    }
    // Listen for data changes in Firebase
    private void listenForDataChanges() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                postList.add(snapshot.getValue(Post.class));
                notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Post updatedPost = snapshot.getValue(Post.class);
                for (int i = 0; i < postList.size(); i++) {
                    if (postList.get(i).getPostId().equals(updatedPost.getPostId())) {
                        postList.set(i, updatedPost);
                        notifyItemChanged(i);
                        break;
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Post removedPost = snapshot.getValue(Post.class);
                for (int i = 0; i < postList.size(); i++) {
                    if (postList.get(i).getPostId().equals(removedPost.getPostId())) {
                        postList.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle if necessary
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_admin, parent, false);
        return new PostViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.txtTitle.setText(post.getTitle());
        holder.txtPrice.setText("Giá: " + post.getPrice() + " VND");
        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.search_icon)
                .error(R.drawable.search_icon)
                .into(holder.imgService);
        // Xử lý nút Xóa
        holder.btnDeletePost.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= postList.size()) {
                Toast.makeText(context, "Vị trí không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            Post currentPost = postList.get(currentPosition);
            String postId = currentPost.getPostId();
            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "Không tìm thấy Post ID!", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Posts");
            cartRef.orderByChild("postId").equalTo(postId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    dataSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                        int updatedPosition = holder.getAdapterPosition();
                                        if (updatedPosition != RecyclerView.NO_POSITION && updatedPosition < postList.size()) {
                                            postList.remove(updatedPosition);
                                            notifyItemRemoved(updatedPosition);
                                            Toast.makeText(context, "Đã xóa khỏi MekongGo!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(context, "Lỗi khi xóa khỏi Firebase!", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                Toast.makeText(context, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        // Xử lý nút Sửa
        holder.btnEditPost.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= postList.size()) {
                Toast.makeText(context, "Vị trí không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            Post currentPost = postList.get(currentPosition);
            String postId = currentPost.getPostId();
            long timestamp = currentPost.getTimestamp();
            Intent intent = new Intent(context, EditPost.class);
            intent.putExtra("postId", postId);
            intent.putExtra("timestamp", timestamp);
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return postList.size();
    }
    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtPrice;
        ImageView imgService;
        ImageButton btnEditPost, btnDeletePost;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgService = itemView.findViewById(R.id.imgService);
            btnEditPost = itemView.findViewById(R.id.btnEditPost);
            btnDeletePost = itemView.findViewById(R.id.btnDeletePost);
        }
    }
}