package org.example.demo.Models;

public class Batiment {
    private int id;
    private String nom;
    private String type;
    private double cons_res;
    private int satisfaction;
    public Batiment(int id, String nom, String type, double cons_res, double consRes, int satisfaction) {
        this.id=id;
        this.nom=nom;
        this.type=type;
        this.cons_res=cons_res;
        this.satisfaction=satisfaction;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getCons_res() {
        return cons_res;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public String getNom() {
        return nom;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCons_res(double cons_res) {
        this.cons_res = cons_res;
    }

    public void setSatisfaction(int satisfaction) {
        this.satisfaction = satisfaction;
    }
    public void utiliser() {
    }
    public String toString() {
        return "Bâtiment: " + nom + ", Adresse: " + type;
    }
}
