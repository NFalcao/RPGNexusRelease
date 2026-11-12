package com.example.rpgnexus;

import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.models.User;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharacterSheetTest {

    @Test
    public void characterSheet_defaultIsPublic() {
        CharacterSheet sheet = new CharacterSheet();
        assertTrue(sheet.isPublic());
    }

    @Test
    public void user_adminRoleCheck() {
        User admin = new User("1", "admin@test.com", "Admin", User.ROLE_ADMIN, false);
        assertTrue(admin.isAdmin());

        User regular = new User("2", "user@test.com", "User", User.ROLE_USER, false);
        assertFalse(regular.isAdmin());
    }
}
