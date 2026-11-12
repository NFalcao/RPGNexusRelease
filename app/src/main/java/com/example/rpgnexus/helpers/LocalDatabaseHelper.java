package com.example.rpgnexus.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.rpgnexus.models.CharacterSheet;
import com.example.rpgnexus.models.User;

import java.util.ArrayList;
import java.util.List;

public class LocalDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "LocalDatabaseHelper";
    private static final String DATABASE_NAME = "rpgnexus.db";
    private static final int DATABASE_VERSION = 3; // Incremented version to reset DB schema & seed clean data

    // Table Users
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_NAME = "displayName";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_ROLE = "role";
    private static final String COL_USER_BLOCKED = "isBlocked";

    // Table Character Sheets
    private static final String TABLE_SHEETS = "character_sheets";
    private static final String COL_SHEET_ID = "id";
    private static final String COL_SHEET_OWNER_ID = "ownerId";
    private static final String COL_SHEET_OWNER_NAME = "ownerName";
    private static final String COL_SHEET_CHAR_NAME = "characterName";
    private static final String COL_SHEET_RPG_SYSTEM = "rpgSystem";
    private static final String COL_SHEET_GENDER = "gender";
    private static final String COL_SHEET_PLAY_STYLE = "playStyle";
    private static final String COL_SHEET_IMAGE_URL = "imageUrl";
    private static final String COL_SHEET_IS_PUBLIC = "isPublic";
    private static final String COL_SHEET_RESOURCES = "resources";
    private static final String COL_SHEET_ATTRIBUTES = "attributes";
    private static final String COL_SHEET_SKILLS = "skills";
    private static final String COL_SHEET_EQUIPMENT = "equipment";
    private static final String COL_SHEET_NOTES = "notes";
    private static final String COL_SHEET_CREATED_AT = "createdAt";

    public LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " TEXT PRIMARY KEY, " +
                    COL_USER_EMAIL + " TEXT UNIQUE, " +
                    COL_USER_NAME + " TEXT, " +
                    COL_USER_PASSWORD + " TEXT, " +
                    COL_USER_ROLE + " TEXT, " +
                    COL_USER_BLOCKED + " INTEGER)";

            String createSheetsTable = "CREATE TABLE " + TABLE_SHEETS + " (" +
                    COL_SHEET_ID + " TEXT PRIMARY KEY, " +
                    COL_SHEET_OWNER_ID + " TEXT, " +
                    COL_SHEET_OWNER_NAME + " TEXT, " +
                    COL_SHEET_CHAR_NAME + " TEXT, " +
                    COL_SHEET_RPG_SYSTEM + " TEXT, " +
                    COL_SHEET_GENDER + " TEXT, " +
                    COL_SHEET_PLAY_STYLE + " TEXT, " +
                    COL_SHEET_IMAGE_URL + " TEXT, " +
                    COL_SHEET_IS_PUBLIC + " INTEGER, " +
                    COL_SHEET_RESOURCES + " TEXT, " +
                    COL_SHEET_ATTRIBUTES + " TEXT, " +
                    COL_SHEET_SKILLS + " TEXT, " +
                    COL_SHEET_EQUIPMENT + " TEXT, " +
                    COL_SHEET_NOTES + " TEXT, " +
                    COL_SHEET_CREATED_AT + " INTEGER)";

            db.execSQL(createUsersTable);
            db.execSQL(createSheetsTable);

            // Populate initial Admin User & Default Sample Character Sheets
            seedInitialData(db);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHEETS);
            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Error in onUpgrade: " + e.getMessage(), e);
        }
    }

    public void resetDatabase() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            onUpgrade(db, 1, DATABASE_VERSION);
        } catch (Exception e) {
            Log.e(TAG, "Error resetting database: " + e.getMessage(), e);
        }
    }

    private void seedInitialData(SQLiteDatabase db) {
        try {
            // Admin user
            ContentValues adminVal = new ContentValues();
            adminVal.put(COL_USER_ID, "admin_id_001");
            adminVal.put(COL_USER_EMAIL, "admin@rpgnexus.com");
            adminVal.put(COL_USER_NAME, "Administrador RPG");
            adminVal.put(COL_USER_PASSWORD, "admin123");
            adminVal.put(COL_USER_ROLE, User.ROLE_ADMIN);
            adminVal.put(COL_USER_BLOCKED, 0);
            db.insert(TABLE_USERS, null, adminVal);

            // Regular user
            ContentValues userVal = new ContentValues();
            userVal.put(COL_USER_ID, "user_id_001");
            userVal.put(COL_USER_EMAIL, "mestre@rpgnexus.com");
            userVal.put(COL_USER_NAME, "Mestre Valeros");
            userVal.put(COL_USER_PASSWORD, "user123");
            userVal.put(COL_USER_ROLE, User.ROLE_USER);
            userVal.put(COL_USER_BLOCKED, 0);
            db.insert(TABLE_USERS, null, userVal);

            // Public Sheet 1: D&D 5e - Paladino
            ContentValues s1 = new ContentValues();
            s1.put(COL_SHEET_ID, "sheet_001");
            s1.put(COL_SHEET_OWNER_ID, "user_id_001");
            s1.put(COL_SHEET_OWNER_NAME, "Mestre Valeros");
            s1.put(COL_SHEET_CHAR_NAME, "Gildor Coração de Ferro");
            s1.put(COL_SHEET_RPG_SYSTEM, "Dungeons & Dragons 5e");
            s1.put(COL_SHEET_GENDER, "Masculino");
            s1.put(COL_SHEET_PLAY_STYLE, "Combate / Tanque");
            s1.put(COL_SHEET_IMAGE_URL, "");
            s1.put(COL_SHEET_IS_PUBLIC, 1);
            s1.put(COL_SHEET_RESOURCES, "PV: 45/45 | CA: 18 | Espaços de Magia Lvl 1: 3/3");
            s1.put(COL_SHEET_ATTRIBUTES, "FOR: 16 (+3) | DES: 10 (+0) | CON: 14 (+2) | INT: 8 (-1) | SAB: 12 (+1) | CAR: 16 (+3)");
            s1.put(COL_SHEET_SKILLS, "Atletismo, Persuasão, Religião, Intuição");
            s1.put(COL_SHEET_EQUIPMENT, "Espada Longa (+5 atingir, 1d8+3 dano), Cota de Malha, Escudo, Símbolo Sagrado");
            s1.put(COL_SHEET_NOTES, "Paladino da Ordem da Alvorada. Juramento de Devoção. Protege os fracos com seu escudo.");
            s1.put(COL_SHEET_CREATED_AT, System.currentTimeMillis() - 86400000);
            db.insert(TABLE_SHEETS, null, s1);

            // Public Sheet 2: Tormenta20 - Arcanista
            ContentValues s2 = new ContentValues();
            s2.put(COL_SHEET_ID, "sheet_002");
            s2.put(COL_SHEET_OWNER_ID, "admin_id_001");
            s2.put(COL_SHEET_OWNER_NAME, "Administrador RPG");
            s2.put(COL_SHEET_CHAR_NAME, "Elara Estrelamaris");
            s2.put(COL_SHEET_RPG_SYSTEM, "Tormenta20");
            s2.put(COL_SHEET_GENDER, "Feminino");
            s2.put(COL_SHEET_PLAY_STYLE, "Mágico / Dano em Área");
            s2.put(COL_SHEET_IMAGE_URL, "");
            s2.put(COL_SHEET_IS_PUBLIC, 1);
            s2.put(COL_SHEET_RESOURCES, "PV: 18/18 | PM: 25/25 | Defesa: 12");
            s2.put(COL_SHEET_ATTRIBUTES, "FOR: 8 (-1) | DES: 14 (+2) | CON: 12 (+1) | INT: 18 (+4) | SAB: 10 (+0) | CAR: 14 (+2)");
            s2.put(COL_SHEET_SKILLS, "Misticismo, Vontade, Ofício (Alquimia), Conhecimento");
            s2.put(COL_SHEET_EQUIPMENT, "Cajado Arcano, Kit de Alquimia, Manto do Convocador, Poções de Mana");
            s2.put(COL_SHEET_NOTES, "Especialista na magia Bola de Fogo e Seta Infalível de Talude.");
            s2.put(COL_SHEET_CREATED_AT, System.currentTimeMillis() - 43200000);
            db.insert(TABLE_SHEETS, null, s2);

            // Public Sheet 3: Call of Cthulhu - Investigador
            ContentValues s3 = new ContentValues();
            s3.put(COL_SHEET_ID, "sheet_003");
            s3.put(COL_SHEET_OWNER_ID, "user_id_001");
            s3.put(COL_SHEET_OWNER_NAME, "Mestre Valeros");
            s3.put(COL_SHEET_CHAR_NAME, "Prof. Thomas Wright");
            s3.put(COL_SHEET_RPG_SYSTEM, "Call of Cthulhu 7e");
            s3.put(COL_SHEET_GENDER, "Masculino");
            s3.put(COL_SHEET_PLAY_STYLE, "Investigação / Mistério");
            s3.put(COL_SHEET_IMAGE_URL, "");
            s3.put(COL_SHEET_IS_PUBLIC, 1);
            s3.put(COL_SHEET_RESOURCES, "PV: 11/11 | Sanidade: 55/55 | Sorte: 60");
            s3.put(COL_SHEET_ATTRIBUTES, "FOR: 45 | CON: 55 | TAM: 65 | DES: 50 | INT: 80 | POD: 65 | EDU: 85");
            s3.put(COL_SHEET_SKILLS, "Encontrar Biblioteca (75%), Arqueologia (70%), Idiomas (60%), Psicologia (50%)");
            s3.put(COL_SHEET_EQUIPMENT, "Bloco de Notas, Lupa, Revólver .38 (3 carregadores), Lanterna de Bolso");
            s3.put(COL_SHEET_NOTES, "Professor de Arqueologia na Universidade Miskatonic. Apaixonado por antiguidades egípcias.");
            s3.put(COL_SHEET_CREATED_AT, System.currentTimeMillis());
            db.insert(TABLE_SHEETS, null, s3);
        } catch (Exception e) {
            Log.e(TAG, "Error in seedInitialData: " + e.getMessage(), e);
        }
    }

    public boolean isDisplayNameExists(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) return false;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE LOWER(" + COL_USER_NAME + ")=LOWER(?)", new String[]{displayName.trim()});
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        } catch (Exception e) {
            return false;
        }
    }

    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE LOWER(" + COL_USER_EMAIL + ")=LOWER(?)", new String[]{email.trim()});
            User user = null;
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean saveUserOrReplace(User user) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Preserve existing local password if user is already present
            String existingPassword = "cloud_pass";
            Cursor cursor = db.rawQuery("SELECT " + COL_USER_PASSWORD + " FROM " + TABLE_USERS + " WHERE LOWER(" + COL_USER_EMAIL + ")=LOWER(?)", new String[]{user.getEmail().trim()});
            if (cursor.moveToFirst()) {
                existingPassword = cursor.getString(0);
            }
            cursor.close();

            db.delete(TABLE_USERS, "LOWER(" + COL_USER_EMAIL + ")=LOWER(?)", new String[]{user.getEmail().trim()});

            ContentValues cv = new ContentValues();
            cv.put(COL_USER_ID, user.getId());
            cv.put(COL_USER_EMAIL, user.getEmail());
            cv.put(COL_USER_NAME, user.getDisplayName());
            cv.put(COL_USER_PASSWORD, existingPassword);
            cv.put(COL_USER_ROLE, user.getRole());
            cv.put(COL_USER_BLOCKED, user.isBlocked() ? 1 : 0);
            long result = db.replace(TABLE_USERS, null, cv);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error saving user or replace: " + e.getMessage(), e);
            return false;
        }
    }

    // CRUD Methods for Users
    public boolean insertUser(User user, String password) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_USER_ID, user.getId());
            cv.put(COL_USER_EMAIL, user.getEmail());
            cv.put(COL_USER_NAME, user.getDisplayName());
            cv.put(COL_USER_PASSWORD, password);
            cv.put(COL_USER_ROLE, user.getRole());
            cv.put(COL_USER_BLOCKED, user.isBlocked() ? 1 : 0);
            long result = db.insert(TABLE_USERS, null, cv);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting user: " + e.getMessage(), e);
            return false;
        }
    }

    public User authenticate(String email, String password) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                    COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?", new String[]{email, password});
            User user = null;
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage(), e);
            return null;
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToUser(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateUserBlocked(String userId, boolean isBlocked) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_USER_BLOCKED, isBlocked ? 1 : 0);
            int updated = db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{userId});
            return updated > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user blocked status: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteUser(String userId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_SHEETS, COL_SHEET_OWNER_ID + "=?", new String[]{userId});
            int deleted = db.delete(TABLE_USERS, COL_USER_ID + "=?", new String[]{userId});
            return deleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
            return false;
        }
    }

    private User cursorToUser(Cursor c) {
        String id = c.getString(c.getColumnIndexOrThrow(COL_USER_ID));
        String email = c.getString(c.getColumnIndexOrThrow(COL_USER_EMAIL));
        String name = c.getString(c.getColumnIndexOrThrow(COL_USER_NAME));
        String role = c.getString(c.getColumnIndexOrThrow(COL_USER_ROLE));
        boolean blocked = c.getInt(c.getColumnIndexOrThrow(COL_USER_BLOCKED)) == 1;
        return new User(id, email, name, role, blocked);
    }

    // CRUD Methods for Character Sheets
    public boolean saveSheet(CharacterSheet sheet) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_SHEET_ID, sheet.getId());
            cv.put(COL_SHEET_OWNER_ID, sheet.getOwnerId());
            cv.put(COL_SHEET_OWNER_NAME, sheet.getOwnerName());
            cv.put(COL_SHEET_CHAR_NAME, sheet.getCharacterName());
            cv.put(COL_SHEET_RPG_SYSTEM, sheet.getRpgSystem());
            cv.put(COL_SHEET_GENDER, sheet.getGender());
            cv.put(COL_SHEET_PLAY_STYLE, sheet.getPlayStyle());
            cv.put(COL_SHEET_IMAGE_URL, sheet.getImageUrl());
            cv.put(COL_SHEET_IS_PUBLIC, sheet.isPublic() ? 1 : 0);
            cv.put(COL_SHEET_RESOURCES, sheet.getResources());
            cv.put(COL_SHEET_ATTRIBUTES, sheet.getAttributes());
            cv.put(COL_SHEET_SKILLS, sheet.getSkills());
            cv.put(COL_SHEET_EQUIPMENT, sheet.getEquipment());
            cv.put(COL_SHEET_NOTES, sheet.getNotes());
            cv.put(COL_SHEET_CREATED_AT, sheet.getCreatedAt());

            long result = db.replace(TABLE_SHEETS, null, cv);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error saving sheet: " + e.getMessage(), e);
            return false;
        }
    }

    public List<CharacterSheet> getPublicSheets(String query, String systemFilter, String genderFilter, String styleFilter) {
        List<CharacterSheet> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_SHEETS + " WHERE " + COL_SHEET_IS_PUBLIC + "=1");
            List<String> args = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                sql.append(" AND (").append(COL_SHEET_CHAR_NAME).append(" LIKE ? OR ").append(COL_SHEET_RPG_SYSTEM).append(" LIKE ?)");
                String qParam = "%" + query.trim() + "%";
                args.add(qParam);
                args.add(qParam);
            }
            if (systemFilter != null && !systemFilter.equals("Todos")) {
                sql.append(" AND ").append(COL_SHEET_RPG_SYSTEM).append("=?");
                args.add(systemFilter);
            }
            if (genderFilter != null && !genderFilter.equals("Todos")) {
                if (genderFilter.equalsIgnoreCase("Outro")) {
                    sql.append(" AND (").append(COL_SHEET_GENDER).append(" NOT IN ('Masculino', 'Feminino') OR ").append(COL_SHEET_GENDER).append(" IS NULL)");
                } else {
                    sql.append(" AND ").append(COL_SHEET_GENDER).append("=?");
                    args.add(genderFilter);
                }
            }
            if (styleFilter != null && !styleFilter.equals("Todos")) {
                sql.append(" AND ").append(COL_SHEET_PLAY_STYLE).append("=?");
                args.add(styleFilter);
            }

            sql.append(" ORDER BY ").append(COL_SHEET_CREATED_AT).append(" DESC");

            Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToSheet(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting public sheets: " + e.getMessage(), e);
        }
        return list;
    }

    public List<CharacterSheet> getUserSheets(String ownerId) {
        List<CharacterSheet> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SHEETS + " WHERE " + COL_SHEET_OWNER_ID + "=? ORDER BY " + COL_SHEET_CREATED_AT + " DESC", new String[]{ownerId});
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToSheet(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user sheets: " + e.getMessage(), e);
        }
        return list;
    }

    public List<CharacterSheet> getAllSheetsForAdmin() {
        List<CharacterSheet> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SHEETS + " ORDER BY " + COL_SHEET_CREATED_AT + " DESC", null);
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToSheet(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting admin sheets: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean deleteSheet(String sheetId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int rows = db.delete(TABLE_SHEETS, COL_SHEET_ID + "=?", new String[]{sheetId});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting sheet: " + e.getMessage(), e);
            return false;
        }
    }

    private CharacterSheet cursorToSheet(Cursor c) {
        String id = c.getString(c.getColumnIndexOrThrow(COL_SHEET_ID));
        String ownerId = c.getString(c.getColumnIndexOrThrow(COL_SHEET_OWNER_ID));
        String ownerName = c.getString(c.getColumnIndexOrThrow(COL_SHEET_OWNER_NAME));
        String charName = c.getString(c.getColumnIndexOrThrow(COL_SHEET_CHAR_NAME));
        String rpgSystem = c.getString(c.getColumnIndexOrThrow(COL_SHEET_RPG_SYSTEM));
        String gender = c.getString(c.getColumnIndexOrThrow(COL_SHEET_GENDER));
        String playStyle = c.getString(c.getColumnIndexOrThrow(COL_SHEET_PLAY_STYLE));
        String imageUrl = c.getString(c.getColumnIndexOrThrow(COL_SHEET_IMAGE_URL));
        boolean isPublic = c.getInt(c.getColumnIndexOrThrow(COL_SHEET_IS_PUBLIC)) == 1;
        String resources = c.getString(c.getColumnIndexOrThrow(COL_SHEET_RESOURCES));
        String attributes = c.getString(c.getColumnIndexOrThrow(COL_SHEET_ATTRIBUTES));
        String skills = c.getString(c.getColumnIndexOrThrow(COL_SHEET_SKILLS));
        String equipment = c.getString(c.getColumnIndexOrThrow(COL_SHEET_EQUIPMENT));
        String notes = c.getString(c.getColumnIndexOrThrow(COL_SHEET_NOTES));

        CharacterSheet sheet = new CharacterSheet(id, ownerId, ownerName, charName, rpgSystem, gender, playStyle, imageUrl, isPublic, resources, attributes, skills, equipment, notes);
        sheet.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(COL_SHEET_CREATED_AT)));
        return sheet;
    }
}
