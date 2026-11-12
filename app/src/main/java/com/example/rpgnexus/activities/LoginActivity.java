package com.example.rpgnexus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rpgnexus.MainActivity;
import com.example.rpgnexus.databinding.ActivityLoginBinding;
import com.example.rpgnexus.repository.AppRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = AppRepository.getInstance(this);

        binding.btnLogin.setOnClickListener(v -> performLogin());
        binding.btnGuest.setOnClickListener(v -> enterAsGuest());
        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String email = binding.etLoginEmail.getText() != null ? binding.etLoginEmail.getText().toString().trim() : "";
        String password = binding.etLoginPassword.getText() != null ? binding.etLoginPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            binding.tilLoginEmail.setError("Informe o e-mail");
            return;
        } else {
            binding.tilLoginEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.tilLoginPassword.setError("Informe a senha");
            return;
        } else {
            binding.tilLoginPassword.setError(null);
        }

        repository.login(email, password, new AppRepository.OnLoginResultListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Bem-vindo, " + repository.getCurrentUser().getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }

            @Override
            public void onEmailNotVerified(FirebaseUser fUser, String unverifiedEmail) {
                new MaterialAlertDialogBuilder(LoginActivity.this)
                        .setTitle("E-mail Não Verificado")
                        .setMessage("O e-mail (" + unverifiedEmail + ") ainda não foi verificado. Por favor, acesse sua caixa de entrada e clique no link de confirmação para ativar sua conta.")
                        .setPositiveButton("Reenviar E-mail", (dialog, which) -> {
                            if (fUser != null) {
                                fUser.sendEmailVerification()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "E-mail de verificação reenviado para: " + unverifiedEmail, Toast.LENGTH_LONG).show())
                                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Erro ao reenviar e-mail: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enterAsGuest() {
        repository.loginGuest();
        Toast.makeText(this, "Modo Convidado ativado.", Toast.LENGTH_SHORT).show();
        startMainActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
