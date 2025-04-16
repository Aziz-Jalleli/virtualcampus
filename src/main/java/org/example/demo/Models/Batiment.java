package org.example.demo.Models;

public class Batiment {
    private int id;
    private String nom;
    private String type;
    private int Capacite;
    private int cons_res;
    private int satisfaction;
    private int gridX;
    private int gridZ;

    public Batiment(int id, String nom, String type, int Capacite, int cons_res, int satisfaction, int gridX, int gridZ) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.Capacite = Capacite;
        this.cons_res = cons_res;
        this.satisfaction = satisfaction;
        this.gridX = gridX;
        this.gridZ = gridZ;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getCons_res() {
        return cons_res;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public String getNom() {
        return nom;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridZ() {
        return gridZ;
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

    public void setCons_res(int cons_res) {
        this.cons_res = cons_res;
    }

    public void setSatisfaction(int satisfaction) {
        this.satisfaction = satisfaction;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridZ(int gridZ) {
        this.gridZ = gridZ;
    }

    public void utiliser() {
    }

    public String toString() {
        return "Bâtiment: " + nom + ", Type: " + type;
    }

    public int getCapacite() {

        return Capacite;
    }
}