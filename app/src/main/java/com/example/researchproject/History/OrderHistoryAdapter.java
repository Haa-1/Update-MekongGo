package com.example.researchproject.History;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.researchproject.Payment.Order.OrderItem;
import com.example.researchproject.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    private Context context;
    private List<OrderHistoryDisplay> orderHistoryList;
    private OnOrderClickListener listener;


    public OrderHistoryAdapter(Context context, List<OrderHistoryDisplay> orderHistoryList, OnOrderClickListener listener) {
        this.context = context;
        this.orderHistoryList = orderHistoryList;
        this.listener = listener;
    }
    public interface OnOrderClickListener {
        void onOrderClick(String orderId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderHistoryDisplay order = orderHistoryList.get(position);

        holder.txtTitle.setText(order.getTitle());
        holder.txtRentalPeriod.setText("Thời gian thuê: " + order.getRentalPeriod() + " ngày");
        holder.txtQuantity.setText("Số lượng: " + order.getQuantity());
        holder.txtTotalPrice.setText("Tổng tiền: " + order.getTotalPrice() + " VND");

        Glide.with(context)
                .load(order.getImageUrl())
                .placeholder(R.drawable.search_icon)
                .error(R.drawable.search_icon)
                .into(holder.imgCar);
        holder.btnDetail.setOnClickListener(v -> {
            Log.d("OrderHistoryAdapter", "Item clicked: " + order.getOrderId());
            listener.onOrderClick(order.getOrderId());
        });
    }


    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCar;
        TextView txtTitle, txtRentalPeriod, txtQuantity, txtTotalPrice;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgService);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtRentalPeriod = itemView.findViewById(R.id.txtRentalPeriod);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}