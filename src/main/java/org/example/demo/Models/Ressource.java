package org.example.demo.Models;
public class Ressource {
    private double wifi;
    private double electricite;
    private double eau;
    private double espace;

    public Ressource(double wifi, double electricite, double eau, double espace) {
        this.wifi = wifi;
        this.electricite = electricite;
        this.eau = eau;
        this.espace = espace;
    }

    public double calculerConsommation() {
        return wifi + electricite + eau + espace;
    }

    public void optimiserRessources() {
        wifi *= 0.95;
        electricite *= 0.90;
        eau *= 0.85;
        espace *= 0.92;
        System.out.println("Ressources optimisées.");
    }
    public double getWifi() { return wifi; }
    public double getElectricite() { return electricite; }
    public double getEau() { return eau; }
    public double getEspace() { return espace; }
}
