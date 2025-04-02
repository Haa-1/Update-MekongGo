package com.example.researchproject.Payment.Order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.researchproject.R;

import java.util.List;

public class OrderItemAdapter  extends BaseAdapter {
    private Context context;
    private List<OrderItem> orderItems;
    private LayoutInflater inflater;
    private OnQuantityChangeListener quantityChangeListener;

    public OrderItemAdapter(Context context, List<OrderItem> orderItems, OnQuantityChangeListener listener) {
        this.context = context;
        this.orderItems = orderItems;
        this.inflater = LayoutInflater.from(context);
        this.quantityChangeListener = listener;
    }

    @Override
    public int getCount() {
        return orderItems.size();
    }

    @Override
    public Object getItem(int position) {
        return orderItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_payment, parent, false);
            holder = new ViewHolder();
            holder.txtProductName = convertView.findViewById(R.id.txtProductName);
            holder.txtPrice = convertView.findViewById(R.id.txtPrice);
            holder.editQuantity = convertView.findViewById(R.id.editQuantity);
            holder.btnDecrease = convertView.findViewById(R.id.btnDecrease);
            holder.btnIncrease = convertView.findViewById(R.id.btnIncrease);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Lấy thông tin sản phẩm
        OrderItem item = orderItems.get(position);
        holder.txtProductName.setText(item.getTitle());
        holder.txtPrice.setText(String.format("%.2f VNĐ", item.getPrice()));
        holder.editQuantity.setText(String.valueOf(item.getQuantity()));

        // Xử lý tăng giảm số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            holder.editQuantity.setText(String.valueOf(item.getQuantity()));
            quantityChangeListener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity()> 1) {
                item.setQuantity(item.getQuantity() - 1);
                holder.editQuantity.setText(item.getQuantity());
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView txtProductName, txtPrice;
        EditText editQuantity;
        Button btnDecrease, btnIncrease;
    }
    // Interface để cập nhật tổng tiền khi số lượng thay đổi
    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }
}
