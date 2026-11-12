package com.example.rpgnexus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rpgnexus.activities.SheetDetailActivity;
import com.example.rpgnexus.adapters.SheetAdapter;
import com.example.rpgnexus.databinding.FragmentPublicSheetsBinding;
import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.repository.AppRepository;

import java.util.List;

public class PublicSheetsFragment extends Fragment implements SheetAdapter.OnSheetClickListener {
    private FragmentPublicSheetsBinding binding;
    private AppRepository repository;
    private SheetAdapter adapter;

    private String currentQuery = "";
    private String selectedSystem = "Todos";
    private String selectedGender = "Todos";
    private String selectedStyle = "Todos";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPublicSheetsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        setupRecyclerView();
        setupFilters();
        setupSearch();

        loadSheets();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSheets();
    }

    private void setupRecyclerView() {
        adapter = new SheetAdapter(requireContext(), this);
        binding.rvPublicSheets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPublicSheets.setAdapter(adapter);
    }

    private void setupFilters() {
        String[] systems = {"Todos", "Dungeons & Dragons 5e", "Tormenta20", "Call of Cthulhu 7e", "Ordem Paranormal", "Vampiro: A Máscara", "Pathfinder 2e", "3D&T Alpha", "Outro"};
        String[] styles = {"Todos", "Combate / Tanque", "Mágico / Dano em Área", "Investigação / Mistério", "Suporte / Cura", "Furtivo / Dano Único", "Social / Utilitário", "Outro"};
        String[] genders = {"Todos", "Masculino", "Feminino", "Outro"};

        ArrayAdapter<String> sysAdapter = new ArrayAdapter<>(requireContext(), com.example.rpgnexus.R.layout.item_dropdown_custom, systems);
        ArrayAdapter<String> styAdapter = new ArrayAdapter<>(requireContext(), com.example.rpgnexus.R.layout.item_dropdown_custom, styles);
        ArrayAdapter<String> genAdapter = new ArrayAdapter<>(requireContext(), com.example.rpgnexus.R.layout.item_dropdown_custom, genders);

        binding.autoCompleteSystemFilter.setAdapter(sysAdapter);
        binding.autoCompleteStyleFilter.setAdapter(styAdapter);
        binding.autoCompleteGenderFilter.setAdapter(genAdapter);

        binding.autoCompleteSystemFilter.setText("Todos", false);
        binding.autoCompleteStyleFilter.setText("Todos", false);
        binding.autoCompleteGenderFilter.setText("Todos", false);

        binding.autoCompleteSystemFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedSystem = parent.getItemAtPosition(position).toString();
            loadSheets();
        });

        binding.autoCompleteStyleFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStyle = parent.getItemAtPosition(position).toString();
            loadSheets();
        });

        binding.autoCompleteGenderFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedGender = parent.getItemAtPosition(position).toString();
            loadSheets();
        });
    }

    private void setupSearch() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                loadSheets();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSheets() {
        List<CharacterSheet> list = repository.getPublicSheets(currentQuery, selectedSystem, selectedGender, selectedStyle);
        adapter.setSheets(list);

        if (list.isEmpty()) {
            binding.tvEmptyPublicSheets.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyPublicSheets.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSheetClick(CharacterSheet sheet) {
        Intent intent = new Intent(requireContext(), SheetDetailActivity.class);
        intent.putExtra("character_sheet", sheet);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
