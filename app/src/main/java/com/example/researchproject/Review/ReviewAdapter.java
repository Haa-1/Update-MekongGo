package com.example.researchproject.Review;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.researchproject.R;
import java.util.List;
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Review> reviewList;
    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
//        holder.txtUser.setText(review.getUser());
        holder.txtComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());
    }
    @Override
    public int getItemCount() {
        return reviewList.size();
    }
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtComment;
        RatingBar ratingBar;
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
//            txtUser = itemView.findViewById(R.id.txtUser);
            txtComment = itemView.findViewById(R.id.txtComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}