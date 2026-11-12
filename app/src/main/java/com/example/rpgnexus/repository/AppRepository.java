package com.example.rpgnexus.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.rpgnexus.helpers.LocalDatabaseHelper;
import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.models.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AppRepository {
    private static final String TAG = "AppRepository";
    private static final String PREFS_NAME = "rpgnexus_session";
    private static final String KEY_USER_ID = "logged_user_id";
    private static final String KEY_IS_GUEST = "is_guest";

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SHEETS = "character_sheets";

    public interface OnAccountDeletedListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface OnAuthResultListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface OnLoginResultListener {
        void onSuccess();
        void onEmailNotVerified(FirebaseUser fUser, String email);
        void onError(String errorMessage);
    }

    public interface OnUserUpdatedListener {
        void onUserUpdated(User user);
    }

    private static AppRepository instance;
    private final LocalDatabaseHelper dbHelper;
    private final SharedPreferences prefs;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private User currentUser;
    private boolean isGuest;
    private OnUserUpdatedListener onUserUpdatedListener;

    private AppRepository(Context context) {
        dbHelper = new LocalDatabaseHelper(context.getApplicationContext());
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        try {
            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.w(TAG, "Firebase não inicializado no ambiente local. Usando armazenamento interno.", e);
        }

        isGuest = prefs.getBoolean(KEY_IS_GUEST, false);
        String savedUserId = prefs.getString(KEY_USER_ID, null);

        if (!isGuest && savedUserId != null) {
            try {
                List<User> users = dbHelper.getAllUsers();
                for (User u : users) {
                    if (u.getId().equals(savedUserId)) {
                        currentUser = u;
                        break;
                    }
                }
            } catch (Exception e) {
                currentUser = null;
            }
        }

        // Sync initial sheets from Firestore if available
        syncFromCloud();
    }

    public static synchronized AppRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppRepository(context);
        }
        return instance;
    }

    public void setOnUserUpdatedListener(OnUserUpdatedListener listener) {
        this.onUserUpdatedListener = listener;
    }

    private void notifyUserUpdated() {
        if (onUserUpdatedListener != null) {
            onUserUpdatedListener.onUserUpdated(currentUser);
        }
    }

    public boolean isGuest() {
        return isGuest || currentUser == null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void loginGuest() {
        this.isGuest = true;
        this.currentUser = null;
        if (mAuth != null) {
            try {
                mAuth.signOut();
            } catch (Exception ignored) {}
        }
        prefs.edit().putBoolean(KEY_IS_GUEST, true).remove(KEY_USER_ID).apply();
        notifyUserUpdated();
    }

    public boolean login(String email, String password) {
        return login(email, password, null);
    }

    public boolean login(String email, String password, OnLoginResultListener listener) {
        User user = dbHelper.authenticate(email, password);

        if (mAuth != null) {
            try {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser fUser = authResult.getUser();
                            if (fUser != null && !fUser.isEmailVerified() && !email.endsWith("@rpgnexus.com")) {
                                mAuth.signOut();
                                logout();
                                if (listener != null) {
                                    listener.onEmailNotVerified(fUser, email);
                                }
                            } else {
                                if (user != null) {
                                    if (user.isBlocked()) {
                                        if (listener != null) listener.onError("Sua conta está bloqueada pelo administrador.");
                                        return;
                                    }
                                    this.currentUser = user;
                                } else if (fUser != null) {
                                    User existingByEmail = dbHelper.findUserByEmail(email);
                                    if (existingByEmail != null) {
                                        this.currentUser = existingByEmail;
                                    } else {
                                        this.currentUser = new User(fUser.getUid(), email, fUser.getDisplayName() != null ? fUser.getDisplayName() : "Jogador", User.ROLE_USER, false);
                                        dbHelper.saveUserOrReplace(this.currentUser);
                                    }
                                }
                                this.isGuest = false;
                                prefs.edit().putBoolean(KEY_IS_GUEST, false).putString(KEY_USER_ID, this.currentUser != null ? this.currentUser.getId() : "").apply();
                                notifyUserUpdated();
                                if (fUser != null) {
                                    fetchUserFromCloud(fUser.getUid());
                                }
                                if (listener != null) {
                                    listener.onSuccess();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (user != null && !user.isBlocked()) {
                                this.currentUser = user;
                                this.isGuest = false;
                                prefs.edit().putBoolean(KEY_IS_GUEST, false).putString(KEY_USER_ID, user.getId()).apply();
                                notifyUserUpdated();
                                if (listener != null) {
                                    listener.onSuccess();
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("E-mail ou senha incorretos: " + e.getMessage());
                                }
                            }
                        });
            } catch (Exception e) {
                if (user != null && !user.isBlocked()) {
                    this.currentUser = user;
                    this.isGuest = false;
                    prefs.edit().putBoolean(KEY_IS_GUEST, false).putString(KEY_USER_ID, user.getId()).apply();
                    notifyUserUpdated();
                    if (listener != null) {
                        listener.onSuccess();
                    }
                } else if (listener != null) {
                    listener.onError("Erro de autenticação: " + e.getMessage());
                }
            }
            return true;
        } else {
            if (user != null) {
                if (user.isBlocked()) {
                    if (listener != null) listener.onError("Sua conta está bloqueada pelo administrador.");
                    return false;
                }
                this.currentUser = user;
                this.isGuest = false;
                prefs.edit().putBoolean(KEY_IS_GUEST, false).putString(KEY_USER_ID, user.getId()).apply();
                notifyUserUpdated();
                if (listener != null) {
                    listener.onSuccess();
                }
                return true;
            } else {
                if (listener != null) {
                    listener.onError("E-mail ou senha incorretos.");
                }
                return false;
            }
        }
    }

    public void register(String email, String password, String displayName, OnAuthResultListener listener) {
        // Enforce minimum 6 characters for password
        if (password == null || password.length() < 6) {
            if (listener != null) {
                listener.onError("A senha deve conter no mínimo 6 caracteres.");
            }
            return;
        }

        // Check for duplicate username
        if (dbHelper.isDisplayNameExists(displayName)) {
            if (listener != null) {
                listener.onError("Este Nome / Apelido de RPG já está sendo utilizado por outro jogador. Escolha um nome diferente.");
            }
            return;
        }

        if (mAuth != null) {
            try {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser fUser = authResult.getUser();
                            String uid = fUser != null ? fUser.getUid() : "user_" + UUID.randomUUID().toString();
                            User newUser = new User(uid, email, displayName, User.ROLE_USER, false);

                            // Insert into local DB and Firestore
                            dbHelper.saveUserOrReplace(newUser);
                            if (mFirestore != null) {
                                mFirestore.collection(COLLECTION_USERS)
                                        .document(uid)
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuário salvo com sucesso no Cloud Firestore"))
                                        .addOnFailureListener(e -> Log.e(TAG, "Erro ao salvar usuário no Firestore: " + e.getMessage(), e));
                            }

                            // Send Email Verification and Sign Out AFTER completion
                            if (fUser != null) {
                                fUser.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            mAuth.signOut();
                                            logout();
                                            if (listener != null) {
                                                listener.onSuccess("Conta criada com sucesso! Enviamos um e-mail de confirmação para " + email + ". Por favor, acesse seu e-mail e clique no link para ativar sua conta antes de fazer o login.");
                                            }
                                        });
                            } else {
                                logout();
                                if (listener != null) {
                                    listener.onSuccess("Conta criada com sucesso!");
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            logout();
                            if (listener != null) {
                                listener.onError("Erro ao criar conta no Firebase: " + e.getMessage());
                            }
                        });
            } catch (Exception e) {
                logout();
                if (listener != null) {
                    listener.onError("Erro ao registrar: " + e.getMessage());
                }
            }
        } else {
            String id = "user_" + UUID.randomUUID().toString();
            User newUser = new User(id, email, displayName, User.ROLE_USER, false);
            boolean success = dbHelper.saveUserOrReplace(newUser);
            logout();
            if (success) {
                if (listener != null) {
                    listener.onSuccess("Conta criada com sucesso no dispositivo! Faça login para continuar.");
                }
            } else {
                if (listener != null) {
                    listener.onError("Erro ao criar conta local.");
                }
            }
        }
    }

    public void logout() {
        this.currentUser = null;
        this.isGuest = true;
        if (mAuth != null) {
            try {
                mAuth.signOut();
            } catch (Exception ignored) {}
        }
        prefs.edit().clear().apply();
        notifyUserUpdated();
    }

    public void resetDatabase() {
        dbHelper.resetDatabase();
        logout();
    }

    public void deleteCurrentAccount(OnAccountDeletedListener listener) {
        if (currentUser == null) {
            if (listener != null) listener.onError("Nenhum usuário logado.");
            return;
        }

        String userId = currentUser.getId();

        // Delete user and user sheets
        deleteUser(userId);

        // Delete user from Firebase Auth
        FirebaseUser fUser = mAuth != null ? mAuth.getCurrentUser() : null;
        if (fUser != null) {
            try {
                fUser.delete().addOnCompleteListener(task -> {
                    logout();
                    if (listener != null) listener.onSuccess();
                });
            } catch (Exception e) {
                logout();
                if (listener != null) listener.onSuccess();
            }
        } else {
            logout();
            if (listener != null) listener.onSuccess();
        }
    }

    // Cloud Synchronization Methods
    public void syncFromCloud() {
        if (mFirestore == null) return;

        try {
            // Fetch public sheets matching security rules
            mFirestore.collection(COLLECTION_SHEETS)
                    .whereEqualTo("public", true)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Set<String> cloudSheetIds = new HashSet<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                CharacterSheet sheet = doc.toObject(CharacterSheet.class);
                                if (sheet != null) {
                                    dbHelper.saveSheet(sheet);
                                    cloudSheetIds.add(sheet.getId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao desserializar ficha do Firestore", e);
                            }
                        }

                        // Remove local sheets no longer in Cloud Firestore
                        try {
                            List<CharacterSheet> localAll = dbHelper.getAllSheetsForAdmin();
                            for (CharacterSheet s : localAll) {
                                if (!cloudSheetIds.contains(s.getId()) && s.getId().startsWith("sheet_")) {
                                    dbHelper.deleteSheet(s.getId());
                                }
                            }
                        } catch (Exception ignored) {}
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Erro na busca de fichas publicas: " + e.getMessage()));

            // Also fetch user's own sheets if logged in
            if (currentUser != null && currentUser.getId() != null) {
                mFirestore.collection(COLLECTION_SHEETS)
                        .whereEqualTo("ownerId", currentUser.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                try {
                                    CharacterSheet sheet = doc.toObject(CharacterSheet.class);
                                    if (sheet != null) {
                                        dbHelper.saveSheet(sheet);
                                    }
                                } catch (Exception ignored) {}
                            }
                        });
            }

            mFirestore.collection(COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Set<String> cloudUserIds = new HashSet<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                User u = doc.toObject(User.class);
                                if (u != null) {
                                    dbHelper.saveUserOrReplace(u);
                                    cloudUserIds.add(u.getId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao desserializar usuário do Firestore", e);
                            }
                        }

                        // Remove local users no longer in Cloud Firestore
                        try {
                            List<User> localUsers = dbHelper.getAllUsers();
                            for (User u : localUsers) {
                                if (!cloudUserIds.contains(u.getId()) && !u.getId().startsWith("admin_") && !u.getId().startsWith("user_id_")) {
                                    dbHelper.deleteUser(u.getId());
                                }
                            }
                        } catch (Exception ignored) {}
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erro na sincronização inicial do Cloud Firestore", e);
        }
    }

    private void fetchUserFromCloud(String uid) {
        if (mFirestore == null) return;

        // Try direct document ID lookup first
        mFirestore.collection(COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                this.currentUser = user;
                                dbHelper.saveUserOrReplace(user);
                                prefs.edit().putBoolean(KEY_IS_GUEST, false).putString(KEY_USER_ID, user.getId()).apply();
                                notifyUserUpdated();
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao ler usuario por ID do Firestore", e);
                        }
                    }

                    // Fallback search by email if document ID is different in Firestore
                    if (mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                        String email = mAuth.getCurrentUser().getEmail();
                        mFirestore.collection(COLLECTION_USERS).whereEqualTo("email", email).get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (QueryDocumentSnapshot doc : querySnapshot) {
                                        try {
                                            User u = doc.toObject(User.class);
                                            if (u != null) {
                                                this.currentUser = u;
                                                dbHelper.saveUserOrReplace(u);
                                                prefs.edit().putBoolean(KEY_IS_GUEST, false).putString(KEY_USER_ID, u.getId()).apply();
                                                notifyUserUpdated();
                                                break;
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Erro ao ler usuario por email do Firestore", e);
                                        }
                                    }
                                });
                    }
                });
    }

    // Sheets
    public List<CharacterSheet> getPublicSheets(String query, String systemFilter, String genderFilter, String styleFilter) {
        syncFromCloud();
        return dbHelper.getPublicSheets(query, systemFilter, genderFilter, styleFilter);
    }

    public List<CharacterSheet> getUserSheets() {
        if (currentUser == null) return java.util.Collections.emptyList();
        syncFromCloud();
        return dbHelper.getUserSheets(currentUser.getId());
    }

    public List<CharacterSheet> getAllSheetsForAdmin() {
        syncFromCloud();
        return dbHelper.getAllSheetsForAdmin();
    }

    public boolean saveSheet(CharacterSheet sheet) {
        if (sheet.getId() == null || sheet.getId().trim().isEmpty()) {
            sheet.setId("sheet_" + UUID.randomUUID().toString());
        }

        boolean localSuccess = dbHelper.saveSheet(sheet);

        // Upload local gallery image file to Firebase Storage if picked from device
        String imgUrl = sheet.getImageUrl();
        if (imgUrl != null && (imgUrl.startsWith("content://") || imgUrl.startsWith("file://"))) {
            try {
                Uri localUri = Uri.parse(imgUrl);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("character_images/" + sheet.getId() + ".jpg");

                storageRef.putFile(localUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                String httpsUrl = downloadUri.toString();
                                sheet.setImageUrl(httpsUrl);
                                dbHelper.saveSheet(sheet);
                                if (mFirestore != null) {
                                    mFirestore.collection(COLLECTION_SHEETS)
                                            .document(sheet.getId())
                                            .set(sheet);
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erro ao enviar imagem da ficha para o Firebase Storage", e);
                            if (mFirestore != null) {
                                mFirestore.collection(COLLECTION_SHEETS)
                                        .document(sheet.getId())
                                        .set(sheet);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Erro no upload da imagem para Firebase Storage", e);
                if (mFirestore != null) {
                    mFirestore.collection(COLLECTION_SHEETS)
                            .document(sheet.getId())
                            .set(sheet);
                }
            }
        } else {
            // Upload directly to Cloud Firestore if HTTPS URL or empty
            if (mFirestore != null) {
                try {
                    mFirestore.collection(COLLECTION_SHEETS)
                            .document(sheet.getId())
                            .set(sheet)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Ficha salva com sucesso no Cloud Firestore"))
                            .addOnFailureListener(e -> Log.e(TAG, "Erro ao salvar ficha no Cloud Firestore", e));
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao chamar Firestore.set()", e);
                }
            }
        }

        return localSuccess;
    }

    public boolean deleteSheet(String sheetId) {
        boolean localSuccess = dbHelper.deleteSheet(sheetId);

        // Delete from Cloud Firestore
        if (mFirestore != null) {
            try {
                mFirestore.collection(COLLECTION_SHEETS)
                        .document(sheetId)
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Ficha removida do Cloud Firestore"))
                        .addOnFailureListener(e -> Log.e(TAG, "Erro ao remover ficha do Cloud Firestore", e));
            } catch (Exception e) {
                Log.e(TAG, "Erro ao chamar Firestore.delete()", e);
            }
        }

        return localSuccess;
    }

    // Admin Users Management
    public List<User> getAllUsers() {
        return dbHelper.getAllUsers();
    }

    public boolean toggleBlockUser(String userId, boolean currentStatus) {
        boolean localSuccess = dbHelper.updateUserBlocked(userId, !currentStatus);

        if (mFirestore != null) {
            try {
                mFirestore.collection(COLLECTION_USERS)
                        .document(userId)
                        .update("blocked", !currentStatus);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao atualizar status de bloqueio no Firestore", e);
            }
        }

        return localSuccess;
    }

    public boolean deleteUser(String userId) {
        // First delete all sheets owned by this user locally and on cloud
        try {
            List<CharacterSheet> sheets = dbHelper.getUserSheets(userId);
            for (CharacterSheet s : sheets) {
                deleteSheet(s.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao apagar fichas do usuário excluído", e);
        }

        boolean localSuccess = dbHelper.deleteUser(userId);

        if (mFirestore != null) {
            try {
                mFirestore.collection(COLLECTION_USERS)
                        .document(userId)
                        .delete();

                // Also delete all sheets from Firestore belonging to this user
                mFirestore.collection(COLLECTION_SHEETS)
                        .whereEqualTo("ownerId", userId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                mFirestore.collection(COLLECTION_SHEETS).document(doc.getId()).delete();
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Erro ao deletar usuário no Firestore", e);
            }
        }

        return localSuccess;
    }
}
