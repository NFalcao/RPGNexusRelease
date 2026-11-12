package com.example.rpgnexus.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpgnexus.databinding.ItemUserCardBinding;
import com.example.rpgnexus.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onToggleBlock(User user);
        void onDeleteUser(User user);
    }

    private final Context context;
    private final List<User> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public UserAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setUsers(List<User> newUsers) {
        this.users.clear();
        if (newUsers != null) {
            this.users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserCardBinding binding = ItemUserCardBinding.inflate(LayoutInflater.from(context), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserCardBinding binding;

        public UserViewHolder(@NonNull ItemUserCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.tvUserDisplayName.setText(user.getDisplayName() + (user.isAdmin() ? " (ADMIN)" : ""));
            binding.tvUserEmail.setText(user.getEmail());

            if (user.isBlocked()) {
                binding.tvUserStatus.setText("BLOQUEADO");
                binding.tvUserStatus.setTextColor(Color.parseColor("#E57373"));
                binding.tvUserStatus.setBackgroundColor(Color.parseColor("#3E1A1A"));
                binding.btnToggleBlock.setText("Desbloquear");
            } else {
                binding.tvUserStatus.setText("ATIVO");
                binding.tvUserStatus.setTextColor(Color.parseColor("#81C784"));
                binding.tvUserStatus.setBackgroundColor(Color.parseColor("#1B381E"));
                binding.btnToggleBlock.setText("Bloquear");
            }

            binding.btnToggleBlock.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleBlock(user);
                }
            });

            binding.btnDeleteUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });
        }
    }
}
