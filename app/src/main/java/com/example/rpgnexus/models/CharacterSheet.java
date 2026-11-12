package com.example.rpgnexus.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class CharacterSheet implements Serializable {
    private String id;
    private String ownerId;
    private String ownerName;
    private String characterName;
    private String rpgSystem;
    private String gender;
    private String playStyle;
    private String imageUrl;

    @PropertyName("isPublic")
    private boolean isPublic;

    private String resources;   // ex: HP: 20/20, MP: 10/10
    private String attributes;  // ex: Força: 16, Destreza: 14, Constituição: 12
    private String skills;      // ex: Atletismo, Percepção, Furtividade
    private String equipment;   // ex: Espada Longa, Poção de Cura (x2)
    private String notes;       // Anotações gerais

    private long createdAt;

    public CharacterSheet() {
        this.isPublic = true;
        this.createdAt = System.currentTimeMillis();
    }

    public CharacterSheet(String id, String ownerId, String ownerName, String characterName,
                          String rpgSystem, String gender, String playStyle, String imageUrl,
                          boolean isPublic, String resources, String attributes,
                          String skills, String equipment, String notes) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.characterName = characterName;
        this.rpgSystem = rpgSystem;
        this.gender = gender;
        this.playStyle = playStyle;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.resources = resources;
        this.attributes = attributes;
        this.skills = skills;
        this.equipment = equipment;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getRpgSystem() {
        return rpgSystem;
    }

    public void setRpgSystem(String rpgSystem) {
        this.rpgSystem = rpgSystem;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPlayStyle() {
        return playStyle;
    }

    public void setPlayStyle(String playStyle) {
        this.playStyle = playStyle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("isPublic")
    public boolean isPublic() {
        return isPublic;
    }

    @PropertyName("isPublic")
    public void setPublic(boolean aPublic) {
        this.isPublic = aPublic;
    }

    @PropertyName("public")
    public boolean getPublic() {
        return isPublic;
    }

    @PropertyName("public")
    public void setPublicProp(boolean aPublic) {
        this.isPublic = aPublic;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
