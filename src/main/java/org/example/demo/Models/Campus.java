package org.example.demo.Models;
import org.example.demo.Models.UserSession;

import java.util.ArrayList;
import java.util.List;

public class Campus {
    private static Campus instance;
    private int id;
    private String nom;
    private int userId;
    private int satisfaction;

    public void setSatisfaction(int satisfaction) {
        this.satisfaction = satisfaction;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    private List<Batiment> batiments = new ArrayList<>();
    private List<Evenement> evenements = new ArrayList<>();
    private Ressource ressources;
    public Campus() {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            this.userId = session.getUserId();
        }
    }

    public Campus(String nom) {
        this.nom = nom;
        UserSession session = UserSession.getInstance();
        if (session != null) {
            this.userId = session.getUserId();
        }
    }
    public Campus(int id, String nom, int userId) {
        this.id = id;
        this.nom = nom;
        this.userId = userId;
    }

    public Campus(int id) {
        this.id = id;
    }

    public Campus(int id, String nom) {
        this.id = id;
        this.nom = nom;

    }

    public static void createInstance(int id,String nom, int userId) {
        instance = new Campus(id,nom,userId);
    }
    public static Campus getInstance() {
        return instance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getUserId() {

        return userId;
    }

    // Optional: override setUserId only if you want to manually allow setting it
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Batiment> getBatiments() {
        return batiments;
    }

    public void setBatiments(List<Batiment> batiments) {
        this.batiments = batiments;
    }

    public List<Evenement> getEvenements() {
        return evenements;
    }

    public void setEvenements(List<Evenement> evenements) {
        this.evenements = evenements;
    }

    public Ressource getRessources() {
        return ressources;
    }

    public void setRessources(Ressource ressources) {
        this.ressources = ressources;
    }
    @Override
    public String toString() {
        return nom; // ou toute autre propriété comme nom + ville, etc.
    }


}
