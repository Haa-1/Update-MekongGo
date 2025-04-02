package com.example.researchproject.Ad;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import java.util.List;
public class AdSliderAdapter extends RecyclerView.Adapter<AdSliderAdapter.AdViewHolder> {
    private Context context;
    private List<Ad> adList;
    public AdSliderAdapter(Context context, List<Ad> adList) {
        this.context = context;
        this.adList = adList;
    }
    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ad_slider, parent, false);
        return new AdViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Ad ad = adList.get(position);
        if (ad != null) {
            // Load ảnh hoặc dữ liệu quảng cáo
            Glide.with(context).load(ad.getImageUrl()).into(holder.imgAd);
        }
        holder.txtAdTitle.setText(ad.getTitle());
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }
    static class AdViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAd;
        TextView txtAdTitle;
        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAd = itemView.findViewById(R.id.imgAd);
            txtAdTitle = itemView.findViewById(R.id.txtAdTitle);
        }
    }
}