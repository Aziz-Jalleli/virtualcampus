package model;

import Models.Personne;

public class Professeur extends Personne {
    private String matiere;
    private boolean disponible;

    public Professeur(String nom, String matiere, boolean disponible) {
        super(nom);
        this.matiere = matiere;
        this.disponible = disponible;
    }

    public void assisterCours() {
        if (disponible) {
            System.out.println(getNom() + " enseigne le cours de " + matiere);
        } else {
            System.out.println(getNom() + " n’est pas disponible pour enseigner actuellement.");
        }
    }

    public void consommerRessource(String ressource) {
        System.out.println(getNom() + " utilise la ressource : " + ressource);
    }

    @Override
    public void exprimerSatisfaction() {
        System.out.println(getNom() + " est satisfait de son emploi du temps.");
    }

    // Getters & Setters
    public String getMatiere() { return matiere; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
