package com.example.rpgnexus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rpgnexus.activities.LoginActivity;
import com.example.rpgnexus.activities.SheetDetailActivity;
import com.example.rpgnexus.activities.SheetFormActivity;
import com.example.rpgnexus.adapters.SheetAdapter;
import com.example.rpgnexus.databinding.FragmentMySheetsBinding;
import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.repository.AppRepository;

import java.util.List;

public class MySheetsFragment extends Fragment implements SheetAdapter.OnSheetClickListener {
    private FragmentMySheetsBinding binding;
    private AppRepository repository;
    private SheetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMySheetsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        setupRecyclerView();

        binding.fabCreateSheet.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SheetFormActivity.class);
            startActivity(intent);
        });

        binding.btnPromptLogin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMySheets();
    }

    private void setupRecyclerView() {
        adapter = new SheetAdapter(requireContext(), this);
        binding.rvMySheets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMySheets.setAdapter(adapter);
    }

    private void loadMySheets() {
        if (repository.isGuest()) {
            binding.layoutGuestPrompt.setVisibility(View.VISIBLE);
            binding.rvMySheets.setVisibility(View.GONE);
            binding.tvEmptyMySheets.setVisibility(View.GONE);
            binding.fabCreateSheet.setVisibility(View.GONE);
            return;
        }

        binding.layoutGuestPrompt.setVisibility(View.GONE);
        binding.fabCreateSheet.setVisibility(View.VISIBLE);

        List<CharacterSheet> list = repository.getUserSheets();
        adapter.setSheets(list);

        if (list.isEmpty()) {
            binding.tvEmptyMySheets.setVisibility(View.VISIBLE);
            binding.rvMySheets.setVisibility(View.GONE);
        } else {
            binding.tvEmptyMySheets.setVisibility(View.GONE);
            binding.rvMySheets.setVisibility(View.VISIBLE);
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
