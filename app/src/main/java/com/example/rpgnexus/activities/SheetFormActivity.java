package com.example.rpgnexus.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rpgnexus.R;
import com.example.rpgnexus.databinding.ActivitySheetFormBinding;
import com.example.rpgnexus.helpers.ImageLoaderHelper;
import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.models.User;
import com.example.rpgnexus.repository.AppRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class SheetFormActivity extends AppCompatActivity {
    private static final String TAG = "SheetFormActivity";
    private ActivitySheetFormBinding binding;
    private AppRepository repository;
    private CharacterSheet existingSheet;

    private String selectedBase64Image = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private static final String[] SYSTEMS = {
            "Dungeons & Dragons 5e", "Tormenta20", "Call of Cthulhu 7e",
            "Ordem Paranormal", "Vampiro: A Máscara", "Pathfinder 2e",
            "3D&T Alpha", "Outro"
    };

    private static final String[] STYLES = {
            "Combate / Tanque", "Mágico / Dano em Área", "Investigação / Mistério",
            "Suporte / Cura", "Furtivo / Dano Único", "Social / Utilitário", "Outro"
    };

    private static final String[] GENDERS = {
            "Masculino", "Feminino", "Outro"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySheetFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {}

                        String base64Image = encodeUriToBase64(uri);
                        if (base64Image != null) {
                            this.selectedBase64Image = base64Image;
                            binding.etImageUrl.setText("[Imagem da Galeria Selecionada]");
                            ImageLoaderHelper.loadImage(SheetFormActivity.this, base64Image, binding.ivFormImagePreview);
                        } else {
                            this.selectedBase64Image = null;
                            binding.etImageUrl.setText(uri.toString());
                        }
                    }
                }
        );

        binding.btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        setupDropdowns();

        if (getIntent().hasExtra("character_sheet")) {
            existingSheet = (CharacterSheet) getIntent().getSerializableExtra("character_sheet");
            populateFormForEdit();
        }

        binding.btnSaveSheet.setOnClickListener(v -> saveSheet());
    }

    private String encodeUriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (originalBitmap == null) return null;

            int maxSize = 180;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
            int newWidth = Math.max(1, Math.round(width * ratio));
            int newHeight = Math.max(1, Math.round(height * ratio));

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();

            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao comprimir imagem da galeria para Base64", e);
            return null;
        }
    }

    private void setupDropdowns() {
        ArrayAdapter<String> sysAdapter = new ArrayAdapter<>(this, com.example.rpgnexus.R.layout.item_dropdown_custom, SYSTEMS);
        ArrayAdapter<String> styAdapter = new ArrayAdapter<>(this, com.example.rpgnexus.R.layout.item_dropdown_custom, STYLES);
        ArrayAdapter<String> genAdapter = new ArrayAdapter<>(this, com.example.rpgnexus.R.layout.item_dropdown_custom, GENDERS);

        binding.autoCompleteRpgSystem.setAdapter(sysAdapter);
        binding.autoCompletePlayStyle.setAdapter(styAdapter);
        binding.autoCompleteGender.setAdapter(genAdapter);

        binding.autoCompleteRpgSystem.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            if ("Outro".equalsIgnoreCase(selected)) {
                binding.tilCustomRpgSystem.setVisibility(View.VISIBLE);
            } else {
                binding.tilCustomRpgSystem.setVisibility(View.GONE);
            }
        });

        binding.autoCompleteGender.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            if ("Outro".equalsIgnoreCase(selected)) {
                binding.tilCustomGender.setVisibility(View.VISIBLE);
            } else {
                binding.tilCustomGender.setVisibility(View.GONE);
            }
        });
    }

    private void populateFormForEdit() {
        if (existingSheet == null) return;
        binding.tvFormTitle.setText("Editar Ficha de Personagem");
        binding.etCharName.setText(existingSheet.getCharacterName());

        // System
        String sys = existingSheet.getRpgSystem();
        List<String> sysList = Arrays.asList(SYSTEMS);
        if (sysList.contains(sys)) {
            binding.autoCompleteRpgSystem.setText(sys, false);
            binding.tilCustomRpgSystem.setVisibility(View.GONE);
        } else {
            binding.autoCompleteRpgSystem.setText("Outro", false);
            binding.tilCustomRpgSystem.setVisibility(View.VISIBLE);
            binding.etCustomRpgSystem.setText(sys);
        }

        // Gender
        String gen = existingSheet.getGender();
        List<String> genList = Arrays.asList(GENDERS);
        if (genList.contains(gen)) {
            binding.autoCompleteGender.setText(gen, false);
            binding.tilCustomGender.setVisibility(View.GONE);
        } else {
            binding.autoCompleteGender.setText("Outro", false);
            binding.tilCustomGender.setVisibility(View.VISIBLE);
            binding.etCustomGender.setText(gen);
        }

        // Play Style
        String sty = existingSheet.getPlayStyle();
        List<String> styList = Arrays.asList(STYLES);
        if (styList.contains(sty)) {
            binding.autoCompletePlayStyle.setText(sty, false);
        } else if (sty != null && !sty.isEmpty()) {
            binding.autoCompletePlayStyle.setText("Outro", false);
        }

        String imgUrl = existingSheet.getImageUrl();
        if (imgUrl != null && (imgUrl.startsWith("data:image") || imgUrl.contains("base64,"))) {
            selectedBase64Image = imgUrl;
            binding.etImageUrl.setText("[Imagem da Galeria Selecionada]");
            ImageLoaderHelper.loadImage(this, imgUrl, binding.ivFormImagePreview);
        } else {
            selectedBase64Image = null;
            binding.etImageUrl.setText(imgUrl != null ? imgUrl : "");
            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                ImageLoaderHelper.loadImage(this, imgUrl, binding.ivFormImagePreview);
            }
        }

        binding.switchPublic.setChecked(existingSheet.isPublic());
        binding.etResources.setText(existingSheet.getResources());
        binding.etAttributes.setText(existingSheet.getAttributes());
        binding.etSkills.setText(existingSheet.getSkills());
        binding.etEquipment.setText(existingSheet.getEquipment());
        binding.etNotes.setText(existingSheet.getNotes());
    }

    private void saveSheet() {
        String charName = binding.etCharName.getText() != null ? binding.etCharName.getText().toString().trim() : "";

        // Resolve RPG System
        String rpgSystem = binding.autoCompleteRpgSystem.getText().toString().trim();
        if ("Outro".equalsIgnoreCase(rpgSystem)) {
            rpgSystem = binding.etCustomRpgSystem.getText() != null ? binding.etCustomRpgSystem.getText().toString().trim() : "";
        }

        // Resolve Gender
        String gender = binding.autoCompleteGender.getText().toString().trim();
        if ("Outro".equalsIgnoreCase(gender)) {
            String customGen = binding.etCustomGender.getText() != null ? binding.etCustomGender.getText().toString().trim() : "";
            gender = customGen.isEmpty() ? "Outro" : customGen;
        }

        // Resolve Play Style
        String playStyle = binding.autoCompletePlayStyle.getText().toString().trim();

        // RN004 - Mandatory Fields Validation
        if (charName.isEmpty()) {
            binding.tilCharName.setError("Nome do personagem é obrigatório");
            return;
        } else {
            binding.tilCharName.setError(null);
        }

        if (rpgSystem.isEmpty()) {
            binding.tilRpgSystem.setError("Sistema de RPG é obrigatório");
            return;
        } else {
            binding.tilRpgSystem.setError(null);
        }

        User user = repository.getCurrentUser();
        if (user == null && existingSheet == null) {
            Toast.makeText(this, "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        CharacterSheet sheet = existingSheet != null ? existingSheet : new CharacterSheet();
        if (existingSheet == null) {
            sheet.setOwnerId(user.getId());
            sheet.setOwnerName(user.getDisplayName());
        }

        sheet.setCharacterName(charName);
        sheet.setRpgSystem(rpgSystem);
        sheet.setGender(gender);
        sheet.setPlayStyle(playStyle);

        String finalImageUrl = "";
        if (selectedBase64Image != null && !selectedBase64Image.isEmpty()) {
            finalImageUrl = selectedBase64Image;
        } else if (binding.etImageUrl.getText() != null) {
            String typed = binding.etImageUrl.getText().toString().trim();
            if (!"[Imagem da Galeria Selecionada]".equalsIgnoreCase(typed)) {
                finalImageUrl = typed;
            }
        }

        sheet.setImageUrl(finalImageUrl);
        sheet.setPublic(binding.switchPublic.isChecked());
        sheet.setResources(binding.etResources.getText() != null ? binding.etResources.getText().toString().trim() : "");
        sheet.setAttributes(binding.etAttributes.getText() != null ? binding.etAttributes.getText().toString().trim() : "");
        sheet.setSkills(binding.etSkills.getText() != null ? binding.etSkills.getText().toString().trim() : "");
        sheet.setEquipment(binding.etEquipment.getText() != null ? binding.etEquipment.getText().toString().trim() : "");
        sheet.setNotes(binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "");

        boolean success = repository.saveSheet(sheet);
        if (success) {
            Toast.makeText(this, existingSheet != null ? "Ficha atualizada!" : "Ficha criada com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao salvar ficha.", Toast.LENGTH_SHORT).show();
        }
    }
}
