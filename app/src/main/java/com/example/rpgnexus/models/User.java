package com.example.rpgnexus.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class User implements Serializable {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private String id;
    private String email;
    private String displayName;
    private String role;
    private boolean isBlocked;
    private boolean admin;

    public User() {
        this.role = ROLE_USER;
        this.isBlocked = false;
        this.admin = false;
    }

    public User(String id, String email, String displayName, String role, boolean isBlocked) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role != null ? role : ROLE_USER;
        this.isBlocked = isBlocked;
        this.admin = ROLE_ADMIN.equalsIgnoreCase(this.role) || "admin".equalsIgnoreCase(this.role);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        if (ROLE_ADMIN.equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role)) {
            this.admin = true;
        }
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @PropertyName("admin")
    public boolean isAdmin() {
        return admin || ROLE_ADMIN.equalsIgnoreCase(this.role) || "admin".equalsIgnoreCase(this.role);
    }

    @PropertyName("admin")
    public void setAdmin(boolean admin) {
        this.admin = admin;
        if (admin) {
            this.role = ROLE_ADMIN;
        }
    }
}
