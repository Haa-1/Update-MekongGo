package com.example.researchproject.Review;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import java.util.List;
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);
        Log.d("DEBUG", "Binding Review: " + review.getRating() + " - " + review.getComment());
        holder.txtComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());

        // Kiểm tra nếu review có ảnh thì hiển thị, nếu không thì ẩn đi
        if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
            holder.img_review.setVisibility(View.VISIBLE);
            Glide.with(context).load(review.getImageUrl()).into(holder.img_review);
        } else {
            holder.img_review.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtComment;
        RatingBar ratingBar;
        ImageView img_review;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtComment = itemView.findViewById(R.id.txtComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            img_review = itemView.findViewById(R.id.img_review);
        }
    }
}