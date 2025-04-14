package org.example.demo.Models;
public class Etudiant extends Models.Personne {
    private String filiere;
    private int heuresCours;
    private int satisfaction; // 0 à 100

    public Etudiant(String nom, String filiere, int heuresCours) {
        super(nom);
        this.filiere = filiere;
        this.heuresCours = heuresCours;
        this.satisfaction = 50; // valeur par défaut
    }

    public void assisterCours() {
        heuresCours += 2;
        satisfaction += 1;
        System.out.println(getNom() + " a assisté à un cours. Heures: " + heuresCours + ", Satisfaction: " + satisfaction);
    }

    public void consommerRessource(String ressource) {
        satisfaction += 2;
        System.out.println(getNom() + " a utilisé la ressource : " + ressource);
    }

    @Override
    public void exprimerSatisfaction() {
        System.out.println(this.satisfaction + " - Satisfaction actuelle : " + satisfaction + "/100");
    }

    // Getters & Setters
    public String getFiliere() { return filiere; }
    public int getHeuresCours() { return heuresCours; }
    public int getSatisfaction() { return satisfaction; }
}

