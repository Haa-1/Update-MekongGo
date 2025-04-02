package com.example.researchproject.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.textViewEmail.setText(user.getEmail());
        holder.textViewRole.setText(user.getRole());

        holder.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("uid", user.getUid());
            context.startActivity(intent);

        });

        holder.btnOrder.setOnClickListener(v -> {
            Intent intent = new Intent(context, ManageOrderActivity.class);
            intent.putExtra("email", user.getEmail());
            intent.putExtra("uid", user.getUid());
            intent.putExtra("role", user.getRole());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEmail, textViewRole;
        Button btnProfile, btnOrder;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEmail = itemView.findViewById(R.id.txtEmail);
            textViewRole = itemView.findViewById(R.id.txtRole);
            btnProfile = itemView.findViewById(R.id.btnProfile);
            btnOrder = itemView.findViewById(R.id.btnManageOrder);
        }
    }
}