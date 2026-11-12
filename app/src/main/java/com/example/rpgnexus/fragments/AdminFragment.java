package com.example.rpgnexus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rpgnexus.activities.SheetDetailActivity;
import com.example.rpgnexus.adapters.SheetAdapter;
import com.example.rpgnexus.adapters.UserAdapter;
import com.example.rpgnexus.databinding.FragmentAdminBinding;
import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.models.User;
import com.example.rpgnexus.repository.AppRepository;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class AdminFragment extends Fragment implements UserAdapter.OnUserActionListener, SheetAdapter.OnSheetClickListener {
    private FragmentAdminBinding binding;
    private AppRepository repository;

    private UserAdapter userAdapter;
    private SheetAdapter sheetAdapter;
    private boolean showingUsersTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        userAdapter = new UserAdapter(requireContext(), this);
        sheetAdapter = new SheetAdapter(requireContext(), this);

        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupTabs();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAdminAndLoadData();
    }

    private void setupTabs() {
        binding.tabLayoutAdmin.addTab(binding.tabLayoutAdmin.newTab().setText("Usuários Cadastrados"));
        binding.tabLayoutAdmin.addTab(binding.tabLayoutAdmin.newTab().setText("Fichas (Moderação)"));

        binding.tabLayoutAdmin.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingUsersTab = tab.getPosition() == 0;
                loadCurrentTabData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void checkAdminAndLoadData() {
        User user = repository.getCurrentUser();
        if (user != null && user.isAdmin()) {
            binding.layoutAdminContent.setVisibility(View.VISIBLE);
            binding.layoutAdminAccessDenied.setVisibility(View.GONE);
            loadCurrentTabData();
        } else {
            binding.layoutAdminContent.setVisibility(View.GONE);
            binding.layoutAdminAccessDenied.setVisibility(View.VISIBLE);
        }
    }

    private void loadCurrentTabData() {
        if (showingUsersTab) {
            binding.rvAdminList.setAdapter(userAdapter);
            List<User> users = repository.getAllUsers();
            userAdapter.setUsers(users);
        } else {
            binding.rvAdminList.setAdapter(sheetAdapter);
            List<CharacterSheet> sheets = repository.getAllSheetsForAdmin();
            sheetAdapter.setSheets(sheets);
        }
    }

    // User Actions
    @Override
    public void onToggleBlock(User user) {
        boolean newStatus = !user.isBlocked();
        repository.toggleBlockUser(user.getId(), user.isBlocked());
        Toast.makeText(requireContext(), "Usuário " + (newStatus ? "bloqueado" : "desbloqueado"), Toast.LENGTH_SHORT).show();
        loadCurrentTabData();
    }

    @Override
    public void onDeleteUser(User user) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remover Usuário")
                .setMessage("Deseja realmente remover o usuário " + user.getDisplayName() + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    repository.deleteUser(user.getId());
                    Toast.makeText(requireContext(), "Usuário removido.", Toast.LENGTH_SHORT).show();
                    loadCurrentTabData();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Sheet Actions
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
