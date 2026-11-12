package com.example.rpgnexus.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rpgnexus.R;
import com.example.rpgnexus.databinding.ItemSheetCardBinding;
import com.example.rpgnexus.models.CharacterSheet;

import java.util.ArrayList;
import java.util.List;

public class SheetAdapter extends RecyclerView.Adapter<SheetAdapter.SheetViewHolder> {

    public interface OnSheetClickListener {
        void onSheetClick(CharacterSheet sheet);
    }

    private final Context context;
    private final List<CharacterSheet> sheets = new ArrayList<>();
    private final OnSheetClickListener listener;

    public SheetAdapter(Context context, OnSheetClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setSheets(List<CharacterSheet> newSheets) {
        this.sheets.clear();
        if (newSheets != null) {
            this.sheets.addAll(newSheets);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSheetCardBinding binding = ItemSheetCardBinding.inflate(LayoutInflater.from(context), parent, false);
        return new SheetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SheetViewHolder holder, int position) {
        CharacterSheet sheet = sheets.get(position);
        holder.bind(sheet);
    }

    @Override
    public int getItemCount() {
        return sheets.size();
    }

    class SheetViewHolder extends RecyclerView.ViewHolder {
        private final ItemSheetCardBinding binding;

        public SheetViewHolder(@NonNull ItemSheetCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CharacterSheet sheet) {
            binding.tvSheetCharName.setText(sheet.getCharacterName());
            binding.tvSheetRpgSystem.setText(sheet.getRpgSystem());
            binding.tvSheetOwner.setText("Criado por: " + (sheet.getOwnerName() != null ? sheet.getOwnerName() : "Desconhecido"));

            if (sheet.getGender() != null && !sheet.getGender().isEmpty()) {
                binding.tvSheetGender.setVisibility(View.VISIBLE);
                binding.tvSheetGender.setText(sheet.getGender());
            } else {
                binding.tvSheetGender.setVisibility(View.GONE);
            }

            if (sheet.getPlayStyle() != null && !sheet.getPlayStyle().isEmpty()) {
                binding.tvSheetStyle.setVisibility(View.VISIBLE);
                binding.tvSheetStyle.setText(sheet.getPlayStyle());
            } else {
                binding.tvSheetStyle.setVisibility(View.GONE);
            }

            if (sheet.isPublic()) {
                binding.tvSheetPrivacy.setText("Pública");
                binding.tvSheetPrivacy.setTextColor(Color.parseColor("#81C784"));
                binding.tvSheetPrivacy.setBackgroundColor(Color.parseColor("#1B381E"));
            } else {
                binding.tvSheetPrivacy.setText("Privada");
                binding.tvSheetPrivacy.setTextColor(Color.parseColor("#E57373"));
                binding.tvSheetPrivacy.setBackgroundColor(Color.parseColor("#3E1A1A"));
            }

            if (sheet.getImageUrl() != null && !sheet.getImageUrl().trim().isEmpty()) {
                com.example.rpgnexus.helpers.ImageLoaderHelper.loadImage(context, sheet.getImageUrl(), binding.ivSheetPortrait);
            } else {
                binding.ivSheetPortrait.setImageResource(R.drawable.ic_nexus_logo);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSheetClick(sheet);
                }
            });
        }
    }
}
