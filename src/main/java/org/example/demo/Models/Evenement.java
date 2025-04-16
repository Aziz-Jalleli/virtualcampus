package org.example.demo.Models;
import java.time.LocalDateTime;

public class Evenement {
    private int id;
    private String titre;
    private String description;
    private int impactSatisfaction;
    private String impactRessource;
    private LocalDateTime dateEvent;
    private int campusId;

    // Default constructor
    public Evenement() {
    }

    // Constructor without ID (e.g., for inserting a new event)
    public Evenement(String titre, String description, int impactSatisfaction,
                     String impactRessource, LocalDateTime dateEvent, int campusId) {
        this.titre = titre;
        this.description = description;
        this.impactSatisfaction = impactSatisfaction;
        this.impactRessource = impactRessource;
        this.dateEvent = dateEvent;
        this.campusId = campusId;
    }

    // Full constructor with ID
    public Evenement(int id, String titre, String description, int impactSatisfaction,
                     String impactRessource, LocalDateTime dateEvent, int campusId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.impactSatisfaction = impactSatisfaction;
        this.impactRessource = impactRessource;
        this.dateEvent = dateEvent;
        this.campusId = campusId;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImpactSatisfaction() {
        return impactSatisfaction;
    }

    public void setImpactSatisfaction(int impactSatisfaction) {
        this.impactSatisfaction = impactSatisfaction;
    }

    public String getImpactRessource() {
        return impactRessource;
    }

    public void setImpactRessource(String impactRessource) {
        this.impactRessource = impactRessource;
    }

    public LocalDateTime getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDateTime dateEvent) {
        this.dateEvent = dateEvent;
    }

    public int getCampusId() {
        return campusId;
    }

    public void setCampusId(int campusId) {
        this.campusId = campusId;
    }
}

