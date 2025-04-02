package com.example.researchproject.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.researchproject.Payment.Order.OrderInformationActivity;
import com.example.researchproject.Post.Post;
import com.example.researchproject.R;
import com.example.researchproject.Post.PostDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Post> cartList; // ✅ Sửa thành Post

    public CartAdapter(Context context, List<Post> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Post post = cartList.get(position);
        holder.txtTitle.setText(post.getTitle());
        holder.txtPrice.setText("Giá: " + post.getPrice() + " VND");

        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.search_icon)
                .error(R.drawable.search_icon)
                .into(holder.imgService);

        // ✅ Nhấn vào item → chuyển sang PostDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("title", post.getTitle());
            intent.putExtra("serviceInfo", post.getServiceInfo());
            intent.putExtra("price", post.getPrice());
            intent.putExtra("rentalTime", post.getRentalTime());
            intent.putExtra("address", post.getAddress());
            intent.putExtra("contact", post.getContact());
            intent.putExtra("imageUrl", post.getImageUrl());
            context.startActivity(intent);
        });

        // ✅ Xử lý nút Xóa
        holder.btnDelete.setOnClickListener(v -> removeItem(holder, post));

        // ✅ Xử lý nút Thanh Toán
        holder.btnPay.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderInformationActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("title", post.getTitle());
            intent.putExtra("price", post.getPrice());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // ✅ Hàm xóa bài viết khỏi giỏ hàng
    private void removeItem(@NonNull CartViewHolder holder, Post post) {
        String postId = post.getPostId();
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(context, "Không tìm thấy Post ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart");
        cartRef.orderByChild("postId").equalTo(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                dataSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                    int updatedPosition = holder.getAdapterPosition();
                                    if (updatedPosition != RecyclerView.NO_POSITION) {
                                        cartList.remove(updatedPosition);
                                        notifyItemRemoved(updatedPosition);
                                        Toast.makeText(context, "Đã xóa khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
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
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtPrice;
        ImageView imgService;
        ImageButton btnDelete, btnPay;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgService = itemView.findViewById(R.id.imgService);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
}
