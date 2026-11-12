package com.example.rpgnexus.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rpgnexus.R;
import com.example.rpgnexus.databinding.ActivitySheetDetailBinding;
import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.models.User;
import com.example.rpgnexus.repository.AppRepository;

public class SheetDetailActivity extends AppCompatActivity {
    private ActivitySheetDetailBinding binding;
    private AppRepository repository;
    private CharacterSheet sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySheetDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        if (getIntent().hasExtra("character_sheet")) {
            sheet = (CharacterSheet) getIntent().getSerializableExtra("character_sheet");
        }

        if (sheet == null) {
            Toast.makeText(this, "Erro ao carregar os dados da ficha.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displaySheetDetails();
        checkActionsPermissions();

        binding.btnEditSheet.setOnClickListener(v -> {
            Intent intent = new Intent(this, SheetFormActivity.class);
            intent.putExtra("character_sheet", sheet);
            startActivity(intent);
            finish();
        });

        binding.btnDeleteSheet.setOnClickListener(v -> confirmDeleteSheet());
    }

    private void displaySheetDetails() {
        binding.tvDetailCharName.setText(sheet.getCharacterName());
        binding.tvDetailRpgSystem.setText(sheet.getRpgSystem());
        binding.tvDetailOwner.setText("Criado por: " + (sheet.getOwnerName() != null ? sheet.getOwnerName() : "Desconhecido"));

        binding.tvDetailGender.setText("Gênero: " + (sheet.getGender() != null && !sheet.getGender().isEmpty() ? sheet.getGender() : "-"));
        binding.tvDetailPlayStyle.setText("Estilo: " + (sheet.getPlayStyle() != null && !sheet.getPlayStyle().isEmpty() ? sheet.getPlayStyle() : "-"));

        if (sheet.isPublic()) {
            binding.tvDetailPrivacy.setText("Pública");
            binding.tvDetailPrivacy.setTextColor(Color.parseColor("#81C784"));
            binding.tvDetailPrivacy.setBackgroundColor(Color.parseColor("#1B381E"));
        } else {
            binding.tvDetailPrivacy.setText("Privada");
            binding.tvDetailPrivacy.setTextColor(Color.parseColor("#E57373"));
            binding.tvDetailPrivacy.setBackgroundColor(Color.parseColor("#3E1A1A"));
        }

        binding.tvDetailResources.setText(hasText(sheet.getResources()) ? sheet.getResources() : "Nenhum recurso informado.");
        binding.tvDetailAttributes.setText(hasText(sheet.getAttributes()) ? sheet.getAttributes() : "Nenhum atributo informado.");
        binding.tvDetailSkills.setText(hasText(sheet.getSkills()) ? sheet.getSkills() : "Nenhuma habilidade/perícia informada.");
        binding.tvDetailEquipment.setText(hasText(sheet.getEquipment()) ? sheet.getEquipment() : "Nenhum equipamento informado.");
        binding.tvDetailNotes.setText(hasText(sheet.getNotes()) ? sheet.getNotes() : "Nenhuma anotação informada.");

        if (sheet.getImageUrl() != null && !sheet.getImageUrl().trim().isEmpty()) {
            com.example.rpgnexus.helpers.ImageLoaderHelper.loadImage(this, sheet.getImageUrl(), binding.ivDetailPortrait);
        } else {
            binding.ivDetailPortrait.setImageResource(R.drawable.ic_nexus_logo);
        }
    }

    private void checkActionsPermissions() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            // RN001: Owner can edit/delete, RN006: Admin can moderate
            boolean isOwner = currentUser.getId().equals(sheet.getOwnerId());
            boolean isAdmin = currentUser.isAdmin();

            if (isOwner || isAdmin) {
                binding.layoutOwnerActions.setVisibility(View.VISIBLE);
            } else {
                binding.layoutOwnerActions.setVisibility(View.GONE);
            }
        } else {
            binding.layoutOwnerActions.setVisibility(View.GONE);
        }
    }

    private void confirmDeleteSheet() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Excluir Ficha")
                .setMessage("Tem certeza que deseja excluir a ficha de " + sheet.getCharacterName() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean deleted = repository.deleteSheet(sheet.getId());
                    if (deleted) {
                        Toast.makeText(this, "Ficha excluída com sucesso.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao excluir a ficha.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
